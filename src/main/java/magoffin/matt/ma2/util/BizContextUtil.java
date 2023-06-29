/* ===================================================================
 * BizContextUtil.java
 * 
 * Created Mar 9, 2007 9:02:57 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.util;

import magoffin.matt.ma2.biz.BizContext;

/**
 * Utility API for dealing with BizContext instances.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class BizContextUtil {
	
	private static ThreadLocal<BizContext> threadLocalBizContext
		= new ThreadLocal<BizContext>();
	
	/**
	 * Set a BizContext for the current thread.
	 * @param context the context
	 */
	public static void attachBizContext(BizContext context) {
		threadLocalBizContext.set(context);
	}
	
	/**
	 * Get the BizContext previously set via 
	 * {@link #attachBizContext(BizContext)}.
	 * 
	 * @return a BizContext, or null
	 */
	public static BizContext getBizContext() {
		return threadLocalBizContext.get();
	}

	/**
	 * Remove a BizContext from the current thread.
	 */
	public static void removeBizContext() {
		threadLocalBizContext.remove();
	}
}
