package be.nabu.libs.channels.resources.simple;

import be.nabu.libs.channels.resources.DirectoryInProperties;

public class SimpleDirectoryInProperties extends BaseResourceInProperties implements DirectoryInProperties {

	private Boolean recursive;
	private String directoryRegex;
	private String fileRegex;

	@Override
	public String getFileRegex() {
		return fileRegex;
	}

	@Override
	public String getDirectoryRegex() {
		return directoryRegex;
	}

	@Override
	public Boolean getRecursive() {
		return recursive;
	}

	public void setRecursive(Boolean recursive) {
		this.recursive = recursive;
	}

	public void setDirectoryRegex(String directoryRegex) {
		this.directoryRegex = directoryRegex;
	}

	public void setFileRegex(String fileRegex) {
		this.fileRegex = fileRegex;
	}
}
