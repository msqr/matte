/* ===================================================================
 * ImageEffectPoolableFactory.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 12, 2004 10:08:33 AM.
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
 * $Id: ImageEffectPoolableFactory.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image;

import magoffin.matt.ma.MediaAlbumRuntimeException;

import org.apache.commons.pool.BasePoolableObjectFactory;

/**
 * Poolable object factory for ImageEffect implementations.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class ImageEffectPoolableFactory extends BasePoolableObjectFactory 
{
	private Class effectClass;

public ImageEffectPoolableFactory(Class effectClass) {
	if ( !ImageEffect.class.isAssignableFrom(effectClass) ) {
		throw new MediaAlbumRuntimeException(effectClass.getName() +" does not implement "
				+ImageEffect.class.getName());
	}
	this.effectClass = effectClass;
}
	
/* (non-Javadoc)
 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
 */
public Object makeObject() throws Exception {
	ImageEffect e = (ImageEffect)effectClass.newInstance();
	e.reset();
	return e;
}

/* (non-Javadoc)
 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
 */
public void passivateObject(Object o) throws Exception {
	((ImageEffect)o).reset();
}

}
