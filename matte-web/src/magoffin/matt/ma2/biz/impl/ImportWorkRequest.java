/* ===================================================================
 * ImportWorkRequest.java
 * 
 * Created Feb 15, 2008 8:24:34 PM
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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;
import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkRequest;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.MediaSpec;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserTag;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.ma2.support.BasicMediaResponse;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.ma2.util.DateTimeUtil;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Helper class for {@link IOBizImpl} to import media items.
 *
 * @author matt
 * @version $Revision$ $Date$
 * @see IOBizImpl
 */
class ImportWorkRequest implements WorkRequest {
	
	private final IOBizImpl ioBizImpl;
	private final BizContext context;
	private final AddMediaCommand command;
	private final File collectionDir;
	private final File srcFile;
	
	private float amountCompleted = 0;
	private int numProcessed = 0;
	private int numExported = 0;
	private final List<String> errors = new LinkedList<String>();
	private URI collectionDirURI;
	private Collection collection;
	private final List<MediaItem> itemsAddedToCollection = new LinkedList<MediaItem>();
	private final List<Long> savedItemIdList = Collections.synchronizedList(
			new LinkedList<Long>());
	private User collectionOwner;
	private final Map<String, Long> archiveAlbumPathMapping = new HashMap<String, Long>();
	
	ImportWorkRequest(IOBizImpl bizImpl, BizContext context, AddMediaCommand command,
			File collectionDir, File srcFile) {
		ioBizImpl = bizImpl;
		this.context = context;
		this.command = command;
		this.collectionDir = collectionDir;
		this.srcFile = srcFile;
	}
	
	public String getDisplayName() {
		return ioBizImpl.getMessages().getMessage(
				"import.media.work.displayName", null,
				"Importing media", context.getLocale());
	}

	public String getMessage() {
		if ( this.amountCompleted >= 0.5 ) {
			return ioBizImpl.getMessages().getMessage(
					"add.work.export.message", new Object[]{numExported,numProcessed},
					"Generating thumbnails", context.getLocale());
		}
		return ioBizImpl.getMessages().getMessage(
				"add.work.message", new Object[]{numProcessed},
				"Processed " +numProcessed +" items", context.getLocale());
	}

	public Integer getPriority() {
		return WorkBiz.DEFAULT_PRIORITY;
	}

	public List<Long> getObjectIdList() {
		return savedItemIdList;
	}

	public boolean canStart() {
		return true;
	}

	public boolean isTransactional() {
		return true;
	}
	
	public void startWork() throws Exception {
		try {
			// attach thread local BizContext
			BizContextUtil.attachBizContext(this.context);
			doWork();
		} finally {
			// detach thread local BizContext
			BizContextUtil.removeBizContext();
		}
	}
	
