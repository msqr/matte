/* ===================================================================
 * MockMediaBiz.java
 * 
 * Created Dec 31, 2006 9:14:25 AM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.biz.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;

import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.KeyNameType;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.Geometry;
import magoffin.matt.ma2.support.MediaInfoCommand;
import magoffin.matt.ma2.support.MoveItemsCommand;
import magoffin.matt.ma2.support.ShareAlbumCommand;
import magoffin.matt.ma2.support.SortAlbumsCommand;
import magoffin.matt.ma2.support.SortMediaItemsCommand;
import magoffin.matt.ma2.support.UserCommentCommand;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Mock implementation of MediaBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class MockMediaBiz implements MediaBiz {
	
	private File baseDir;
	
	private final Logger log = Logger.getLogger(getClass());
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#addMediaItemsToAlbum(magoffin.matt.ma2.domain.Album, java.lang.Long[], magoffin.matt.ma2.biz.BizContext)
	 */
	public int addMediaItemsToAlbum(Album album, Long[] mediaItemIds,
			BizContext context) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#moveMediaItems(magoffin.matt.ma2.support.MoveItemsCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public void moveMediaItems(MoveItemsCommand command, BizContext context) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeAlbumOrdering(magoffin.matt.ma2.support.SortAlbumsCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeAlbumOrdering(SortAlbumsCommand command, BizContext context) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemOrdering(magoffin.matt.ma2.support.SortMediaItemsCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeMediaItemOrdering(SortMediaItemsCommand command,
			BizContext context) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#sortAlbumItems(magoffin.matt.ma2.domain.Album)
	 */
	public void sortAlbumItems(Album album) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbumParent(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getAlbumParent(Long childAlbumId, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#deleteAlbum(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album deleteAlbum(Long albumId, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#deleteCollectionAndItems(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public List<MediaItem> deleteCollectionAndItems(Long collectionId, BizContext context) {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#deleteMediaItems(java.lang.Long[], magoffin.matt.ma2.biz.BizContext)
	 */
	public int deleteMediaItems(Long[] itemIds, BizContext context) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbum(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getAlbum(Long albumId, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbumWithItems(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getAlbumWithItems(Long albumId, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getCollectionWithItems(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Collection getCollectionWithItems(Long collectionId,
			BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getGeometry(magoffin.matt.ma2.MediaSize)
	 */
	public Geometry getGeometry(MediaSize size) {
		return size.getGeometry();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaHandler(java.io.File)
	 */
	public MediaHandler getMediaHandler(File file) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaHandler(java.lang.String)
	 */
	public MediaHandler getMediaHandler(String mime) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemCollection(magoffin.matt.ma2.domain.MediaItem)
	 */
	public Collection getMediaItemCollection(MediaItem item) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemResource(magoffin.matt.ma2.domain.MediaItem)
	 */
	public Resource getMediaItemResource(MediaItem item) {
		return new FileSystemResource(new File(baseDir, item.getPath()));
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemWithInfo(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public MediaItem getMediaItemWithInfo(Long itemId, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemsForAlbum(magoffin.matt.ma2.domain.Album, magoffin.matt.ma2.biz.BizContext)
	 */
	public List<MediaItem> getMediaItemsForAlbum(Album album, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getMediaItemsForCollection(magoffin.matt.ma2.domain.Collection, magoffin.matt.ma2.biz.BizContext)
	 */
	public List<MediaItem> getMediaItemsForCollection(Collection collection, 
			BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getQualityValue(magoffin.matt.ma2.MediaQuality)
	 */
	public float getQualityValue(MediaQuality quality) {
		return quality.getQualityValue();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getScaledGeometry(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest)
	 */
	public Geometry getScaledGeometry(MediaItem item, MediaRequest request) {
		int imageWidth;// = item.getWidth().intValue();
		int imageHeight;// = item.getHeight().intValue();

		imageWidth = item.getWidth();
		imageHeight = item.getHeight();

		int desiredWidth = imageWidth;
		int desiredHeight = imageHeight;
		
		int w = desiredWidth;
		int h = desiredHeight;
				
		Geometry desiredGeometry = getGeometry(request.getSize());
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
		
		Geometry resultGeometry = new Geometry(w,h);

		if ( log.isDebugEnabled() ) {
			log.debug("Output dimensions: " +resultGeometry);
		}
		
		return resultGeometry;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getSharedAlbum(java.lang.String, magoffin.matt.ma2.biz.BizContext)
	 */
	public Album getSharedAlbum(String key, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#incrementMediaItemHits(java.lang.Long)
	 */
	public int incrementMediaItemHits(Long itemId) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#isFileSupported(java.io.File)
	 */
	public boolean isFileSupported(File file) {
		return false;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#removeMediaItemsFromAlbum(java.lang.Long, java.lang.Long[], magoffin.matt.ma2.biz.BizContext)
	 */
	public int removeMediaItemsFromAlbum(Long albumId, Long[] itemIds,
			BizContext context) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#shareAlbum(magoffin.matt.ma2.support.ShareAlbumCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public String shareAlbum(ShareAlbumCommand command, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeAlbum(magoffin.matt.ma2.domain.Album, magoffin.matt.ma2.biz.BizContext)
	 */
	public Long storeAlbum(Album album, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeAlbumParent(java.lang.Long, java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeAlbumParent(Long childAlbumId, Long parentAlbumId, BizContext context) {
		// not supported
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemInfo(magoffin.matt.ma2.support.MediaInfoCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeMediaItemInfo(MediaInfoCommand command, BizContext context) {
		// not supported
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemRating(java.lang.Long[], short, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeMediaItemRating(Long[] itemIds, short rating,
			BizContext context) {
		// not supported
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#unShareAlbum(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public void unShareAlbum(Long albumId, BizContext context) {
		// not supported
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#getAlbumSortTypes(magoffin.matt.ma2.biz.BizContext)
	 */
	public List<KeyNameType> getAlbumSortTypes(BizContext context) {
		// not supported
		return Collections.emptyList();
	}

	public void storeMediaItemPoster(Long itemId, Long albumId, BizContext context) {
		// not supported
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.MediaBiz#storeMediaItemUserComment(magoffin.matt.ma2.support.UserCommentCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public void storeMediaItemUserComment(UserCommentCommand command,
			BizContext context) {
		// not supported
	}

	/**
	 * @return the baseDir
	 */
	public File getBaseDir() {
		return baseDir;
	}
	
	/**
	 * @param baseDir the baseDir to set
	 */
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

}
