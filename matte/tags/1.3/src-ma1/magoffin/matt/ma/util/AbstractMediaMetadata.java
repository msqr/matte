/* ===================================================================
 * AbstractMediaMetadata.java
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
 * $Id: AbstractMediaMetadata.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.util.Date;

import magoffin.matt.ma.MediaMetadata;

/**
 * A base class for MediaMetadata implementations.
 * 
 * <p> Created on Dec 9, 2002 6:06:56 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractMediaMetadata implements MediaMetadata {

	protected Date creationDate = null;

/**
 * @see magoffin.matt.ma.MediaMetadata#getCreationDate()
 */
public Date getCreationDate() 
{
	return creationDate;
}

/**
 * Sets the creation date.
 * 
 * @param creationDate the creation date to set
 */
public void setCreationDate(Date creationDate) 
{
	this.creationDate = creationDate;
}

} // class AbstractMediaMetadata
