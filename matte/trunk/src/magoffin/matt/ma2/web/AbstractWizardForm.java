/* ===================================================================
 * AbstractWizardForm.java
 * 
 * Created Feb 2, 2006 8:04:20 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.ma2.web.util.WebHelper;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

/**
 * Base class for Media Album wizard form controllers.
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
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public abstract class AbstractWizardForm extends AbstractWizardFormController {

	private String successView = null;
	private String cancelView = null;
	private DomainObjectFactory domainObjectFactory;
	private SystemBiz systemBiz = null;
	private WebHelper webHelper = null;
    
	@Override
	protected void initApplicationContext() {
		super.initApplicationContext();
		String cmdName = getCommandName();
		if ( !StringUtils.hasText(cmdName) || cmdName.equals("command") ) {
			// default to own command name
			setCommandName(WebConstants.DEFALUT_MODEL_OBJECT);
		}
	}

	/**
	 * @return Returns the cancelView.
	 */
	public String getCancelView() {
		return cancelView;
	}
	/**
	 * @param cancelView The cancelView to set.
	 */
	public void setCancelView(String cancelView) {
		this.cancelView = cancelView;
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

}
