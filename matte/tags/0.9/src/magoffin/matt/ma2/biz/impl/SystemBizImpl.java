/* ===================================================================
 * SystemBizImpl.java
 * 
 * Created Feb 3, 2006 10:36:39 AM
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
 * $Id: SystemBizImpl.java,v 1.28 2007/09/25 06:24:02 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import magoffin.matt.ma2.ConfigurationException;
import magoffin.matt.ma2.ValidationException;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.ThemeDao;
import magoffin.matt.ma2.dao.TimeZoneDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.plugin.Plugin;
import magoffin.matt.ma2.support.AddThemeCommand;
import magoffin.matt.ma2.web.util.WebBizContext;
import magoffin.matt.util.StringMerger;
import magoffin.matt.xweb.XwebParameter;
import magoffin.matt.xweb.util.MessagesSource;
import magoffin.matt.xweb.util.XwebParamDao;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

/**
 * Standard implementation of {@link magoffin.matt.ma2.biz.UserBiz}.
 * 
 * <p><b>Note:</b> the {@link #init()} method should be called after 
 * configuring this class but before calling any other methods; the 
 * {@link #finish()} should be called when finished using.</p>
 * 
 * <p>The {@link #init()} method will attempt to populate the backend 
 * database with all available time zones if none are found in the 
 * database already.</p>
 * 
 * <dl class="class-properties">
 *   <dt>cacheDirectory</dt>
 *   <dd>The directory for creating and storing cached media items.</dd>
 *   
 *   <dt>collectionRootDirectory</dt>
 *   <dd>The root directory for storing collections, i.e. the root directory
 *   for all media items.</dd>
 *   
 *   <dt>defaultTimeZoneCode</dt>
 *   <dd>The default TimeZone code to use. Defaults to 
 *   {@link java.util.TimeZone#getDefault()}'s <code>getID()</code>.</dd>
 *   
 *   <dt>domainObjectFactory</dt>
 *   <dd>The {@link magoffin.matt.ma2.biz.DomainObjectFactory} implementation
 *   to use for creating instances of our domain objects.</dd>
 *   
 *   <dt>externalThemeDirectory</dt>
 *   <dd>The path to the directory for storing external theme resources.</dd>
 *   
 *   <dt>themeDao</dt>
 *   <dd>The {@link magoffin.matt.ma2.dao.ThemeDao} implementation 
 *   to use.</dd>
 *   
 *   <dt>timeZoneDao</dt>
 *   <dd>The {@link magoffin.matt.ma2.dao.TimeZoneDao} implementation 
 *   to use.</dd>
 *   
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.28 $ $Date: 2007/09/25 06:24:02 $
 */
