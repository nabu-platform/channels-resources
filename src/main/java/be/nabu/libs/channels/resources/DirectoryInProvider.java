package be.nabu.libs.channels.resources;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.libs.authentication.impl.AuthenticationUtils;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.resources.simple.SimpleFileInProperties;
import be.nabu.libs.datastore.api.WritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;

public class DirectoryInProvider implements ChannelProvider<DirectoryInProperties> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void transact(DirectoryInProperties properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionProvider, URI...requests) throws ChannelException {
		try {
			Resource resource = ResourceFactory.getInstance().resolve(new URI(URIUtils.encodeURI(properties.getUri())), AuthenticationUtils.toPrincipal(properties.getUsername(), properties.getPassword()));
			if (resource != null) {
				try {
					if (!(resource instanceof ResourceContainer)) {
						throw new ChannelException("The resource " + properties.getUri() + " is not a directory");
					}
					scan(properties, datastore, transactionProvider, (ResourceContainer<?>) resource, null);
				}
				finally {
					if (resource instanceof Closeable) {
						try {
							((Closeable) resource).close();
						}
						catch (IOException e) {
							logger.error("Could not close " + properties.getUri(), e);
						}
					}
				}
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

	private void scan(DirectoryInProperties properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionProvider, ResourceContainer<?> container, String path) throws ChannelException, IOException {
		List<Resource> children = new ArrayList<Resource>();
		// make a new list, the original container will be modified when a file in provider is set to delete the files triggering a concurrentmodificationexception
		for (Resource child : container) {
			children.add(child);
		}
		for (Resource child : children) {
			if (child instanceof ReadableResource && (properties.getFileRegex() == null || child.getName().matches(properties.getFileRegex()))) {
				String processedDirectory = null;
				if (properties.getProcessedDirectory() != null) {
					processedDirectory = path == null ? properties.getProcessedDirectory() : path.replaceAll("[^/]+", "..") + "/" + properties.getProcessedDirectory();
				}
				SimpleFileInProperties fileProperties = new SimpleFileInProperties();
				fileProperties.setUri(URIUtils.decodeURI(ResourceUtils.getURI(child).toString()));
				fileProperties.setUsername(properties.getUsername());
				fileProperties.setPassword(properties.getPassword());
				fileProperties.setDeleteOriginal(properties.getDeleteOriginal());
				fileProperties.setProcessedDirectory(processedDirectory);
				fileProperties.setProcessedExtension(properties.getProcessedExtension());
				FileInProvider fileProvider = new FileInProvider();
				fileProvider.transact(fileProperties, datastore, transactionProvider, child, false);
			}
			if (Boolean.TRUE.equals(properties.getRecursive()) && child instanceof ResourceContainer && (properties.getDirectoryRegex() == null || child.getName().matches(properties.getDirectoryRegex()))) {
				scan(properties, datastore, transactionProvider, (ResourceContainer<?>) child, path == null ? child.getName() : path + "/" + child.getName());
			}
		}
	}

	@Override
	public Direction getDirection() {
		return Direction.IN;
	}

	@Override
	public Class<DirectoryInProperties> getPropertyClass() {
		return DirectoryInProperties.class;
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof DirectoryInProvider;
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
