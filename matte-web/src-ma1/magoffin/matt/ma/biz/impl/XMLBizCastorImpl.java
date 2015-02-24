/* ===================================================================
 * XMLBizImpl.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 17, 2004 4:59:57 PM.
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
 * $Id: XMLBizCastorImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.biz.XMLBiz;
import magoffin.matt.ma.util.SAX1toDOM;
import magoffin.matt.ma.util.SAX1toDOMPoolableFactory;
import magoffin.matt.util.config.Config;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;
import org.w3c.dom.Document;

/**
 * Implementation of XMLBiz interface using Castor XML.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class XMLBizCastorImpl extends AbstractBiz implements XMLBiz 
{
	private static final Logger LOG = Logger.getLogger(XMLBizCastorImpl.class);

	private DocumentBuilderFactory docFactory = null; 
	private DocumentBuilder docBuilder = null;
	private ObjectPool sax1toDomObjectPool = null;
	private boolean validateMarshall = true;
	
/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) 
{
	super.init(initializer);
	docFactory = DocumentBuilderFactory.newInstance();
	try {
		docBuilder = docFactory.newDocumentBuilder();
	} catch ( ParserConfigurationException e ) {
		throw new MediaAlbumRuntimeException("Unable to get JAXP DocumentBuilder",e);
	}
	sax1toDomObjectPool = new StackObjectPool(new SAX1toDOMPoolableFactory(),8,4);
	validateMarshall = Config.getBoolean(ApplicationConstants.CONFIG_ENV,
			CONFIG_VALIDATE_ON_MARSHALL,true);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.XMLBiz#getDocument()
 */
public Document getDocument() throws MediaAlbumException {
	return docBuilder.newDocument();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.XMLBiz#marshallToDocument(java.lang.Object)
 */
public Document marshallToDocument(Object o) throws MediaAlbumException {
	if ( o == null ) {
		return getDocument();
	}
	SAX1toDOM s2d = null;
	try {
		s2d = (SAX1toDOM)sax1toDomObjectPool.borrowObject();
		Marshaller m = new Marshaller(s2d);
		m.setValidation(validateMarshall);
		m.marshal(o);
		return s2d.getDocument();
	} catch (Exception e) {
		LOG.error("Error marshalling " +o.getClass().getName() +" to DOM: " 
				+e.getMessage(),e);
		throw new MediaAlbumException(e.getMessage(),e);
	} finally {
		if ( s2d != null ) {
			try {
				sax1toDomObjectPool.returnObject(s2d);
			} catch (Exception e) {
				LOG.warn("Unable to return SAX1toDOM to pool: " +e.getMessage());
			}
		}
	}
}

}
