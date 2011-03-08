/* ===================================================================
 * MediaMetadata.java
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
 * $Id: MediaMetadata.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

import java.util.Date;

/**
 * Metadata about a media item.
 * 
 * <p>Any value that is unknown should return <em>null</em>.</p>
 * 
 * <p> Created on Dec 9, 2002 5:02:15 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public interface MediaMetadata 
{
	
/**
 * Get the date the item was created.
 * 
 * @return Date
 */
public Date getCreationDate();


/**
 * Serialize the meta data into a String.
 * 
 * <p>The implementation must be able to deserialize itself from this String
 * produced by this method, using the {@link #deserializeFromString(String)}
 * method.</p>
 * 
 * @return String
 */
public String serializeToString();


/**
 * Deserialize the meta data from a String.
 * 
 * <p>The input <var>s</var> will be the result of calling the 
 * {@link #serializeToString()} method. The method should not assume any data
 * to be present. For data not found in <var>s</var> the corresponding field
 * should be treated as <em>null</em>.</p>
 * 
 * @param s
 */
public void deserializeFromString(String s);

}
