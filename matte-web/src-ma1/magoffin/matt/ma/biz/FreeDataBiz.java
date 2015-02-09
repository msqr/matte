/* ===================================================================
 * FreeDataBiz.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 25, 2004 9:21:31 AM.
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
 * $Id: FreeDataBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.FreeDataKind;

/**
 * Biz interface for FreeData maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface FreeDataBiz extends Biz 
{

/**
 * Get all FreeDataKind objects available.
 * 
 * @param allowCached if <em>true</em> then allow cached objects to be used
 * @return array of FreeDataKind (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 */
public FreeDataKind[] getAllFreeDataKinds(boolean allowCached) 
throws MediaAlbumException;	

/**
 * Populate the <code>dataTypeName</code> attribute of a set of FreeData objects.
 * @param data the FreeData to populate
 * @param allowCached if <em>true</em> then allow cached objects to be used
 * @throws MediaAlbumException if an error occurs
 */
public void populateFreeDataTypeNames(FreeData[] data, boolean allowCached)
throws MediaAlbumException;

}