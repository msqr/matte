/* ===================================================================
 * FileSelection.java
 * 
 * Copyright (c) 2008 Matt Magoffin (spamsqr@msqr.us)
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web.applet;

import java.io.File;

import magoffin.matt.ma2.domain.AlbumImportType;
import magoffin.matt.ma2.domain.ItemImportType;

/**
 * A selected file, for use in displaying the selected items to upload.
 * 
 * <p>This object is used for either directories or files, and has an 
 * associated {@link AlbumImportType} (for directories) and {@link ItemImportType}
 * (for files) to hold customized name, comments, etc. for each selected file.</p>
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class FileSelection {

	private File file;
	private AlbumImportType album;
	private ItemImportType item;
	
	@Override
	public String toString() {
		if ( item != null && item.getName() != null ) {
			return item.getName();
		}
		if ( album != null && album.getName() != null ) {
			return album.getName();
		}
		if ( file != null ) {
			return file.getName();
		}
		return "FileSelection{?}";
	}
	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	/**
	 * @return the album
	 */
	public AlbumImportType getAlbum() {
		return album;
	}
	/**
	 * @param album the album to set
	 */
	public void setAlbum(AlbumImportType album) {
		this.album = album;
	}
	/**
	 * @return the item
	 */
	public ItemImportType getItem() {
		return item;
	}
	/**
	 * @param item the item to set
	 */
	public void setItem(ItemImportType item) {
		this.item = item;
	}
	
}
