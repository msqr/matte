/* ===================================================================
 * RegisterConfirmController.java
 * 
 * Created Oct 5, 2004 8:45:27 PM
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
 * $Id$
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
 * Controller for handing the registration confirmation request.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class RegisterConfirmController extends AbstractCommandController {
	
	private UserBiz userBiz;
	
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		LogonCommand logonCommand = (LogonCommand)command;
		BizContext context = getWebHelper().getBizContext(request, false);
		User user = null;
		try {
			user = userBiz.confirmRegisteredUser(logonCommand.getLogin(),logonCommand.getCode(), context);
		} catch ( AuthorizationException e ) {
			switch ( e.getReason() ) {
				case REGISTRATION_ALREADY_CONFIRMED:
					errors.reject("error.already.confirmed.registration","Registration already confirmed.");
					break;
				
				default:
					errors.reject("error.not.confirmed.registration", "Unable to confirm registration.");
					break;
			}
			
			return new ModelAndView(getErrorView(),errors.getModel());
		}
		
		// save the user to session
		getWebHelper().saveUserSession(request,user);
		return new ModelAndView(getSuccessView(),errors.getModel());
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
