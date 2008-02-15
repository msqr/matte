/* ===================================================================
 * ExportZipArchive.java
 * 
 * Created Feb 15, 2008 7:34:10 PM
 * 
 * Copyright (c) 2008 Matt Magoffin.
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.IOBiz.TwoPhaseExportRequest;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumImportType;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.CollectionImport;
import magoffin.matt.ma2.domain.ItemImportType;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.UserTag;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.util.NonClosingOutputStream;

import org.springframework.util.StringUtils;

/**
 * Helper class for the {@link IOBizImpl} class to export Zip archives.
 * 
 * @author matt
 * @version $Revision$ $Date$
 * @see IOBizImpl
 */
class ExportZipArchive implements TwoPhaseExportRequest {
	
	private final IOBizImpl ioBizImpl;
	private final Long[] itemIds;
	private final BizContext context;
	private final MediaRequest request;
	private MediaResponse response;
	private Album album;
	private CollectionImport metadata;
	private MediaItem currItem = null;
	private List<Long> processedItems = new LinkedList<Long>();
	private String exportMessage;
	private Long workTicket = null;
	private Set<String> zipNames = new LinkedHashSet<String>();
	
	ExportZipArchive(IOBizImpl ioBizImpl, Long[] itemIds, String exportMessage, 
			MediaRequest request, MediaResponse response, BizContext context) {
		this.ioBizImpl = ioBizImpl;
		this.request = request;
		this.response = response;
		this.itemIds = itemIds;
		this.exportMessage = exportMessage;
		this.context = context;
	}
	
