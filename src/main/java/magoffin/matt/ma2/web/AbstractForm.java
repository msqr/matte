/* ===================================================================
 * AbstractEatForm.java
 * 
 * Created Aug 9, 2004 1:16:48 PM
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

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.ma2.web.util.WebHelper;
import magoffin.matt.util.CalendarEditor;
import magoffin.matt.util.ThreadSafeDateFormat;
import magoffin.matt.xweb.util.ServletRequestDataBinderTemplate;

/**
 * Abstract base class for form controllers.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
@SuppressWarnings("deprecation")
public abstract class AbstractForm extends org.springframework.web.servlet.mvc.SimpleFormController {

	/**
	 * Parameter triggering the cancel action. Can be called from any wizard
	 * page!
	 */
	public static final String PARAM_CANCEL = "_cancel";

	/** The HTTP User-Agent header. */
	public static final String HTTP_USER_AGENT_HEADER = "User-Agent";

	private String cancelView = null;
	private DomainObjectFactory domainObjectFactory;
	private SystemBiz systemBiz = null;
	private WebHelper webHelper = null;
	private ServletRequestDataBinderTemplate binderTemplate = null;

	@Override
	protected void initApplicationContext() {
		super.initApplicationContext();
		String cmdName = getCommandName();
		if ( !StringUtils.hasText(cmdName) || cmdName.equals("command") ) {
			// default to own command name
			setCommandName(WebConstants.DEFALUT_MODEL_OBJECT);
		}
	}

	@Override
	protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object command)
			throws Exception {
		if ( binderTemplate == null ) {
			return super.createBinder(request, command);
		}
		Constructor<? extends ServletRequestDataBinderTemplate> c = binderTemplate.getClass()
				.getConstructor(new Class<?>[] { Object.class, String.class, DataBinder.class });
		ServletRequestDataBinder binder = (ServletRequestDataBinder) c
				.newInstance(new Object[] { command, getCommandName(), binderTemplate });
		if ( getMessageCodesResolver() != null ) {
			binder.setMessageCodesResolver(getMessageCodesResolver());
		}
		if ( getBindingErrorProcessor() != null ) {
			binder.setBindingErrorProcessor(getBindingErrorProcessor());
		}
		if ( getPropertyEditorRegistrars() != null ) {
			for ( int i = 0; i < getPropertyEditorRegistrars().length; i++ ) {
				getPropertyEditorRegistrars()[i].registerCustomEditors(binder);
			}
		}
		initBinder(request, binder);
		return binder;
	}

	/**
	 * Register a {@link CalendarEditor} property editor for Calendar bean
	 * properties.
	 * 
	 * @param binder
	 *        the binder
	 * @param context
	 *        the current context, or <em>null</em> to not use User's time zone
	 * @param format
	 *        the date format to use
	 * @param zone
	 *        the time zone to use, or <em>null</em> for default
	 */
	protected void registerCalendarEditor(ServletRequestDataBinder binder, BizContext context,
			ThreadSafeDateFormat format, TimeZone zone) {
		if ( zone == null && context != null && context.getActingUser().getTz() != null ) {
			zone = TimeZone.getTimeZone(context.getActingUser().getTz().getCode());
		}

		// register our Calendar binder...
		binder.registerCustomEditor(Calendar.class, new CalendarEditor(format, zone, true));
	}

	/**
	 * Return if cancel action is specified in the request.
	 * 
	 * <p>
	 * Default implementation looks for "_cancel" parameter in the request.
	 * </p>
	 * 
	 * @param request
	 *        current HTTP request
	 * @return <em>true</em> if user canceled action
	 * @see #PARAM_CANCEL
	 */
	protected boolean isCancel(HttpServletRequest request) {
		return WebUtils.hasSubmitParameter(request, PARAM_CANCEL);
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors) throws Exception {
		// check for 'cancel' request, otherwise defer to super implementation
		if ( isCancel(request) ) {
			return processCancel(request, response, command, errors);
		}
		return super.processFormSubmission(request, response, command, errors);
	}

	/**
	 * Perform a cancel form submit request.
	 * 
	 * <p>
	 * This method is called by
	 * {@link #processFormSubmission(HttpServletRequest, HttpServletResponse, Object, BindException)}
	 * if the {@link #isCancel(HttpServletRequest)} method returns
	 * <em>true</em>.
	 * </p>
	 * 
	 * @param request
	 *        the current request
	 * @param response
	 *        the response
	 * @param command
	 *        the command
	 * @param errors
	 *        the errors
	 * @return a ModelAndView for handling the cancel request
	 */
	protected ModelAndView processCancel(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) {
		// default implementation is to simply return cancel view
		return new ModelAndView(getCancelView());
	}

	/**
	 * @return Returns the cancelView.
	 */
	public String getCancelView() {
		return cancelView;
	}

	/**
	 * @param cancelView
	 *        The cancelView to set.
	 */
	public void setCancelView(String cancelView) {
		this.cancelView = cancelView;
	}

	/**
	 * @return Returns the domainObjectFactory.
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory
	 *        The domainObjectFactory to set.
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
	 * @param systemBiz
	 *        The systemBiz to set.
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
	 * @param webHelper
	 *        The webHelper to set.
	 */
	public void setWebHelper(WebHelper webHelper) {
		this.webHelper = webHelper;
	}

	/**
	 * @return the binderTemplate
	 */
	public ServletRequestDataBinderTemplate getBinderTemplate() {
		return binderTemplate;
	}

	/**
	 * @param binderTemplate
	 *        the binderTemplate to set
	 */
	public void setBinderTemplate(ServletRequestDataBinderTemplate binderTemplate) {
		this.binderTemplate = binderTemplate;
	}

}
