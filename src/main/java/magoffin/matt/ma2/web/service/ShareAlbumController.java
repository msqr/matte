/* ===================================================================
 * ShareAlbumController.java
 * 
 * Created Jun 18, 2006 8:14:13 PM
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

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.support.ShareAlbumCommand;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Share (and un-share) an album.
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public class ShareAlbumController extends AbstractCommandController {
	
	private MediaBiz mediaBiz;

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		ShareAlbumCommand cmd = (ShareAlbumCommand)command;
		BizContext context = getWebHelper().getBizContext(request,true);
		MessageSourceResolvable msg = null;
		if ( cmd.isShared() ) {
			mediaBiz.shareAlbum(cmd, context);
			Album sharedAlbum = mediaBiz.getAlbum(cmd.getAlbumId(), context);
			msg = new DefaultMessageSourceResolvable(
				new String[] {"share.album.shared"}, 
				new Object[]{sharedAlbum.getName(),
						getSystemBiz().getSharedAlbumUrl(sharedAlbum, context)},
				"The album is now shared.");
		} else {
			mediaBiz.unShareAlbum(cmd.getAlbumId(), context);
			Album unSharedAlbum = mediaBiz.getAlbum(cmd.getAlbumId(), context);
			msg = new DefaultMessageSourceResolvable(
					new String[] {"share.album.unshared"}, 
					new Object[]{unSharedAlbum.getName()},
					"The album is no longer shared.");
		}

		// add the album into the response so can update display
		Model model = getDomainObjectFactory().newModelInstance();
		model.getAlbum().add(mediaBiz.getAlbum(cmd.getAlbumId(), context));
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,model);
		return new ModelAndView(getSuccessView(),viewModel);
	}

	/**
	 * @return Returns the mediaBiz.
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz The mediaBiz to set.
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}
	
}
