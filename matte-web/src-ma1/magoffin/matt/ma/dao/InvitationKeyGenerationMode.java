/* ===================================================================
 * InvitationKeyGenerationMode.java
 * 
 * Created Jan 21, 2004 2:41:27 PM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: InvitationKeyGenerationMode.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import java.sql.Connection;
import java.util.Date;

import magoffin.matt.dao.DataObject;
import magoffin.matt.gerdal.dataobjects.TableMetaData;
import magoffin.matt.ma.xsd.Invitation;

/**
 * GeRDAL GenerationMode for invitation keys.
 * 
 * <p>The key is a SHA message digest of a concatination of the:</p>
 * 
 * <ul>
 * <li>email</li>
 * <li>current time</li>
 * <li>random double</li>
 * </ul>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class InvitationKeyGenerationMode
extends AbstractAnonKeyGenerationMode 
{
	/** The salt used to digest the user key. */
	private final static byte[] SALT = new byte[0];

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dataobjects.TableMetaData.GenerationMode#getRecognizedOptionTypes()
 */
public String[] getRecognizedOptionTypes() {
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dataobjects.TableMetaData.GenerationMode#generate(magoffin.matt.gerdal.dataobjects.TableMetaData, java.lang.String, magoffin.matt.dao.DataObject, java.sql.Connection)
 */
public Object generate(
	TableMetaData meta,
	String columnAlias,
	DataObject row,
	Connection conn)
	throws Exception 
{
	if ( !(row instanceof Invitation) ) {
		throw new Exception("Class " +(row == null ? "(null)" : row.getClass().getName())
				+" not supported.");
	}
	
	Invitation invite = (Invitation)row;
	String key = invite.getEmail()+new Date().toString()+Math.random();
	
	return generateKey(key,SALT);
}

}
