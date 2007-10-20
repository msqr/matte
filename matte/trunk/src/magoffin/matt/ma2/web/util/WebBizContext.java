/* ===================================================================
 * WebBizContext.java
 * 
 * Created Oct 5, 2004 5:10:32 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma2.support.BasicBizContext;
import magoffin.matt.xweb.util.AppContextSupport;

/**
 * BizContext for web layer.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class WebBizContext extends BasicBizContext {
	
	/**
	 * The attribute key for the webapp base URL.
	 */
	public static final String URL_BASE = "magoffin.matt.ma2.web.URL_BASE";

	/**
	 * Constructor.
	 * 
	 * @param request the HTTP request
	 * @param appContextSupport
	 */
	public WebBizContext(HttpServletRequest request, AppContextSupport appContextSupport) {
		super(appContextSupport);
		setLocale(request.getLocale());
	}
	
}
