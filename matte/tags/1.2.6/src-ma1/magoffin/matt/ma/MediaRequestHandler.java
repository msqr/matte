/* ===================================================================
 * MediaRequestHandler.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: MediaRequestHandler.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */
 
package magoffin.matt.ma;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaHandlerConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemMetadata;

/**
 * Interface for handling media requests.
 * 
 * <p>The job of implementations of this interface is to be able
 * to generate an output stream from an input stream, given
 * implementation-specific parameters. For example, a handler
 * implementation might know how to read JPEG image files, 
 * and scale them to arbitrary sizes and/or re-compress them.</p>
 * 
 * <p>This interface hides most of the details of how the media
 * request was made or where the resulting output stream goes, 
 * and provides a way for implementations to
 * specify what request parameters they support via the 
 * {@link magoffin.matt.ma.MediaRequestHandlerParams} 
 * interface.</p>
 * 
 * <p>Created Sep 29, 2002 4:42:05 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public interface MediaRequestHandler 
{

/**
 * Initialize this MediaRequestHandler.
 * 
 * <p>If there are any problems initializing the instance, it should
 * throw a MediaAlbumException.</p>
 * @param config the MediaHandlerConfig for this handler
 * @param pf the media request pool factory
 * @param appConfig the application config
 * 
 * @throws MediaAlbumException if an error occurs
 */
public void init(MediaHandlerConfig config, PoolFactory pf, MediaAlbumConfig appConfig) throws MediaAlbumException;

/**
 * Set values on a media item object from a given input stream.
 * 
 * <p>This method should examine the given input stream and set
 * as many fields of the given MediaItem according to what is found
 * on the input stream. For example, if the width and height can be 
 * determined from the input stream, then this method should call
 * {@link MediaItem#setWidth(java.lang.Integer)} and 
 * {@link MediaItem#setHeight(java.lang.Integer)} with appropriate
 * values.</p>
 * 
 * <p>It should also attempt to extract any meta information from the media item
 * possible, and return this in the form of a MediaMetadata object. This meta
 * data will be then stored in the back end with the rest of the media
 * information.</p>
 *  * @param mediaFile the MediaItem file
 * @param item the media item to update
 * @return a populated MediaMetadata object for the media item, or <em>null</em>
 * if unable to create one
 * @throws MediaAlbumException if an error occurs
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item) throws MediaAlbumException;


/**
 * Get an instance of MediaRequestHandlerParams suitable for this
 * MediaRequestHandler.
 * 
 * <p>You should assume the returned object has been borrowed from a pool,
 * and thus must <em>always</em> call {@link #postProcessParams(MediaItem, MediaRequestHandlerParams)}
 * when finished with this object as that method is responsible for handling
 * any clean up tasks such as returning this borrowed object to the pool.</p>
 * 
 * @return MediaRequestHandlerParams instance
 */
public MediaRequestHandlerParams getParamInstance();


/**
 * Perform any processing on the params for the specific media item, at the
 * beginning of the request cycle.
 *  * @param item the item for the request * @param params the params for the reqeust */
public void preProcessParams(MediaItem item, MediaRequestHandlerParams params);


/**
 * Perform any processing on the params for the specific media item, at the
 * end of the request cycle.
 * 
 * <p>This method should be called every time (once done with this object) 
 * after calling the {@link #getParamInstance()} method. The calling implementation 
 * must guarantee not to make any more use of <var>params</var> after calling this 
 * method.</p>
 *  * @param item the media item * @param params the params instance */
public void postProcessParams(MediaItem item, MediaRequestHandlerParams params);


/**
 * Get a cache key for a media request, if the item can be cached.
 * 
 * <p>If the specified item should not be cached by the server (e.g. the request
 * is for the media in it's unaltered state) then this method should return
 * <em>null</em>. Otherwise, it must return a key which must be unique for the
 * combination of the specific <var>item</var> combined with the specific 
 * <var>params</var>.</p>
 *  * @param item the media item * @param params the params to apply to the media item * @return a unique cache key for <var>item</var> with <var>params</var> applied */
public String getCacheKey(MediaItem item, MediaRequestHandlerParams params);

/**
 * Return <em>true</em> if <var>key</var> is likely a cache key for a given item.
 * 
 * <p>This method will return <em>true</em> if the <var>key</var> value 
 * appears to be a cache key for <var>item</var>. It is only a guess, however, 
 * as this method operates without a MediaRequestHandlerParams object 
 * which is required to generate a true key.</p>
 * 
 * @param item the media item
 * @param key the key
 * @return <em>true</em> if the key appears to be for the item
 */
public boolean isCacheKey(MediaItem item, String key);

/**
 * Get the MIME type that will result from the request for a media item.
 * 
 * @param item the media item
 * @param params the request parameters
 * @return String
 */
public String getOutputMime(MediaItem item, MediaRequestHandlerParams params);


/**
 * Get the output geometry for an item with a given param instance.
 * 
 * @param item the media item
 * @param params the request params
 * @return a Geometry object
 */
public Geometry getOutputGeometry(MediaItem item, MediaRequestHandlerParams params);


/**
 * Decode an image from an input stream and write it to an output stream
 * given the supplied size contraints and parameters.
 *  * @param item the media item to write
 * @param out the output stream * @param in the input stream * @param params the image parameters * @throws MediaAlbumException if an error occurs
 * @throws IOException
 */
public void writeMedia(MediaItem item, OutputStream out, InputStream in, MediaRequestHandlerParams params)
throws MediaAlbumException, IOException;


/**
 * Get a metadata object from a MediaItem.
 * 
 * <p>This method should return <em>null</em> if the meta data can not be
 * created.</p>
 * 
 * @param item the MediaItem to extract the metadata from
 * @return MediaMetadata, or <em>null</em> if none available
 */
public MediaItemMetadata[] getMetadataForItem(MediaItem item);

/**
 * Flag to tell if request handler uses streams, or works directly
 * with files.
 * 
 * <p>If <em>false</em> then the in/out streams passed to {@link #writeMedia(MediaItem, OutputStream, InputStream, MediaRequestHandlerParams)}
 * will not be opened when passed in, nor will they be flushed / closed automatically.
 * The application will instead pass in/out File objects as request handler params,
 * and read from the output file as necessary.</p>
 * 
 * <p>If <em>true</em> then the application will open both the input and output streams
 * before calling {@link #writeMedia(MediaItem, OutputStream, InputStream, MediaRequestHandlerParams)}
 * and then close them when completed.</p>
 * 
 * @return boolean
 */
public boolean useStreamsForWrite();

}
