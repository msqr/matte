/* ===================================================================
 * MediaItemQueryTest.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 30, 2004 10:36:46 AM.
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
 * $Id: MediaItemQueryTest.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.search.test;

import magoffin.matt.ma.search.MediaItemQueries;
import magoffin.matt.ma.search.MediaItemQuery;
import magoffin.matt.ma.search.SearchConstants;
import junit.framework.TestCase;

/**
 * Test case for MeidaItemQuery class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class MediaItemQueryTest extends TestCase 
{
	
/*
 * Class to test for String toString()
 */
public void testToString() {
	MediaItemQuery query = getQuery();
	System.out.println(query.toString());
	
	MediaItemQueries queries = new MediaItemQueries(SearchConstants.AND);
	queries.addQuery(getQuery2());
	queries.addQuery(getQuery3());
	MediaItemQueries nested = queries.addNestedQuery(getQuery4(),SearchConstants.OR,
			SearchConstants.NECESSARY);
	nested.addQuery(getQuery5());
	System.out.println(queries);
}

private MediaItemQuery getQuery() {
	MediaItemQuery query = new MediaItemQuery();
	query.setText("foo bar");
	query.setKeyword("nature kids");
	query.setCategory("California");
	query.setNecessity(SearchConstants.NECESSARY);
	return query;
}

private MediaItemQuery getQuery2() {
	MediaItemQuery query = new MediaItemQuery();
	query.setText("pow baz");
	return query;
}

private MediaItemQuery getQuery3() {
	MediaItemQuery query = new MediaItemQuery();
	query.setKeyword("fish");
	return query;
}

private MediaItemQuery getQuery4() {
	MediaItemQuery query = new MediaItemQuery();
	query.setCategory("Michigan");
	return query;
}

private MediaItemQuery getQuery5() {
	MediaItemQuery query = new MediaItemQuery();
	query.setText("jesus");
	query.setNecessity(SearchConstants.PROHIBITED);
	return query;
}

}
