/* ===================================================================
 * AddMediaCommand.java
 * 
 * Created Feb 27, 2006 7:55:40 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: AddMediaCommand.java,v 1.1 2006/10/29 01:32:50 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

import magoffin.matt.util.TemporaryFile;

/**
 * Command object for adding new media.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/10/29 01:32:50 $
 */
public class AddMediaCommand {
	
	private TemporaryFile tempFile;
	private Long collectionId;
	private String localTz;
	private String mediaTz;
	private boolean autoAlbum;
	
	
	/**
	 * @return the mediaTz
	 */
	public String getMediaTz() {
		return mediaTz;
	}
	
	/**
	 * @param mediaTz the mediaTz to set
	 */
	public void setMediaTz(String mediaTz) {
		this.mediaTz = mediaTz;
	}

	/**
	 * @return the tz
	 */
	public String getLocalTz() {
		return localTz;
	}
	
	/**
	 * @param tz the tz to set
	 */
	public void setLocalTz(String tz) {
		this.localTz = tz;
	}
	/**
	 * @return Returns the autoAlbum.
	 */
	public boolean isAutoAlbum() {
		return autoAlbum;
	}
	/**
	 * @param autoAlbum The autoAlbum to set.
	 */
	public void setAutoAlbum(boolean autoAlbum) {
		this.autoAlbum = autoAlbum;
	}
	/**
	 * @return Returns the collectionId.
	 */
	public Long getCollectionId() {
		return collectionId;
	}
	/**
	 * @param collectionId The collectionId to set.
	 */
	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}
	/**
	 * @return Returns the tempFile.
	 */
	public TemporaryFile getTempFile() {
		return tempFile;
	}
	/**
	 * @param tempFile The tempFile to set.
	 */
	public void setTempFile(TemporaryFile tempFile) {
		this.tempFile = tempFile;
	}

}
