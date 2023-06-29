/* ===================================================================
 * HelpController.java
 * 
 * Created Sep 24, 2006 2:29:52 PM
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

import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for returning dynamic help.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>customViews</dt>
 *   <dd>A Map of help IDs to view names, to use instead of the 
 *   default success view. This allows for specific help contents
 *   to be generated as more than just a simple message.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class HelpController extends AbstractCommandController {
	
	/** The view model key that will contain the requested help ID. */
	public static final String HELP_ID_VIEW_MODEL_KEY = "helpId";
	
	private Map<String, String> customViews = null;

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		String helpId = cmd.getHelpId();
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		viewModel.put(HELP_ID_VIEW_MODEL_KEY, helpId);

		if ( customViews != null && customViews.containsKey(helpId) ) {
			return new ModelAndView(customViews.get(helpId), viewModel);
		}
		
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {helpId}, null, "Missing dynamic help [" +helpId +"]");
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);

		return new ModelAndView(getSuccessView(),viewModel);
	}

	/**
	 * Command class.
	 */
	public static class Command {
		
		private String helpId;

		/**
		 * @return the helpId
		 */
		public String getHelpId() {
			return helpId;
		}
		
		/**
		 * @param helpId the helpId to set
		 */
		public void setHelpId(String helpId) {
			this.helpId = helpId;
		}
		
	}
	
	/**
	 * @return the customViews
	 */
	public Map<String, String> getCustomViews() {
		return customViews;
	}
	
	/**
	 * @param customViews the customViews to set
	 */
	public void setCustomViews(Map<String, String> customViews) {
		this.customViews = customViews;
	}

}
