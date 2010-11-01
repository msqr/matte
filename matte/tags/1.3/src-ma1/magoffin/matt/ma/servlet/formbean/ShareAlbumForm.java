/* ===================================================================
 * ShareAlbumForm.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: ShareAlbumForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

/**
 * Form bean for sharing an album.
 * 
 * <p>Created Oct 25, 2002 4:21:40 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ShareAlbumForm extends ValidatorForm 
{
	private Integer album = null;
	private Integer[] groups = null;
	private Integer[] friends = null;
	private Group[] groupList = null;
	private User[] friendList = null;
	private Boolean allowAnonymous;
	private Boolean allowOriginal;
	private Boolean recurse;
	private String submitAction;
	private Album mediaAlbum;
	private boolean shared = false;
	private boolean hasChildren = false;
	
	/**
	 * Returs <em>true</em> if any friends or groups are available 
	 * in the <code>friendList</v> or <code>groupList</code>.
	 * @return boolean
	 */
	public boolean isHasFriendsOrGroupsAvailable() {
		return groupList != null && groupList.length > 0 
				&& friendList != null && friendList.length > 0
				? true : false;
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
	 * @return Returns the mediaAlbum.
	 */
	public Album getMediaAlbum() {
		return mediaAlbum;
	}

	/**
	 * @param mediaAlbum The mediaAlbum to set.
	 */
	public void setMediaAlbum(Album mediaAlbum) {
		this.mediaAlbum = mediaAlbum;
	}

	/**
	 * @return Returns the submitAction.
	 */
	public String getSubmitAction() {
		return submitAction;
	}

	/**
	 * @param submitAction The submitAction to set.
	 */
	public void setSubmitAction(String submitAction) {
		this.submitAction = submitAction;
	}

	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	public void reset(ActionMapping arg0, HttpServletRequest arg1) {
		super.reset(arg0, arg1);
		allowAnonymous = Boolean.FALSE;
		allowOriginal = Boolean.FALSE;
		recurse = Boolean.FALSE;
		friends = new Integer[0];
		groups = new Integer[0];
	}

	/**
	 * @return Returns the allowAnonymous.
	 */
	public Boolean getAllowAnonymous() {
		return allowAnonymous;
	}

	/**
	 * @param allowAnonymous The allowAnonymous to set.
	 */
	public void setAllowAnonymous(Boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
	}

	/**
	 * @return Returns the allowOriginal.
	 */
	public Boolean getAllowOriginal() {
		return allowOriginal;
	}

	/**
	 * @param allowOriginal The allowOriginal to set.
	 */
	public void setAllowOriginal(Boolean allowOriginal) {
		this.allowOriginal = allowOriginal;
	}

	/**
	 * @return Returns the recurse.
	 */
	public Boolean getRecurse() {
		return recurse;
	}

	/**
	 * @param recurse The recurse to set.
	 */
	public void setRecurse(Boolean recurse) {
		this.recurse = recurse;
	}

	/**
	 * @return Returns the friendList.
	 */
	public User[] getFriendList() {
		return friendList;
	}

	/**
	 * @param friendList The friendList to set.
	 */
	public void setFriendList(User[] friendList) {
		this.friendList = friendList;
	}

	/**
	 * @return Returns the friends.
	 */
	public Integer[] getFriends() {
		return friends;
	}

	/**
	 * @param friends The friends to set.
	 */
	public void setFriends(Integer[] friends) {
		this.friends = friends;
	}

	/**
	 * @return Returns the groupList.
	 */
	public Group[] getGroupList() {
		return groupList;
	}

	/**
	 * @param groupList The groupList to set.
	 */
	public void setGroupList(Group[] groupList) {
		this.groupList = groupList;
	}

	/**
	 * @return Returns the groups.
	 */
	public Integer[] getGroups() {
		return groups;
	}

	/**
	 * @param groups The groups to set.
	 */
	public void setGroups(Integer[] groups) {
		this.groups = groups;
	}

	/**
	 * @return Returns the shared.
	 */
	public boolean isShared()
	{
		return shared;
	}
	/**
	 * @param shared The shared to set.
	 */
	public void setShared(boolean shared)
	{
		this.shared = shared;
	}
	
	/**
	 * @return Returns the hasChildren.
	 */
	public boolean isHasChildren() {
		return hasChildren;
	}
	
	/**
	 * @param hasChildren The hasChildren to set.
	 */
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
}
