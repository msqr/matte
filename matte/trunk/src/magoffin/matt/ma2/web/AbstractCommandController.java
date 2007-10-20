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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import java.util.Calendar;
import java.util.TimeZone;

import org.springframework.web.bind.ServletRequestDataBinder;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.ma2.web.util.WebHelper;
import magoffin.matt.util.CalendarEditor;
import magoffin.matt.util.StringUtil;
import magoffin.matt.util.ThreadSafeDateFormat;
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
 * @version $Revision$ $Date$
 */
public abstract class AbstractCommandController 
extends org.springframework.web.servlet.mvc.AbstractCommandController {

	/** The HTTP User-Agent header. */
	public static final String HTTP_USER_AGENT_HEADER = "User-Agent";
	
	// private ServletRequestDataBinderTemplate binderTemplate = null;
	private String successView = null;
	private String errorView = null;
	private MessagesSource messagesSource = null;
	private DomainObjectFactory domainObjectFactory = null;
	private SystemBiz systemBiz = null;
	private WebHelper webHelper;
	
	/**
	 * Default constructor.
	 */
	public AbstractCommandController() {
		String myClass = getClass().getName()+"$Command";
		try {
			setCommandClass(Class.forName(myClass));
		} catch ( Exception e ) {
			// ignore
		}
	}
	
	@Override
	protected void initApplicationContext() {
		super.initApplicationContext();
		String cmdName = StringUtil.trimToNull(getCommandName());
		if ( cmdName == null || cmdName.equals("command") ) {
			// default
			setCommandName(WebConstants.DEFALUT_MODEL_OBJECT);
		}
	}

	/**
	 * Create a DataBinder object based on the <code>dataBinderClass</code> property.
	 * 
	 * <p>If the <code>dataBinderClass</code> property is set, this method will
	 * attempt to instantiate that class by calling a constructor with a method 
	 * signature of <code>ServletRequestDataBinder(Object,String,Map)</code>. 
	 * The Object and String passed into the constructor are the standard 
	 * command and command name objects normally passed to ServetRequestDataBinder
	 * implementations. The Map argument will be the <code>dataBinderInitializerMap</code>
	 * object configured in this controller instance.</p>
	 */
	/*protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object command)
    throws Exception {
		if ( binderTemplate == null ) {
			return super.createBinder(request,command);
		}
		Constructor c = binderTemplate.getClass().getConstructor(
				new Class[] {Object.class,String.class,DataBinder.class});
		ServletRequestDataBinder binder = (ServletRequestDataBinder)c.newInstance(
				new Object[] {command,getCommandName(),binderTemplate});
		if (getMessageCodesResolver() != null) {
			binder.setMessageCodesResolver(getMessageCodesResolver());
		}
		initBinder(request, binder);
		return binder;
	}*/
	
	/**
	 * Register a {@link CalendarEditor} property editor for Calendar bean properties.
	 * 
	 * @param binder the binder
	 * @param context the current context, or <em>null</em> to not use User's time zone
	 * @param format the date format to use
	 * @param zone the time zone to use, or <em>null</em> for default
	 */
	protected void registerCalendarEditor(ServletRequestDataBinder binder, 
			BizContext context, ThreadSafeDateFormat format, TimeZone zone) {
		if ( zone == null && context != null && context.getActingUser().getTz() != null ) {
			zone = TimeZone.getTimeZone(context.getActingUser().getTz().getCode());
		}
		
		// register our Calendar binder...
		binder.registerCustomEditor(Calendar.class, new CalendarEditor(format, zone, true));
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
