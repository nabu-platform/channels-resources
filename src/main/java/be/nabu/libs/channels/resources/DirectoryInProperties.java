package be.nabu.libs.channels.resources;


public interface DirectoryInProperties extends ResourceProperties {
	/**
	 * The regex of the files that will be picked up (defaults to ".*")
	 */
	public String getFileRegex();
	/**
	 * The regex of the directories -if any- that will be recursed (defaults to ".*")
	 */
	public String getDirectoryRegex();
	/**
	 * Whether or not to perform a recursive scan
	 */
	public Boolean getRecursive();
	
}
