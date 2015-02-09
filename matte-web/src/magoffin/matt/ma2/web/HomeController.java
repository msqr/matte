/* ===================================================================
 * HomeController.java
 * 
 * Created Aug 3, 2004 4:10:16 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.xweb.XData;
import magoffin.matt.xweb.util.XDataPostProcessor;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Home controller.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class HomeController extends AbstractCommandController {

	private UserBiz userBiz = null;
	private MediaBiz mediaBiz = null;
	private AlbumDao albumDao = null;
	
	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request,true);
		Model model = getDomainObjectFactory().newModelInstance();
		
		prepareModelForView(model,cmd,context);
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,model);
		return new ModelAndView(getSuccessView(),viewModel);
	}
	
	@SuppressWarnings("unchecked")
	private void prepareModelForView(Model model, Command cmd, BizContext context) {
		User actingUser = context.getActingUser();
		if ( model.getCollection().size() < 1 ) {
			
			// 1: add user's collections to model
			List<Collection> collections = userBiz.getCollectionsForUser(
					actingUser,context);
			model.getCollection().addAll(collections);
			
			// 2: if collection is specified on request, add media items for that collection
			if ( cmd.getCollectionId() != null ) {
				// only add collection items if user currently owns collection
				for ( Collection c : collections ) {
					if ( cmd.getCollectionId().equals(c.getCollectionId()) ) {
						model.getItem().addAll(
								mediaBiz.getMediaItemsForCollection(c, context));
						break;
					}
				}
			}
		}
		if ( model.getAlbum().size() < 1 ) {
			// 3: add user's albums to model
			List<Album> albums = userBiz.getAlbumsForUser(
					actingUser,context);
			model.getAlbum().addAll(albums);
			
			// 4: if album is specified on request, add media items for that album
			if ( cmd.getAlbumId() != null ) {
				
				// only add album items if user currently owns album
				Album a = albumDao.get(cmd.getAlbumId());
				if ( a != null && a.getOwner().getUserId().equals(actingUser.getUserId()) ) {
					model.getItem().addAll(mediaBiz.getMediaItemsForAlbum(a, context));
				}
			}
		}
		if ( model.getTimeZone().size() < 1 ) {	
			// 5: add TimeZone data
			model.getTimeZone().addAll(getSystemBiz().getAvailableTimeZones());
		}
		if ( model.getTheme().size() < 1 ) {
			// 6: get all themes
			model.getTheme().addAll(getSystemBiz().getAvailableThemes());
		}
		
		// 7: add media sizes
		getWebHelper().populateMediaSizeAndQuality(model.getMediaSize());
	}
	
	/**
	 * Command class.
	 */
	public static class Command {
		private Long collectionId;
		private Long albumId;
		
		/**
		 * @return Returns the albumId.
		 */
		public Long getAlbumId() {
			return albumId;
		}
		
		/**
		 * @param albumId The albumId to set.
		 */
		public void setAlbumId(Long albumId) {
			this.albumId = albumId;
		}
		
		/**
		 * @return Returns the collectionId.
		 */
		public Long getCollectionId() {
			return collectionId;
		}
		
		/**
		 * @param collectionId The collectionId to set.
		 */
		public void setCollectionId(Long collectionId) {
			this.collectionId = collectionId;
		}
		
	}
	
	/**
	 * A XDataPostProcessor implementation that ensures the home view
	 * has the user's data (like collections, albums, etc) populated.
	 * 
	 * @author matt.magoffin
	 * @version $Revision$ $Date$
	 */
	public static class PostProcessor implements XDataPostProcessor {
		
		private final Logger log = Logger.getLogger(PostProcessor.class);
		
		private HomeController myController;
		
		/**
		 * Construct.
		 * @param controller my reference
		 */
		public PostProcessor(HomeController controller) {
			this.myController = controller;
		}
		
		public boolean supportsView(String viewName) {
			return myController.getSuccessView().equalsIgnoreCase(viewName);
		}

		public void process(XData xData, HttpServletRequest request) {
			BizContext context = myController.getWebHelper().getBizContext(request,true);
			
			Object o = xData.getXModel().getAny();
			Model model = null;
			if ( o instanceof Model ) {
				model = (Model)o;
			} else {
				if ( log.isDebugEnabled() ) {
					log.debug("Replacing XModel Any [" +o +"] with Model instance");
				}
				model = myController.getDomainObjectFactory().newModelInstance();
				xData.getXModel().setAny(model);
			}
			
			Command cmd = new Command();
			try {
				ServletRequestDataBinder binder = myController.createBinder(request, cmd);
				binder.bind(request);
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
			myController.prepareModelForView(model,cmd,context);
		}

	}

	/**
	 * @return Returns the userBiz.
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz The userBiz to set.
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
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
	
	/**
	 * @return Returns the albumDao.
	 */
	public AlbumDao getAlbumDao() {
		return albumDao;
	}
	
	/**
	 * @param albumDao The albumDao to set.
	 */
	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}
	
}
