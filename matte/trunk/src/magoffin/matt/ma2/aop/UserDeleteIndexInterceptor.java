/* ===================================================================
 * UserDeleteIndexInterceptor.java
 * 
 * Created May 26, 2006 3:52:34 PM
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
 * $Id: UserDeleteIndexInterceptor.java,v 1.1 2006/05/27 05:41:32 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.aop;

import java.lang.reflect.Method;

/**
 * Interceptor to support removal of User domain objects from 
 * the search index.
 * 
 * <p>This method expects an <code>Long</code> value to exist on the 
 * <code>args</code> parameter passed to the 
 * {@link #afterReturning(Object, Method, Object[], Object)} method. It will
 * assume the first Long object found is the <code>userId</code> of the 
 * user that should be removed from the user index via 
 * {@link magoffin.matt.ma2.biz.IndexBiz#removeUserFromIndex(Long)}.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/05/27 05:41:32 $
 */
public class UserDeleteIndexInterceptor extends AbstractIndexInterceptor {

	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		
		// assume one parameter is the Long User ID of the User to delete
		Long userId = null;
		for ( int i = 0; i < args.length && userId == null; i++ ) {
			if ( args[i] instanceof Long ) {
				userId = (Long)args[i];
			}
		}
		
		if ( userId == null ) {
			throw new RuntimeException("User ID not available in method arguments");
		}
		
		getIndexBiz().removeUserFromIndex(userId);
	}

}