	public void setMediaResponse(MediaResponse response) {
		this.response = response;
		if ( workTicket != null ) {
			ioBizImpl.getWorkBiz().workReadyNow(workTicket);
		}
	}
	public float getAmountCompleted() {
		return itemIds == null ? 0f 
				: (float)processedItems.size() / (float)itemIds.length;
	}
	public String getDisplayName() {
		return exportMessage;
	}
	public String getMessage() {
		return currItem == null ? "" : 
			ioBizImpl.getMessages().getMessage("export.items.message", 
				new Object[] {
					this.processedItems.size(),
					this.itemIds.length,
					currItem.getPath(),
				}, context.getLocale());
	}
	public List<Long> getObjectIdList() {
		return processedItems;
	}
	public Integer getPriority() {
		return WorkBiz.DEFAULT_PRIORITY;
	}
	public boolean canStart() {
		return this.response != null;
	}
	public boolean isTransactional() {
		return true;
	}
	@SuppressWarnings("unchecked")
	public void startWork() throws Exception {
		response.setMimeType(ioBizImpl.getZipMimeType());
		final ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
		String zipPathFormat = (album == null 
			? "%s/%s" 
			: "%s/%0" +String.valueOf(itemIds.length).length() + "d_%s" );
		try {
			int itemCount = 0;
			AlbumImportType albumMetadata = null;
			if ( this.metadata != null && this.album != null ) {
				albumMetadata = setupAlbumMetadata();
			}
			for ( Long itemId : itemIds ) {
				itemCount++;
				final MediaItem item = ioBizImpl.getMediaItemDao().get(itemId);
				currItem = item;
				
				// construct zip path from collection + item path
				String zipPath = null;
				if ( album == null ) {
					Collection col = ioBizImpl.getCollectionDao().getCollectionForMediaItem(
							item.getItemId());
					zipPath = String.format(zipPathFormat, 
							col.getName().replace('/', '_'),item.getPath());
				} else {
					zipPath = String.format(zipPathFormat, 
							album.getName().replace('/','_'),  
							itemCount, 
							StringUtils.getFilename(item.getPath()));
				}
				if ( zipNames.contains(zipPath) ) {
					int count = 0;
					while ( true ) {
						count++;
						int idx = zipPath.lastIndexOf('.');
						if ( idx >= 0 ) {
							zipPath = zipPath.substring(0, idx)
								+'_' +count +zipPath.substring(idx);
						} else {
							zipPath += '_' +count;
						}
						if ( !zipNames.contains(zipPath) ) {
							break;
						}
					}
				}
				zipNames.add(zipPath);
				if ( albumMetadata != null ) {
					ItemImportType itemImportType = setupItemMetadata(zipPath);
					albumMetadata.getItem().add(itemImportType);
				}
				ZipEntry entry = new ZipEntry(zipPath);
				zout.putNextEntry(entry);
				BasicMediaRequest itemRequest = new BasicMediaRequest(request);
				itemRequest.setMediaItemId(item.getItemId());
				ioBizImpl.exportSingleMediaItem(itemRequest, new MediaResponse() {
					public OutputStream getOutputStream() {
						return new NonClosingOutputStream(zout);
					}
					public void setItem(MediaItem responseItem) {
						// ignore
					}
					public void setMediaLength(long length) {
						// ignore
					}
					public void setMimeType(String mime) {
						// ignore
					}
					public void setModifiedDate(long date) {
						// ignore
					}
				}, context);
				processedItems.add(item.getItemId());
			}
			if ( this.metadata != null ) {
				ZipEntry entry = new ZipEntry("metadata.xml");
				zout.putNextEntry(entry);
				this.ioBizImpl.getXmlHelper().getMarshaller()
					.marshal(this.metadata, zout);
			}
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} finally {
			if ( zout != null ) {
				try {
					zout.flush();
					zout.finish();
					zout.close();
				} catch ( IOException e ) {
					ioBizImpl.log.warn("IOException closing zip output stream: " +e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ItemImportType setupItemMetadata(String zipPath) {
		ItemImportType itemMetadata = this.ioBizImpl.getDomainObjectFactory()
			.newItemImportTypeInstance();
		itemMetadata.setArchivePath(zipPath);
		itemMetadata.setComment(this.currItem.getDescription());
		List<UserTag> tagList = this.currItem.getUserTag();
		if ( tagList != null && tagList.size() > 0 ) {
			StringBuilder buf = new StringBuilder();
			for ( UserTag userTag : tagList ) {
				if ( buf.length() > 0 ) {
					buf.append(", ");
				}
				buf.append(userTag.getTag());
			}
			itemMetadata.setKeywords(buf.toString());
		}
		itemMetadata.setName(this.currItem.getName());
		List<MediaItemRating> ratingList = this.currItem.getUserRating();
		if ( ratingList != null && ratingList.size() > 0 ) {
			Collection col = ioBizImpl.getCollectionDao().getCollectionForMediaItem(
					this.currItem.getItemId());
			for ( MediaItemRating rating : ratingList ) {
				if ( rating.getRatingUser().getUserId().equals(
						col.getOwner().getUserId()) ) {
					itemMetadata.setRating(rating.getRating());
					break;
				}
			}
		}
		// TODO support metadata list?
		return itemMetadata;
	}

	@SuppressWarnings("unchecked")
	private AlbumImportType setupAlbumMetadata() {
		AlbumImportType albumMetadata = this.ioBizImpl.getDomainObjectFactory()
			.newAlbumImportTypeInstance();
		albumMetadata.setAlbumDate(this.album.getAlbumDate());
		albumMetadata.setComment(this.album.getComment());
		albumMetadata.setCreationDate(this.album.getCreationDate());
		albumMetadata.setModifyDate(this.album.getModifyDate());
		albumMetadata.setName(this.album.getName());
		// perhaps add this? albumMetadata.setSort()
		this.metadata.getAlbum().add(albumMetadata);
		return albumMetadata;
	}

	/**
	 * @param album the album to set
	 */
	void setAlbum(Album album) {
		this.album = album;
		this.metadata = this.ioBizImpl.getDomainObjectFactory()
			.newCollectionImportInstance();
	}

	/**
	 * @param workTicket the workTicket to set
	 */
	void setWorkTicket(Long workTicket) {
		this.workTicket = workTicket;
	}
	
}