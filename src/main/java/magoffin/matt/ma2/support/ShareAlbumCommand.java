/* ===================================================================
 * ShareAlbumCommand.java
 * 
 * Created Oct 30, 2006 6:13:33 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 */

package magoffin.matt.ma2.support;

/**
 * Command object for sharing an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class ShareAlbumCommand {

	private Long albumId = null;
	private boolean shared = false;
	private boolean browse = false;
	private boolean feed = false;
	private boolean original = false;
	private Long themeId = null;
	private boolean applyToChildren = false;
	
	/**
	 * @return the feed
	 */
	public boolean isFeed() {
		return feed;
	}
	
	/**
	 * @param feed the feed to set
	 */
	public void setFeed(boolean feed) {
		this.feed = feed;
	}

	/**
	 * @return Returns the albumId.
	 */
	public Long getAlbumId() {
		return albumId;
	}
	
	/**
	 * @param albumId The albumId to set.
	 */
	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}
	
	/**
	 * @return Returns the shared.
	 */
	public boolean isShared() {
		return shared;
	}
	
	/**
	 * @param shared The shared to set.
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
	/**
	 * @return Returns the themeId.
	 */
	public Long getThemeId() {
		return themeId;
	}
	
	/**
	 * @param themeId The themeId to set.
	 */
	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}
	
	/**
	 * @return the browse
	 */
	public boolean isBrowse() {
		return browse;
	}

	/**
	 * @param browse the browse to set
	 */
	public void setBrowse(boolean browse) {
		this.browse = browse;
	}

	/**
	 * @return the applyToChildren
	 */
	public boolean isApplyToChildren() {
		return applyToChildren;
	}
	
	/**
	 * @param applyToChildren the applyToChildren to set
	 */
	public void setApplyToChildren(boolean applyToChildren) {
		this.applyToChildren = applyToChildren;
	}

	/**
	 * @return the original
	 */
	public boolean isOriginal() {
		return original;
	}

	/**
	 * @param original the original to set
	 */
	public void setOriginal(boolean original) {
		this.original = original;
	}

}
