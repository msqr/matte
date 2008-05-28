/* ===================================================================
 * Xform.java
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
 * $Id: Xform.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.util.FileUtil;
import magoffin.matt.util.StringUtil;
import magoffin.matt.util.XMLUtil;
import magoffin.matt.util.cache.CacheFactory;
import magoffin.matt.util.cache.SimpleCache;
import magoffin.matt.util.config.ConfigurationManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Servlet to perform XSLT transform.
 * 
 * <p>This servlet can perform XSL transform on XML data.</p>
 * 
 * <table border="0" cellpadding="1" cellspacing="0"><tr bgcolor="black"><td>
 * <table border="0" cellpadding="4" cellspacing="1">
 * <tr bgcolor="#cccccc">
 * 	<th>HTTP Parameter</th><th>Description</th>
 * </tr>
 * <tr align="left" bgcolor="white" valign="top">
 * 	<th>xml</th>
 * 	<td>A webapp-relative path to an XML file to use as the XML source
 * 	for the transformation.</td>
 * </tr>
 * <tr align="left" bgcolor="white" valign="top">
 * 	<th>xsl</th>
 * 	<td>A webapp-relative path to an XSL file to use as the XSL source
 * 	for the transformation.</td>
 * </tr>
 * <tr align="left" bgcolor="white" valign="top">
 * 	<th>t</th>
 * 	<td><p>The Xform init param name of the XSL Templates object to use
 * 	as the XSL source for the transformation. For example, you might 
 * 	have the following init params defined in web.xml:</p>
 * <pre> &lt;init-param&gt;
 * 	&lt;param-name&gt;xsl:test&lt;/param-name&gt;
 * 	&lt;param-value&gt;/WEB-INF/test.xsl&lt;/param-value&gt;
 * &lt;/init-param&gt;</pre>
 * 	<p>In this case, the value <em>test</em> may be used with this
 * 	parameter to use the <code>/WEB-INF/test.xsl</code> XSL source.</p>
 *  </td>
 * </tr>
 * </table>
 * 
 * <p>Created Oct 12, 2002 3:18:59 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class Xform 
extends HttpServlet 
{
	/** 
	 * The init parameter to specify if Xform allows the XML source to be specified
	 * as a request parameter (can be <em>true</em> or <em>false</em>):
	 * <code>allow.url.xml</code>.
	 */
	public final static String INIT_KEY_ALLOW_URL_XML = "allow.url.xml";
	
	/** 
	 * The init parameter to specify if Xform allows the XSL source to be specified
	 * as a request parameter (can be <em>true</em> or <em>false</em>):
	 * <code>allow.url.xsl</code>.
	 */
	public final static String INIT_KEY_ALLOW_URL_XSL = "allow.url.xsl";
	
	/** The init parameter prefix to use for XSL templates. */
	public final static String INIT_KEY_XSL_PREFIX = "xsl:";
	
	/** The request parameter key for specifying an XML file to use. */
	public final static String REQ_KEY_XML_FILE = "xml";
	
	/** The request parameter key for specifying an XSL file to use. */
	public final static String REQ_KEY_XSL_FILE = "xsl";
	
	/** The request parameter key for specifying an XSL Templates key to use. */
	public final static String REQ_KEY_XSL_TEMPLATES_KEY = "t";
	
	/** The request parameter key for specifying that the XSL Templates should be regenerated. */
	public final static String REQ_KEY_XSL_RELOAD_XSL = "regenXsl";
	
	/** The XSL parameter for the HTTP user agent, set automatically: <code>user-agent</code>. */
	public final static String XSL_PARAM_USER_AGENT = "user-agent";
	
	/** The XSL parameter for the HTTP web context path, set automatically: <code>web-context</code>. */
	public final static String XSL_PARAM_WEB_CONTEXT = "web-context";
	
	/** The XSL parameter for the HTTP server name, set automatically: <code>server-name</code> */
	public final static String XSL_PARAM_SERVER_NAME = "server-name";
	
	/** The XSL parameter for the HTTP server port, set automatically: <code>server-port</code> */
	public final static String XSL_PARAM_SERVER_PORT = "server-port";
	
	/** The XSL parameter for the user's locale, set automatically: <code>user-locale</code> */
	public final static String XSL_PARAM_USER_LOCALE = "user-locale";
	
	private final static Logger LOG = Logger.getLogger(Xform.class);
	private final static Logger LOG_THEME = Logger.getLogger(Xform.class.getName()+".THEME");
	
	private Map templatesMap = null;
	private Map msgMap = null;
	private Map templatesFileMap = null;
	private TransformerFactory tFactory = null;
	
	private boolean allowUrlXml = false;
	private boolean allowUrlXsl = false;
	private SimpleCache themeCache = null;
	//private ThemeBiz themeBiz = null;
	private Map reservedRequestParameters = null;

