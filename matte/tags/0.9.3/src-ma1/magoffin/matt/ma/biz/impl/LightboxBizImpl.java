/* ===================================================================
 * LightboxBizImpl.java
 * 
 * Created Jun 14, 2004 5:48:02 PM
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
 * $Id: LightboxBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.LightboxBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.dao.LightboxCriteria;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.LightboxAlbum;
import magoffin.matt.ma.xsd.LightboxItem;
import magoffin.matt.ma.xsd.User;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool.ObjectPool;

/**
 * Biz implementation for the LightboxBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class LightboxBizImpl extends AbstractBiz implements LightboxBiz {

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.LightboxBiz#getLightbox(magoffin.matt.ma.xsd.User, boolean)
 */
public Lightbox getLightbox(User user, boolean allowCached)
throws MediaAlbumException 
{
	LightboxCriteria crit = null;
	ObjectPool pool = null;
	Lightbox result = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Lightbox.class);
		crit = (LightboxCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(LightboxCriteria.LIGHTBOX_FOR_USER_SEARCH);
		crit.setQuery(user.getUserId());
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Lightbox.class);
		
		Lightbox[] data = (Lightbox[])dao.findByCriteria(crit);
		
		if ( data != null && data.length > 0 ) {
			result = data[0];
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	if ( result != null ) {
		// translate lbAlbum and lbItem objects into albumId and itemId integers
		int numItems = result.getLbAlbumCount();
		for ( int i = 0; i < numItems; i++ ) {
			LightboxAlbum lba = result.getLbAlbum(i);
			result.addAlbumId(lba.getAlbumId());
		}
		
		numItems = result.getLbItemCount();
		for ( int i = 0; i < numItems; i++ ) {
			LightboxItem lbi = result.getLbItem(i);
			result.addItemId(lbi.getItemId());
		}
		
		result.clearLbAlbum();
		result.clearLbItem();
	}
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.LightboxBiz#saveLightbox(magoffin.matt.ma.xsd.Lightbox, magoffin.matt.ma.xsd.User)
 */
public Lightbox saveLightbox(Lightbox lightbox, User actingUser)
throws MediaAlbumException 
{
	Lightbox lb = null;
	try {
		lb = (Lightbox)BeanUtils.cloneBean(lightbox);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data",e);
	}
	
	if ( lb.getOwner() == null ) {
		lb.setOwner(actingUser.getUserId());
	}
	
	// translate albumId and itemId arrays into LightboxAlbum, LightboxItem objects
	
	int numItems = lb.getAlbumIdCount();
	lb.clearLbAlbum();
	for ( int i = 0; i < numItems; i++ ) {
		Integer albumId = lb.getAlbumId(i);
		LightboxAlbum lba = new LightboxAlbum();
		lba.setAlbumId(albumId);
		lb.addLbAlbum(lba);
	}
	
	numItems = lb.getItemIdCount();
	lb.clearLbItem();
	for ( int i = 0; i < numItems; i++ ) {
		Integer itemId = lb.getItemId(i);
		LightboxItem lbi = new LightboxItem();
		lbi.setItemId(itemId);
		lb.addLbItem(lbi);
	}
	
	DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
			Lightbox.class);
	
	try {
		if ( lb.getLightboxId() == null ) {
			// create new
			lb.setCreationDate(new Date());
			dao.create(lb);
		} else {
			// update
			lb.setModificationDate(new Date());
			dao.update(lb);
		}
	} catch ( DAOException e ) {
		throw new MediaAlbumException("Unable to save Lightbox",e);
	}
	
	lb.clearLbAlbum();
	lb.clearLbItem();
	lb.setDirty(Boolean.FALSE);
	
	return lb;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.LightboxBiz#addMediaItemToLightbox(magoffin.matt.ma.xsd.Lightbox, java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public boolean addMediaItemToLightbox(Lightbox lightbox, Integer itemId,
		User actingUser) throws MediaAlbumException, NotAuthorizedException 
{
	Integer[] itemIds = new Integer[] {itemId};
	int count = addMediaItemsToLightbox(lightbox,itemIds,actingUser);
	if ( count < 1 ) {
		return false;
	}
	return true;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.LightboxBiz#addMediaItemsToLightbox(magoffin.matt.ma.xsd.Lightbox, java.lang.Integer[], magoffin.matt.ma.xsd.User)
 */
public int addMediaItemsToLightbox(Lightbox lightbox, Integer[] itemIds,
		User actingUser) throws MediaAlbumException, NotAuthorizedException 
{
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	// constrain items to not have duplicates
	List currList = Arrays.asList(lightbox.getItemId());
	Set itemSet = new HashSet();
	itemSet.addAll(currList);
	int count = 0;
	for ( int i = 0; i < itemIds.length; i++ ) {
		if ( !itemSet.contains(itemIds[i]) ) {
			lightbox.addItem(itemBiz.getMediaItemById(itemIds[i],
					ApplicationConstants.CACHED_OBJECT_ALLOWED));
			lightbox.addItemId(itemIds[i]);
			count++;
		}
	}
	
	lightbox.setDirty(Boolean.TRUE);
	return count;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.LightboxBiz#getLightboxInstance(magoffin.matt.ma.xsd.User)
 */
public Lightbox getLightboxInstance(User actingUser)
		throws MediaAlbumException 
{
	Lightbox lb = new Lightbox();
	if ( actingUser != null ) {
		lb.setOwner(actingUser.getUserId());
	}
	return lb;
}
}
