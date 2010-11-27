/* ===================================================================
 * MyDataFormAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 24, 2004 9:40:50 AM.
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
 * $Id: MyDataFormAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.FreeDataForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to view edit form for user free data maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class MyDataFormAction extends AbstractAction {
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response, ActionResult result)
		throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);	
	FreeDataForm dForm = (FreeDataForm)form;
	User user = usd.getUser();
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
		
	dForm.setUser(user);
	dForm.setUserMode(true);
	
	// get user free data
	FreeData[] data = userBiz.getFreeData(user, 
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( data.length > 0 ) {
		// sort data into copyright, keywords
		for ( int i = 0; i < data.length; i++ ) {
			Integer type = data[i].getDataTypeId();
			if ( ApplicationConstants.FREE_DATA_TYPE_COPYRIGHT.equals(type) ) {
				dForm.setCopyright(data[i]);
			} else if (ApplicationConstants.FREE_DATA_TYPE_KEYWORD.equals(type) ) {
				dForm.setKeywords(data[i]);
			}
		}
	}
	
	if ( dForm.getCopyright() == null ) {
		dForm.setCopyright(new FreeData());
	}
	
	if ( dForm.getKeywords() == null ) {
		dForm.setKeywords(new FreeData());
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
