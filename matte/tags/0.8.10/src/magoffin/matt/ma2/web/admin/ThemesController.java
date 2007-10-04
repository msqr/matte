/* ===================================================================
 * ThemesController.java
 * 
 * Created Jun 4, 2006 9:22:42 AM
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
 * $Id: ThemesController.java,v 1.3 2007/07/13 23:17:22 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web.admin;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to administer themes.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2007/07/13 23:17:22 $
 */
public class ThemesController extends AbstractCommandController {
	
	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		getWebHelper().getAdminBizContext(request);
		Command cmd = (Command)command;
		Model model = getDomainObjectFactory().newModelInstance();
		
		if ( cmd.themeId == null ) {
			// get all themes
			model.getTheme().addAll(getSystemBiz().getAvailableThemes());
		} else {
			Theme theme = getSystemBiz().getThemeById(cmd.getThemeId());
			if ( theme != null ) {
				model.getTheme().add(theme);
			}
		}
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,model);
		return new ModelAndView(getSuccessView(),viewModel);
	}
	
	/** Command object. */
	public static class Command {
		private Long themeId;
		
		/**
		 * @return Returns the themeId.
		 */
		public Long getThemeId() {
			return themeId;
		}
		
		/**
		 * @param themeId The themeId to set.
		 */
		public void setThemeId(Long themeId) {
			this.themeId = themeId;
		}
		
	}
	
}
