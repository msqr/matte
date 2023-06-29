/* ===================================================================
 * BizContext.java
 * 
 * Created Aug 17, 2004 8:48:09 AM
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
 */

package magoffin.matt.ma2.biz;

import java.util.Locale;

import magoffin.matt.ma2.domain.User;
import magoffin.matt.xweb.util.AppContextSupport;

/**
 * Interface for application input data.
 * 
 * <p>This interface is passed to Biz implementations to allow for 
 * context-specific information to get passed into the Biz layer.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public interface BizContext {
	
	/**
	 * An enumeration of features supported by the application.
	 */
	public enum Feature {
		
		/** Feature to allow user self-registration. */
		REGISTRATION;
		
		/**
		 * Get a Feature from a String key.
		 * @param key the key
		 * @return the Feature
		 */
		public Feature fromKey(String key) {
			return Feature.valueOf(key.toUpperCase());
		}
		
	}
	
	/**
	 * Get the acting user.
	 * @return the acting user
	 */
	public User getActingUser();
	
	/**
	 * Test if a feature is enabled.
	 * @param feature the feature to test
	 * @return boolean
	 */
	public boolean isFeatureEnabled(Feature feature);
	
	/**
	 * Get the application context.
	 * @return AppContextSupport
	 */
	public AppContextSupport getAppContextSupport();
	
	/**
	 * Get the Locale to use for messages, etc.
	 * @return locale
	 */
	public Locale getLocale();
	
	/**
	 * Get an attribute value.
	 * @param key the attribute key
	 * @return the attribute value, or <em>null</em>
	 */
	public Object getAttribute(String key);
	
	/**
	 * Set an attribute value.
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	public void setAttribute(String key, Object value);
	
}
