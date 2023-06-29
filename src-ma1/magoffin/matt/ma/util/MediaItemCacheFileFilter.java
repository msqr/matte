/* ===================================================================
 * MediaItemCacheFileFilter.java
 * 
 * Created Jul 21, 2004 2:23:33 PM
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
 * $Id: MediaItemCacheFileFilter.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.io.File;
import java.io.FileFilter;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.xsd.MediaItem;

/**
 * Filter to find cache files for a given media item.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaItemCacheFileFilter implements FileFilter 
{
	public MediaItem item;
	public MediaRequestHandler handler;

public MediaItemCacheFileFilter(MediaItem item, MediaRequestHandler handler)
throws MediaAlbumException
{
	this.item = item;
	this.handler = handler;
}
	
/* (non-Javadoc)
 * @see java.io.FileFilter#accept(java.io.File)
 */
public boolean accept(File f) {
	return handler.isCacheKey(item,f.getName());
}

}