public void init(ServletConfig config)
throws ServletException
{
	super.init(config);
	
	LOG.info("Initializing Xform");
	
	this.allowUrlXml = StringUtil.parseBoolean(config.getInitParameter(INIT_KEY_ALLOW_URL_XML));
	if ( LOG.isInfoEnabled() ) {
		LOG.info("URL parameter support for XML source is " +
			(this.allowUrlXml ? "ON" : "OFF") );
	}

	this.allowUrlXsl = StringUtil.parseBoolean(config.getInitParameter(INIT_KEY_ALLOW_URL_XSL));
	if ( LOG.isInfoEnabled() ) {
		LOG.info("URL parameter support for XSL source is " +
			(this.allowUrlXsl ? "ON" : "OFF") );
	}
	
	msgMap = new HashMap();
	templatesMap = new HashMap();
	templatesFileMap = new HashMap();
	Enumeration enum = config.getInitParameterNames();
	tFactory = TransformerFactory.newInstance();
	while ( enum.hasMoreElements() ) {
		String name = (String)enum.nextElement();
		if ( name.startsWith(INIT_KEY_XSL_PREFIX) ) {
			String key = name.substring(INIT_KEY_XSL_PREFIX.length());
			String xslFilePath = config.getInitParameter(name);
			try {
				Templates templates = tFactory.newTemplates(getResourceSource(xslFilePath));
				if ( LOG.isInfoEnabled() ) {
					LOG.info("Initialized XSL template " +key +" from " +xslFilePath);
				}
				templatesMap.put(key,templates);
				templatesFileMap.put(key,xslFilePath);
			} catch ( Exception e ) {
				LOG.error("Unable to initialize XSL template " +key +" from " +xslFilePath
					+": " +e.getMessage(),e);
			}
		}
	}
	
	// initialize the default theme XSL
	generateDefaultThemeTemplates();
	
	try {
		// initialize the theme cache
		themeCache = ServletUtil.getCacheFactoryCache(config.getServletContext(),
			ApplicationConstants.CacheFactoryKeys.THEME);
		if ( themeCache != null && LOG.isInfoEnabled() ) {
			LOG.info("Theme cache: " +CacheFactory.getInfoString(themeCache));
		}
	} catch ( MediaAlbumException e ) {
		LOG.fatal("Unable to initialize theme cache: " +e.getMessage());
		throw new ServletException("Unable to initialize theme cache.");
	}
	
	// initialize reserved request param map
	reservedRequestParameters = new HashMap(10);
	reservedRequestParameters.put(REQ_KEY_XML_FILE,null);
	reservedRequestParameters.put(REQ_KEY_XSL_FILE,null);
	reservedRequestParameters.put(REQ_KEY_XSL_RELOAD_XSL,null);
	reservedRequestParameters.put(REQ_KEY_XSL_TEMPLATES_KEY,null);
	reservedRequestParameters.put(XSL_PARAM_SERVER_NAME,null);
	reservedRequestParameters.put(XSL_PARAM_SERVER_PORT,null);
	reservedRequestParameters.put(XSL_PARAM_USER_AGENT,null);
	reservedRequestParameters.put(XSL_PARAM_WEB_CONTEXT,null);	
}

/**
 * Generate the default theme XSL and save in templates map.
 * 
 * @return the theme templates object
 * @throws ServletException if an error occurs
 */
