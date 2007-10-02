/* ===================================================================
 * XmlHelper.java
 *
 * Created May 1, 2006 11:25:28 AM
 *
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: XmlHelper.java,v 1.4 2007/07/28 10:25:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import magoffin.matt.ma2.domain.ObjectFactory;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for working with XML data.
 *
 * <dl class="class-properties">
 *   <dt>documentBuilderFactory</dt>
 *   <dd>The JAXP DocumentBuilderFactory to use for creating Document objects.</dd>
 *
 *   <dt>jaxbContext</dt>
 *   <dd>The JAXBContext to use for MA2 JAXB domain operations.</dd>
 *
 *   <dt>marshallerProperties</dt>
 *   <dd>A Map of properties to set on the JAXB Marshaller. This is useful
 *   for passing such properties as <code>com.sun.xml.bind.namespacePrefixMapper</code>
 *   property.</dd>
 *
 *   <dt>objectFactory</dt>
 *   <dd>The JAXB ObjectFactory to use for MA2 JAXB domain objects.</dd>
 *
 *   <dt>starObjectFactory</dt>
 *   <dd>The JAXB ObjectFactory to use for MA2 JAXB domain objects.</dd>
 *
 *   <dt>transformerFactory</dt>
 *   <dd>The JAXP TransformerFactory to use for creating Transformer objects.</dd>
 *
 *   <dt>xpathFactory</dt>
 *   <dd>The JAXP XPathFactory to use for evaluating XPath queries.</dd>
 * </dl>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/28 10:25:54 $
 */
public class XmlHelper {

	/** The JAXP DocumentBuilderFactory to use for getting our DocumentBuilder. */
	private DocumentBuilderFactory documentBuilderFactory = null;

	/** The JAXP TransformerFactory to use for getting transformers. */
	private TransformerFactory transformerFactory = null;

	/** the JAXP XPathFactory to use for getting XPath evaulators */
	private XPathFactory xpathFactory = null;

	/** The JAXBContext for handling our MA2 objects. */
	private JAXBContext jaxbContext = null;

	/** A Map of properties to use with the JAXB Marshaller. */
	private Map<String,String> marshallerProperties = null;

	/** The MA2 package JAXB ObjectFactory. */
	private ObjectFactory objectFactory = null;
	
	private NamespaceContext xpathNamespaceContext = null;
	
	private Ehcache schemaCache = null;

	/* Internal fields below. */
	
	private final Logger log = Logger.getLogger(XmlHelper.class);

	/**
	 * The JAXP DocumentBuilder to created Document objects with.
	 * Note this instance is not thread-safe and should only be used by the
	 * newDocument() method.
	 */
	private DocumentBuilder docBuilder = null;

