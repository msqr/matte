/* ===================================================================
 * ViewAlbumController.java
 * 
 * Created May 21, 2006 4:07:46 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BasicAlbumSearchCriteria;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.web.util.WebConstants;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for viewing a shared album or a virtual album by way of a
 * {@link magoffin.matt.ma2.plugin.BrowseModePlugin}.
 * 
 * <p>
 * To view a normal shared album, pass the {@code key} property on the request
 * of the anonymous key of the album to view. To view a child album of that
 * shared album, also pass the {@code childKey} of that child album.
 * </p>
 * 
 * <p>
 * The default theme will be added to the view model, unless a {@code themeId}
 * parameter for some other theme is provided.
 * </p>
 * 
 * <p>
 * To view a <em>virtual album</em> from a browse mode (see the
 * {@link magoffin.matt.ma2.plugin.BrowseModePlugin} API) you must pass the
 * {@code userKey} of the owner of the shared albums and a {@code mode} for the
 * browse mode being viewed. The {@code key} parameter in this case will be set
 * as the {@link PaginationCriteria#setIndexKey(String)} passed to
 * {@link SearchBiz#findAlbumsForBrowsing(BrowseAlbumsCommand, PaginationCriteria, BizContext)}
 * and the first {@link AlbumSearchResult} returned in the results will be
 * passed to the view as the album to view.
 * </p>
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public class ViewAlbumController extends AbstractCommandController {

	/** The model key for the selected Album's anonymous key. */
	public static final String DISPLAY_ALBUM_KEY = "display.album.key";

	/** The model key for the selected MediaItem's ID. */
	public static final String DISPLAY_ITEM_ID_KEY = "display.item.id";

	private MediaBiz mediaBiz = null;
	private SearchBiz searchBiz = null;
	private UserBiz userBiz = null;

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getBizContextWithViewSettings(request);
		Command cmd = (Command) command;

		Model model = getDomainObjectFactory().newModelInstance();

		// get the album
		Album album = null;
		if ( StringUtils.hasText(cmd.userKey) && StringUtils.hasText(cmd.getMode())
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
			if ( album.getAlbum() != null && album.getAlbum().size() > 0 ) {
				// also perform a search, to get all nested item counts
				SearchResults sr = searchBiz.findAlbums(new BasicAlbumSearchCriteria(cmd.getKey()),
						null, context);
				model.setSearchResults(sr);
			}
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
				theme = customTheme;
			}
		}

		// if album does not have theme and none requested, provide default in model
		if ( theme == null ) {
			theme = getSystemBiz().getDefaultTheme();
		}

		// save the request theme
		model.getTheme().add(theme);
		getWebHelper().saveRequestTheme(theme);

		// add the media sizes
		getWebHelper().populateMediaSizeAndQuality(model.getMediaSize());

		Map<String, Object> viewModel = errors.getModel();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		if ( displayAlbum != null ) {
			viewModel.put(DISPLAY_ALBUM_KEY, displayAlbum.getAnonymousKey());
		}
		viewModel.put(DISPLAY_ITEM_ID_KEY, cmd.getItemId());
		return new ModelAndView(getSuccessView() + theme.getBasePath() + "/theme", viewModel);
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

	/**
	 * Command class.
	 */
	public static class Command {

		private String key = null;
		private Long themeId = null;
		private Long itemId = null;
		private String childKey = null; // for nested album selection
		private String userKey = null; // for virtual album selection
		private String mode = null; // for virtual album selection

		/**
		 * @return Returns the themeId.
		 */
		public Long getThemeId() {
			return themeId;
		}

		/**
		 * @param themeId
		 *        The themeId to set.
		 */
		public void setThemeId(Long themeId) {
			this.themeId = themeId;
		}

		/**
		 * @return Returns the key.
		 */
		public String getKey() {
			return key;
		}

		/**
		 * @param key
		 *        The key to set.
		 */
		public void setKey(String key) {
			this.key = key;
		}

		/**
		 * @return Returns the itemId.
		 */
		public Long getItemId() {
			return itemId;
		}

		/**
		 * @param itemId
		 *        The itemId to set.
		 */
		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}

		/**
		 * @return the childKey
		 */
		public String getChildKey() {
			return childKey;
		}

		/**
		 * @param childKey
		 *        the childKey to set
		 */
		public void setChildKey(String childKey) {
			this.childKey = childKey;
		}

		/**
		 * @return the userKey
		 */
		public String getUserKey() {
			return userKey;
		}

		/**
		 * @param userKey
		 *        the userKey to set
		 */
		public void setUserKey(String userKey) {
			this.userKey = userKey;
		}

		/**
		 * @return the mode
		 */
		public String getMode() {
			return mode;
		}

		/**
		 * @param mode
		 *        the mode to set
		 */
		public void setMode(String mode) {
			this.mode = mode;
		}

	}

	/**
	 * @return Returns the mediaBiz.
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz
	 *        The mediaBiz to set.
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	/**
	 * @return the searchBiz
	 */
	public SearchBiz getSearchBiz() {
		return searchBiz;
	}

	/**
	 * @param searchBiz
	 *        the searchBiz to set
	 */
	public void setSearchBiz(SearchBiz searchBiz) {
		this.searchBiz = searchBiz;
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
