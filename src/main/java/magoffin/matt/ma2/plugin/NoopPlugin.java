/* ===================================================================
 * NoopPlugin.java
 * 
 * Created Sep 19, 2007 3:26:47 PM
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
 */

package magoffin.matt.ma2.plugin;

import org.springframework.context.ApplicationContext;

/**
 * Plugin implementation that does nothing.
 * 
 * <p>This plugin exists just to be able to test plugin infrastructure.</p>
 *
 * @author matt
 * @version 1.0
 */
public class NoopPlugin implements Plugin {

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#getPluginType()
	 */
	public Class<? extends Plugin> getPluginType() {
		return Plugin.class;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#initialize(org.springframework.context.ApplicationContext)
	 */
	public void initialize(ApplicationContext application) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#getMessageResourceNames()
	 */
	public String[] getMessageResourceNames() {
		return null;
	}

}
