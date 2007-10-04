/* ===================================================================
 * AbstractDomainObject.java
 * 
 * Created May 30, 2006 10:20:54 AM
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
 * $Id: AbstractDomainObject.java,v 1.1 2006/05/30 01:08:01 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

import magoffin.matt.lucene.SearchMatch;

/**
 * Helper class for domain objects to extend to also make them 
 * implement useful Matte interfaces.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/05/30 01:08:01 $
 */
public abstract class AbstractDomainObject implements SearchMatch {
	// nothing to add
}
