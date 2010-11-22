/* ===================================================================
 * UserResourceController.java
 * 
 * Copyright (c) 2007 Matt Magoffin.
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
import java.io.FileInputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for returning Matte user resources.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class UserResourceController extends AbstractCommandController {

	private FileTypeMap fileTypeMap = new MimetypesFileTypeMap();
	
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		if ( cmd.getUserId() == null || !StringUtils.hasText(cmd.getResource()) ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		File baseDir = new File(getSystemBiz().getResourceDirectory(), 
				cmd.getUserId().toString());
		File resource = new File(baseDir, cmd.getResource());
		if ( !resource.exists() || !resource.isFile() ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		String mime = fileTypeMap.getContentType(resource);
		if ( mime != null ) {
			response.setContentType(mime);
		}
		response.setContentLength((int)resource.length());
		FileCopyUtils.copy(new FileInputStream(resource), response.getOutputStream());
		return null;
	}

	/** Command object. */
	public static class Command {
		private Long userId;
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
		 * @return the userId
		 */
		public Long getUserId() {
			return userId;
		}

		/**
		 * @param userId the userId to set
		 */
		public void setUserId(Long userId) {
			this.userId = userId;
		}

	}

	/**
	 * @return the fileTypeMap
	 */
	public FileTypeMap getFileTypeMap() {
		return fileTypeMap;
	}

	/**
	 * @param fileTypeMap the fileTypeMap to set
	 */
	public void setFileTypeMap(FileTypeMap fileTypeMap) {
		this.fileTypeMap = fileTypeMap;
	}

}
