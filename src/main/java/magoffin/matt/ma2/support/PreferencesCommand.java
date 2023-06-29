/* ===================================================================
 * PreferencesCommand.java
 * 
 * Created Jan 7, 2007 10:45:56 AM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

import magoffin.matt.ma2.domain.MediaSpec;
import magoffin.matt.util.TemporaryFile;

/**
 * Command object for storing user preferences.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class PreferencesCommand {

	private Long userId;
	private MediaSpec thumb;
	private MediaSpec view;
	private Long browseThemeId;
	private String timeZone;
	private String locale;
	private TemporaryFile watermarkFile;
	private boolean deleteWatermark = false;
	
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

	/**
	 * @return the thumb
	 */
	public MediaSpec getThumb() {
		return thumb;
	}
	
	/**
	 * @param thumb the thumb to set
	 */
	public void setThumb(MediaSpec thumb) {
		this.thumb = thumb;
	}
	
	/**
	 * @return the view
	 */
	public MediaSpec getView() {
		return view;
	}
	
	/**
	 * @param view the view to set
	 */
	public void setView(MediaSpec view) {
		this.view = view;
	}
	
	/**
	 * @return the browseThemeId
	 */
	public Long getBrowseThemeId() {
		return browseThemeId;
	}
	
	/**
	 * @param browseThemeId the browseThemeId to set
	 */
	public void setBrowseThemeId(Long browseThemeId) {
		this.browseThemeId = browseThemeId;
	}

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the watermarkFile
	 */
	public TemporaryFile getWatermarkFile() {
		return watermarkFile;
	}

	/**
	 * @param watermarkFile the watermarkFile to set
	 */
	public void setWatermarkFile(TemporaryFile watermarkFile) {
		this.watermarkFile = watermarkFile;
	}

	/**
	 * @return the deleteWatermark
	 */
	public boolean isDeleteWatermark() {
		return deleteWatermark;
	}

	/**
	 * @param deleteWatermark the deleteWatermark to set
	 */
	public void setDeleteWatermark(boolean deleteWatermark) {
		this.deleteWatermark = deleteWatermark;
	}

}
