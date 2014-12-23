package be.nabu.libs.channels.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;

import be.nabu.libs.channels.util.ChannelManagerConfiguration;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.xml.XMLBinding;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.java.BeanType;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class ChannelResourceConfiguration {
	
	private XMLBinding binding;
	private Charset charset = Charset.forName("UTF-8");
	
	public void marshal(WritableResource resource, ChannelManagerConfiguration configuration) throws IOException {
		WritableContainer<ByteBuffer> output = resource.getWritable();
		try {
			getBinding().marshal(IOUtils.toOutputStream(output), new BeanInstance<ChannelManagerConfiguration>(configuration));
		}
		finally {
			output.close();
		}
	}
	
	public ChannelManagerConfiguration unmarshal(ReadableResource resource) throws IOException, ParseException {
		ReadableContainer<ByteBuffer> readable = resource.getReadable();
		try {
			return unmarshal(IOUtils.toInputStream(readable));
		}
		finally {
			readable.close();
		}
	}

	public ChannelManagerConfiguration unmarshal(InputStream input) throws IOException, ParseException {
		return TypeUtils.getAsBean(getBinding().unmarshal(input, new Window[0]), ChannelManagerConfiguration.class);
	}
	
	private XMLBinding getBinding() {
		if (binding == null) {
			synchronized(this) {
				if (binding == null) {
					binding = new XMLBinding(new BeanType<ChannelManagerConfiguration>(ChannelManagerConfiguration.class), charset);
				}
			}
		}
		return binding;
	}
}
