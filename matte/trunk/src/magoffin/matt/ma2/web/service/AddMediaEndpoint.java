/* ===================================================================
 * AddMediaEndpoint.java
 * 
 * Created Dec 1, 2007 10:23:29 AM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javanet.staxutils.XMLStreamEventWriter;
import javanet.staxutils.XMLStreamUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.util.Base64;

import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.AbstractStaxStreamPayloadEndpoint;

/**
 * Web service endpoint for adding media.
 * 
 * <p>Uses StAX to handle potentially large amount of data encoded into the request.</p>
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class AddMediaEndpoint extends AbstractStaxStreamPayloadEndpoint {
	
	/** The NS URI for XMIME.*/
	public static final String XMIME_NS = "http://www.w3.org/2005/05/xmlmime";
	
	private IOBiz ioBiz = null;
	
	private final Logger log = Logger.getLogger(getClass());

	@Override
	protected void invokeInternal(XMLStreamReader reader, XMLStreamWriter writer) {
		// split XML into <m:collection-import> without media and a decoded media file
		File xmlFile = null;
		File mediaFile = null;
		OutputStream mediaFileOut = null;
		try {
			xmlFile = File.createTempFile("matte-add-media-", ".xml");
			if ( log.isDebugEnabled() ) {
				log.debug("Creating <m:collection-import> document at " 
						+xmlFile.getAbsolutePath());
			}
			XMLStreamWriter xmlFileOut = getOutputFactory().createXMLStreamWriter(
					new BufferedOutputStream(new FileOutputStream(xmlFile)),
					"UTF-8");
			xmlFileOut.writeStartDocument();

			// use XMLEventReader to copy <collection-import>
			XMLEventReader colImportReader = getInputFactory().createXMLEventReader(reader);
			XMLStreamUtils.nextElement(colImportReader); // go to <AddMediaRequest>
			colImportReader.nextEvent(); // skip <AddMediaRequest>
			if ( null == XMLStreamUtils.nextElement(colImportReader, 
					new QName("http://msqr.us/xsd/matte", "collection-import")) ) {
				throw new RuntimeException("Expected <m:collection-import> element missing");
			}
			XMLStreamUtils.copyElement(colImportReader, new XMLStreamEventWriter(xmlFileOut));
			xmlFileOut.flush();
			xmlFileOut.close();
			
			// skip to <media-data> element
			StartElement mediaDataElement = XMLStreamUtils.nextElement(colImportReader, 
					new QName("http://msqr.us/xsd/matte", "media-data"));
			
			// look for for xmime:contentType
			Attribute mimeAttr = mediaDataElement.getAttributeByName(
					new QName(XMIME_NS, "contentType"));
			if ( log.isDebugEnabled() ) {
				log.debug("Got XMIME: " +mimeAttr.getValue());
			}
			//colImportReader.close();
			
			// now use XMLStreamReader to decode base64 data
			mediaFile = File.createTempFile("matte-add-media-", ".zip");
			if ( log.isDebugEnabled() ) {
				log.debug("Decoding Base64 media archive to " +mediaFile.getAbsolutePath());
			}
			
			// forward to element conetnt
			OUTER: while ( true ) {
				switch ( reader.getEventType() ) {
					case XMLStreamConstants.CHARACTERS:
					case XMLStreamConstants.CDATA:
					case XMLStreamConstants.SPACE:
						break OUTER;
						
					default:
						reader.next();
				}
			}
			
			mediaFileOut = new BufferedOutputStream(new FileOutputStream(mediaFile));
			int totalChars = 0;
			int totalBytes = 0;
			int length = 1024;
			char[] myBuffer = new char[length];
			for ( int sourceStart = 0 ; ; sourceStart += length ) {
				int nCopied = reader.getTextCharacters(sourceStart, myBuffer, 0, length );
				totalChars += nCopied;
				if ( nCopied < length ) {
					char[] tmp = new char[nCopied];
					System.arraycopy(myBuffer, 0, tmp, 0, nCopied);
					byte[] decoded = Base64.decode(tmp);
					totalBytes += decoded.length;
					mediaFileOut.write(decoded);
					break;
				}
				byte[] decoded = Base64.decode(myBuffer);
				totalBytes += decoded.length;
				mediaFileOut.write(decoded);
			}
			if ( log.isDebugEnabled() ) {
				log.debug("Decoded " +totalChars +" Base64 characters into "
						+totalBytes +" bytes");
			}
			
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		} finally {
			if ( mediaFileOut != null ) {
				try {
					mediaFileOut.flush();
					mediaFileOut.close();
				} catch ( IOException e ) {
					log.warn("IOException closing output stream: " +e);
				}
			}
		}
	}

	/**
	 * @return the ioBiz
	 */
	public IOBiz getIoBiz() {
		return ioBiz;
	}

	/**
	 * @param ioBiz the ioBiz to set
	 */
	public void setIoBiz(IOBiz ioBiz) {
		this.ioBiz = ioBiz;
	}
	
}
