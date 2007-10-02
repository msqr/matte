/* ===================================================================
 * UserEmailInterceptor.java
 * 
 * Created Oct 1, 2004 7:37:38 AM
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
 * $Id: UserEmailInterceptor.java,v 1.4 2007/07/28 10:25:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.aop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.web.util.WebBizContext;
import magoffin.matt.util.StringMerger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.mail.SimpleMailMessage;

/**
 * Handle emailing user emails.
 * 
 * <p>This interceptor requires the intercepted method to have a 
 * {@link magoffin.matt.ma2.domain.User} object as a parameter, from 
 * which the email will be sent to. If the method also has a 
 * {@link magoffin.matt.ma2.biz.BizContext} parameter, the interceptor
 * will look for a {@link magoffin.matt.ma2.web.util.WebBizContext#URL_BASE}
 * attribute to prepend to the <code>confirmUrl</code>.
 * </p>
 * 
 * <p>The <code>confirmUrl</code> field is used to construct a URL to 
 * include in the merged email body. The value of this field can contain
 * any variables available in the email body merge model object (except
 * itself, of course). Note that due to possible unattended variable 
 * processing of variables derived from property files, the variables
 * in this field may be specified with <code>$[<i>var</i>]</code> instead
 * of the normal <code>${<i>var</i>}</code> syntax.</p>
 * 
 * <p>The mail merge model will be set up with the following attributes:</p>
 * 
 * <dl class="class-properties">
 *   <dt>user</dt>
 *   <dd>The {@link magoffin.matt.ma2.domain.User} object intercepted from 
 *   the method parameter.</dd>
 * 
 *   <dt>result</dt>
 *   <dd>The result of the intercepted method.</dd>
 * 
 *   <dt>confirmUrl</dt>
 *   <dd>A URL itself merged from the <code>user</code> and <code>result</code>
 *   attributes with the <code>confirmUrl</code> field.</dd>
 * </dl>
 * 
 * <p>See the {@link magoffin.matt.ma2.aop.AbstractMailTemplateInterceptor}
 * class for more information about the email fields (like subject).</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>confirmUrl</dt>
 *   <dd>A URL to add to the mail merge model map. This URL can be used 
 *   to supply a link for the user to perform some action. For example 
 *   during registration the registration confirmation email must contain 
 *   a URL for the user to go to to confirm their registration.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/28 10:25:54 $
 */

public class UserEmailInterceptor extends AbstractMailTemplateInterceptor {
    
    /** The model key for the User object. */
    public static final String USER_KEY = "user";
    
    /** The model key for the result object. */
    public static final String RESULT_KEY = "result";
    
    /** The model key for the confirmation URL. */
    public static final String CONFIRM_URL_KEY = "confirmUrl";
	
	private String confirmUrl = null;
	
	@Override
	protected Map<String,Object> getInitialModel(MethodInvocation invocation) {
		Object[] args = invocation.getArguments();

		// look for User and return object for merge model
		User user = null;
		BizContext context = null;
		for ( int i = 0; i < args.length && (user == null || context == null); i++ ) {
			if ( args[i] instanceof User ) {
				user = (User)args[i];
			} else if ( args[i] instanceof BizContext ) {
				context = (BizContext)args[i];
			}
		}
		
		Map<String,Object> model = new LinkedHashMap<String,Object>();
		
		// look for baseUrl attribute
		if ( context != null && context.getAttribute(WebBizContext.URL_BASE) != null ) {
			model.put("baseUrl",context.getAttribute(WebBizContext.URL_BASE));
		}
		
		model.put(USER_KEY,user);
		return model;
	}

	@Override
	protected SimpleMailMessage postProcessModel(MethodInvocation invocation, 
			Map<String,Object> model, Object result) {
		if ( model.get(USER_KEY) == null ) {
			// check if result object is a User
			if ( result instanceof User ) {
				model.put(USER_KEY,result);
			} else {
				throw new RuntimeException("No User found in method invocation arguments or result");
			}
		}
		
		if ( result != null ) {
			if ( result instanceof String ) {
				try {
					result = URLEncoder.encode((String)result,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					log.warn("Unable to encode URL: " +e.toString());
				}
			}
			model.put(RESULT_KEY,result);
		}
		
		// merge confirm url
		String url = StringMerger.mergeString(confirmUrl,"",model);
		if ( model.get("baseUrl") != null ) {
			url = model.get("baseUrl").toString() +url;
			model.remove("baseUrl");
		}
		model.put(CONFIRM_URL_KEY,url);
		
		User user = (User)model.get(USER_KEY);
				
		SimpleMailMessage msg = new SimpleMailMessage(getMailMergeSupport().getMessageTemplate());
		msg.setTo(user.getEmail());
		
		return msg;
	}

	@Override
	protected Locale getLocale(MethodInvocation invocation, Map<String,Object> model,
			Object result) {
		User user = (User)model.get(USER_KEY);
		if ( user.getCountry() != null && user.getLanguage() != null ) {
			return new Locale(user.getLanguage(),user.getCountry());
		}
		return super.getLocale(invocation, model, result);
	}

	/* Method injectors. */
	
	/**
	 * @return Returns the confirmUrl.
	 */
	public String getConfirmUrl() {
		return confirmUrl;
	}

	/**
	 * @param confirmUrl The confirmUrl to set.
	 */
	public void setConfirmUrl(String confirmUrl) {
		this.confirmUrl = confirmUrl.replace('[','{').replace(']','}');
	}
	
}
