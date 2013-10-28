/* ===================================================================
 * ImageEffectPoolFactory.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 12, 2004 10:01:53 AM.
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
 * $Id: ImageEffectPoolFactory.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image;

import java.util.HashMap;
import java.util.Map;

import magoffin.matt.ma.MediaAlbumRuntimeException;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 * ObjectPool factory for ImageEffect objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public final class ImageEffectPoolFactory {
	
	private static ImageEffectPoolFactory pf = new ImageEffectPoolFactory();
	
	private Map pools;
	
private ImageEffectPoolFactory() {
	pools = new HashMap(8);
}

/**
 * Get an instance of this class.
 * @return ImageEffectPoolFactory
 */
public static ImageEffectPoolFactory getInstance() {
	return pf;
}
	
/**
 * Get an ObjectPool for an ImageEffect class.
 * @param effectType the ImageEffect class
 * @return ObjectPool
 * @throws MediaAlbumRuntimeException if an error occurs
 */
public ObjectPool getEffectPool(Class effectType) {
	if ( pools.containsKey(effectType) ) {
		return (ObjectPool)pools.get(effectType);
	}
	synchronized ( pools ) {
		if ( pools.containsKey(effectType) ) {
			return (ObjectPool)pools.get(effectType);
		}
		ImageEffectPoolableFactory poolableFactory = new ImageEffectPoolableFactory(
				effectType);
		ObjectPool pool = new StackObjectPool(poolableFactory);
		pools.put(effectType,pool);
		return pool;
	}
}

/**
 * Borrow an ImageEffect object from an ObjectPool.
 * 
 * <p>Once finished you must call {@link #returnEffect(ImageEffect)}.</p>
 * 
 * @param effectType the ImageEffect class
 * @return ImageEffect instance
 * @throws MediaAlbumRuntimeException if an error occurs
 */
public ImageEffect borrowEffect(Class effectType) {
	ObjectPool pool = getEffectPool(effectType);
	try {
		return (ImageEffect)pool.borrowObject();
	} catch ( Exception e ) {
		throw new MediaAlbumRuntimeException("Unknown exception borrowing ImageEffect",e);
	}
}

/**
 * Return a borrowed ImageEffect object to its ObjectPool.
 * 
 * @param effect the ImageEffect borrowed via {@link #borrowEffect(Class)}
 * @throws MediaAlbumRuntimeException if an error occurs
 */
public void returnEffect(ImageEffect effect) {
	ObjectPool pool = getEffectPool(effect.getClass());
	try {
		pool.returnObject(effect);
	} catch ( Exception e ) {
		throw new MediaAlbumRuntimeException("Unknown exception returning ImageEffect",e);
	}
}

}
