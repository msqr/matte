/* ===================================================================
 * UserSessionData.java
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
 * $Id: UserSessionData.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.ma.xsd.User;

/**
 * Data object for a user on the site.
 * 
 * <p>Created Oct 8, 2002 3:13:03 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class UserSessionData 
{
	private boolean loggedIn = false;
	private User user = null;
	private Group[] groups = null;
	private Collection[] collections = null;
	private MediaSpec thumbSpec = null;
	private MediaSpec singleSpec = null;
	private boolean admin = false;
	private Lightbox lightbox = null;
	
/**
 * @return Returns the lightbox.
 */
public Lightbox getLightbox() {
	return lightbox;
}
/**
 * @param lightbox The lightbox to set.
 */
public void setLightbox(Lightbox lightbox) {
	this.lightbox = lightbox;
}
/**
 * Returns the loggedIn.
 * @return boolean
 */
public boolean isLoggedIn() {
	return loggedIn;
}

/**
 * Returns the user.
 * @return User
 */
public User getUser() {
	return user;
}

/**
 * Sets the loggedIn.
 * @param loggedIn The loggedIn to set
 */
public void setLoggedIn(boolean loggedIn) {
	this.loggedIn = loggedIn;
}

/**
 * Sets the user.
 * @param user The user to set
 */
public void setUser(User user) {
	this.user = user;
}

/**
 * Returns the dirs.
 * @return Collection[]
 */
public Collection[] getCollections() {
	return collections;
}

/**
 * Sets the collections.
 * @param collections The collections to set
 */
public void setCollections(Collection[] collections) {
	this.collections = collections;
}

/**
 * Returns the singleSpec.
 * @return MediaSpec
 */
public MediaSpec getSingleSpec() {
	return singleSpec;
}

/**
 * Returns the thumbSpec.
 * @return MediaSpec
 */
public MediaSpec getThumbSpec() {
	return thumbSpec;
}

/**
 * Sets the singleSpec.
 * @param singleSpec The singleSpec to set
 */
public void setSingleSpec(MediaSpec singleSpec) {
	this.singleSpec = singleSpec;
}

/**
 * Sets the thumbSpec.
 * @param thumbSpec The thumbSpec to set
 */
public void setThumbSpec(MediaSpec thumbSpec) {
	this.thumbSpec = thumbSpec;
}



/**
 * Return a string representation of this object.
 * @return String suitable for debugging
 */
public String toString() {
	if ( user == null ) {
		return "UserSessionData{user=null}";
	}
	return "UserSessionData{user.name="
		+user.getName()
		+",user.id=" +user.getUserId()
		+",user.username=" +user.getUsername()
		+(thumbSpec == null ? "" :
			",thumbSpec.width="  +thumbSpec.getWidth()
			+",thumbSpec.height=" +thumbSpec.getHeight())
		+(singleSpec == null ? "" :
			",singleSpec.width="  +singleSpec.getWidth()
			+",singleSpec.height=" +singleSpec.getHeight())
		+",loggedIn=" +loggedIn
		+",admin=" +admin
		+"}";
		
}

/**
 * Returns the admin.
 * @return boolean
 */
public boolean isAdmin() {
	return admin;
}

/**
 * Sets the admin.
 * 
 * @param admin The admin to set
 */
public void setAdmin(boolean admin) {
	this.admin = admin;
}

/**
 * Returns the groups.
 * @return Group[]
 */
public Group[] getGroups() {
	return groups;
}

/**
 * Sets the groups.
 * @param groups The groups to set
 */
public void setGroups(Group[] groups) {
	this.groups = groups;
}

} // class UserSessionData
