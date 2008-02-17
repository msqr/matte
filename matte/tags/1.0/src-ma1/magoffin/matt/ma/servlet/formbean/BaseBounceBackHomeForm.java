/* ===================================================================
 * BaseBounceBackHomeForm.java
 * 
 * Created Jun 10, 2004 9:09:38 AM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: BaseBounceBackHomeForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.search.MediaItemQuery;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Base action form to use in admin UI for "bouncing back" to 
 * proper location after submitting form.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public abstract class BaseBounceBackHomeForm extends ActionForm 
{
	// array of media item IDs to act on
	protected Integer[] mitems;

	// bounce back data fields:
	protected Integer collection;
	protected Integer album;
	protected MediaItemQuery query;
	
/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) 
{
	collection = album = null;
	query = new MediaItemQuery();
	mitems = new Integer[0];
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
 * @return Returns the mitems.
 */
public Integer[] getMitems() {
	return mitems;
}

/**
 * @param mitems The mitems to set.
 */
public void setMitems(Integer[] mitems) {
	this.mitems = mitems;
}
}
