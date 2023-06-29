/* ===================================================================
 * DeleteCollectionController.java
 * 
 * Created May 1, 2006 12:33:32 PM
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

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delete a Collection (deleting all media items from it as well).
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class DeleteCollectionController extends AbstractCommandController {
	
	private MediaBiz mediaBiz = null;
	private CollectionDao collectionDao = null;

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractCommandController#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		
		// check for error
		if ( errors.hasErrors() ) {
			return new ModelAndView(getErrorView(),errors.getModel());
		}
		
		Command cmd = (Command)command;
		
		// get the collection so we know its name
		Collection collectionToDelete = collectionDao.get(cmd.collectionId);
		
		// if collection not found return error
		if ( collectionToDelete == null ) {
			errors.rejectValue("collectionId", "collection.notavailable", new Object[]{cmd.collectionId}, 
					"The requested collection [" +cmd.collectionId +"] is not available.");
			return new ModelAndView(getErrorView(),errors.getModel()); 
		}
		
		// delete collection
		BizContext context = getWebHelper().getBizContext(request,true);
		int numItemsDeleted = mediaBiz.deleteCollectionAndItems(
				cmd.collectionId, context).size();
		
		Map<String,Object> model = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"collection.deleted"}, new Object[]{
						collectionToDelete.getName(), numItemsDeleted},
				"The album ["+collectionToDelete.getName()+"] has been deleted.");
		
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		model.put("deleted.collectionId",String.valueOf(cmd.collectionId));

		return new ModelAndView(getSuccessView(),model);
	}

	/** Command class. */
	public static class Command {
		private Long collectionId = null;
	
		/**
		 * @return Returns the albumId.
		 */
		public Long getCollectionId() {
			return collectionId;
		}
		
		/**
		 * @param albumId The albumId to set.
		 */
		public void setCollectionId(Long albumId) {
			this.collectionId = albumId;
		}
		
		
	}
	
	/** Validator class. */
	public static class CommandValidator implements Validator {

		public boolean supports(@SuppressWarnings("rawtypes") Class clazz) {
			return Command.class.isAssignableFrom(clazz);
		}

		public void validate(Object obj, Errors errors) {
			Command cmd = (Command)obj;
			if ( cmd.collectionId == null ) {
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
	 * @param mediaBiz the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	/**
	 * @return the collectionDao
	 */
	public CollectionDao getCollectionDao() {
		return collectionDao;
	}
	
	/**
	 * @param collectionDao the collectionDao to set
	 */
	public void setCollectionDao(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
	}

}
