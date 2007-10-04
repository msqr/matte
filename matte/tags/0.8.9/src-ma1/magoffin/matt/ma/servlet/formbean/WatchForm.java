/* ===================================================================
 * WatchForm.java
 * 
 * Created Apr 28, 2004 8:27:51 AM
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
 * $Id: WatchForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

/**
 * Form bean for watch album / user actions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class WatchForm extends BaseBounceBackForm
{
	private Integer user = null;
	private boolean watch = false;
	
	/**
	 * @return Returns the user.
	 */
	public Integer getUser()
	{
		return user;
	}
	/**
	 * @param user The user to set.
	 */
	public void setUser(Integer user)
	{
		this.user = user;
	}
	/**
	 * @return Returns the watch.
	 */
	public boolean isWatch()
	{
		return watch;
	}
	/**
	 * @param watch The watch to set.
	 */
	public void setWatch(boolean watch)
	{
		this.watch = watch;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request)
	{
		super.reset(mapping, request);
		watch = false;
	}
}
