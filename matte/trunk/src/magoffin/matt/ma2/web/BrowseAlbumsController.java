/* ===================================================================
 * BrowseAlbumsController.java
 * 
 * Created Dec 9, 2006 12:12:35 PM
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

package magoffin.matt.ma2.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.ObjectNotFoundException;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.plugin.BrowseModePlugin;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for browsing albums.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>userBiz</dt>
 *   <dd>The {@link UserBiz} implemntation to use.</dd>
 *   
 *   <dt>searchBiz</dt>
 *   <dd>The {@link SearchBiz} implemntation to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class BrowseAlbumsController extends AbstractCommandController {

	private UserBiz userBiz;
	private SearchBiz searchBiz;
	
	@Override
	protected Object getCommand(HttpServletRequest request) throws Exception {
		return new BrowseAlbumsCommand();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		
		BizContext context = getWebHelper().getBizContextWithViewSettings(request);
		
		BrowseAlbumsCommand cmd = (BrowseAlbumsCommand)command;

		User browseUser = getUserBiz().getUserByAnonymousKey(cmd.getUserKey());
		if ( browseUser == null ) {
			throw new ObjectNotFoundException("User [" +cmd.getUserKey() 
					+"] not available");
		}
		Model model = getDomainObjectFactory().newModelInstance();
		model.getUser().add(browseUser);

		Theme theme = browseUser.getBrowseTheme();
		if ( theme == null ) {
			theme = getSystemBiz().getDefaultTheme();
		}
		model.getTheme().add(theme);
		
		getWebHelper().populateMediaSizeAndQuality(model.getMediaSize());

		// save the request theme
		getWebHelper().saveRequestTheme(theme);
		
		// set command Locale
		cmd.setLocale(request.getLocale());
		PaginationCriteria pagination = null;
		if ( StringUtils.hasText(cmd.getSection()) ) {
			pagination = getDomainObjectFactory().newPaginationCriteriaInstance();
			pagination.setIndexKey(cmd.getSection());
		}
		SearchResults results = getSearchBiz().findAlbumsForBrowsing(
				cmd, pagination, context);
		model.setSearchResults(results);
		
		// populate available browse modes
		List<BrowseModePlugin> browseModes = getSystemBiz().getPluginsOfType(
				BrowseModePlugin.class);
		for ( BrowseModePlugin plugin : browseModes ) {
			for ( String mode : plugin.getSupportedModes() ) {
				Metadata meta = getDomainObjectFactory().newMetadataInstance();
				meta.setKey("browse-mode");
				meta.setValue(mode);
				model.getUiMetadata().add(meta);
			}
		}
		
		Map<String,Object> viewModel = errors.getModel();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return new ModelAndView(
				getSuccessView()+theme.getBasePath()+"/browse",
				viewModel);
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
