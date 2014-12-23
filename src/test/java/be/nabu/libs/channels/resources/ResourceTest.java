package be.nabu.libs.channels.resources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TimeZone;

import javax.sql.DataSource;

import be.nabu.libs.channels.ChannelUtils;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.SingleChannelResultHandler;
import be.nabu.libs.channels.resources.simple.SimpleDirectoryInProperties;
import be.nabu.libs.datastore.resources.ResourceDatastore;
import be.nabu.libs.datastore.resources.base.DataRouterBase;
import be.nabu.libs.datastore.urn.DatabaseURNManager;
import be.nabu.libs.datatransactions.api.DataTransaction;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.DataTransactionProvider;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.ProviderResolver;
import be.nabu.libs.datatransactions.api.Transactionality;
import be.nabu.libs.datatransactions.database.DatabaseTransactionProvider;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.ResourceWritableContainer;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;

/**
 * For this test case the first pick up is slow due to lazy jdbc pool startup: ~250ms
 * The second and third pickup though were in the range of ~15ms as calculated from datatransaction entries
 */
public class ResourceTest {
	
	private static String id;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void test(DataSource dataSource) throws IOException, URISyntaxException, ChannelException {
		// put some files into memory
		ManageableContainer<?> directory = (ManageableContainer<?>) ResourceUtils.mkdir(new URI("memory:/test"), null);
		Resource test1 = directory.create("test1.txt", "text/plain");
		IOUtils.copyBytes(IOUtils.wrap("test".getBytes(), true), new ResourceWritableContainer((WritableResource) test1));
		
		Resource test2 = directory.create("test2.xml", "application/xml");
		IOUtils.copyBytes(IOUtils.wrap("<test/>".getBytes(), true), new ResourceWritableContainer((WritableResource) test2));
		
		Resource test3 = directory.create("test3.html", "text/html");
		IOUtils.copyBytes(IOUtils.wrap("<html><test/></html>".getBytes(), true), new ResourceWritableContainer((WritableResource) test3));
		
		ChannelProvider<DirectoryInProperties> channelProvider = new DirectoryInProvider();
		SimpleDirectoryInProperties properties = new SimpleDirectoryInProperties();
		properties.setUri("memory:/");
		properties.setProcessedDirectory("processed");
//		properties.setFileRegex(".*\\.txt");
		properties.setFileRegex(".*");
		properties.setDirectoryRegex(".*");
		properties.setRecursive(true);
		
		DataTransactionProvider dataTransactionProvider = new DatabaseTransactionProvider(dataSource, TimeZone.getDefault());
		ResourceDatastore datastore = new ResourceDatastore(new DataRouterBase(new URI("memory:/datastore")));
		datastore.setUrnManager(new DatabaseURNManager(dataSource, TimeZone.getDefault(), "com.example"));
		
		channelProvider.transact(
			properties, 
			datastore,
			ChannelUtils.manage(dataTransactionProvider.newBatch(new EmptyProviderResolver(), "context", "localhost", null, Direction.IN, Transactionality.THREE_PHASE),
//					new TemporaryResultHandler())
					ChannelUtils.newChannelResultHandler(new TemporaryDataTransactionhandler()))
		);
		
		String content = new String(IOUtils.toBytes(new ResourceReadableContainer((ReadableResource) ResourceFactory.getInstance().resolve(new URI("memory:/processed/test1.txt"), null))));
		System.out.println("Moved content = " + content);
		
		System.out.println("Original resource: " + directory.getChild("test1.txt"));
		
		// get the transaction again
		System.out.println(dataTransactionProvider.getTransaction(id));
		
		System.exit(0);
	}
	
	public static class TemporaryDataTransactionhandler implements SingleChannelResultHandler {
		@Override
		public void handle(DataTransaction<?> transaction) throws ChannelException {
			id = transaction.getId();
		}
	}
	
	public static class TemporaryResultHandler implements ChannelResultHandler {
		@Override
		public void handle(DataTransactionHandle...transaction) {
			System.out.println("PUSHED " + transaction.length + " transactions");
			id = transaction[0].getTransaction().getId();
			System.out.println("Received data: " + transaction[0].getTransaction().getResponse());
		}
	}
	
	public static class EmptyProviderResolver implements ProviderResolver<ChannelProvider<?>> {

		@Override
		public String getId(ChannelProvider<?> provider) {
			return "test";
		}

		@Override
		public ChannelProvider<?> getProvider(String id) {
			return null;
		}
	}
}
