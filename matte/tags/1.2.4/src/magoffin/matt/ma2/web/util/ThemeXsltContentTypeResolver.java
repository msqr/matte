/* ===================================================================
 * ThemeXsltContentTypeResolver.java
 * 
 * Created Aug 27, 2010 11:21:59 AM
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

package magoffin.matt.ma2.web.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.xweb.util.ContentTypeResolver;

/**
 * Resolve the XSLT content type for theme resources.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class ThemeXsltContentTypeResolver implements ContentTypeResolver {
	
	private String defaultType = "text/plain";

	public String resolveContentType(HttpServletRequest request,
			Map<String, ?> model) {
		String type = (String)request.getAttribute(WebConstants.REQ_KEY_THEME_RESOURCE_CONTENT_TYPE);
		return type == null ? defaultType : type;
	}

	/**
	 * @return the defaultType
	 */
	public String getDefaultType() {
		return defaultType;
	}

	/**
	 * @param defaultType the defaultType to set
	 */
	public void setDefaultType(String defaultType) {
		this.defaultType = defaultType;
	}

}
