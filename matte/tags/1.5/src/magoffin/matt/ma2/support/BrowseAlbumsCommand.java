/* ===================================================================
 * BrowseAlbumsCommand.java
 * 
 * Created Dec 9, 2006 12:01:22 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.support;

import java.util.Date;
import java.util.Locale;

/**
 * Command object for supporing browsing of media items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class BrowseAlbumsCommand {
	
	/** The default value for the {@code mode} property. */
	public static final String MODE_ALBUMS = "albums";

	/** The mode for an album feed. */
	public static final String MODE_ALBUM_FEED = "album-feed";

	private String mode = MODE_ALBUMS;
	private String section = null;
	private String userKey;
	private int maxEntries = -1;
	private Date entriesSince = null;
	private Locale locale = Locale.getDefault();

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
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the section
	 */
	public String getSection() {
		return section;
	}

	/**
	 * @param section the section to set
	 */
	public void setSection(String section) {
		this.section = section;
	}
	
}
