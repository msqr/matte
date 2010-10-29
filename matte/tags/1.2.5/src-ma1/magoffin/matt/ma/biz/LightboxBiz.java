/* ===================================================================
 * LightboxBiz.java
 * 
 * Created Jun 14, 2004 5:44:14 PM
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
 * $Id: LightboxBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.User;

/**
 * Business interface for Lightbox functionality.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface LightboxBiz extends Biz 
{
	
/**
 * Get the lightbox for a user.
 * 
 * <p>The returned lightbox will only have the media item and album 
 * IDs loaded.</p>
 * 
 * @param user the User to get the lightbox for
 * @param allowCached if <em>true</em> then allow returning cached objects
 * @return the Lightbox
 * @throws MediaAlbumException if an error occurs
 */
public Lightbox getLightbox(User user, boolean allowCached) 
throws MediaAlbumException;

/**
 * Save a lightbox to the back end.
 * 
 * <p>If the lightbox does not have the <code>owner</code> field
 * set this method will assume the <var>actingUser</var> is the
 * owner.</p>
 * 
 * @param lightbox the Lightbox to save
 * @param actingUser the acting user
 * @return the Lightbox, with updated data
 * @throws MediaAlbumException if an error occurs
 */
public Lightbox saveLightbox(Lightbox lightbox, User actingUser)
throws MediaAlbumException;

/**
 * Add a media item to a lightbox.
 * 
 * @param lightbox the lightbox to add the media item
 * @param itemId the media item ID to add to the lightbox
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have 
 * permission to add the selected item to the lightbox
 * @see #addMediaItemsToLightbox(Lightbox, Integer[], User)
 * @return <em>true</em> if item was added, <em>false</em> if item 
 * was already in the lightbox
 */
public boolean addMediaItemToLightbox(Lightbox lightbox, Integer itemId,
		User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Add multiple media item to a lightbox.
 * 
 * <p>Note this does <em>not</em> save the lightbox, it merely 
 * adds the media item to the provided ligthbox object and 
 * marks the lightbox as "dirty". Duplicate media item IDs
 * will not be added to the lightbox.</p>
 * 
 * @param lightbox the lightbox to add the media item
 * @param itemIds the media item IDs to add to the lightbox
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have 
 * permission to add the selected item to the lightbox
 * @return number of items actually added to lightbox, which could be
 * less than the number of items passed in if some of those items were
 * already in the lightbox
 */
public int addMediaItemsToLightbox(Lightbox lightbox, Integer[] itemIds,
		User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get a new Lightbox instance.
 * 
 * <p>The Lightbox will be owned by the <var>actingUser</var>.</p>
 * 
 * @param actingUser the acting user
 * @return a new Lightbox instance
 * @throws MediaAlbumException if an error occurs
 */
public Lightbox getLightboxInstance(User actingUser) throws MediaAlbumException;
}
