/* ===================================================================
 * MediaBizImpl.java
 * 
 * Created Mar 3, 2006 9:13:56 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.biz.impl;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import magoffin.matt.dao.BasicBatchOptions;
import magoffin.matt.dao.BatchableDao.BatchCallback;
import magoffin.matt.dao.BatchableDao.BatchCallbackResult;
import magoffin.matt.dao.BatchableDao.BatchMode;
import magoffin.matt.dao.BatchableDao.BatchResult;
import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.AuthorizationException.Reason;
import magoffin.matt.ma2.ConfigurationException;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.ValidationException;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.KeyNameType;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserComment;
import magoffin.matt.ma2.domain.UserTag;
import magoffin.matt.ma2.support.Geometry;
import magoffin.matt.ma2.support.MediaInfoCommand;
import magoffin.matt.ma2.support.MoveItemsCommand;
import magoffin.matt.ma2.support.ShareAlbumCommand;
import magoffin.matt.ma2.support.SortAlbumsCommand;
import magoffin.matt.ma2.support.SortMediaItemsCommand;
import magoffin.matt.ma2.support.UserCommentCommand;
import magoffin.matt.ma2.util.DateTimeUtil;
import magoffin.matt.ma2.util.MediaItemSorter;
import magoffin.matt.ma2.util.MediaItemSorter.SortMode;
import magoffin.matt.util.MessageDigester;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

/**
 * Default implementation of MediaBiz.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>mediaHandlerFileExtensionMap</dt>
 *   <dd>A Map of file name extensions (without the period) to 
 *   implementations of {@link magoffin.matt.ma2.MediaHandler} 
 *   that should be used for files with matching extensions.</dd>
 *   
 *   <dt>mediaHandlerMimeMap</dt>
 *   <dd>A Map of MIME types to 
 *   implementations of {@link magoffin.matt.ma2.MediaHandler} 
 *   that should be used for media items with matching MIME types.</dd>
 *   
 *   <dt>geometryMap</dt>
 *   <dd>A mapping of MediaSize constants to Geometry sizes.</dd>
 *   
 *   <dt>qualityMap</dt>
 *   <dd>A mapping of MediaQuality constants to Float values, where 
 *   the float ranges from <kbd>0.0</kbd> to <kdb>1.0</kbd> and <kdb>1.0</kbd> 
 *   represents the best quality possible.</dd>
 *   
 *   <dt>sortModeMap</dt>
 *   <dd>A mapping of integer sort keys to Comparators for sorting
 *   MediaItem instances with. This is used for sorting album items.
 *   If not configured, then the {@link MediaItemSorter} class will be
 *   used by default. For each sort key defined (either via this map
 *   or via the {@link SortMode} keys) two message 
 *   properties will be looked up during a call to the 
 *   {@link #getAlbumSortTypes(BizContext)} method. The first will be
 *   like <code>album.sortmode.<b>&lt;KEY&gt;</b>.name</code>, where
 *   <code>&lt;KEY&gt;</code> is the sort key. This will be used for 
 *   the display name of this sort mode. The second message will be like
 *   <code>album.sortmode.<b>&lt;KEY&gt;</b>.caption</code> and will be
 *   used as the optional caption for the sort mode.</dd>
 * </dl>
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class MediaBizImpl implements MediaBiz {
	
	private static final String[] ALBUM_STORE_DO_NOT_CLONE = new String[] {
		"album", "item"
	};
	
	private Map<String,MediaHandler> mediaHandlerFileExtensionMap;
	private Map<String,MediaHandler> mediaHandlerMimeMap;
	private EnumMap<MediaSize,Geometry> geometryMap;
	private EnumMap<MediaQuality,Float> qualityMap;
	private AlbumDao albumDao;
	private CollectionDao collectionDao;
	private MediaItemDao mediaItemDao;
	private SystemBiz systemBiz;
	private DomainObjectFactory domainObjectFactory;
	private UserBiz userBiz;
	private IOBiz ioBiz;
	private byte[] salt;
	private MessageSource messages;
	private Map<Integer, Comparator<MediaItem>> sortModeMap;
	
	private final Logger log = Logger.getLogger(MediaBizImpl.class);
	
	/**
	 * Call to initialize after peroprties have been set.
	 */
	public synchronized void init() {
		if ( mediaHandlerFileExtensionMap == null ) {
			throw new ConfigurationException(null,"mediaHandlerFileExtensionMap");
		}
		if ( mediaHandlerMimeMap == null ) {
			throw new ConfigurationException(null,"mediaHandlerMimeMap");
		}
		if ( albumDao == null ) {
			throw new ConfigurationException(null,"albumDao");
		}
		if ( collectionDao == null ) {
			throw new ConfigurationException(null,"collectionDao");
		}
		if ( mediaItemDao == null ) {
			throw new ConfigurationException(null,"mediaItemDao");
		}
		if ( this.ioBiz == null ) {
			throw new ConfigurationException(null,"ioBiz");
		}
		if ( systemBiz == null ) {
			throw new ConfigurationException(null,"systemBiz");
		}
		if ( userBiz == null ) {
			throw new ConfigurationException(null,"userBiz");
		}
		if ( domainObjectFactory == null ) {
			throw new ConfigurationException(null,"domainObjectFactory");
		}
		if ( messages == null ) {
			throw new ConfigurationException(null,"messages");
		}
		if ( geometryMap == null ) {
			log.warn("The geometryMap property is not configured, will use default size values");
		}
		if ( qualityMap == null ) {
			log.warn("The qualityMap property is not configured, will use default quality values");
		}
		if ( sortModeMap == null ) {
			log.warn("The sortModeMap property is not configured, will use "
					+MediaItemSorter.class.getName());
		}
	}
	
	/**
	 * Call to clean up resources as necessary.
	 */
	public synchronized void finish() {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#isFileSupported(java.io.File)
	 */
	public boolean isFileSupported(File file) {
		return this.mediaHandlerFileExtensionMap.containsKey(getFileExtension(file.getName()));
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaHandler(java.io.File)
	 */
	public MediaHandler getMediaHandler(File file) {
		return this.mediaHandlerFileExtensionMap.get(getFileExtension(file.getName()));
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaHandler(java.lang.String)
	 */
	public MediaHandler getMediaHandler(String mime) {
		return this.mediaHandlerMimeMap.get(mime);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getGeometry(magoffin.matt.ma2.MediaSize)
	 */
	public Geometry getGeometry(MediaSize size) {
		if ( this.geometryMap != null && this.geometryMap.containsKey(size) ) {
			return this.geometryMap.get(size);
		}
		return size.getGeometry();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getQualityValue(magoffin.matt.ma2.MediaQuality)
	 */
	public float getQualityValue(MediaQuality quality) {
		if ( this.qualityMap != null && this.qualityMap.containsKey(quality) ) {
			return this.qualityMap.get(quality);
		}
		return quality.getQualityValue();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemRating(java.lang.Long, short, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public void storeMediaItemRating(Long[] itemIds, short rating, BizContext context) {
		User actingUser = context.getActingUser();
		for ( Long itemId : itemIds ) {
			MediaItem item = mediaItemDao.get(itemId);
			MediaItemRating r = null;
			if ( item.getUserRating().size() > 0 ) {
				for ( MediaItemRating oneRating : (List<MediaItemRating>)item.getUserRating() ) {
					if ( actingUser.getUserId().equals(oneRating.getRatingUser().getUserId()) ) {
						r = oneRating;
						break;
					}
				}
			}
			if ( r == null ) {
				r = domainObjectFactory.newMediaItemRatingInstance();
				item.getUserRating().add(r);
			}
	
			r.setCreationDate(Calendar.getInstance());
			r.setRating(rating);
			r.setRatingUser(actingUser);
			mediaItemDao.store(item);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemPoster(java.lang.Long, java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeMediaItemPoster(Long itemId, Long albumId, BizContext context) {
		User actingUser = context.getActingUser();
		MediaItem item = mediaItemDao.get(itemId);
		User owner = collectionDao.getCollectionForMediaItem(itemId).getOwner();
		if ( !owner.getUserId().equals(actingUser.getUserId()) ) {
			throw new AuthorizationException(actingUser.getLogin(), 
					Reason.ACCESS_DENIED);
		}
		Album album = albumDao.get(albumId);
		if ( !owner.getUserId().equals(album.getOwner().getUserId()) ) {
			throw new AuthorizationException(actingUser.getLogin(),
					Reason.ACCESS_DENIED);
		}
		
		album.setPoster(item);
		albumDao.store(album);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemResource(magoffin.matt.ma2.domain.MediaItem)
	 */
	public Resource getMediaItemResource(MediaItem item) {
		Collection col = getMediaItemCollection(item);
		File collectionDir = userBiz.getCollectionDirectory(col,null);
		File mediaItemFile = new File(collectionDir,item.getPath());
		return new FileSystemResource(mediaItemFile);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemCollection(magoffin.matt.ma2.domain.MediaItem)
	 */
	public Collection getMediaItemCollection(MediaItem item) {
		return collectionDao.getCollectionForMediaItem(item.getItemId());
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemsForCollection(magoffin.matt.ma2.domain.Collection, magoffin.matt.ma2.biz.BizContext)
	 */
	public List<MediaItem> getMediaItemsForCollection(Collection collection,
			BizContext context) {
		List<MediaItem> items = mediaItemDao.findItemsForCollection(collection.getCollectionId());
		List<MediaItem> results = new ArrayList<MediaItem>(items.size());
		for ( MediaItem item : items ) {
			results.add((MediaItem)domainObjectFactory.clone(item));
		}
		return results;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemsForAlbum(magoffin.matt.ma2.domain.Album, magoffin.matt.ma2.biz.BizContext)
	 */
	public List<MediaItem> getMediaItemsForAlbum(Album album, BizContext context) {
		List<MediaItem> items = mediaItemDao.findItemsForAlbum(album.getAlbumId());
		List<MediaItem> results = new ArrayList<MediaItem>(items.size());
		for ( MediaItem item : items ) {
			results.add((MediaItem)domainObjectFactory.clone(item));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#sortAlbumItems(magoffin.matt.ma2.domain.Album)
	 */
	@SuppressWarnings("unchecked")
	public void sortAlbumItems(Album album) {
		if ( album.getSortMode() != 0 ) {
			Comparator<MediaItem> sorter = null;
			if ( this.sortModeMap != null ) {
				sorter = this.sortModeMap.get(album.getSortMode());
			} 
			if ( sorter == null) {
				sorter = new MediaItemSorter(album.getSortMode());
			}
			
			// the following is a hack to work with Hibernate, which does not 
			// "see" that the list has changed if we simply re-sort it
			List<MediaItem> items = new LinkedList<MediaItem>();
			items.addAll(album.getItem());
			Collections.sort(items, sorter);
			boolean changed = false;
			for ( Iterator<MediaItem> itr1 = items.iterator(), itr2 = album.getItem().iterator(); 
					itr1.hasNext(); ) {
				MediaItem item1 = itr1.next();
				MediaItem item2 = itr2.next();
				// note we can compare object identity here!
				if ( item1 != item2 ) {
					changed = true;
					break;
				}
			}
			if ( changed ) {
				album.getItem().clear();
				album.getItem().addAll(items);
			}
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemOrdering(magoffin.matt.ma2.support.SortMediaItemsCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public void storeMediaItemOrdering(SortMediaItemsCommand command,
			BizContext context) {
		Album parent = albumDao.get(command.getAlbumId());
		List<MediaItem> children = parent.getItem();
		final Long[] ordering = command.getItemIds();
		Collections.sort(children, new Comparator<MediaItem>() {
			public int compare(MediaItem o1, MediaItem o2) {
				Integer pos1 = getPosition(o1.getItemId());
				Integer pos2 = getPosition(o2.getItemId());
				return pos1.compareTo(pos2);
			}
			private int getPosition(Long id) {
				for ( int i = 0; i < ordering.length; i++ ) {
					if ( id.equals(ordering[i]) ) {
						return i;
					}
				}
				return -1;
			}
		});
		parent.setSortMode(SortMode.NONE.getModeFlag());
		albumDao.store(parent);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#addMediaItemsToAlbum(magoffin.matt.ma2.domain.Album, java.lang.Long[], magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public int addMediaItemsToAlbum(Album album, Long[] mediaItemIds, BizContext context) {
		// don't add duplicate items to album
		Set<Long> albumItemIds = new HashSet<Long>();
		Album theAlbum = albumDao.get(album.getAlbumId());
		List<MediaItem> items = theAlbum.getItem();
		for ( MediaItem item : items ) {
			albumItemIds.add(item.getItemId());
		}
		int added = 0;
		for ( Long itemId : mediaItemIds ) {
			if ( albumItemIds.contains(itemId) ) continue;
			MediaItem item = mediaItemDao.get(itemId);
			if ( item != null ) {
				items.add(item);
				added++;
			}
		}
		if ( added > 0 ) {
			sortAlbumItems(theAlbum);
			albumDao.store(theAlbum);
		}
		if ( log.isDebugEnabled() ) {
			log.debug("Added " +added +" items to album [" +theAlbum.getAlbumId() +"]");
		}
		return added;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#deleteCollectionAndItems(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public List<MediaItem> deleteCollectionAndItems(Long collectionId, BizContext context) {
		Collection collectionToDelete = collectionDao.get(collectionId);
		if ( collectionToDelete == null ) return Collections.emptyList();
		
		int numItems = collectionToDelete.getItem().size();
		
		// physically delete the item resources
		int numDeleted = ioBiz.deleteMedia(collectionToDelete.getItem());
		if ( numItems != numDeleted ) {
			log.warn("Deleted " +numDeleted +" items from collection " +collectionToDelete.getCollectionId()
					+" but expected to delete " +numItems);
		}
		
		// remove any item currently in this collection from all albums
		List<MediaItem> removedItems = mediaItemDao.removeItemsOfCollectionFromAlbums(collectionId);
		if ( log.isDebugEnabled() ) {
			log.debug("Removed " +removedItems.size() +" items from albums");
		}
		
		// get all media items to return...
		removedItems = collectionToDelete.getItem();
		
		for ( MediaItem item : removedItems ) {
			mediaItemDao.delete(item);
		}
		
		// and then delete the collection object
		collectionDao.delete(collectionToDelete);

		return removedItems;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getScaledGeometry(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest)
	 */
	public Geometry getScaledGeometry(MediaItem item, MediaRequest request) {
		int imageWidth = item.getWidth();
		int imageHeight = item.getHeight();
		Geometry desiredGeometry = getGeometry(request.getSize());
		
		// check for rotation
		List<MediaEffect> effects = request.getEffects();
		boolean rotate = false;
		for ( MediaEffect effect : effects ) {
			if ( effect.getKey().endsWith(MediaEffect.KEY_ROTATE) ) {
				Object degrees = request.getParameters().get(
						MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES);
				if ( degrees instanceof Number ) {
					Number deg = (Number)degrees;
					if ( deg.intValue() != 0 && Math.abs(deg.intValue()) != 180 ) {
						rotate = true;
						break;
					}
				}
			}
		}
		if ( rotate ) {
			imageWidth = item.getHeight();
			imageHeight = item.getWidth();
		}

		
		int desiredWidth = imageWidth;
		int desiredHeight = imageHeight;
		
		int w = desiredWidth;
		int h = desiredHeight;
				
		// set up desired w, h
		desiredWidth = desiredGeometry.getWidth();
		desiredHeight = desiredGeometry.getHeight();

		if ( desiredWidth > imageWidth ) {
			desiredWidth = imageWidth;
		}
		if ( desiredHeight > imageHeight ) {
			desiredHeight = imageHeight;
		}
		
		// now calculate w, h
		
		if ( desiredWidth != imageWidth || desiredHeight != imageHeight ) {
			
			if ( !Geometry.Mode.EXACT.equals(desiredGeometry.getMode()) ) {
			
				double imageRatio = (double)imageWidth / (double)imageHeight;
		
				if ( desiredWidth != imageWidth && desiredHeight != imageHeight ) {
					// determine thumbnail size from WIDTH and HEIGHT
				    double ratio = (double)desiredWidth / (double)desiredHeight;
				    if ( log.isDebugEnabled() ) {
				    	log.debug("Desired ratio = " +ratio +"; image ratio = " +imageRatio);
				    }
					if ( imageRatio > ratio) {
						h = (int)Math.round(desiredWidth / imageRatio);
					} else {
						w = (int)Math.round(desiredHeight * imageRatio);
					}
				} else {
					if ( desiredHeight == imageHeight )  {
						// only specified width, so scale desired with to ratio of width
						h = (int)Math.round(desiredWidth / imageRatio);
						if ( log.isDebugEnabled() ) {
							log.debug("Desired height not specified, setting to " +desiredHeight);
						}
					} else {
						// only specified height, so scale desired with to ratio of height
						w = (int)Math.round(desiredHeight * imageRatio);
						if ( log.isDebugEnabled() ) {
							log.debug("Desired width not specified, setting to " +desiredWidth);
						}
					}
				}
			}
		}
		
		// last check for rounding over
		if ( w > desiredWidth ) {
			w = desiredWidth;
		}
		if ( h > desiredHeight ) {
			h = desiredHeight;
		}
		
		Geometry resultGeometry = rotate ? new Geometry(h,w) : new Geometry(w,h);

		if ( log.isDebugEnabled() ) {
			log.debug("Output dimensions: " +resultGeometry);
		}
		
		return resultGeometry;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbumWithItems(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getAlbumWithItems(Long albumId, BizContext context) {
		Album result = albumDao.getAlbumWithItems(albumId);
		if ( result == null ) return null;
		return getAlbumAndItems(result);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbum(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getAlbum(Long albumId, BizContext context) {
		Album album = albumDao.get(albumId);
		if ( album == null ) return null;
		return getAlbumCopy(album);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemWithInfo(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public MediaItem getMediaItemWithInfo(Long itemId, BizContext context) {
		return mediaItemDao.getMediaItemWithInfo(itemId);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getCollectionWithItems(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public Collection getCollectionWithItems(Long collectionId, BizContext context) {
		Collection original = collectionDao.getCollectionWithItems(collectionId);
		if ( original == null ) return null;
		Collection copy = (Collection)domainObjectFactory.clone(original);
		copy.getItem().clear();
		for ( MediaItem item : (List<MediaItem>)original.getItem() ) {
			copy.getItem().add(domainObjectFactory.clone(item));
		}
		return copy;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#removeMediaItemsFromAlbum(java.lang.Long, java.lang.Long[], magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public int removeMediaItemsFromAlbum(Long albumId, Long[] itemIds, BizContext context) {
		// get the album
		Album album = albumDao.get(albumId);
		if ( album == null ) {
			throw new IllegalArgumentException("Album not available");
		}
		Set<Long> itemIdSet = new HashSet<Long>();
		itemIdSet.addAll(Arrays.asList(itemIds));
		
		int numRemoved = 0;
		for ( Iterator<MediaItem> itemItr = ((List<MediaItem>)album.getItem()).iterator(); 
				itemItr.hasNext(); ) {
			MediaItem item = itemItr.next();
			if ( itemIdSet.contains(item.getItemId()) ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Removing item " +item.getItemId() +" from album " 
							+album.getAlbumId());
				}
				itemItr.remove();
				numRemoved++;
			}
		}
		
		albumDao.store(album);
		return numRemoved;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#deleteMediaItems(java.lang.Long[], magoffin.matt.ma2.biz.BizContext)
	 */
	public int deleteMediaItems(Long[] itemIds, BizContext context) {
		// physically delete the item resources
		List<MediaItem> items = new ArrayList<MediaItem>(itemIds.length);
		for ( Long itemId : itemIds ) {
			MediaItem item = mediaItemDao.get(itemId);
			if ( item != null ) {
				items.add(item);
			}
		}
		int numDeleted = ioBiz.deleteMedia(items);
		if ( log.isDebugEnabled() ) {
			log.debug("Deleted " +numDeleted +" media item resources");
		}
		
		// remove items from all albums
		List<MediaItem> removedItems = mediaItemDao.removeItemsFromAlbums(itemIds);
		if ( log.isDebugEnabled() ) {
			log.debug("Removed " +removedItems.size() +" items from albums");
		}
		
		// remove items from their collections
		removedItems = mediaItemDao.removeItemsFromCollections(itemIds);
		if ( log.isDebugEnabled() ) {
			log.debug("Removed " +removedItems.size() +" items from collections");
		}
		for ( MediaItem item : removedItems ) {
			mediaItemDao.delete(item);
		}
		
		return removedItems.size();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#shareAlbum(magoffin.matt.ma2.support.ShareAlbumCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public String shareAlbum(ShareAlbumCommand command, BizContext context) {
		Album album = albumDao.get(command.getAlbumId());
		Album parent = getAlbumParent(album.getAlbumId(), context);
		Theme theme = null;
		if ( command.getThemeId() != null ) {
			theme = systemBiz.getThemeById(command.getThemeId());
		}
		if ( theme == null ) {
			theme = systemBiz.getDefaultTheme();
		}

		// the parent == null is so that feed setting only applied to top-level albums
		album = applyShareSettings(album, theme, 
				parent == null, command);
		return album.getAnonymousKey();
	}
	
	@SuppressWarnings("unchecked")
	private Album applyShareSettings(Album album, Theme theme, boolean setFeed, 
			ShareAlbumCommand command) {
		if ( theme != null ) {
			album.setTheme(theme);
		}
		album.setAllowAnonymous(command.isShared());
		if ( command.isShared() && album.getAnonymousKey() == null ) {
			String data = album.getAlbumId().toString()+';'
				+album.getCreationDate().toString()+';'
				+System.currentTimeMillis();
			String key = generateKey(data);
			album.setAnonymousKey(key);
		}
		album.setAllowOriginal(command.isOriginal());
		album.setAllowBrowse(command.isBrowse());
		if ( setFeed ) {
			album.setAllowFeed(command.isFeed());
		}
		Album result = albumDao.get(albumDao.store(album));
		if ( command.isApplyToChildren() ) {
			for ( Album child : (List<Album>) album.getAlbum() ) {
				// do not apply share feed settings to child albums
				// but do apply unshare feed settings
				applyShareSettings(child, theme, 
						!command.isShared(), command);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getSharedAlbum(java.lang.String, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getSharedAlbum(String key, BizContext context) {
		Album result = albumDao.getAlbumForKey(key);
		if ( result == null ) return null;
		result = getAlbumAndItems(result);
		
		// remove any non-shared child albums
		purgeNonSharedAlbums(result);
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void purgeNonSharedAlbums(Album album) {
		for ( Iterator<Album> itr = ((List<Album>)album.getAlbum()).iterator(); itr.hasNext(); ) {
			Album child = itr.next();
			if ( !child.isAllowAnonymous() ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Removing non-shared album [" +child.getAlbumId() +"] from parent ["
							+album.getAlbumId() +"]");
				}
				itr.remove();
				continue;
			}
			purgeNonSharedAlbums(child);
		}
	}

	@SuppressWarnings("unchecked")
	private Album getAlbumAndItems(Album original) {
		Album copy = getAlbumCopy(original);
		for ( MediaItem item : (List<MediaItem>)original.getItem() ) {
			copy.getItem().add(domainObjectFactory.clone(item));
		}
		return copy;
	}
	
	private Album getAlbumCopy(Album original) {
		Album copy = (Album)domainObjectFactory.clone(original);
		copy.getItem().clear();
		
		// clone album poster item, too
		if ( original.getPoster() != null ) {
			MediaItem poster = (MediaItem)domainObjectFactory.clone(original.getPoster());
			copy.setPoster(poster);
		}
		return copy;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#unShareAlbum(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public void unShareAlbum(Long albumId, BizContext context) {
		Album a = albumDao.get(albumId);
		if ( a == null ) {
			return;
		}
		ShareAlbumCommand command = new ShareAlbumCommand();
		command.setAlbumId(albumId);
		command.setApplyToChildren(true);
		command.setFeed(false);
		command.setShared(false);
		command.setBrowse(false);
		applyShareSettings(a, null, true, command);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#incrementMediaItemHits(java.lang.Long)
	 */
	public synchronized int incrementMediaItemHits(Long itemId) {
		MediaItem item = getMediaItemDao().get(itemId);
		int hits = item.getHits()+1;
		item.setHits(hits);
		getMediaItemDao().store(item);
		return hits;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbumParent(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getAlbumParent(Long childAlbumId, BizContext context) {
		Album parent = getAlbumDao().getParentAlbum(childAlbumId);
		if ( parent != null ) {
			return getAlbumCopy(parent);
		}
		return parent;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeAlbumParent(java.lang.Long, java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public void storeAlbumParent(Long childAlbumId, Long parentAlbumId, BizContext context) {
		Album child = getAlbumDao().get(childAlbumId);
		if ( parentAlbumId == null ) {
			// remove child from parent
			Album parent = getAlbumDao().getParentAlbum(childAlbumId);
			if ( parent == null ) {
				BindException errors = new BindException(child, "childAlbum");
				errors.addError(new ObjectError("childAlbum",
						new String[]{"error.remove.parent.album.notfound"}, 
						new Object[] {child.getName()}, 
						"The album has no parent."));
				throw new ValidationException(errors);
			}
			parent.getAlbum().remove(child);
			storeAlbum(parent, context);
			storeAlbum(child, context);
			return;
		}
		
		// verify no cycles, i.e. child not already parent of parent
		Album parent = findChildAlbum(child, parentAlbumId);
		if ( parent != null ) {
			BindException errors = new BindException(child, "childAlbum");
			errors.addError(new ObjectError("childAlbum",
					new String[]{"error.add.parent.album.as.child"}, 
					new Object[] {child.getName(), parent.getName()}, 
					"You cannot add that album as a child to the other because it is already its parent"));
			throw new ValidationException(errors);
		}
		parent = getAlbumDao().get(parentAlbumId);
		parent.getAlbum().add(child);
		storeAlbum(parent, context);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#deleteAlbum(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album deleteAlbum(Long albumId, BizContext context) {
		Album a = getAlbumDao().get(albumId);
		if ( a == null ) {
			return null;
		}
		getAlbumDao().delete(a);
		return a;
	}

	@SuppressWarnings("unchecked")
	private Album findChildAlbum(Album parent, Long childAlbumId) {
		if ( childAlbumId.equals(parent.getAlbumId()) ) {
			return parent;
		}
		for ( Album childAlbum : (List<Album>)parent.getAlbum() ) {
			Album found = findChildAlbum(childAlbum, childAlbumId);
			if ( found != null ) {
				return found;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemInfo(magoffin.matt.ma2.util.MediaInfoCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeMediaItemInfo(MediaInfoCommand command, final BizContext context) {
		final Long[] itemIds = command.getItemIds();
		if ( log.isDebugEnabled() ) {
			log.debug("Saving MediaItem infos for items " 
					+Arrays.toString(itemIds));
		}
		final String name = StringUtils.hasText(command.getName())
			? command.getName().trim() : null;
		final String comments = StringUtils.hasText(command.getComments())
			? command.getComments().trim() : null;
		final Calendar itemDate = command.getDate();
		final String tags = StringUtils.hasText(command.getTags())
			? command.getTags().trim() : null;
		final magoffin.matt.ma2.domain.TimeZone mediaTimeZone = 
			command.getMediaTimeZone() == null
			? getSystemBiz().getDefaultTimeZone()
			: getSystemBiz().getTimeZoneForCode(command.getMediaTimeZone().getID());
		final magoffin.matt.ma2.domain.TimeZone displayTimeZone = 
			command.getDisplayTimeZone() == null
			? getSystemBiz().getDefaultTimeZone()
			: getSystemBiz().getTimeZoneForCode(command.getDisplayTimeZone().getID());
		BasicBatchOptions batchOptions = new BasicBatchOptions(
				MediaItemDao.BATCH_NAME_PROCESS_MEDIA_IDS, BatchMode.LIVE);
		batchOptions.getParameters().put(
				MediaItemDao.BATCH_PROCESS_PARAM_MEDIA_IDS_LIST, 
				itemIds);
		BatchResult batchResult = mediaItemDao.batchProcess(new BatchCallback<MediaItem>() {
			@SuppressWarnings("unchecked")
			public BatchCallbackResult handle(MediaItem mediaItem) {
				
				User owner = getMediaItemCollection(mediaItem).getOwner();
				
				// only allow setting some info if owner of item
				if ( context.getActingUser().getUserId().equals(owner.getUserId()) ) {
					
					// name
					if ( name != null && itemIds.length == 1 ) {
						mediaItem.setName(name);
					}
					
					// date
					if ( itemDate != null || itemIds.length == 1 ) {
						mediaItem.setItemDate(itemDate);
					}
					
					// tz
					mediaItem.setTz(mediaTimeZone);
					mediaItem.setTzDisplay(displayTimeZone);
					TimeZone mediaTz = TimeZone.getTimeZone(mediaTimeZone.getCode());
					TimeZone displayTz = TimeZone.getTimeZone(displayTimeZone.getCode());
					if ( !mediaTz.equals(displayTz) ) {
						try {
							DateTimeUtil.adjustItemDateTimeZone(mediaItem, mediaTz, displayTz);
						} catch ( ParseException e ) {
							log.warn("Date parse excaption on item " + mediaItem.getItemId() + ": " + e);
						}
					}
					
					// comments
					if ( comments != null || itemIds.length == 1) {
						mediaItem.setDescription(comments);
					}
					
					// copyright
					// TODO copyright
				
				}
				
				// tags can be set by any user
				if ( tags != null || itemIds.length == 1 ) {
					UserTag tag = null;
					for ( Iterator<UserTag> itr = mediaItem.getUserTag().iterator(); 
							itr.hasNext(); ) {
						tag = itr.next();
						if ( context.getActingUser().getUserId().equals(
								tag.getTaggingUser().getUserId()) ) {
							if ( tags == null ) {
								itr.remove();
							}
							break;
						}
					}
					if ( tags != null ) {
						if ( tag == null ) {
							tag = domainObjectFactory.newUserTagInstance();
							tag.setCreationDate(Calendar.getInstance());
							tag.setTaggingUser(context.getActingUser());
							mediaItem.getUserTag().add(tag);
						}
						tag.setTag(tags);
					}
				}
				
				return BatchCallbackResult.UPDATE;
			}
		}, batchOptions);
		if ( log.isDebugEnabled() ) {
			log.debug("Processed " +batchResult.numProcessed() +" MediaItem objects");
		}
	}

	private String generateKey(String data) {
		String key = MessageDigester.generateDigest(data, salt);
		int idx = key.indexOf('}');
		if ( idx >= 0 ) {
			idx += 1;
		} else {
			idx = 0;
		}
		
		int endIdx = key.length() - 1;
		
		// remove any url-unfriendly characters
		char[] chars = key.toCharArray();
		
		if ( chars[endIdx] == '=' ) {
			// ignore the trailing =
			endIdx--;
		}
		
		StringBuilder buf = new StringBuilder();
		for ( int i = idx; i <= endIdx; i++ ) {
			switch ( chars[i] ) {
				case '+':
					buf.append("Pl");
					break;
				case '&':
					buf.append("Am");
					break;
				case '=':
					buf.append("Eq");
					break;
				case '?':
					buf.append("Qu");
					break;
				case '%':
					buf.append("Pe");
					break;
				case ' ':
					buf.append("Sp");
					break;
				case '\\':
					buf.append("Sl");
					break;
				
				default:
					buf.append(chars[i]); 
			}
		}
		return buf.toString();
	}

	private void prepareAlbumForStorage(Album album, User owner) {
		// check owner set
		if ( album.getOwner() == null ) {
			album.setOwner(owner);
		}
		if ( album.getCreationDate() == null ) {
			album.setCreationDate(Calendar.getInstance());
		}
		if ( album.getAlbumDate() == null ) {
			album.setAlbumDate(album.getCreationDate());
		}
		if ( album.getAlbumId() != null ) {
			album.setModifyDate(Calendar.getInstance());
		}
		if ( album.getSortMode() == 0 ) {
			album.setSortMode(SortMode.DATE.getModeFlag());
		}
		if ( album.getSortMode() != SortMode.NONE.getModeFlag() ) {
			// make sure items sorted properly
			sortAlbumItems(album);
		}
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeAlbum(magoffin.matt.ma2.domain.Album, magoffin.matt.ma2.biz.BizContext)
	 */
	public Long storeAlbum(Album album, BizContext context) {
		Album albumToStore = album.getAlbumId() == null
			? album : getAlbumDao().get(album.getAlbumId());
		if ( album != albumToStore ) {
			// copy data from album to album to store
			BeanUtils.copyProperties(album, albumToStore, ALBUM_STORE_DO_NOT_CLONE);
		}
		prepareAlbumForStorage(albumToStore, context.getActingUser());
		return getAlbumDao().store(albumToStore);
	}

	private String getFileExtension(String fileName) {
		int pIdx = fileName.lastIndexOf('.');
		if ( pIdx > 0 && (pIdx+1) < fileName.length() ) {
			fileName = fileName.substring(fileName.lastIndexOf('.')+1);
		}
		return fileName.toLowerCase();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbumSortTypes(magoffin.matt.ma2.biz.BizContext)
	 */
	public List<KeyNameType> getAlbumSortTypes(BizContext context) {
		List<KeyNameType> sortModes = new LinkedList<KeyNameType>();
		if ( sortModeMap == null ) {
			for ( MediaItemSorter.SortMode mode : MediaItemSorter.SortMode.values() ) {
				KeyNameType sortMode = getDomainObjectFactory().newKeyNameTypeInstance();
				sortMode.setKey(mode.getModeFlag());
				sortModes.add(sortMode);
			}
		} else {
			for ( Integer mode : sortModeMap.keySet() ) {
				KeyNameType sortMode = getDomainObjectFactory().newKeyNameTypeInstance();
				sortMode.setKey(mode);
				sortModes.add(sortMode);
			}
		}
		for ( KeyNameType sortMode : sortModes ) {
			String name = messages.getMessage(
					"album.sortmode."+sortMode.getKey()+".name", 
					new Object[] {sortMode.getKey()}, context.getLocale());
			String comment = messages.getMessage(
					"album.sortmode."+sortMode.getKey()+".caption",
					new Object[] {sortMode.getKey(), name}, context.getLocale());
			if ( name != null ) {
				sortMode.setName(name);
			} else {
				sortMode.setName(String.valueOf(sortMode.getKey()));
			}
			if ( comment != null ) {
				sortMode.setComment(comment);
			}

		}
		return sortModes;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeAlbumOrdering(magoffin.matt.ma2.support.SortAlbumsCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public void storeAlbumOrdering(SortAlbumsCommand command, BizContext context) {
		Album parent = albumDao.get(command.getAlbumId());
		List<Album> children = parent.getAlbum();
		final Long[] ordering = command.getChildAlbumIds();
		Collections.sort(children, new Comparator<Album>() {
			public int compare(Album o1, Album o2) {
				Integer pos1 = getPosition(o1.getAlbumId());
				Integer pos2 = getPosition(o2.getAlbumId());
				return pos1.compareTo(pos2);
			}
			private int getPosition(Long id) {
				for ( int i = 0; i < ordering.length; i++ ) {
					if ( id.equals(ordering[i]) ) {
						return i;
					}
				}
				return -1;
			}
		});
		albumDao.store(parent);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#moveMediaItems(magoffin.matt.ma2.support.MoveItemsCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public void moveMediaItems(MoveItemsCommand command, BizContext context) {
		List<MediaItem> itemsToMove = new LinkedList<MediaItem>();
		for ( Long itemId : command.getItemIds() ) {
			itemsToMove.add(mediaItemDao.get(itemId));
		}
		Collection moveTo = collectionDao.get(command.getCollectionId());
		int numFilesMoved = ioBiz.moveMedia(itemsToMove, moveTo);
		if ( log.isDebugEnabled() ) {
			log.debug("Moved [" +numFilesMoved +"] media files to collection ["
					+command.getCollectionId() +']');
		}

		for ( MediaItem item : itemsToMove ) {
			Collection old = getMediaItemCollection(item);
			old.getItem().remove(item);
			moveTo.getItem().add(item);
			collectionDao.store(old);
		}
		collectionDao.store(moveTo);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemUserComment(magoffin.matt.ma2.support.UserCommentCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public void storeMediaItemUserComment(UserCommentCommand command,
			BizContext context) {
		if ( !StringUtils.hasText(command.getComment()) ) {
			return;
		}
		MediaItem item = mediaItemDao.get(command.getItemId());
		UserComment comment = domainObjectFactory.newUserCommentInstance();
		comment.setComment(command.getComment());
		comment.setCreationDate(Calendar.getInstance());
		User actingUser = context.getActingUser();
		if ( userBiz.isAnonymousUser(actingUser) ) {
			StringBuilder commenter = new StringBuilder();
			if ( StringUtils.hasText(command.getName()) ) {
				commenter.append(command.getName());
			}
			if ( StringUtils.hasText(command.getEmail()) ) {
				commenter.append(" (").append(command.getEmail())
					.append(')');
			}
			if ( commenter.length() > 0 ) {
				comment.setCommenter(commenter.toString());
			}
		} else {
			comment.setCommentingUser(actingUser);
		}
		comment.setApproved(true); // TODO set "approved" to false by default
		item.getUserComment().add(comment);
		mediaItemDao.store(item);
	}

	/**
	 * @return Returns the fileExtensionMap.
	 */
	public Map<String, MediaHandler> getMediaHandlerFileExtensionMap() {
		return mediaHandlerFileExtensionMap;
	}

	/**
	 * @param fileExtensionMap The fileExtensionMap to set.
	 */
	public void setMediaHandlerFileExtensionMap(Map<String, MediaHandler> fileExtensionMap) {
		this.mediaHandlerFileExtensionMap = fileExtensionMap;
	}

	/**
	 * @return Returns the mediaHandlerMimeMap.
	 */
	public Map<String, MediaHandler> getMediaHandlerMimeMap() {
		return mediaHandlerMimeMap;
	}

	/**
	 * @param mediaHandlerMimeMap The mediaHandlerMimeMap to set.
	 */
	public void setMediaHandlerMimeMap(Map<String, MediaHandler> mediaHandlerMimeMap) {
		this.mediaHandlerMimeMap = mediaHandlerMimeMap;
	}
	
	/**
	 * @return Returns the geometryMap.
	 */
	public EnumMap<MediaSize, Geometry> getGeometryMap() {
		return geometryMap;
	}
	
	/**
	 * @param geometryMap The geometryMap to set.
	 */
	public void setGeometryMap(EnumMap<MediaSize, Geometry> geometryMap) {
		this.geometryMap = geometryMap;
	}
	
	/**
	 * @return Returns the qualityMap.
	 */
	public EnumMap<MediaQuality, Float> getQualityMap() {
		return qualityMap;
	}
	
	/**
	 * @param qualityMap The qualityMap to set.
	 */
	public void setQualityMap(EnumMap<MediaQuality, Float> qualityMap) {
		this.qualityMap = qualityMap;
	}

	/**
	 * @return Returns the collectionDao.
	 */
	public CollectionDao getCollectionDao() {
		return collectionDao;
	}
	
	/**
	 * @param collectionDao The collectionDao to set.
	 */
	public void setCollectionDao(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
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
	 * @return Returns the mediaItemDao.
	 */
	public MediaItemDao getMediaItemDao() {
		return mediaItemDao;
	}
	
	/**
	 * @param mediaItemDao The mediaItemDao to set.
	 */
	public void setMediaItemDao(MediaItemDao mediaItemDao) {
		this.mediaItemDao = mediaItemDao;
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

	/**
	 * @return the ioBiz
	 */
	public IOBiz getIoBiz() {
		return ioBiz;
	}
	
	/**
	 * @param ioBiz the ioBiz to set
	 */
	public void setIoBiz(IOBiz ioBiz) {
		this.ioBiz = ioBiz;
	}
	
	/**
	 * @return the domainObjectFactory
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}
	
	/**
	 * @param domainObjectFactory the domainObjectFactory to set
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	/**
	 * @return Returns the salt.
	 */
	public byte[] getSalt() {
		return salt;
	}

	/**
	 * @param salt The salt to set.
	 */
	public void setSalt(byte[] salt) {
		this.salt = salt;
	}
	
	/**
	 * @return the systemBiz
	 */
	public SystemBiz getSystemBiz() {
		return systemBiz;
	}
	
	/**
	 * @param systemBiz the systemBiz to set
	 */
	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}
	
	/**
	 * @return the messages
	 */
	public MessageSource getMessages() {
		return messages;
	}
	
	/**
	 * @param messages the messages to set
	 */
	public void setMessages(MessageSource messages) {
		this.messages = messages;
	}
	
}
