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
