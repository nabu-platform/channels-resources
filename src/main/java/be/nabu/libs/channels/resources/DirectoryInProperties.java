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
