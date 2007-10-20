/* ===================================================================
 * BasicBizContext.java
 * 
 * Created Oct 4, 2004 12:13:14 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.xweb.util.AppContextSupport;

/**
 * Basic implementation of BizContext interface.
 * 
 * <p>Note this implementation is not thread safe.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class BasicBizContext implements BizContext {
	
	private User actingUser;
	private AppContextSupport appContextSupport;
	private Map<String,Object> attributes;
	private Locale locale;
	
	/**
	 * Constructor.
	 * @param appContextSupport the application context
	 */
	public BasicBizContext(AppContextSupport appContextSupport) {
		this.appContextSupport = appContextSupport;
	}
	
	/**
	 * Default constructor.
	 */
	public BasicBizContext() {
		// nothing to do here
	}
	
	/**
	 * Set the acting user.
	 * @param user
	 */
	public void setActingUser(User user) {
		actingUser = user;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ieat.biz.BizContext#getActingUser()
	 */
	public User getActingUser() {
		return actingUser;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ieat.biz.BizContext#getAppContextSupport()
	 */
	public AppContextSupport getAppContextSupport() {
		return appContextSupport;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.BizContext#getLocale()
	 */
	public Locale getLocale() {
		if ( actingUser != null && actingUser.getLanguage() != null 
				&& actingUser.getCountry() != null ) {
			return new Locale(actingUser.getLanguage(),actingUser.getCountry());
		}
		if ( locale != null ) return locale;
		return Locale.getDefault();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.BizContext#isFeatureEnabled(magoffin.matt.ma2.biz.BizContext.Feature)
	 */
	public boolean isFeatureEnabled(Feature feature) {
		// look for feature in AppContextSupport
		AppContextSupport support = getAppContextSupport();
		return support.isParameterTrue(
				"feature."+feature.toString().toLowerCase());
	}

	/**
	 * Set the locale.
	 * 
	 * @param locale the locale to set
	 */
	protected void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * Set the AppContextSupport.
	 * @param appContextSupport
	 */
	protected void setAppContextSupport(AppContextSupport appContextSupport) {
		this.appContextSupport = appContextSupport;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ieat.biz.BizContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key) {
		if ( attributes == null ) return null;
		return attributes.get(key);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ieat.biz.BizContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String key, Object value) {
		if ( attributes == null ) {
			attributes = new LinkedHashMap<String,Object>();
		}
		attributes.put(key,value);
	}

}
