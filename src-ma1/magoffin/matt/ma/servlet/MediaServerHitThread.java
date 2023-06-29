/* ===================================================================
 * MediaServerHitThread.java
 * 
 * Created Jan 12, 2004 11:04:29 AM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: MediaServerHitThread.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.util.BaseQueueThread;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.cache.CacheFactory;
import magoffin.matt.util.cache.SimpleCache;

import org.apache.log4j.Logger;

/**
 * Thread to update media item data for each "hit" from MediaServer.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class MediaServerHitThread extends BaseQueueThread {
	
	private static final Logger LOG = Logger.getLogger(MediaServerHitThread.class);
	
	private MediaItemBiz itemBiz;
	private SimpleCache hitCache;
	
public MediaServerHitThread(BizFactory bizFactory, CacheFactory cacheFactory) 
{
	super();
	itemBiz = (MediaItemBiz)bizFactory.getBizInstance(
			BizConstants.MEDIA_ITEM_BIZ);
	hitCache = cacheFactory.getCacheInstance(
			ApplicationConstants.CacheFactoryKeys.ITEM_HITS);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.util.BaseIntegerQueueThread#getThreadName()
 */
public String getThreadName() {
	return "MediaServerHitThread";
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.util.BaseIntegerQueueThread#handleItem(java.lang.Object)
 */
protected void handleItem(Object o) {
	Integer id = (Integer)o;
	try {
		MediaItem item = null;

		Object cachedItem = hitCache.get(id);
		if ( cachedItem != null ) {
			item = (MediaItem)cachedItem;
		} else {
			item = itemBiz.getMediaItemById(id,
					ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
			hitCache.put(id,item);
		}
		
		if ( item.getHits() == null ) {
			item.setHits(new Integer(1));
		} else {
			item.setHits(new Integer(item.getHits().intValue()+1));
		}
		
		itemBiz.updateMediaItemHits(item);
	} catch ( MediaAlbumException e ) {
		LOG.error("Exception updating hit data",e);
	}
}

}
