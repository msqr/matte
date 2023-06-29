/* ===================================================================
 * ForgotPasswordForm.java
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

package magoffin.matt.ma2.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.support.LogonCommand;

/**
 * Form controller for resetting a user's password.
 * 
 * @author matt.magoffin
 * @version 1.1
 */
public class ForgotPasswordForm extends AbstractForm {

	private UserBiz userBiz;

	@SuppressWarnings("deprecation")
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		LogonCommand form = (LogonCommand) command;
		BizContext context = getWebHelper().getAnonymousBizContext(request);
		ModelAndView result = null;
		try {
			userBiz.forgotPassword(form.getLogin(), context);
			result = new ModelAndView(getSuccessView(), errors.getModel());
		} catch ( AuthorizationException e ) {
			errors.reject("error.unknown.login", "Authorization error.");
			result = showForm(request, response, errors);
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
	 * @param userBiz
	 *        the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

}
