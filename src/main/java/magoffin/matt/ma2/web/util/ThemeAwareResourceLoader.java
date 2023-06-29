/* ===================================================================
 * ThemeAwareResourceLoader.java
 * 
 * Created Jul 16, 2006 8:54:04 PM
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
 */

package magoffin.matt.ma2.web.util;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import magoffin.matt.ma2.domain.Theme;

import org.apache.log4j.Logger;
import org.springframework.web.context.ServletContextAware;

/**
 * ClassLoader implementation that is able to load theme resources.
 * 
 * <p>This class loader allows themes to have their own message resources.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class ThemeAwareResourceLoader extends ClassLoader implements ServletContextAware {
	
	private WebHelper webHelper;
	private String baseThemePath = "/WEB-INF/themes";
	private ServletContext servletContext;
	
	private final Logger log = Logger.getLogger(ThemeAwareResourceLoader.class);
	
	/**
	 * Constrcutor.
	 */
	public ThemeAwareResourceLoader() {
		super(Thread.currentThread().getContextClassLoader());
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	protected URL findResource(String name) {
		Theme theme = webHelper.getRequestTheme();
		URL resource = null;
		if ( theme != null ) {
			String path = baseThemePath +theme.getBasePath()
				+(name.startsWith("/") ? name : "/"+name);
			try {
				resource = servletContext.getResource(path);
			} catch (MalformedURLException e) {
				log.warn("MalformedURLException loading resource [" +path +"]");
			}
		}
		return resource;
	}

	/**
	 * @return the baseThemePath
	 */
	public String getBaseThemePath() {
		return baseThemePath;
	}

	/**
	 * @param baseThemePath the baseThemePath to set
	 */
	public void setBaseThemePath(String baseThemePath) {
		this.baseThemePath = baseThemePath;
	}

	/**
	 * @return the webHelper
	 */
	public WebHelper getWebHelper() {
		return webHelper;
	}

	/**
	 * @param webHelper the webHelper to set
	 */
	public void setWebHelper(WebHelper webHelper) {
		this.webHelper = webHelper;
	}

}
