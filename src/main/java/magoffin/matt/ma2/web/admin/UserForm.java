/* ===================================================================
 * UserForm.java
 * 
 * Created Jun 4, 2006 2:29:27 PM
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

package magoffin.matt.ma2.web.admin;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Edit;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.web.AbstractForm;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Form controller for administering user details.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class UserForm extends AbstractForm {

	private UserBiz userBiz;

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		Command cmd = new Command();
		BizContext context = getWebHelper().getAdminBizContext(request);

		// see if trying to populate user
		ServletRequestDataBinder binder = createBinder(request, cmd);
		binder.bind(request);

		User user = getDomainObjectFactory().newUserInstance();
		if ( cmd.getUserId() != null ) {
			User domainUser = userBiz.getUserById(cmd.getUserId(), context);
			BeanUtils.copyProperties(domainUser, user);

			// set password to "special" value so we know if not to change it
			user.setPassword(UserBiz.DO_NOT_CHANGE_VALUE);
		} else {
			user.setTz(getSystemBiz().getDefaultTimeZone());
		}

		Edit model = getDomainObjectFactory().newEditInstance();
		model.setUser(user);
		return model;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
			throws Exception {
		Map<String, Object> ref = new LinkedHashMap<String, Object>();

		Model model = getDomainObjectFactory().newModelInstance();
		model.getTimeZone().addAll(getSystemBiz().getAvailableTimeZones());

		ref.put(WebConstants.DEFALUT_REFERENCE_DATA_OBJECT, model);
		return ref;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		Edit model = (Edit) command;
		BizContext context = getWebHelper().getAdminBizContext(request);

		boolean isNew = model.getUser().getUserId() == null;

		// save user
		Long userId = userBiz.storeUser(model.getUser(), context);
		User savedUser = userBiz.getUserById(userId, context);

		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		MessageSourceResolvable msg = null;
		if ( isNew ) {
			msg = new DefaultMessageSourceResolvable(new String[] { "add.user.success" },
					new Object[] { savedUser.getName() }, "The user has been added.");
		} else {
			msg = new DefaultMessageSourceResolvable(new String[] { "update.user.success" },
					new Object[] { savedUser.getName() }, "The user has been saved.");
		}
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);

		return new ModelAndView(getSuccessView(), viewModel);
	}

	/** The command class. */
	public static class Command {

		private Long userId;

		/**
		 * @return Returns the userId.
		 */
		public Long getUserId() {
			return userId;
		}

		/**
		 * @param userId
		 *        The userId to set.
		 */
		public void setUserId(Long userId) {
			this.userId = userId;
		}

	}

	/**
	 * @return Returns the userBiz.
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz
	 *        The userBiz to set.
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

}
