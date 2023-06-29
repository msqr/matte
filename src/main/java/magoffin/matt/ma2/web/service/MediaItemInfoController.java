/* ===================================================================
 * MediaItemInfoController.java
 * 
 * Created May 16, 2006 9:12:14 PM
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

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Return a single media item with full info or an aggregate of a set 
 * of media item info populated in a single media item.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class MediaItemInfoController extends AbstractCommandController {

	/** A flag added to the view model to signal that only the item info is desired. */
	public static final String MEDIA_ITEM_INFO_FLAG = "item.info.flag";
	
	private MediaBiz mediaBiz = null;

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request, false);
		Model model = getDomainObjectFactory().newModelInstance();
		
		if ( cmd.getAlbumKey() != null ) {
			Album album = mediaBiz.getSharedAlbum(cmd.getAlbumKey(), context);
			model.getAlbum().add(album);
		}
		
		if ( cmd.getItemIds() != null && cmd.getItemIds().length == 1) {
			MediaItem item = mediaBiz.getMediaItemWithInfo(cmd.getItemIds()[0], context);
			model.getItem().add(item);
		} else if ( cmd.getItemIds() != null && cmd.getItemIds().length > 1 ) {
			// TODO support multiple items here
			MediaItem item = mediaBiz.getMediaItemWithInfo(cmd.getItemIds()[0], context);
			model.getItem().add(item);
		}
		
		if ( cmd.getThemeId() != null ) {
			Theme theme = getSystemBiz().getThemeById(cmd.getThemeId());
			if ( theme != null ) {
				model.getTheme().add(theme);
			}
		}
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,model);
		viewModel.put(MEDIA_ITEM_INFO_FLAG, Boolean.TRUE);
		String successView = getSuccessView();
		if ( model.getTheme().size() == 1 ) {
			successView += ((Theme)model.getTheme().get(0)).getBasePath()+"/info";
		}
		return new ModelAndView(successView, viewModel);
	}

	/**
	 * Command class.
	 */
	public static class Command {
		private String albumKey = null;
		private Long[] itemIds = null;
		private Long themeId = null;
		
		/**
		 * @return the albumKey
		 */
		public String getAlbumKey() {
			return albumKey;
		}
		
		/**
		 * @param albumKey the albumKey to set
		 */
		public void setAlbumKey(String albumKey) {
			this.albumKey = albumKey;
		}

		/**
		 * Set a single item ID.
		 * @param itemId the ID
		 */
		public void setItemId(Long itemId) {
			itemIds = new Long[] {itemId};
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
