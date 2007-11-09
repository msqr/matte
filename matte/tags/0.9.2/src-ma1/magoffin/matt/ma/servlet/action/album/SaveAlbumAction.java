/* ===================================================================
 * SaveAlbumAction.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: SaveAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.EmailNotificationBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.EditAlbumForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.util.ComparatorUtil;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumMedia;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.ArrayUtil;
import magoffin.matt.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.RequestUtils;

/**
 * Save an album to the back-end.
 * 
 * <p>Created Oct 28, 2002 8:19:09 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class SaveAlbumAction extends AbstractAction 
{
	private static Logger LOG = Logger.getLogger(SaveAlbumAction.class);
	
	private static final Comparator SORT_ITEMS_BY_ID = 
		new ComparatorUtil.MediaItemItemIdSort();
	
	private static final Comparator SORT_ALBUM_MEDIA_BY_MEDIA_ID = 
		new ComparatorUtil.AlbumMediaMediaIdSort();

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, 
	ActionResult result)
throws Exception 
{
	UserSessionData usd = this.getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	EditAlbumForm dForm = (EditAlbumForm)form;
	Album eAlbum = dForm.getA();
	
	String action = dForm.getCmd();
	
	if ( action != null && action.equals("cancel") ) {
		redirectToAlbum(eAlbum.getAlbumId(),request,response,
				mapping.findForward(StrutsConstants.DEFAULT_CANCEL_FORWARD));
		return;
	}
	
	
	if ( eAlbum.getAlbumId() == null ) {
		notFound(mapping,request,response);
		return;
	}
	
	// compare edited album with album from database to see if any changes made
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	Album album = albumBiz.getAlbumById(eAlbum.getAlbumId(),usd.getUser(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
	MediaItem[] items = (MediaItem[])ArrayUtil.prune(dForm.getItem());
	AlbumMedia[] members = new AlbumMedia[items.length];
	List changedItems = new ArrayList(items.length);
	
	for ( int i = 0; i < items.length; i++ ) {
		AlbumMedia albumItem = new AlbumMedia();
		albumItem.setAlbumId(eAlbum.getAlbumId());
		albumItem.setMediaId(items[i].getItemId());
		albumItem.setDisplayOrder(items[i].getDisplayOrder());
		members[i] = albumItem;
		MediaItem currItem = itemBiz.getMediaItemById(items[i].getItemId(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		try {
			currItem = (MediaItem)BeanUtils.cloneBean(currItem);
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data",e);
		}
		if ( MediaUtil.copyChanges(items[i],currItem, itemBiz) ) {
			changedItems.add(currItem);
		}
	}
	
	// get current items to see if
	MediaItem[] currItems = albumBiz.getMediaItemsForAlbum(album.getAlbumId(),
			ApplicationConstants.POPULATE_MODE_NONE,
			ApplicationConstants.CACHED_OBJECT_ALLOWED, usd.getUser());
	if ( currItems == null ) {
		currItems = new MediaItem[0];
	}
	
	// save album changes
	try {
		album = (Album)BeanUtils.cloneBean(album);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data",e);
	}
	boolean albumChanged = copyChanges(eAlbum,album);
	if ( albumChanged ) {
		albumBiz.updateAlbum(album,usd.getUser());
	}
	
	// save members
	albumBiz.setAlbumMediaItems(album.getAlbumId(),members,usd.getUser());
	
	// save item changes
	if ( changedItems.size() > 0 ) {
		MediaItem[] changed = (MediaItem[])changedItems.toArray(
				new MediaItem[changedItems.size()]);
		itemBiz.updateMediaItems(changed,usd.getUser());
	}
	
	if ( action != null && action.equals("change-source") ) {
		result.setForward(mapping.findForward("change-source"));
	}
	
	boolean notify = false;
	if ( currItems.length > members.length ) {
		// don't notify when items removed
	} else if ( currItems.length != members.length ) {
		notify = true;
	} else {
		// check for chagned album contents
		Arrays.sort(currItems,SORT_ITEMS_BY_ID);
		Arrays.sort(members,SORT_ALBUM_MEDIA_BY_MEDIA_ID);
		for ( int i = 0; i < members.length; i++ ) {
			if ( !members[i].getMediaId().equals(currItems[i].getItemId()) ) {
				notify = true;
				break;
			}
		}
	}
	if ( notify ) {
		EmailNotificationBiz alertBiz = (EmailNotificationBiz)getBiz(
				BizConstants.EMAIL_NOTIFICATIONS_BIZ);
		// TODO support different forward with form for message capture
		URL viewAlbumUrl = RequestUtils.absoluteURL(request,
				mapping.findForward(StrutsConstants.ALBUM_SLIDESHOW_FORWARD).getPath());
		URL browseUserUrl = RequestUtils.absoluteURL(request,
				mapping.findForward(StrutsConstants.BROWSE_ALBUMS_FORWARD).getPath());
		alertBiz.processUpdatedAlbumNotifications(new Album[] {album},null,
				viewAlbumUrl,browseUserUrl);
	}
	
	redirectToAlbum(album.getAlbumId(),request,response,
			mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

private boolean copyChanges(Album eAlbum, Album album) 
{
	boolean albumChanged = false;
	
	eAlbum.setName(StringUtil.trimToNull(eAlbum.getName()));
	if ( (eAlbum.getName() == null && eAlbum.getName() != null) ||
		(eAlbum.getName() != null && !eAlbum.getName().equals(album.getName())) ) {
		albumChanged = true;
		album.setName(eAlbum.getName());
	}
	
	eAlbum.setComment(StringUtil.trimToNull(eAlbum.getComment()));
	if ( (eAlbum.getComment() == null && eAlbum.getComment() != null) ||
		(eAlbum.getComment() != null && !eAlbum.getComment().equals(album.getComment())) ) {
		albumChanged = true;
		album.setComment(eAlbum.getComment());
	}
	
	if ( eAlbum.getThemeId() != null && eAlbum.getThemeId().intValue() == 0 ) {
		eAlbum.setThemeId(null);
	}
	if ( eAlbum.getThemeId() == null ) {
		if ( album.getThemeId() != null ) {
			albumChanged = true;
			album.setThemeId(null);
		}
	} else {
		Integer themeId = eAlbum.getThemeId();
		if ( !themeId.equals(album.getThemeId()) ) {
			albumChanged = true;
			album.setThemeId(themeId);
		}
	}
	
	if ( eAlbum.getParentId() != null && eAlbum.getParentId().intValue() == 0 ) {
		eAlbum.setParentId(null);
	}
	if ( eAlbum.getParentId() == null ) {
		if ( album.getParentId() != null ) {
			albumChanged = true;
			album.setParentId(null);
		}
	} else {
		Integer parentId = eAlbum.getParentId();
		if ( !parentId.equals(album.getParentId()) ) {
			albumChanged = true;
			album.setParentId(parentId);
		}
	}
	
	if ( eAlbum.getPosterId() == null ) { // 0 ok
		if ( album.getPosterId() != null ) {
			albumChanged = true;
			album.setPosterId(null);
		}
	} else {
		Integer posterId = eAlbum.getPosterId();
		if ( !posterId.equals(album.getPosterId()) ) {
			albumChanged = true;
			album.setPosterId(posterId);
		}
	}
	
	if ( eAlbum.getSortMode() == null ) { // ok to be 0
		if ( album.getSortMode() != null ) {
			albumChanged = true;
			album.setSortMode(null);
		}
	} else {
		Integer sortMode = eAlbum.getSortMode();
		if ( !sortMode.equals(album.getSortMode()) ) {
			albumChanged = true;
			album.setSortMode(sortMode);
		}
	}
	
	if ( eAlbum.getAlbumDate() == null ) {
		if ( album.getAlbumDate() != null ) {
			albumChanged = true;
			album.setAlbumDate(null);
		}
	} else {
		Date albumDate = eAlbum.getAlbumDate();
		if ( !albumDate.equals(album.getAlbumDate()) ) {
			albumChanged = true;
			album.setAlbumDate(albumDate);
		}
	}
	return albumChanged;
}

}
