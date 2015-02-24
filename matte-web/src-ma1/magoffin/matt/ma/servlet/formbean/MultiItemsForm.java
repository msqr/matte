/* ===================================================================
 * MultiItemsForm.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Jun 13, 2004 10:24:42 PM.
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
 * $Id: MultiItemsForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

/**
 * Form bean for dealing with multiple media items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MultiItemsForm extends BaseBounceBackForm {
	
	private Integer actionId;
	private Boolean booleanFlag;
	
/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) {
	super.reset(mapping, request);
	actionId = null;
	booleanFlag = null;
}

/**
 * @return Returns the booleanFlag.
 */
public Boolean getBooleanFlag() {
	return booleanFlag;
}
/**
 * @param booleanFlag The booleanFlag to set.
 */
public void setBooleanFlag(Boolean booleanFlag) {
	this.booleanFlag = booleanFlag;
}
/**
 * @return Returns the actionId.
 */
public Integer getActionId() {
	return actionId;
}
/**
 * @param actionId The actionId to set.
 */
public void setActionId(Integer actionId) {
	this.actionId = actionId;
}
}
