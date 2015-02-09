/* ===================================================================
 * DownloadThemeController.java
 * 
 * Created Sep 28, 2006 3:35:59 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.Theme;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to download a Theme archive.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class DownloadThemeController extends AbstractCommandController {

	private String baseThemePath = "/WEB-INF/themes";
	private String contentType = "application/zip";
	//private String themeXslFileName = "theme.xsl";

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		BizContext context = getWebHelper().getBizContext(request, true);

		Theme theme = getSystemBiz().getThemeById(cmd.getThemeId());
		
		// check first for internal theme
		//String internalResourcePath = baseThemePath
		//	+theme.getBasePath() +"/" +themeXslFileName;
		File internalThemeBasePath = new File(
				getServletContext().getRealPath(baseThemePath));
		File internalThemeDir = new File(internalThemeBasePath, 
				theme.getBasePath());
		File themeDir = internalThemeDir.exists()
			? internalThemeBasePath : null;
		response.setContentType(this.contentType);
		response.setHeader("Content-Disposition","attachment; filename=\"" 
				+theme.getName()+" ThemePak.zip\"");
		getSystemBiz().exportTheme(theme, response.getOutputStream(), themeDir, 
				context);
		return null;
	}

	/** Command object. */
	public static class Command {
		private Long themeId;

		/**
		 * @return Returns the themeId.
		 */
		public Long getThemeId() {
			return themeId;
		}
		
		/**
		 * @param themeId The themeId to set.
		 */
		public void setThemeId(Long themeId) {
			this.themeId = themeId;
		}
		
	}
	
	/**
	 * @return the baseThemePath
	 */
	public String getBaseThemePath() {
		return baseThemePath;
	}
	
	/**
	 * @param baseThemePath the baseThemePath to set
	 */
	public void setBaseThemePath(String baseThemePath) {
		this.baseThemePath = baseThemePath;
	}
	
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
	
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
