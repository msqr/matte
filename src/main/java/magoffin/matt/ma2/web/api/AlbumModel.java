/* ===================================================================
 * AlbumModel.java
 * 
 * Created Feb 3, 2015 7:47:56 PM
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

import magoffin.matt.ma2.domain.Model;

/**
 * Album model object.
 *
 * @author matt
 * @version 1.0
 */
public class AlbumModel {

	private String displayAlbumKey;
	private Long displayItemId;
	private Model model;

	public String getDisplayAlbumKey() {
		return displayAlbumKey;
	}

	public void setDisplayAlbumKey(String displayAlbumKey) {
		this.displayAlbumKey = displayAlbumKey;
	}

	public Long getDisplayItemId() {
		return displayItemId;
	}

	public void setDisplayItemId(Long displayItemId) {
		this.displayItemId = displayItemId;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
