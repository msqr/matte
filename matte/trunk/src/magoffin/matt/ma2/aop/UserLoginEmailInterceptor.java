/* ===================================================================
 * UserLoginEmailInterceptor.java
 * 
 * Created Feb 12, 2005 5:10:06 PM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: UserLoginEmailInterceptor.java,v 1.3 2007/07/28 10:25:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.aop;

import java.util.Map;

import magoffin.matt.ma2.dao.UserDao;
import magoffin.matt.ma2.domain.User;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.mail.SimpleMailMessage;

/**
 * Email interceptor for forgot password email handling.
 * 
 * <p>This email interceptor will look for a user login as the first 
 * argument to the method being intercepted.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>userDao</dt>
 *   <dd>The {@link magoffin.matt.ma2.dao.UserDao} instance to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2007/07/28 10:25:54 $
 */
public class UserLoginEmailInterceptor extends UserEmailInterceptor {
    
    private UserDao userDao;

    @Override
    protected SimpleMailMessage postProcessModel(MethodInvocation invocation,
            Map<String, Object> model, Object result) {
        // check for null result, and in that case treat first method argument as the
        // user login, and get the user from UserDao
        if ( !(result instanceof User) ) {
            Object[] args = invocation.getArguments();
            if ( args == null || args.length < 1 || args[0] == null ) {
                throw new RuntimeException("No user login found in method invocation arguments");
            }
            String login = args[0].toString();
            User user = userDao.getUserByLogin(login);
            model.put(USER_KEY,user);
        }
        return super.postProcessModel(invocation, model, result);
    }
	
	/**
	 * @return the userDao
	 */
	public UserDao getUserDao() {
		return userDao;
	}
	
	/**
	 * @param userDao the userDao to set
	 */
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
    
}
