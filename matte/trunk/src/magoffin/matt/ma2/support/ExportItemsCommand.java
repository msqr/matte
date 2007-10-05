/* ===================================================================
 * ExportItemsCommand.java
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: ExportItemsCommand.java,v 1.1 2007/08/06 09:48:12 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

/** 
 * The command class.
 * 
 * <p>Initially the {@code itemIds} or {@code albumId} 
 * will be requested. The work ticket will then be returned 
 * to the client, which can then request the download by 
 * supplying the {@code ticket} value. This is to allow for an a 
 * progress display of the download.</p>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2007/08/06 09:48:12 $
 */
public class ExportItemsCommand {

	private Long ticket;
	private Long[] itemIds;
	private Long albumId;
	private String albumKey;
	private String userKey;
	private String mode;
	private String size;
	private String quality;
	private boolean download;
	private boolean original;
	private boolean direct;
	
	/**
	 * Default constructor.
	 */
	public ExportItemsCommand() {
		super();
	}
	
	/**
	 * Construct with album ID.
	 * 
	 * @param albumId the album ID
	 */
	public ExportItemsCommand(Long albumId) {
		this.albumId = albumId;
	}
	
	
	/**
	 * Construct with item IDs.
	 * 
	 * @param itemIds the item IDs
	 */
	public ExportItemsCommand(Long[] itemIds) {
		this.itemIds = itemIds;
	}
	
	
	/**
	 * @return the albumKey
	 */
	public String getAlbumKey() {
		return albumKey;
	}

	/**
	 * @param albumKey the albumKey to set
	 */
	public void setAlbumKey(String albumKey) {
		this.albumKey = albumKey;
	}

	/**
	 * @return the albumId
	 */
	public Long getAlbumId() {
		return albumId;
	}

	/**
	 * @param albumId the albumId to set
	 */
	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}

	/**
	 * @return the itemIds
	 */
	public Long[] getItemIds() {
		return itemIds;
	}
	
	/**
	 * @param itemIds the itemIds to set
	 */
	public void setItemIds(Long[] itemIds) {
		this.itemIds = itemIds;
	}
	
	/**
	 * @return the ticket
	 */
	public Long getTicket() {
		return ticket;
	}

	/**
	 * @param ticket the ticket to set
	 */
	public void setTicket(Long ticket) {
		this.ticket = ticket;
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

	/**
	 * @return the size
	 */
	public String getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * @return the quality
	 */
	public String getQuality() {
		return quality;
	}

	/**
	 * @param quality the quality to set
	 */
	public void setQuality(String quality) {
		this.quality = quality;
	}

	/**
	 * @return the download
	 */
	public boolean isDownload() {
		return download;
	}

	/**
	 * @param download the download to set
	 */
	public void setDownload(boolean download) {
		this.download = download;
	}

	/**
	 * @return the direct
	 */
	public boolean isDirect() {
		return direct;
	}

	/**
	 * @param direct the direct to set
	 */
	public void setDirect(boolean direct) {
		this.direct = direct;
	}

	/**
	 * @return the userKey
	 */
	public String getUserKey() {
		return userKey;
	}

	/**
	 * @param userKey the userKey to set
	 */
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
	
}