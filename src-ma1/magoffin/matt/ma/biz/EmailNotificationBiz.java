/* ===================================================================
 * EmailNotificationBiz.java
 * 
 * Created Apr 21, 2004 7:32:01 PM
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
 * $Id: EmailNotificationBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import java.net.URL;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.User;

/**
 * Biz interface for email notification actions.
 * 
 * <p>This biz provides the ability to "watch" for updates to 
 * either a user's shared collection or a specific album. It 
 * also allows for stopping from watchin on these items. When 
 * an event occurs where a user should be notified, the application 
 * will send that user an email notifying them of the update.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface EmailNotificationBiz extends Biz
{
	/** The message key for when watching an album is enabled. */
	public static final String MSG_WATCH_ALBUM_ENABLED = "watch.album.on";
	
	/** The message key for when watchin an album is disabled. */
	public static final String MSG_WATCH_ALBUM_DISABLED = "watch.album.off";
	
	/** The message key for when watching a user is enabled. */
	public static final String MSG_WATCH_USER_ENABLED = "watch.user.on";
	
	/** The message key for when watchin a user is disabled. */
	public static final String MSG_WATCH_USER_DISABLED = "watch.user.off";
	
/**
 * Watch for new media items available from another user.
 * @param userId the ID of the user to watch for new items from
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void watchForNewItems(Integer userId, User actingUser) 
throws MediaAlbumException;

/**
 * Stop watching for new media items from another user.
 * @param userId the ID of the user to stop watching for new items from
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void stopWatchingForNewItems(Integer userId, User actingUser)
throws MediaAlbumException;

/**
 * Watch for new media itema available from an album.
 * @param albumId the album ID to watch
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void watchForNewItemsInAlbum(Integer albumId, User actingUser)
throws MediaAlbumException;

/**
 * Stop watching for new media items from an album.
 * @param albumId the album ID to stop watching
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void stopWatchingForNewItemsInAlbum(Integer albumId, User actingUser)
throws MediaAlbumException;

/**
 * Process a set of updated albums.
 * 
 * <p>This method will send email notifications to the appropriate 
 * people for the set of albums passed here. If <var>message</var>
 * is provided it will be added to the email body, otherwise a generic
 * email message will be used.</p>
 * 
 * @param albums the albums that have been updated
 * @param message the custom email message (optional)
 * @param viewAlbumUrl the URL to use as the base link URL for the email message
 * @param browseUserUrl the URL to use as the base link URL for browsing
 */
public void processUpdatedAlbumNotifications(Album[] albums, String message, 
		URL viewAlbumUrl, URL browseUserUrl)
throws MediaAlbumException;
}
