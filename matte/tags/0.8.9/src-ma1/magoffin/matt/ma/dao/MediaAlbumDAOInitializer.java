/* ===================================================================
 * MediaAlbumDAOInitializer.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: MediaAlbumDAOInitializer.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.gerdal.dao.BaseRdbDAOXMLInitializer;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.xsd.ObjectPoolConfig;

/**
 * XML initializer for the Media Album DAO system.
 * 
 * <p>Created Oct 9, 2002 8:56:13 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class MediaAlbumDAOInitializer extends BaseRdbDAOXMLInitializer {
	
	private MediaAlbumConfig config = null;

/**
 * Constructor for MediaAlbumDAOInitializer.
 * @param config
 */
public MediaAlbumDAOInitializer(MediaAlbumConfig config) {
	super(config.getDao());
	this.config = config;
}

public ObjectPoolConfig getPrimaryKeyPoolConfig()
{
	return config.getDao().getPkPool();
}

public ObjectPoolConfig getCriteriaPoolConfig()
{
	return config.getDao().getCriteriaPool();
}

} // class MediaAlbumDAOInitializer
