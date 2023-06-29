/* ===================================================================
 * MockSystemBiz.java
 * 
 * Created Oct 22, 2012 10:40:47 AM
 * 
 * Copyright (c) 2012 Matt Magoffin.
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
 */

package magoffin.matt.ma2.biz.impl;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import javax.annotation.PostConstruct;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.dao.ThemeDao;
import magoffin.matt.ma2.dao.TimeZoneDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Locale;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.plugin.Plugin;
import magoffin.matt.ma2.support.AddThemeCommand;
import magoffin.matt.xweb.util.XwebParamDao;
import org.springframework.core.io.Resource;

/**
 * Mock implementation of {@link SystemBiz} for unit tests.
 * 
 * @author matt
 * @version 1.0
 */
public class MockSystemBiz implements SystemBiz {

	@javax.annotation.Resource
	private final XwebParamDao settingsDao = null;
	@javax.annotation.Resource
	private final ThemeDao themeDao = null;
	@javax.annotation.Resource
	private final TimeZoneDao timeZoneDao = null;
	@javax.annotation.Resource
	private final DomainObjectFactory domainObjectFactory = null;

	@PostConstruct
	public void init() {
		initTimeZones();
	}

	private void initTimeZones() {
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		if ( tzList.size() < 1 ) {
			// initialize time zone values into db
			// initialize time zones list in ascending order
			String[] ids = java.util.TimeZone.getAvailableIDs();
			for ( int i = 0; i < ids.length; i++ ) {
				java.util.TimeZone currTz = java.util.TimeZone.getTimeZone(ids[i]);
				String name = currTz.getDisplayName();
				if ( !(name.startsWith("GMT") && name.endsWith(":00")) ) {
					continue;
				}
				name = name.substring(0, name.length() - 3);
				if ( name.endsWith("00") ) {
					name = "GMT";
				}

				TimeZone tz = domainObjectFactory.newTimeZoneInstance();
				tz.setCode(currTz.getID());
				tz.setName(name);
				tz.setOffset(currTz.getRawOffset());
				tz.setOrdering(i);

				timeZoneDao.store(tz);
			}
		}
	}

	public List<TimeZone> getAvailableTimeZones() {
		return timeZoneDao.findAllTimeZones();
	}

	public List<Locale> getAvailableLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	public TimeZone getTimeZoneForCode(String code) {
		return timeZoneDao.get(code);
	}

	public TimeZone getDefaultTimeZone() {
		return getTimeZoneForCode("Etc/GMT");
	}

	public File getCollectionRootDirectory() {
		File dir = new File(System.getProperty("java.io.tmpdir"), "matte-collections");
		if ( !dir.exists() ) {
			dir.mkdirs();
		}
		return dir;
	}

	public File getCacheDirectory() {
		File dir = new File(System.getProperty("java.io.tmpdir"), "matte-cache");
		if ( !dir.exists() ) {
			dir.mkdirs();
		}
		return dir;
	}

	public File getResourceDirectory() {
		File dir = new File(System.getProperty("java.io.tmpdir"), "matte-resources");
		if ( !dir.exists() ) {
			dir.mkdirs();
		}
		return dir;
	}

	public Theme getDefaultTheme() {
		// TODO Auto-generated method stub
		return null;
	}

	public Theme getThemeById(Long themeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Theme> getAvailableThemes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Long storeTheme(Theme theme, BizContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	public Resource getThemeResource(Theme theme, String path, BizContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	public Long storeTheme(AddThemeCommand themeCommand, BizContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	public void exportTheme(Theme theme, OutputStream out, File baseDirectory, BizContext context) {
		// TODO Auto-generated method stub

	}

	public void deleteTheme(Theme theme, BizContext context) {
		// TODO Auto-generated method stub

	}

	public String getSharedAlbumUrl(Album album, BizContext context) {
		return "http://my.url/here?" + album.getAnonymousKey();
	}

	public boolean isApplicationConfigured() {
		return true;
	}

	public <T extends Plugin> List<T> getPluginsOfType(Class<T> pluginType) {
		// TODO Auto-generated method stub
		return null;
	}

}