private Templates generateDefaultThemeTemplates() throws ServletException 
{
	// initialize the default theme XSL
	try {
		BizFactory bizFactory = ServletUtil.getBizIntfFactory(getServletContext());
		ThemeBiz themeBiz = (ThemeBiz)bizFactory.getBizInstance(
				BizConstants.THEME_BIZ);
		AlbumTheme defaultTheme = themeBiz.getDefaultAlbumTheme();
		Templates albumTemplate = this.getThemeXslTemplates(
				ServletConstants.THEME_XSL_PATH_PREFIX+defaultTheme.getBaseDir()+defaultTheme.getXsl(),
				ServletConstants.THEME_XSL_HEADER_ALBUM,
				ServletConstants.THEME_XSL_FOOTER_ALBUM );
		templatesMap.put(ServletConstants.THEME_DEFAULT_TEMPLATE_KEY,albumTemplate);
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Initialized default theme album XSL template " 
					+ServletConstants.THEME_DEFAULT_TEMPLATE_KEY
					+" from " +ServletConstants.THEME_XSL_PATH_PREFIX
					+defaultTheme.getBaseDir()+defaultTheme.getXsl());
		}
		return albumTemplate;
	} catch ( Exception e ) {
		LOG.fatal("Unable to initialize default theme XSL from " 
				+ServletConstants.THEME_XSL_PATH_PREFIX
				+": " +e.getMessage(),e);
		throw new ServletException("Unable to initialize XSL.");
	}
}
	
public final void doGet(HttpServletRequest req, HttpServletResponse res) 
throws ServletException, IOException
{
	handleRequest(req,res);
}

public final void doPost(HttpServletRequest req, HttpServletResponse res) 
throws ServletException, IOException
{
	handleRequest(req,res);
}

