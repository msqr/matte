/* ===================================================================
 * ThemeBizImpl.java
 * 
 * Created Dec 28, 2003 11:12:58 AM
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
 * $Id: ThemeBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DataObject;
import magoffin.matt.dao.PrimaryKeyObjectPoolFactory;
import magoffin.matt.gerdal.dataobjects.CountData;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.dao.AlbumCriteria;
import magoffin.matt.ma.dao.AlbumThemeCriteria;
import magoffin.matt.ma.dao.AlbumThemePK;
import magoffin.matt.ma.dao.UserCriteria;
import magoffin.matt.ma.util.ComparatorUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.InitParamConfig;
import magoffin.matt.ma.xsd.ThemeMetaData;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.ArrayUtil;
import magoffin.matt.util.FileUtil;
import magoffin.matt.util.StringUtil;
import magoffin.matt.util.config.Config;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

/**
 * Biz implementation for ThemeBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ThemeBizImpl extends AbstractBiz implements ThemeBiz {
	
	private static final String GLOBAL_THEMES_CACHE_KEY = "_g";
	
	private static final Logger LOG = Logger.getLogger(ThemeBizImpl.class);

	private static final Comparator SORT_BY_NAME = 
			new ComparatorUtil.ThemeNameSort();
	
	private static final Comparator SORT_BY_DATE = 
			new ComparatorUtil.ThemeDateSort();
	
	private static final Comparator SORT_BY_AUTHOR = 
		new ComparatorUtil.ThemeAuthorSort();

	private Integer myDefaultThemeId = null;
	
	private File myBaseDirThemeWWW = null;
	
	private File myBaseDirThemeApp = null;
	
	private Map mySupportedThemeFileTypes = null;
	
	private static class AlbumThemeSupportFileFilter implements FilenameFilter
	{
		private Map disallow;
		
		public AlbumThemeSupportFileFilter(AlbumTheme theme) {
			disallow = new HashMap(6);
			disallow.put("CVS",null); // ignore any CVS directories
			if ( theme.getCss() != null ) {
				disallow.put(theme.getCss(),null);
			}
			if ( theme.getXsl() != null ) {
				disallow.put(theme.getXsl(),null);
			}
			if ( theme.getIcon() != null ) {
				disallow.put(theme.getIcon(),null);
			}
			if ( theme.getPreview() != null ) {
				disallow.put(theme.getPreview(),null);
			}
		}
		
		/* (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String name) {
			return !disallow.containsKey(name);
		}
	}
	
	/**
	 * Implementation of AlbumThemeData.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	private static class AlbumThemeDataImpl implements ThemeBiz.AlbumThemeData 
	{
		private InputStream css = null;
		private String cssName = null;
		private InputStream xsl = null;
		private String xslName = null;
		private InputStream previewThumbnail = null;
		private String previewThumbnailName = null;
		private InputStream preview = null;
		private String previewName = null;
		private ZipInputStream supportZip = null;
		private String supportZipName = null;
		private long size = 0;
		
		private File tempZip;
		
		public AlbumThemeDataImpl(AlbumTheme theme, File wwwDir, File appDir) 
		{
			File currFile = null;
			try {
				currFile = new File(wwwDir,theme.getCss());
				if ( theme.getCss() != null && currFile.exists() ) {
					css = new FileInputStream(currFile);
					cssName = theme.getCss();
					size += currFile.length();
				}
				
				currFile = new File(appDir,theme.getXsl());
				if ( theme.getXsl() != null && currFile.exists() ) {
					xsl = new FileInputStream(currFile);
					xslName = theme.getXsl();
					size += currFile.length();
				}
				
				currFile = new File(wwwDir,theme.getIcon());
				if ( theme.getIcon() != null && currFile.exists() ) {
					previewThumbnail = new FileInputStream(currFile);
					previewThumbnailName = theme.getIcon();
					size += currFile.length();
				}
				
				currFile = new File(wwwDir,theme.getPreview());
				if ( theme.getPreview() != null && currFile.exists() ) {
					preview = new FileInputStream(currFile);
					previewName = theme.getPreview();
					size += currFile.length();
				}
				
				// check for support files
				FilenameFilter filter = new AlbumThemeSupportFileFilter(theme);
				File[] files = appDir.listFiles(filter);
				files = (File[])ArrayUtil.merge(files,wwwDir.listFiles(filter));
				if ( files != null && files.length > 0 ) {
					supportZipName = "support-files.zip";
					// zip up files into temp file 
					tempZip = File.createTempFile("ma-support-files",".zip");
					ZipOutputStream zout = new ZipOutputStream(
							new FileOutputStream(tempZip));
					doZipDir("",files,zout);
					zout.flush();
					zout.close();
					supportZip = new ZipInputStream(new FileInputStream(tempZip));
					size += tempZip.length();
				}
			} catch ( Exception e ) {
				LOG.warn("Exception getting theme form data: " +e.toString());
			}
		}
		
		private void doZipDir(String prefix, File[] files, ZipOutputStream zout) 
		throws IOException
		{
			for ( int i = 0; i < files.length; i++ ) {
				if ( files[i].isDirectory() ) {
					if ( !"CVS".equals(files[i].getName()) ) {
						// recurse for any nested files
						File[] nested = files[i].listFiles();
						if ( nested != null && nested.length > 0 ) {
							doZipDir(files[i].getName()+'/',nested,zout);
						}
					}
					continue;
				}
				
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Zipping theme support file "
							+files[i].getAbsolutePath());
				}
				ZipEntry entry = new ZipEntry(prefix+files[i].getName());
				entry.setSize(files[i].length());
				zout.putNextEntry(entry);
				FileUtil.slurp(files[i],zout);
				zout.closeEntry();
			}
		}
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.biz.ThemeBiz.AlbumThemeData#close()
		 */
		public void close() {
			if ( css != null ) {
				try {
					css.close();
				} catch ( Exception e ) {
					// ignore
				}
				css = null;
			}
			if ( xsl != null ) {
				try {
					xsl.close();
				} catch ( Exception e ) {
					// ignore
				}
				xsl = null;
			}
			if ( previewThumbnail != null ) {
				try {
					previewThumbnail.close();
				} catch ( Exception e ) {
					// ignore
				}
				previewThumbnail = null;
			}
			if ( preview != null ) {
				try {
					preview.close();
				} catch ( Exception e ) {
					// ignore
				}
				preview = null;
			}
			if ( supportZip != null ) {
				try {
					supportZip.close();
				} catch ( Exception e ) {
					// ignore
				}
				if ( !tempZip.delete() ) {
					LOG.warn("Unable to delete temp zip file " +tempZip.getAbsolutePath());
				}
				supportZip = null;
				tempZip = null;
			}
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#finalize()
		 */
		protected void finalize() throws Throwable {
			super.finalize();
			close();
		}

		/**
		 * @return Returns the css.
		 */
		public InputStream getCss() {
			return css;
		}
		/**
		 * @return Returns the cssName.
		 */
		public String getCssName() {
			return cssName;
		}
		/**
		 * @return Returns the preview.
		 */
		public InputStream getPreview() {
			return preview;
		}
		/**
		 * @return Returns the previewName.
		 */
		public String getPreviewName() {
			return previewName;
		}
		/**
		 * @return Returns the previewThumbnail.
		 */
		public InputStream getPreviewThumbnail() {
			return previewThumbnail;
		}
		/**
		 * @return Returns the previewThumbnailName.
		 */
		public String getPreviewThumbnailName() {
			return previewThumbnailName;
		}
		/**
		 * @return Returns the supportZip.
		 */
		public ZipInputStream getSupportZip() {
			return supportZip;
		}
		/**
		 * @return Returns the supportZipName.
		 */
		public String getSupportZipName() {
			return supportZipName;
		}
		/**
		 * @return Returns the xsl.
		 */
		public InputStream getXsl() {
			return xsl;
		}
		/**
		 * @return Returns the xslName.
		 */
		public String getXslName() {
			return xslName;
		}

		/**
		 * @return Returns the size.
		 */
		public long getSize() {
			return size;
		}
	}

