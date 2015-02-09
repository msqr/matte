/* ===================================================================
 * MediaResponse.java
 * 
 * Copyright (c) 200304 Matt Magoffin. Created Mar 2, 2003.
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
 * $Id: MediaResponse.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

import magoffin.matt.ma.xsd.MediaItem;

/**
 * Interface to allow response values during a media request.
 * 
 * <p>Created Mar 2, 2003 8:12:06 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public interface MediaResponse 
{

/** 
 * Set the MIME type of a response. 
 * 
 * @param mime the MIME type of the response
 */
public void setMimeType(String mime);

/** 
 * Set the length of the media of the response. 
 * @param length the length of the content
 */
public void setMediaLength(long length);

/**
 * Set the modification date of the response.
 * 
 * <p>This method is to aid client caching.</p>
 * 
 * @param date the modification date
 */
public void setModifiedDate(long date);

/** 
 * Reset the state of this response.
 * 
 * <p>This method must be implemented so the MediaResponse
 * instance can be re-used.</p>
 */
public void reset();

/**
 * Set the media item used in the response.
 * 
 * @param item the media item
 */
public void setItem(MediaItem item);

}
