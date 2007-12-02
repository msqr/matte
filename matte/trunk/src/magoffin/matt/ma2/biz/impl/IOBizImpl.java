/* ===================================================================
 * IOBizImpl.java
 * 
 * Created Mar 2, 2006 7:03:19 PM
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;

import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.ObjectNotFoundException;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.WorkBiz.WorkRequest;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.MediaItemDao;
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
import magoffin.matt.ma2.support.ExportItemsCommand;
import magoffin.matt.ma2.support.InternalBizContext;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.ma2.util.XmlHelper;
import magoffin.matt.util.NonClosingOutputStream;
import magoffin.matt.util.SimpleThreadSafeDateFormat;
import magoffin.matt.util.ThreadSafeDateFormat;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of IOBiz.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class IOBizImpl implements IOBiz {
	
	/** The default property for the {@link #getZipMimeType()} property. */
	public static final String DEFAULT_ZIP_MIME_TYPE = "application/zip";
	
	private static final String REPARSE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private CollectionDao collectionDao;
	private WorkBiz workBiz;
	private MediaBiz mediaBiz;
	private SystemBiz systemBiz;
	private AlbumDao albumDao;
	private MediaItemDao mediaItemDao;
	private Set<String> zipContentTypes;
	private MessageSource messages;
	private FileTypeMap fileTypeMap;
	private XmlHelper xmlHelper;
	private DomainObjectFactory domainObjectFactory;
	private Resource metadataSchemaResource = 
		new ClassPathResource("magoffin/matt/ma2/biz/import.xsd");
	private ThreadSafeDateFormat xmlDateFormat
		= new SimpleThreadSafeDateFormat("yyyy-MM-dd");
	private ThreadSafeDateFormat xmlDateTimeFormat
		= new SimpleThreadSafeDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private String zipMimeType = DEFAULT_ZIP_MIME_TYPE;
	private List<Pattern> zipIgnorePatterns;
	
	private final Logger log = Logger.getLogger(IOBizImpl.class);
	
	/**
	 * Call to initialize after peroprties have been set.
	 */
	public synchronized void init() {
		// nothing to do
	}
	
	/**
	 * Call to clean up resources as necessary.
	 */
	public synchronized void finish() {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.IOBiz#importMedia(magoffin.matt.ma2.util.AddMediaCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	public WorkInfo importMedia(final AddMediaCommand command, final BizContext context) {
		if ( command == null || command.getTempFile() == null 
				|| command.getTempFile().getName() == null ) {
			throw new IllegalArgumentException("The AddMediaCommand's temporary file name can not be null.");
		}
		
		// get the Collection we are importing the media to...
		Collection c = collectionDao.get(command.getCollectionId());
		
		if ( c == null ) {
			throw new ObjectNotFoundException("Collection " +command.getCollectionId()
					+" not found");
		}

		// have to save temp file to non-temp location as work happens in new thread
		final File collectionDir = new File(systemBiz.getCollectionRootDirectory(),c.getPath());
		
		// make sure collection dir exists
		collectionDir.mkdirs();
		
		if ( command.getTempFile() == null || command.getTempFile().getSize() < 1 ) {
			throw new IllegalArgumentException(messages.getMessage("upload.nofiles", 
					null, context.getLocale()));
		}
		
		final File srcFile = new File(collectionDir,command.getTempFile().getName());
		if ( log.isDebugEnabled() ) {
			log.debug("Saving temp file to " +srcFile.getAbsolutePath());
		}
		try {
			FileCopyUtils.copy(command.getTempFile().getInputStream(),
				new FileOutputStream(srcFile));
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		
		return workBiz.submitWork(
			new ImportWorkRequest(context, command, collectionDir, srcFile));
	}
	
	private Calendar parseDate(String dateStr) {
		if ( dateStr.indexOf('T') != -1 ) {
			return xmlDateTimeFormat.parseCalendar(dateStr.substring(0,19));
		}
		return xmlDateFormat.parseCalendar(dateStr.substring(0, 10));
	}
	
	private String escapeItemNameForXPath(String string) {
		return string.replace("\"", "\\\"");
    }

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.IOBiz#deleteMedia(java.util.List)
	 */
	public int deleteMedia(List<MediaItem> itemsToDelete) {
		int numDeleted = 0;
		for ( final MediaItem item : itemsToDelete ) {
			Resource r = mediaBiz.getMediaItemResource(item);
			if ( r != null && r.exists() ) {
				try {
					File f = r.getFile();
					if ( f.delete() ) {
						numDeleted++;
					}
				} catch ( IOException e ) {
					log.warn("IOException getting file for Resource [" 
							+r.getDescription() +"]");
				}
			}
			
			deleteCacheFilesForItem(item);
		}
		return numDeleted;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.IOBiz#moveMedia(java.util.List, java.io.File)
	 */
	public int moveMedia(List<MediaItem> itemsToMove, Collection toCollection) {
		File toFolder = new File(systemBiz.getCollectionRootDirectory(), 
				toCollection.getPath());
		if ( !toFolder.exists() || !toFolder.isDirectory() ) {
			throw new IllegalArgumentException("Destination directory not valid: "
					+toFolder.getAbsolutePath());
		}
		int numMoved = 0;
		for ( final MediaItem item : itemsToMove ) {
			Resource r = mediaBiz.getMediaItemResource(item);
			if ( r != null && r.exists() ) {
				try {
					File src = r.getFile();
					File dest = new File(toFolder, src.getName());
					if ( log.isDebugEnabled() ) {
						log.debug("Moving media file [" +src.getAbsolutePath() 
								+"] to [" +dest.getAbsolutePath() +']');
					}
					if ( !src.renameTo(dest) ) {
						// manually move
						FileCopyUtils.copy(src, dest);
					}
					numMoved++;
				} catch ( IOException e ) {
					log.warn("IOException getting file for Resource [" 
							+r.getDescription() +"]");
				}
			}
			
			deleteCacheFilesForItem(item);
		}
		return numMoved;
	}

	private void deleteCacheFilesForItem(final MediaItem item) {
		// also clean out any cache files
		Collection col = mediaBiz.getMediaItemCollection(item);
		File cacheDir = new File(systemBiz.getCacheDirectory(),
					col.getOwner().getUserId().toString());
		File[] cacheFiles = cacheDir.listFiles(new FilenameFilter() {
			public boolean accept(File f, String name) {
				return name.startsWith(item.getItemId()+"_");
			}
		});
		for ( File cacheFile : cacheFiles ) {
			if ( !cacheFile.delete() ) {
				log.warn("Unable to delete media item cache file [" 
						+cacheFile.getAbsolutePath() +"]");
			} else if ( log.isDebugEnabled() ) {
				log.debug("Deleted mediaitem cache file ["
						+cacheFile.getAbsolutePath() +"]");
			}
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.IOBiz#exportMedia(magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse, magoffin.matt.ma2.biz.BizContext)
	 */
	public WorkInfo exportMedia(final MediaRequest request, final MediaResponse response, 
			final BizContext context){
		return workBiz.submitWork(
				new ExportWorkRequest(request, response, context));
	}
	
	private class ExportWorkRequest implements TwoPhaseExportRequest {	
		private boolean done = false;
		private List<Long> idList = new ArrayList<Long>(1);
		private MediaRequest request;
		private MediaResponse response;
		private BizContext context;
		private Long workTicket = null;
		
		private ExportWorkRequest(MediaRequest request, MediaResponse response, 
				BizContext context) {
			this.request = request;
			this.response = response;
			this.context = context;
		}

		public void setMediaResponse(MediaResponse response) {
			this.response = response;
			if ( workTicket != null ) {
				getWorkBiz().workReadyNow(workTicket);
			}
		}
		public float getAmountCompleted() {
			return done ? 1f : 0f;
		}
		public String getDisplayName() {
			return messages.getMessage("export.displayName", null, context.getLocale())
				+" " +messages.getMessage("item.displayName", null, context.getLocale())
				+" " +request.getMediaItemId();
		}
		public String getMessage() {
			return null;
		}
		public List<Long> getObjectIdList() {
			return idList;
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
		public void startWork() throws Exception {
			exportSingleMediaItem(request, response, context);
			idList.add(request.getMediaItemId());
			done = true;
		}
	}
	
	private void exportSingleMediaItem(MediaRequest request, MediaResponse response, BizContext context) {
		MediaItem item = this.mediaItemDao.get(request.getMediaItemId());
		if ( item == null ) {
			throw new ObjectNotFoundException("Item ["
					+request.getMediaItemId() +"] not available");
		}
		response.setItem(item);
		MediaHandler handler = this.mediaBiz.getMediaHandler(item.getMime());
		if ( !request.isOriginal() && !item.isUseIcon() ) {
			File cacheFile = null;
			if ( systemBiz.getCacheDirectory() != null ) {
				// see if already cached and can return that
				Collection col = context instanceof ImportBizContext
					? ((ImportBizContext)context).importCollection
					: mediaBiz.getMediaItemCollection(item);
				cacheFile = new File(systemBiz.getCacheDirectory(),
						col.getOwner().getUserId().toString());
				if ( !cacheFile.exists() ) {
					cacheFile.mkdirs();
				}
				
				String extension = handler.getFileExtension(item, request);
				cacheFile = new File(cacheFile,request.getCacheKey()+"."+extension);
				
				if ( cacheFile.exists() && cacheFile.length() > 0 ) {
					handleCacheFile(item,response,cacheFile);
					return;
				}
			}
			if ( cacheFile != null ) {
				try {
					// redirect response to cache file, then return cache file
					final OutputStream cacheOutput = new BufferedOutputStream(
							new FileOutputStream(cacheFile));
					MediaResponse tempResponse = new MediaResponse() {
						public void setMimeType(String mime) {
							// no need
						}
						public void setMediaLength(long length) {
							// no need
						}
						public void setModifiedDate(long date) {
							// no need
						}
						public void setItem(MediaItem responseItem) {
							// no need
						}
						public OutputStream getOutputStream() {
							return cacheOutput;
						}
					};
					request.getParameters().put(MediaRequest.OUTPUT_FILE_KEY, cacheFile);
					try {
						handler.handleMediaRequest(item,request,tempResponse);
					} finally {
						try {
							cacheOutput.flush();
							cacheOutput.close();
						} catch ( IOException e ) {
							log.warn("IOException closing cache file [" +cacheFile +"]: " 
									+e.toString());
						}
					}
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
				handleCacheFile(item,response,cacheFile);
				return;
			}
		}
		if ( response.getOutputStream() != null ) {
			// no cache file to be used, so handle without
			handler.handleMediaRequest(item,request,response);
		}
		return;
	}

	private void handleCacheFile(MediaItem item, MediaResponse response, File cacheFile) {
		if ( cacheFile.exists() && cacheFile.length() > 0 ) {
			response.setItem(item);
			response.setMediaLength(cacheFile.length());
			
			// get MIME type via JAF since some handlers change type for cached files
			FileDataSource ds = new FileDataSource(cacheFile);
			if ( getFileTypeMap() != null ) {
				ds.setFileTypeMap(getFileTypeMap());
			}
			response.setMimeType(ds.getContentType());

			response.setModifiedDate(cacheFile.lastModified());
			if ( response.getOutputStream() != null ) {
				try {
					FileCopyUtils.copy(new BufferedInputStream(
							new FileInputStream(cacheFile)), response.getOutputStream());
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private static class ImportBizContext extends InternalBizContext {
		private Collection importCollection;
		private ImportBizContext(Collection c) {
			this.importCollection = c;
		}
	}

	@SuppressWarnings("unchecked")
	private WorkInfo exportAlbum(Long albumId, boolean direct, final MediaRequest request, 
			final MediaResponse response, final BizContext context) {
		Album album = albumDao.getAlbumWithItems(albumId);
		List<MediaItem> items = album.getItem();
		Long[] itemIds = new Long[items.size()];
		int i = 0;
		for ( Iterator<MediaItem> itr = items.iterator(); itr.hasNext(); i++ ) {
			itemIds[i] = itr.next().getItemId();
		}
		String msg = messages.getMessage("export.displayName", null, context.getLocale())
			+" " +messages.getMessage("album.displayName", null, context.getLocale())
			+ " \"" +album.getName() +"\"";
		ExportZipArchive export = new ExportZipArchive(itemIds, msg, 
				request, response, context);
		export.album = album;
		WorkInfo info = workBiz.submitWork(export);
		if ( !direct ) {
			export.workTicket = info.getTicket();
		}
		return info;
	}

	private WorkInfo exportItems(Long[] itemIds, boolean direct, 
			MediaRequest request, MediaResponse response, BizContext context) {
		String msg = messages.getMessage("export.displayName", null, context.getLocale())
			+" " +messages.getMessage("items", null, context.getLocale());
		ExportZipArchive export = new ExportZipArchive(itemIds, msg, 
				request, response, context);
		WorkInfo info = workBiz.submitWork(export);
		if ( !direct ) {
			export.workTicket = info.getTicket();
		}
		return info;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.IOBiz#exportItems(magoffin.matt.ma2.support.ExportItemsCommand, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse, magoffin.matt.ma2.biz.BizContext)
	 */
	public WorkInfo exportItems(ExportItemsCommand command,
			MediaRequest request, MediaResponse response, BizContext context) {
		if ( command.getAlbumId() != null ) {
			return exportAlbum(command.getAlbumId(), command.isDirect(),
					request, response, context);
		}
		if ( command.getAlbumKey() != null && command.getItemIds() == null ) {
			Album album = mediaBiz.getSharedAlbum(command.getAlbumKey(), context);
			return exportAlbum(album.getAlbumId(), command.isDirect(),
					request, response, context);
		}
		return exportItems(command.getItemIds(), command.isDirect(),
				request, response, context);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.IOBiz#clearCacheFiles(magoffin.matt.ma2.domain.User, java.util.Set)
	 */
	public int clearCacheFiles(User user, final Set<MediaSize> ofSize) {
		if ( systemBiz.getCacheDirectory() == null ) {
			return 0;
		}
		File cacheDir = new File(systemBiz.getCacheDirectory(), 
				user.getUserId().toString());
		FilenameFilter filter = new FilenameFilter() {
			private Pattern pat = null;
			{
				if ( ofSize != null && ofSize.size() > 0 ) {
					StringBuilder buf = new StringBuilder();
					buf.append("^\\d+_(");
					for ( MediaSize size : ofSize ) {
						if ( buf.charAt(buf.length()-1) != '(' ) {
							buf.append('|');
						}
						buf.append(size.name());
					}
					buf.append(')');
					pat = Pattern.compile(buf.toString());
				}
			}
			public boolean accept(File dir, String name) {
				if ( pat == null ) {
					return true;
				}
				return pat.matcher(name).find();
			}
		};
		File[] cacheFiles = cacheDir.listFiles(filter);
		if ( cacheFiles != null ) {
			for ( File f : cacheFiles ) {
				f.delete();
			}
		}
		if ( log.isDebugEnabled() ) {
			log.debug("Deleted " +(cacheFiles == null ? 0 : cacheFiles.length) 
					+" cache files" +(ofSize == null ? "" : " matching sizes " 
					+ofSize));
		}
		return cacheFiles == null ? 0 : cacheFiles.length;
	}

	private class ExportZipArchive implements TwoPhaseExportRequest {
		
		private Album album;
		private Long[] itemIds;
		private BizContext context;
		private MediaRequest request;
		private MediaResponse response;
		private MediaItem currItem = null;
		private List<Long> processedItems = new LinkedList<Long>();
		private String exportMessage;
		private Long workTicket = null;
		private Set<String> zipNames = new LinkedHashSet<String>();
		
		private ExportZipArchive(Long[] itemIds, String exportMessage, 
				MediaRequest request, MediaResponse response, BizContext context) {
			this.request = request;
			this.response = response;
			this.itemIds = itemIds;
			this.exportMessage = exportMessage;
			this.context = context;
		}
		
		public void setMediaResponse(MediaResponse response) {
			this.response = response;
			if ( workTicket != null ) {
				getWorkBiz().workReadyNow(workTicket);
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
				messages.getMessage("export.items.message", 
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
		public void startWork() throws Exception {
			response.setMimeType(zipMimeType);
			final ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
			String zipPathFormat = (album == null 
				? "%s/%s" 
				: "%s/%0" +String.valueOf(itemIds.length).length() + "d_%s" );
			try {
				int itemCount = 0;
				for ( Long itemId : itemIds ) {
					itemCount++;
					final MediaItem item = mediaItemDao.get(itemId);
					currItem = item;
					
					// construct zip path from collection + item path
					String zipPath = null;
					if ( album == null ) {
						Collection col = getCollectionDao().getCollectionForMediaItem(
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
					ZipEntry entry = new ZipEntry(zipPath);
					zout.putNextEntry(entry);
					BasicMediaRequest itemRequest = new BasicMediaRequest(request);
					itemRequest.setMediaItemId(item.getItemId());
					exportSingleMediaItem(itemRequest, new MediaResponse() {
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
			} catch ( IOException e ) {
				throw new RuntimeException(e);
			} finally {
				if ( zout != null ) {
					try {
						zout.flush();
						zout.finish();
						zout.close();
					} catch ( IOException e ) {
						log.warn("IOException closing zip output stream: " +e);
					}
				}
			}
		}
	}

	private class ImportWorkRequest implements WorkRequest {
		
		private BizContext context;
		private AddMediaCommand command;
		private File collectionDir;
		private File srcFile;
		
		private float amountCompleted = 0;
		private int numProcessed = 0;
		private int numExported = 0;
		private List<String> errors = new LinkedList<String>();
		private URL collectionDirURL;
		private Collection collection;
		private List<MediaItem> itemsAddedToCollection = new LinkedList<MediaItem>();
		private List<Long> savedItemIdList = Collections.synchronizedList(
				new LinkedList<Long>());
		private User collectionOwner;
		private Map<String, Long> archiveAlbumPathMapping = new HashMap<String, Long>();
		
		private ImportWorkRequest(BizContext context, AddMediaCommand command,
				File collectionDir, File srcFile) {
			this.context = context;
			this.command = command;
			this.collectionDir = collectionDir;
			this.srcFile = srcFile;
		}
		
		public String getDisplayName() {
			return messages.getMessage(
					"import.media.work.displayName", null,
					"Importing media", context.getLocale());
		}

		public String getMessage() {
			if ( this.amountCompleted >= 0.5 ) {
				return messages.getMessage(
						"add.work.export.message", new Object[]{numExported,numProcessed},
						"Generating thumbnails", context.getLocale());
			}
			return messages.getMessage(
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
			collectionDirURL = collectionDir.toURL();
			
			collection = collectionDao.get(command.getCollectionId());
			collectionOwner = collection.getOwner();
			
			float numZipEntries = 0f;
			
			// 2: is this a zip archive?
			if ( command.getTempFile().getName().toLowerCase().endsWith(".zip") 
					|| zipContentTypes.contains(command.getTempFile().getContentType()) ) {

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
								&& IMPORT_MEDIA_XML_METADATA_NAME.equalsIgnoreCase(entry.getName())) {
							// load into DOM so can query with XPath later
							metadata = xmlHelper.getDocument(zipFile.getInputStream(entry));
							numZipEntries--;
							continue;
						}
						if ( shouldIgnoreZipResource(entry.getName()) ) {
							numZipEntries--;
							if ( log.isDebugEnabled() ) {
								log.debug("Ignoring zip resource [" +entry.getName() +']');
							}
							continue;
						}
					}
					if ( metadata != null && getMetadataSchemaResource() != null ) {
						getXmlHelper().validateXml(new DOMSource(metadata), getMetadataSchemaResource());
					}
					
					zipEnum = zipFile.entries();
					while ( zipEnum.hasMoreElements() ) {
						ZipEntry entry = zipEnum.nextElement();
						boolean validItem = false;
						try {
							String zipEntryName = entry.getName();
							if ( entry.isDirectory() || shouldIgnoreZipResource(zipEntryName) ) {
								continue;
							}
							File currOutputFile = new File(collectionDir,zipEntryName);
							currOutputFile.getParentFile().mkdirs();
							if ( log.isDebugEnabled() ) {
								log.debug("Unzipping file " +currOutputFile.getAbsolutePath());
							}
							FileCopyUtils.copy(zipFile.getInputStream(entry),
									new FileOutputStream(currOutputFile));
							MediaItem item = handleNewMediaItem(currOutputFile);
							if ( item != null ) {
								validItem = true;
								if ( metadata != null ) {
									handleMetadata(zipEntryName, item, metadata);
								} else if ( command.isAutoAlbum() ) {
									handleAutoAlbum(zipEntryName, item);
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
						log.warn("Unable to delete file [" +srcFile +"]");
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
			collectionDao.store(collection);
			this.numProcessed = itemsAddedToCollection.size();
			for ( MediaItem item : itemsAddedToCollection ) {
				// find out what the saved MediaItem IDs are via their paths
				MediaItem savedItem = mediaItemDao.getItemForPath(
						collection.getCollectionId(), item.getPath());
				Long savedItemId = savedItem.getItemId();
				
				this.savedItemIdList.add(savedItemId);
				
				// if caching enabled, generate normal thumbnail image now
				if ( systemBiz.getCacheDirectory() != null ) {
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
								log.warn("Ignoring invalid media size/quality MediaSpec "
										+thumbSpec.getSize() +"/" +thumbSpec.getQuality());
							}
						}
					}
					request.setQuality(thumbQuality);
					request.setSize(thumbSize);
					BasicMediaResponse response = new BasicMediaResponse();
					exportSingleMediaItem(request, response, 
							new ImportBizContext(collection));
					this.numExported++;
				}
				
				this.amountCompleted += 1f / (this.numProcessed * 2f);
			}
			
			this.amountCompleted = 1.0f;
		}

		@SuppressWarnings("unchecked")
		private void handleMetadata(String zipEntryName, MediaItem item, Document metadata) {
			// look for item via XPath
			String itemXPath = "//m:item[@archive-path=\"" 
				+escapeItemNameForXPath(zipEntryName) +"\"]";
			Element itemNode = (Element)getXmlHelper().evaluateXPath(
					metadata.getDocumentElement(), 
					itemXPath, XPathConstants.NODE);
			if ( itemNode == null ) {
				handleAutoAlbum(zipEntryName, item);
				return;
			}
			
			// handle album
			Element albumNode = (Element)itemNode.getParentNode();
			Album album = handleAutoAlbum(zipEntryName, item);
			if ( album != null ) {
				// Don't set album name... messes up auto album
				if ( !StringUtils.hasText(album.getComment()) ) {
					String comment = (String)getXmlHelper().evaluateXPath(albumNode, 
							"normalize-space(m:comment)", XPathConstants.STRING);
					if ( StringUtils.hasText(comment) ) {
						album.setComment(comment);
					}
				}
				if ( StringUtils.hasText(albumNode.getAttribute("creation-date")) ) {
					album.setCreationDate(parseDate(albumNode.getAttribute("creation-date")));
				}
				if ( StringUtils.hasText(albumNode.getAttribute("modify-date")) ) {
					album.setModifyDate(parseDate(albumNode.getAttribute("modify-date")));
				}
				if ( StringUtils.hasText(albumNode.getAttribute("album-date")) ) {
					album.setAlbumDate(parseDate(albumNode.getAttribute("album-date")));
				}
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
						userRating = getDomainObjectFactory().newMediaItemRatingInstance();
						userRating.setRatingUser(collectionOwner);
						userRating.setCreationDate(Calendar.getInstance());
						ratingList.add(userRating);
					}
					userRating.setRating((short)ratingValue);
				} catch ( Exception e ) {
					log.warn("Exception parsing rating: " +e.toString());
				}
			}
			
			// set description
			String comment = (String)getXmlHelper().evaluateXPath(itemNode, 
					"normalize-space(m:comment)", XPathConstants.STRING);
			if ( StringUtils.hasText(comment) ) {
				item.setDescription(comment);
			}

			// handle keywords
			String keywords = (String)getXmlHelper().evaluateXPath(itemNode, 
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
					tag = getDomainObjectFactory().newUserTagInstance();
					tag.setCreationDate(Calendar.getInstance());
					tag.setTaggingUser(collectionOwner);
					tagList.add(tag);
				}
				tag.setTag(keywords);
			}
			
			// handle metadata
			NodeList metaNodeList = (NodeList)getXmlHelper().evaluateXPath(itemNode,
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
						Metadata meta = getDomainObjectFactory().newMetadataInstance();
						meta.setKey(metaKey);
						meta.setValue(value);
						metaList.add(meta);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		private Album handleAutoAlbum(String zipEntryName, MediaItem item) {
			Album album = null;
			String[] albumNames = zipEntryName.split("/");
			if ( albumNames != null && albumNames.length > 1 ) {				
				String archiveAlbumPath = zipEntryName.substring(0, zipEntryName.lastIndexOf('/'));
				if ( archiveAlbumPathMapping.containsKey(archiveAlbumPath) ) {
					album = getAlbumDao().get(archiveAlbumPathMapping.get(archiveAlbumPath));
				} else {						
					// get the root album
					List<Album> albums = getAlbumDao().findAlbumsForUserAndName(
							collectionOwner.getUserId(), albumNames[0]);
					if ( albums.size() < 1 ) {
						// create new album
						album = getDomainObjectFactory().newAlbumInstance();
						album.setOwner(collectionOwner);
						album.setName(albumNames[0]);
						album = getAlbumDao().get(getMediaBiz().storeAlbum(
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
							childAlbum = getDomainObjectFactory().newAlbumInstance();
							childAlbum.setOwner(collectionOwner);
							childAlbum.setName(oneAlbumName);
							childAlbum = getAlbumDao().get(getMediaBiz().storeAlbum(
									childAlbum, context));
							album.getAlbum().add(childAlbum);
							getAlbumDao().store(album);
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
					getMediaBiz().sortAlbumItems(album);
					getAlbumDao().store(album);
				}
			}
			return album;
		}
		
		@SuppressWarnings("unchecked")
		private MediaItem handleNewMediaItem(File mediaFile) throws Exception {
			if ( !mediaBiz.isFileSupported(mediaFile) ) {
				this.errors.add(messages.getMessage(
						"media.not.supported",
						new Object[]{mediaFile.getName()},
						"The media file [" +mediaFile.getName() 
						+"] is not a supported type.",
						context.getLocale()));
				mediaFile.delete();
				if ( log.isDebugEnabled() ) {
					log.debug("ERROR importing media item: The media file [" 
							+mediaFile.getName() +"] is not a supported type.");
				}
				return null;
			}
			numProcessed++;
			MediaHandler handler = mediaBiz.getMediaHandler(mediaFile);
			MediaItem item = handler.createNewMediaItem(mediaFile);
			if ( item.getCreationDate() == null ) {
				item.setCreationDate(Calendar.getInstance());
			}
			item.setFileSize(mediaFile.length());
			item.setHits(0);
			
			// set the path using URLs so path normalized between OSes
			URL fileUrl = mediaFile.toURL();
			String path = fileUrl.toString().substring(collectionDirURL.toString().length());
			item.setPath(path);
			if ( item.getName() == null ) {
				item.setName(StringUtils.getFilename(path));
			}
			
			//item.setCollection(collection);
			
			// set the time zones
			if ( StringUtils.hasText(command.getMediaTz()) ) {
				item.setTz(systemBiz.getTimeZoneForCode(command.getMediaTz()));
			} else {
				item.setTz(collectionOwner.getTz());
			}
			if ( StringUtils.hasText(command.getLocalTz()) ) {
				item.setTzDisplay(systemBiz.getTimeZoneForCode(command.getLocalTz()));
			} else {
				item.setTzDisplay(collectionOwner.getTz());
			}
			
			// the item's date is in the server's local time zone
			// so adjust it to the specified media tz if available
			if ( item.getItemDate() != null ) {
				TimeZone mediaTz = TimeZone.getTimeZone(item.getTz().getCode());
				TimeZone displayTz = TimeZone.getTimeZone(item.getTzDisplay().getCode());
				if ( !mediaTz.equals(displayTz) ) {
					adjustItemDateTimeZone(item, mediaTz, displayTz);
				}
			}
			
			// see if this item is actually already saved, via the path
			MediaItem currItem = mediaItemDao.getItemForPath(
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

		private void adjustItemDateTimeZone(MediaItem item, TimeZone tz, 
				TimeZone displayTz) throws ParseException {
			// format as String, then re-parse in correct zone
			SimpleDateFormat sdf = new SimpleDateFormat(REPARSE_DATE_FORMAT);
			String dateStr = sdf.format(item.getItemDate().getTime());
			sdf.setTimeZone(tz);
			Calendar newDate = Calendar.getInstance(tz);
			newDate.setTime(sdf.parse(dateStr));
			
			// now format date into display TZ
			sdf.setTimeZone(displayTz);
			dateStr = sdf.format(newDate.getTime());
			newDate = Calendar.getInstance();
			sdf.setTimeZone(newDate.getTimeZone());
			newDate.setTime(sdf.parse(dateStr));
			
			
			item.setItemDate(newDate);
			if ( log.isDebugEnabled() ) {
				log.debug("Re-parsed date to [" 
						+sdf.format(newDate.getTime()) +"] in time zone ["
						+displayTz.getDisplayName() +"]");
			}
		}

		public float getAmountCompleted() {
			return amountCompleted;
		}
		
	}
	
	private boolean shouldIgnoreZipResource(String name) {
		if ( CollectionUtils.isEmpty(this.zipIgnorePatterns) ) {
			return false;
		}
		for ( Pattern pat : this.zipIgnorePatterns ) {
			if ( pat.matcher(name).find() ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return the zipIgnorePatterns
	 */
	public List<String> getZipIgnorePatterns() {
		List<String> pats = new LinkedList<String>();
		if ( !CollectionUtils.isEmpty(this.zipIgnorePatterns) ) {
			for ( Pattern pat : this.zipIgnorePatterns ) {
				pats.add(pat.pattern());
			}
		}
		return pats;
	}

	/**
	 * @param zipIgnorePatterns the zipIgnorePatterns to set
	 */
	public void setZipIgnorePatterns(List<String> zipIgnorePatterns) {
		List<Pattern> pats = new LinkedList<Pattern>();
		if ( !CollectionUtils.isEmpty(zipIgnorePatterns) ) {
			for ( String pat : zipIgnorePatterns ) {
				pats.add(Pattern.compile(pat));
			}
		}
		this.zipIgnorePatterns = pats;
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
	 * @return Returns the messages.
	 */
	public MessageSource getMessages() {
		return messages;
	}
	
	/**
	 * @param messages The messages to set.
	 */
	public void setMessages(MessageSource messages) {
		this.messages = messages;
	}
	
	/**
	 * @return Returns the workBiz.
	 */
	public WorkBiz getWorkBiz() {
		return workBiz;
	}
	
	/**
	 * @param workBiz The workBiz to set.
	 */
	public void setWorkBiz(WorkBiz workBiz) {
		this.workBiz = workBiz;
	}
	
	/**
	 * @return Returns the zipContentTypes.
	 */
	public Set<String> getZipContentTypes() {
		return zipContentTypes;
	}
	
	/**
	 * @param zipContentTypes The zipContentTypes to set.
	 */
	public void setZipContentTypes(Set<String> zipContentTypes) {
		this.zipContentTypes = zipContentTypes;
	}
	
	/**
	 * @return Returns the systemBiz.
	 */
	public SystemBiz getSystemBiz() {
		return systemBiz;
	}
	
	/**
	 * @param systemBiz The systemBiz to set.
	 */
	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}
	
	/**
	 * @return the fileTypeMap
	 */
	public FileTypeMap getFileTypeMap() {
		return fileTypeMap;
	}
	
	/**
	 * @param fileTypeMap the fileTypeMap to set
	 */
	public void setFileTypeMap(FileTypeMap fileTypeMap) {
		this.fileTypeMap = fileTypeMap;
	}
	
	/**
	 * @return the albumDao
	 */
	public AlbumDao getAlbumDao() {
		return albumDao;
	}
	
	/**
	 * @param albumDao the albumDao to set
	 */
	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
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
	 * @return the xmlHelper
	 */
	public XmlHelper getXmlHelper() {
		return xmlHelper;
	}
	
	/**
	 * @param xmlHelper the xmlHelper to set
	 */
	public void setXmlHelper(XmlHelper xmlHelper) {
		this.xmlHelper = xmlHelper;
	}
	
	/**
	 * @return the metadataSchemaResource
	 */
	public Resource getMetadataSchemaResource() {
		return metadataSchemaResource;
	}
	
	/**
	 * @param metadataSchemaResource the metadataSchemaResource to set
	 */
	public void setMetadataSchemaResource(Resource metadataSchemaResource) {
		this.metadataSchemaResource = metadataSchemaResource;
	}
	
	/**
	 * @return the xmlDateFormat
	 */
	public ThreadSafeDateFormat getXmlDateFormat() {
		return xmlDateFormat;
	}
	
	/**
	 * @param xmlDateFormat the xmlDateFormat to set
	 */
	public void setXmlDateFormat(ThreadSafeDateFormat xmlDateFormat) {
		this.xmlDateFormat = xmlDateFormat;
	}
	
	/**
	 * @return the xmlDateTimeFormat
	 */
	public ThreadSafeDateFormat getXmlDateTimeFormat() {
		return xmlDateTimeFormat;
	}
	
	/**
	 * @param xmlDateTimeFormat the xmlDateTimeFormat to set
	 */
	public void setXmlDateTimeFormat(ThreadSafeDateFormat xmlDateTimeFormat) {
		this.xmlDateTimeFormat = xmlDateTimeFormat;
	}

	/**
	 * @return the zipMimeType
	 */
	public String getZipMimeType() {
		return zipMimeType;
	}

	/**
	 * @param zipMimeType the zipMimeType to set
	 */
	public void setZipMimeType(String zipMimeType) {
		this.zipMimeType = zipMimeType;
	}

}