private void handleRequest(HttpServletRequest req, HttpServletResponse res)
throws ServletException, IOException
{
	String xmlFilePath = allowUrlXml ? req.getParameter(REQ_KEY_XML_FILE) : null;
	String xslFilePath = allowUrlXsl ? req.getParameter(REQ_KEY_XSL_FILE) : null;
	String xslTemplatesKey = allowUrlXsl ? req.getParameter(REQ_KEY_XSL_TEMPLATES_KEY) : null;
	
	Source xmlSource = null;
	
	if ( xmlFilePath != null ) {
		xmlSource = this.getResourceSource(xmlFilePath);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Got XML source file: " +xmlFilePath);
		}
	} else if ( req.getAttribute(ServletConstants.REQ_ATTR_XFORM_DOM) != null ) {
		// check for DOM at special key
		xmlSource = this.getSource(req.getAttribute(ServletConstants.REQ_ATTR_XFORM_DOM));
	}
	
	if ( xmlSource == null ) {
		LOG.error("No XML source obtained.");
		res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return;
	}
	
	Transformer transformer = getTransformer(req,xslFilePath,xslTemplatesKey);
	
	// clear any parameters
	transformer.clearParameters();
	
	Object o = req.getAttribute(ServletConstants.REQ_ATTR_XFORM_PARAM);
	if ( o instanceof Map ) {
		Map paramMap = (Map)o;
		for ( Iterator itr = paramMap.entrySet().iterator(); itr.hasNext(); ) {
			Map.Entry me = (Map.Entry)itr.next();
			if ( me.getValue() != null ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Setting XSL param " +me.getKey() +": " +me.getValue());
				}
				transformer.setParameter((String)me.getKey(),me.getValue());
			}
		}
	}
	
	// set implicit parameters
	String ua = req.getHeader("user-agent");
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Setting XSL param " +XSL_PARAM_USER_AGENT +": " +ua);
	}
	transformer.setParameter(XSL_PARAM_USER_AGENT, (ua == null ? "" : ua) );
	
	String webContext = req.getContextPath();
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Setting XSL param " +XSL_PARAM_WEB_CONTEXT +": " +webContext);
	}
	transformer.setParameter(XSL_PARAM_WEB_CONTEXT, (webContext == null ? "" : webContext) );
	
	String serverName = req.getServerName();
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Setting XSL param " +XSL_PARAM_SERVER_NAME +": " +serverName);
	}
	transformer.setParameter(XSL_PARAM_SERVER_NAME, (serverName == null ? "" : serverName));
	
	String serverPort = String.valueOf(req.getServerPort());
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Setting XSL param " +XSL_PARAM_SERVER_PORT +": " +serverPort);
	}
	transformer.setParameter(XSL_PARAM_SERVER_PORT, serverPort);
	
	String userLoc = req.getLocale().toString();
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Setting XSL param " +XSL_PARAM_USER_LOCALE +": " +userLoc);
	}
	transformer.setParameter(XSL_PARAM_USER_LOCALE, userLoc);
	
	// set any additional URL parameters passed via request
	Map paramMap = req.getParameterMap();
	
	Node xnode = xmlSource instanceof DOMSource 
		? ((DOMSource)xmlSource).getNode() : null;
	Document dom = null;
	Element httpRequestElem = null;
	if ( xnode != null ) {
		dom = xnode instanceof Document 
			? (Document)xnode : xnode.getOwnerDocument();

		if ( LOG.isDebugEnabled() && dom != null ) {
			// we debug this BEFORE insert messages into DOM
			XMLUtil.debugDOM(dom,
					"---- START DOM -----\n", "----- END DOM ------\n",
					LOG);
		}
			
		// insert messages into DOM
		Node msgs = dom.importNode(getMessages(dom,req.getLocale()),true);
		dom.getFirstChild().insertBefore(msgs,dom.getFirstChild().getFirstChild());
		httpRequestElem = dom.createElement("x-request");
		dom.getFirstChild().insertBefore(httpRequestElem,dom.getFirstChild().getFirstChild());
	}
	
	for ( Iterator itr = paramMap.keySet().iterator(); itr.hasNext(); ) {
		String key = itr.next().toString();
		if ( !reservedRequestParameters.containsKey(key) ) {
			String[] paramVals = (String[])paramMap.get(key);
			/*String value = ArrayUtil.join(paramVals,',',-1);
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Setting XSL param " +key +": " +value);
			}
			transformer.setParameter(key,value);*/
			if ( httpRequestElem != null ) {
				for ( int i = 0; i < paramVals.length; i++ ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Setting XSL x-request param " +key +": " +paramVals[i]);
					}
					Element e = dom.createElement("param");
					e.setAttribute("key",key);
					e.appendChild(dom.createTextNode(paramVals[i]));
					httpRequestElem.appendChild(e);
				}
			}
		}
	}
	
	res.setContentType("text/html; charset=UTF-8"); // TODO this should be dynamic 
	OutputStream out = res.getOutputStream();
	try {
		transformer.transform(xmlSource, new StreamResult(out));
	} catch ( TransformerException e ) {
		Throwable cause = e;
		while ( cause.getCause() != null ) {
			cause = cause.getCause();
		}
		if ( cause instanceof SAXException ) {
			cause = ((SAXException)cause).getException();
		}
		if ( cause instanceof IOException ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("IOException transforming: " +cause.toString());
			}
		} else {
			LOG.error("Exception transforming: " +e.getMessage(),e);
			throw new ServletException(e);
		}
	} catch ( Exception e ) {
		LOG.error("Exception transforming: " +e.getMessage(),e);
		throw new ServletException(e);
	}
}

