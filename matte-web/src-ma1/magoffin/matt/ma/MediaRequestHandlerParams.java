/* ===================================================================
 * MediaRequestHandlerParams.java
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
 * $Id: MediaRequestHandlerParams.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

import magoffin.matt.ma.biz.WorkBiz;

/**
 * API for passing parameters to a MediaRequestHandler instance.
 * 
 * <p>Created Sep 29, 2002 4:51:07 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public interface MediaRequestHandlerParams 
{
	/** Option param key for size: <code>s</code> */
	public static final String SIZE = "s";
	
	/** The SIZE parameter value prefix to signal a thumbnail size: <code>t</code> */
	public static final String THUMBNAIL_SIZE_PREFIX = "t";
	
	/** Option param key for compression: <code>c</code>. */
	public static final String COMPRESSION = "c";

	/** Optional internal param key for the input file. */
	public static final String INPUT_FILE = "_in_file";
	
	/** Optional internal param key for the output (cache) file. */
	public static final String OUTPUT_FILE = "_out_file";
	
	/** Optional param key for watermark, value should be a local file path. */
	public static final String WATERMARK = "wmark";
	
	/** Optional param key for watermark parameter, value can be anything. */
	public static final String WATERMARK_PARAM = "wparam";
	
	/** Parameter to indicate that the original media item is desired. */
	public static final String WANT_ORIGINAL = "_want_original";
	
	/** The request's locale. */
	public static final String LOCALE = "_locale";
	
/**
 * Return a list of supported parameter names.
 * 
 * <p><strong>Note:</strong> the names should be sorted with
 * {@link java.util.Arrays#sort(java.lang.Object[])}.</p>
 * 
 * @return array of parameter names
 */
public String[] getSupportedParamNames();



/**
 * Return an array of param names that are only suitable for
 * users with administration access.
 * 
 * <p><strong>Note:</strong> the names should be sorted with
 * {@link java.util.Arrays#sort(java.lang.Object[])}.</p>
 *  * @return String[] (may be <em>null</em>) */
public String[] getAdminOnlyParamNames();


/**
 * Set a parameter value.
 *  * @param name the parameter name to set * @param value the parameter value to get */
public void setParam(String name, Object value);


/**
 * Get a parameter value, or <em>null</em> if not defined.
 *  * @param name the parameter value to get * @return Object the parameter value, or <em>null</em> if not set */
public Object getParam(String name);


/**
 * Tell if any custom params have been set on this object.
 * 
 * @return <em>true</em> if any parameters have been set
 */
public boolean hasParamsSet();


/**
 * Tell if one specific param has been set on this object.
 *  * @param name the param name to check * @return <em>true</em> if the param <var>name</var> is non-null. */
public boolean hasParamSet(String name);


/**
 * Set the work biz.
 * 
 * @param workBiz */
public void setWorkBiz(WorkBiz workBiz);


/**
 *  Get the work biz.
 *  * @return WorkBiz */
public WorkBiz getWorkBiz();

}
