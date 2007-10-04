/* ===================================================================
 * SystemBiz.java
 * 
 * Created Feb 3, 2006 10:34:58 AM
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
 * $Id: SystemBiz.java,v 1.17 2007/09/19 07:43:05 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Locale;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.plugin.Plugin;
import magoffin.matt.ma2.support.AddThemeCommand;

import org.springframework.core.io.Resource;

/**
 * Business API for system settings and functions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.17 $ $Date: 2007/09/19 07:43:05 $
 */
public interface SystemBiz {

	/** The name of the required theme XSLT file. */
	public static final String THEME_XSLT_FILE_NAME = "theme.xsl";
	
	/** The name of the optional theme properties file. */
	public static final String THEME_PROPERTIES_FILE_NAME = "theme.properties";
	
	/** The theme property for the name. */
	public static final String THEME_PROPERTY_NAME = "theme.name";
	
	/** The theme property for the author. */
	public static final String THEME_PROPERTY_AUTHOR = "theme.author";
	
	/** The theme property for the author's email. */
	public static final String THEME_PROPERTY_AUTHOR_EMAIL = "theme.authoremail";
	
	/** The theme property for the created date (yyyy-mm-dd format). */
	public static final String THEME_PROPERTY_CREATED_DATE = "theme.created";
	
	/** The setting key for the setup complete flag. */
	public static final String SETTING_KEY_SETUP_COMPLETE = "app.setup.complete";
	
	/** The setting key for the setup: require admin flag. */
	public static final String SETTING_KEY_SETUP_REQUIRE_ADMIN = "app.setup.requireadmin";
	
	/**
	 * Get a list of available time zones.
	 * @return list of time zones
	 */
	List<TimeZone> getAvailableTimeZones();
	
	/**
	 * Get a list of available locales.
	 * @return list of locales
	 */
	List<Locale> getAvailableLocales();
	
	/**
	 * Get a specific TimeZone instance based on its code.
	 * @param code the time zone code
	 * @return the TimeZone if available, <em>null</em> if not found
	 */
	TimeZone getTimeZoneForCode(String code);
	
	/**
	 * Get the system's default time zone.
	 * @return default time zone
	 */
	TimeZone getDefaultTimeZone();
	
	/**
	 * Get the root directory to store all collections within.
	 * @return a file representing the directory to store collections in
	 */
	File getCollectionRootDirectory();
	
	/**
	 * Get a directory for caching items in.
	 * @return a file representing the directory to cache items in
	 */
	File getCacheDirectory();
	
	/**
	 * Get a directory for storing user resources in.
	 * 
	 * @return a file representing the directory to use for user resources
	 */
	File getResourceDirectory();
	
	/**
	 * Get a default theme.
	 * @return theme
	 */
	Theme getDefaultTheme();
	
	/**
	 * Get a Theme by its ID.
	 * 
	 * @param themeId the Theme ID
	 * @return the Theme, or <em>null</em> if not found
	 */
	Theme getThemeById(Long themeId);
	
	/**
	 * Get a list of available themes.
	 * @return list of themes
	 */
	List<Theme> getAvailableThemes();
	
	/**
	 * Store a Theme in the back end.
	 * 
	 * <p>This method will accept new Theme instances as well as updates 
	 * to existing themes.</p>
	 * 
	 * @param theme the theme to store
	 * @param context the current context
	 * @return the stored theme's primary key
	 */
	Long storeTheme(Theme theme, BizContext context);
	
	/**
	 * Get a Theme resource.
	 * 
	 * @param theme the theme
	 * @param path the path of the resource to get
	 * @param context the current context
	 * @return the Resource
	 */
	Resource getThemeResource(Theme theme, String path, BizContext context);
	
	/**
	 * Store a Theme, and associated resources, in the back end.
	 * 
	 * @param themeCommand the theme command data
	 * @param context the current context
	 * @return the stored theme's primary key
	 */
	Long storeTheme(AddThemeCommand themeCommand, BizContext context);
	
	/**
	 * Export a Theme, and associated resources.
	 * 
	 * <p>Calling this method will write a Zip archive of the theme
	 * and all it's associated resources to the supplied 
	 * OutputStream. This method shall not close the OutputStream
	 * when finished.</p>
	 * 
	 * @param theme the theme to export
	 * @param out the stream to output the theme to
	 * @param baseDirectory the base directory the Theme is stored in, 
	 * or if <em>null</em> use the default external theme directory
	 * @param context the currenent context
	 */
	void exportTheme(Theme theme, OutputStream out, File baseDirectory, 
			BizContext context);
	
	/**
	 * Delete a Theme.
	 * 
	 * <p>Note that internal themes cannot be deleted.</p>
	 * 
	 * @param theme the Theme to delete
	 * @param context the current context
	 */
	void deleteTheme(Theme theme, BizContext context);
	
	/**
	 * Get the Matte URL for a shared album.
	 * 
	 * @param album the album to get the URL for
	 * @param context the current context
	 * @return the URL as a String
	 */
	String getSharedAlbumUrl(Album album, BizContext context);
	
	/**
	 * Return the status of the overall application configuration.
	 * 
	 * <p>This configuration setting means the application has been 
	 * started at least once and subsequently configured for the first
	 * time, and properly set up. Some other services may not want to 
	 * start up if the application is not configured.</p>
	 * 
	 * @return boolean
	 */
	boolean isApplicationConfigured();
	
	/**
	 * Get all registered plugins of a specific type.
	 * 
	 * @param <T> the plugin class to get
	 * @param pluginType the type of plugins to get
	 * @return a read-only list of registred plugins, or an empty list if none
	 * available
	 */
	<T extends Plugin> List<T> getPluginsOfType(Class<T> pluginType);
	
}
