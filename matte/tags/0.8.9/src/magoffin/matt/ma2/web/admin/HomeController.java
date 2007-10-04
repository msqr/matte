/* ===================================================================
 * HomeController.java
 * 
 * Created May 23, 2006 9:22:02 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: HomeController.java,v 1.4 2007/09/07 08:34:09 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.web.AbstractController;

import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the admin home view.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.4 $ $Date: 2007/09/07 08:34:09 $
 */
public class HomeController extends AbstractController {
	
	private UserBiz userBiz = null;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		BizContext context = getWebHelper().getAdminBizContext(request);
		if ( logger.isDebugEnabled() ) {
			logger.debug("Admin home for user [" +context.getActingUser().getLogin() +"]");
		}
		return new ModelAndView(getSuccessView());
	}

	/**
	 * @return Returns the userBiz.
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz The userBiz to set.
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

}
