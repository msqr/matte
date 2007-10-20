/* ===================================================================
 * MediaItemsController.java
 * 
 * Created May 7, 2006 7:58:10 AM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Populate a list of media items, either from a collection, an album, 
 * or individually specified.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class MediaItemsController extends AbstractCommandController{

	private UserBiz userBiz = null;
	private MediaBiz mediaBiz = null;
	private MediaItemDao mediaItemDao = null;

	@Override
	protected ModelAndView handle(HttpServletRequest request, 
			HttpServletResponse response, Object command, BindException errors) 
	throws Exception {
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request,true);
		Model model = getDomainObjectFactory().newModelInstance();
		
		prepareModelForView(model,cmd,context);
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,model);
		return new ModelAndView(getSuccessView(),viewModel);
	}
	
	@SuppressWarnings("unchecked")
	private void prepareModelForView(Model model, Command cmd, BizContext context) {
		if ( model.getCollection().size() < 1 ) {
			User actingUser = context.getActingUser();
			
			// 1: populate media items for collection
			if ( cmd.getCollectionId() != null ) {
				Collection c = mediaBiz.getCollectionWithItems(cmd.getCollectionId(), context);
				if ( c != null ) {
					model.getCollection().add(c);
				}
				return;
			}
			
			// 2: populate media items for album
			if ( cmd.getAlbumId() != null ) {
				Album a = mediaBiz.getAlbumWithItems(cmd.getAlbumId(), context);
				if ( a != null ) {
					model.getAlbum().add(a);
				}
				return;
			}
			
			// 3: populate media items specified directly
			for ( Long itemId : cmd.getItemIds() ) {
				MediaItem item = mediaItemDao.get(itemId);
				Collection itemCollection = mediaBiz.getMediaItemCollection(item);
				if ( item != null && itemCollection.getOwner().getUserId().equals(
						actingUser.getUserId())) {
					model.getItem().add(item);
				}
			}
		}
	}
	
	/**
	 * Command class.
	 */
	public static class Command {
		private Long collectionId = null;
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
		 * @return Returns the collectionId.
		 */
		public Long getCollectionId() {
			return collectionId;
		}
		
		/**
		 * @param collectionId The collectionId to set.
		 */
		public void setCollectionId(Long collectionId) {
			this.collectionId = collectionId;
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
	
	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}
	
	/**
	 * @param userBiz the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}
	
	/**
	 * @return the mediaItemDao
	 */
	public MediaItemDao getMediaItemDao() {
		return mediaItemDao;
	}
	
	/**
	 * @param mediaItemDao the mediaItemDao to set
	 */
	public void setMediaItemDao(MediaItemDao mediaItemDao) {
		this.mediaItemDao = mediaItemDao;
	}

}
