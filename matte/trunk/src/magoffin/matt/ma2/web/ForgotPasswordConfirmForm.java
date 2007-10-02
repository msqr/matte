/* ===================================================================
 * ForgotPasswordConfirmForm.java
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
 * $Id: ForgotPasswordConfirmForm.java,v 1.1 2007/07/25 08:58:02 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.LogonCommand;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for confirming a forgotten password, and resetting
 * the password.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2007/07/25 08:58:02 $
 */
public class ForgotPasswordConfirmForm extends AbstractForm {

	private UserBiz userBiz;
	
	@Override
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception {
		LogonCommand form = (LogonCommand)command;
		BizContext context = getWebHelper().getAnonymousBizContext(request);
		ModelAndView result = null;
		try {
			User user = userBiz.confirmForgotPassword(form.getLogin(), 
					form.getCode(), form.getPassword(), context);
			
			// log the user in
			getWebHelper().saveUserSession(request, user);
			
			result = new ModelAndView(getSuccessView());
		} catch ( AuthorizationException e ) {
			switch ( e.getReason() ) {
				case REGISTRATION_NOT_CONFIRMED:
					errors.reject("error.password.not.confirmed", 
							"Unable to confirm password.");
					break;
				
				default:
				    errors.reject("error.unknown.login", "Authorization error.");
					break;
			}
			result = showForm(request,response,errors);
		}
		return result;
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
