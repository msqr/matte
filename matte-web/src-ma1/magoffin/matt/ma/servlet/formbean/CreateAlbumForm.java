/* ===================================================================
 * CreateAlbumForm.java
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
 * $Id: CreateAlbumForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

import javax.servlet.http.HttpServletRequest;

/**
 * Form bean for creating a new album.
 * 
 * <p>Created Oct 25, 2002 4:21:40 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class CreateAlbumForm extends ValidatorForm 
{

	private String name;
	private Integer parentAlbumId;
	private String submitAction;

/**
 * Constructor for CreateAlbumForm.
 */
public CreateAlbumForm() {
	super();
}

/**
 * Returns the name.
 * @return String
 */
public String getName() {
	return name;
}

/**
 * Sets the name.
 * @param name The name to set
 */
public void setName(String name) {
	this.name = name;
}


/**
 * Returns the parentAlbumId.
 * @return Integer
 */
public Integer getParentAlbumId() {
	return parentAlbumId;
}


/**
 * Sets the parentAlbumId.
 * 
 * <p>If <var>parentAlbumId</var> is <code>0</code>, 
 * the parentAlbumId will be set to <em>null</em>.</p>
 * 
 * @param parentAlbumId The parentAlbumId to set
 */
public void setParentAlbumId(Integer parentAlbumId) {
	if ( parentAlbumId != null && parentAlbumId.intValue() == 0 ) {
		parentAlbumId = null;
	}
	this.parentAlbumId = parentAlbumId;
}


/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping arg0, HttpServletRequest arg1) {
	super.reset(arg0, arg1);
	this.name = null;
	this.parentAlbumId = null;
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

}
