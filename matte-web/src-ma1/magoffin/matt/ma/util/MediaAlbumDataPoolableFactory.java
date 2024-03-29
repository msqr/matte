/* ===================================================================
 * MediaAlbumDataPoolableFactory.java
 * 
 * Created Feb 6, 2004 9:38:43 AM
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
 * $Id: MediaAlbumDataPoolableFactory.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import magoffin.matt.ma.xsd.MediaAlbumData;

/**
 * PoolableObjectFactory for MediaAlbumData objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaAlbumDataPoolableFactory extends AbstractDataPoolableFactory 
{

/* (non-Javadoc)
 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
 */
public Object makeObject() throws Exception {
	MediaAlbumData data = new MediaAlbumData();
	passivateObject(data);
	return data;
}

/* (non-Javadoc)
 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
 */
public void passivateObject(Object o) throws Exception {
	MediaAlbumData data = (MediaAlbumData)o;
	passivateDataObject(data);
	data.clearAlbum();
	data.clearCollection();
	data.deleteDisplayAlbum();
	data.deleteDisplaySource();
	data.clearFriend();
	data.clearGroup();
	data.clearItem();
	data.clearThemes();
	data.setItemSearch(null);
	data.setItemSearchResults(null);
	data.clearOwner();
	data.setTheme(null);
}

}
