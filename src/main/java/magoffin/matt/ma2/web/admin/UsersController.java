/* ===================================================================
 * UsersController.java
 * 
 * Created Oct 2, 2006 7:23:06 PM
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

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to administer users.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class UsersController extends AbstractCommandController {

	private UserBiz userBiz;
	
	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getAdminBizContext(request);
		Command cmd = (Command)command;
		Model model = getDomainObjectFactory().newModelInstance();
		if ( cmd.userId != null ) {
			User user = getUserBiz().getUserById(cmd.userId, context);
			if ( user != null ) {
				model.getUser().add(user);
			}
		}
		
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return new ModelAndView(getSuccessView(), viewModel);
	}
	
	/** Command object. */
	public static class Command {
		private Long userId;

		/**
		 * @return the userId
		 */
		public Long getUserId() {
			return userId;
		}
		
		/**
		 * @param userId the userId to set
		 */
		public void setUserId(Long userId) {
			this.userId = userId;
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
