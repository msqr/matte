/* ============================================================================
 * AbstractIndexInterceptor.java
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.aop;

import magoffin.matt.ma2.biz.IndexBiz;

import org.apache.log4j.Logger;
import org.springframework.aop.AfterReturningAdvice;

/**
 * Base aspect for providing distributed index services.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>indexBiz</dt>
 *   <dd>The {@link IndexBiz} implementation to use.</dd>
 * </dl>
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public abstract class AbstractIndexInterceptor implements AfterReturningAdvice {

	/** A class Logger. */
	protected final Logger log = Logger.getLogger(getClass());
	
	/** The IndexBiz to use for indexing the domain objects. */
	private IndexBiz indexBiz;

	/**
	 * @return the indexBiz
	 */
	public IndexBiz getIndexBiz() {
		return indexBiz;
	}
	
	/**
	 * @param indexBiz the indexBiz to set
	 */
	public void setIndexBiz(IndexBiz indexBiz) {
		this.indexBiz = indexBiz;
	}

}
