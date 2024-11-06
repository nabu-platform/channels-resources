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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.resources.simple.SimpleDirectoryInProperties;
import be.nabu.libs.channels.util.ChannelManagerConfiguration;
import be.nabu.libs.channels.util.ChannelManagerConfiguration.ChannelImpl;
import be.nabu.libs.channels.util.ChannelManagerConfiguration.ChannelProviderConfiguration;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;

public class TestConfiguration {
	public static void main(String...args) throws IOException, URISyntaxException, ParseException {
		Resource resource = ResourceUtils.touch(new URI("memory:/testing.xml"), null);
		ChannelResourceConfiguration handler = new ChannelResourceConfiguration();
		ChannelManagerConfiguration configuration = new ChannelManagerConfiguration();
		ChannelProviderConfiguration<Object> providerConfiguration = new ChannelProviderConfiguration<Object>();
		providerConfiguration.setName("file+dir");
		providerConfiguration.setProviderClass(DirectoryInProvider.class.getName());
		configuration.getProviders().add(providerConfiguration);
		ChannelImpl<DirectoryInProperties> channel = new ChannelImpl<DirectoryInProperties>();
		channel.setContext("someContext");
		channel.setDirection(Direction.IN);
		channel.setProviderId(providerConfiguration.getName());
		SimpleDirectoryInProperties properties = new SimpleDirectoryInProperties();
		properties.setDeleteOriginal(true);
		properties.setUri("memory:/somewhere");
		properties.setFileRegex(".*\\.txt");
		channel.setProperties(properties);
		configuration.getChannels().add(channel);
		handler.marshal((WritableResource) resource, configuration);
		
		ReadableContainer<ByteBuffer> readable = ((ReadableResource) resource).getReadable();
		System.out.println(new String(IOUtils.toBytes(readable)));
		
		ChannelManagerConfiguration unmarshalled = handler.unmarshal((ReadableResource) resource);
		for (Channel<?> unmarshalledChannel : unmarshalled.getChannels()) {
			System.out.println(unmarshalledChannel.getProviderId());
			System.out.println("\t" + unmarshalledChannel.getDirection());
			System.out.println("\t" + unmarshalledChannel.getProperties());
		}
	}
}
