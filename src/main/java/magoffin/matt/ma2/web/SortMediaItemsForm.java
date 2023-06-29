/* ===================================================================
 * SortAlbumsForm.java
 * 
 * Created Jul 3, 2007 9:28:05 PM
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
 */

package magoffin.matt.ma2.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.support.SortMediaItemsCommand;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Controller to save the ordering of child albums within an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class SortMediaItemsForm extends AbstractForm {

	private MediaBiz mediaBiz;

	/**
	 * Command class.
	 */
	public static class Command {

		private Long albumId = null;
		private MediaItemData[] items = new MediaItemData[0];

		/**
		 * @return the albumId
		 */
		public Long getAlbumId() {
			return albumId;
		}

		/**
		 * @param albumId
		 *        the albumId to set
		 */
		public void setAlbumId(Long albumId) {
			this.albumId = albumId;
		}

		/**
		 * @return the items
		 */
		public MediaItemData[] getItems() {
			return items;
		}

		/**
		 * @param items
		 *        the items to set
		 */
		public void setItems(MediaItemData[] items) {
			this.items = items;
		}

	}

	/**
	 * MediaItem sort data structure.
	 */
	public static class MediaItemData {

		private Long itemId = null;
		private Integer order = null;

		/**
		 * @return the itemId
		 */
		public Long getItemId() {
			return itemId;
		}

		/**
		 * @param itemId
		 *        the itemId to set
		 */
		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}

		/**
		 * @return the order
		 */
		public Integer getOrder() {
			return order;
		}

		/**
		 * @param order
		 *        the order to set
		 */
		public void setOrder(Integer order) {
			this.order = order;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#
	 * formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);
		Command cmd = new Command();
		if ( request.getParameter("albumId") != null ) {
			BeanWrapper wrapper = new BeanWrapperImpl(cmd);
			wrapper.setPropertyValue("albumId", request.getParameter("albumId"));
			Album a = this.mediaBiz.getAlbumWithItems(cmd.getAlbumId(), context);
			MediaItemData[] itemData = new MediaItemData[a.getItem().size()];
			for ( int i = 0; i < itemData.length; i++ ) {
				itemData[i] = new MediaItemData();
			}
			cmd.setItems(itemData);
		}
		return cmd;
	}

	@Override
	protected void onBindOnNewForm(HttpServletRequest request, Object command) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);

		// populate album data from albumIds
		Command cmd = (Command) command;
		if ( cmd.getAlbumId() == null ) {
			return;
		}
		Album a = this.mediaBiz.getAlbumWithItems(cmd.getAlbumId(), context);
		MediaItemData[] itemData = cmd.getItems();
		for ( int i = 0, length = cmd.getItems().length; i < length; i++ ) {
			MediaItem item = (MediaItem) a.getItem().get(i);
			MediaItemData data = new MediaItemData();
			data.setItemId(item.getItemId());
			data.setOrder(i);
			itemData[i] = data;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
			throws Exception {
		Command cmd = (Command) command;
		BizContext context = getWebHelper().getBizContext(request, true);
		Map<String, Object> viewModel = new HashMap<String, Object>();
		Model model = getDomainObjectFactory().newModelInstance();

		Album a = mediaBiz.getAlbumWithItems(cmd.getAlbumId(), context);
		model.getAlbum().add(a);

		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return viewModel;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);
		Command cmd = (Command) command;

		SortMediaItemsCommand sortCmd = new SortMediaItemsCommand();
		sortCmd.setAlbumId(cmd.getAlbumId());
		SortedMap<Integer, Long> orderedItems = new TreeMap<Integer, Long>();
		if ( cmd.getItems() != null ) {
			for ( MediaItemData item : cmd.getItems() ) {
				orderedItems.put(item.getOrder(), item.getItemId());
			}
		}
		Long[] itemIds = orderedItems.values().toArray(new Long[0]);
		sortCmd.setItemIds(itemIds);
		this.mediaBiz.storeMediaItemOrdering(sortCmd, context);

		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] { "save.items.ordering.success" }, null,
				"The item ordering has been saved.");
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);
		viewModel.put("albumId", cmd.getAlbumId());
		return new ModelAndView(getSuccessView(), viewModel);
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
