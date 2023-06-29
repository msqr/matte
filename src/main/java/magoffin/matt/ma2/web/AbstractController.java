/* ===================================================================
 * AbstractEatCommandController.java
 * 
 * Created Sep 19, 2004 4:52:17 PM
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
 */

package magoffin.matt.ma2.web;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.web.util.WebHelper;
import magoffin.matt.xweb.util.MessagesSource;

/**
 * Abstract base class for command controllers.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>cancelView</dt>
 *   <dd>The name of the view to go to if the form is canceled.</dd>
 *   
 *   <dt>domainObjectFactory</dt>
 *   <dd>The {@link magoffin.matt.ma2.biz.DomainObjectFactory} implementation
 *   to use for creating instances of our domain objects.</dd>
 *   
 *   <dt>messagesSource</dt>
 *   <dd>A {@link magoffin.matt.xweb.util.MessagesSource} instance.</dd>
 *   
 *   <dt>successView</dt>
 *   <dd>The name of the view to go to if the form is completed successfully.</dd>
 *   
 *   <dt>systemBiz</dt>
 *   <dd>An implementation of {@link magoffin.matt.ma2.biz.SystemBiz} to use.</dd>
 *   
 *   <dt>webHelper</dt>
 *   <dd>A {@link magoffin.matt.ma2.web.util.WebHelper} instance.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public abstract class AbstractController 
extends org.springframework.web.servlet.mvc.AbstractController {

	private String successView = null;
	private String errorView = null;
	private MessagesSource messagesSource = null;
	private DomainObjectFactory domainObjectFactory = null;
	private SystemBiz systemBiz = null;
	private WebHelper webHelper;
	
	/**
	 * @return Returns the domainObjectFactory.
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}
	
	/**
	 * @param domainObjectFactory The domainObjectFactory to set.
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}
	
	/**
	 * @return Returns the errorView.
	 */
	public String getErrorView() {
		return errorView;
	}
	
	/**
	 * @param errorView The errorView to set.
	 */
	public void setErrorView(String errorView) {
		this.errorView = errorView;
	}
	
	/**
	 * @return Returns the messagesSource.
	 */
	public MessagesSource getMessagesSource() {
		return messagesSource;
	}
	
	/**
	 * @param messagesSource The messagesSource to set.
	 */
	public void setMessagesSource(MessagesSource messagesSource) {
		this.messagesSource = messagesSource;
	}
	
	/**
	 * @return Returns the successView.
	 */
	public String getSuccessView() {
		return successView;
	}
	
	/**
	 * @param successView The successView to set.
	 */
	public void setSuccessView(String successView) {
		this.successView = successView;
	}
	
	/**
	 * @return Returns the webHelper.
	 */
	public WebHelper getWebHelper() {
		return webHelper;
	}
	
	/**
	 * @param webHelper The webHelper to set.
	 */
	public void setWebHelper(WebHelper webHelper) {
		this.webHelper = webHelper;
	}
	
	/**
	 * @return Returns the systemBiz.
	 */
	public SystemBiz getSystemBiz() {
		return systemBiz;
	}
	
	/**
	 * @param systemBiz The systemBiz to set.
	 */
	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}

}
