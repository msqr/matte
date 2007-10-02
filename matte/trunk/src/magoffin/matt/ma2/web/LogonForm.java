/* ===================================================================
 * LoginForm.java
 * 
 * Created Oct 4, 2004 12:32:26 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: LogonForm.java,v 1.5 2007/01/06 08:32:03 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.LogonCommand;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller for logging into the application.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.5 $ $Date: 2007/01/06 08:32:03 $
 */
public class LogonForm extends AbstractForm {
	
	private UserBiz userBiz;
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		getWebHelper().clearUserSessionData(request);
		LogonCommand form = (LogonCommand)command;
		ModelAndView result = null;
		try {
			User user = userBiz.logonUser(form.getLogin(),form.getPassword());
			getWebHelper().saveUserSession(request,user);
			if ( getWebHelper().getSavedRequestURL(request) != null ) {
				String savedUrl = getWebHelper().getSavedRequestURL(request);
				getWebHelper().clearSavedRequestURL(request);
				response.sendRedirect(savedUrl);
				return null;
			}
			result = new ModelAndView(getSuccessView());
		} catch ( AuthorizationException e ) {
			if ( AuthorizationException.Reason.REGISTRATION_NOT_CONFIRMED.equals(e.getReason()) ) {
				errors.reject("error.logon.pending", "Registration confirmation pending");
			} else {
				errors.reject("error.logon.auth", "Authorization error.");
			}
			result = showForm(request,response,errors);
		}
		return result;
	}

	/**
	 * @return Returns the userBiz.
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz The userBiz to set.
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}
	
}
