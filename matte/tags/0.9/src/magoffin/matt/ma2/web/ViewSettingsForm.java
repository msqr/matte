/* ===================================================================
 * ViewSettingsForm.java
 * 
 * Created Jan 6, 2007 1:57:51 PM
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
 * $Id: ViewSettingsForm.java,v 1.1 2007/01/06 08:32:03 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.domain.MediaSpec;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller for updating the current (session only) view settings.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/01/06 08:32:03 $
 */
public class ViewSettingsForm extends AbstractForm {

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		Command cmd = (Command)super.formBackingObject(request);
		if ( cmd.thumb == null ) {
			cmd.thumb = getDomainObjectFactory().newMediaSpecInstance();
		}
		if ( cmd.view == null ) {
			cmd.view = getDomainObjectFactory().newMediaSpecInstance();
		}
		return cmd;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, 
			HttpServletResponse response, Object command, BindException errors) throws Exception {
		
		Command cmd = (Command)command;

		getWebHelper().saveAnonymousUserSession(request, cmd.getThumb(), cmd.getView());
		
		if ( getWebHelper().getSavedRequestURL(request) != null ) {
			String savedUrl = getWebHelper().getSavedRequestURL(request);
			getWebHelper().clearSavedRequestURL(request);
			response.sendRedirect(savedUrl);
			return null;
		}
		return new ModelAndView(getSuccessView());
	}

	/** Command class. */
	public static class Command {
		private MediaSpec thumb;
		private MediaSpec view;
		
		/**
		 * @return the thumb
		 */
		public MediaSpec getThumb() {
			return thumb;
		}
		
		/**
		 * @param thumb the thumb to set
		 */
		public void setThumb(MediaSpec thumb) {
			this.thumb = thumb;
		}
		
		/**
		 * @return the view
		 */
		public MediaSpec getView() {
			return view;
		}
		
		/**
		 * @param view the view to set
		 */
		public void setView(MediaSpec view) {
			this.view = view;
		}
		
	}

}
