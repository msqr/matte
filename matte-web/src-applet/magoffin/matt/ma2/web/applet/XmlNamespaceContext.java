/* ===================================================================
 * XmlNamespaceContext.java
 * 
 * Copyright (c) 2008 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.web.applet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Namespace context for the Matte upload applet.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class XmlNamespaceContext implements NamespaceContext {

	/** The Matte XML namespace prefix to use. */
	public static final String MATTE_NAMESPACE_PREFIX = "m";

	/** The Matte XML namespace URI to use. */
	public static final String MATTE_NAMESPACE_URI = "http://msqr.us/xsd/matte";

	/** The XWeb namespace prefix to use. */
	public static final String XWEB_NAMESPACE_PREFIX = "x";

	/** The XWeb namespace URI to use. */
	public static final String XWEB_NAMESPACE_URI = "http://msqr.us/xsd/jaxb-web";

	public String getNamespaceURI(String prefix) {
		if ( MATTE_NAMESPACE_PREFIX.equals(prefix) ) {
			return MATTE_NAMESPACE_URI;
		} else if ( XWEB_NAMESPACE_PREFIX.equals(prefix) ) {
			return XWEB_NAMESPACE_URI;
		} else if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
			return XMLConstants.XML_NS_URI;
		} else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		} else {
			return XMLConstants.NULL_NS_URI;
		}
	}

	public String getPrefix(String namespaceURI) {
		if ( MATTE_NAMESPACE_URI.equals(namespaceURI) ) {
			return MATTE_NAMESPACE_PREFIX;
		} else if ( XWEB_NAMESPACE_URI.equals(namespaceURI) ) {
			return XWEB_NAMESPACE_PREFIX;
		} else if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
			return XMLConstants.XML_NS_PREFIX;
		} else if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
			return XMLConstants.XMLNS_ATTRIBUTE;
		} else {
			return null;
		}
	}

	public Iterator<String> getPrefixes(String namespaceURI) {
		List<String> tmpList = new ArrayList<String>(1);
		tmpList.add(getPrefix(namespaceURI));
		return tmpList.iterator();
	}

}
