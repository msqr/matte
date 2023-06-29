/* ===================================================================
 * SAX1toDOMPoolableFactory.java
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
 * $Id: SAX1toDOMPoolableFactory.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.log4j.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Poolable factory for SAX1toDOM objects.
 * 
 * <p>Created Oct 13, 2002 1:50:00 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class SAX1toDOMPoolableFactory extends BasePoolableObjectFactory
{
	
	private static final Logger log = Logger.getLogger(SAX1toDOMPoolableFactory.class);
	
	private SAXParser saxParser = null;
	
/**
 * Constructor for SAX1toDOMPoolableFactory.
 */
public SAX1toDOMPoolableFactory() {
	super();
	SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	saxParserFactory.setNamespaceAware(true);
	try {
		saxParser = saxParserFactory.newSAXParser();
	} catch (Exception e) {
		log.error("Unable to create SAXParser: " +e.getMessage(),e);
	}
}

/**
 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
 */
public Object makeObject() throws Exception {
	log.debug("Creating new SAX1toDOM instance for pool.");
	return new SAX1toDOM(saxParser.getParser(),true);
}

/**
 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
 */
public void passivateObject(Object o) throws Exception {
	((SAX1toDOM)o).reset();	
}

} // class SAX1toDOMPoolableFactory