private Node getMessages(Document dom, Locale locale) {
	String key = ApplicationConstants.CONFIG_MSG 
		+"." +locale.getLanguage();
	if ( msgMap.containsKey(key) ) {
		return (Node)msgMap.get(key);
	}
	
	synchronized ( msgMap ) {
		if ( msgMap.containsKey(key ) ) {
			return (Node)msgMap.get(key);
		}
		boolean useLang = true;
		
		// locale not loaded, try with lang first
		ConfigurationManager mgr = ConfigurationManager.getInstance(key);
		if ( mgr == null ) {
			if ( msgMap.containsKey(null) ) {
				msgMap.put(key,msgMap.get(null));
				return (Node)msgMap.get(null);
			}
			// not found, try without lang
			mgr = ConfigurationManager.getInstance(ApplicationConstants.CONFIG_MSG);
			useLang = false;
		}
		
		Element msgElem = dom.createElement("x-msg");
		if ( useLang ) {
			Attr lang = dom.createAttributeNS("http://www.w3.org/XML/1998/namespace","xml:lang");
			lang.setValue(locale.getLanguage());
			msgElem.setAttributeNodeNS(lang);
		}
		
		Map m = mgr.getProperties();
		for ( Iterator itr = m.entrySet().iterator(); itr.hasNext(); ) {
			Map.Entry me = (Map.Entry)itr.next();
			Element e = dom.createElement("msg");
			e.setAttribute("key",me.getKey().toString());
			Object val = me.getValue();
			if ( val != null ) {
				e.appendChild(dom.createTextNode(val.toString()));
			}
			msgElem.appendChild(e);
		}
		msgMap.put(key,msgElem);
		if ( !useLang ) {
			msgMap.put(null,msgElem);
		}
		return msgElem;
	}
}

private Transformer getTransformer( 
	HttpServletRequest req,
	String xslFilePath,
	String xslTemplatesKey ) 
throws ServletException {

	// If the resource is specified, use that for the input source
	Object xslSource = null;
	if ( xslFilePath != null ) {
		xslSource = getResourceSource(xslFilePath);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Got XSL source file: " +xslFilePath);
		}
	} else if ( xslTemplatesKey != null 
		|| req.getAttribute(ServletConstants.REQ_ATTR_XFORM_XSL) != null ) {
		if ( xslTemplatesKey == null ) {
			xslTemplatesKey = (String)req.getAttribute(ServletConstants.REQ_ATTR_XFORM_XSL);
		}
		xslSource = templatesMap.get(xslTemplatesKey);
		if ( xslSource == null ) {
			LOG.error("No Tempaltes object associated with key " +xslTemplatesKey);
			throw new ServletException("No Templates object associated with key " +xslTemplatesKey);
		}
		
		// handle regenXsl support (good for development)
		String regenXsl = allowUrlXsl ? req.getParameter(REQ_KEY_XSL_RELOAD_XSL) : null;
		if ( regenXsl != null ) {
			if ( xslTemplatesKey.equals(ServletConstants.THEME_DEFAULT_TEMPLATE_KEY)) {
				xslSource = generateDefaultThemeTemplates();
			} else {
				try {
					Templates templates = tFactory.newTemplates(getResourceSource(
							(String)templatesFileMap.get(xslTemplatesKey)));
					if ( LOG.isInfoEnabled() ) {
						LOG.info("Initialized XSL template " +xslTemplatesKey 
								+" from " +templatesFileMap.get(xslTemplatesKey));
					}
					templatesMap.put(xslTemplatesKey,templates);
					xslSource = templates;
				} catch ( Exception e ) {
					LOG.error("Unable to initialize XSL template " +xslTemplatesKey +" from " +xslFilePath
							+": " +e.getMessage(),e);
					throw new ServletException("Unable to regen XSL",e);
				}
				
			}
		}
		
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Got Templates object from key " +xslTemplatesKey);
		}	
	} else if ( req.getAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME) != null ) {
		// get a theme XSL
		String themeXslPath = (String)req.getAttribute(
			ServletConstants.REQ_ATTR_XFORM_XSL_THEME);
		if ( themeCache != null && req.getParameter(REQ_KEY_XSL_RELOAD_XSL) == null ) {
			xslSource = themeCache.get(themeXslPath);
		}
		if ( xslSource == null ) {
			// not in cache, or cache disabled, so generate now
			xslSource = getThemeXslTemplates(
				themeXslPath,
				(String)req.getAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME_HEADER),
				(String)req.getAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME_FOOTER));
			if ( themeCache != null ) {
				themeCache.put(themeXslPath,xslSource);
			}
		}
	}
	
	if ( xslSource == null ) {
		LOG.error("No XSL source from xslFilePath=" +xslFilePath +", xslTemplatesKey="
			+xslTemplatesKey);
		throw new ServletException("Unable to obtain XSL any source.");
	}
	
	if ( xslSource instanceof Templates ) {
		try {
			return ((Templates)xslSource).newTransformer();
		} catch ( TransformerException e ) {
			throw new ServletException( e.toString() );
		}
	}

	// do transform from scratch
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Creating new transformer from source " +xslSource);
	}
	TransformerFactory tFactory = TransformerFactory.newInstance();
	try {
		Source s = getSource(xslSource);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Got XSL source " +s);
		}
		return tFactory.newTransformer( s );
	} catch ( TransformerException e ) {
		throw new ServletException(e.toString());
	}		

} // getTransformer( String, String, String )


