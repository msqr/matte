/* ===================================================================
 * ApplicationConstants.java
 * 
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: ApplicationConstants.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */
 
package magoffin.matt.ma;

import java.util.TimeZone;

import magoffin.matt.util.config.Config;


/**
 * Constants used across the entire application.
 * 
 * <p>Created Sep 22, 2003</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class ApplicationConstants {
	
public static final class CacheFactoryKeys
{
	public static final String ALBUM = "album";
	public static final String ALBUM_FREE_DATA = "album.fdata";
	public static final String ALBUM_ITEMS = "album.items";
	public static final String ALBUM_KEYS = "album.keys";
	public static final String ALBUM_PERMISSIONS = "album.perm";
	public static final String COLLECTION = "dir";
	public static final String COLLECTION_ITEMS = "collection.items";
	public static final String FREE_DATA = "fdata";
	public static final String ITEM = "item";
	public static final String ITEM_COMMENTS = "item.comments";
	public static final String ITEM_FREE_DATA = "item.fdata";
	public static final String ITEM_HITS = "hits";
	public static final String ITEM_RATINGS = "item.ratings";
	public static final String THEME = "theme";
	public static final String THEME_FOR_USER = "theme.user";
	public static final String THEME_FOR_USER_GLOBALS = "theme.user.global";
	public static final String THEME_FOR_USER_OWNER= "theme.user.owner";
	public static final String THEME_META_DATA = "theme.meta";
	public static final String USER = "user";
	public static final String USER_FREE_DATA = "user.fdata";
}

public static final boolean CACHED_OBJECT_ALLOWED = true;

public static final boolean CACHED_OBJECT_NOT_ALLOWED = false;

public static final String CONFIG_BIZ = "biz";
	
public static final String CONFIG_DAO = "dao";

public static final String CONFIG_ENV = "environment";

public static final String CONFIG_MSG = "msg";

public static final String ENV_APP_CONFIG = "app.config";

public static final String ENV_APP_WORK_QUEUES = "app.work.queues";

public static final String ENV_APP_WORK_SCHEDULERS = "app.work.schedulers";

public static final String ENV_APP_ALBUM_DEFAULT_THEME = "app.album.default.theme";

public static final String ENV_BASE_FILE_PATH_APP = "base.file.path.app";

public static final String ENV_BASE_FILE_PATH_WWW = "base.file.path.www";

public static final String ENV_BASE_FILE_PATH_COLLECTION = "base.file.path.collection";

public static final String ENV_BASE_FILE_PATH_INDEX = "base.file.path.index";

public static final String ENV_BASE_FILE_PATH_MEDIA_CACHE = "base.file.path.media.cache";

public static final String ENV_LOG4J_CONFIG = "log4j.config";

public static final String ENV_LOG4J_WATCH = "log4j.watch";

public static final String ENV_MAIL_JNDI = "mail.jndi";

public static final String ENV_MAIL_FROM = "mail.from";

public static final String ENV_MAIL_MAILER = "mail.mailer";

/** 
 * The name given to the first collection created for new users:
 * <code>user.new.collection.name</code>
 */
public static final String ENV_NEW_USER_COLLECTION_NAME = "user.new.collection.name";

/** The base path for theme APP files: <code>theme.base.app</code> */
public static final String ENV_THEME_BASE_APP = "theme.base.app";

/** The base path for theme WWW files: <code>theme.base.www</code> */
public static final String ENV_THEME_BASE_WWW = "theme.base.www";

/** The album item sort mode for sort by creation date: <code>1</code> */
public static final int SORT_MODE_CREATION_DATE = 1;

/** The album item sort mode for sort by name: <code>2</code> */
public static final int SORT_MODE_NAME = 2;

/** The default page size for browsing items, defaulting to 5. */
public static final int DEFAULT_BROWSE_ALBUM_PAGE_SIZE = 
	Config.getInt(CONFIG_ENV,"browse.albums.default.pagesize",5);

/**
 * The root resource path for mail merge template files:
 * <code>mail.template.root</code>
 */
public static final String ENV_MAILMERGE_ROOT_PATH = "mail.template.root";

/** The UTC prefix to add before a numeric time zone offset value. */
public static final String TIME_ZONE_PREFIX = "GMT";

/** The application's time zone */
public static final TimeZone TIME_ZONE = TimeZone.getDefault();

public static final Integer TIME_ZONE_OFFSET = new Integer(TIME_ZONE.getRawOffset());

public static final Integer ANONYMOUS_USER_ID = new Integer(-1);

/** The free data type for copyright. */
public static final Integer FREE_DATA_TYPE_COPYRIGHT = new Integer(1);

/** The free data type for keyword. */
public static final Integer FREE_DATA_TYPE_KEYWORD = new Integer(2);

/** The free data type for category. */
public static final Integer FREE_DATA_TYPE_CATEGORY = new Integer(3);

/** The free data type for watermark effect parameters. */
public static final Integer FREE_DATA_TYPE_WATERMARK_PARAM = new Integer(4);

/** The free data type for email notification. */
public static final Integer FREE_DATA_TYPE_EMAIL_NOTIFICATION = new Integer(5);

/** The free data type for custom meta data. */
public static final Integer FREE_DATA_TYPE_CUSTOM_META_DATA = new Integer(6);

/** The free data mode for user. */
public static final Integer FREE_DATA_MODE_USER = new Integer(1);

/** The free data mode for collection. */
public static final Integer FREE_DATA_MODE_COLLECTION = new Integer(2);

/** The free data mode for album. */
public static final Integer FREE_DATA_MODE_ALBUM = new Integer(4);

/** The free data mode for item. */
public static final Integer FREE_DATA_MODE_ITEM = new Integer(8);

/** The free data mode for second user. */
public static final Integer FREE_DATA_MODE_SECOND_USER = new Integer(16);

/** 
 * The item populate mode for no data population.
 */
public static final int POPULATE_MODE_NONE = 0;

/** 
 * The item populate mode for FreeData population. 
 */
public static final int POPULATE_MODE_FREE_DATA = 1;

/** 
 * The item populate mode for comment population.
 */
public static final int POPULATE_MODE_COMMENTS = 2;

/** 
 * The item populate mode for rating population. 
 */
public static final int POPULATE_MODE_RATINGS = 4;

/** 
 * The item populate mode for all data population. 
 */
public static final int POPULATE_MODE_ALL = POPULATE_MODE_FREE_DATA
	| POPULATE_MODE_COMMENTS | POPULATE_MODE_RATINGS;

/** Watermark param for performing overlay effect: <code>overlay</code> */
public static final String WATERMARK_PARAM_OVERLAY = "overlay";

public static final Integer ICON_WIDTH = new Integer(64);

public static final Integer ICON_HEIGHT = ICON_WIDTH;
}
