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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;

import magoffin.matt.ma2.MediaHandler;
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
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.support.ExportItemsCommand;
import magoffin.matt.ma2.util.XmlHelper;
import magoffin.matt.util.SimpleThreadSafeDateFormat;
import magoffin.matt.util.ThreadSafeDateFormat;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

/**
 * Implementation of IOBiz.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class IOBizImpl implements IOBiz {
	
	/** The default property for the {@link #getZipMimeType()} property. */
	public static final String DEFAULT_ZIP_MIME_TYPE = "application/zip";
	
	/** A date format pattern for re-parsing dates across time zones. */
	static final String REPARSE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

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
	private Resource metadataSchemaResource = null;
	private ThreadSafeDateFormat xmlDateFormat
		= new SimpleThreadSafeDateFormat("yyyy-MM-dd");
	private ThreadSafeDateFormat xmlDateTimeFormat
		= new SimpleThreadSafeDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private String zipMimeType = DEFAULT_ZIP_MIME_TYPE;
	private List<Pattern> zipIgnorePatterns;
	
	final Logger log = Logger.getLogger(IOBizImpl.class);
	
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
			new ImportWorkRequest(this, context, command, collectionDir, srcFile));
	}
	
	Calendar parseDate(String dateStr) {
		if ( dateStr.indexOf('T') != -1 ) {
			return xmlDateTimeFormat.parseCalendar(dateStr.substring(0,19));
		}
		return xmlDateFormat.parseCalendar(dateStr.substring(0, 10));
	}
	
	String escapeItemNameForXPath(String string) {
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
	
	void exportSingleMediaItem(MediaRequest request, MediaResponse response, BizContext context) {
		MediaItem item = this.mediaItemDao.get(request.getMediaItemId());
		if ( item == null ) {
			throw new ObjectNotFoundException("Item ["
					+request.getMediaItemId() +"] not available");
		}
		MediaHandler handler = this.mediaBiz.getMediaHandler(item.getMime());
		String responseFilename = getResponseFilename(item, request, handler);
		response.setFilename(responseFilename);
		if ( !request.isOriginal() && !item.isUseIcon() ) {
			
			File cacheFile = null;
			if ( systemBiz.getCacheDirectory() != null ) {
				// see if already cached and can return that
				Collection col = context instanceof ImportBizContext
					? ((ImportBizContext)context).getImportCollection()
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
						public void setFilename(String filename) {
							// no need
						}
						public void setPartialResponse(long start, long end,
								long total) {
							// no need
						}
						public boolean hasOutputStream() {
							return true;
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
		//if ( response.hasOutputStream() ) {
			// no cache file to be used, so handle without
			response.setItem(item);
			handler.handleMediaRequest(item,request,response);
		//}
		return;
	}

	private String getResponseFilename(MediaItem item, MediaRequest request, MediaHandler handler) {
		String name = item.getName().toLowerCase();
		String extension = '.'+handler.getFileExtension(item, request);
		if ( name != null && !name.endsWith(extension) ) {
			name = item.getName() +extension;
		} else {
			name = item.getName();
		}
		return name;
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
			if ( response.hasOutputStream() ) {
				try {
					FileCopyUtils.copy(new BufferedInputStream(
							new FileInputStream(cacheFile)), response.getOutputStream());
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
			}
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
		ExportZipArchive export = new ExportZipArchive(this, itemIds, msg, 
				request, response, context);
		export.setAlbum(album);
		WorkInfo info = workBiz.submitWork(export);
		if ( !direct ) {
			export.setWorkTicket(info.getTicket());
		}
		return info;
	}

	private WorkInfo exportItems(Long[] itemIds, boolean direct, 
			MediaRequest request, MediaResponse response, BizContext context) {
		String msg = messages.getMessage("export.displayName", null, context.getLocale())
			+" " +messages.getMessage("items", null, context.getLocale());
		ExportZipArchive export = new ExportZipArchive(this, itemIds, msg, 
				request, response, context);
		WorkInfo info = workBiz.submitWork(export);
		if ( !direct ) {
			export.setWorkTicket(info.getTicket());
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

	boolean shouldIgnoreZipResource(String name) {
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
