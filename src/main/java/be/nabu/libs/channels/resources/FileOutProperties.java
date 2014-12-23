package be.nabu.libs.channels.resources;

import java.security.Principal;

public interface FileOutProperties {
	public String getDirectoryPath();
	public String getFileName();
	public Boolean getOverwriteIfExists();
	public Principal getPrincipal();
}
