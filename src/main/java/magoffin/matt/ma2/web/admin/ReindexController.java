/* ===================================================================
 * ReindexController.java
 * 
 * Created May 27, 2006 3:07:38 PM
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

package magoffin.matt.ma2.web.admin;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IndexBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.lucene.IndexType;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Controller for re-indexing a search index.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class ReindexController extends AbstractCommandController {
	
	private IndexBiz indexBiz;

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getAdminBizContext(request);
		Command cmd = (Command)command;
				
		WorkInfo workInfo = null;
		switch ( cmd.indexType ) {
			case USER:
				workInfo = indexBiz.recreateUserIndex(context);
				break;
				
			case MEDIA_ITEM:
				workInfo = indexBiz.recreateMediaItemIndex(context);
				break;
				
			default:
				throw new UnsupportedOperationException();
		}

		Map<String,Object> model = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = null;
		getWebHelper().populateModelWorkInfo(request, workInfo, model);
		msg = new DefaultMessageSourceResolvable(
				new String[] {"reindexing.pending"}, null,
				"The reindexing request has been submitted.");
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		
		return new ModelAndView(getSuccessView(),model);
	}

	/**
	 * Command class.
	 */
	public static class Command {
		
		private IndexType indexType = null;
		
		/**
		 * @return the indexType
		 */
		public IndexType getIndexType() {
			return indexType;
		}
		
		/**
		 * @param indexType the indexType to set
		 */
		public void setIndexType(IndexType indexType) {
			this.indexType = indexType;
		}
		
	}
	
	/**
	 * @return the indexBiz
	 */
	public IndexBiz getIndexBiz() {
		return indexBiz;
	}
	
	/**
	 * @param indexBiz the indexBiz to set
	 */
	public void setIndexBiz(IndexBiz indexBiz) {
		this.indexBiz = indexBiz;
	}

}
