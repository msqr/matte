/* ===================================================================
 * ThemeResourceController.java
 * 
 * Created Jun 28, 2006 7:29:50 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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

import java.io.IOException;
import java.net.URL;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.Theme;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.LastModified;

/**
 * Controller for returning Matte theme resources.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class ThemeResourceController extends AbstractCommandController implements LastModified {
	
	private String baseThemePath = "/WEB-INF/themes";
	private FileTypeMap fileTypeMap = new MimetypesFileTypeMap();
	
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		if ( cmd.themeId == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		Theme theme = getSystemBiz().getThemeById(cmd.themeId);
		if ( theme == null ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		if ( cmd.getResource() == null ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		URL url = getThemeResource(request, cmd, theme);
		if ( url == null ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		String mime = fileTypeMap.getContentType(url.getPath());
		if ( mime != null ) {
			response.setContentType(mime);
		}
		FileCopyUtils.copy(url.openStream(),response.getOutputStream());
		return null;
	}
	
	private URL getThemeResource(HttpServletRequest request, Command cmd, Theme theme) 
	throws IOException {
		String resourcePath = cmd.getResource();
		resourcePath = baseThemePath +theme.getBasePath()
			+(resourcePath.startsWith("/") ? resourcePath : "/"+resourcePath);
		URL url = getServletContext().getResource(resourcePath);
		if ( url == null ) {
			// try external resource
			BizContext context = getWebHelper().getBizContext(request, false);
			Resource themeResource = getSystemBiz().getThemeResource(theme, 
					cmd.getResource(), context);
			if ( !themeResource.exists() ) {
				return null;
			}
			url = themeResource.getURL();
		}
		return url;
	}

	public long getLastModified(HttpServletRequest request) {
		Command cmd = new Command();
		try {
			ServletRequestDataBinder binder = createBinder(request, cmd);
			binder.bind(request);
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
		Theme theme = getSystemBiz().getThemeById(cmd.themeId);
		if ( theme == null ) {
			return -1;
		}
		try {
			URL url = getThemeResource(request, cmd, theme);
			UrlResource urlResource = new UrlResource(url);
			if ( urlResource.getFile() != null ) {
				return urlResource.getFile().lastModified();
			}
		} catch ( IOException e ) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("IOException checking last-modified: " +e);
			}
		}
		return -1;
	}
	
	/** Command object. */
	public static class Command {
		private Long themeId;
		private String resource;
		
		/**
		 * @return Returns the resource.
		 */
		public String getResource() {
			return resource;
		}
		
		/**
		 * @param resource The resource to set.
		 */
		public void setResource(String resource) {
			this.resource = resource;
		}
		
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
	 * @return Returns the baseThemePath.
	 */
	public String getBaseThemePath() {
		return baseThemePath;
	}

	/**
	 * @param baseThemePath The baseThemePath to set.
	 */
	public void setBaseThemePath(String baseThemePath) {
		this.baseThemePath = baseThemePath;
	}

	/**
	 * @return Returns the fileTypeMap.
	 */
	public FileTypeMap getFileTypeMap() {
		return fileTypeMap;
	}

	/**
	 * @param fileTypeMap The fileTypeMap to set.
	 */
	public void setFileTypeMap(FileTypeMap fileTypeMap) {
		this.fileTypeMap = fileTypeMap;
	}

}
