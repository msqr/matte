/* ===================================================================
 * SortAlbumsCommand.java
 * 
 * Created Jul 4, 2007 8:36:09 PM
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
 * $Id: SortMediaItemsCommand.java,v 1.3 2007/08/20 00:07:33 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

/**
 * Command class to sort media items within an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2007/08/20 00:07:33 $
 */
public class SortMediaItemsCommand {

	private Long albumId;
	private Long[] itemIds;
	
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
	
}
