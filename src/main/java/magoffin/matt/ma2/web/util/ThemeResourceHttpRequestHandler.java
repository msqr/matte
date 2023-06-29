/* ===================================================================
 * ThemeResourceHttpRequestHandler.java
 * 
 * Created Feb 6, 2015 7:45:28 AM
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

import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.domain.Theme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Extension of {@link ResourceHttpRequestHandler} to work with theme resources
 * servied via theme ID paths, e.g. {@code /theme/1/theme.js}.
 *
 * @author matt
 * @version 1.0
 */
public class ThemeResourceHttpRequestHandler extends ResourceHttpRequestHandler {

	@Autowired
	private WebHelper webHelper;

	@Autowired
	private SystemBiz systemBiz;

	// expected path form: /theme/1/ac/qt-test.js

	@Override
	protected String processPath(String path) {
		int idx = path.indexOf('/');
		if ( idx > 0 && idx + 1 < path.length() ) {
			String themeIdString = path.substring(0, idx);
			String resourcePath = path.substring(idx);
			try {
				Long themeId = Long.valueOf(themeIdString);
				Theme t = systemBiz.getThemeById(themeId);
				if ( t != null ) {
					String themeResourcePath = t.getBasePath() + resourcePath;
					return super.processPath(themeResourcePath);
				}
			} catch ( NumberFormatException e ) {
				// not a theme ID, ignore
			}
		}
		return super.processPath(path);
	}

	public WebHelper getWebHelper() {
		return webHelper;
	}

	public void setWebHelper(WebHelper webHelper) {
		this.webHelper = webHelper;
	}

	public SystemBiz getSystemBiz() {
		return systemBiz;
	}

	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}

}
