/* ===================================================================
 * AbstractSecurityInterceptor.java
 * 
 * Created Jun 6, 2007 7:23:30 PM
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

package magoffin.matt.ma2.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.AuthorizationException.Reason;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import net.sf.ehcache.Ehcache;

/**
 * Base AOP interceptor to validate security.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public abstract class AbstractSecurityInterceptor implements MethodInterceptor {

	private final Logger log = Logger.getLogger(getClass());

	private UserBiz userBiz;
	private Ehcache securityCache;

	@Override
	public final Object invoke(MethodInvocation invocation) throws Throwable {
		// look for BizContext in method args to tell who current user is
		BizContext context = null;
		if ( invocation.getArguments() != null ) {
			for ( Object o : invocation.getArguments() ) {
				if ( o instanceof BizContext ) {
					context = (BizContext) o;
					break;
				}
			}
		}
		if ( context == null ) {
			// no security...
			log.warn("No BizContext found in method arguments, security ignored: "
					+ invocation.toString());
			return invocation.proceed();
		}

		if ( isAllowed(invocation, context) ) {
			return invocation.proceed();
		}

		log.warn("Security error: invocation [" + invocation + "] denied for user ["
				+ (userBiz.isAnonymousUser(context.getActingUser()) ? "anonymous"
						: context.getActingUser().getUserId() + ":" + context.getActingUser().getLogin())
				+ "]");

		throw new AuthorizationException(context.getActingUser().getLogin(), Reason.ACCESS_DENIED);
	}

	/**
	 * Return <em>true</em> if the method invocation should be allowed.
	 * 
	 * @param invocation
	 *        the invocation
	 * @param context
	 *        the context
	 * @return boolean
	 */
	abstract protected boolean isAllowed(MethodInvocation invocation, BizContext context);

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz
	 *        the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	/**
	 * @return the securityCache
	 */
	public Ehcache getSecurityCache() {
		return securityCache;
	}

	/**
	 * @param securityCache
	 *        the securityCache to set
	 */
	public void setSecurityCache(Ehcache securityCache) {
		this.securityCache = securityCache;
	}

}
