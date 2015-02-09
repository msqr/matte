/* ===================================================================
 * SaveMediaItemInfoController.java
 * 
 * Created Oct 3, 2006 7:08:06 PM
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
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.support.MediaInfoCommand;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.util.ThreadSafeDateFormat;
import magoffin.matt.util.TimeZoneEditor;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for saving media info.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class SaveMediaItemInfoController extends AbstractCommandController {

	private MediaBiz mediaBiz;
	private ThreadSafeDateFormat mediaDateFormat;

	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, false);
		MediaInfoCommand cmd = (MediaInfoCommand) command;
		mediaBiz.storeMediaItemInfo(cmd, context);
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] { "media.info.stored" }, null, "The media info has been saved.");

		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);
		return new ModelAndView(getSuccessView(), viewModel);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
			throws Exception {
		super.initBinder(request, binder);
		registerCalendarEditor(binder, null, this.mediaDateFormat, null);
		binder.registerCustomEditor(TimeZone.class, new TimeZoneEditor(true));
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

	/**
	 * @return the mediaDateFormat
	 */
	public ThreadSafeDateFormat getMediaDateFormat() {
		return mediaDateFormat;
	}

	/**
	 * @param mediaDateFormat
	 *        the mediaDateFormat to set
	 */
	public void setMediaDateFormat(ThreadSafeDateFormat mediaDateFormat) {
		this.mediaDateFormat = mediaDateFormat;
	}

}
