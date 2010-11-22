/* ===================================================================
 * AddCommentForm.java
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
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.support.UserCommentCommand;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller for adding user comments to media items.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class AddCommentForm extends AbstractForm {

	private MediaBiz mediaBiz;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command,
			Errors errors) throws Exception {
		BizContext context = getWebHelper().getAnonymousBizContext(request);
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		Model model = getDomainObjectFactory().newModelInstance();
		UserCommentCommand cmd = (UserCommentCommand)command;
		MediaItem item = mediaBiz.getMediaItemWithInfo(cmd.getItemId(), context);
		if ( item != null ) {
			model.getItem().add(item);
		}
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return viewModel;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command,	BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getAnonymousBizContext(request);
		UserCommentCommand cmd = (UserCommentCommand)command;
		mediaBiz.storeMediaItemUserComment(cmd, context);
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] {"add.comment.saved"}, null,
				"Your comment has been saved.");
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
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
	
}
