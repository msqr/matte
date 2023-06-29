/* ===================================================================
 * WebBizContextFilter.java
 * 
 * Created Feb 4, 2015 4:51:34 PM
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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.Session;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.xweb.util.AppContextSupport;
import magoffin.matt.xweb.util.XwebConstants;

/**
 * Filter that attaches a {@link WebBizContext} to the request thread.
 * 
 * @author matt
 * @version 1.1
 */
public class WebBizContextFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing to do
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		BizContext activeContext = BizContextUtil.getBizContext();
		if ( activeContext == null && request instanceof HttpServletRequest ) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			AppContextSupport appContextSupport = getAppContextSupport(httpRequest);
			WebBizContext context = new WebBizContext(httpRequest, appContextSupport);
			Session userSession = getUserSession(httpRequest);
			if ( userSession != null && userSession.getActingUser() != null ) {
				context.setActingUser(userSession.getActingUser());
			}
			BizContextUtil.attachBizContext(context);
		}
		// pass the request/response on
		try {
			chain.doFilter(request, response);
		} finally {
			BizContextUtil.removeBizContext();
		}
	}

	private AppContextSupport getAppContextSupport(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if ( session != null ) {
			Object o = session.getServletContext().getAttribute(XwebConstants.APP_KEY_APP_CONTEXT);
			if ( o instanceof AppContextSupport ) {
				return (AppContextSupport) o;
			}
		}
		return null;

	}

	public Session getUserSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if ( session == null )
			return null;
		return (Session) session.getAttribute(WebConstants.SES_KEY_USER_SESSION_DATA);
	}

}
