/* ===================================================================
 * EditInitializer.java
 * 
 * Created Apr 14, 2007 8:53:40 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.web.util;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.xweb.util.DynamicInitializer;

/**
 * DynamicInitializer for {@link Album} albums.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class EditInitializer implements DynamicInitializer {
	
	private DomainObjectFactory domainObjectFactory;

	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.DynamicInitializer#newInstance(java.lang.Object, java.lang.String)
	 */
	public Object newInstance(Object bean, String property) {
		if ( property.equals("uiMetadata") ) {
			return domainObjectFactory.newMetadataInstance();
		}
		throw new UnsupportedOperationException("The property " +property 
				+" is not supported for " 
				+(bean == null ? "(null)" : bean.getClass().getName()) );
	}
	
	/**
	 * @return the domainObjectFactory
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}
	
	/**
	 * @param domainObjectFactory the domainObjectFactory to set
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

}
