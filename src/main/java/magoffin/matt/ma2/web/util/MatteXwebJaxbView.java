/* ===================================================================
 * MatteXwebJaxbView.java
 * 
 * Created Feb 5, 2015 5:37:18 PM
 * 
 * Copyright (c) 2015 Matt Magoffin.
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

package magoffin.matt.ma2.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.SimpleTransformErrorListener;
import magoffin.matt.xweb.util.ContentTypeResolver;
import magoffin.matt.xweb.util.XwebJaxbView;

/**
 * Extension of {@link XwebJaxbView} to support custom XSLT content types.
 * 
 * <p>
 * This class implements {@link ContentTypeResolver} itself, and will look in an
 * optional properties file named the same as the configured XSLT resource for a
 * property named {@code http.contentType} that will be used as the resolved
 * HTTP {@code Content-Type}. If such a property is not available, then the
 * configured {@link ContentTypeResolver} set via
 * {@link #setContentTypeResolver(ContentTypeResolver)} will be used to resolve
 * the content type.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public class MatteXwebJaxbView extends XwebJaxbView implements ContentTypeResolver {

	/**
	 * The property name for a HTTP Content-Type value to use for the response.
	 */
	public static final String PROP_HTTP_CONTENT_TYPE = "http.contentType";

	private boolean cacheProperties = true;
	private boolean propsLoaded;
	private String customContentType;
	private ContentTypeResolver delegateContentTypeResolver;

	/**
	 * Default constructor.
	 */
	@SuppressWarnings("deprecation")
	public MatteXwebJaxbView() {
		super();
		super.setContentTypeResolver(this);
		setErrorListener(new SimpleTransformErrorListener(logger) {

			@Override
			public void fatalError(TransformerException ex) throws TransformerException {
				// also log fatal exceptions, which the superclass does not do
				logger.error("Transformer fatal error in [" + getStylesheetLocation() + "]", ex);
				super.fatalError(ex);
			}

		});
	}

	@Override
	public String resolveContentType(HttpServletRequest request, Map<String, ?> model) {
		if ( propsLoaded == false ) {
			Properties p = new Properties();
			@SuppressWarnings("deprecation")
			Resource xslt = getStylesheetLocation();
			try {
				String fileName = xslt.getFilename();
				String xsltExtension = StringUtils.getFilenameExtension(fileName);
				if ( xsltExtension != null ) {
					fileName = fileName.substring(0, fileName.length() - xsltExtension.length());
				}
				fileName += "properties";
				Resource props = xslt.createRelative(fileName);
				if ( props.exists() ) {
					InputStream in = null;
					try {
						in = props.getInputStream();
						p.load(in);
					} finally {
						if ( in != null ) {
							in.close();
						}
					}
				}
			} catch ( IOException e ) {
				logger.debug("IOException reading properties for XSLT resource " + xslt + ": "
						+ e.getMessage());
				// ignore
			}
			customContentType = p.getProperty(PROP_HTTP_CONTENT_TYPE);
			if ( cacheProperties ) {
				propsLoaded = true;
			}
		}
		if ( customContentType != null ) {
			return customContentType;
		}
		if ( delegateContentTypeResolver != null ) {
			return delegateContentTypeResolver.resolveContentType(request, model);
		}
		return getContentType();
	}

	@Override
	public void setContentTypeResolver(ContentTypeResolver contentTypeResolver) {
		delegateContentTypeResolver = contentTypeResolver;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setCache(boolean cache) {
		super.setCache(cache);
		cacheProperties = cache;
	}

}
