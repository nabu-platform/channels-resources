package be.nabu.libs.channels.resources.simple;

import be.nabu.libs.channels.resources.ResourceProperties;

abstract public class BaseResourceInProperties implements ResourceProperties {

	private String uri;
	private Boolean mustExist;
	private Boolean deleteOriginal;
	private String processedExtension, processedDirectory;
	private String username, password;
	
	/**
	 * It is made a string to enable easy replacement
	 */
	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public Boolean getMustExist() {
		return mustExist;
	}
	
	@Override
	public Boolean getDeleteOriginal() {
		return deleteOriginal;
	}

	@Override
	public String getProcessedDirectory() {
		return processedDirectory;
	}
	
	@Override
	public String getProcessedExtension() {
		return processedExtension;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setMustExist(Boolean mustExist) {
		this.mustExist = mustExist;
	}

	public void setDeleteOriginal(Boolean deleteOriginal) {
		this.deleteOriginal = deleteOriginal;
	}

	public void setProcessedExtension(String processedExtension) {
		this.processedExtension = processedExtension;
	}

	public void setProcessedDirectory(String processedDirectory) {
		this.processedDirectory = processedDirectory;
	}

	@Override
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
