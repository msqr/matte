/* ===================================================================
 * BasicFileMediaRequestHandler.java
 * 
 * Created Jul 18, 2004 5:33:46 PM
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
 * $Id: BasicFileMediaRequestHandler.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.iio.PngMediaRequestHandler;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaHandlerConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemMetadata;
import magoffin.matt.util.FileUtil;
import magoffin.matt.util.StringUtil;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;


/**
 * Base media handler for generic files.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class BasicFileMediaRequestHandler implements MediaRequestHandler 
{
	private static final Logger LOG = Logger.getLogger(BasicFileMediaRequestHandler.class);
	private ObjectPool MRHP_POOL = null;
	private Map ICON_FILE_CACHE = null;
	private File baseIconDir = null;

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#init(magoffin.matt.ma.xsd.MediaHandlerConfig, magoffin.matt.ma.util.PoolFactory)
 */
public void init(MediaHandlerConfig config, PoolFactory pf, MediaAlbumConfig appConfig) throws MediaAlbumException 
{
	MRHP_POOL = pf.getPoolInstance(BasicFileMediaRequestHandlerParams.class);
	ICON_FILE_CACHE = new HashMap();
	File www = MediaAlbumConfigUtil.getBaseWWWDir();
	if ( www == null ) {
		throw new MediaAlbumException("Base WWW dir not available.");
	}
	www = new File(www,"img");
	if ( !www.exists() || !www.isDirectory() ) {
		LOG.error("Unable to find web 'img' directory " +www.getAbsolutePath());
		throw new MediaAlbumException("Unable to configure icon image directory.");
	}
	baseIconDir = new File(www,"icons");
	if ( !baseIconDir.exists() || !baseIconDir.isDirectory() ) {
		LOG.error("Unable to find web 'icons' directory " +baseIconDir.getAbsolutePath());
		throw new MediaAlbumException("Unable to configure icon image directory.");
	}
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getCacheKey(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public String getCacheKey(MediaItem item, MediaRequestHandlerParams params) 
{
	return null; // no cache
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#isCacheKey(magoffin.matt.ma.xsd.MediaItem, java.lang.String)
 */
public boolean isCacheKey(MediaItem item, String key) {
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getMetadataForItem(magoffin.matt.ma.xsd.MediaItem)
 */
public MediaItemMetadata[] getMetadataForItem(MediaItem item) 
{
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getOutputMime(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public String getOutputMime(MediaItem item, MediaRequestHandlerParams params) 
{
	// if want original, use item MIME, otherwise we're returning icon, so 
	// use PNG image MIME
	if ( params.hasParamSet(MediaRequestHandlerParams.WANT_ORIGINAL) ) {
		return item.getMime();
	}
	return PngMediaRequestHandler.PNG_MIME;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getParamInstance()
 */
public MediaRequestHandlerParams getParamInstance() {
	try {
		return (BasicFileMediaRequestHandlerParams)MRHP_POOL.borrowObject();
	} catch ( Exception e ) {
		LOG.error("Unable to get pooled FileMediaRequestHandlerParams object: " +e.getMessage(),e);
	}
	return new BasicFileMediaRequestHandlerParams();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#postProcessParams(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void postProcessParams(MediaItem item, MediaRequestHandlerParams params) 
{
	try {
		// return the params to the pool
		MRHP_POOL.returnObject(params);
	} catch ( Exception e ) {
		LOG.error("Unable to return MediaRequestHandlerParams object to pool:" +e.getMessage(),e);
	}	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getOutputGeometry(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public Geometry getOutputGeometry(MediaItem item,
		MediaRequestHandlerParams params) 
{
	Geometry geo = new Geometry(
			ApplicationConstants.ICON_WIDTH.intValue(),
			ApplicationConstants.ICON_HEIGHT.intValue());
	return geo;
}
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#preProcessParams(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void preProcessParams(MediaItem item, MediaRequestHandlerParams params) 
{
	// nothing to do here
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#setMediaItemParameters(java.io.File, magoffin.matt.ma.xsd.MediaItem)
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item)
throws MediaAlbumException 
{
	item.setWidth(ApplicationConstants.ICON_WIDTH);
	item.setHeight(ApplicationConstants.ICON_WIDTH);
	item.setUseIcon(Boolean.TRUE);
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#useStreamsForWrite()
 */
public boolean useStreamsForWrite() {
	return true;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#writeMedia(magoffin.matt.ma.xsd.MediaItem, java.io.OutputStream, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void writeMedia(MediaItem item, OutputStream out, InputStream in,
		MediaRequestHandlerParams params) throws MediaAlbumException,
		IOException 
{
	if ( !params.hasParamSet(MediaRequestHandlerParams.WANT_ORIGINAL) ) {
		// return the icon
		writeFileIcon(item,out,params);
		return;
	}

	// want original, simply stream the original file back, no need to alter
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Returning unaltered file " +item.getPath());
	}
	FileUtil.copy(in,out,false,false);
}

/**
 * Write an icon for a media item file.
 * 
 * @param item the item
 * @param out the output stream
 * @param params the current request params
 */
protected void writeFileIcon(MediaItem item, OutputStream out, MediaRequestHandlerParams params) 
throws MediaAlbumException
{
	File iconFile = null;
	String mime = item.getMime();
	if ( ICON_FILE_CACHE.containsKey(mime) ) {
		iconFile = (File)ICON_FILE_CACHE.get(mime);
	} else {
		synchronized (ICON_FILE_CACHE) {
			if ( ICON_FILE_CACHE.containsKey(mime) ) {
				iconFile = (File)ICON_FILE_CACHE.get(mime);
			} else {
				// first try complete mime name, with '/' replaced by _
				String currMime = StringUtil.replace(mime,"/","_");
				File f = new File(baseIconDir,currMime+".png");
				if ( !f.exists() ) {
					// try with only first part of mime
					currMime = StringUtil.substringBefore(mime,'/');
					f = new File(baseIconDir,currMime+".png");
					if ( !f.exists() ) {
						// resort to "unknown"
						f = new File(baseIconDir,"unknown.png");
					}
				}
				iconFile = f;
				ICON_FILE_CACHE.put(mime,iconFile);
			}
		}
	}
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Returning file icon " +iconFile.getPath());
	}
	try {
		FileUtil.slurp(iconFile,out);
	} catch ( IOException e ) {
		LOG.error("IOException returning file icon " +iconFile.getPath()
				+": " +e.toString() );
		throw new MediaAlbumException("Unable to return file icon: "
				+e.getMessage());
	}
}

/**
 * Create a new MediaItemMetadata object and add it to a list.
 * 
 * @param list the list to add the new MediaItemMetadata object too
 * @param key the key for the meta data
 * @param value the value of the meta data
 */
protected void addMediaItemMetadata(List list, String key, Object value) {
	if ( value == null ) return;
	MediaItemMetadata meta = new MediaItemMetadata();
	meta.setKey(key);
	meta.setContent(value.toString());
	list.add(meta);
}

}
