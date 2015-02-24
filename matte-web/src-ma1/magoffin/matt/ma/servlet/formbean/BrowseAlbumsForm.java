/* ===================================================================
 * BrowseForm.java
 * 
 * Created Feb 5, 2004 3:50:52 PM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: BrowseAlbumsForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import org.apache.struts.action.ActionForm;

/**
 * Form bean for browsing album actions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class BrowseAlbumsForm extends ActionForm {
	
	private Integer theme = null;
	private Integer browsePage = null;
	private String key = null;
	private Integer pageSize = null;
	private Integer album = null;
	private Integer mitem = null;

/**
 * @return Returns the theme.
 */
public Integer getTheme() {
	return theme;
}

/**
 * @param theme The theme to set.
 */
public void setTheme(Integer theme) {
	this.theme = theme;
}

/**
 * FIXME remove
 * @return Returns the page.
 */
public Integer getPage() {
	return browsePage;
}

/**
 * FIXME remove
 * @param page The page to set.
 */
public void setPage(Integer page) {
	this.browsePage = page;
}

/**
 * @return Returns the key.
 */
public String getKey() {
	return key;
}

/**
 * @param key The key to set.
 */
public void setKey(String key) {
	this.key = key;
}

/**
 * @return Returns the pageSize.
 */
public Integer getPageSize() {
	return pageSize;
}

/**
 * @param pageSize The pageSize to set.
 */
public void setPageSize(Integer pageSize) {
	this.pageSize = pageSize;
}

/**
 * @return Returns the album.
 */
public Integer getAlbum() {
	return album;
}

/**
 * @param album The album to set.
 */
public void setAlbum(Integer album) {
	this.album = album;
}

/**
 * @return Returns the item.
 */
public Integer getMitem() {
	return mitem;
}

/**
 * @param item The item to set.
 */
public void setMitem(Integer item) {
	this.mitem = item;
}

/**
 * @return Returns the browsePage.
 */
public Integer getBrowsePage() {
	return browsePage;
}
/**
 * @param browsePage The browsePage to set.
 */
public void setBrowsePage(Integer browsePage) {
	this.browsePage = browsePage;
}
}
