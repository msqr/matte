/* ===================================================================
 * AddMediaStaxEndpoint.java
 * 
 * Created Nov 3, 2010 2:52:27 PM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

import magoffin.matt.ma2.SystemConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.util.Base64;
import magoffin.matt.util.FileBasedTemporaryFile;

import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.AbstractStaxStreamPayloadEndpoint;

/**
 * Web service endpoint for adding media.
 * 
 * <p>Uses StAX to handle potentially large amount of data encoded into the request.</p>
 * 
 * <p>Note that a {@link BizContext} must be available via {@link BizContextUtil#getBizContext()}
 * prior to invoking this service, to pass the user authentication to the import.</p> 
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class AddMediaStaxEndpoint extends AbstractStaxStreamPayloadEndpoint {

	private IOBiz ioBiz = null;
	
	final Logger log = Logger.getLogger(getClass());

	@Override
	protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter)
	throws Exception {
		File xmlFile = File.createTempFile("matte-add-media-", ".xml");
		if ( log.isDebugEnabled() ) {
			log.debug("Creating <m:collection-import> document at " 
					+xmlFile.getAbsolutePath());
		}
		File mediaFile = File.createTempFile("matte-add-media-", ".zip");
		if ( log.isDebugEnabled() ) {
			log.debug("Decoding <m:media-data> to "  +mediaFile.getAbsolutePath());
		}

		AddMediaSplitter splitter = new AddMediaSplitter(streamReader, xmlFile, 
				mediaFile, getOutputFactory());
		AddMediaCommand command = splitter.split();
		
		/*
		command.setCollectionId(addContentHander.getCollectionId());
		command.setLocalTz(addContentHander.getLocalTz());
		command.setMediaTz(addContentHander.getMediaTz());
		command.setTempFile(new FileBasedTemporaryFile(addContentHander.getMediaFile(), 
				"application/zip"));
		command.setMetaXmlFile(new FileBasedTemporaryFile(addContentHander.getXmlFile(), 
				"text/xml"));
		*/
		BizContext context = BizContextUtil.getBizContext();
		WorkInfo workInfo = ioBiz.importMedia(command, context);
		streamWriter.writeStartElement("m", "AddMediaResponse", SystemConstants.MATTE_XML_NAMESPACE_URI);
		streamWriter.writeNamespace("m", SystemConstants.MATTE_XML_NAMESPACE_URI);
		streamWriter.writeAttribute("success", Boolean.TRUE.toString());
		streamWriter.writeAttribute("ticket", String.valueOf(workInfo.getTicket()));
		streamWriter.writeEndElement();
	}

	@Override
	protected XMLInputFactory createXmlInputFactory() {
		XMLInputFactory factory = super.createXmlInputFactory();
		factory.setProperty("javax.xml.stream.isCoalescing", Boolean.FALSE);
        return factory;
    }
	
	private static class AddMediaSplitter {
		
		/** An enum for tracking state. */
		private enum ContentHandlerMode {
			
			/** Look for the <m:collection-import> element. */
			FIND_COLLECTION_IMPORT,
			
			/** Copying collection import data. */
			COPY_COLLECTION_IMPORT,
			
			/** Look for the <m:media-data> element. */
			FIND_MEDIA_DATA,
			
			/** Decoding media data. */
			DECODE_MEDIA_DATA,
			
			/** All done. */
			DONE,
		}

		private static final String ADD_MEDIA_ELEMENT_NAME = "AddMediaRequest";
		private static final String COLLECTION_IMPORT_ELEMENT_NAME = "collection-import";
		private static final String MEDIA_DATA_ELEMENT_NAME = "media-data";
		private static final int BASE64_BUFFER_SIZE = 4096;
		
		private ContentHandlerMode mode = ContentHandlerMode.FIND_COLLECTION_IMPORT;
		private final XMLStreamReader streamReader;
//		private final File xmlFile;
		private final File mediaFile;
		
		private AddMediaCommand command = new AddMediaCommand();
		private XMLStreamWriter xmlWriter;
		private OutputStream mediaFileOut;
		private int totalMediaDataCharsIn = 0;
		private int totalMediaDataBytesOut = 0;
		private char[] base64Buffer = new char[BASE64_BUFFER_SIZE];
		private int base64BufferPtr = 0;
		
		private final Logger log = Logger.getLogger(getClass());
		
		private AddMediaSplitter(XMLStreamReader streamReader, File xmlFile, File mediaFile, 
				XMLOutputFactory outputFactory) throws XMLStreamException, IOException {
			this.streamReader = streamReader;
//			this.xmlFile = xmlFile;
			this.mediaFile = mediaFile;
			this.xmlWriter = outputFactory.createXMLStreamWriter(
					new BufferedOutputStream(new FileOutputStream(xmlFile)),
					"UTF-8");
			this.xmlWriter.writeStartDocument();
			command.setTempFile(new FileBasedTemporaryFile(mediaFile, "application/zip"));
			command.setMetaXmlFile(new FileBasedTemporaryFile(xmlFile, "text/xml"));
	}
		
		private AddMediaCommand split() throws XMLStreamException {
			
			int eventType = 0;
			while ( streamReader.hasNext() ) {
				switch ( mode ) {
				case FIND_COLLECTION_IMPORT:
				case FIND_MEDIA_DATA:
					eventType = streamReader.nextTag();
					break;
					
				default:
					eventType = streamReader.next();
				}
				
				switch ( eventType ) {
				case XMLEvent.NAMESPACE:
					namespace();
					break;
				
				case XMLEvent.START_ELEMENT:
					startElement();
					break;
					
				case XMLEvent.END_ELEMENT:
					endElement();
					break;
					
				case XMLEvent.CDATA:
				case XMLEvent.CHARACTERS:
					characters();
					break;
				}
			}
			
			return command;
		}
		
		private void namespace() throws XMLStreamException {
			int i, len;
			for ( i = 0, len = streamReader.getNamespaceCount(); i < len; i++ ) {
				xmlWriter.writeNamespace(streamReader.getNamespacePrefix(i), 
						streamReader.getNamespaceURI(i));
			}
		}
		
		public void startElement() throws XMLStreamException {
			String uri = streamReader.getNamespaceURI();
			String localName = streamReader.getLocalName();
			String prefix = streamReader.getPrefix();
			if ( mode == ContentHandlerMode.FIND_COLLECTION_IMPORT ) {
				if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
						&& ADD_MEDIA_ELEMENT_NAME.equals(localName) ) {
					// get import attribute values
					String colIdStr = streamReader.getAttributeValue(null, "collection-id");
					try {
						command.setCollectionId(Long.valueOf(colIdStr));
					} catch ( RuntimeException e ) {
						log.warn("Unable to parse collection-id attribute: " +e);
					}
					command.setLocalTz(streamReader.getAttributeValue(null, "local-tz"));
					command.setMediaTz(streamReader.getAttributeValue(null, "media-tz"));
				} else if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
						&& COLLECTION_IMPORT_ELEMENT_NAME.equals(localName)) {
					copyElement();
					boolean needsMapping = prefix != null;
					for ( int i = 0, len = streamReader.getNamespaceCount(); needsMapping && i < len; i++ ) {
						if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(streamReader.getNamespaceURI(i)) ) {
							needsMapping = false;
						}
					}
					if ( needsMapping ) {
						xmlWriter.writeNamespace(prefix, SystemConstants.MATTE_XML_NAMESPACE_URI);
					}
					mode = ContentHandlerMode.COPY_COLLECTION_IMPORT;
				}
			} else if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
				copyElement();
			} else if ( mode == ContentHandlerMode.FIND_MEDIA_DATA ) {
				if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
						&& MEDIA_DATA_ELEMENT_NAME.equals(localName)) {
					mode = ContentHandlerMode.DECODE_MEDIA_DATA;
					try {
						mediaFileOut = new BufferedOutputStream(
								new FileOutputStream(mediaFile));						
					} catch ( FileNotFoundException e ) {
						throw new XMLStreamException(e);
					}
				}
			}
		}
		
		private void endElement() throws XMLStreamException {
			String uri = streamReader.getNamespaceURI();
			String localName = streamReader.getLocalName();
			if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
				xmlWriter.writeEndElement();
				if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
						&& COLLECTION_IMPORT_ELEMENT_NAME.equals(localName) ) {
					try {
						xmlWriter.flush();
						xmlWriter.close();
					} catch (XMLStreamException e) {
						throw new XMLStreamException("Exception closing collection-import XML", e);
					}
					xmlWriter = null;
					mode = ContentHandlerMode.FIND_MEDIA_DATA;
				}
				return;
			} else if ( mode == ContentHandlerMode.DECODE_MEDIA_DATA ) {
				mode = ContentHandlerMode.DONE;
				if ( base64BufferPtr > 0 ) {
					decodeBase64Buffer(0, base64BufferPtr);
				}
				if ( log.isDebugEnabled() ) {
					log.debug("Decoded " +totalMediaDataCharsIn +" Base64 characters into "
							+totalMediaDataBytesOut +" bytes");
				}
				try {
					mediaFileOut.flush();
					mediaFileOut.close();
				} catch ( IOException e ) {
					log.warn("IOException closing output stream: " +e);	
				}
				mediaFileOut = null;
				return;
			}
		}
		
		public void characters() throws XMLStreamException {
			char[] txt = streamReader.getTextCharacters();
			int start = streamReader.getTextStart();
			int len = streamReader.getTextLength();
			if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
				xmlWriter.writeCharacters(txt, start, len);
			} else if ( mode == ContentHandlerMode.DECODE_MEDIA_DATA ) {
				// copy into buffer, decoding buffer when full
				characters(txt, start, len);
			}
		}
		
		public void characters(char[] ch, int start, int length) throws XMLStreamException {
			int inputIdx = start;
			int inputEnd = start + length;
			while ( inputIdx < inputEnd && base64BufferPtr < base64Buffer.length ) {
				char c = ch[inputIdx++];
				if ( Character.isWhitespace(c) ) {
					continue;
				}
				base64Buffer[base64BufferPtr++] = c;
			}
			if ( base64BufferPtr == base64Buffer.length ) {
				// buffer full... decode and flush
				decodeBase64Buffer(0, base64Buffer.length);
			}
			
			// in case we didn't copy all of input, do that now via recursion
			if ( inputIdx < inputEnd ) {
				characters(ch, inputIdx, inputEnd - inputIdx);
			}
		}
		
		private void copyElement() throws XMLStreamException {
			if  ( streamReader.isStartElement() ) {
				xmlWriter.writeStartElement(
						streamReader.getPrefix(), 
						streamReader.getLocalName(), 
						streamReader.getNamespaceURI());
				int i, len;
				for ( i = 0, len = streamReader.getAttributeCount(); i < len; i++ ) {
					xmlWriter.writeAttribute(streamReader.getAttributePrefix(i), 
							streamReader.getAttributeNamespace(i),
							streamReader.getAttributeLocalName(i),
							streamReader.getAttributeValue(i));
				}
			} else {
				xmlWriter.writeEndElement();
			}
		}
		
		private void decodeBase64Buffer(int start, int end ) throws XMLStreamException {
			try {
				totalMediaDataCharsIn += (end - start);
				byte[] data = Base64.decodeFast(base64Buffer, start, end);
				totalMediaDataBytesOut += data.length;
				mediaFileOut.write(data);
				base64BufferPtr = 0;
			} catch ( IOException e ) {
				throw new XMLStreamException(e);
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
