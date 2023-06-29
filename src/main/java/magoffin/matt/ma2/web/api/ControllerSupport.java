/* ===================================================================
 * ControllerSupport.java
 * 
 * Created Feb 4, 2015 7:58:45 PM
 * 
 * Copyright (c) 2015 Matt Magoffin.
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
 */

package magoffin.matt.ma2.web.api;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.web.util.WebHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base support class for web API controllers.
 *
 * @author matt
 * @version 1.0
 */
public class ControllerSupport {

	@Autowired
	private WebHelper webHelper;

	@Autowired
	private DomainObjectFactory domainObjectFactory;

	public WebHelper getWebHelper() {
		return webHelper;
	}

	public void setWebHelper(WebHelper webHelper) {
		this.webHelper = webHelper;
	}

	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

}