	/**
	 * Initialize this instance.
	 *
	 * <p>
	 * This method should be called after setting all properties and before
	 * using any (non-property) methods.
	 * </p>
	 */
	public void init() {
		if ( documentBuilderFactory == null ) {
			throw new RuntimeException("The documentBuilderFactory property is not configured.");
		}
		if ( transformerFactory == null ) {
			throw new RuntimeException("The transformerFactory property is not configured.");
		}
		if ( xpathFactory == null ) {
			throw new RuntimeException("The xpathFactory property is not configured.");
		}
		if ( jaxbContext == null ) {
			throw new RuntimeException("The jaxbContext property is not configured.");
		}
		if ( objectFactory == null ) {
			throw new RuntimeException("The objectFactory property is not configured.");
		}
		try {
			this.docBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch ( ParserConfigurationException e ) {
			throw new RuntimeException("Unable to get JAXP DocumentBuilder",e);
		}
	}

	/**
	 * Evaluate an XPath expression and return the result.
	 *
     * @param object - The starting context.
     * @param xpath - The XPath expression.
     * @param returnType - The desired return type.
     * @return Result of evaluating an XPath expression
	 */
	public Object evaluateXPath(Object object, String xpath, QName returnType) {
		Object value;

		try {
			XPath xpathx  = xpathFactory.newXPath();
			if ( this.xpathNamespaceContext != null ) {
				xpathx.setNamespaceContext(this.xpathNamespaceContext);
			}
			value = xpathx.evaluate(xpath, object, returnType);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("Error evaluating XPath [" +xpath +"]", e);
		}

		return value;
	}

	/**
	 * Get a JAXB Marshaller, configured for our context.
	 *
	 * @return a Marshaller
	 * @throws JAXBException if a JAXB exception occurs
	 */
	public Marshaller getMarshaller() throws JAXBException {
		Marshaller marshaller = jaxbContext.createMarshaller();
		if ( marshallerProperties != null ) {
			for ( Iterator<Map.Entry<String, String>> itr =
					marshallerProperties.entrySet().iterator(); itr.hasNext(); ) {
				Map.Entry<String,String> me = itr.next();
				marshaller.setProperty(me.getKey().toString(),me.getValue());
			}
		}
		return marshaller;
	}

	/**
	 * Marshal a JAXB object as XML to the specifiec Logger.
	 * @param notice a string to prefix the output XML with
	 * @param jaxbObject the JAXB object to debug
	 * @param aLog the Logger to log to
	 */
	public void debugJaxbObject(String notice, Object jaxbObject, Logger aLog ) {
		try {
			debugXml(notice,new JAXBSource(getMarshaller(),jaxbObject),aLog);
		} catch ( JAXBException e ) {
			throw new RuntimeException("Unable to debug JAXB object",e);
		}
	}


	/**
	 * Debug an XML Source to a Logger.
	 *
	 * <p>Note the XML will only be serialized if the <code>log</code>
	 * has <code>DEBUG</code> level enabled.</p>
	 *
	 * @param notice a string to prefix the output XML with
	 * @param xml the XML Source to debug
	 * @param aLog the Logger to log to
	 */
	public void debugXml(String notice, Source xml, Logger aLog) {
		if ( !aLog.isDebugEnabled() ) return;
		StringWriter writer = new StringWriter();
		transformXml(xml,new StreamResult(writer));
		aLog.debug(notice +writer.toString());
	}

	/**
	 * Perform an copy transformation (copy XML from source to result).
	 *
	 * <p>This method can be used to serialize DOM or JAXB sources to an
	 * XML String.</p>
	 *
	 * @param source the XML source
	 * @param result the XML result
	 */
	public void transformXml(Source source, Result result) {
		transformXml(source, result, (Map<String, ?>)null);
	}

	/**
	 * Perform an copy transformation (copy XML from source to result).
	 *
	 * <p>This method can be used to serialize DOM or JAXB sources to an
	 * XML String.</p>
	 *
	 * @param source the XML source
	 * @param result the XML result
	 * @param xsltParameters optional XSLT parameters
	 */
	public void transformXml(Source source, Result result, Map<String, ?> xsltParameters) {
		try {
			transformXml(source, result, transformerFactory.newTransformer(), xsltParameters);
		} catch ( Exception e ) {
			throw new RuntimeException("Unable to transform Document to XML String", e);
		}
	}

	/**
	 * Perform an XSLT transformation.
	 *
	 * @param source the XML source
	 * @param result the XML result
	 * @param xslt the XSLT to transform with
	 */
	public void transformXml(Source source, Result result, Templates xslt ) {
		transformXml(source, result, xslt, null);
	}

	/**
	 * Perform an XSLT transformation with parameters.
	 *
	 * @param source the XML source
	 * @param result the XML result
	 * @param xslt the XSLT to transform with
	 * @param xsltParameters optional XSLT parameters to set
	 */
	public void transformXml(Source source, Result result, Templates xslt,
			Map<String, ?> xsltParameters ) {
		try {
			transformXml(source, result, xslt.newTransformer(), xsltParameters);
		} catch ( Exception e ) {
			throw new RuntimeException("Unable to transform Document to XML String", e);
		}
	}

	/**
	 * Internal method for transforming so that indentation is set consistently.
	 *
	 * @param source the XML source
	 * @param result the XML result
	 * @param transformer the XSLT transformer to use
	 * @param xsltParameter optional XSLT parameters to set
	 */
	private void transformXml(Source source, Result result, Transformer transformer,
			Map<String, ?> xsltParameters) {
		try {
			if ( xsltParameters != null ) {
				for ( String key : xsltParameters.keySet() ) {
					transformer.setParameter(key, xsltParameters.get(key));
				}
			}
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			if ( transformer.getURIResolver() == null
					&& transformerFactory.getURIResolver() != null ) {
				// See http://issues.apache.org/jira/browse/XALANJ-1131
				transformer.setURIResolver(transformerFactory.getURIResolver());
			}
			transformer.transform(source, result);
		} catch ( Exception e ) {
			throw new RuntimeException("Unable to transform Document to XML String", e);
		}
	}

	/**
	 * Get a new Document instance.
	 * @return the new Document
	 */
	public Document getNewDocument() {
		return docBuilder.newDocument();
	}

	/**
	 * Get a Document object from an XML data stream.
	 * @param xmlInput the XML data stream
	 * @return the Document
	 */
	public Document getDocument(InputStream xmlInput) {
		try {
			return this.documentBuilderFactory.newDocumentBuilder().parse(xmlInput);
		} catch (Exception e) {
			throw new RuntimeException("Unable to parse XML Document from InputStream", e);
		}
	}

	/**
	 * Get a Document object from an XML InputSource.
	 * @param xmlSource the XML data
	 * @return the Document
	 */
	public Document getDocument(InputSource xmlSource) {
		try {
			return this.documentBuilderFactory.newDocumentBuilder().parse(xmlSource);
		} catch (Exception e) {
			throw new RuntimeException("Unable to parse XML Document from InputSource", e);
		}
	}

	/**
	 * Get a Document object from an XML string.
	 * @param xml the XML string
	 * @return the Document
	 */
	public Document getDocument(String xml) {
		try {
			return this.documentBuilderFactory.newDocumentBuilder().parse(
				new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			throw new RuntimeException("Unable to parse XML Document from InputSource", e);
		}
	}

	/**
	 * Get XML data as a String.
	 *
	 * <p>This method exists to validate the input XML is valid.</p>
	 *
	 * @param xmlInput the XML data stream
	 * @return the XML as a String
	 */
	public String getXmlString(InputStream xmlInput) {
		// first copy input stream into memory
		byte[] xml;
		try {
			xml = FileCopyUtils.copyToByteArray(xmlInput);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// now parse into DOM
		getDocument(new ByteArrayInputStream(xml));

		// if we get here, we know the XML was at least well-formed,
		// so return String
		return new String(xml);
	}

	/**
	 * Validate XLM against a schema.
	 * 
	 * @param xml the XML to validate
	 * @param schemaResource the schema to validate against
	 * @throws SAXException if an XML validation error occurs
	 */
	public void validateXml(Source xml, Resource schemaResource) throws SAXException {
		try {
			Schema schema = getSchemaFromResource(schemaResource);
			Validator validator = schema.newValidator();
			validator.validate(xml);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get a Schema, possibly from cache.
	 * 
	 * @param schemaResource the resource to get the Schema object ffrom
	 * @return the schema
	 * @throws IOException if an IO error occurs
	 */
	public Schema getSchemaFromResource(Resource schemaResource) throws IOException {
		if ( schemaCache != null ) {
			// look in cache first
			try {
				Element cachedSchemaElement = schemaCache.get(schemaResource.getURL().toString());
				if ( cachedSchemaElement != null ) {
					return (Schema)cachedSchemaElement.getObjectValue();
				}
			} catch ( CacheException e ) {
				log.warn("Error using schema cache, proceeding without cache", e);
			}
		}

		SchemaFactory schemaFactory = null;
		String schemaExtension = StringUtils.getFilenameExtension(
				schemaResource.getFilename());
		if ( "xsd".equals(schemaExtension) ) {
			schemaFactory = SchemaFactory.newInstance(
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else if ( "dtd".equals(schemaExtension) ) {
			schemaFactory = SchemaFactory.newInstance(
					XMLConstants.XML_DTD_NS_URI);
		} else {
			throw new RuntimeException("Unknown schema type: " 
				+schemaResource.getFilename());
		}

		Source schemaSource = new StreamSource(schemaResource.getInputStream());
		try {
			Schema schema = schemaFactory.newSchema(schemaSource);
	
			if ( schemaCache != null ) {
				Element cachedSchemaElement = new Element(
						schemaResource.getURL().toString(), schema);
				schemaCache.put(cachedSchemaElement);
			}
			
			return schema;
		} catch ( SAXException e ) {
			throw new RuntimeException("Unable to parse schema for resource [" 
					+schemaResource.getURL() +"]", e);
		}
	}
	
	/**
	 * @return the documentBuilderFactory
	 */
	public DocumentBuilderFactory getDocumentBuilderFactory() {
		return documentBuilderFactory;
	}

	/**
	 * @param documentBuilderFactory the documentBuilderFactory to set
	 */
	public void setDocumentBuilderFactory(
			DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}

	/**
	 * @return the jaxbContext
	 */
	public JAXBContext getJaxbContext() {
		return jaxbContext;
	}

	/**
	 * @param jaxbContext the jaxbContext to set
	 */
	public void setJaxbContext(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	/**
	 * @return the marshallerProperties
	 */
	public Map<String, String> getMarshallerProperties() {
		return marshallerProperties;
	}

	/**
	 * @param marshallerProperties the marshallerProperties to set
	 */
	public void setMarshallerProperties(Map<String, String> marshallerProperties) {
		this.marshallerProperties = marshallerProperties;
	}

	/**
	 * @return the objectFactory
	 */
	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	/**
	 * @param objectFactory the objectFactory to set
	 */
	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	/**
	 * @return the transformerFactory
	 */
	public TransformerFactory getTransformerFactory() {
		return transformerFactory;
	}

	/**
	 * @param transformerFactory the transformerFactory to set
	 */
	public void setTransformerFactory(TransformerFactory transformerFactory) {
		this.transformerFactory = transformerFactory;
	}

	/**
	 * @return the xpathFactory
	 */
	public XPathFactory getXpathFactory() {
		return xpathFactory;
	}

	/**
	 * @param xpathFactory the xpathFactory to set
	 */
	public void setXpathFactory(XPathFactory xpathFactory) {
		this.xpathFactory = xpathFactory;
	}
	
	/**
	 * @return the xpathNamespaceContext
	 */
	public NamespaceContext getXpathNamespaceContext() {
		return xpathNamespaceContext;
	}
	
	/**
	 * @param xpathNamespaceContext the xpathNamespaceContext to set
	 */
	public void setXpathNamespaceContext(NamespaceContext xpathNamespaceContext) {
		this.xpathNamespaceContext = xpathNamespaceContext;
	}
	
	/**
	 * @return the schemaCache
	 */
	public Ehcache getSchemaCache() {
		return schemaCache;
	}
	
	/**
	 * @param schemaCache the schemaCache to set
	 */
	public void setSchemaCache(Ehcache schemaCache) {
		this.schemaCache = schemaCache;
	}

}
