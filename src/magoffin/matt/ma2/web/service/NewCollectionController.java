/* ===================================================================
 * AddToAlbumController.java
 * 
 * Created Mar 23, 2006 4:01:34 PM
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
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

/**
 * Create a new Collection for the active user.
 * 
 * @deprecated see {@link magoffin.matt.ma2.web.CollectionForm}
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
@Deprecated
public class NewCollectionController extends AbstractCommandController {
	
	private UserBiz userBiz;
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractCommandController#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		
		// check for error
		if ( errors.hasErrors() ) {
			return new ModelAndView(getErrorView(),errors.getModel());
		}
		
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request,true);
		Collection newCollection = getDomainObjectFactory().newCollectionInstance();
		if ( StringUtils.hasText(cmd.name) ) {
			newCollection.setName(cmd.name);
		}
		if ( StringUtils.hasText(cmd.comment) ) {
			newCollection.setComment(cmd.comment.trim());
		}
		
		newCollection = userBiz.newCollectionForUser(newCollection, 
				context.getActingUser(), context);
		
		Map<String,Object> model = new LinkedHashMap<String,Object>();
		Model ui = getDomainObjectFactory().newModelInstance();
		ui.getCollection().add(newCollection);
		model.put(WebConstants.DEFALUT_MODEL_OBJECT,ui);

		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"new.collection.created"}, new Object[]{newCollection.getName()},
				"The collection ["+newCollection.getName()+"] has been created.");
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);

		return new ModelAndView(getSuccessView(),model);
	}
	
	/** Command class. */
	public static class Command {
		private String name;
		private String comment;
		
		/**
		 * @param comment The comment to set.
		 */
		public void setComment(String comment) {
			this.comment = comment;
		}
		
		/**
		 * @param name The name to set.
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * @return Returns the comment.
		 */
		public String getComment() {
			return comment;
		}
		
		/**
		 * @return Returns the name.
		 */
		public String getName() {
			return name;
		}
		
	}
	
	/** Validator class. */
	public static class CommandValidator implements Validator {

		public boolean supports(@SuppressWarnings("rawtypes") Class clazz) {
			return Command.class.isAssignableFrom(clazz);
		}

		public void validate(Object obj, Errors errors) {
			Command cmd = (Command)obj;
			if ( !StringUtils.hasText(cmd.name) ) {
				errors.rejectValue("name", "error.required", null, "album.name.displayName");
			}
		}
		
	}
	
	/**
	 * @return Returns the userBiz.
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}
	
	/**
	 * @param userBiz The userBiz to set.
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}
	
}
