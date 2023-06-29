/* ===================================================================
 * ServletConstants.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: ServletConstants.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.util.config.Config;

/**
 * Constants for use with the servlet package.
 * 
 * <p>Created Oct 8, 2002 5:53:27 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class ServletConstants 
{
	/** The key of the application-scoped BizFactory instance. */
	public final static String APP_KEY_BIZ_INTF_FACTORY = "bf";
	
	/** The key of the application-scoped File object of the base App file path (JSP, XSL, etc). */
	public static final String APP_KEY_APP_BASE_FILE_PATH = "app";
	
	/** The key of the application-scoped CacheFactory object. */
	public static final String APP_KEY_CACHE_FACTORY = "cfactory";
	
	/** The key of the application-scoped MediaAlbumConfig object. */
	public static final String APP_KEY_CONFIG = "c";
	
	/** The key of the application-scoped File object of the base collection path. */
	public static final String APP_KEY_COLLECTION_BASE_FILE_PATH = "collection";

	/** The key of the application-scoped PoolFactory object for media requests. */
	public final static String APP_KEY_MEDIA_REQUEST_POOL_FACTORY = "mrpf";
	
	/** The key of the application-scoped WorkQueue object. */
	public static final String APP_KEY_WORK_QUEUE = "wqueue";
	
	/** The key of the application-scoped WorkSchedulers object. */
	public static final String APP_KEY_WORK_SCHEDULERS = "wsc";
	
	/** The key of the application-scoped File object of the base WWW file path (CSS, JS, HTML, etc). */
	public static final String APP_KEY_WWW_BASE_FILE_PATH = "www";
	
	/** The key of the default request-scoped DOM object. */
	public static final String REQ_ATTR_XFORM_DOM = "dom";

	/** The key of the default request-scoped XSL template key. */
	public static final String REQ_ATTR_XFORM_XSL = "xsl";
	
	/** The key of the request-scoped XSL parameter Map key. */
	public static final String REQ_ATTR_XFORM_PARAM = "xsl-param";
	
	/** The request attribute key for supplying a AlbumTheme to Xform. */
	public final static String REQ_ATTR_XFORM_XSL_THEME = "xsl-theme";
	
	/** The request attribute key for supplying a AlbumTheme header to Xform. */
	public final static String REQ_ATTR_XFORM_XSL_THEME_HEADER = "xsl-theme-h";
	
	/** The request attribute key for supplying a AlbumTheme footer to Xform. */
	public final static String REQ_ATTR_XFORM_XSL_THEME_FOOTER = "xsl-theme-f";
	
	/** The key of the request parameter for an album ID: <code>album</code>. */
	public static final String REQ_KEY_ALBUM_ID = "album";
	
	/** The key of the request parameter for an album key: <code>key</code>. */
	public static final String REQ_KEY_ALBUM_KEY = "key";
	
	/** The key of the request parameter for an email address: <code>email</code> */
	public static final String REQ_KEY_EMAIL = "email";

	/** The key of the request parameter for a friend ID: <code>friend</code>. */
	public static final String REQ_KEY_FRIEND_ID = "friend";

	/** The key of the request parameter for a collection ID: <code>collection</code>. */
	public static final String REQ_KEY_COLLECTION_ID = "collection";

	/** The key of the request parameter for a group ID: <code>group</code>. */
	public static final String REQ_KEY_GROUP_ID = "group";

	/** The key of the request parameter for a media item ID: <code>mitem</code>. */
	public static final String REQ_KEY_ITEM_ID = "mitem";

	/** The key of the request parameter for a media item ID: <code>mitems</code>. */
	public static final String REQ_KEY_ITEMS_ID = "mitems";

	/** The key of the request parameter for a Media Server media item ID: <code>id</code>. */
	public static final String REQ_KEY_MEDIA_SERVER_ITEM_ID = "id";

	/** The key of the request parameter for a Media Server original media request: <code>original</code>. */
	public static final String REQ_KEY_MEDIA_SERVER_ORIGINAL = "original";

	/** The key of the request parameter for a key: <code>key</code>. */
	public static final String REQ_KEY_KEY = "key";

	/** The key of the request parameter for a name (album, collection, etc): <code>name</code> */
	public static final String REQ_KEY_NAME = "name";

	/** The key of the request parameter for a page number: <code>page</code> */
	public static final String REQ_KEY_PAGE = "page";

	/** The key of the request parameter for a page size: <code>pageSize</code> */
	public static final String REQ_KEY_PAGE_SIZE = "pageSize";

	/** The key of the request parameter for a parent album ID: <code>parent</code>. */
	public static final String REQ_KEY_PARENT_ID = "parent";
	
	/** The key of the request parameter for a sort type: <code>sort</code>. */
	public static final String REQ_KEY_SORT = "sort";
	
	/** The key of the request parameter for a theme ID: <code>theme</code>. */
	public static final String REQ_KEY_THEME_ID = "theme";
	
	/** The key of the request parameter for a user ID: <code>user</code>. */
	public static final String REQ_KEY_USER_ID = "user";
	
	/** The form parameter for the submit action: <code>submitAction</code> */
	public static final String REQ_KEY_SUBMIT_ACTION = "submitAction";
	
	public static final String REQ_VAL_SUBMIT_ACTION_CANCEL = "cancel";
	
	/** The key of the invitation key session attribute. */
	public static final String SES_KEY_INVITE_KEY = "invite.key";
	
	/** The key of the session-scoped UserSessionData object. */
	public static final String SES_KEY_USER = "usd";
	
	/** The key of the session-scoped Collection array for the current user. */
	public static final String SES_KEY_USER_MEDIA_DIRS = "dirs";
	
	/** The key of the session-scoped saved URL state. */
	public static final String SES_KEY_SAVED_URL = "url";
	
	/** The key of the sesson-scoped User object for logging on with. */
	public static final String SES_KEY_AUTO_LOGON = "logon.user";
	
	/** The key of the session-scoped "bounce back" flag. */
	public static final String SES_KEY_BOUNCING_BACK = "bounce.back";
	
	/** The prefix applied to theme WWW (CSS, JS) paths. */
	public static final String THEME_WWW_PATH_PREFIX = 
		'/' +Config.get(ApplicationConstants.CONFIG_ENV,
				ApplicationConstants.ENV_THEME_BASE_WWW) +'/';
	
	/** The prefix applied to user theme uploads. */
	public static final String THEME_USER_XSL_PREFIX = "user/";
	
	/** The Xform XSL key to use for the default theme: <code>view-album</code> */
	public static final String THEME_DEFAULT_TEMPLATE_KEY = "view-album";

	/** The prefix applied to theme XSL paths. */
	public static final String THEME_XSL_PATH_PREFIX = 
		'/' +Config.get(ApplicationConstants.CONFIG_ENV,
				ApplicationConstants.ENV_THEME_BASE_APP) +'/';
	
	/** The album theme header XSL: <code>album-header.xsl</code> */
	public final static String THEME_XSL_HEADER_ALBUM = 
		THEME_XSL_PATH_PREFIX+"album-header.xsl";

	/** The album theme footer XSL: <code>album-footer.xsl</code> */
	public final static String THEME_XSL_FOOTER_ALBUM = 
		THEME_XSL_PATH_PREFIX+"album-footer.xsl";

	/** The XSL param key for album mode: <code>album-mode</code> */
	public final static String XSL_PARAM_KEY_ALBUM_MODE = "album-mode";
	
	/** The XSL param key for album browse mode: <code>browse</code> */
	public final static String XSL_PARAM_ALBUM_MODE_BROWSE = "browse";
	
	/** The XSL param value for media item metadata mode: <code>item-meta</code> */
	public final static String XSL_PARAM_ALBUM_MODE_ITEM_METADATA = "item-meta";
		
	/** The XSL param value for album listing mode: <code>listing</code> */
	public final static String XSL_PARAM_ALBUM_MODE_LISTING = "listing";
	
	/** The XSL param value for url path: <code>url-path</code> */
	public final static String XSL_PARAM_KEY_URL_PATH = "url-path";
	
	/** The password key for an unset or unchagned password: <code>_nopass_</code> */
	public static final String UNASSIGNED_PASSWORD = "_nopass_";

	/** 
	 * The request-scoped attribute key for keeping the state of the saved session URL:
	 * <code>_url</code>.
	 */
	public static final String REQ_ATTR_SAVED_URL = "_url";
	
}
