/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.channels.resources;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.libs.authentication.impl.AuthenticationUtils;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.TwoPhaseChannelProvider;
import be.nabu.libs.datastore.api.WritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

/**
 * This class is _not_ thread safe
 */
public class FileInProvider implements TwoPhaseChannelProvider<FileInProperties> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The resources is kept for the two phases to prevent double lookup
	 */
	private Resource resource;
	
	/**
	 * Whether we should close the resource in the second phase
	 */
	private boolean close = true;
	
	@Override
	public void transact(FileInProperties properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionBatch, URI...requests) throws ChannelException {
		try {
			Resource resource = ResourceFactory.getInstance().resolve(new URI(URIUtils.encodeURI(properties.getUri())), AuthenticationUtils.toPrincipal(properties.getUsername(), properties.getPassword()));
			if (resource != null) {
				transact(properties, datastore, transactionBatch, resource, true);
			}
			else if (Boolean.TRUE.equals(properties.getMustExist())) {
				throw new ChannelException("The file " + properties.getUri() + " does not exist");
			}
		}
		catch (IOException e) {
			throw new ChannelException(e);
		}
		catch (URISyntaxException e) {
			throw new ChannelException(e);
		}
	}

	void transact(FileInProperties properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionBatch, Resource resource, boolean close) throws ChannelException, IOException {
		// throw an explicit exception to prevent accidental wrong behavior
		if (this.resource != null) {
			throw new ChannelException("This class is not thread safe");
		}
		
		this.resource = resource;
		this.close = close;

		logger.debug("Picking up file: {}", properties.getUri());
		DataTransactionHandle transactionHandle = transactionBatch.start(this, properties, null);
		try {
			if (!(resource instanceof ReadableResource)) {
				throw new ChannelException("The resource " + properties.getUri() + " is not readable");
			}
			InputStream input = new BufferedInputStream(IOUtils.toInputStream(((ReadableResource) resource).getReadable()));
			try {
				URI uri = datastore.store(
					input, 
					properties.getName() == null ? resource.getName() : properties.getName(), 
					properties.getActualType() == null ? URLConnection.guessContentTypeFromName(resource.getName()) : properties.getActualType()
				);
				transactionHandle.commit(uri);
			}
			finally {
				input.close();
			}
		}
		catch (IOException e) {
			logger.error("Could not pick up file " + properties.getUri(), e);
			transactionHandle.fail("Could not pick up file " + properties.getUri() + ": " + e.getMessage());
		}
	}

	@Override
	public Direction getDirection() {
		return Direction.IN;
	}

	@Override
	public Class<FileInProperties> getPropertyClass() {
		return FileInProperties.class;
	}

	@Override
	public void finish(FileInProperties properties) throws ChannelException {
		try {
			boolean hasMoved = false;
			if (properties.getProcessedExtension() != null || properties.getProcessedDirectory() != null) {
				if (resource == null) {
					resource = ResourceFactory.getInstance().resolve(new URI(URIUtils.encodeURI(properties.getUri())), AuthenticationUtils.toPrincipal(properties.getUsername(), properties.getPassword()));
				}
				if (resource != null) {
					String targetName = properties.getProcessedExtension() == null ? resource.getName() : resource.getName().replaceAll("\\.([^.]+)$", properties.getProcessedExtension());
					Resource target = properties.getProcessedDirectory() == null ? resource.getParent() : ResourceUtils.resolve(resource.getParent(), properties.getProcessedDirectory());
					if (target == null) {
						if (properties.getProcessedDirectory() != null) {
							target = ResourceUtils.mkdirs(resource.getParent(), properties.getProcessedDirectory());
						}
						else {
							throw new ChannelException("The parent directory is unreachable");
						}
					}
					if (!(target instanceof ManageableContainer<?>)) {
						throw new ChannelException("The target directory " + ResourceUtils.getURI(target) + " is not manageable");
					}
					else if (!(resource.getParent() instanceof ManageableContainer<?>)) {
						throw new ChannelException("The parent directory " + ResourceUtils.getURI(resource.getParent()) + " is not manageable");
					}
					ManageableContainer<?> targetDirectory = (ManageableContainer<?>) target;
					if (targetDirectory.getChild(targetName) != null) {
						throw new ChannelException("Can not move the child to " + ResourceUtils.getURI(target) + " because a file with name " + targetName + " already exists");
					}
					Resource targetResource = targetDirectory.create(targetName, resource.getContentType());
					if (!(targetResource instanceof WritableResource)) {
						throw new ChannelException("Can not write to the target " + targetName + " in directory " + ResourceUtils.getURI(target));
					}
					WritableContainer<ByteBuffer> writable = IOUtils.bufferWritable(((WritableResource) targetResource).getWritable(), IOUtils.newByteBuffer(4096*16, true));
					try {
						ReadableContainer<ByteBuffer> readable = IOUtils.bufferReadable(((ReadableResource) resource).getReadable(), IOUtils.newByteBuffer(4096*16, true));
						try {
							IOUtils.copyBytes(readable, writable);
							hasMoved = true;
						}
						finally {
							readable.close();
						}
					}
					finally {
						writable.close();
					}
				}
			}
			if (hasMoved || Boolean.TRUE.equals(properties.getDeleteOriginal())) {
				if (this.resource == null) {
					this.resource = ResourceFactory.getInstance().resolve(new URI(URIUtils.encodeURI(properties.getUri())), AuthenticationUtils.toPrincipal(properties.getUsername(), properties.getPassword()));
				}
				if (resource != null) {
					if (!(resource.getParent() instanceof ManageableContainer)) {
						throw new ChannelException("We can not delete " + properties.getUri() + " as the parent is not a manageable container");
					}
					String name = resource.getName();
					ManageableContainer<?> parent = (ManageableContainer<?>) resource.getParent();
					parent.delete(name);
					if (parent.getChild(name) != null) {
						throw new ChannelException("Could not remove the file " + properties.getUri());
					}
				}
			}
		}
		catch (IOException e) {
			throw new ChannelException(e);
		}
		catch (URISyntaxException e) {
			throw new ChannelException(e);
		}
		finally {
			if (close && resource instanceof Closeable) {
				try {
					((Closeable) resource).close();
				}
				catch (IOException e) {
					logger.error("Could not close " + properties.getUri(), e);
				}
			}
		}
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof FileInProvider;
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
