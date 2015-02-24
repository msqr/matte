/* ===================================================================
 * SimpleWsSecurityInterceptor.java
 * 
 * Created Dec 4, 2007 8:07:35 AM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

package magoffin.matt.ma2.web.service;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPathConstants;

import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BasicBizContext;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.ma2.util.XmlHelper;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.AbstractWsSecurityInterceptor;
import org.springframework.ws.soap.security.WsSecurityFaultException;
import org.springframework.ws.soap.security.WsSecuritySecurementException;
import org.springframework.ws.soap.security.WsSecurityValidationException;

/**
 * Bare-bones WS-Security interceptor implementation for authenticating basic
 * usernames with the {@link UserBiz}.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class SimpleWsSecurityInterceptor extends AbstractWsSecurityInterceptor {
	
	/** The WSSE namespace URI. */
	public static final String WSSE_NAMESPACE_URI 
		= "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	
	/** The SOAP namespace URI. */
	public static final String SOAP_NAMESPACE_URI
		= "http://schemas.xmlsoap.org/soap/envelope/";
	
	/** The wsse:UsernameToken element name. */
	public static final String WSSE_SECURITY_ELEMENT_NAME = "Security";
	
	private UserBiz userBiz;
	private XmlHelper xmlHelper;

	@Override
	protected void secureMessage(SoapMessage soapMessage,
			MessageContext messageContext)
			throws WsSecuritySecurementException {
		// nothing to do here
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
			throws WsSecurityValidationException {
		// look for wsse:UsernameToken
		SoapHeader header = soapMessage.getSoapHeader();
		Iterator<SoapHeaderElement> itr = header.examineAllHeaderElements();
		while ( itr.hasNext() ) {
			SoapHeaderElement el = itr.next();
			QName name = el.getName();
			if ( WSSE_NAMESPACE_URI.equals(name.getNamespaceURI())
					&& WSSE_SECURITY_ELEMENT_NAME.equals(name.getLocalPart())) {
				Source usernameTokenSource = el.getSource();
				DOMResult domResult = new DOMResult();
				xmlHelper.transformXml(usernameTokenSource, domResult);
				String username = (String)xmlHelper.evaluateXPath(domResult.getNode(), 
						"/wsse:Security/wsse:UsernameToken/wsse:Username", XPathConstants.STRING);
				String password = (String)xmlHelper.evaluateXPath(domResult.getNode(), 
						"/wsse:Security/wsse:UsernameToken/wsse:Password", XPathConstants.STRING);
				authenticate(username, password);
				break;
			}
		}
	}
	
	@Override
	protected void cleanUp() {
		// anything needed here?
	}

	private static final class SecurityFaultException extends WsSecurityFaultException {

		private static final long serialVersionUID = -4430317919760404562L;

		private SecurityFaultException(QName faultCode, String faultString,
				String faultActor) {
			super(faultCode, faultString, faultActor);
		}
		
	}
	
	private void authenticate(String username, String password) {
		try {
			User user = userBiz.logonUser(username, password);
			BasicBizContext bizContext = new BasicBizContext();
			bizContext.setActingUser(user);
			BizContextUtil.attachBizContext(bizContext);
		} catch ( AuthorizationException e ) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("WS authentication failure for user [" +username +"]: " +e);
			}
			QName faultCode = new QName(SOAP_NAMESPACE_URI, "Server");
			throw new SecurityFaultException(faultCode, "Authorization error.", "");
		}
	}

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	/**
	 * @return the xmlHelper
	 */
	public XmlHelper getXmlHelper() {
		return xmlHelper;
	}

	/**
	 * @param xmlHelper the xmlHelper to set
	 */
	public void setXmlHelper(XmlHelper xmlHelper) {
		this.xmlHelper = xmlHelper;
	}

}
