/* ===================================================================
 * AlbumFeedController.java
 * 
 * Created Nov 3, 2006 5:47:17 PM
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
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for generating a user's album feed.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class AlbumFeedController extends AbstractCommandController {
	
	private UserBiz userBiz;
	private SearchBiz searchBiz;

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getAnonymousBizContext(request);
		BrowseAlbumsCommand cmd = (BrowseAlbumsCommand)command;
		
		Model model = getDomainObjectFactory().newModelInstance();
		User browseUser = getUserBiz().getUserByAnonymousKey(cmd.getUserKey());
		model.getUser().add(browseUser);

		Theme theme = browseUser.getBrowseTheme();
		if ( theme == null ) {
			theme = getSystemBiz().getDefaultTheme();
		}
		model.getTheme().add(theme);

		//List<Album> feed = getUserBiz().getAlbumFeedForUser(cmd);
		//model.getAlbum().addAll(feed);
		SearchResults feed = getSearchBiz().findAlbumsForBrowsing(
				cmd, null, context);
		model.setSearchResults(feed);
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,model);
		return new ModelAndView(getSuccessView(), viewModel);
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
