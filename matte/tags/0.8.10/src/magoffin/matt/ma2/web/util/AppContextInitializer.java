/* ===================================================================
 * AppContextInitializer.java
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
 * $Id: AppContextInitializer.java,v 1.1 2007/07/21 10:20:14 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web.util;

import java.util.Map;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.xweb.XAppContext;
import magoffin.matt.xweb.XwebParameter;
import magoffin.matt.xweb.util.AppContextSupport;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * Initialzie the application {@link AppContextSupport} instance.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2007/07/21 10:20:14 $
 */
public class AppContextInitializer implements ApplicationContextAware {
	
	private DomainObjectFactory domainObjectFactory;
	private Map<String, String> applicationProperties;
	
	private final Logger log = Logger.getLogger(AppContextInitializer.class);

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@SuppressWarnings("unchecked")
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		if ( !(context instanceof WebApplicationContext) ) {
			throw new RuntimeException("Only a " 
					+WebApplicationContext.class 
					+" ApplicationContext is supported.");
		}
		
		WebApplicationContext webAppCtx = (WebApplicationContext)context;
		
		XAppContext appCtx = domainObjectFactory.newXAppContextInstance();
		for ( String key : applicationProperties.keySet() ) {
			XwebParameter param = domainObjectFactory.newXwebParameterInstance();
			param.setKey(key);
			param.setValue(applicationProperties.get(key));
			appCtx.getParam().add(param);
		}

		AppContextSupport appCtxSupport = new AppContextSupport(appCtx);
		if ( log.isDebugEnabled() ) {
			log.debug("Storing XAppContext [" +appCtx +"] with " 
					+applicationProperties.size() 
					+" properties in the servlet context at ["
					+WebConstants.APP_KEY_APP_CONTEXT +"]");
		}
		
		webAppCtx.getServletContext().setAttribute(
				WebConstants.APP_KEY_APP_CONTEXT, appCtxSupport);
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

	/**
	 * @return the applicationProperties
	 */
	public Map<String, String> getApplicationProperties() {
		return applicationProperties;
	}

	/**
	 * @param applicationProperties the applicationProperties to set
	 */
	public void setApplicationProperties(Map<String, String> applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

}
