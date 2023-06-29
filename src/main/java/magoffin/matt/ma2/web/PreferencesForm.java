/* ===================================================================
 * PreferencesForm.java
 * 
 * Created Jan 7, 2007 10:40:21 AM
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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.PreferencesCommand;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.util.TemporaryFile;
import magoffin.matt.util.TemporaryFileMultipartFileEditor;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller for editing overall user preferences.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class PreferencesForm extends AbstractForm {
	
	private UserBiz userBiz;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, 
			Errors errors) throws Exception {
		getWebHelper().getBizContext(request, true);
		
		Model model = getDomainObjectFactory().newModelInstance();
		model.getTheme().addAll(getSystemBiz().getAvailableThemes());
		model.getTimeZone().addAll(getSystemBiz().getAvailableTimeZones());
		model.getLocale().addAll(getSystemBiz().getAvailableLocales());
		
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		viewModel.put(WebConstants.DEFALUT_REFERENCE_DATA_OBJECT, model);
		return viewModel;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		PreferencesCommand cmd = (PreferencesCommand)super.formBackingObject(request);
		BizContext context = getWebHelper().getBizContext(request, true);
		if ( cmd.getThumb() == null ) {
			cmd.setThumb(context.getActingUser().getThumbnailSetting());
		}
		if ( cmd.getView() == null ) {
			cmd.setView(context.getActingUser().getViewSetting());
		}
		if ( cmd.getTimeZone() == null ) {
			cmd.setTimeZone(context.getActingUser().getTz().getCode());
		}
		return cmd;
	}

	@Override
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		// register our Multipart TemporaryFile binder...
		binder.registerCustomEditor(TemporaryFile.class, 
				new TemporaryFileMultipartFileEditor(true));
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, 
			HttpServletResponse response, Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);
		PreferencesCommand cmd = (PreferencesCommand)command;
		
		// force userId to the current user
		cmd.setUserId(context.getActingUser().getUserId());
		
		getUserBiz().storeUserPreferences(cmd, context);
		
		// reload context user data
		User user = getUserBiz().getUserById(cmd.getUserId(), context);
		getWebHelper().saveUserSession(request, user);

		Map<String,Object> model = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"user.prefs.saved"}, null,
				"The settings have been saved.");
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		return new ModelAndView(getSuccessView(),model);
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
