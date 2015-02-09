/* ===================================================================
 * AlbumCommand.java
 * 
 * Created Feb 3, 2015 7:43:16 PM
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

package magoffin.matt.ma2.web.api;

/**
 * Command class for album related tasks.
 *
 * @author matt
 * @version 1.0
 */
public class AlbumCommand {

	private String key = null;
	private Long themeId = null;
	private Long itemId = null;
	private String childKey = null; // for nested album selection
	private String userKey = null; // for virtual album selection
	private String mode = null; // for virtual album selection

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getThemeId() {
		return themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getChildKey() {
		return childKey;
	}

	public void setChildKey(String childKey) {
		this.childKey = childKey;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
