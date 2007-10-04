/* ===================================================================
 * MediaItemRatingController.java
 * 
 * Created May 18, 2006 10:01:33 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: MediaItemRatingController.java,v 1.2 2006/05/19 10:11:46 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Set the rating for a media item.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.2 $ $Date: 2006/05/19 10:11:46 $
 */
public class MediaItemRatingController extends AbstractCommandController {
	
	private MediaBiz mediaBiz = null;

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request,true);
		
		mediaBiz.storeMediaItemRating(cmd.getItemIds(), cmd.getRating(), context);
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"items.rating.set"}, new Object[] {
						cmd.getItemIds() == null ? 0 : cmd.getItemIds().length,
						cmd.getRating() },
				(cmd.getItemIds() == null ? 0 : cmd.getItemIds().length) 
						+" media item ratings set to " +cmd.getRating());
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		return new ModelAndView(getSuccessView(),viewModel);
	}

	/**
	 * Command class.
	 */
	public static class Command {
		private Long[] itemIds;
		private short rating;
		
		/**
		 * @return Returns the rating.
		 */
		public short getRating() {
			return rating;
		}

		/**
		 * @param rating The rating to set.
		 */
		public void setRating(short rating) {
			this.rating = rating;
		}

		/**
		 * @return the itemIds
		 */
		public Long[] getItemIds() {
			return itemIds;
		}
		
		/**
		 * @param itemIds the itemIds to set
		 */
		public void setItemIds(Long[] itemIds) {
			this.itemIds = itemIds;
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
