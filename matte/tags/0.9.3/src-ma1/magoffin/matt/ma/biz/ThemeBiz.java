/* ===================================================================
 * ThemeBiz.java
 * 
 * Created Dec 28, 2003 10:45:00 AM
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
 * $Id: ThemeBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.ThemeMetaData;
import magoffin.matt.ma.xsd.User;

/**
 * Biz interface for Media Album theme maintenance actions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface ThemeBiz extends Biz {
	
	/** 
	 * The error message key for when an error occurs saving theme CSS:
	 * <code>theme.error.save.css</code>
	 */
	public static final String ERROR_CSS = "theme.error.save.css";
	
	/** 
	 * The error message key for when an error occurs saving theme XSL:
	 * <code>theme.error.save.xsl</code>
	 */
	public static final String ERROR_XSL = "theme.error.save.xsl";
	
	/** 
	 * The error message key for when an error occurs saving theme previews:
	 * <code>theme.error.save.preview</code>
	 */
	public static final String ERROR_PREVIEW = "theme.error.save.preview";
	
	/**
	 * The error message key for when an error occurs saving theme files:
	 * <code>theme.error.save.file.general</code>
	 */
	public static final String ERROR_FILE_GENERAL = "theme.error.save.file.general";
	
	/**
	 * The error message key for when an unsupported theme file type is encountered:
	 * <code>theme.error.save.file.unsupported</code>
	 */
	public static final String ERROR_THEME_SUPPORT_TYPE = 
			"theme.error.save.file.unsupported";
	
	/** The user theme directory name: <code>user</code> */
	public static final String USER_THEME_DIR = "user";
	
	public static final int SORT_MODE_NAME = 1;
	
	public static final int SORT_MODE_DATE = 2;
	
	public static final int SORT_MODE_AUTHOR = 3;
	
	public static final int DEFAULT_SORT_MODE = SORT_MODE_NAME;
	
	/**
	 * Data structure for dealing with theme data files.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
	 */
	public static interface AlbumThemeData {
		public InputStream getXsl();
		public String getXslName();
		public InputStream getCss();
		public String getCssName();
		public InputStream getPreviewThumbnail();
		public String getPreviewThumbnailName();
		public InputStream getPreview();
		public String getPreviewName();
		public ZipInputStream getSupportZip();
		public String getSupportZipName();
		public long getSize();
		
		public void close();
	}

/**
 * Delete an album theme.
 * 
 * @param themeId the ID of the album theme to delete
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user does not have permission to 
 * delete the album theme
 */
public void deleteAlbumTheme(Object themeId, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Tell if a user has permission to delete an album theme.
 * 
 * @param themeId the album theme ID the user wants to delete
 * @param actingUser the user trying to delete the album theme
 * @return <em>true</em> if user has permission to delete the album theme
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserDeleteAlbumTheme(Object themeId, User actingUser)
throws MediaAlbumException;

/**
 * Get an album theme by its ID.
 * 
 * @param themeId the ID of the theme to get
 * @param actingUser the user trying to view the theme
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return the album theme
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user does not have permission to
 * view the album theme
 */
public AlbumTheme getAlbumThemeById(Object themeId, User actingUser, boolean allowCached)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Tell if a user has permission to view an album theme.
 * 
 * @param themeId the ID of the theme the user wants to view
 * @param actingUser the user trying to view the album theme
 * @return <em>true</em> if the user has permission to view the album theme
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserViewAlbumTheme(Object themeId, User actingUser)
throws MediaAlbumException;

/**
 * Get the application default album theme.
 * 
 * @return the album theme
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme getDefaultAlbumTheme() throws MediaAlbumException;

/**
 * Get all album themes that are globally accessible.
 * 
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return array of album themes, or <em>null</em> if none available
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme[] getGlobalAlbumThemes(boolean allowCached) 
throws MediaAlbumException;

/**
 * Get all album themes that are globally accessible and are not owned by a user.
 * 
 * @param ignoreUser exclude themes owned by this user
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return array of album themes, or <em>null</em> if none available
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme[] getGlobalAlbumThemes(User ignoreUser, boolean allowCached)
throws MediaAlbumException;

/**
 * Tell if a user has permission to update an album theme.
 * 
 * @param themeId the ID of the theme the user wants to update
 * @param actingUser the user trying to update the theme
 * @return <em>true</em> if user has permission to view the album theme
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserUpdateTheme(Object themeId, User actingUser)
throws MediaAlbumException;

/**
 * Save changes to an album theme to the backend.
 * 
 * @param theme the theme
 * @param themeData the theme data files
 * @param actingUser the user trying to update the theme
 * @return the album theme with updated data
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the user does not have permission 
 * to update the album theme
 */
public AlbumTheme updateAlbumTheme(AlbumTheme theme, 
		AlbumThemeData themeData, User actingUser)
throws MediaAlbumException, NotAuthorizedException;


/**
 * Get all album themes owned by a user.
 * 
 * @param owner the owner that owns the album themes
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return array of album themes
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme[] getAlbumThemesForOwner(User owner, boolean allowCached)
throws MediaAlbumException;

/**
 * Create a new album theme.
 * 
 * @param theme the new theme to create
 * @param themeData the theme data files
 * @param actingUser the acting user
 * @return the album theme, with update data
 * @throws MediaAlbumException if an error occured
 */
public AlbumTheme createAlbumTheme(
		AlbumTheme theme, 
		AlbumThemeData themeData, 
		User actingUser)
throws MediaAlbumException;

/**
 * Get the album theme data for a theme.
 * 
 * @param themeId the theme ID
 * @param actingUser the acting user
 * @return album theme data
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the user does not have permission 
 * to view the album theme
 */
public AlbumThemeData getAlbumThemeData(Object themeId, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get all themes a user has the ability to view (and thus assign).
 * 
 * @param actingUser the acting user
 * @param sortMode the sort mode
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return array of album themes
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme[] getAlbumThemesViewableForUser(
		User actingUser, 
		int sortMode,
		boolean allowCached)
throws MediaAlbumException;

/**
 * Get all themes a user has the ability to make changes to.
 * 
 * @param actingUser the acting user
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return array of album themes
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme[] getAlbumThemesEditableForUser(User actingUser, boolean allowCached)
throws MediaAlbumException;

/**
 * Get a user theme resource.
 * 
 * <p>A user theme resource is something like a watermark image, icon image, 
 * etc.</p>
 * 
 * @param actingUser the owner of the resource
 * @param path the path to the resource
 * @return a File object for the resource
 * @throws MediaAlbumException if an error occurs
 */
public File getUserThemeResource(User actingUser, String path)
throws MediaAlbumException;

/**
 * Save a user theme resource and return the resource path.
 * 
 * @param actingUser the owner of the resource
 * @param in the resource input stream
 * @param path a relative path for the resource
 * @throws MediaAlbumException if an error occurs
 */
public void saveUserThemeResource(User actingUser, InputStream in, String path)
throws MediaAlbumException;

/**
 * Delete a user theme resource.
 * 
 * @param actingUser the owner of the resource
 * @param path the resource path to delete
 * @throws MediaAlbumException if an error occurs
 */
public void deleteUserThemeResource(User actingUser, String path)
throws MediaAlbumException;

/**
 * Get the meta data object for a specific theme.
 * 
 * @param themeId the ID of the theme to get the meta data for
 * @param allowCached if <em>true</em> then allow returning cached data
 * @return the meta data
 * @throws MediaAlbumException if an error occurs
 */
public ThemeMetaData getThemeMetaData(Integer themeId, boolean allowCached)
throws MediaAlbumException;
}
