/* ===================================================================
 * UserIndexInterceptor.java
 * 
 * Created May 26, 2006 3:46:20 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.aop;

import java.lang.reflect.Method;

import magoffin.matt.ma2.domain.User;

/**
 * Interceptor to support automatic indexing of updated User domain objects.
 * 
 * <p>This interceptor expects a {@link magoffin.matt.ma2.domain.User}
 * domain object (or a <code>Long</code> User ID) to be the 
 * <code>returnValue</code> passed to the 
 * {@link #afterReturning(Object, Method, Object[], Object)} method. 
 * Using the <code>userId</code> of that User object (or the Long 
 * User ID directly) it will call the 
 * {@link magoffin.matt.ma2.biz.IndexBiz#indexUser(Long)}
 * method to index the user.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class UserIndexInterceptor extends AbstractIndexInterceptor {

	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		Long userId = null;
		if ( returnValue instanceof Long ) {
			userId = (Long)returnValue;
		} else if ( returnValue instanceof User ) {
			userId = ((User)returnValue).getUserId();
		}
		
		if ( userId != null ) {
			getIndexBiz().indexUser(userId);
		}

	}

}
