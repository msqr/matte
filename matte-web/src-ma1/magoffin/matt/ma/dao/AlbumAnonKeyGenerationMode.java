/* ===================================================================
 * AlbumAnonKeyGenerationMode.java
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
 * $Id: AlbumAnonKeyGenerationMode.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import java.sql.Connection;
import java.util.Date;

import magoffin.matt.dao.DataObject;
import magoffin.matt.gerdal.dataobjects.TableMetaData;
import magoffin.matt.ma.xsd.Album;

/**
 * GeRDAL GenerationMode for anonymous album keys.
 * 
 * <p>The key is a SHA message digest of a concatination of the:</p>
 * 
 * <ul>
 * <li>album ID</li>
 * <li>album name</li>
 * <li>current time multiplied by a random double</li>
 * </ul>
 * 
 * <p>Created Nov 8, 2002 5:23:49 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class AlbumAnonKeyGenerationMode extends AbstractAnonKeyGenerationMode 
{
	
	/** The salt used to digest the album key. */
	private final static byte[] SALT = new byte[0]; //{12,6,36,4,32,2,3,93,28,19,74};

/**
 * Constructor for AlbumAnonKeyGenerationMode.
 */
public AlbumAnonKeyGenerationMode() {
	super();
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dataobjects.TableMetaData.GenerationMode#getRecognizedOptionTypes()
 */
public String[] getRecognizedOptionTypes() {
	return null;
}


/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dataobjects.TableMetaData.GenerationMode#generate(magoffin.matt.gerdal.dataobjects.TableMetaData, java.lang.String, magoffin.matt.gerdal.dataobjects.TableData, java.sql.Connection)
 */
public Object generate(
	TableMetaData meta,
	String columnAlias,
	DataObject row,
	Connection conn)
throws Exception 
{
	if ( !(row instanceof Album) ) {
		throw new Exception("Class " +(row == null ? "(null)" : row.getClass().getName())
			+" not supported.");
	}
	
	Album album = (Album)row;
	String key = String.valueOf(album.getAlbumId())+album.getName()+(new Date().toString())+Math.random();
	
	return generateKey(key,SALT);
	
}

}
