/* ===================================================================
 * MediaItemIndexUpdateThread.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 31, 2004 9:08:20 AM.
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
 * $Id: MediaItemIndexUpdateThread.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.search;

import org.apache.log4j.Logger;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.SearchBiz;
import magoffin.matt.ma.util.BaseQueueThread;
import magoffin.matt.ma.xsd.MediaItem;

/**
 * Thread to update media item index for update or deletion.
 * 
 * <p>This queue accepts Data objects. For each engueued item it 
 * calls the {@link magoffin.matt.ma.biz.SearchBiz#index(MediaItem, IndexParams)}
 * method, assuming the implementation knows how to handle update/delete
 * via the parameter object supplied.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaItemIndexUpdateThread extends BaseQueueThread 
{
	public static final char DELETE = 'd';
	public static final char UPDATE = 'u';
	
	private static final Logger LOG = Logger.getLogger(MediaItemIndexUpdateThread.class);
	
	/**
	 * Data object to add to thread queue.
	 */
	public static final class Data {
		private Integer itemId;
		private IndexParams params;
		private char mode;
		public Data(Integer itemId, IndexParams params, char mode) {
			this.itemId = itemId;
			this.params = params;
			this.mode = mode;
		}
	}
	
	private MediaItemBiz itemBiz;
	private MediaItem[] itemArray = new MediaItem[1];
	
public MediaItemIndexUpdateThread(BizFactory bizFactory) 
{
	super();
	itemBiz = (MediaItemBiz)bizFactory.getBizInstance(
			BizConstants.MEDIA_ITEM_BIZ);
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.util.BaseIntegerQueueThread#getThreadName()
 */
public String getThreadName() {
	return "MediaItemIndexUpdateThread";
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.util.BaseIntegerQueueThread#handleItem(java.lang.Object)
 */
protected void handleItem(Object o) {
	Data data = (Data)o;
	if ( LOG.isDebugEnabled() ) {
		switch ( data.mode) {
			case UPDATE:
				LOG.debug("Updating item " +data.itemId +" index data");
				break;
			
			case DELETE:
				LOG.debug("Deleting item " +data.itemId +" from index");
				break;
		}
	}
	try {
		MediaItem item = null;
		if ( data.mode != DELETE ) {
			item = itemBiz.getMediaItemById(data.itemId,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
			itemArray[0] = item;
			itemBiz.populateItems(itemArray, ApplicationConstants.POPULATE_MODE_ALL,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
		}
		SearchBiz searchBiz = data.params.getSearchBiz();
		searchBiz.index(item,data.params);		
	} catch ( MediaAlbumException e ) {
		LOG.error("Exception indexing media item",e);
	}
}

}
