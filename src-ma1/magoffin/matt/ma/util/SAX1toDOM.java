/* ===================================================================
 * SAX1toDOM.java
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
 * $Id: SAX1toDOM.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import org.apache.log4j.Logger;

import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.Attributes;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * Populates a DOM Document from SAX DocumentHandler events.
 *
 * <p>This class can generate a DOM <code>Document</code> object out of SAX 1
 * events. It actually uses the SAX 2 <code>ParserAdapter</code> to handle
 * namespaces correctly with DOM. It was designed this way to work with the
 * Castor <code>Marshaller</code> class, which uses SAX 1 events to marshall
 * objects into XML. To use this with Castor, do something like this:</p>
 *
 * <p><pre>SAX1toDOM s2dom = new SAX1toDOM(parser);
 * Marshaller marshaller = new Marshaller(s2dom);
 * marshaller.marshal(object);
 * org.w3c.dom.Document doc = s2dom.getDocument();
 * </pre></p>
 *
 * <p>Created Oct 12, 2002 5:39:40 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class SAX1toDOM extends ParserAdapter {

	private static Logger log = Logger.getLogger(SAX1toDOM.class);
	
	private static DocumentBuilderFactory docBuilderFactory = 
		DocumentBuilderFactory.newInstance();

	private Document _doc = null;
	private DocumentBuilder _docBuilder = null;
	private SAXtoDOMContentHandler _ch = null;
	
private class SAXtoDOMContentHandler extends DefaultHandler {

	private StringBuffer _buf;
	private Stack _parents = new Stack();
	private boolean _ns;

	public SAXtoDOMContentHandler( boolean ns ) {
        _buf = new StringBuffer();
		_ns = ns;
	}

	public void startElement( String uri, 
								String localName, 
								String qName, 
								Attributes attributes ) {

		if ( log.isDebugEnabled() ) {

			log.debug( "Starting element <" +
					( _ns ? uri+":"+qName +"(" +localName+")": localName ) + ">" );
		
		}
		
		Element element = (_ns && uri != null && uri.length() > 0
				? _doc.createElementNS(uri,localName) 
				: _doc.createElement(localName) );
		int length = attributes.getLength();
		for ( int i = 0; i < length; i++ ) {
			if ( log.isDebugEnabled() ) {
				log.debug("Attribute: [" + attributes.getURI(i)
					+ "] " + attributes.getQName(i)
					+ " = " + attributes.getValue(i) );
			}
			if ( _ns ) {
				element.setAttributeNS( attributes.getURI(i),
					attributes.getQName(i),
					attributes.getValue(i));
			} else {
				element.setAttribute( attributes.getLocalName(i),
					attributes.getValue(i) );
			}
		}
		
		if ( _parents.size() > 0 ) {
			((Element)_parents.peek()).appendChild( element );
		} else {
			_doc.appendChild( element );
		}
		_parents.push( element );
		
	} // startElement( String, String, String, Attributes )

	public void characters(char[] chars, int offset, int length) {
		_buf.append(chars, offset, length);
	}

	public void endElement( String uri, String localName, String qName ) {
		
		Element element = (Element)_parents.pop();
		if (_buf.length() > 0) {
			Text text = _doc.createTextNode(_buf.toString());
				element.appendChild(text);
		}
		_buf.setLength(0);
		
	} // endElement( String, String, String )
	
	public void reset() {
		_buf.setLength(0);
	}
	
	
} // class SAXtoDOMContentHandler


/**
 * Constructor, with namespace support on.
 *  * @see org.xml.sax.helpers.ParserAdapter#ParserAdapter(Parser) */
public SAX1toDOM( Parser parser ) throws SAXException {
	this(parser,true);
}

/**
 * Constructor.
 *
 * <p>You must supply the SAX Parser object, which you can easily obtain
 * like this:</p>
 *
 * <p><pre> javax.xml.parsers.SAXParserFactory spf = 
 *         javax.xml.parsers.SAXParserFactory.newInstance();
 * SAX1toJDOM s2j = new SAX1toJDOM(spf.newSAXParser().getParser());
 * </pre></p>
 * 
 * <p>This will create a new <code>Document</code> object for you.</p>
 *
 * @param parser the <code>Parser</code> object
 * @throws SAXException if unable to construct this object
 */
public SAX1toDOM( Parser parser, boolean ns ) throws SAXException {
	this(parser,null,ns);
	try {
		_docBuilder = docBuilderFactory.newDocumentBuilder();
		_doc = _docBuilder.newDocument();
	} catch ( ParserConfigurationException e ) {
		throw new SAXException(e.toString());
	}

}

	
/**
 * Constructor.
 *
 * <p>You must supply the SAX <code>Parser</code> object, which you can 
 * easily obtain like this:</p>
 *
 * <p><pre> javax.xml.parsers.SAXParserFactory spf = 
 *         javax.xml.parsers.SAXParserFactory.newInstance();
 * Parser parser = spf.newSAXParser().getParser();
 * </pre></p>
 * 
 * <p>You must also provide a DOM <code>Document</code> object, which you
 * can also easily obtain like this:</p>
 *
 * <p><pre> javax.xml.parsers.DocumentBuilderFactory docFactory =
 *					javax.xml.parsers.DocumentBuilderFactory.newInstance();
 * Document doc = docFactory.newDocumentBuilder().newDocument();
 * </pre></p>
 *
 * @param parser the <code>Parser</code> object
 * @param doc the <code>Document</code> object
 * @throws SAXException if unable to construct this object
 */
public SAX1toDOM( Parser parser, Document doc, boolean ns ) throws SAXException {

	super(parser);
	_doc = doc;
	_ch = new SAXtoDOMContentHandler(ns);
	setContentHandler( _ch );

}

/**
 * Get the DOM Document object.
 *
 * <p>Call this method after parsing is complete.</p>
 *
 * @return Document, or <em>null</em> if nothing parsed
 */
public Document getDocument() { return _doc; }

/**
 * Reset so can create a new DOM object. */
public void reset()
{
	_doc = _docBuilder.newDocument();
	_ch.reset();
}

} // class SAX1toDOM

