/* ===================================================================
 * DynamicThemeResourceController.java
 * 
 * Created Jun 21, 2007 3:55:01 PM
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
 * $Id: DynamicThemeResourceController.java,v 1.2 2007/06/23 07:21:13 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to render dynamic content, such as JavaScript or CSS, for a theme.
 * 
 * <p>This is especially useful for generating dynamic CSS url() values, but can 
 * be used to render any dynamic content as a content type other than XML/HTML.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/06/23 07:21:13 $
 */
public class DynamicThemeResourceController extends AbstractCommandController {
	
	private String themeResourceName = "css";

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		getWebHelper().getBizContextWithViewSettings(request);
		Command cmd = (Command)command;
		Model model = getDomainObjectFactory().newModelInstance();
		Theme theme = null;
		if ( cmd.getThemeId() != null ) {
			theme = getSystemBiz().getThemeById(cmd.getThemeId());
		}
		if ( theme == null ) {
			theme = getSystemBiz().getDefaultTheme();
		}
		model.getTheme().add(theme);
		getWebHelper().saveRequestTheme(theme);
		
		Map<String,Object> viewModel = errors.getModel();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return new ModelAndView(
				getSuccessView()+theme.getBasePath()+"/"+themeResourceName,
				viewModel);
	}

	/** Command class. */
	public static final class Command {
		
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
	
	/**
	 * @return the themeResourceName
	 */
	public String getThemeResourceName() {
		return themeResourceName;
	}

	/**
	 * @param themeResourceName the themeResourceName to set
	 */
	public void setThemeResourceName(String themeResourceName) {
		this.themeResourceName = themeResourceName;
	}
	
}
