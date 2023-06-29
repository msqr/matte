/* ===================================================================
 * AbstractMailTemplateInterceptor.java
 * 
 * Created Oct 1, 2004 8:34:17 AM
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

package magoffin.matt.ma2.aop;

import java.util.Locale;
import java.util.Map;

import magoffin.matt.ma2.ProcessingException;
import magoffin.matt.ma2.util.MailMergeHelper;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

/**
 * Base class for mail merge interceptors.
 * 
 * <p>This interceptor serves as a base for email interceptors using 
 * templated email content (mail merge). The {@link #invoke(MethodInvocation)}
 * method performs the following steps:</p>
 * 
 * <ol>
 *   <li>Calls {@link #getInitialModel(MethodInvocation)}. This method must 
 *   return a non-null Map to use for the model data for the mail merge.</li>
 * 
 *   <li>Calls {@link MethodInvocation#proceed()}.</li>
 * 
 *   <li>Calls {@link #postProcessModel(MethodInvocation, Map, Object)}, passing 
 *   the model Map previously returned and the result of the method invocation.</li>
 * 
 *   <li>Calls {@link #getLocale(MethodInvocation, Map, Object)}.</li>
 * 
 *   <li>Calls {@link magoffin.matt.ma2.util.MailMergeHelper#sendMerge(Locale, ClassLoader, Map, SimpleMailMessage)}
 *   to perform the mail merge and send the email.</li>
 * </ol>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>mailMergeSupport</dt>
 *   <dd>The {@link magoffin.matt.ma2.util.MailMergeHelper} instance to use for 
 *   sending the mail merge.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */

public abstract class AbstractMailTemplateInterceptor implements MethodInterceptor {
	
	/** Class level log. */
	protected final Log log = LogFactory.getLog(getClass());
	
	private MailMergeHelper mailMergeSupport = null;
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public final Object invoke(MethodInvocation invocation) throws Throwable {
		
		if ( mailMergeSupport == null ) {
			throw new RuntimeException("Mail templateName not configured in " +getClass());
		}
		
		// get model
		Map<String,Object> model = getInitialModel(invocation);
		
		// run method
		Object result = invocation.proceed();
		
		// get mail message
		SimpleMailMessage msg = postProcessModel(invocation,model,result);
		
		// get the Locale for the mail merge
		Locale locale = getLocale(invocation,model,result);
		
		// send the mail via a mail merge
		try {
			mailMergeSupport.sendMerge(locale,getClass().getClassLoader(),model,msg);
		} catch( MailException ex ) {
			log.warn("Unable to send email: " +ex.toString());
			throw new ProcessingException(result,"Unable to send email",ex);
		}
        
		return result;
	}
	
	/**
	 * Get the locale for the email message.
	 * 
	 * <p>This implementation simply returns {@link Locale#getDefault()}. Extending 
	 * implementations can use the invocation and invocation result to provide 
	 * a custom Locale as deisred.</p>
	 * 
	 * @param invocation the current method invocation
	 * @param model the model
	 * @param result the resut of the current modthod invocation
	 * @return a Locale to use for the mail merge
	 */
	protected Locale getLocale(
			MethodInvocation invocation, 
			Map<String,Object> model, 
			Object result) {
		return Locale.getDefault();
	}
	
	/**
	 * Get an initial Map object to use for the model data for the mail merge.
	 * @param invocation the current method invocation
	 * @return Map
	 */
	protected abstract Map<String,Object> getInitialModel(MethodInvocation invocation);
	
	/**
	 * Get a SimpleMailMessage object based on the result of the method invocation.
	 * 
	 * <p>You can use the MailMergeHelper instance's 
	 * {@link MailMergeHelper#getMessageTemplate()} method to obtain a mail message
	 * template, and use the 
	 * {@link SimpleMailMessage#SimpleMailMessage(org.springframework.mail.SimpleMailMessage)}
	 * copy constructor to create the SimpleMailMessage to return here. Most likely the 
	 * only property you'll need to set on the result if the <code>to</code> property.</p>
	 * 
	 * @param invocation the current method invocation
	 * @param model the model Map
	 * @param result the method invocation result object
	 * @return a SimpleMailMessage object with appropriate information set, 
	 * i.e. the <code>to</code> property
	 */
	protected abstract SimpleMailMessage postProcessModel(MethodInvocation invocation, 
			Map<String,Object> model, Object result);

	/* Injector methods. */
	
	/**
	 * @return Returns the mailMergeSupport.
	 */
	public MailMergeHelper getMailMergeSupport() {
		return mailMergeSupport;
	}

	/**
	 * @param mailMergeSupport The mailMergeSupport to set.
	 */
	public void setMailMergeSupport(MailMergeHelper mailMergeSupport) {
		this.mailMergeSupport = mailMergeSupport;
	}

}
