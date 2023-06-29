/* ===================================================================
 * CollectionForm.java
 * 
 * Created Nov 30, 2006 8:36:30 PM
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

package magoffin.matt.ma2.web;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.Edit;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Form controller for administering collection details.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class CollectionForm extends AbstractForm {

	private UserBiz userBiz;

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		Command cmd = new Command();
		BizContext context = getWebHelper().getBizContext(request, true);

		// see if trying to populate collection
		ServletRequestDataBinder binder = createBinder(request, cmd);
		binder.bind(request);

		Collection collection = null;
		if ( cmd.getCollectionId() != null ) {
			collection = userBiz.getCollection(cmd.getCollectionId(), context);
		} else {
			collection = getDomainObjectFactory().newCollectionInstance();
		}

		Edit model = getDomainObjectFactory().newEditInstance();
		model.setCollection(collection);
		return model;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		Edit model = (Edit) command;
		BizContext context = getWebHelper().getBizContext(request, true);

		boolean isNew = model.getCollection().getCollectionId() == null;

		// save collection
		Long collectionId = userBiz.storeCollection(model.getCollection(), context);
		Collection savedCollection = userBiz.getCollection(collectionId, context);

		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		Model ui = getDomainObjectFactory().newModelInstance();
		ui.getCollection().add(savedCollection);
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, ui);

		MessageSourceResolvable msg = null;
		if ( isNew ) {
			msg = new DefaultMessageSourceResolvable(new String[] { "add.collection.success" },
					new Object[] { savedCollection.getName() }, "The collection has been added.");
		} else {
			msg = new DefaultMessageSourceResolvable(new String[] { "update.collection.success" },
					new Object[] { savedCollection.getName() }, "The collection has been saved.");
		}
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);

		return new ModelAndView(getSuccessView(), viewModel);
	}

	/** The command class. */
	public static class Command {

		private Long collectionId;

		/**
		 * @return the collectionId
		 */
		public Long getCollectionId() {
			return collectionId;
		}

		/**
		 * @param collectionId
		 *        the collectionId to set
		 */
		public void setCollectionId(Long collectionId) {
			this.collectionId = collectionId;
		}

	}

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz
	 *        the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

}