	private void doWork() throws Exception {
		collectionDirURI = collectionDir.toURI();
		
		collection = ioBizImpl.getCollectionDao().get(command.getCollectionId());
		collectionOwner = collection.getOwner();
		
		float numZipEntries = 0f;
		
		// 2: is this a zip archive?
		if ( command.getTempFile().getName().toLowerCase().endsWith(".zip") 
				|| ioBizImpl.getZipContentTypes().contains(command.getTempFile().getContentType()) ) {

			// a zipped media item(s) ... copy file and process
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(srcFile);
			
				numZipEntries = zipFile.size(); // float for %completed
				float currEntry = 0.0f;
				
				// look for metadata entry
				Enumeration<? extends ZipEntry> zipEnum = zipFile.entries();
				Document metadata = null;
				while ( zipEnum.hasMoreElements() ) {
					ZipEntry entry = zipEnum.nextElement();
					if ( entry.isDirectory() ) {
						numZipEntries--;
						continue;
					}
					if ( metadata == null 
							&& IOBizImpl.IMPORT_MEDIA_XML_METADATA_NAME.equalsIgnoreCase(entry.getName())) {
						// load into DOM so can query with XPath later
						metadata = ioBizImpl.getXmlHelper().getDocument(zipFile.getInputStream(entry));
						numZipEntries--;
						continue;
					}
					if ( ioBizImpl.shouldIgnoreZipResource(entry.getName()) ) {
						numZipEntries--;
						if ( ioBizImpl.log.isDebugEnabled() ) {
							ioBizImpl.log.debug("Ignoring zip resource [" +entry.getName() +']');
						}
						continue;
					}
				}
				
				// also support metadata document from command directly
				if ( command.getMetaXmlFile() != null ) {
					metadata = ioBizImpl.getXmlHelper().getDocument(
							command.getMetaXmlFile().getInputStream());
				}
				
				if ( metadata != null && ioBizImpl.getMetadataSchemaResource() != null ) {
					ioBizImpl.getXmlHelper().validateXml(new DOMSource(metadata), ioBizImpl.getMetadataSchemaResource());
				}
				
				zipEnum = zipFile.entries();
				while ( zipEnum.hasMoreElements() ) {
					ZipEntry entry = zipEnum.nextElement();
					boolean validItem = false;
					try {
						String zipEntryName = entry.getName();
						if ( entry.isDirectory() || ioBizImpl.shouldIgnoreZipResource(zipEntryName) ) {
							continue;
						}
						
						// remove leading slash, if present
						if ( zipEntryName.charAt(0) == '/' ) {
							zipEntryName = zipEntryName.substring(1);
						}
						
						File currOutputFile = new File(collectionDir,zipEntryName);
						currOutputFile.getParentFile().mkdirs();
						if ( ioBizImpl.log.isDebugEnabled() ) {
							ioBizImpl.log.debug("Unzipping file " +currOutputFile.getAbsolutePath());
						}
						FileCopyUtils.copy(zipFile.getInputStream(entry),
								new FileOutputStream(currOutputFile));
						MediaItem item = handleNewMediaItem(currOutputFile);
						if ( item != null ) {
							validItem = true;
							if ( metadata != null ) {
								handleMetadata(zipEntryName, item, metadata);
							} else if ( command.isAutoAlbum() ) {
								handleAutoAlbum(zipEntryName.split("/"), item);
							}
						} else {
							numZipEntries--;
						}
					} finally {
						if ( validItem ) {
							currEntry++;
							amountCompleted = currEntry / (numZipEntries * 2f);
						}
					}
				}
				
				// delete the original zip file
				if ( !srcFile.delete() ) {
					ioBizImpl.log.warn("Unable to delete file [" +srcFile +"]");
				}
			} finally {
				if ( zipFile != null ) {
					zipFile.close();
				}
			}
		} else {
			// just a single media item ... copy file and process
			this.amountCompleted = 0.5f;
			handleNewMediaItem(srcFile);
		}
		
		// now save collection and items
		ioBizImpl.getCollectionDao().store(collection);
		this.numProcessed = itemsAddedToCollection.size();
		for ( MediaItem item : itemsAddedToCollection ) {
			// find out what the saved MediaItem IDs are via their paths
			MediaItem savedItem = ioBizImpl.getMediaItemDao().getItemForPath(
					collection.getCollectionId(), item.getPath());
			Long savedItemId = savedItem.getItemId();
			
			this.savedItemIdList.add(savedItemId);
			
			// if caching enabled, generate normal thumbnail image now
			if ( ioBizImpl.getSystemBiz().getCacheDirectory() != null ) {
				BasicMediaRequest request = new BasicMediaRequest(savedItemId);
				request.setOriginal(false);
				MediaSize thumbSize = MediaSize.THUMB_NORMAL;
				MediaQuality thumbQuality = MediaQuality.GOOD;
				if ( context.getActingUser() != null ) {
					User actingUser = context.getActingUser();
					if ( actingUser.getThumbnailSetting() != null ) {
						MediaSpec thumbSpec = actingUser.getThumbnailSetting();
						try {
							thumbSize = MediaSize.valueOf(thumbSpec.getSize());
							thumbQuality = MediaQuality.valueOf(thumbSpec.getQuality());
						} catch ( IllegalArgumentException e ) {
							ioBizImpl.log.warn("Ignoring invalid media size/quality MediaSpec "
									+thumbSpec.getSize() +"/" +thumbSpec.getQuality());
						}
					}
				}
				request.setQuality(thumbQuality);
				request.setSize(thumbSize);
				BasicMediaResponse response = new BasicMediaResponse();
				ioBizImpl.exportSingleMediaItem(request, response, 
						new ImportBizContext(collection));
				this.numExported++;
			}
			
			this.amountCompleted += 1f / (this.numProcessed * 2f);
		}
		
