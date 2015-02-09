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
 */

package magoffin.matt.ma2.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletContext;
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
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for browsing albums.
 * 
 * <p>
 * This serves as a web-based interface to the
 * {@link SearchBiz#findAlbumsForBrowsing(BrowseAlbumsCommand, PaginationCriteria, BizContext)}
 * method.
 * </p>
 * 
 * <p>
 * This class looks for registered {@link BrowseModePlugin} instances via
 * {@link magoffin.matt.ma2.biz.SystemBiz#getPluginsOfType(Class)}, and for each
 * plugin returned will create a {@link Metadata} instance for each plugin's
 * {@link BrowseModePlugin#getSupportedModes()} values, using a key of
 * {@code browse-mode}. This allows the view to display a list of options to the
 * user for browsing shared items in any available browse mode.
 * </p>
 * 
 * <p>
 * If the {@link BrowseAlbumsCommand#getSection()} value is provided on the
 * request, this value will be set as the
 * {@link PaginationCriteria#setIndexKey(String)} value passed to the
 * {@link SearchBiz#findAlbumsForBrowsing(BrowseAlbumsCommand, PaginationCriteria, BizContext)}
 * method.
 * </p>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>userBiz</dt>
 * <dd>The {@link UserBiz} implemntation to use.</dd>
 * 
 * <dt>searchBiz</dt>
 * <dd>The {@link SearchBiz} implemntation to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class BrowseAlbumsController extends AbstractCommandController {

	public static final String THEME_PROP_ALBUMS_USE_DATE_RANGE = "theme.browseMode.albums.useDateRange";

	private UserBiz userBiz;
	private SearchBiz searchBiz;

	private final Map<Long, Properties> themePropCache = new HashMap<Long, Properties>();

	@Override
	protected Object getCommand(HttpServletRequest request) throws Exception {
		return new BrowseAlbumsCommand();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {

		BizContext context = getWebHelper().getBizContextWithViewSettings(request);

		BrowseAlbumsCommand cmd = (BrowseAlbumsCommand) command;

		User browseUser = getUserBiz().getUserByAnonymousKey(cmd.getUserKey());
		if ( browseUser == null ) {
			throw new ObjectNotFoundException("User [" + cmd.getUserKey() + "] not available");
		}
		Model model = getDomainObjectFactory().newModelInstance();
		model.getUser().add(browseUser);

		Theme theme = browseUser.getBrowseTheme();
		if ( theme == null ) {
			theme = getSystemBiz().getDefaultTheme();
		}

		// check theme properties for mode flags
		Properties themeProps = themePropCache.get(theme.getThemeId());
		if ( themeProps == null ) {
			Properties p = new Properties();
			InputStream in = null;
			try {
				// try "core" theme first
				ServletContext servletContext = request.getSession().getServletContext();
				in = servletContext.getResourceAsStream("/WEB-INF/themes" + theme.getBasePath()
						+ "/theme.properties");
				if ( in == null ) {
					// try external theme
					Resource r = getSystemBiz().getThemeResource(theme, "theme.properties", context);
					in = r.getInputStream();
				}
				if ( in != null ) {
					p.load(in);
				}
			} catch ( IOException e ) {
				logger.warn("Error reading theme " + theme.getThemeId() + " properties file: "
						+ e.getMessage());
			} finally {
				if ( in != null ) {
					try {
						in.close();
					} catch ( IOException e ) {
						// ignore
					}
				}
			}
			// don't worry about threading here, we might simply read the file more than once but the data should be identical
			themePropCache.put(theme.getThemeId(), p);
			themeProps = p;
		}

		// see if want to use date range for albums mode
		if ( BrowseAlbumsCommand.MODE_ALBUMS.equals(cmd.getMode()) ) {
			String useDateRangeValue = themeProps.getProperty(THEME_PROP_ALBUMS_USE_DATE_RANGE, "false");
			boolean useDateRange = false;
			if ( "true".equalsIgnoreCase(useDateRangeValue) || "yes".equalsIgnoreCase(useDateRangeValue)
					|| "1".equals(useDateRangeValue) ) {
				useDateRange = true;
			}
			if ( useDateRange == true && cmd.getSection() == null ) {
				// use "LATEST" section
				cmd.setSection(BrowseAlbumsCommand.SECTION_LATEST);
			}
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
		SearchResults results = getSearchBiz().findAlbumsForBrowsing(cmd, pagination, context);
		model.setSearchResults(results);

		// populate available browse modes
		List<BrowseModePlugin> browseModes = getSystemBiz().getPluginsOfType(BrowseModePlugin.class);
		for ( BrowseModePlugin plugin : browseModes ) {
			for ( String mode : plugin.getSupportedModes() ) {
				Metadata meta = getDomainObjectFactory().newMetadataInstance();
				meta.setKey("browse-mode");
				meta.setValue(mode);
				model.getUiMetadata().add(meta);
			}
		}

		Map<String, Object> viewModel = errors.getModel();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return new ModelAndView(getSuccessView() + theme.getBasePath() + "/browse", viewModel);
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

}
