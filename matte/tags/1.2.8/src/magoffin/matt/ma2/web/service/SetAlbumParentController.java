/* ===================================================================
 * SetAlbumParentController.java
 * 
 * Created Apr 9, 2007 6:05:29 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

/**
 * Controller for setting the parent of an album (or adding one album 
 * as a child to another album).
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class SetAlbumParentController extends AbstractCommandController {

	private MediaBiz mediaBiz;
	
	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);
		Command cmd = (Command)command;
		mediaBiz.storeAlbumParent(cmd.getAlbumId(), cmd.getParentAlbumId(), context);
		
		Map<String,Object> model = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = null;
		if ( cmd.getParentAlbumId() != null ) {
			Album parent = mediaBiz.getAlbum(cmd.getParentAlbumId(), context);
			Album child = null;
			for ( Album oneChild : (List<Album>)parent.getAlbum() ) {
				if ( cmd.getAlbumId().equals(oneChild.getAlbumId()) ) {
					child = oneChild;
					break;
				}
			}
			
			msg = new DefaultMessageSourceResolvable(
					new String[] {"album.parent.set"}, 
					new Object[] {parent.getName(), 
							child != null ? child.getName() : ""},
					"The album has been moved.");
		} else {
			Album child = mediaBiz.getAlbum(cmd.getAlbumId(), context);
			msg = new DefaultMessageSourceResolvable(
					new String[] {"album.parent.removed"}, 
					new Object[] {child.getName(), child.getName()},
					"The album has been moved.");
		}
		
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		return new ModelAndView(getSuccessView(),model);
	}

	/**
	 * Command object.
	 */
	public static class Command {
		private Long albumId;
		private Long parentAlbumId;
		
		/**
		 * @return the albumId
		 */
		public Long getAlbumId() {
			return albumId;
		}
		
		/**
		 * @param albumId the albumId to set
		 */
		public void setAlbumId(Long albumId) {
			this.albumId = albumId;
		}
		
		/**
		 * @return the parentAlbumId
		 */
		public Long getParentAlbumId() {
			return parentAlbumId;
		}
		
		/**
		 * @param parentAlbumId the parentAlbumId to set
		 */
		public void setParentAlbumId(Long parentAlbumId) {
			this.parentAlbumId = parentAlbumId;
		}
		
	}
	
	/**
	 * @return the mediaBiz
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

}
