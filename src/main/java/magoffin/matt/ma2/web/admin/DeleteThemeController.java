/* ===================================================================
 * DeleteThemeController.java
 * 
 * Created Oct 1, 2006 2:34:03 PM
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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.ValidationException;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to delete a Theme.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class DeleteThemeController extends AbstractCommandController {

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getAdminBizContext(request);
		Command cmd = (Command)command;
		Theme theme = getSystemBiz().getThemeById(cmd.themeId);
		
		Map<String, Object> viewModel = errors.getModel();
		try {
			getSystemBiz().deleteTheme(theme, context);
			MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
					new String[] {"delete.theme.success"},
					new Object[] {theme.getName()},
					"Theme [" +theme.getName() +"] has been deleted");
			viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);
		} catch ( ValidationException e ) {
			// in case tried to delete internal theme
			errors.addAllErrors(e.getErrors());
		}
		return new ModelAndView(getSuccessView(), viewModel);
	}

	/**
	 * Command class.
	 */
	public static class Command {
		private Long themeId;

		/**
		 * @return the themeId
		 */
		public Long getThemeId() {
			return themeId;
		}
		
		/**
		 * @param themeId the themeId to set
		 */
		public void setThemeId(Long themeId) {
			this.themeId = themeId;
		}
		
	}

}
