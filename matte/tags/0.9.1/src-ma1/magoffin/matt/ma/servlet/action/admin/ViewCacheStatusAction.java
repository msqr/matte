/* ===================================================================
 * ViewCacheStatusAction.java
 * 
 * Created Jan 8, 2004 2:29:06 PM
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
 * $Id: ViewCacheStatusAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.AdminBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.AdminData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to view a single cache status.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ViewCacheStatusAction extends AbstractAdminAction {

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.admin.AbstractAdminAction#goAdmin(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.AdminData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goAdmin(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response,
		ActionResult result,
		AdminData data, UserSessionData usd)
	throws Exception 
{
	
	DynaActionForm dForm = (DynaActionForm)form;
	
	String cacheKey = (String)dForm.get("name");
	
	AdminBiz adminBiz = (AdminBiz)getBiz(BizConstants.ADMIN_BIZ);
	
	data.setCacheStatus(adminBiz.getCacheStatuses(cacheKey));
	data.setDisplayCacheStatus(cacheKey);
	
	result.setData(data);
	result.setXslTemplate(ADMIN_HOME_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
