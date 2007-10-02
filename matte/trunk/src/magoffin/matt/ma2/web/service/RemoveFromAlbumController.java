/* ===================================================================
 * RemoveFromAlbumController.java
 * 
 * Created May 12, 2006 6:29:14 PM
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
 * $Id: RemoveFromAlbumController.java,v 1.2 2006/07/13 09:09:56 matt Exp $
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
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Remove media items from an album.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.2 $ $Date: 2006/07/13 09:09:56 $
 */
public class RemoveFromAlbumController extends AbstractCommandController {
	
	private MediaBiz mediaBiz = null;
	private AlbumDao albumDao = null;

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request,true);
		Album a = albumDao.get(cmd.albumId);
		int numRemoved = mediaBiz.removeMediaItemsFromAlbum(cmd.albumId, 
				cmd.itemIds, context);

		Map<String,Object> model = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"items.removedfrom.album"}, new Object[]{numRemoved,a.getName()},
				numRemoved +" media items have been removed from album [" +a.getName() +"]");
		
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		return new ModelAndView(getSuccessView(),model);
	}

	/** Command class. */
	public static class Command {
		private Long albumId = null;
		private Long[] itemIds = null;
	
		/**
		 * @return Returns the albumId.
		 */
		public Long getAlbumId() {
			return albumId;
		}
		
		/**
		 * @param albumId The albumId to set.
		 */
		public void setAlbumId(Long albumId) {
			this.albumId = albumId;
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
	 * @return the albumDao
	 */
	public AlbumDao getAlbumDao() {
		return albumDao;
	}
	
	/**
	 * @param albumDao the albumDao to set
	 */
	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
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
