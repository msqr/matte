/* ===================================================================
 * MediaItemIndexCallback.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 29, 2004 11:38:35 AM.
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
 * $Id: MediaItemIndexCallback.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import magoffin.matt.dao.Criteria;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DAOSearchCallbackMatch;
import magoffin.matt.gerdal.dao.BaseRdbCriteria;
import magoffin.matt.gerdal.dao.BaseRdbDAOCallbackMatch;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.SearchBiz;
import magoffin.matt.ma.search.IndexParams;
import magoffin.matt.ma.search.MediaItemDAOIndexCallback;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.ItemComment;
import magoffin.matt.ma.xsd.ItemRating;
import magoffin.matt.ma.xsd.MediaItem;

/**
 * DAOSearchCallback implementation for indexing Media Item objects
 * using GeRDAL.
 * 
 * <p>This callback assumes the <code>ResultSet</code> contains all 
 * MediaItem columns, as well as MediaItem FreeData, ItemComments, 
 * and ItemRatings, sorted by MediaItem <code>itemId</code>. Duplicate 
 * rows are consolidated into a single MediaItem for indexing. After 
 * the call to {@link magoffin.matt.dao.DAO#find(magoffin.matt.dao.DAOSearchCallback)}
 * returns the calling code should call the {@link #finish()} method
 * otherwse the last MediaItem will not be indexed.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class MediaItemIndexCallback extends AbstractGeRDALSearchCallback 
implements MediaItemDAOIndexCallback
{
	private static final Logger LOG = Logger.getLogger(MediaItemIndexCallback.class);
	
	private IndexParams params = null;
	private MediaItem currItem = null;
	private Map prevDataTypes = new HashMap();
	private Map prevUserComments = new HashMap();
	private Map prevUserRatings = new HashMap();
	private int count = 0;

/* (non-Javadoc)
 * @see magoffin.matt.ma.search.MediaItemDAOIndexCallback#setIndexParams(magoffin.matt.ma.search.IndexParams)
 */
public void setIndexParams(IndexParams params) 
{
	this.params = params;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.search.MediaItemDAOIndexCallback#setCriteria(magoffin.matt.dao.Criteria)
 */
public void setCriteria(Criteria criteria) {
	super.setCriteria((BaseRdbCriteria)criteria);
}

/* (non-Javadoc)
 * @see magoffin.matt.dao.DAOSearchCallback#handleMatch(magoffin.matt.dao.DAOSearchCallbackMatch)
 */
public void handleMatch(DAOSearchCallbackMatch match) throws DAOException 
{
	BaseRdbDAOCallbackMatch bMatch = (BaseRdbDAOCallbackMatch)match;
	
	try {
		ResultSet rs = bMatch.getResultSet();
		int itemId = rs.getInt("itemId");
	
		if ( currItem == null || itemId != currItem.getItemId().intValue() ) {
			// starting new item, so index prev item if prev not null and start new
			indexCurrItem();
			currItem = (MediaItem)bMatch.buildDataObject();
			prevDataTypes.clear();
			prevUserComments.clear();
			prevUserRatings.clear();
		}
		
		// check for free data
		Integer dataId = new Integer(rs.getInt("dataId"));
		if ( !rs.wasNull() && !prevDataTypes.containsKey(dataId) ) {
			// add new free data to curr item
			FreeData data = new FreeData();
			data.setDataId(dataId);
			data.setDataTypeId(new Integer(rs.getInt("dataTypeId")));
			data.setItemId(currItem.getItemId());
			data.setDataValue(rs.getString("dataValue"));
			currItem.addData(data);
			prevDataTypes.put(dataId,data);
		}
		
		// check for user comment
		Integer commentId = new Integer(rs.getInt("commentId"));
		if ( !rs.wasNull() && !prevUserComments.containsKey(commentId) ) {
			// add new user comment to curr item
			ItemComment comm = new ItemComment();
			comm.setCommentId(commentId);
			comm.setContent(rs.getString("userComment"));
			currItem.addUserComment(comm);
			prevUserComments.put(commentId,comm);
		}
		
		// check for user rating
		Integer ratingId = new Integer(rs.getInt("ratingId"));
		if ( !rs.wasNull() && !prevUserRatings.containsKey(ratingId) ) {
			// add new free data to curr item
			ItemRating rating = new ItemRating();
			rating.setRatingId(ratingId);
			rating.setItemId(currItem.getItemId());
			rating.setRating(new Short(rs.getShort("rating")));
			currItem.addUserRating(rating);
			prevUserRatings.put(ratingId,rating);
		}
		
	} catch ( SQLException e ) {
		throw new DAOException("SQLException building data object",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.search.MediaItemDAOIndexCallback#finish()
 */
public void finish() throws DAOException
{
	indexCurrItem();
}

private void indexCurrItem() throws DAOException
{
	if ( currItem == null ) {
		return;
	}
	
	SearchBiz searchBiz = params.getSearchBiz();
	try {
		searchBiz.index(currItem,params);
	} catch ( MediaAlbumException e ) {
		throw new DAOException("Unable to handle index search callback",e);
	}
	count++;
	if ( LOG.isInfoEnabled() && count % 100 == 0 ) {
		LOG.info("Indexing MediaItem " +count);
	}
}

}
