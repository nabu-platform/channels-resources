package be.nabu.libs.channels.resources.simple;

import java.security.Principal;

import be.nabu.libs.channels.resources.ResourceProperties;

abstract public class BaseResourceInProperties implements ResourceProperties {

	private String uri;
	private Principal principal;
	private Boolean mustExist;
	private Boolean deleteOriginal;
	private String processedExtension, processedDirectory;
	
	/**
	 * It is made a string to enable easy replacement
	 */
	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public Principal getPrincipal() {
		return principal;
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

	public void setPrincipal(Principal principal) {
		this.principal = principal;
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
}
