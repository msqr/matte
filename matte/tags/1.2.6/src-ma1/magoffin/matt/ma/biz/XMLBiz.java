/* ===================================================================
 * XMLBiz.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 17, 2004 4:54:50 PM.
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
 * $Id: XMLBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import org.w3c.dom.Document;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;

/**
 * Biz interface for XML functions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface XMLBiz extends Biz {
	
	/** 
	 * Config key for boolean marshalling validation setting:
	 * <code>xml.marshall.validate</code>
	 */
	public static final String CONFIG_VALIDATE_ON_MARSHALL = "xml.marshall.validate";

/**
 * Get an XML Document instance.
 * 
 * @return new Document instance
 * @throws MediaAlbumException if an error occurs
 */
public Document getDocument() throws MediaAlbumException;
	
/**
 * Marshall an object to an XML DOM.
 * 
 * @param o the object to marshall
 * @return the DOM object
 * @throws MediaAlbumException if an error occurs
 */
public Document marshallToDocument(Object o) throws MediaAlbumException;
	
}
