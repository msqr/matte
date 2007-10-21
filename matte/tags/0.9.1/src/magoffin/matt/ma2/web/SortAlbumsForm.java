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
 * $Id$
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

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.support.BasicAlbumSearchCriteria;
import magoffin.matt.ma2.support.SortAlbumsCommand;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to save the ordering of child albums within an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class SortAlbumsForm extends AbstractForm {
	
	private MediaBiz mediaBiz;
	private SearchBiz searchBiz;
	
	/**
	 * Command class.
	 */
	public static class Command {
		
		private Long albumId = null;
		private AlbumData[] children = new AlbumData[0];
		
		/**
		 * @return the children
		 */
		public AlbumData[] getChildren() {
			return children;
		}
		
		/**
		 * @param children the children to set
		 */
		public void setChildren(AlbumData[] children) {
			this.children = children;
		}

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
		
	}
	
	/**
	 * Album sort data structure.
	 */
	public static class AlbumData {
		private Long albumId = null;
		private Long posterId = null;
		private Integer order = null;
		
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
		 * @return the order
		 */
		public Integer getOrder() {
			return order;
		}
		
		/**
		 * @param order the order to set
		 */
		public void setOrder(Integer order) {
			this.order = order;
		}
		
		/**
		 * @return the posterId
		 */
		public Long getPosterId() {
			return posterId;
		}
		
		/**
		 * @param posterId the posterId to set
		 */
		public void setPosterId(Long posterId) {
			this.posterId = posterId;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		Command cmd = new Command();
		if ( request.getParameter("albumId") != null ) {
			BeanWrapper wrapper = new BeanWrapperImpl(cmd);
			wrapper.setPropertyValue("albumId", request.getParameter("albumId"));
			Album a = this.mediaBiz.getAlbum(cmd.getAlbumId(), context);
			AlbumData[] albumData = new AlbumData[a.getAlbum().size()];
			for ( int i = 0; i < albumData.length; i++ ) {
				albumData[i] = new AlbumData();
			}
			cmd.setChildren(albumData);
		}
		return cmd;
	}

	@Override
	protected void onBindOnNewForm(HttpServletRequest request, Object command) 
	throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		
		// populate album data from albumIds
		Command cmd = (Command)command;
		if ( cmd.getAlbumId() == null ) {
			return;
		}
		Album a = this.mediaBiz.getAlbum(cmd.getAlbumId(), context);
		AlbumData[] albumData = cmd.getChildren();
		for ( int i = 0; i < cmd.children.length; i++ ) {
			Album child = (Album)a.getAlbum().get(i);
			AlbumData data = new AlbumData();
			data.setAlbumId(child.getAlbumId());
			data.setOrder(i);
			if ( a.getPoster() != null ) {
				data.setPosterId(a.getPoster().getItemId());
			} else if ( a.getItem().size() > 0 ) {
				data.setPosterId(((MediaItem)a.getItem().get(0)).getItemId());
			}
			albumData[i] = data;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request, true);
		Map<String, Object> viewModel = new HashMap<String, Object>();
		Model model = getDomainObjectFactory().newModelInstance();
			
		// we use search API here for performance, so we don't load all items in album
		BasicAlbumSearchCriteria criteria = new BasicAlbumSearchCriteria(cmd.getAlbumId());
		SearchResults searchResults = searchBiz.findAlbums(criteria, 
				null, context);
		model.setSearchResults(searchResults);

		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return viewModel;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command,	BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		Command cmd = (Command)command;
		
		SortAlbumsCommand sortCmd = new SortAlbumsCommand();
		sortCmd.setAlbumId(cmd.getAlbumId());
		SortedMap<Integer, Long> orderedChildren = new TreeMap<Integer, Long>();
		if ( cmd.getChildren() != null ) {
			for ( AlbumData child : cmd.getChildren() ) {
				orderedChildren.put(child.getOrder(), child.getAlbumId());
			}
		}
		Long[] childrenIds = orderedChildren.values().toArray(new Long[0]);
		sortCmd.setChildAlbumIds(childrenIds);
		this.mediaBiz.storeAlbumOrdering(sortCmd, context);
		
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"save.album.ordering.success"}, 
				null, "The album ordering has been saved.");
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);
		return new ModelAndView(getSuccessView(), viewModel);
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
	 * @return the searchBiz
	 */
	public SearchBiz getSearchBiz() {
		return searchBiz;
	}
	
	/**
	 * @param searchBiz the searchBiz to set
	 */
	public void setSearchBiz(SearchBiz searchBiz) {
		this.searchBiz = searchBiz;
	}
	
}
