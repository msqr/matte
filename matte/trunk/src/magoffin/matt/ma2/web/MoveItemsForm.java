/* ===================================================================
 * MoveItemsForm.java
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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItemSearchResult;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.support.MoveItemsCommand;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller for moving items into a new Collection.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class MoveItemsForm extends AbstractForm {
	
	private MediaBiz mediaBiz;
	private UserBiz userBiz;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, 
			Errors errors) throws Exception {
		MoveItemsCommand cmd = (MoveItemsCommand)command;
		BizContext context = getWebHelper().getBizContext(request, true);
		
		Model model = getDomainObjectFactory().newModelInstance();
		model.getCollection().add(userBiz.getCollection(cmd.getCollectionId(), context));
		SearchResults sr = getDomainObjectFactory().newSearchResultsInstance();
		for ( Long itemId : cmd.getItemIds() ) {
			MediaItemSearchResult item = getDomainObjectFactory().newMediaItemSearchResultInstance();
			item.setItemId(itemId);
			sr.getItem().add(item);
		}
		model.setSearchResults(sr);
		
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		viewModel.put(WebConstants.DEFALUT_REFERENCE_DATA_OBJECT, model);
		return viewModel;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, 
			HttpServletResponse response, Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);
		MoveItemsCommand cmd = (MoveItemsCommand)command;
		
		getMediaBiz().moveMediaItems(cmd, context);

		Map<String,Object> model = new LinkedHashMap<String,Object>();
		Collection c = userBiz.getCollection(cmd.getCollectionId(), context);
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"move.items.moved"}, 
				new Object[] {c.getName()},
				"The items have been moved into the [" +c.getName() +"] collection");
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		return new ModelAndView(getSuccessView(),model);
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
