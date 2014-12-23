package be.nabu.libs.channels.resources.simple;

import be.nabu.libs.channels.resources.FileInProperties;

public class SimpleFileInProperties extends BaseResourceInProperties implements FileInProperties {

	private String name, actualType;

	@Override
	public String getActualType() {
		return actualType;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setActualType(String actualType) {
		this.actualType = actualType;
	}
}
