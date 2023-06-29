/* ===================================================================
 * MediaCommand.java
 * 
 * Created Feb 4, 2015 4:00:08 PM
 * 
 * Copyright (c) 2015 Matt Magoffin.
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

package magoffin.matt.ma2.web.api;

/**
 * Command object for media related requests.
 *
 * @author matt
 * @version 1.0
 */
public class MediaCommand {

	private String albumKey;
	private Long[] itemIds;

	public String getAlbumKey() {
		return albumKey;
	}

	public void setAlbumKey(String albumKey) {
		this.albumKey = albumKey;
	}

	public Long getItemId() {
		return (itemIds != null && itemIds.length > 0 ? itemIds[0] : null);
	}

	public void setItemId(Long itemId) {
		if ( itemId == null ) {
			itemIds = null;
		} else {
			itemIds = new Long[] { itemId };
		}
	}

	public Long[] getItemIds() {
		return itemIds;
	}

	public void setItemIds(Long[] itemIds) {
		this.itemIds = itemIds;
	}

}
