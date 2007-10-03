/* ===================================================================
 * AbstractJdbcBrowseModePlugin.java
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: AbstractJdbcBrowseModePlugin.java,v 1.3 2007/10/01 01:53:21 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.support;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.plugin.BrowseModePlugin;
import magoffin.matt.ma2.plugin.Plugin;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ClassUtils;

/**
 * Base class for JDBC based implementations of {@link BrowseModePlugin}.
 * 
 * <p>The class is a useful class to extend for JDBC-based implementations of
 * the {@link BrowseModePlugin} API. This class provides a common framework for 
 * initializing the plugin instance with a Spring context file. The 
 * {@link #initialize(ApplicationContext)} method works in the following way:</p>
 * 
 * <ol>
 *   <li>Looks for a classpath resource named <b><em>classname</em>Context.xml</b>. 
 *   For example, if the implementing class is named <em>MyPlugin</em> then 
 *   this looks for a file named <em>MyPluginContext.xml</em>.</li>
 *   
 *   <li>If the previous step does not find the resource, it looks for a 
 *   classpath resource named <b>META-INF/<em>classname</em>Context.xml</b>. This
 *   is the fall-back default configuration, that should ship with the plugin
 *   code. The first resource path is a way for the user to customize their
 *   plugin configuration, while this resource path is the fallback default.</li>
 *   
 *   <li>Creates a new {@link ClassPathXmlApplicationContext} from the resource
 *   found in either step #1 or #2, above.</li>
 *   
 *   <li>Calls {@link ClassPathXmlApplicationContext#setParent(ApplicationContext)},
 *   passing in the {@code ApplicationContext} passed to this method.</li>
 *   
 *   <li>Calls {@link ClassPathXmlApplicationContext#refresh()} to initialize the
 *   context. This context should define a bean named the <em>full class
 *   name of this plugin implementation</em>. This bean should be a fully-configured
 *   instance of this plugin.</li>
 *   
 *   <li>Gets the class-plugin bean from the {@code ClassPathXmlApplicationContext}
 *   and uses {@link BeanUtils#copyProperties(Object, Object)} to copy the properties
 *   of that instance onto this instance.</li>
 *   
 *   <li>Calls the {@link #init(ApplicationContext)} method, for the implementation
 *   to perform any custom initialization.</li>
 * </ol>
 * 
 * <p>In this way, plugins can easily configure themselves by providing a Spring 
 * configuration of themselves.</p>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.3 $ $Date: 2007/10/01 01:53:21 $
 */
public abstract class AbstractJdbcBrowseModePlugin implements BrowseModePlugin {

	/** A class-level logger. */
	protected final Logger log = Logger.getLogger(getClass());

	private UserBiz userBiz;
	private JdbcTemplate jdbcTemplate;
	private DomainObjectFactory domainObjectFactory;

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#getPluginType()
	 */
	public Class<? extends Plugin> getPluginType() {
		return BrowseModePlugin.class;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#initialize(org.springframework.context.ApplicationContext)
	 */
	public final void initialize(ApplicationContext application) {
		if ( !(application instanceof ConfigurableApplicationContext) ) {
			throw new RuntimeException("Only a ["
					+ConfigurableApplicationContext.class +"] is suppored");
		}
		ConfigurableApplicationContext parent = (ConfigurableApplicationContext)application;
		
		String myName = ClassUtils.getShortName(getClass());
		ClassPathXmlApplicationContext myConfig = null;
		String configPath = myName+"Context.xml";
		try {	
			if ( log.isInfoEnabled() ) {
				log.info("Looking for plugin configuration ["
						+configPath +']');
			}
			myConfig = new ClassPathXmlApplicationContext(
					new String[] {configPath}, false);
			myConfig.setParent(parent);
			myConfig.refresh();
		} catch ( FatalBeanException e ) {
			// try META-INF default config
			if ( log.isInfoEnabled() ) {
				log.info("Unable to load plugin configuration ["
						+configPath +"], trying fallback configuration [META-INF/"
						+configPath +']');
			}
			configPath = "META-INF/" +configPath;
			myConfig = new ClassPathXmlApplicationContext(
					new String[] {configPath}, false);
			myConfig.setParent(parent);
			myConfig.refresh();
		}
		
		AbstractJdbcBrowseModePlugin template = (AbstractJdbcBrowseModePlugin)
			myConfig.getBean(getClass().getName(), getClass());
		BeanUtils.copyProperties(template, this);
		init(myConfig);
	}
	
	/**
	 * Initialization hook for subclasses.
	 * 
	 * <p>This method will be called at the end of the {@link #initialize} method.</p>
	 * 
	 * @param application the ApplicationContext
	 */
	protected void init(ApplicationContext application) {
		// subclasses can override
	}
	
	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	/**
	 * @return the jdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @return the domainObjectFactory
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory the domainObjectFactory to set
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

}
