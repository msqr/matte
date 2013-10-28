/* ===================================================================
 * ConfigurationException.java
 * 
 * Created Jan 25, 2006 9:42:07 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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

package magoffin.matt.ma2;

/**
 * Exception thrown during application initialization.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -941428630917642011L;

	/**
	 * Construct with a message.
	 * @param msg
	 */
	public ConfigurationException(String msg) {
		super(msg);
	}

	/**
	 * Construct with a message and Throwable.
	 * @param msg the message
	 * @param throwable the Throwable
	 */
	public ConfigurationException(String msg, Throwable throwable) {
		super(msg,throwable);
	}

	/**
	 * Construct with a mis-configured property.
	 * @param propertyValue the property value
	 * @param propertyName the property name
	 */
	public ConfigurationException(Object propertyValue, String propertyName) {
		super(propertyValue == null 
				? "The [" +propertyName +"] property is required but not configured"
				: "[" +propertyValue +"] is not a valid value for the property ["
					+propertyName +"]"
				);
	}

}
