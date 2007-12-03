/* ===================================================================
 * PasswordValidationCallbackHandler.java
 * 
 * Created Dec 3, 2007 8:06:53 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

package magoffin.matt.ma2.web.service;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BasicBizContext;
import magoffin.matt.ma2.util.BizContextUtil;

import org.springframework.ws.soap.security.xwss.callback.AbstractCallbackHandler;

/**
 * Callback implementation that uses {@link UserBiz} to autentication plain text
 * passowrd requests.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class PasswordValidationCallbackHandler extends AbstractCallbackHandler {
	
	private UserBiz userBiz;

	@Override
	protected void handleInternal(Callback callback) throws IOException,
			UnsupportedCallbackException {
		/*if (callback instanceof PasswordValidationCallback) {
			PasswordValidationCallback passwordCallback = (PasswordValidationCallback) callback;
			if (passwordCallback.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
				passwordCallback.setValidator(new PlainTextPasswordValidator());
			}
		}*/
	}

	private class PlainTextPasswordValidator /*implements
			PasswordValidationCallback.PasswordValidator*/ {

		public boolean validate(/*PasswordValidationCallback.Request request*/)
				/*throws PasswordValidationCallback.PasswordValidationException*/ {
			//PasswordValidationCallback.PlainTextPasswordRequest plainTextPasswordRequest = (PasswordValidationCallback.PlainTextPasswordRequest) request;
			String username = null;/*= plainTextPasswordRequest.getUsername()*/
			String pass = null;/*= plainTextPasswordRequest.getPassword()*/
			try {
				User user = userBiz.logonUser(username, pass);
				BasicBizContext bizContext = new BasicBizContext();
				bizContext.setActingUser(user);
				BizContextUtil.attachBizContext(bizContext);
				return true;
			} catch ( AuthorizationException e ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug("WS authentication failure for user [" +username +"]: " +e);
				}
			}
			return false;
		}
	}

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}
	
}
