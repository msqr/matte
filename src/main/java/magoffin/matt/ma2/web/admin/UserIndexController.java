/* ===================================================================
 * UserIndexController.java
 * 
 * Created May 30, 2006 6:02:40 PM
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

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Get User index data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class UserIndexController extends AbstractCommandController {
	
	private SearchBiz searchBiz;

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getAdminBizContext(request);
		Command cmd = (Command)command;
		
		// perform user index search
		PaginationCriteria criteria = getDomainObjectFactory().newPaginationCriteriaInstance();
		criteria.setIndexKey(cmd.getIndexKey());
		SearchResults results = searchBiz.findUsersForIndex(criteria, context);
		results.setPagination(criteria);
		
		Model model = getDomainObjectFactory().newModelInstance();
		model.setSearchResults(results);
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,model);
		return new ModelAndView(getSuccessView(),viewModel);
	}

	/** Command class. */
	public static class Command {
		
		private String indexKey = null;
		
		/**
		 * @return the indexKey
		 */
		public String getIndexKey() {
			return indexKey;
		}
		
		/**
		 * @param indexKey the indexKey to set
		 */
		public void setIndexKey(String indexKey) {
			this.indexKey = indexKey;
		}
		
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
