/* ===================================================================
 * MediaInfoController.java
 * 
 * Created Feb 4, 2015 3:59:48 PM
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BasicMediaItemSearchCriteria;
import magoffin.matt.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Web API controller for media items.
 *
 * @author matt
 * @version 1.0
 */
@Controller
@RequestMapping("/v1/media")
public class MediaItemController extends ControllerSupport {

	/** The default value for the <code>searchDateFormat</code> property. */
	public static final String DEFAULT_SEARCH_DATE_FORMAT = "yyyy-MM-dd";

	@Autowired
	private MediaBiz mediaBiz;

	@Autowired
	private SearchBiz searchBiz;

	@Autowired
	private UserBiz userBiz;

	/**
	 * Get detailed information on one or more items.
	 * 
	 * @param request
	 *        The current request.
	 * @param cmd
	 *        The command options. The {@code itemIds} property must be
	 *        specified.
	 * @return The resulting MediaItem list, with full details included.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	@ResponseBody
	public Response<List<MediaItem>> getItemInfo(HttpServletRequest request, MediaCommand cmd) {
		return getItemInfos(request, cmd.getItemIds());
	}

	/**
	 * Get detailed information on one or more items.
	 * 
	 * @param request
	 *        The current request.
	 * @param itemIds
	 *        The IDs of the items to get.
	 * @return The resulting MediaItem list, with full details included.
	 */
	@RequestMapping(value = "/{itemIds}", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<MediaItem>> getItemInfos(HttpServletRequest request,
			@PathVariable("itemIds") Long[] itemIds) {
		BizContext context = getWebHelper().getBizContext(request, false);
		List<MediaItem> results;
		if ( itemIds != null && itemIds.length > 0 ) {
			results = new ArrayList<MediaItem>(itemIds.length);
			for ( Long itemId : itemIds ) {
				MediaItem item = mediaBiz.getMediaItemWithInfo(itemId, context);
				if ( item != null ) {
					results.add(item);
				}
			}
		} else {
			results = Collections.emptyList();
		}
		return Response.response(results);
	}

	/**
	 * Perform a simple search for media items.
	 * 
	 * @param request
	 *        The current request.
	 * @param userKey
	 *        The anonymous key of the owner of the items to search for.
	 * @param query
	 *        The search query.
	 * @return The search results.
	 */
	@RequestMapping(value = "/search/{userKey}", method = RequestMethod.GET, params = "!userKey")
	@ResponseBody
	public Response<SearchResults> findItems(HttpServletRequest request,
			@PathVariable("userKey") String userKey, @RequestParam("query") String query) {
		MediaSearchCommand cmd = new MediaSearchCommand();
		cmd.setUserKey(userKey);
		cmd.setQuery(query);
		return findItems(request, cmd);
	}

	/**
	 * Perform a search for media items.
	 * 
	 * @param request
	 *        The current request.
	 * @param cmd
	 *        The search criteria. The {@code userKey} property must be
	 *        provided.
	 * @return The search results.
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET, params = "userKey")
	@ResponseBody
	public Response<SearchResults> findItems(HttpServletRequest request, MediaSearchCommand cmd) {
		if ( cmd == null ) {
			throw new IllegalArgumentException("Search criteria must be provided.");
		}
		if ( cmd.getUserKey() == null ) {
			throw new IllegalArgumentException("The userKey parameter is required.");
		}

		BizContext context = getWebHelper().getBizContext(request, false);
		BasicMediaItemSearchCriteria criteria = new BasicMediaItemSearchCriteria();
		criteria.setUserAnonymousKey(cmd.getUserKey());
		criteria.setQuickSearch(cmd.getQuery());

		if ( cmd.getStartDate() != null || cmd.getEndDate() != null ) {
			TimeZone zone = null;
			User user = userBiz.getUserByAnonymousKey(cmd.getUserKey());
			if ( user != null && user.getTz() != null ) {
				zone = TimeZone.getTimeZone(user.getTz().getCode());
			} else {
				zone = TimeZone.getTimeZone("UTC");
			}

			if ( cmd.getStartDate() != null ) {
				Calendar cal = Calendar.getInstance(zone);
				Date date = cmd.getStartDate().toDate(zone);
				cal.setTime(date);
				criteria.setStartDate(cal);
			}
			if ( cmd.getEndDate() != null ) {
				Calendar cal = Calendar.getInstance(zone);
				Date date = cmd.getEndDate().toDate(zone);
				cal.setTime(date);
				criteria.setEndDate(cal);
			}
		}
		SearchResults results = searchBiz.findMediaItems(criteria, null, context);
		return Response.response(results);
	}

	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	public void setSearchBiz(SearchBiz searchBiz) {
		this.searchBiz = searchBiz;
	}

	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

}
