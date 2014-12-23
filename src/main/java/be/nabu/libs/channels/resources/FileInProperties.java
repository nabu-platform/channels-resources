package be.nabu.libs.channels.resources;

public interface FileInProperties extends ResourceProperties {
	/**
	 * If not specifically set, the type will be deduced from the fileName
	 */
	public String getActualType();
	
	/**
	 * If not specifically set, the original fileName is used
	 */
	public String getName();
}