/**
 * Construct a Templates object for a given XSL theme.
 * @param themeXslPath
 * @param headerXslPath
 * @param footerXslPath
 * @return a Templates object for the theme's XSL
 * @throws ServletException
 */
private Templates getThemeXslTemplates(
	String themeXslPath,
	String headerXslPath,
	String footerXslPath) 
	throws ServletException
{
	StringBuffer xslBuffer = new StringBuffer();

	ServletContext context = this.getServletContext();
	String systemId = null;
	try {
		// slurp up header...
		URL url = context.getResource(headerXslPath);
		FileUtil.slurp(url,xslBuffer);
		
		// get theme (and use this for system id)
		url = context.getResource(themeXslPath);
		if ( url == null ) {
			throw new ServletException("Could not get theme XSL: " +themeXslPath);
		}
		String urlPath = url.toString();
		systemId = urlPath.substring(0,urlPath.lastIndexOf('/')+1);
		FileUtil.slurp(url,xslBuffer);
		
		// get footer
		url = context.getResource(footerXslPath);
		FileUtil.slurp(url,xslBuffer);
	} catch (Exception e) {
		LOG.error("Can't get theme XSL: " +e.getMessage());
		throw new ServletException("Could not get theme XSL: " +e.getMessage());
	}
	
	if ( LOG_THEME.isDebugEnabled() ) {
		LOG_THEME.debug("Theme XSL for " +themeXslPath +"\n" +xslBuffer.toString());
	}
	
	try {
		return tFactory.newTemplates(new StreamSource(
			new StringReader(xslBuffer.toString()),systemId));
	} catch ( Exception e ) {
		LOG.error("Transformer exception from theme XSL: " +e.toString());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("XSL system ID: " +systemId +", buffer: " +xslBuffer.toString());
		}
		throw new ServletException("Unable to parse theme XSL: " +e.toString());
	}
}

private Source getResourceSource( String resource ) throws ServletException {

	if ( resource == null ) return null;

	ServletContext context = this.getServletContext();
	if (context == null) {
		throw new ServletException("Cannot find servlet context");
	}
	
	URL url = null;
	try {
		url = context.getResource(resource);
	} catch (Exception e) {
		LOG.error("Can't get URL: " +e.getMessage());
	}
	
	if ( url == null ) {
		return null;
	}
	
	String urlPath = url.toString();
	String systemId = urlPath.substring(0,urlPath.lastIndexOf('/')+1);
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("System ID for resource " +resource +" is " +systemId);
	}

	try {
		return new StreamSource(url.openStream(),systemId);
	} catch ( IOException e ) {
		throw new ServletException("IOException gettting resource " +resource
			+": " +e.getMessage());
	}
}

private Source getSource( Object source ) throws ServletException {

	// Create an XSLTInputSource for the specified source object
	if (source instanceof Source)
	    return ((Source) source);
	else if (source instanceof String)
	    return (new StreamSource(new StringReader((String) source)));
	else if (source instanceof InputSource)
	    return (new SAXSource((InputSource) source));
	else if (source instanceof InputStream)
	    return (new StreamSource((InputStream) source));
	else if (source instanceof Node)
	    return (new DOMSource((Node) source));
	else if (source instanceof Reader)
	    return (new StreamSource((Reader) source));
	else
	    throw new ServletException("Invalid input source type '" +
				   source.getClass().getName() + "'");
	
}

}
