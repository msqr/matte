package magoffin.matt.ma2.web.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javanet.staxutils.ContentHandlerToXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import magoffin.matt.ma2.SystemConstants;
import magoffin.matt.util.Base64;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ContentHandler implementation that splits the <m:AddMediaRequest> XML document
 * into one <m:collection-import> XML document and one Base64-decoded zip archive
 * from the <m:media-data> element.
 * 
 * <p>Low-level SAX is used for parsing here to efficiently parse potentially huge
 * Base64 character streams from large zip archives. StAX is used to copy the 
 * <m:collection-import> SAX events to an XMLStreamWriter.</p>
 */
public final class AddMediaContentHandler extends DefaultHandler {
	
	/** An enum for tracking state in AddMediaContentHandler. */
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
	private ContentHandlerToXMLStreamWriter delegateXmlWriter;
	private XMLStreamWriter xmlWriter;
	private File xmlFile;
	private File mediaFile;
	private OutputStream mediaFileOut;
	private int totalMediaDataCharsIn = 0;
	private int totalMediaDataBytesOut = 0;
	private char[] base64Buffer = new char[BASE64_BUFFER_SIZE];
	private int base64BufferPtr = 0;
	
	private final Logger log = Logger.getLogger(getClass());
	
	private Long collectionId = null;
	private String localTz = null;
	private String mediaTz = null;
	
	/**
	 * Constructor.
	 * 
	 * @param xmlFile the file to store the XML metadata document in
	 * @param mediaFile the file to store the media binary data in
	 * @param outputFactory the OutputFactory to use
	 * @throws XMLStreamException if any XML stream exception occurs
	 * @throws FileNotFoundException if either of the files are not found
	 */
	public AddMediaContentHandler(File xmlFile, File mediaFile, XMLOutputFactory outputFactory) 
	throws XMLStreamException, FileNotFoundException {
		this.xmlFile = xmlFile;
		this.xmlWriter = outputFactory.createXMLStreamWriter(
				new BufferedOutputStream(new FileOutputStream(xmlFile)),
				"UTF-8");
		this.xmlWriter.writeStartDocument();
		this.mediaFile = mediaFile;
		this.mediaFileOut = null;
		this.delegateXmlWriter = new ContentHandlerToXMLStreamWriter(xmlWriter);
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if ( mode == ContentHandlerMode.FIND_COLLECTION_IMPORT ) {
			if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
					&& ADD_MEDIA_ELEMENT_NAME.equals(localName) ) {
				// get import attribute values
				String colIdStr = attributes.getValue("", "collection-id");
				try {
					collectionId = Long.valueOf(colIdStr);
				} catch ( RuntimeException e ) {
					log.warn("Unable to parse collection-id attribute: " +e);
				}
				localTz = attributes.getValue("", "local-tz");
				mediaTz = attributes.getValue("", "media-tz");
			} else if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
					&& COLLECTION_IMPORT_ELEMENT_NAME.equals(localName)) {
				mode = ContentHandlerMode.COPY_COLLECTION_IMPORT;					
				delegateXmlWriter.startElement(uri, localName, name, attributes);
				return;
			}
		} else if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
			delegateXmlWriter.startElement(uri, localName, name, attributes);
		} else if ( mode == ContentHandlerMode.FIND_MEDIA_DATA ) {
			if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
					&& MEDIA_DATA_ELEMENT_NAME.equals(localName)) {
				mode = ContentHandlerMode.DECODE_MEDIA_DATA;
				try {
					mediaFileOut = new BufferedOutputStream(
							new FileOutputStream(mediaFile));						
				} catch ( FileNotFoundException e ) {
					throw new SAXException(e);
				}
				return;
			}
		}
		super.startElement(uri, localName, name, attributes);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
			delegateXmlWriter.characters(ch, start, length);
			return;
		} else if ( mode == ContentHandlerMode.DECODE_MEDIA_DATA ) {
			// copy into buffer, decoding buffer when full
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
			return;
		}
		super.characters(ch, start, length);
	}
	
	private void decodeBase64Buffer(int start, int end ) throws SAXException {
		try {
			totalMediaDataCharsIn += (end - start);
			byte[] data = Base64.decodeFast(base64Buffer, start, end);
			totalMediaDataBytesOut += data.length;
			mediaFileOut.write(data);
			base64BufferPtr = 0;
		} catch ( IOException e ) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
			delegateXmlWriter.endElement(uri, localName, name);
			if ( SystemConstants.MATTE_XML_NAMESPACE_URI.equals(uri)
					&& COLLECTION_IMPORT_ELEMENT_NAME.equals(localName) ) {
				try {
					xmlWriter.flush();
					xmlWriter.close();
				} catch (XMLStreamException e) {
					throw new SAXException("Exception closing collection-import XML", e);
				}
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
			return;
		}
		super.endElement(uri, localName, name);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		if ( delegateXmlWriter != null ) {
			delegateXmlWriter.endPrefixMapping(prefix);
			return;
		}
		super.endPrefixMapping(prefix);
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws IOException, SAXException {
		if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
			return delegateXmlWriter.resolveEntity(publicId, systemId);
		}
		return super.resolveEntity(publicId, systemId);
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		if ( delegateXmlWriter != null ) {
			delegateXmlWriter.startPrefixMapping(prefix, uri);
			return;
		}
		super.startPrefixMapping(prefix, uri);
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
		if ( mode == ContentHandlerMode.COPY_COLLECTION_IMPORT ) {
			delegateXmlWriter.unparsedEntityDecl(name, publicId, systemId, notationName);
			return;
		}
		super.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	/**
	 * @return the collectionId
	 */
	public Long getCollectionId() {
		return collectionId;
	}

	/**
	 * @return the xmlFile
	 */
	public File getXmlFile() {
		return xmlFile;
	}

	/**
	 * @return the localTz
	 */
	public String getLocalTz() {
		return localTz;
	}

	/**
	 * @return the mediaTz
	 */
	public String getMediaTz() {
		return mediaTz;
	}

	/**
	 * @return the mediaFile
	 */
	public File getMediaFile() {
		return mediaFile;
	}
	
}