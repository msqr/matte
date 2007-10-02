/* ===================================================================
 * MailMergeHelper.java
 * 
 * Created Mar 6, 2005 2:46:49 PM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: MailMergeHelper.java,v 1.4 2007/07/28 10:25:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import magoffin.matt.util.StringMerger;
import magoffin.matt.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * A bean to support MailMerge email sending.
 * 
 * <p>The email templates are handled also in a locale-specific way. 
 * The <code>templatePath</code> property should be set to the directory
 * that contains the message templates, and the <code>templateName</code>
 * should be set to the desired template to use, without any locale in
 * the name. The {@link #performMerge(String, ClassLoader, Map)} will 
 * construct a path to the template resource by inserting the language
 * before the template extension. For example, if <code>templatePath</code>
 * is set to <code>mailtemplates</code> and <code>templateName</code>
 * set to <code>confirmation.txt</code>, then for the English language
 * it will look for a resource in the classpath at 
 * <code>mailtemplates/confirmation_en.txt</code>.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>templatePath</dt>
 *   <dd>The classpath-relative path to the directory containing the locale-specific 
 *   mail merge template files.</dd>
 * 
 *   <dt>templateName</dt>
 *   <dd>The file name of the mail merge template, without any locale suffix.</dd>
 * 
 *   <dt>messageTemplate</dt>
 *   <dd>A mail message to use as a template for the outgoing mail message. This 
 *   can be used to configure the from address, for example.</dd>
 *   
 *   <dt>mailsender</dt>
 *   <dd>The MailSender to use to send the email.</dd>
 * 
 *   <dt>messageSource</dt>
 *   <dd>The MessageSource to use for resource messages.</dd>
 * 
 *   <dt>subjectMessageKey</dt>
 *   <dd>The resource message key to use for the subject of the email message.</dd>
 *   
 *   <dt>ignoreMailExceptions</dt>
 *   <dd>If <em>true</em> then ignore all mail exceptions. This can be useful
 *   for development, when you may not have access to a real mail server. 
 *   Defaults to <em>false</em>.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/28 10:25:54 $
 */

public class MailMergeHelper {
	
	/** The key for the message resource for a "no subject" subject. */
	public static final String MSG_NO_SUBJECT_KEY = "email.no.subject";
	
	private final Log log = LogFactory.getLog(MailMergeHelper.class);

	/* Injected fields. */
	
	private String templatePath = null;
	private String templateName = null;
	private SimpleMailMessage messageTemplate = null;
	private MailSender mailSender = null;
	private MessageSource messageSource = null;
	private String subjectMessageKey = null;
	private boolean ignoreMailExceptions = false;
	
	/**
	 * Perform a mail merge and send an email with the result of the merge as the 
	 * email message text.
	 * 
	 * <p>The <code>msg</code> should have all required fields already set prior to 
	 * calling this method. The <code>text</code> property of the message will be 
	 * set to the result of the mail merge. If the <code>msg</code> has a <em>null</em>
	 * subject, then the subject will be set to the resource message specified by 
	 * the <code>subjectMessageKey</code> of this bean. If <code>subjectMessageKey</code>
	 * is <em>null</em> then the {@link #MSG_NO_SUBJECT_KEY} resource message will 
	 * be used.</p>
	 * 
	 * @param locale the locale for the mail merge template. If <em>null</em> the 
	 * default Locale will be used.
	 * @param loader the class loader to use
	 * @param model the merge model data
	 * @param msg the mail message
	 */
	public void sendMerge(Locale locale, ClassLoader loader, Map<String, ?> model, SimpleMailMessage msg) {
		if ( locale == null ) {
			locale = Locale.getDefault();
		}
		String merged = performMerge(locale.getLanguage(),loader,model);
		msg.setText(merged);
		if ( msg.getSubject() == null ) {
	        if ( subjectMessageKey != null ) {
		    		String subject = messageSource.getMessage(subjectMessageKey,null,locale);
		    		msg.setSubject(subject);
	        } else {
	        		msg.setSubject(messageSource.getMessage(MSG_NO_SUBJECT_KEY,null,locale));
	        }
		}
		send(msg);
	}
	
	/**
	 * Send a mail message.
	 * 
	 * @param msg the message to send
	 * @throws MailException if an error occurs
	 */
	public void send(SimpleMailMessage msg) {
		if ( log.isDebugEnabled() ) {
			log.debug("Sending mail '" +msg.getSubject() +"' to " 
					+StringUtil.valueOf(msg.getTo()) 
					+" with msg:\n" +msg.getText());
		}
		try {
			mailSender.send(msg);
		} catch ( RuntimeException e ) {
			if ( ignoreMailExceptions ) {
				log.warn("Unable to send mail [" +msg.getSubject() 
						+"] to [" +msg.getTo() 
						+"], ignoring exception [" +e.toString() 
						+"]. Message body: \n" +msg.getText());
			} else {
				throw e;
			}
		}
	}
	
	/**
	 * Perform  a mail merge.
	 * 
	 * @param lang the language for the mail merge template
	 * @param loader the class loader to use
	 * @param model the merge model data
	 * @return the merged message body
	 */
	public String performMerge(String lang, ClassLoader loader, Map<String, ?> model) {
		try {
			String templateResourcePath = (templatePath == null ? "" : templatePath +'/')
				+ (lang == null ? templateName 
					: (StringUtil.substringBeforeLast(templateName,'.') + '_' + lang + '.' +
							StringUtil.substringAfter(templateName,'.')));
			String mergeResult = StringMerger.mergeResource(
					loader,
					templateResourcePath, model);
			return mergeResult;
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to process mail template",e);
		}
	}

	/**
	 * @return Returns the mailSender.
	 */
	public MailSender getMailSender() {
		return mailSender;
	}

	/**
	 * @param mailSender The mailSender to set.
	 */
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * @return Returns the messageSource.
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * @param messageSource The messageSource to set.
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @return Returns the messageTemplate.
	 */
	public SimpleMailMessage getMessageTemplate() {
		return messageTemplate;
	}

	/**
	 * @param messageTemplate The messageTemplate to set.
	 */
	public void setMessageTemplate(SimpleMailMessage messageTemplate) {
		this.messageTemplate = messageTemplate;
	}

	/**
	 * @return Returns the subjectMessageKey.
	 */
	public String getSubjectMessageKey() {
		return subjectMessageKey;
	}

	/**
	 * @param subjectMessageKey The subjectMessageKey to set.
	 */
	public void setSubjectMessageKey(String subjectMessageKey) {
		this.subjectMessageKey = subjectMessageKey;
	}

	/**
	 * @return Returns the templateName.
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * @param templateName The templateName to set.
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	/**
	 * @return Returns the templatePath.
	 */
	public String getTemplatePath() {
		return templatePath;
	}

	/**
	 * @param templatePath The templatePath to set.
	 */
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	/**
	 * @return the ignoreMailExceptions
	 */
	public boolean isIgnoreMailExceptions() {
		return ignoreMailExceptions;
	}

	/**
	 * @param ignoreMailExceptions the ignoreMailExceptions to set
	 */
	public void setIgnoreMailExceptions(boolean ignoreMailExceptions) {
		this.ignoreMailExceptions = ignoreMailExceptions;
	}
	
}