/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) 
{
	super.init(initializer);
	
	myDefaultThemeId = Config.getInteger(
			ApplicationConstants.CONFIG_ENV, 
			ApplicationConstants.ENV_APP_ALBUM_DEFAULT_THEME);
	
	myBaseDirThemeWWW = new File(Config.get(
			ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_BASE_FILE_PATH_WWW)
			+File.separator
			+Config.get(
					ApplicationConstants.CONFIG_ENV,
					ApplicationConstants.ENV_THEME_BASE_WWW));
	
	myBaseDirThemeApp = new File(Config.get(
			ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_BASE_FILE_PATH_APP)
			+File.separator
			+Config.get(
					ApplicationConstants.CONFIG_ENV,
					ApplicationConstants.ENV_THEME_BASE_APP));

	// get supported theme file types

	InitParamConfig[] extTypes = this.initializer.getAppConfig()
			.getThemeUploadSupportMime();
	mySupportedThemeFileTypes = new HashMap((int)(extTypes.length * 1.2));
	for ( int i = 0; i < extTypes.length; i++ ) {
		mySupportedThemeFileTypes.put(extTypes[i].getName(),
				extTypes[i].getContent());
	}

}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#deleteAlbumTheme(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public void deleteAlbumTheme(Object themeId, User actingUser)
	throws MediaAlbumException, NotAuthorizedException 
{
	AlbumTheme theme = getAlbumThemeById(themeId,actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( !canUserDeleteAlbumTheme(theme,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_THEME);
	}
	
	AlbumThemePK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				AlbumTheme.class);
		pk = (AlbumThemePK)borrowObjectFromPool(pool);
		
		pk.setId(themeId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.THEME,
			themeId.toString());
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER);
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_GLOBALS);
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_OWNER);
	
	deleteThemeFiles(theme);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#canUserDeleteAlbumTheme(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public boolean canUserDeleteAlbumTheme(Object themeId, User actingUser)