		this.amountCompleted = 1.0f;
	}

	@SuppressWarnings("unchecked")
	private void handleMetadata(String zipEntryName, MediaItem item, Document metadata) {
		String[] itemPath = zipEntryName.split("/");
		
		// look for item via XPath
		String itemXPath = "//m:item[@archive-path=\"" 
			+ioBizImpl.escapeItemNameForXPath(zipEntryName) +"\"]";
		NodeList itemNodes = (NodeList)ioBizImpl.getXmlHelper().evaluateXPath(
				metadata.getDocumentElement(), 
				itemXPath, XPathConstants.NODESET);
		if ( itemNodes == null || itemNodes.getLength() < 1 ) {
			handleAutoAlbum(itemPath, item);
			return;
		}
		
		// handle albums
		Element itemNode = null;
		for ( int i = 0, len = itemNodes.getLength(); i < len; i++ ) {
			Element currItemNode = (Element)itemNodes.item(i);
			if ( i == 0 ) {
				itemNode = currItemNode;
			}
			
			// create item path from <album> elements, because zip path
			// might point to an item also in another album, i.e. one
			// item shared between multiple albums
			Element albumNode = (Element)currItemNode.getParentNode();
			Element currNode = albumNode;
			Deque<String> albums = new LinkedList<String>();
			while ( currNode.getLocalName().equals("album") ) {
				albums.addFirst(currNode.getAttribute("name"));
				currNode = (Element)currNode.getParentNode();
				if ( currNode.getParentNode() == null ) {
					break;
				}
			}
			albums.addLast(itemPath[itemPath.length-1]);
			String[] albumPath = albums.toArray(new String[albums.size()]);
			
			Album album = handleAutoAlbum(albumPath, item);
			if ( album != null ) {
				// Don't set album name... messes up auto album
				if ( !StringUtils.hasText(album.getComment()) ) {
					String comment = (String)ioBizImpl.getXmlHelper().evaluateXPath(albumNode, 
							"normalize-space(m:comment)", XPathConstants.STRING);
					if ( StringUtils.hasText(comment) ) {
						album.setComment(comment);
					}
				}
				if ( StringUtils.hasText(albumNode.getAttribute("creation-date")) ) {
					album.setCreationDate(ioBizImpl.parseDate(albumNode.getAttribute("creation-date")));
				}
				if ( StringUtils.hasText(albumNode.getAttribute("modify-date")) ) {
					album.setModifyDate(ioBizImpl.parseDate(albumNode.getAttribute("modify-date")));
				}
				if ( StringUtils.hasText(albumNode.getAttribute("album-date")) ) {
					album.setAlbumDate(ioBizImpl.parseDate(albumNode.getAttribute("album-date")));
				}
			}
		}
		
		if ( itemNode == null ) {
			return;
		}
		
		// set name
		if ( itemNode.hasAttribute("name") ) {
			item.setName(itemNode.getAttribute("name"));
		}
		
		// set rating
		if ( itemNode.hasAttribute("rating") ) {
			try {
				float ratingValue = Float.parseFloat(itemNode.getAttribute("rating"));
				List<MediaItemRating> ratingList = item.getUserRating();
				MediaItemRating userRating = null;
				for ( MediaItemRating rating : ratingList ) {
					if ( collectionOwner.getUserId().equals(rating.getRatingUser().getUserId()) ) {
						userRating = rating;
						break;
					}
				}
				if ( userRating == null ) {
					userRating = ioBizImpl.getDomainObjectFactory().newMediaItemRatingInstance();
					userRating.setRatingUser(collectionOwner);
					userRating.setCreationDate(Calendar.getInstance());
					ratingList.add(userRating);
				}
				userRating.setRating((short)ratingValue);
			} catch ( Exception e ) {
				ioBizImpl.log.warn("Exception parsing rating: " +e.toString());
			}
		}
		
		// set description
		String comment = (String)ioBizImpl.getXmlHelper().evaluateXPath(itemNode, 
				"normalize-space(m:comment)", XPathConstants.STRING);
		if ( StringUtils.hasText(comment) ) {
			item.setDescription(comment);
		}
		
		if ( itemNode.hasAttribute("item-date") ) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			try {
				Date date = sdf.parse(itemNode.getAttribute("item-date"));
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				item.setCreationDate(cal);
			} catch ( ParseException e ) {
				// we ignore the date
			}
		}

		// handle keywords
		String keywords = (String)ioBizImpl.getXmlHelper().evaluateXPath(itemNode, 
				"normalize-space(m:keywords)", XPathConstants.STRING);
		if ( keywords != null && keywords.length() > 0) {
			List<UserTag> tagList = item.getUserTag();
			UserTag tag = null;
			for ( UserTag userTag : tagList ) {
				if ( collectionOwner.getUserId().equals(userTag.getTaggingUser().getUserId()) ) {
					tag = userTag;
					break;
				}
			}
			if ( tag == null ) {
				tag = ioBizImpl.getDomainObjectFactory().newUserTagInstance();
				tag.setCreationDate(Calendar.getInstance());
				tag.setTaggingUser(collectionOwner);
				tagList.add(tag);
			}
			tag.setTag(keywords);
		}
		
		// handle metadata
		NodeList metaNodeList = (NodeList)ioBizImpl.getXmlHelper().evaluateXPath(itemNode,
				"m:meta", XPathConstants.NODESET);
		if ( metaNodeList.getLength() > 0 ) {
			List<Metadata> metaList = item.getMetadata();
			Map<String, Element> metaElementMap = new LinkedHashMap<String, Element>();
			for ( int nodeIdx = 0; nodeIdx < metaNodeList.getLength(); nodeIdx++ ) {
				Element elem = (Element)metaNodeList.item(nodeIdx);
				metaElementMap.put(elem.getAttribute("name"), elem);
			}
			for ( Metadata meta : metaList ) {
				if ( metaElementMap.containsKey(meta.getKey()) ) {
					Element metaNode = metaElementMap.remove(meta.getKey());
					metaNode.normalize();
					String value = metaNode.getTextContent();
					if ( StringUtils.hasText(value) ) {
						meta.setValue(value);
					}
				}
			}
			for ( String metaKey : metaElementMap.keySet() ) {
				Element metaNode = metaElementMap.get(metaKey);
				metaNode.normalize();
				String value = metaNode.getTextContent();
				if ( StringUtils.hasText(value) ) {
					Metadata meta = ioBizImpl.getDomainObjectFactory().newMetadataInstance();
					meta.setKey(metaKey);
					meta.setValue(value);
					metaList.add(meta);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Album handleAutoAlbum(String[] albumNames, MediaItem item) {
		Album album = null;
		if ( albumNames != null && albumNames.length > 1 ) {				
			StringBuilder buf = new StringBuilder(albumNames[0]);
			for ( int i = 1, len = albumNames.length - 1; i < len; i++ ) {
				buf.append('/').append(albumNames[i]);
			}
			String archiveAlbumPath = buf.toString();
			if ( archiveAlbumPathMapping.containsKey(archiveAlbumPath) ) {
				album = ioBizImpl.getAlbumDao().get(archiveAlbumPathMapping.get(archiveAlbumPath));
			} else {						
				// get the root album
				List<Album> albums = ioBizImpl.getAlbumDao().findAlbumsForUserAndName(
						collectionOwner.getUserId(), albumNames[0]);
				if ( albums.size() < 1 ) {
					// create new album
					album = ioBizImpl.getDomainObjectFactory().newAlbumInstance();
					album.setOwner(collectionOwner);
					album.setName(albumNames[0]);
					album = ioBizImpl.getAlbumDao().get(ioBizImpl.getMediaBiz().storeAlbum(
							album, context));
				} else {
					album = albums.get(0);
				}
				
				for ( int i = 1; i < albumNames.length - 1; i++ ) {
					String oneAlbumName = albumNames[i];
					Album childAlbum = null;
					for ( Album oneChildAlbum : (List<Album>)album.getAlbum() ) {
						if ( oneAlbumName.equals(oneChildAlbum.getName()) ) {
							childAlbum = oneChildAlbum;
							break;
						}
					}
					if ( childAlbum == null ) {
						// create new child album
						childAlbum = ioBizImpl.getDomainObjectFactory().newAlbumInstance();
						childAlbum.setOwner(collectionOwner);
						childAlbum.setName(oneAlbumName);
						childAlbum = ioBizImpl.getAlbumDao().get(ioBizImpl.getMediaBiz().storeAlbum(
								childAlbum, context));
						album.getAlbum().add(childAlbum);
						ioBizImpl.getAlbumDao().store(album);
					}
					album = childAlbum;
				}
				if ( album != null ) {
					archiveAlbumPathMapping.put(archiveAlbumPath, album.getAlbumId());
				}
			}
		}
		if ( album != null ) {
			// only add to album if not already there
			boolean found = false;
			for ( MediaItem albumItem : (List<MediaItem>)album.getItem() ) {
				if ( albumItem.getPath().equals(item.getPath()) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				album.getItem().add(item);
				ioBizImpl.getMediaBiz().sortAlbumItems(album);
				ioBizImpl.getAlbumDao().store(album);
			}
		}
		return album;
	}
	
	@SuppressWarnings("unchecked")
	private MediaItem handleNewMediaItem(File mediaFile) throws Exception {
		if ( !ioBizImpl.getMediaBiz().isFileSupported(mediaFile) ) {
			this.errors.add(ioBizImpl.getMessages().getMessage(
					"media.not.supported",
					new Object[]{mediaFile.getName()},
					"The media file [" +mediaFile.getName() 
					+"] is not a supported type.",
					context.getLocale()));
			mediaFile.delete();
			if ( ioBizImpl.log.isDebugEnabled() ) {
				ioBizImpl.log.debug("ERROR importing media item: The media file [" 
						+mediaFile.getName() +"] is not a supported type.");
			}
			return null;
		}
		numProcessed++;
		MediaHandler handler = ioBizImpl.getMediaBiz().getMediaHandler(mediaFile);
		MediaItem item = handler.createNewMediaItem(mediaFile);
		if ( item.getCreationDate() == null ) {
			item.setCreationDate(Calendar.getInstance());
		}
		item.setFileSize(mediaFile.length());
		item.setHits(0);
		
		// set the path using URLs so path normalized between OSes
		URI fileUri = collectionDirURI.relativize(mediaFile.toURI());
		String path = URLDecoder.decode(fileUri.toString(), "UTF-8");
		item.setPath(path);
		if ( item.getName() == null ) {
			item.setName(StringUtils.getFilename(path));
		}
		
		//item.setCollection(collection);
		
		// set the time zones
		if ( StringUtils.hasText(command.getMediaTz()) ) {
			item.setTz(ioBizImpl.getSystemBiz().getTimeZoneForCode(command.getMediaTz()));
		} else {
			item.setTz(collectionOwner.getTz());
		}
		if ( StringUtils.hasText(command.getLocalTz()) ) {
			item.setTzDisplay(ioBizImpl.getSystemBiz().getTimeZoneForCode(command.getLocalTz()));
		} else {
			item.setTzDisplay(collectionOwner.getTz());
		}
		
		// the item's date is in the server's local time zone
		// so adjust it to the specified media tz if available
		if ( item.getItemDate() != null ) {
			TimeZone mediaTz = TimeZone.getTimeZone(item.getTz().getCode());
			TimeZone displayTz = TimeZone.getTimeZone(item.getTzDisplay().getCode());
			if ( !mediaTz.equals(displayTz) ) {
				DateTimeUtil.adjustItemDateTimeZone(item, mediaTz, displayTz);
			}
		}
		
		// see if this item is actually already saved, via the path
		MediaItem currItem = ioBizImpl.getMediaItemDao().getItemForPath(
				collection.getCollectionId(), path);
		if ( currItem != null) {
			// item already there, so just copy data to the persisted object for saving
			
			currItem.setCreationDate(item.getCreationDate());
			if ( item.getItemDate() != null ) {
				currItem.setItemDate(item.getItemDate());
			}
			currItem.setModifyDate(Calendar.getInstance());
			currItem.getMetadata().clear();
			currItem.getMetadata().addAll(item.getMetadata());
			currItem.setWidth(item.getWidth());
			currItem.setHeight(item.getHeight());
			currItem.setFileSize(item.getFileSize());
			currItem.setMime(item.getMime());
			currItem.setTz(item.getTz());
			currItem.setTzDisplay(item.getTzDisplay());
			
			item = currItem;
		} else {
			collection.getItem().add(item);
		}
		itemsAddedToCollection.add(item);
		return item;
	}

	public float getAmountCompleted() {
		return amountCompleted;
	}
	
}