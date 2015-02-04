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
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.web.util.WebHelper;
import magoffin.matt.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
	 * @return The album.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET, params = "!mode")
	@ResponseBody
	public Response<Album> viewAlbum(HttpServletRequest request, @RequestParam("key") String key) {
		BizContext context = getWebHelper().getBizContextWithViewSettings(request);
		Album album = mediaBiz.getSharedAlbum(key, context);
		return Response.response(album);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET, params = "mode")
	@ResponseBody
	public Response<AlbumModel> viewAlbum(HttpServletRequest request, AlbumCommand cmd) {
		BizContext context = getWebHelper().getBizContextWithViewSettings(request);
		Model model = getDomainObjectFactory().newModelInstance();

		// get the album
		Album album = null;
		if ( StringUtils.hasText(cmd.getUserKey()) && StringUtils.hasText(cmd.getMode())
				&& !BrowseAlbumsCommand.MODE_ALBUMS.equals(cmd.getMode()) ) {
			BrowseAlbumsCommand baCmd = new BrowseAlbumsCommand();
			baCmd.setMode(cmd.getMode());
			baCmd.setLocale(request.getLocale());
			baCmd.setUserKey(cmd.getUserKey());
			PaginationCriteria pc = getDomainObjectFactory().newPaginationCriteriaInstance();
			pc.setIndexKey(cmd.getKey());
			SearchResults sr = searchBiz.findAlbumsForBrowsing(baCmd, pc, context);
			List<AlbumSearchResult> searchAlbums = sr.getAlbum();
			if ( searchAlbums.size() > 0 ) {
				album = searchAlbums.get(0);
				if ( album.getTheme() == null ) {
					User u = userBiz.getUserByAnonymousKey(cmd.getUserKey());
					if ( u != null ) {
						album.setTheme(u.getBrowseTheme());
					}
				}
			}
		} else {
			album = mediaBiz.getSharedAlbum(cmd.getKey(), context);
		}
		model.getAlbum().add(album);

		Album displayAlbum = album;
		if ( cmd.getChildKey() != null ) {
			// see if child album available
			Album selectedAlbum = getAlbum(album, cmd.getChildKey(), context);
			if ( selectedAlbum != null ) {
				displayAlbum = selectedAlbum;
			}
		}

		Theme theme = album != null ? album.getTheme() : null;
		if ( cmd.getThemeId() != null ) {
			Theme customTheme = getSystemBiz().getThemeById(cmd.getThemeId());
			if ( customTheme != null ) {
				model.getTheme().add(customTheme);
				theme = customTheme;
			}
		}

		// if album does not have theme and none requested, provide default in model
		if ( theme == null ) {
			theme = getSystemBiz().getDefaultTheme();
			model.getTheme().add(theme);
		}

		// save the request theme
		getWebHelper().saveRequestTheme(theme);

		// add the media sizes
		getWebHelper().populateMediaSizeAndQuality(model.getMediaSize());

		AlbumModel viewModel = new AlbumModel();
		viewModel.setModel(model);
		if ( displayAlbum != null ) {
			viewModel.setDisplayAlbumKey(displayAlbum.getAnonymousKey());
		}
		viewModel.setDisplayItemId(cmd.getItemId());
		return Response.response(viewModel);
	}

	@SuppressWarnings("unchecked")
	private Album getAlbum(Album album, String childKey, BizContext context) {
		if ( album == null ) {
			return null;
		}
		if ( childKey.equals(album.getAnonymousKey()) ) {
			return album;
		}
		if ( album.getAlbum() != null ) {
			for ( int i = 0; i < album.getAlbum().size(); i++ ) {
				Album a = (Album) album.getAlbum().get(i);
				Album foundAlbum = getAlbum(a, childKey, context);
				if ( foundAlbum != null && a.getAlbumId().equals(foundAlbum.getAlbumId()) ) {
					// get full album details and replace child
					foundAlbum = getMediaBiz().getSharedAlbum(foundAlbum.getAnonymousKey(), context);
					album.getAlbum().set(i, foundAlbum);
				}
				if ( foundAlbum != null ) {
					return foundAlbum;
				}
			}
		}
		return null;
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
