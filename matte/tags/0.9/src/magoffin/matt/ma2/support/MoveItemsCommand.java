/* ===================================================================
 * MoveItemsCommand.java
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
 * $Id: MoveItemsCommand.java,v 1.1 2007/08/19 02:50:22 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

/**
 * Command object for moving items into a new Collection.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2007/08/19 02:50:22 $
 */
public class MoveItemsCommand {

	private Long collectionId;
	private Long[] itemIds;
	
	
	/**
	 * Default constructor.
	 */
	public MoveItemsCommand() {
		super();
	}
	
	/**
	 * Construct with data.
	 * 
	 * @param collectionId the collection ID
	 * @param itemIds the item IDs
	 */
	public MoveItemsCommand(Long collectionId, Long[] itemIds) {
		this.collectionId = collectionId;
		this.itemIds = itemIds;
	}
	/**
	 * @return the collectionId
	 */
	public Long getCollectionId() {
		return collectionId;
	}
	/**
	 * @param collectionId the collectionId to set
	 */
	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
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
