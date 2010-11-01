/* ===================================================================
 * AbstractPlugin.java
 * 
 * Created Nov 8, 2007 12:01:07 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

import magoffin.matt.ma2.plugin.Plugin;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * Abstract base {@link Plugin} implementation that provides a standardized Spring
 * initialization method.
 * 
 * <p>The {@link #initialize(ApplicationContext)} method works in the following way:</p>
 * 
 * <ol>
 *   <li>Calls the {@link #getConfigName()} method to determine the <em>base config
 *   name</em> to look for.</li>
 *   
 *   <li>Looks for a classpath resource named <b><em>base config name</em>Context.xml</b>. 
 *   For example, if the base config name is <em>MyPlugin</em> then 
 *   this looks for a file named <em>MyPluginContext.xml</em>.</li>
 *   
 *   <li>If the previous step does not find the resource, it looks for a 
 *   classpath resource named <b>META-INF/<em>base config name</em>Context.xml</b>. This
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
 * @author matt
 * @version $Revision$ $Date$
 */
public abstract class AbstractPlugin implements Plugin {

	/** A class-level logger. */
	protected final Logger log = Logger.getLogger(getClass());

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#initialize(org.springframework.context.ApplicationContext)
	 */
	public final void initialize(ApplicationContext application) {
		if ( !(application instanceof ConfigurableApplicationContext) ) {
			throw new RuntimeException("Only a ["
					+ConfigurableApplicationContext.class +"] is suppored");
		}
		ConfigurableApplicationContext parent = (ConfigurableApplicationContext)application;
		
		String myName = getConfigName();
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
		
		AbstractPlugin template = (AbstractPlugin)myConfig.getBean(
				getClass().getName(), getClass());
		BeanUtils.copyProperties(template, this);
		init(myConfig);
	}
	
	/**
	 * Get the base name for this plugin's Spring configuration.
	 * 
	 * <p>This method must return the base name of the Spring configuration file. 
	 * The base name will have <code>Context.xml</code> appended to it, and possibly
	 * a path prefix.</p>
	 * 
	 * <p>This default method returns the name of this class, without any packages.</p>
	 * 
	 * @return a Spring configuration resource base name
	 */
	protected String getConfigName() {
		return ClassUtils.getShortName(getClass());
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

}
