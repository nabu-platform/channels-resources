package be.nabu.libs.channels.resources.simple;

import java.security.Principal;

import be.nabu.libs.channels.resources.FileOutProperties;

public class SimpleFileOutProperties implements FileOutProperties {
	
	private String directoryPath, fileName;
	private Boolean overwriteIfExists;
	private Principal principal;
	
	@Override
	public String getDirectoryPath() {
		return directoryPath;
	}
	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}
	@Override
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	@Override
	public Boolean getOverwriteIfExists() {
		return overwriteIfExists;
	}
	public void setOverwriteIfExists(Boolean overwriteIfExists) {
		this.overwriteIfExists = overwriteIfExists;
	}
	@Override
	public Principal getPrincipal() {
		return principal;
	}
	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}
}
