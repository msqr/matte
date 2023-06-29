/* ===================================================================
 * RegisterForm.java
 * 
 * Created Feb 2, 2006 5:17:06 PM
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
 */

package magoffin.matt.ma2.web;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.AuthorizationException.Reason;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.BizContext.Feature;
import magoffin.matt.ma2.domain.Edit;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

/**
 * Wizard form controller for registering as a new user.
 * 
 * <dl class="class-properties">
 *   <dt>userBiz</dt>
 *   <dd>The {@link magoffin.matt.ma2.biz.UserBiz} to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class RegisterForm extends AbstractWizardForm {
	
	private UserBiz userBiz;
	
	@Override
	protected Object formBackingObject(HttpServletRequest request)
	throws Exception 
	{
		BizContext context = getWebHelper().getAnonymousBizContext(request);
		if ( !context.isFeatureEnabled(Feature.REGISTRATION) ) {
			throw new AuthorizationException(null, Reason.ACCESS_DENIED);
		}
		Edit edit = getDomainObjectFactory().newEditInstance();
		User user = getDomainObjectFactory().newUserInstance();
		
		user.setTz(getSystemBiz().getDefaultTimeZone());
		
		edit.setUser(user);
		return edit;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors, int page) throws Exception {
		Map<String,Object> ref =  new LinkedHashMap<String,Object>();
		
		Model model = getDomainObjectFactory().newModelInstance();
		model.getTimeZone().addAll(getSystemBiz().getAvailableTimeZones());
		
		ref.put(WebConstants.DEFALUT_REFERENCE_DATA_OBJECT, model);
		return ref;
	}

	@Override
	protected void validatePage(Object command, Errors errors, int page) {
		Edit edit = (Edit)command;
		User user = edit.getUser();
		Validator val = getValidator();
		val.validate(user, errors);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView processFinish(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Map<String,Object> model = errors.getModel();
		Edit edit = (Edit)command;
		User user = edit.getUser();
		BizContext context = getWebHelper().getAnonymousBizContext(request);
		try {
			userBiz.registerUser(user, context);
		} catch ( AuthorizationException e ) {
			switch ( e.getReason() ) {
				case DUPLICATE_LOGIN:
					errors.rejectValue("user.login", "user.error.login.taken", 
							new Object[] {user.getLogin()}, "login.displayName");
					return showPage(request,errors,getInitialPage(request));
				
				case DUPLICATE_EMAIL:
					errors.rejectValue("user.email", "user.error.email.taken", 
							new Object[] {user.getEmail()}, "email.displayName");
					return showPage(request,errors,getInitialPage(request));
					
				default:
					throw e;
			}
		}
		
		model.put(WebConstants.DEFALUT_MODEL_OBJECT,edit);
		return new ModelAndView(getSuccessView(),model);
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