throws MediaAlbumException 
{
	AlbumTheme theme = getAlbumThemeById(themeId,actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserDeleteAlbumTheme(theme,actingUser);
}

private boolean canUserDeleteAlbumTheme(AlbumTheme theme, User actingUser)
throws MediaAlbumException
{
	if ( actingUser == null ) return true;
	
	// must be owner or super user to delete
	if ( actingUser.getUserId().equals(theme.getOwner()) ) {
		return true;
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	if ( userBiz.isUserSuperUser(actingUser.getUserId()) ) {
		return true;
	}
	
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getAlbumThemeById(java.lang.Object, magoffin.matt.ma.xsd.User, boolean)
 */
public AlbumTheme getAlbumThemeById(Object themeId, User actingUser, boolean allowCached)
	throws MediaAlbumException, NotAuthorizedException 
{
	if ( themeId == null ) {
		throw new MediaAlbumException("Null id passed to getAlbumThemeById");
	}
	
	AlbumTheme result = (AlbumTheme)getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.THEME,themeId.toString());
	if ( result != null ) {
		if ( actingUser != null && !canUserViewAlbumTheme(result,actingUser) ) {
			throw new NotAuthorizedException(actingUser.getUsername(), 
					MessageConstants.ERR_AUTH_VIEW_THEME);
		}
		return result;
	}
	
	AlbumThemePK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				AlbumTheme.class);
		pk = (AlbumThemePK)borrowObjectFromPool(pool);
		
		pk.setId(themeId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		
		result = (AlbumTheme)dao.get(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.THEME,themeId.toString(),
			result);
	
	if ( actingUser != null && !canUserViewAlbumTheme(result,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(), 
				MessageConstants.ERR_AUTH_VIEW_THEME);
	}
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#canUserViewAlbumTheme(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public boolean canUserViewAlbumTheme(Object themeId, User actingUser)
throws MediaAlbumException 
{
	AlbumTheme theme = getAlbumThemeById(themeId,actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserViewAlbumTheme(theme,actingUser);
}

private boolean canUserViewAlbumTheme(AlbumTheme theme, User actingUser)
{
	// only checking if owner or global
	if ( actingUser == null ) return true;
	if ( theme.getGlobal() != null && theme.getGlobal().booleanValue() ) {
		return true;
	}
	return actingUser.getUserId().equals(theme.getOwner());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getDefaultAlbumTheme()
 */
public AlbumTheme getDefaultAlbumTheme() throws MediaAlbumException {
	AlbumTheme theme = getAlbumThemeById(myDefaultThemeId,null,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	theme.setDefault(Boolean.TRUE);
	return theme;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getGlobalAlbumThemes(boolean)
 */
public AlbumTheme[] getGlobalAlbumThemes(boolean allowCached)
throws MediaAlbumException 
{
	AlbumTheme[] result = (AlbumTheme[])getCachedObject(
			ApplicationConstants.CACHED_OBJECT_ALLOWED,
			ApplicationConstants.CacheFactoryKeys.THEME,
			GLOBAL_THEMES_CACHE_KEY);
	if ( result != null ) {
		return result;
	}
	
	AlbumThemeCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumTheme.class);
		crit = (AlbumThemeCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumThemeCriteria.SEARCH_BY_GLOBAL);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		
		AlbumTheme[] results = (AlbumTheme[])dao.findByCriteria(crit);
		
		if ( results == null || results.length < 1 ) {
			return null;
		}
		
		result = results;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}

	cacheObject(ApplicationConstants.CacheFactoryKeys.THEME,
			GLOBAL_THEMES_CACHE_KEY,result);
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#canUserUpdateTheme(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public boolean canUserUpdateTheme(Object themeId, User actingUser)
throws MediaAlbumException 
{
	AlbumTheme theme = getAlbumThemeById(themeId,actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserUpdateTheme(theme,actingUser);
}

private boolean canUserUpdateTheme(AlbumTheme theme, User actingUser)
throws MediaAlbumException
{
	if ( actingUser == null ) return true;
	if ( actingUser.getUserId().equals(theme.getOwner()) ) {
		return true;
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	if ( userBiz.isUserSuperUser(actingUser.getUserId()) ) {
		return true;
	}
	
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#updateAlbumTheme(magoffin.matt.ma.xsd.AlbumTheme, magoffin.matt.ma.biz.ThemeBiz.AlbumThemeData, magoffin.matt.ma.xsd.User)
 */
public AlbumTheme updateAlbumTheme(
	AlbumTheme theme,
	AlbumThemeData themeData,
	User actingUser)
	throws MediaAlbumException, NotAuthorizedException 
{
	if ( theme == null ) {
		throw new MediaAlbumException("Null album passed to updateAlbumTheme");
	}
	
	if ( !canUserUpdateTheme(theme,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_THEME);
	}
	
	// clone to change data
	try {
		theme = (AlbumTheme)BeanUtils.cloneBean(theme);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data",e);
	}
	
	theme.setModificationDate(new Date());
	theme.setXsl(themeData.getXslName());
	theme.setCss(themeData.getCssName());
	theme.setPreview(themeData.getPreviewName());
	theme.setIcon(themeData.getPreviewThumbnailName());
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		dao.update(theme);
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.THEME,
			theme.getThemeId().toString());
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER);
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_GLOBALS);
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_OWNER);
	
	// delete existing files
	deleteThemeFiles(theme);
	
	// save files
	saveThemeFiles(theme,themeData);
	
	return theme;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getAlbumThemesForOwner(magoffin.matt.ma.xsd.User, boolean)
 */
public AlbumTheme[] getAlbumThemesForOwner(User owner, boolean allowCached)
	throws MediaAlbumException 
{
	AlbumTheme[] result = (AlbumTheme[])getCachedObject(
			allowCached,
			ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_OWNER,
			owner.getUserId());
	if ( result != null ) {
		return result;
	}
	
	AlbumThemeCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumTheme.class);
		crit = (AlbumThemeCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumThemeCriteria.SEARCH_BY_OWNER);
		crit.setQuery(owner.getUserId());
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		
		AlbumTheme[] results = (AlbumTheme[])dao.findByCriteria(crit);
		
		if ( results != null || results.length > 0 ) {
			result = results;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_OWNER,
			owner.getUserId(),
			result);
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#createAlbumTheme(magoffin.matt.ma.xsd.AlbumTheme, magoffin.matt.ma.biz.ThemeBiz.AlbumThemeData, magoffin.matt.ma.xsd.User)
 */
public AlbumTheme createAlbumTheme(AlbumTheme theme,
		AlbumThemeData themeData, User actingUser)
throws MediaAlbumException 
{
	if ( theme == null || themeData == null || actingUser == null ) {
		throw new MediaAlbumException("Null data passed to createAlbumTheme");
	}
	
	AlbumTheme newTheme = null;
	try {
		newTheme = (AlbumTheme)BeanUtils.cloneBean(theme);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data",e);
	}
	
	Date createDate = new Date();
	
	// set new theme data
	newTheme.setCreationDate(createDate);
	newTheme.setOwner(actingUser.getUserId());
	
	
	// set base dir to user ID + createDate
	newTheme.setBaseDir(USER_THEME_DIR +'/'
			+actingUser.getUserId() +'/'
			+createDate.getTime() +'/');
	
	// save files
	saveThemeFiles(newTheme,themeData);
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		dao.create(newTheme);
	} catch ( DAOException e ) {
		deleteThemeFiles(newTheme);
		throw new MediaAlbumException("DAO exception",e);
	}
	
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER);
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_GLOBALS);
	clearCache(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_OWNER);

	return newTheme;
}

private void saveThemeFiles(
		AlbumTheme theme,
		AlbumThemeData data) 
		throws MediaAlbumException
{
	File outFile = null;
	InputStream in = null;
	String inName = null;
	
	File wwwDir = new File(myBaseDirThemeWWW.getAbsolutePath() 
			+File.separator +theme.getBaseDir());
	File appDir = new File(myBaseDirThemeApp.getAbsolutePath() 
			+File.separator +theme.getBaseDir());
	
	try {
		// save css
		in = data.getCss();
		inName = data.getCssName();
		if ( in == null ) {
			throw new MediaAlbumException(ERROR_CSS,(Object[])null);
		}
		outFile = new File(wwwDir,inName);
		theme.setCss(inName);
		saveThemeFile(in, inName, outFile, true);
		
		// save xsl
		in = data.getXsl();
		if ( in != null ) {
			inName = data.getXslName();
			if ( in == null ) {
				throw new MediaAlbumException(ERROR_XSL,(Object[])null);
			}
			outFile = new File(appDir,inName);
			theme.setXsl(inName);
			saveThemeFile(in, inName, outFile, true);
		}
		
		// save preview thumbnail
		in = data.getPreviewThumbnail();
		if ( in != null ) {
			inName = data.getPreviewThumbnailName();
			if ( in == null ) {
				throw new MediaAlbumException(ERROR_PREVIEW,(Object[])null);
			}
			outFile = new File(wwwDir,inName);
			theme.setIcon(inName);
			saveThemeFile(in, inName, outFile, true);
		}
		
		// save preview
		in = data.getPreview();
		if ( in != null ) {
			inName = data.getPreviewName();
			if ( in == null ) {
				throw new MediaAlbumException(ERROR_PREVIEW,(Object[])null);
			}
			outFile = new File(wwwDir,inName);
			theme.setPreview(inName);
			saveThemeFile(in, inName, outFile, true);
		}
				
		// save support files
		if ( data.getSupportZip() != null ) {
			// for each file in zip, place in either WWW or APP 
			// directory based on file type... CSS, JS, HTML
			this.saveThemeFiles(theme, data.getSupportZip(),data.getSupportZipName());
		}
		
	} catch ( MediaAlbumException e ) {
		deleteThemeFiles(theme);
		throw e;
	}
}

private void saveThemeFiles(AlbumTheme theme, ZipInputStream zipIn, String zipName)
throws MediaAlbumException
{
	LOG.debug("Decoding theme zip support file");

	File wwwDir = new File(myBaseDirThemeWWW.getAbsolutePath() 
			+File.separator +theme.getBaseDir());
	File appDir = new File(myBaseDirThemeApp.getAbsolutePath() 
			+File.separator +theme.getBaseDir());
	
	try {
		while (true) {
			ZipEntry entry = zipIn.getNextEntry();
			if ( entry == null ) {
				break;
			}
			if ( entry.isDirectory() ) {
				zipIn.closeEntry();
				continue;
			}
			
			String fileName = entry.getName();
			String ext = StringUtil.substringAfter(fileName,'.');
			if ( !mySupportedThemeFileTypes.containsKey(ext) ) {
				throw new MediaAlbumException(ERROR_THEME_SUPPORT_TYPE,
						new Object[]{fileName});
			}
			
			File outFile = null;
			if ( ext.equals("xsl") ) {
				outFile = new File(appDir,fileName);
			} else {
				outFile = new File(wwwDir,fileName);
			}
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Saving theme zip entry " +fileName +" to " 
						+outFile.getPath());
			}
			saveThemeFile(zipIn,fileName,outFile,false);
			zipIn.closeEntry();
		}
	} catch (IOException e) {
		throw new MediaAlbumException("upload.file.error.general", 
				new Object[]{zipName, e.getMessage()});
	} finally {
		try {
			zipIn.close();
		} catch ( Exception e ) {
			// ignore
		}
	}
}



/**
 * Save a theme file.
 * 
 * @param in the input stream
 * @param inName the intput file name
 * @param outFile the output file
 * @param closeInput if <em>true</em> then close the input stream
 */
private void saveThemeFile(InputStream in, String inName, File outFile, boolean closeInput) 
throws MediaAlbumException
{
	OutputStream out = null;
	try {
		if ( !(in instanceof BufferedInputStream) ) {
			in = new BufferedInputStream(in);
		}

		// make sure parent dir exists
		outFile.getParentFile().mkdirs();
		
		out = new BufferedOutputStream(new FileOutputStream(outFile));
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Writing file " +outFile.getPath());
		}
		int size = FileUtil.copy(in,out,closeInput,true);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Wrote file " +outFile.getPath() +", " +size +" bytes");
		}
	} catch ( IOException e ) {
		LOG.error("IOException writing theme file '" +inName +"': " +e);
		outFile.delete();
		throw new MediaAlbumException(ERROR_FILE_GENERAL, 
				new Object[]{inName,e.getMessage()});
	}
}

