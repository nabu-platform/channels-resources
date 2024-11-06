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

import javax.validation.constraints.NotNull;

public interface ResourceProperties {
	
	@NotNull
	public String getUri();
	
	/**
	 * The username used to access the backend
	 */
	public String getUsername();
	/**
	 * The password used to access the backend
	 */
	public String getPassword();
	
	/**
	 * Defaults to false, if set to true, it will throw an exception if it doesn't exist
	 */
	public Boolean getMustExist();
	
	/**
	 * Defaults to "false"
	 */
	public Boolean getDeleteOriginal();
	
	/**
	 * If this is filled in, the file will be moved to here after it is done
	 * Note that this should be a _relative_ path to the original directory
	 * Absolute paths are not allowed as you could cross systems (and protocols) which is hard to do in a performant manner
	 */
	public String getProcessedDirectory();
	
	/**
	 * If this is filled in, the file will be renamed with this extension after it is done
	 * You can use "$1" to reference the current extension if necessary
	 */
	public String getProcessedExtension();
}
