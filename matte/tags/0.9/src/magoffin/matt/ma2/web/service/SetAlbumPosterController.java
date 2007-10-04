/* ===================================================================
 * SetAlbumPosterController.java
 * 
 * Created Mar 18, 2007 6:18:29 PM
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
 * $Id: SetAlbumPosterController.java,v 1.1 2007/03/18 06:50:59 matt Exp $
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
import org.springframework.web.servlet.ModelAndView;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Controller to set the media item poster of an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/03/18 06:50:59 $
 */
public class SetAlbumPosterController extends AbstractCommandController {
	
	private MediaBiz mediaBiz;

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);
		Command cmd = (Command)command;
		mediaBiz.storeMediaItemPoster(cmd.getItemId(), cmd.getAlbumId(), context);
		
		Map<String,Object> model = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"album.poster.set"}, null,
				"The album poster has been set.");
		
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		return new ModelAndView(getSuccessView(),model);
	}

	/**
	 * Command object.
	 */
	public static class Command {
		private Long itemId;
		private Long albumId;
		
		/**
		 * @return the albumId
		 */
		public Long getAlbumId() {
			return albumId;
		}
		
		/**
		 * @param albumId the albumId to set
		 */
		public void setAlbumId(Long albumId) {
			this.albumId = albumId;
		}
		
		/**
		 * @return the itemId
		 */
		public Long getItemId() {
			return itemId;
		}
		
		/**
		 * @param itemId the itemId to set
		 */
		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}
		
	}
	
	/**
	 * @return the mediaBiz
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}
	
	/**
	 * @param mediaBiz the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}
	
}
