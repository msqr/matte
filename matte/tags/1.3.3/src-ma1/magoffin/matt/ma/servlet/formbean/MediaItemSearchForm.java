/* ===================================================================
 * MediaItemSearchForm.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 30, 2004 3:02:19 PM.
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
 * $Id: MediaItemSearchForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import magoffin.matt.ma.search.MediaItemQuery;

import org.apache.struts.action.ActionForm;

/**
 * Form bean for media item searches.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaItemSearchForm extends ActionForm 
{
	private MediaItemQuery query = new MediaItemQuery();
	private Integer album = null;
	private Integer collection = null;
	
/**
 * @return Returns the query.
 */
public MediaItemQuery getQuery() {
	return query;
}
/**
 * @param query The query to set.
 */
public void setQuery(MediaItemQuery query) {
	this.query = query;
}

/**
 * @return Returns the album.
 */
public Integer getAlbum() {
	return album;
}
/**
 * @param album The album to set.
 */
public void setAlbum(Integer album) {
	this.album = album;
}
/**
 * @return Returns the collection.
 */
public Integer getCollection() {
	return collection;
}
/**
 * @param collection The collection to set.
 */
public void setCollection(Integer collection) {
	this.collection = collection;
}
}
