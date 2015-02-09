/* ===================================================================
 * AlbumController.java
 * 
 * Created Feb 3, 2015 6:20:31 PM
 * 
 * Copyright (c) 2015 Matt Magoffin.
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

package magoffin.matt.ma2.web.api;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.plugin.BrowseModePlugin;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Web API controller for user albums.
 *
 * @author matt
 * @version 1.0
 */
@Controller
@RequestMapping("/v1/album")
public class AlbumController extends ControllerSupport {

	@Autowired
	private MediaBiz mediaBiz;

	@Autowired
	private SearchBiz searchBiz;

	@Autowired
	private SystemBiz systemBiz;

	@Autowired
	private UserBiz userBiz;

	/**
	 * Get full details on a single album.
	 * 
	 * @param request
	 *        The current request.
	 * @param key
	 *        The anonymous album key to get.
	 * @return The album result.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET, params = { "!mode", "!userKey" })
	@ResponseBody
	public Response<Album> viewAlbum(HttpServletRequest request, @RequestParam("key") String key) {
		BizContext context = getWebHelper().getBizContextWithViewSettings(request);
		Album album = mediaBiz.getSharedAlbum(key, context);
		return Response.response(album);
	}

	@RequestMapping(value = "/{key}", method = RequestMethod.GET, params = { "!mode", "!userKey" })
	@ResponseBody
	public Response<Album> viewAlbumViaPath(HttpServletRequest request, @PathVariable("key") String key) {
		return viewAlbum(request, key);
	}

	@RequestMapping(value = "/{key}/{userKey}/{mode}", method = RequestMethod.GET)
	@ResponseBody
	public Response<Album> viewVirtualAlbumViaPath(HttpServletRequest request,
			@PathVariable("userKey") String userKey, @PathVariable("key") String key,
			@PathVariable("mode") String mode) {
		return viewVirtualAlbum(request, key, userKey, mode);
	}

	/**
	 * Get full details on a single virtual album.
	 * 
	 * @param request
	 *        The current request.
	 * @param key
	 *        The anonymous virtual album key to get.
	 * @param userKey
	 *        The anonymous user key that owns the album.
	 * @param mode
	 *        The virtual album mode.
	 * @return The album result.
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET, params = "mode!="
			+ BrowseAlbumsCommand.MODE_ALBUMS)
	@ResponseBody
	public Response<Album> viewVirtualAlbum(HttpServletRequest request, @RequestParam("key") String key,
			@RequestParam("userKey") String userKey, @RequestParam("mode") String mode) {
		BizContext context = getWebHelper().getBizContextWithViewSettings(request);

		// get the album
		Album album = null;
		BrowseAlbumsCommand baCmd = new BrowseAlbumsCommand();
		baCmd.setMode(mode);
		baCmd.setLocale(request.getLocale());
		baCmd.setUserKey(userKey);
		PaginationCriteria pc = getDomainObjectFactory().newPaginationCriteriaInstance();
		pc.setIndexKey(key);
		SearchResults sr = searchBiz.findAlbumsForBrowsing(baCmd, pc, context);
		List<AlbumSearchResult> searchAlbums = sr.getAlbum();
		if ( searchAlbums.size() > 0 ) {
			album = searchAlbums.get(0);
			if ( album.getTheme() == null ) {
				User u = userBiz.getUserByAnonymousKey(userKey);
				if ( u != null ) {
					album.setTheme(u.getBrowseTheme());
				}
			}
		}
		return Response.response(album);
	}

	/**
	 * Browse all non-virtual albums for user.
	 * 
	 * @param request
	 *        The current request.
	 * @param userKey
	 *        The anonymous user key to browse albums for.
	 * @return The result albums.
	 */
	@RequestMapping(value = "/browse/{userKey}", method = RequestMethod.GET)
	@ResponseBody
	public Response<SearchResults> browse(HttpServletRequest request,
			@PathVariable("userKey") String userKey) {
		BrowseAlbumsCommand cmd = new BrowseAlbumsCommand();
		cmd.setUserKey(userKey);
		return browse(request, cmd);
	}

	/**
	 * Browse albums for a user.
	 * 
	 * @param request
	 *        The current request.
	 * @param cmd
	 *        The browse command. The {@code userKey} property is required.
	 * @return The result albums.
	 */
	@RequestMapping(value = "/browse", method = RequestMethod.GET)
	@ResponseBody
	public Response<SearchResults> browse(HttpServletRequest request, BrowseAlbumsCommand cmd) {
		BizContext context = getWebHelper().getBizContextWithViewSettings(request);

		// set command Locale
		cmd.setLocale(request.getLocale());
		PaginationCriteria pagination = null;
		if ( StringUtils.hasText(cmd.getSection()) ) {
			pagination = getDomainObjectFactory().newPaginationCriteriaInstance();
			pagination.setIndexKey(cmd.getSection());
		}
		SearchResults results = getSearchBiz().findAlbumsForBrowsing(cmd, pagination, context);
		return Response.response(results);
	}

	/**
	 * Get an array of available browse mode keys.
	 * 
	 * @return An array response of all available browse mode keys.
	 */
	@RequestMapping(value = "/browse/modes", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<String>> availableBrowseModes() {
		List<BrowseModePlugin> browseModes = getSystemBiz().getPluginsOfType(BrowseModePlugin.class);
		List<String> results = new ArrayList<String>(browseModes.size() * 2);
		for ( BrowseModePlugin plugin : browseModes ) {
			for ( String mode : plugin.getSupportedModes() ) {
				results.add(mode);
			}
		}
		return Response.response(results);
	}

	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	public void setSearchBiz(SearchBiz searchBiz) {
		this.searchBiz = searchBiz;
	}

	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	public SearchBiz getSearchBiz() {
		return searchBiz;
	}

	public UserBiz getUserBiz() {
		return userBiz;
	}

	public SystemBiz getSystemBiz() {
		return systemBiz;
	}

	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}

}
