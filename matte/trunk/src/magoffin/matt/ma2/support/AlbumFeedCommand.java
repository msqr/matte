/* ===================================================================
 * AlbumFeedCommand.java
 * 
 * Created Nov 3, 2006 5:52:18 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: AlbumFeedCommand.java,v 1.2 2006/11/19 06:57:22 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

import java.util.Date;

/**
 * Command object for user album feed.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/11/19 06:57:22 $
 */
public class AlbumFeedCommand {

	private String userKey = null;
	private int maxEntries = -1;
	private Date entriesSince = null;

	/**
	 * @return the userKey
	 */
	public String getUserKey() {
		return userKey;
	}
	
	/**
	 * @param userKey the userKey to set
	 */
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
	
	/**
	 * @return the entriesSince
	 */
	public Date getEntriesSince() {
		return entriesSince;
	}
	
	/**
	 * @param entriesSince the entriesSince to set
	 */
	public void setEntriesSince(Date entriesSince) {
		this.entriesSince = entriesSince;
	}
	
	/**
	 * @return the maxEntries
	 */
	public int getMaxEntries() {
		return maxEntries;
	}
	
	/**
	 * @param maxEntries the maxEntries to set
	 */
	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}
	
}
