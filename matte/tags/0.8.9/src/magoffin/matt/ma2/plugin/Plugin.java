/* ===================================================================
 * Plugin.java
 * 
 * Created Sep 19, 2007 3:15:32 PM
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
 * $Id: Plugin.java,v 1.3 2007/09/25 06:24:02 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.plugin;

import org.springframework.context.ApplicationContext;

/**
 * Base API for system plugins.
 *
 * @author matt
 * @version $Revision: 1.3 $ $Date: 2007/09/25 06:24:02 $
 */
public interface Plugin {
	
	/**
	 * Return the type of plugin this plugin represents.
	 * 
	 * @return the plugin type
	 */
	Class<? extends Plugin> getPluginType();

	/**
	 * Initialize the plugin.
	 * 
	 * @param application the Spring application context
	 */
	public void initialize(ApplicationContext application);
	
	/**
	 * Get a list of message resource names to register.
	 * 
	 * @return the message resource names
	 */
	public String[] getMessageResourceNames();
	
}