/**
 * Delete all files associated with a theme.
 * 
 * @param theme the theme to delete files for
 */
private void deleteThemeFiles(AlbumTheme theme)
{
	String baseDir = theme.getBaseDir();
	if ( baseDir == null || baseDir.length() < 1 ) {
		return;
	}
	
	File f = new File(myBaseDirThemeApp,baseDir);
	FileUtil.deleteRecursive(f, true);
	f = new File(myBaseDirThemeWWW,baseDir);
	FileUtil.deleteRecursive(f, true);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getAlbumThemeData(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public AlbumThemeData getAlbumThemeData(Object themeId, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	AlbumTheme theme = getAlbumThemeById(themeId,actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	File wwwDir = new File(myBaseDirThemeWWW.getAbsolutePath()
			+File.separator +theme.getBaseDir());
	File appDir = new File(myBaseDirThemeApp.getAbsolutePath()
			+File.separator +theme.getBaseDir());
	AlbumThemeDataImpl data = new AlbumThemeDataImpl(theme,wwwDir,appDir);
	return data;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getAlbumThemesEditableForUser(magoffin.matt.ma.xsd.User, boolean)
 */
public AlbumTheme[] getAlbumThemesEditableForUser(User actingUser,
		boolean allowCached) throws MediaAlbumException 
{
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	if ( userBiz.isUserSuperUser(actingUser.getUserId()) ) {
		return getAllAlbumThemes(DEFAULT_SORT_MODE);
	}

	// only allowed to edit themes user owns
	return getAlbumThemesForOwner(actingUser, allowCached);
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getAlbumThemesViewableForUser(magoffin.matt.ma.xsd.User, int, boolean)
 */
public AlbumTheme[] getAlbumThemesViewableForUser(User actingUser,
		int sortMode, boolean allowCached) throws MediaAlbumException 
{
	AlbumTheme[] result = (AlbumTheme[])getCachedObject(
			allowCached,
			ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER,
			actingUser.getUserId());
	if ( result != null ) {
		return result;
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	if ( userBiz.isUserSuperUser(actingUser.getUserId()) ) {
		result = getAllAlbumThemes(sortMode);
		cacheObject(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER,
				actingUser.getUserId(),
				result);
		return result;
	}
	
	AlbumThemeCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumTheme.class);
		crit = (AlbumThemeCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumThemeCriteria.SEARCH_VIEWABLE_FOR_USER);
		crit.setQuery(actingUser.getUserId());
		// TODO set sort mode in criteria, let db sort
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		
		AlbumTheme[] results = (AlbumTheme[])dao.findByCriteria(crit);
		
		if ( results != null || results.length > 0 ) {
			result = results;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}

	result = sort(result,sortMode);
	cacheObject(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER, 
			actingUser.getUserId(),
			result);
	return result;
}

/**
 * Get all album themes.
 * 
 * @param sortMode the sort mode
 * @return album theme array
 */
private AlbumTheme[] getAllAlbumThemes(int sortMode) throws MediaAlbumException
{
	AlbumThemeCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumTheme.class);
		crit = (AlbumThemeCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumThemeCriteria.SEARCH_ALL);
		// TODO put sort into criteria
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		
		return sort((AlbumTheme[])dao.findByCriteria(crit),sortMode);
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	}
}

private AlbumTheme[] sort(AlbumTheme[] themes, int sortMode) {
	switch ( sortMode ) {
		case SORT_MODE_NAME:
			Arrays.sort(themes,SORT_BY_NAME);
			break;
		
		case SORT_MODE_DATE:
			Arrays.sort(themes,SORT_BY_DATE);
			break;
			
		case SORT_MODE_AUTHOR:
			Arrays.sort(themes,SORT_BY_AUTHOR);
			break;
	}
	return themes;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getGlobalAlbumThemes(magoffin.matt.ma.xsd.User, boolean)
 */
public AlbumTheme[] getGlobalAlbumThemes(User ignoreUser,
		boolean allowCached) throws MediaAlbumException 
{
	AlbumTheme[] result = (AlbumTheme[])getCachedObject(
			allowCached, 
			ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_GLOBALS,
			ignoreUser.getUserId());
	if ( result != null ) {
		return result;
	}
	
	AlbumThemeCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumTheme.class);
		crit = (AlbumThemeCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumThemeCriteria.SEARCH_BY_GLOBAL_EXCEPT_USER);
		crit.setQuery(ignoreUser.getUserId());
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumTheme.class);
		
		AlbumTheme[] results = (AlbumTheme[])dao.findByCriteria(crit);
		
		if ( results == null || results.length < 1 ) {
			return null;
		}
		
		result = results;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}

	cacheObject(ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_GLOBALS,
			ignoreUser.getUserId(),
			result);
	
	return result;
}

private String getUserThemeResourcePath(User actingUser, String path)
{
	return USER_THEME_DIR +File.separator +actingUser.getUserId()
	+File.separator +(path==null?"":path);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getUserThemeResource(magoffin.matt.ma.xsd.User, java.lang.String)
 */
public File getUserThemeResource(User actingUser, String path)
throws MediaAlbumException 
{
	File resource = new   File(myBaseDirThemeApp, 
			getUserThemeResourcePath(actingUser,path));
	
	if ( !resource.exists() ) {
		return null;
	}
	return resource;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#deleteUserThemeResource(magoffin.matt.ma.xsd.User, java.lang.String)
 */
public void deleteUserThemeResource(User actingUser, String path)
throws MediaAlbumException 
{
	File f = getUserThemeResource(actingUser,path);
	if ( f != null ) {
		if ( !f.delete() ) {
			LOG.warn("Unable to delete user theme resource: " +f.getAbsolutePath());
		}
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#saveUserThemeResource(magoffin.matt.ma.xsd.User, java.io.InputStream, java.lang.String)
 */
public void saveUserThemeResource(User actingUser, InputStream in, String path)
throws MediaAlbumException 
{
	File f = new File(myBaseDirThemeApp,getUserThemeResourcePath(actingUser,path));
	saveThemeFile(in,path,f,true);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz#getThemeMetaData(java.lang.Integer, boolean)
 */
public ThemeMetaData getThemeMetaData(Integer themeId, boolean allowCached)
throws MediaAlbumException 
{
	ThemeMetaData result = (ThemeMetaData)getCachedObject(
			allowCached, 
			ApplicationConstants.CacheFactoryKeys.THEME_META_DATA,
			themeId);
	if ( result != null ) {
		return result;
	}
	
	// see if default theme requested
	boolean isDefault = false;
	AlbumTheme defTheme = getDefaultAlbumTheme();
	if ( defTheme.getThemeId().equals(themeId) ) {
		isDefault = true;
	}
	
	int albumsUsing = 0;
	int browseUsing = 0;
	
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		if ( isDefault ) {
			crit.setSearchType(AlbumCriteria.ALBUMS_USING_DEFAULT_THEME_COUNT);
		} else {
			crit.setSearchType(AlbumCriteria.ALBUMS_USING_THEME_COUNT);
		}
		crit.setQuery(themeId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		DataObject[] data = dao.findByCriteria(crit);
		
		if ( !(data instanceof CountData[]) ) {
			LOG.error("Expecting " +CountData.class +" result from DAO but got "
					+(data == null ? "null" : data.getClass().toString()));
			throw new MediaAlbumException("DAO not configured properly: " +data);
		}
		
		albumsUsing = ((CountData[])data)[0].getCount();
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	// get # users using theme (for browse)
	
	UserCriteria userCrit = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				User.class);
		userCrit = (UserCriteria)borrowObjectFromPool(pool);
		
		if ( isDefault ) {
			userCrit.setSearchType(UserCriteria.USERS_USING_DEFAULT_THEME_COUNT);
		} else {
			userCrit.setSearchType(UserCriteria.USERS_USING_THEME_COUNT);
		}
		userCrit.setQuery(themeId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		DataObject[] data = dao.findByCriteria(userCrit);
		
		if ( !(data instanceof CountData[]) ) {
			LOG.error("Expecting " +CountData.class +" result from DAO but got "
					+(data == null ? "null" : data.getClass().toString()));
			throw new MediaAlbumException("DAO not configured properly: " +data);
		}
		
		browseUsing = ((CountData[])data)[0].getCount();
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,userCrit);
	}
	
	result = new ThemeMetaData();
	result.setThemeId(themeId.intValue());
	result.setAlbumsUsing(albumsUsing);
	result.setBrowseUsing(browseUsing);

	cacheObject(ApplicationConstants.CacheFactoryKeys.THEME_META_DATA,
			themeId,	result);
	
	return result;
}

}
