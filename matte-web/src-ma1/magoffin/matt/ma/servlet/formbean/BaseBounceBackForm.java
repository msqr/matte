/* ===================================================================
 * BaseBounceBackForm.java
 * 
 * Created Apr 28, 2004 8:04:00 AM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: BaseBounceBackForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

/**
 * Abstract form to help support actions that need to return
 * to browse / slideshow locations.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public abstract class BaseBounceBackForm extends BaseBounceBackHomeForm
{
	protected Integer browsePage;
	protected Integer browseMode; // virtual mode!
	protected String key;
	protected Integer mitem;
	
	
	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);
		album = collection = browsePage = browseMode = mitem = null;
		key = null;
	}
	
	/**
	 * @return Returns the browseMode.
	 */
	public Integer getBrowseMode() {
		return browseMode;
	}
	/**
	 * @param browseMode The browseMode to set.
	 */
	public void setBrowseMode(Integer browseMode) {
		this.browseMode = browseMode;
	}
	/**
	 * @return Returns the browsePage.
	 */
	public Integer getBrowsePage()
	{
		return browsePage;
	}
	/**
	 * @param browsePage The browsePage to set.
	 */
	public void setBrowsePage(Integer browsePage)
	{
		this.browsePage = browsePage;
	}
	/**
	 * @return Returns the item.
	 */
	public Integer getMitem()
	{
		return mitem;
	}
	/**
	 * @param item The item to set.
	 */
	public void setMitem(Integer item)
	{
		this.mitem = item;
	}
	/**
	 * @return Returns the key.
	 */
	public String getKey()
	{
		return key;
	}
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key)
	{
		this.key = key;
	}
}
