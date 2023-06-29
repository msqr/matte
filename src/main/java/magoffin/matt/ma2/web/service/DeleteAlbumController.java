/* ===================================================================
 * DeleteAlbumController.java
 * 
 * Created Mar 23, 2006 4:01:34 PM
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

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Delete an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class DeleteAlbumController extends AbstractCommandController {

	private MediaBiz mediaBiz;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.AbstractCommandController#handle(
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, java.lang.Object,
	 * org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {

		BizContext context = getWebHelper().getBizContext(request, true);

		// check for error
		if ( errors.hasErrors() ) {
			return new ModelAndView(getErrorView(), errors.getModel());
		}

		Command cmd = (Command) command;

		// delete album
		Album deletedAlbum = mediaBiz.deleteAlbum(cmd.albumId, context);

		// if album not found return error
		if ( deletedAlbum == null ) {
			errors.rejectValue("albumId", "album.notavailable", new Object[] { cmd.albumId },
					"The requested album [" + cmd.albumId + "] is not available.");
			return new ModelAndView(getErrorView(), errors.getModel());
		}

		Map<String, Object> model = new LinkedHashMap<String, Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] { "album.deleted" }, new Object[] { deletedAlbum.getName() },
				"The album [" + deletedAlbum.getName() + "] has been deleted.");

		model.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);
		model.put("deleted.albumId", String.valueOf(cmd.albumId));

		return new ModelAndView(getSuccessView(), model);
	}

	/** Command class. */
	public static class Command {

		private Long albumId = null;

		/**
		 * @return Returns the albumId.
		 */
		public Long getAlbumId() {
			return albumId;
		}

		/**
		 * @param albumId
		 *        The albumId to set.
		 */
		public void setAlbumId(Long albumId) {
			this.albumId = albumId;
		}

	}

	/** Validator class. */
	public static class CommandValidator implements Validator {

		@Override
		public boolean supports(@SuppressWarnings("rawtypes") Class clazz) {
			return Command.class.isAssignableFrom(clazz);
		}

		@Override
		public void validate(Object obj, Errors errors) {
			Command cmd = (Command) obj;
			if ( cmd.albumId == null ) {
				errors.rejectValue("albumId", "error.required", null, "album.id.displayName");
			}
		}

	}

	/**
	 * @return the mediaBiz
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz
	 *        the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

}
