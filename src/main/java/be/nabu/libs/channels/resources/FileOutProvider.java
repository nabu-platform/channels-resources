package be.nabu.libs.channels.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.datastore.api.DataProperties;
import be.nabu.libs.datastore.api.WritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;

public class FileOutProvider implements ChannelProvider<FileOutProperties> {

	@Override
	public void transact(FileOutProperties properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionBatch, URI...requests) throws ChannelException {
		ChannelException exception = null;
		ManageableContainer<?> container = null;
		try {
			container = (ManageableContainer<?>) ResourceUtils.mkdir(new URI(URIUtils.encodeURI(properties.getDirectoryPath())), properties.getPrincipal());
		}
		catch (IOException e) {
			throw new ChannelException(e);
		}
		catch (URISyntaxException e) {
			throw new ChannelException(e);
		}
		for (URI request : requests) {
			try {
				DataTransactionHandle handle = transactionBatch.start(this, properties, request);
				try {
					Resource target = null;
					DataProperties dataProperties = datastore.getProperties(request);
					String targetFileName = properties.getFileName() == null ? dataProperties.getName() : properties.getFileName();
					target = container.getChild(targetFileName);
					if (target != null && Boolean.FALSE.equals(properties.getOverwriteIfExists())) {
						throw new IOException("The target resource " + ResourceUtils.getURI(target) + " already exists");
					}
					if (target == null) {
						target = container.create(targetFileName, dataProperties.getContentType());
					}
					if (!(target instanceof WritableResource)) {
						throw new IOException("The target resource " + ResourceUtils.getURI(target) + " is not writable");
					}
					InputStream input = datastore.retrieve(request);
					try {
						WritableContainer<ByteBuffer> output = ((WritableResource) target).getWritable();
						try {
							IOUtils.copyBytes(IOUtils.wrap(input), output);
						}
						finally {
							output.close();
						}
					}
					finally {
						input.close();
					}
					handle.commit(null);
				}
				catch (IOException e) {
					handle.fail(e.getMessage());
				}
			}
			catch (IOException e) {
				if (exception == null) {
					exception = new ChannelException(e);
				}
				else {
					exception.addSuppressedException(e);
				}
			}
		}
		if (exception != null) {
			throw exception;
		}
	}

	@Override
	public Direction getDirection() {
		return Direction.OUT;
	}

	@Override
	public Class<FileOutProperties> getPropertyClass() {
		return FileOutProperties.class;
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
