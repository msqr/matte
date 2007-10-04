/* ===================================================================
 * TestConstants.java
 * 
 * Created Sep 19, 2005 2:49:37 PM
 * 
 * Copyright (c) 2005 Matt Magoffin.
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
 * $Id: TestConstants.java,v 1.9 2007/03/05 03:55:31 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

/**
 * Constants for test cases.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.9 $ $Date: 2007/03/05 03:55:31 $
 */
public final class TestConstants {

	/** The paths to the default Spring context files. */
	public static final String[] DEFAULT_APP_CONTEXT_PATHS = {
			"classpath:magoffin/matt/ma2/TestContext.xml",
			"file:web/WEB-INF/applicationContext.xml",
			"file:web/WEB-INF/dataAccessContext.xml",
			"classpath:environmentContext.xml",
	};

	/** The name for the Album table. */
	public static final String TABLE_ALBUMS = "album";
	
	/** The name for the album item relation table. */
	public static final String TABLE_ALBUM_ITEM = "album_item";
	
	/** The name for the Collection table. */
	public static final String TABLE_COLLECTION = "collection";
	
	/** The name for the MediaItem table. */
	public static final String TABLE_MEDIA_ITEM = "media_item";
	
	/** The name for the MediaItem rating table. */
	public static final String TABLE_MEDIA_ITEM_RATING = "media_item_rating";
	
	/** The name for the MediaItem tag table. */
	public static final String TABLE_MEDIA_ITEM_TAG = "user_tag";
	
	/** The name for the MediaItem comment table. */
	public static final String TABLE_MEDIA_ITEM_COMMENT = "user_comment";
	
	/** The name for the Metadata table. */
	public static final String TABLE_METADATA = "metadata";
	
	/** The name for the settings table. */
	public static final String TABLE_SETTINGS = "settings";
	
	/** The name for the Theme table. */
	public static final String TABLE_THEMES = "theme";
	
	/** The name for the TimeZone table. */
	public static final String TABLE_TIME_ZONE = "time_zone";
	
	/** The name for the Users table. */
	public static final String TABLE_USERS = "users";
	
	/** Array of table names necessary to delete from in order to "clear" database for tests. */
	public static final String[] ALL_TABLES_FOR_CLEAR = {
		TABLE_METADATA,
		TABLE_ALBUM_ITEM,
		TABLE_ALBUMS,
		TABLE_MEDIA_ITEM_RATING,
		TABLE_MEDIA_ITEM_TAG,
		TABLE_MEDIA_ITEM_COMMENT,
		TABLE_MEDIA_ITEM,
		TABLE_COLLECTION, 
		TABLE_USERS,
	};
	
}
