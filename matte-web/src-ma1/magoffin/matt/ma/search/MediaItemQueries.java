/* ===================================================================
 * MediaItemQueries.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 30, 2004 9:54:23 AM.
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
 * $Id: MediaItemQueries.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Logical grouping of MediaItemQuery objects.
 * 
 * <p>This class is not search implementation specific. The {@link #toString()} 
 * method generates a pseudo search query string to demonstrate how 
 * the fields of this class can be used.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaItemQueries implements Serializable 
{
	private char join;
	private char necessity;
	private List queries = null;
	private List nested = null;

/**
 * Construct a query set with an implicit necessity of 
 * {@link SearchConstants#UNSPECIFIED}.
 * 
 * @param joinType the logical join type
 * @see #MediaItemQueries(char, char)
 */
public MediaItemQueries(char joinType)
{
	this(joinType,SearchConstants.UNSPECIFIED);
}

/**
 * Construct a query set.
 * @param joinType the logical join type (AND, OR, etc.)
 * @param necessity the necessity flag
 */
public MediaItemQueries(char joinType, char necessity)
{
	this.join = joinType;
	this.necessity = necessity;
}

/**
 * Add a query to this query set.
 * 
 * @param query the query to add
 */
public void addQuery(MediaItemQuery query) 
{
	if ( queries == null ) {
		queries = new ArrayList(4);
	}
	queries.add(query);
}

/**
 * Add a MediaItemQueries object as a nested query set.
 * 
 * @param queries the query set to nest
 */
public void addNestedQuery(MediaItemQueries queries) 
{
	if ( nested == null ) {
		nested = new ArrayList(4);
	}
	nested.add(queries);
}

/**
 * Add a query as a nested query, returning the new nested MediaItemsQueries 
 * object.
 * 
 * @param query the query to add as a nested query
 * @param joinType the logical join type
 * @param necessity the necessity flag
 * @return the new nested queries object
 */
public MediaItemQueries addNestedQuery(MediaItemQuery query, char joinType, char necessity)
{
	MediaItemQueries queries = new MediaItemQueries(joinType,necessity);
	queries.addQuery(query);
	addNestedQuery(queries);
	return queries;
}

/**
 * Append this set of queries to a StringBuffer.
 * 
 * @param buf the buffer to append to
 */
public void appendToString(StringBuffer buf)
{
	StringBuffer myBuf = new StringBuffer();
	if ( queries != null ) {
		int size = queries.size();
		// for each search query, append value
		for ( int i = 0; i < size; i++ ) {
			MediaItemQuery query = (MediaItemQuery)queries.get(i);
			if ( myBuf.length() > 0 ) {
				myBuf.append(' ').append(join).append(' ');
			}
			query.appendToString(myBuf);
		}
	}
	if ( nested != null ) {
		// for each nested search query, append value
		int size = nested.size();
		for ( int i = 0; i < size; i++ ) {
			MediaItemQueries queries = (MediaItemQueries)nested.get(i);
			if ( myBuf.length() > 0 ) {
				myBuf.append(' ').append(join);
			}
			queries.appendToString(myBuf);
		}
	}
	if ( buf.length() > 0 ) {
		buf.append(' ');
	}
	switch ( necessity ) {
		case SearchConstants.NECESSARY:
			buf.append('+');
			break;
		
		case SearchConstants.PROHIBITED:
			buf.append('-');
			break;
	}
	buf.append("(");
	buf.append(myBuf);
	buf.append(")");
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
	StringBuffer buf = new StringBuffer();
	appendToString(buf);
	return buf.toString();
}

}
