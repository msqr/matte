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

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.web.util.WebHelper;
import magoffin.matt.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
public class AlbumController {

	@Autowired
	private MediaBiz mediaBiz;

	@Autowired
	private SearchBiz searchBiz;

	@Autowired
	private UserBiz userBiz;

	@Autowired
	private WebHelper webHelper;

	@Autowired
	private DomainObjectFactory domainObjectFactory;

	@Autowired
	private SystemBiz systemBiz;

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

	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	public void setSearchBiz(SearchBiz searchBiz) {
		this.searchBiz = searchBiz;
	}

	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	public WebHelper getWebHelper() {
		return webHelper;
	}

	public void setWebHelper(WebHelper webHelper) {
		this.webHelper = webHelper;
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

	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	public SystemBiz getSystemBiz() {
		return systemBiz;
	}

	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}

}
