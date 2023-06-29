/* ===================================================================
 * AddThemeCommand.java
 * 
 * Created Sep 19, 2006 9:43:17 PM
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
 */

package magoffin.matt.ma2.support;

import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.util.TemporaryFile;

/**
 * Command object for adding (or modifying) Theme resources.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class AddThemeCommand {
	
	private TemporaryFile tempFile;
	private Long themeId;
	private Theme theme;
	
	/**
	 * @return the tempFile
	 */
	public TemporaryFile getTempFile() {
		return tempFile;
	}
	
	/**
	 * @param tempFile the tempFile to set
	 */
	public void setTempFile(TemporaryFile tempFile) {
		this.tempFile = tempFile;
	}
	
	/**
	 * @return the theme
	 */
	public Theme getTheme() {
		return theme;
	}
	
	/**
	 * @param theme the theme to set
	 */
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	/**
	 * @return the themeId
	 */
	public Long getThemeId() {
		return themeId;
	}
	
	/**
	 * @param themeId the themeId to set
	 */
	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

}
