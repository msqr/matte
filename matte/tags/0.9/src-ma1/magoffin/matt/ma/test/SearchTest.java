/* ===================================================================
 * SearchTest.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 30, 2004 12:04:48 PM.
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
 * $Id: SearchTest.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;

import org.apache.log4j.Logger;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.SearchBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.search.MediaItemMatch;
import magoffin.matt.ma.search.MediaItemQuery;
import magoffin.matt.ma.search.MediaItemResults;
import magoffin.matt.ma.xsd.SearchResults;
import magoffin.matt.ma.xsd.User;

/**
 * Test application to test searching.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class SearchTest extends BaseAppTester
{
	private static final Logger LOG = Logger.getLogger(SearchTest.class);
	
public SearchTest(String configPath)
{
	init(configPath);
}

public void search(MediaItemQuery query, Integer userId) 
throws MediaAlbumException
{
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	User actingUser = userBiz.getUserById(userId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	SearchBiz searchBiz = (SearchBiz)getBiz(BizConstants.SEARCH_BIZ);
	MediaItemResults result = searchBiz.search(query,actingUser);
	SearchResults sr = result.getResults();
	System.out.println("Search for [" +query +"] returned " +sr.getReturnedResults() +" results:");
	if ( sr.getReturnedResults() > 0 ) {
		MediaItemMatch[] matches = result.getMatches();
		StringBuffer buf = new StringBuffer();
		for ( int i = 0; i < matches.length; i++ ) {
			buf.setLength(0);
			buf.append(i).append(") ").append(matches[i].getItemId())
				.append(": ").append(matches[i].getName());
			System.out.println(buf.toString());
		}
	}
}

public static void main(String[] args) 
{
	String configPath = args[0];
	Integer userId = Integer.valueOf(args[1]);
	
	SearchTest test = new SearchTest(configPath);
	MediaItemQuery query = new MediaItemQuery();
	query.setSimple("used");
	try {
		test.search(query,userId);
	} catch ( MediaAlbumException e ) {
		LOG.error("Unable to search",e);
	}
}

}
