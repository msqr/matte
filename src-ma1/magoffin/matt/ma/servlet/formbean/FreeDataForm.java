/* ===================================================================
 * FreeDataForm.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 24, 2004 9:34:26 AM.
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
 * $Id: FreeDataForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionMapping;

/**
 * Form bean for maintaining free data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class FreeDataForm extends BaseBounceBackHomeForm 
{
	private FreeData copyright;
	private FreeData keywords;
	private boolean multi = false;
	private boolean userMode = false;
	private User user;
	private Collection cx;
	private Album al;
	private MediaItem item;
	private boolean allowUserCategories;
	
/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) {
	keywords = new FreeData();
	copyright = new FreeData();
}

/**
 * @return Returns the allowUserCategories.
 */
public boolean isAllowUserCategories() {
	return allowUserCategories;
}

/**
 * @param allowUserCategories The allowUserCategories to set.
 */
public void setAllowUserCategories(boolean allowUserCategories) {
	this.allowUserCategories = allowUserCategories;
}

/**
 * @return Returns the album.
 */
public Album getAl() {
	return al;
}

/**
 * @param al The album to set.
 */
public void setAl(Album al) {
	this.al = al;
}

/**
 * @return Returns the item.
 */
public MediaItem getItem() {
	return item;
}

/**
 * @param item The item to set.
 */
public void setItem(MediaItem item) {
	this.item = item;
}

/**
 * @return Returns the user.
 */
public User getUser() {
	return user;
}

/**
 * @param user The user to set.
 */
public void setUser(User user) {
	this.user = user;
}

/**
 * @return Returns the copyright.
 */
public FreeData getCopyright() {
	return copyright;
}

/**
 * @param copyright The copyright to set.
 */
public void setCopyright(FreeData copyright) {
	this.copyright = copyright;
}

/**
 * @return Returns the keywords.
 */
public FreeData getKeywords() {
	return keywords;
}

/**
 * @param keywords The keywords to set.
 */
public void setKeywords(FreeData keywords) {
	this.keywords = keywords;
}

/**
 * @return Returns the cx.
 */
public Collection getCx() {
	return cx;
}

/**
 * @param cx The cx to set.
 */
public void setCx(Collection cx) {
	this.cx = cx;
}

/**
 * @return Returns the multi.
 */
public boolean isMulti() {
	return multi;
}

/**
 * @param multi The multi to set.
 */
public void setMulti(boolean multi) {
	this.multi = multi;
}
/**
 * @return Returns the userMode.
 */
public boolean isUserMode() {
	return userMode;
}
/**
 * @param userMode The userMode to set.
 */
public void setUserMode(boolean userMode) {
	this.userMode = userMode;
}
}
