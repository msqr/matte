/* ===================================================================
 * MatteNamespaceContext.java
 * 
 * Created Feb 11, 2007 1:31:37 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 */

package magoffin.matt.ma2.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import magoffin.matt.ma2.SystemConstants;

/**
 * {@link NamespaceContext} implementation for Matte.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 * @deprecated see {@link magoffin.matt.xweb.util.BasicNamespaceContext}
 * @see magoffin.matt.xweb.util.BasicNamespaceContext
 */
@Deprecated
public class MatteNamespaceContext implements NamespaceContext {

	/** The MA2 XML namespace prefix to use. */
	private String ma2NamespacePrefix = "ma";

	/** The MA2 XML namespace URI to use. */
	private String ma2NamespaceURI = "http://msqr.us/xsd/MediaAlbum";

	/** The Matte XML namespace prefix to use. */
	private String matteNamespacePrefix = "m";

	/** The Matte XML namespace URI to use. */
	private String matteNamespaceURI = SystemConstants.MATTE_XML_NAMESPACE_URI;

	/** The XWeb namespace prefix to use. */
	private String xWebNamespacePrefix = "x";

	private String xWebNamespaceURI = "http://msqr.us/xsd/jaxb-web";

	@Override
	public String getNamespaceURI(String prefix) {
		if ( matteNamespacePrefix.equals(prefix) ) {
			return matteNamespaceURI;
		} else if ( ma2NamespacePrefix.equals(prefix) ) {
			return ma2NamespaceURI;
		} else if ( xWebNamespacePrefix.equals(prefix) ) {
			return xWebNamespaceURI;
		} else if ( prefix.equals(XMLConstants.XML_NS_PREFIX) ) {
			return XMLConstants.XML_NS_URI;
		} else if ( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) ) {
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		} else {
			return XMLConstants.NULL_NS_URI;
		}
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if ( matteNamespaceURI.equals(namespaceURI) ) {
			return matteNamespacePrefix;
		} else if ( ma2NamespaceURI.equals(namespaceURI) ) {
			return ma2NamespacePrefix;
		} else if ( xWebNamespaceURI.equals(namespaceURI) ) {
			return xWebNamespacePrefix;
		} else if ( namespaceURI.equals(XMLConstants.XML_NS_URI) ) {
			return XMLConstants.XML_NS_PREFIX;
		} else if ( namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) ) {
			return XMLConstants.XMLNS_ATTRIBUTE;
		} else {
			return null;
		}
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		List<String> tmpList = new ArrayList<String>(1);
		tmpList.add(getPrefix(namespaceURI));
		return tmpList.iterator();
	}

	/**
	 * @return the ma2NamespacePrefix
	 */
	public String getMa2NamespacePrefix() {
		return ma2NamespacePrefix;
	}

	/**
	 * @param ma2NamespacePrefix
	 *        the ma2NamespacePrefix to set
	 */
	public void setMa2NamespacePrefix(String ma2NamespacePrefix) {
		this.ma2NamespacePrefix = ma2NamespacePrefix;
	}

	/**
	 * @return the ma2NamespaceURI
	 */
	public String getMa2NamespaceURI() {
		return ma2NamespaceURI;
	}

	/**
	 * @param ma2NamespaceURI
	 *        the ma2NamespaceURI to set
	 */
	public void setMa2NamespaceURI(String ma2NamespaceURI) {
		this.ma2NamespaceURI = ma2NamespaceURI;
	}

	/**
	 * @return the matteNamespacePrefix
	 */
	public String getMatteNamespacePrefix() {
		return matteNamespacePrefix;
	}

	/**
	 * @param matteNamespacePrefix
	 *        the matteNamespacePrefix to set
	 */
	public void setMatteNamespacePrefix(String matteNamespacePrefix) {
		this.matteNamespacePrefix = matteNamespacePrefix;
	}

	/**
	 * @return the matteNamespaceURI
	 */
	public String getMatteNamespaceURI() {
		return matteNamespaceURI;
	}

	/**
	 * @param matteNamespaceURI
	 *        the matteNamespaceURI to set
	 */
	public void setMatteNamespaceURI(String matteNamespaceURI) {
		this.matteNamespaceURI = matteNamespaceURI;
	}

	/**
	 * @return the xWebNamespacePrefix
	 */
	public String getXWebNamespacePrefix() {
		return xWebNamespacePrefix;
	}

	/**
	 * @param webNamespacePrefix
	 *        the xWebNamespacePrefix to set
	 */
	public void setXWebNamespacePrefix(String webNamespacePrefix) {
		xWebNamespacePrefix = webNamespacePrefix;
	}

	/**
	 * @return the xWebNamespaceURI
	 */
	public String getXWebNamespaceURI() {
		return xWebNamespaceURI;
	}

	/**
	 * @param webNamespaceURI
	 *        the xWebNamespaceURI to set
	 */
	public void setXWebNamespaceURI(String webNamespaceURI) {
		xWebNamespaceURI = webNamespaceURI;
	}

}
