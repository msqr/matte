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
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.web.util.WebHelper;
import magoffin.matt.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Web API controller for media items.
 *
 * @author matt
 * @version 1.0
 */
@Controller
@RequestMapping("/v1/media")
public class MediaInfoController {

	@Autowired
	private MediaBiz mediaBiz = null;

	@Autowired
	private WebHelper webHelper;

	@Autowired
	private DomainObjectFactory domainObjectFactory;

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
		BizContext context = getWebHelper().getBizContext(request, false);
		List<MediaItem> results;
		if ( cmd.getItemIds() != null && cmd.getItemIds().length > 0 ) {
			results = new ArrayList<MediaItem>(cmd.getItemIds().length);
			for ( Long itemId : cmd.getItemIds() ) {
				MediaItem item = mediaBiz.getMediaItemWithInfo(itemId, context);
				results.add(item);
			}
		} else {
			results = Collections.emptyList();
		}
		return Response.response(results);
	}

	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	public WebHelper getWebHelper() {
		return webHelper;
	}

	public void setWebHelper(WebHelper webHelper) {
		this.webHelper = webHelper;
	}

	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

}
