/* ===================================================================
 * ImportBizContext.java
 * 
 * Created Feb 15, 2008 8:28:06 PM
 * 
 * Copyright (c) 2008 Matt Magoffin.
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

package magoffin.matt.ma2.biz.impl;

import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.support.InternalBizContext;

/**
 * Helper class for {@link IOBizImpl} to import media items.
 * 
 * @author matt
 * @version 1.0
 * @see IOBizImpl
 */
class ImportBizContext extends InternalBizContext {
	private Collection importCollection;
	
	/**
	 * Constructor.
	 * @param c the collection being imported into
	 */
	ImportBizContext(Collection c) {
		this.importCollection = c;
	}
	
	/**
	 * Get the import Collection.
	 * @return Collection
	 */
	Collection getImportCollection() {
		return this.importCollection;
	}
}