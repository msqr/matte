/* ===================================================================
 * AbstractDataPoolableFactory.java
 * 
 * Created Jul 1, 2004 5:50:24 PM
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
 * $Id: AbstractDataPoolableFactory.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import magoffin.matt.ma.xsd.AbstractData;

import org.apache.commons.pool.BasePoolableObjectFactory;

/**
 * Base PoolableFactory for AbstractData objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public abstract class AbstractDataPoolableFactory 
	extends BasePoolableObjectFactory 
{
	
protected void passivateDataObject(AbstractData data) throws Exception {
	data.deleteAdmin();
	data.setSettings(null);
	data.setUser(null);
	data.setLightbox(null);
	data.clearActionMessages();
}

}
