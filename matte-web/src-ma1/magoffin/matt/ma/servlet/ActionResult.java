/* ===================================================================
 * ActionResult.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 18, 2004 5:13:31 PM.
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
 * $Id: ActionResult.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import java.util.Map;

import org.apache.struts.action.ActionForward;

import magoffin.matt.ma.xsd.AbstractData;
import magoffin.matt.util.ResetableObject;

/**
 * Encapsulate Media Album action response data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public final class ActionResult implements ResetableObject {
	
	private ActionForward forward;
	private AbstractData data;
	private String xslTemplate;
	private Map xslParams;
	private boolean changedUserSettings;
	
/**
 * @return Returns the data.
 */
public AbstractData getData() {
	return data;
}

/**
 * @param data The data to set.
 */
public void setData(AbstractData data) {
	this.data = data;
}

/**
 * @return Returns the forward.
 */
public ActionForward getForward() {
	return forward;
}

/**
 * @param forward The forward to set.
 */
public void setForward(ActionForward forward) {
	this.forward = forward;
}

/* (non-Javadoc)
 * @see magoffin.matt.util.ResetableObject#reset()
 */
public void reset() {
	data = null;
	forward = null;
	xslParams = null;
	xslTemplate = null;
	changedUserSettings = false;
}

/**
 * @return Returns the xslTemplate.
 */
public String getXslTemplate() {
	return xslTemplate;
}

/**
 * @param xslTemplate The xslTemplate to set.
 */
public void setXslTemplate(String xslTemplate) {
	this.xslTemplate = xslTemplate;
}

/**
 * @return Returns the xslParams.
 */
public Map getXslParams() {
	return xslParams;
}
/**
 * @param xslParams The xslParams to set.
 */
public void setXslParams(Map xslParams) {
	this.xslParams = xslParams;
}

/**
 * @return Returns the changedUserSettings.
 */
public boolean isChangedUserSettings()
{
	return changedUserSettings;
}
/**
 * @param changedUserSettings The changedUserSettings to set.
 */
public void setChangedUserSettings(boolean changedUserSettings)
{
	this.changedUserSettings = changedUserSettings;
}
}