public class SystemBizImpl implements SystemBiz, 
ApplicationContextAware, ApplicationListener {
	
	/** The theme created date property date format. */
	public static final String THEME_PROPERTY_CREATED_DATE_FORMAT = "yyyy-MM-dd";

	/** The model key where the shared Album is placed. */
	public static final String SHARED_ALBUM_TEMPLATE_KEY = "album";
	
	/** The plugin configuration key. */
	public static final String PLUGIN_CONFIG_KEY = "matte.plugin";
	
	private static final String THEME_TEMP_BASE_PATH = "/temp.base.path";
	
	private DomainObjectFactory domainObjectFactory = null;
	private AlbumDao albumDao = null;
	private XwebParamDao settingsDao = null;
	private ThemeDao themeDao = null;
	private TimeZoneDao timeZoneDao = null;
	private String defaultTimeZoneCode = java.util.TimeZone.getDefault().getID();
	private File collectionRootDirectory = null;
	private File cacheDirectory = null;
	private File resourceDirectory = null;
	private String defaultThemeName = "Woosh";
	private Map<String,Object> defaultThemeTemplate = null;
	private String sharedAlbumUrlTemplate = null;
	private String externalThemeDirectory = null;
	private MessagesSource messages = null;
	
	private List<TimeZone> myTimeZones = Collections.emptyList();
	private List<magoffin.matt.ma2.domain.Locale> myLocales 
		= Collections.emptyList();
	private TimeZone myDefaultTimeZone = null;
	private Theme myDefaultTheme = null;
	private File externalThemeDirFile = null;
	private boolean setupComplete = false;
	private Map<Class<? extends Plugin>, List<Plugin>> plugins 
		= new LinkedHashMap<Class<? extends Plugin>, List<Plugin>>();
	private ApplicationContext applicationContext = null;
	
	private final Logger log = Logger.getLogger(SystemBizImpl.class);
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;	
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onApplicationEvent(ApplicationEvent event) {
		if ( !(event instanceof ContextRefreshedEvent) ) {
			return;
		}
		ContextRefreshedEvent cre = (ContextRefreshedEvent)event;
		if ( !(cre.getApplicationContext() == this.applicationContext) ) {
			return;
		}
		try {
			registerPlugins();
		} catch ( IOException e ) {
			throw new RuntimeException("Error registering plugins", e);
		}
	}

	/**
	 * Call to initialize the class after configuring properties.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void init() {
		if ( domainObjectFactory == null ) {
			throw new ConfigurationException(null,"domainObjectFactory");
		}
		if ( albumDao == null ) {
			throw new ConfigurationException(null,"albumDao");
		}
		if ( themeDao == null ) {
			throw new ConfigurationException(null,"themeDao");
		}
		if ( timeZoneDao == null ) {
			throw new ConfigurationException(null,"timeZoneDao");
		}
		if ( collectionRootDirectory == null ) {
			throw new ConfigurationException(null,"collectionRootDirectory");
		}
		if ( resourceDirectory == null ) {
			throw new ConfigurationException(null,"resourceDirectory");
		}
		if ( defaultThemeName == null ) {
			throw new ConfigurationException(null,"defaultThemeName");
		}
		if ( sharedAlbumUrlTemplate == null ) {
			throw new ConfigurationException(null,"sharedAlbumUrlTemplate");
		}
		if ( externalThemeDirectory == null ) {
			throw new ConfigurationException(null, "externalThemeDirectory");
		}
		if ( settingsDao == null ) {
			throw new ConfigurationException(null, "settingsDao");
		}
		if ( messages == null ) {
			throw new ConfigurationException(null, "messages");
		}
		
		XwebParameter param = settingsDao.getParameter(SETTING_KEY_SETUP_COMPLETE);
		setupComplete = param != null 
			&& Boolean.TRUE.toString().equals(param.getValue());
		
		if ( !setupComplete ) {
			log.warn("Setup not complete, not continuing initialization.");
			return;
		}
		
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		
		if ( tzList.size() < 1 ) {
			// initialize time zone values into db
			// initialize time zones list in ascending order
			String[] ids = java.util.TimeZone.getAvailableIDs();
			for ( int i = 0; i < ids.length; i++ ) {
				java.util.TimeZone currTz = java.util.TimeZone.getTimeZone(ids[i]);
				TimeZone tz = domainObjectFactory.newTimeZoneInstance();
				tz.setCode(currTz.getID());
				
				String name = currTz.getDisplayName();
				if ( name.startsWith("GMT") && name.endsWith(":00") ) {
					name = name.substring(0,name.length()-3);
					if ( name.endsWith("00") ) {
						name = "GMT";
					}
				} else {
					name = currTz.getID();
				}
				tz.setName(name);
				tz.setOffset(currTz.getRawOffset());
				tz.setOrdering(i);
				
				tzList.add(tz);
				timeZoneDao.store(tz);
			}
		}
		
		this.myTimeZones = Collections.unmodifiableList(tzList);
		
		// setup default time zone
		this.myDefaultTimeZone = timeZoneDao.get(this.defaultTimeZoneCode);
		if ( this.myDefaultTimeZone == null ) {
			log.warn("The defaultTimeZoneCode [" +this.defaultTimeZoneCode 
					+"] is not available in the database");
			throw new ConfigurationException(this.defaultTimeZoneCode,
					"defaultTimeZoneCode");
		}
		
		// TODO store locales in DB, so can prune list like time zones
		Locale[] locales = Locale.getAvailableLocales();
		List<magoffin.matt.ma2.domain.Locale> localeList 
			= new LinkedList<magoffin.matt.ma2.domain.Locale>();
		Arrays.sort(locales, new Comparator<Locale>() {
			public int compare(Locale o1, Locale o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		for ( int i = 0, len = locales.length; i < len; i++ ) {
			Locale locale = locales[i];
 			if ( locale.getCountry().length() < 1 || locale.getLanguage().length() < 1 ) {
				continue; // only use country/language locales
			}
			magoffin.matt.ma2.domain.Locale l = domainObjectFactory.newLocaleInstance();
			l.setCode(locale.toString());
			l.setName(locale.getDisplayName());
			l.setOrdering(i);
			localeList.add(l);
		}
		myLocales = localeList;
		
		// setup default theme
		Theme defaultTheme = themeDao.getThemeForName(this.defaultThemeName);
		if ( defaultTheme != null ) {
			this.myDefaultTheme = defaultTheme;
		} else {
			// create default theme now
			if ( this.defaultThemeTemplate == null ) {
				throw new ConfigurationException(null,"defaultThemeTemplate");
			}
			defaultTheme = domainObjectFactory.newThemeInstance();
			BeanWrapper wrapper = new BeanWrapperImpl(defaultTheme);
			wrapper.setPropertyValues(this.defaultThemeTemplate);
			prepareThemeForStore(defaultTheme,null);
			Long defaultThemeId = themeDao.store(defaultTheme);
			this.myDefaultTheme = themeDao.get(defaultThemeId);
			
			if ( log.isInfoEnabled() ) {
				log.info("Default Theme [" +this.myDefaultTheme.getName() +"] stored.");
			}
		}
		
		// setup external theme dir file
		this.externalThemeDirFile = new File(this.externalThemeDirectory);
		if ( !externalThemeDirFile.isDirectory() && !externalThemeDirFile.mkdirs() ) {
			log.warn("External theme path is not a directory: " +this.externalThemeDirectory);
		}
	}
	
	private void registerPlugins() throws IOException {
		Enumeration<URL> pluginConfigs = getClass().getClassLoader().getResources(
				"META-INF/matte-plugin.properties");
		int numConfigs = 0;
		int numPlugins = 0;
		while ( pluginConfigs.hasMoreElements() ) {
			URL pluginConfigUrl = pluginConfigs.nextElement();
			numConfigs++;
			if ( log.isDebugEnabled() ) {
				log.debug("Processing plugin resource [" +pluginConfigUrl +"]");
			}
			Properties pluginConfigProps = new Properties();
			pluginConfigProps.load(pluginConfigUrl.openStream());
			for ( Object configKey : pluginConfigProps.keySet() ) {
				String key = configKey.toString();
				if ( PLUGIN_CONFIG_KEY.equalsIgnoreCase(key) ) {
					String pluginNames = pluginConfigProps.getProperty(key, "UNKOWN");
					for ( String pluginName : pluginNames.split("[, ]+") ) {
						try {
							if ( registerPlugin(pluginConfigProps, pluginName) ) {
								numPlugins++;
							}
						} catch ( Exception e ) {
							log.error("Unable to register plugin [" +pluginName +']', e);
						}
					}
				}
			}
		}
		if ( log.isInfoEnabled() ) {
			log.info("Processed " +numConfigs + " plugin configuration resources");
			log.info("Registered " +numPlugins + " plugins");
		}
	}

	private boolean registerPlugin(Properties pluginConfigProps, String pluginName) 
	throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String pluginPrefix = PLUGIN_CONFIG_KEY +'.' +pluginName +'.';
		String pluginClassNameKey = pluginPrefix +"class";
		String pluginClassName = pluginConfigProps.getProperty(pluginClassNameKey);
		if ( pluginClassName == null ) {
			log.warn("Plugin [" +pluginClassNameKey +"] class property not found, skipping");
			return false;
		}
		Class<?> pluginClass = Class.forName(pluginClassName);
		Class<? extends Plugin> pluginClazz = pluginClass.asSubclass(Plugin.class);
		Plugin plugin = pluginClazz.newInstance();
		if ( log.isDebugEnabled() ) {
			log.debug("Instantiated plugin [" +pluginName +"] class ["
					+pluginClazz.getName() +']');
		}
		plugin.initialize(this.applicationContext);
		String[] msgResourceNames = plugin.getMessageResourceNames();
		if ( msgResourceNames != null ) {
			for ( String msgResourceName : msgResourceNames ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Registering message resource [" +msgResourceName +']');
				}
				this.messages.registerMessageResource(msgResourceName);
			}
		}
		List<Plugin> pluginList = this.plugins.get(plugin.getPluginType());
		if ( pluginList == null ) {
			pluginList = new LinkedList<Plugin>();
			this.plugins.put(plugin.getPluginType(), pluginList);
		}
		pluginList.add(plugin);
		return true;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getPluginsOfType(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Plugin> List<T> getPluginsOfType(Class<T> pluginType) {
		if ( this.plugins.containsKey(pluginType) ) {
			return Collections.unmodifiableList((List<T>)this.plugins.get(pluginType));
		}
		return Collections.emptyList();
	}

	/**
	 * Call to release any class resources when finished using.
	 */
	public void finish() {
		this.myTimeZones = Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#isApplicationConfigured()
	 */
	public boolean isApplicationConfigured() {
		return setupComplete;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getAvailableThemes()
	 */
	public List<Theme> getAvailableThemes() {
		return themeDao.findAllThemes();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getThemeById(java.lang.Long)
	 */
	public Theme getThemeById(Long themeId) {
		return themeDao.get(themeId);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getAvailableTimeZones()
	 */
	public List<TimeZone> getAvailableTimeZones() {
		return this.myTimeZones;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getAvailableLocales()
	 */
	public List<magoffin.matt.ma2.domain.Locale> getAvailableLocales() {
		return myLocales;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getTimeZoneForCode(java.lang.String)
	 */
	public TimeZone getTimeZoneForCode(String code) {
		return timeZoneDao.get(code);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getDefaultTimeZone()
	 */
	public TimeZone getDefaultTimeZone() {
		return this.myDefaultTimeZone;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getCollectionRootDirectory()
	 */
	public File getCollectionRootDirectory() {
		return this.collectionRootDirectory;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getThemeResource(magoffin.matt.ma2.domain.Theme, java.lang.String, magoffin.matt.ma2.biz.BizContext)
	 */
	public Resource getThemeResource(Theme theme, String path, BizContext context) {
		String resourcePath = this.externalThemeDirFile.getAbsolutePath() 
			+theme.getBasePath()
			+(path.startsWith("/") ? path : "/"+path);
		return new FileSystemResource(resourcePath);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getCacheDirectory()
	 */
	public File getCacheDirectory() {
		return cacheDirectory;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getDefaultTheme()
	 */
	public Theme getDefaultTheme() {
		return myDefaultTheme;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#storeTheme(magoffin.matt.ma2.domain.Theme, magoffin.matt.ma2.biz.BizContext)
	 */
	public Long storeTheme(Theme theme, BizContext context) {
		prepareThemeForStore(theme, context);
		return themeDao.store(theme);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#getSharedAlbumUrl(magoffin.matt.ma2.domain.Album, magoffin.matt.ma2.biz.BizContext)
	 */
	public String getSharedAlbumUrl(Album album, BizContext context) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put(SHARED_ALBUM_TEMPLATE_KEY, album);
		String url = StringMerger.mergeString(this.sharedAlbumUrlTemplate, "",	model);
		if ( context != null && context.getAttribute(
				WebBizContext.URL_BASE) != null ) {
			url = context.getAttribute(WebBizContext.URL_BASE) +url;
		}
		return url;
	}

	private void prepareThemeForStore(Theme theme, BizContext context) {
		if ( context != null && theme.getOwner() == null ) {
			theme.setOwner(context.getActingUser());
		}
		if ( theme.getCreationDate() == null ) {
			theme.setCreationDate(Calendar.getInstance());
		}
		if ( theme.getThemeId() != null ) {
			theme.setModifyDate(Calendar.getInstance());
		}
		if ( theme.getBasePath() == null ) {
			theme.setBasePath(THEME_TEMP_BASE_PATH);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#storeTheme(magoffin.matt.ma2.util.AddThemeCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public Long storeTheme(AddThemeCommand themeCommand, BizContext context) {
		Theme theme = themeCommand.getTheme();
		if ( theme == null ) {
			theme = getDomainObjectFactory().newThemeInstance();
		}
		// see if theme properties available
		Properties themeProperties = new Properties();
		try {
			ZipInputStream in = new ZipInputStream(
					themeCommand.getTempFile().getInputStream());
			for ( ZipEntry entry = in.getNextEntry(); entry != null; 
					entry = in.getNextEntry() ) {
				if ( THEME_PROPERTIES_FILE_NAME.equals(entry.getName()) ) {
					themeProperties.load(in);
					break;
				}
			}			
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		if ( themeProperties.containsKey(THEME_PROPERTY_AUTHOR) ) {
			theme.setAuthor(themeProperties.getProperty(THEME_PROPERTY_AUTHOR));
		}
		if ( themeProperties.containsKey(THEME_PROPERTY_AUTHOR_EMAIL) ) {
			theme.setAuthorEmail(themeProperties.getProperty(THEME_PROPERTY_AUTHOR_EMAIL));
		}
		if ( themeProperties.containsKey(THEME_PROPERTY_CREATED_DATE) ) {
			String dateStr = themeProperties.getProperty(THEME_PROPERTY_CREATED_DATE);
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(THEME_PROPERTY_CREATED_DATE_FORMAT);
				Date cDate = sdf.parse(dateStr);
				Calendar cCal = Calendar.getInstance();
				cCal.setTime(cDate);
				theme.setCreationDate(cCal);
			} catch ( ParseException e ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Unable to parse theme date [" +dateStr +"]");
				}
			}
		}
		if ( themeProperties.containsKey(THEME_PROPERTY_NAME) ) {
			theme.setName(themeProperties.getProperty(THEME_PROPERTY_NAME));
		}
		
		// now store theme so generate ID
		prepareThemeForStore(theme, context);
		Long themeId = themeDao.store(theme);
		theme = themeDao.get(themeId);
		if ( THEME_TEMP_BASE_PATH.equals(theme.getBasePath()) ) {
			theme.setBasePath("/"+themeId);
			theme = themeDao.get(themeDao.store(theme));
		}
		
		// save theme resource files
		ZipInputStream in = null;
		try {
			in = new ZipInputStream(
				themeCommand.getTempFile().getInputStream());
			boolean hasXslt = false;
			int count = 0;
			for ( ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry() ) {
				if ( THEME_XSLT_FILE_NAME.equals(entry.getName()) ) {
					hasXslt = true;
				}
				if ( entry.isDirectory() ) {
					continue;
				}
				File outputFile = new File(this.externalThemeDirFile, 
						theme.getBasePath()+"/"+entry.getName());
				outputFile.getParentFile().mkdirs();
				if ( log.isDebugEnabled() ) {
					log.debug("Saving theme resource [" 
							+outputFile.getAbsolutePath() +"]");
				}
				FileOutputStream out = new FileOutputStream(outputFile);
				try {
					copy(in, out);
				} finally {
					try {
						out.close();
					} catch (IOException ex) {
						log.warn("Could not close OutputStream", ex);
					}
				}

				count++;
			}
			if ( log.isDebugEnabled() ) {
				log.debug("Saved " +count +" resources for theme [" 
						+theme.getThemeId() +"]");
			}
			if ( !hasXslt ) {
				BindException errors = new BindException(themeCommand, "theme");
				errors.addError(new ObjectError("theme", 
						new String[] {"theme.add.missing.xslt"},
						null, "No XSLT provided with theme"));
				throw new ValidationException(errors);
			}
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch ( IOException e ) {
					log.warn("Unable to close input stream");
				}
			}
		}
		return themeId;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#exportTheme(magoffin.matt.ma2.domain.Theme, java.io.OutputStream, java.io.File, magoffin.matt.ma2.biz.BizContext)
	 */
	public void exportTheme(Theme theme, OutputStream out, File baseDirectory, 
			BizContext context) {
		if ( baseDirectory == null ) {
			baseDirectory = this.externalThemeDirFile;
		}
		baseDirectory = new File(baseDirectory, theme.getBasePath());
		ZipOutputStream zout = new ZipOutputStream(out);
		try {
			exportThemeFiles(baseDirectory, baseDirectory, zout);
			File[] themeProps = baseDirectory.listFiles(new FilenameFilter() {
				public boolean accept(File file, String name) {
					return THEME_PROPERTIES_FILE_NAME.equals(name);
				}
			});
			if (  themeProps == null || themeProps.length < 1 ) {
				// create theme properties file
				Properties props = new Properties();
				props.put(THEME_PROPERTY_AUTHOR, theme.getAuthor());
				props.put(THEME_PROPERTY_AUTHOR_EMAIL, theme.getAuthorEmail());
				SimpleDateFormat sdf = new SimpleDateFormat(
						THEME_PROPERTY_CREATED_DATE_FORMAT);
				props.put(THEME_PROPERTY_CREATED_DATE, sdf.format(
						theme.getCreationDate().getTime()));
				props.put(THEME_PROPERTY_NAME, theme.getName());
				ZipEntry entry = new ZipEntry(THEME_PROPERTIES_FILE_NAME);
				zout.putNextEntry(entry);
				props.store(zout, "Created by Matte on " 
						+sdf.format(new Date()));
			}
			zout.finish();
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	private void exportThemeFiles(File rootDirectory, File baseDirectory, 
			ZipOutputStream zout) throws IOException {
		File[] files = baseDirectory.listFiles();
		String rootPath = rootDirectory.getAbsolutePath();
		for ( File f : files ) {
			if ( f.isDirectory() ) {
				exportThemeFiles(rootDirectory, f, zout);
				continue;
			}
			String zipPath = f.getAbsolutePath().substring(rootPath.length()+1);
			ZipEntry entry = new ZipEntry(zipPath);
			zout.putNextEntry(entry);
			InputStream in = new FileInputStream(f);
			if ( log.isDebugEnabled() ) {
				log.debug("Exporting theme resource [" +f.getAbsolutePath() +"]");
			}
			try {
				copy(in, zout);
			} finally {
				try {
					in.close();
				} catch (IOException ex) {
					log.warn("Could not close InputStream", ex);
				}
			}
			zout.closeEntry();
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SystemBiz#deleteTheme(magoffin.matt.ma2.domain.Theme, magoffin.matt.ma2.biz.BizContext)
	 */
	public void deleteTheme(Theme theme, BizContext context) {
		File themeDir = new File(this.externalThemeDirFile, theme.getBasePath());
		if ( !themeDir.exists() ) {
			BindException errors = new BindException(theme,"theme");
			errors.reject("delete.theme.notfound", new Object[] {theme.getName()}, 
					"Theme [" +theme.getName() +"] cannot be deleted (not found).");
			throw new ValidationException(errors);
		}
		
		int count = albumDao.reassignAlbumsUsingTheme(theme, getDefaultTheme());
		if ( log.isDebugEnabled() ) {
			log.debug("Reassigned " +count + " albums using theme [" 
					+theme.getThemeId() +"]");
		}
		
		// delete theme from db
		themeDao.delete(theme);
		
		// delete theme files
		deleteDirNested(themeDir);
	}
	
	private void deleteDirNested(File baseDir) {
		for ( File f : baseDir.listFiles() ) {
			if ( f.isDirectory() ) {
				deleteDirNested(f);
			} else {
				if ( log.isDebugEnabled() ) {
					log.debug("Deleting file [" +f.getAbsolutePath() +"]");
				}
				f.delete();
			}
		}
		if ( log.isDebugEnabled() ) {
			log.debug("Deleting directory [" +baseDir.getAbsolutePath() +"]");
		}
		baseDir.delete();
	}

	/**
	 * Adapted from Spring's FileCopyUtils, but does not close the 
	 * input or output streams.
	 * 
	 * @param in the input stream
	 * @param out the output stream
	 * @return the number of bytes copied
	 * @throws IOException if an error occurs
	 */
	private int copy(InputStream in, OutputStream out) throws IOException {
		int byteCount = 0;
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
			byteCount += bytesRead;
		}
		out.flush();
		return byteCount;
	}

	/**
	 * @return Returns the domainObjectFactory.
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory The domainObjectFactory to set.
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}
	
	/**
	 * @return Returns the timeZoneDao.
	 */
	public TimeZoneDao getTimeZoneDao() {
		return timeZoneDao;
	}
	
	/**
	 * @param timeZoneDao The timeZoneDao to set.
	 */
	public void setTimeZoneDao(TimeZoneDao timeZoneDao) {
		this.timeZoneDao = timeZoneDao;
	}
	
	/**
	 * @return Returns the defaultTimeZoneCode.
	 */
	public String getDefaultTimeZoneCode() {
		return defaultTimeZoneCode;
	}
	
	/**
	 * @param defaultTimeZoneCode The defaultTimeZoneCode to set.
	 */
	public void setDefaultTimeZoneCode(String defaultTimeZoneCode) {
		this.defaultTimeZoneCode = defaultTimeZoneCode;
	}

	/**
	 * @param collectionRootDirectory The collectionRootDirectory to set.
	 */
	public void setCollectionRootDirectory(File collectionRootDirectory) {
		this.collectionRootDirectory = collectionRootDirectory;
	}

	/**
	 * @param cacheDirectory The cacheDirectory to set.
	 */
	public void setCacheDirectory(File cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}

	/**
	 * @return Returns the themeDao.
	 */
	public ThemeDao getThemeDao() {
		return themeDao;
	}

	/**
	 * @param themeDao The themeDao to set.
	 */
	public void setThemeDao(ThemeDao themeDao) {
		this.themeDao = themeDao;
	}

	/**
	 * @return Returns the defaultThemeName.
	 */
	public String getDefaultThemeName() {
		return defaultThemeName;
	}

	/**
	 * @param defaultThemeName The defaultThemeName to set.
	 */
	public void setDefaultThemeName(String defaultThemeName) {
		this.defaultThemeName = defaultThemeName;
	}

	/**
	 * @return Returns the defaultThemeTemplate.
	 */
	public Map<String, Object> getDefaultThemeTemplate() {
		return defaultThemeTemplate;
	}

	/**
	 * @param defaultThemeTemplate The defaultThemeTemplate to set.
	 */
	public void setDefaultThemeTemplate(Map<String, Object> defaultThemeTemplate) {
		this.defaultThemeTemplate = defaultThemeTemplate;
	}

	/**
	 * @return Returns the sharedAlbumUrlTemplate.
	 */
	public String getSharedAlbumUrlTemplate() {
		return sharedAlbumUrlTemplate;
	}

	/**
	 * @param sharedAlbumUrlTemplate The sharedAlbumUrlTemplate to set.
	 */
	public void setSharedAlbumUrlTemplate(String sharedAlbumUrlTemplate) {
		this.sharedAlbumUrlTemplate = sharedAlbumUrlTemplate.replaceAll(
				"\\$\\[([^\\]]+)\\]","\\${$1}");
	}
	
	/**
	 * @return the externalThemeDirectory
	 */
	public String getExternalThemeDirectory() {
		return externalThemeDirectory;
	}
	
	/**
	 * @param externalThemeDirectory the externalThemeDirectory to set
	 */
	public void setExternalThemeDirectory(String externalThemeDirectory) {
		this.externalThemeDirectory = externalThemeDirectory;
	}

	/**
	 * @return the albumDao
	 */
	public AlbumDao getAlbumDao() {
		return albumDao;
	}
	
	/**
	 * @param albumDao the albumDao to set
	 */
	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}
	
	/**
	 * @return the settingsDao
	 */
	public XwebParamDao getSettingsDao() {
		return settingsDao;
	}

	/**
	 * @param settingsDao the settingsDao to set
	 */
	public void setSettingsDao(XwebParamDao settingsDao) {
		this.settingsDao = settingsDao;
	}

	/**
	 * @return the resourceDirectory
	 */
	public File getResourceDirectory() {
		return resourceDirectory;
	}

	/**
	 * @param resourceDirectory the resourceDirectory to set
	 */
	public void setResourceDirectory(File resourceDirectory) {
		this.resourceDirectory = resourceDirectory;
	}

	/**
	 * @return the messages
	 */
	public MessagesSource getMessages() {
		return messages;
	}

	/**
	 * @param messages the messages to set
	 */
	public void setMessages(MessagesSource messages) {
		this.messages = messages;
	}
	
}
