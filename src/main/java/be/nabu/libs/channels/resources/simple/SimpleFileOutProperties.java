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
