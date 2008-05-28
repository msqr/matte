/* ===================================================================
 * AbstractAction.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: AbstractAction.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import magoffin.matt.biz.Biz;
import magoffin.matt.biz.BizFactory;
import magoffin.matt.exception.ValidationExceptionIntf;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.biz.XMLBiz;
import magoffin.matt.ma.search.MediaItemQuery;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.ServletUtil;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.BaseBounceBackForm;
import magoffin.matt.ma.servlet.formbean.BaseBounceBackHomeForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.xsd.AbstractData;
import magoffin.matt.ma.xsd.ActionMsgs;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaAlbumSettings;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.ConfigObjectPoolFactory;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.w3c.dom.Document;



/**
 * Base Action for Media Album Action implementations.
 * 
 * <p>Created Oct 8, 2002 6:29:03 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractAction extends Action 
{
	/** The Xform XSL template key: <code>home</code>. */
	public static final String HOME_TEMPLATES_KEY = "home";

	/** Flag to pass to {@link #getUserSessionData(HttpServletRequest,boolean)}. */
	protected static final boolean ANONYMOUS_USER_OK = true;
	
	/** Flag to pass to {@link #getUserSessionData(HttpServletRequest,boolean)}. */
	protected static final boolean ANONYMOUS_USER_NOT_OK = false;
		
	/** The request-scoped attribute key for error messages: <code>_e</code>. */
	private static final String REQ_ATTR_ERRORS = "_e";
	
	/** The request-scoped attribute key for message: <code>_m</code>. */
	private static final String REQ_ATTR_MESSAGES = "_m";
	
	/** 
	 * The request-scoped attribute key for signaling not to marshall result DOM.
	 */
	protected static final String REQ_ATTR_NO_MARSHALL = "_nom";
	
	private static final Logger LOG = Logger.getLogger(AbstractAction.class);
	
/**
 * Calls {@link #go(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse, ActionResult)}.
 * 
 * <p>This method will also remove the saved URL from session if the action did not 
 * call the {@link ServletUtil#saveRequestURL(HttpServletRequest)} during this invocation.</p>
 * 
 * @param mapping current action mapping
 * @param form current form bean
 * @param request current request
 * @param response current response
 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 * @return action forward
 * @throws Exception if an error occurs
 */
public final ActionForward execute(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response)
throws Exception 
{
	ActionResult result = null;
	
	try {
		// borrow result object
		result = (ActionResult)borrowPooledObject(ActionResult.class);
			
		// remove any session errors, place on request
		if ( request.getSession().getAttribute(REQ_ATTR_ERRORS) != null ) {
			request.setAttribute(REQ_ATTR_ERRORS,request.getSession().getAttribute(REQ_ATTR_ERRORS));
			request.getSession().removeAttribute(REQ_ATTR_ERRORS);
		}
		
		// remove any session messages, place on request
		if ( request.getSession().getAttribute(REQ_ATTR_MESSAGES) != null ) {
			request.setAttribute(REQ_ATTR_MESSAGES,request.getSession().getAttribute(REQ_ATTR_MESSAGES));
			request.getSession().removeAttribute(REQ_ATTR_MESSAGES);
		}
		
		// bouncing is a little trick to work with setBounceBackActionForward()
		// methods, we only allow for bouncing flag to exist for one forward
		boolean bouncing = request.getSession().getAttribute(
				ServletConstants.SES_KEY_BOUNCING_BACK) != null;

		go(mapping,form,request,response,result);

		if ( request.getAttribute(ServletConstants.REQ_ATTR_SAVED_URL) == null ) {
			request.getSession().removeAttribute(ServletConstants.SES_KEY_SAVED_URL);
		}
		
		if ( result.isChangedUserSettings() ) {
			UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_OK);
			if ( usd.getUser() != null ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Refreshing user session data: " 
							+usd.getUser().getUsername());
				}
				try {
					UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
					User newUser = userBiz.getFullUser(usd.getUser().getUserId());
					usd.setUser(newUser);
					MediaSpec spec = MediaSpecUtil.getThumbImageSpec(
							newUser.getThumbSize(),newUser.getThumbCompress());
					usd.setThumbSpec(spec);
					spec = MediaSpecUtil.getImageSpec(
							newUser.getSingleSize(),newUser.getSingleCompress());
					usd.setSingleSpec(spec);
				} catch ( MediaAlbumException e ) {
					LOG.error("Unable to refresh user data",e);
				}
			}
		}

		saveActionMessages(request,result.getData(),result.getForward());
		
		if ( result.getData() != null && 
				request.getAttribute(REQ_ATTR_NO_MARSHALL) == null) {
			marshallResultDOM(request,result.getData(),result.getXslTemplate(),
					result.getXslParams());
		}
		
		if ( bouncing ) {
			request.getSession().removeAttribute(ServletConstants.SES_KEY_BOUNCING_BACK);
		}
		
		return result.getForward();
	} catch ( UserAccessException e ) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("UserAccessException: " +e.getMessage());
		}
		ServletUtil.saveRequestURL(request);
		throw e;
	} catch ( MediaAlbumException e ) {
		if ( !(e instanceof ValidationExceptionIntf) ) {
			LOG.error("Exception caught in AbstractAction: " +e.toString(),e);
		}
		ActionError err = null;
		if ( e.getErrorCode() != null ) {
			err = new ActionError(e.getErrorCode(),e.getErrorParams());
		} else {
			err = new ActionError(MessageConstants.ERR_UNKNOWN,e.toString());
		}
		this.addActionMessage(request,ActionErrors.GLOBAL_ERROR,err);
		ActionForward fwd = result == null || result.getForward() == null
			? mapping.findForward(StrutsConstants.DEFAULT_ERROR_FORWARD)
			: result.getForward();
		this.saveActionMessages(request,null,fwd);
		return fwd;
	} catch ( Exception e ) {
		LOG.error("Exception caught in AbstractAction: " +e.getMessage(),e);
		this.addActionMessage(request,ActionErrors.GLOBAL_ERROR,
			new ActionError(MessageConstants.ERR_UNKNOWN,e.getMessage()));
		this.saveActionMessages(request,null,
				(result == null ? null : result.getForward()));
		throw e;
	} finally {
		if ( result != null ) {
			AbstractData data = result.getData();
			if ( data != null ) {
				MediaAlbumSettings settings = data.getSettings();
				if ( settings != null ) {
					returnPooledObject(settings);
				}
				returnPooledObject(data);
			}
			returnPooledObject(result);
		}
	}
}


/**
 * Extending Action implementations must override this method.
 * 
 * <p>If the ActionResult AbstractData object is non-null, it will be assumed
 * it came from an ObjectPool and will be passed to {@link #returnPooledObject(Object)}.
 * In addition, if the AbstractData's MediaAlbumSettings object is non-null, 
 * that will likewise be returned to an ObjectPool via 
 * {@link #returnPooledObject(Object)}.</p>
 *  * @param mapping the action mapping
 * @param form the form bean
 * @param request the servlet request
 * @param response the servlet response
 * @param result the action result
 * @throws Exception if an error occurs
 */
protected abstract void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, 
	ActionResult result) throws Exception;
	
/**
 * Marshall an object as the result DOM for Xform.
 * 
 * <p>The object <var>o</var> will be marshalled into a DOM object, and placed
 * onto the request at {@link ServletConstants#REQ_ATTR_XFORM_DOM}. If
 * <var>templatesKey</var> is non-null, it will be placed on the request at
 * {@link ServletConstants#REQ_ATTR_XFORM_XSL}. If <var>params</var>
 * is non-null, it will be placed on the request at 
 * {@link ServletConstants#REQ_ATTR_XFORM_PARAM}. Thus the action 
 * can forward to Xform without needing to specify any parameters.</p>
 * 
 * @param req the current request
 * @param o the object to marshall
 * @param templatesKey the Xform XSL templates key to use (may be <em>null</em>)
 * @param params XSL parameters to set on the templates object (may be <em>null</em>) * @throws MediaAlbumException */
private final void marshallResultDOM(HttpServletRequest req, Object o, String templatesKey, Map params)
throws MediaAlbumException
{
	XMLBiz xmlBiz = (XMLBiz)getBiz(BizConstants.XML_BIZ);
	Document doc = xmlBiz.marshallToDocument(o);
	
	req.setAttribute(ServletConstants.REQ_ATTR_XFORM_DOM,doc);
	if ( templatesKey != null ) {
		req.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL,templatesKey);
	}
	if ( params != null ) {
		req.setAttribute(ServletConstants.REQ_ATTR_XFORM_PARAM,params);
	}
}

/**
 * Get the currently logged in user session data.
 * 
 * <p>Throws a <code>UserSessionDataException</code> if the user session
 * data can't be found.</p>
 *  * @param request the current request * @param allowAnonymous <em>true</em> if allow anonymous user
 * @return UserSessionData * @throws UserAccessException if the session data is not available */
protected UserSessionData getUserSessionData(HttpServletRequest request, boolean allowAnonymous)
throws UserAccessException
{
	UserSessionData usd = (UserSessionData) request.getSession().getAttribute(ServletConstants.SES_KEY_USER);
	if ( usd == null ) {
		throw new UserAccessException("Null UserSessionData.");
	}
	if ( usd.getUser() == null && !allowAnonymous ) {
		throw new NotAuthorizedException();
	}
	return usd;
}

/**
 * Test if a user has permission to view a specified collection.
 * 
 * @param user the acting user * @param collectionId the collection ID * @throws MediaAlbumException if an error occurs * @throws NotAuthorizedException if the user is not authorized to view the collection
 */
protected final void verifyUserCanViewCollection(User user, Integer collectionId) 
throws MediaAlbumException, NotAuthorizedException
{
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	
	if ( collectionBiz.canUserViewCollection(user,collectionId) ) {
		return;
	}

	throw new NotAuthorizedException(user.getUsername(),
				MessageConstants.ERR_AUTH_VIEW_COLLECTION);
}


protected final void populateCollectionsForUser(
		MediaAlbumData data, 
		Integer dirId,
		User actingUser) 
throws MediaAlbumException
{
	// get all dirs for user, optionally populate all items for dir if collectionId provided
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);

	Collection[] dirs = userBiz.getCollectionsForUser(actingUser.getUserId());

	if ( dirs != null && dirs.length > 0 ) {
		for ( int i = 0; i < dirs.length; i++ ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Adding Collection to MediaAlbumData: " +dirs[i].getCollectionId());
			}
			if ( dirId != null && dirId.intValue() == dirs[i].getCollectionId().intValue() ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Adding media items to Collection " +dirs[i].getCollectionId());
				}
				MediaItem[] items = collectionBiz.getMediaItemsForCollection(dirs[i].getCollectionId(),
						ApplicationConstants.CACHED_OBJECT_ALLOWED, actingUser);
				MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
				itemBiz.populateItems(items,ApplicationConstants.POPULATE_MODE_ALL,
						ApplicationConstants.CACHED_OBJECT_ALLOWED);
				dirs[i].setItem(items);
				data.setDisplaySource(dirId.intValue());
			}	
		}
	}
	data.setCollection(dirs);
}


/**
 * Saves an ActionMessage onto the request.
 * 
 * <p>Use this method along with the 
 * {@link #saveActionMessages(HttpServletRequest, MediaAlbumData, ActionForward)} method to get
 * action messages into the MediaAlbumData object.</p>
 *  * @param request the current request * @param msgKey the message key * @param msg the action message to add
 * @see #saveActionMessages(HttpServletRequest, AbstractData, ActionForward) */
protected final void addActionMessage(HttpServletRequest request, String msgKey, ActionMessage msg) 
{
	if ( msg == null || msgKey == null || request == null ) {
		return;
	}
	
	boolean isError = msg instanceof ActionError;
	
	Object o = null;
	if ( isError ) {
		o = request.getAttribute(REQ_ATTR_ERRORS);
	} else {
		o = request.getAttribute(REQ_ATTR_MESSAGES);
	}
	ActionMessages msgs = null;
	if ( o != null ) {
		msgs = (ActionMessages)o;
	} else {
		if ( isError ) {
			msgs = new ActionErrors();
			request.setAttribute(REQ_ATTR_ERRORS,msgs);
		} else {
			msgs = new ActionMessages();
			request.setAttribute(REQ_ATTR_MESSAGES,msgs);
		}
	}
	
	msgs.add(msgKey,msg);
}


/**
 * Tell if any ActionMessage objects have been saved via 
 * {@link #saveActionMessages(HttpServletRequest, AbstractData, ActionForward)}.
 * 
 * @param request the current request
 * @return boolean <em>true</em> if any messages have been saved
 */
protected final boolean hasActionErrorsOrMessages(HttpServletRequest request)
{
	Object o = request.getAttribute(REQ_ATTR_ERRORS);
	if ( o != null ) return true;
	o = request.getAttribute(REQ_ATTR_MESSAGES);
	return ( o != null );
}

/**
 * Save any ActionMessage objects from the current request.
 * 
 * <p>If <var>data</var> is non-null, then the messages will be added to that
 * object instead of using the Struts 
 * {@link Action#saveMessages(javax.servlet.http.HttpServletRequest, org.apache.struts.action.ActionMessages)} 
 * method.</p>
 *  * @param request current request
 * @param data (<em>null</em> allowed)
 * @param forward TODO */
private final void saveActionMessages(HttpServletRequest request, AbstractData data, 
		ActionForward forward)
{
	Object o = request.getAttribute(REQ_ATTR_ERRORS);
	if ( o != null ) {
		saveMessages(request,data,(ActionErrors)o,forward);
	}
	o = request.getAttribute(REQ_ATTR_MESSAGES);
	if ( o != null ) {
		saveMessages(request,data,(ActionMessages)o,forward);
	}
}

/**
 * Return <em>true</em> if should save any request messages to session.
 * @param forward the current action forward
 * @return boolean
 */
private boolean shouldSaveMessagesToSession(HttpServletRequest request, ActionForward forward)
{
	if ( (forward == null && request.getAttribute(REQ_ATTR_NO_MARSHALL) == null) 
			|| (forward != null && !forward.getPath().endsWith(".do") ) ) {
		return false;
	}
	return true;
}

private void saveMessages(HttpServletRequest request, AbstractData data, 
		ActionMessages msgs, ActionForward forward)
{
	boolean isErrors = msgs instanceof ActionErrors;
	boolean saveToSession = shouldSaveMessagesToSession(request,forward);
	if ( data == null ) {
		if ( isErrors ) {
			saveErrors(request,(ActionErrors)msgs);
			if ( saveToSession ) {
				request.getSession().setAttribute(REQ_ATTR_ERRORS,msgs);
			}
		} else {
			saveMessages(request,msgs);
			if ( saveToSession ) {
				request.getSession().setAttribute(REQ_ATTR_MESSAGES,msgs);
			}
		}
	} else {
		if ( msgs.isEmpty() ) {
			return;
		}
		MessageResources resources = this.getResources(request);
			
		for ( Iterator propItr = msgs.properties(); propItr.hasNext(); ) {
			String prop = (String)propItr.next();
			ActionMsgs m = new ActionMsgs();
			m.setKey(prop);
			for ( Iterator itr = msgs.get(prop); itr.hasNext(); ) {
				ActionMessage msg = (ActionMessage)itr.next();
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Added ActionMessage to data: " +msg);
				}
				m.addMsg(resources.getMessage(msg.getKey(),msg.getValues()));
			}
			data.addActionMessages(m);
		}
	}
	if ( isErrors ) {
		request.removeAttribute(REQ_ATTR_ERRORS);
	} else {
		request.removeAttribute(REQ_ATTR_MESSAGES);
	}
}


/**
 * Send an email message.
 * 
 * <p>The email's from address will be taken from calling the {@link
 * #getFromEmailAddress()} method.</p>
 *  * @param to the 'to' address * @param cc the 'cc' addresses (pass <em>null</em> to ignore) * @param bcc the 'bcc' addresses (pass <em>null</em> to ignore) * @param subject the subject * @param content the body of the message * @throws MediaAlbumException *
protected final void sendEmail(String to, String[] cc, String[] bcc, String subject, String content)
throws MediaAlbumException
{
	try {
		Session session = this.getMailSession();
		String from = this.getFromEmailAddress();
		MailUtil.sendTextEmail(session,from,
			new String[] {to},cc,bcc,subject,content);
	} catch ( NotInitializedException e ) {
		throw e;
	} catch ( Exception e ) {
		log.error("Unable to send email: " +e.getMessage());
		throw new MediaAlbumException("Unable to send email",e);
	}
}


/**
 * Get the configured <code>Session</code> object.
 * @return Session
 * @throws NotInitializedException
 *
protected final Session getMailSession() throws NotInitializedException
{
	ServletContext ctx = servlet.getServletContext();
	Object session = ctx.getAttribute(ServletConstants.APP_KEY_MAIL_SESSION);
	if (!(session instanceof Session) ) {
		throw new NotInitializedException("JavaMail Session not configured properly: "
			+session);
	}
	return (Session)session;
}


/**
 * Get the default email address to use for the <em>from</em> address.
 * @return from email address
 * @throws NotInitializedException
 *
protected final String getDefaultFromEmailAddress() throws NotInitializedException
{
	ServletContext ctx = servlet.getServletContext();
	Object from = ctx.getAttribute(ServletConstants.APP_KEY_MAIL_FROM);
	if ( !(from instanceof String) ) {
		throw new NotInitializedException("JavaMail from address not configured properly: "
			+from);
	}
	return (String)from;
}


/**
 * Get the email address to use to send email messages from 
 * {@link #sendEmail()}.
 * 
 * <p>This method simply calls the {@link #getDefaultFromEmailAddress()} method.
 * Extending classes can override this method to provide a different email
 * address as needed.</p>
 * 
 * @return String the email address
 * @throws NotInitializedException
 *
protected String getFromEmailAddress() throws NotInitializedException
{
	return this.getDefaultFromEmailAddress();
}
*/

/**
 * Return a HTTP NOT_FOUND response.
 * 
 * <p>Return the result of this message from the {@link #go(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse, ActionResult)}
 * method. If <var>mapping</var> is <em>null</em> then an HTTP NOT FOUND response will
 * be set and <em>null</em> will be returned. Otherwise the ActionForward
 * for <em>notfound</em> will be returned.</p>
 *  * @param mapping the current mapping
 * @param request the current request
 * @param response the current response
 * @return ActionForward (always <em>null</em>) */
protected final ActionForward notFound(ActionMapping mapping, 
		HttpServletRequest request, HttpServletResponse response)
{
	if ( mapping == null || mapping.findForward("notfound") == null ) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		request.setAttribute(REQ_ATTR_NO_MARSHALL,Boolean.TRUE);
		return null;
	} else { 
		return mapping.findForward("notfound");
	}
}


/**
 * Return a HTTP FORBIDDEN response.
 * 
 * <p>Return the result of this method from the {@link #go(ActionMapping,
 * ActionForm, HttpServletRequest, HttpServletResponse, ActionResult)} method. If
 * <var>mapping</var> is <em>null</em> then an HTTP FORBIDDEN response will
 * be set and <em>null</em> will be returned. Otherwise the ActionForward
 * for <em>forbidden</em> will be returned.</p>
 * 
 * @param mapping the current mapping
 * @param response the current response
 * @return ActionForward
 */
protected final ActionForward forbidden(ActionMapping mapping, HttpServletResponse response)
{
		if ( mapping == null || mapping.findForward("forbidden") == null ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		} else { 
			return mapping.findForward("forbidden");
		}
}

/**
 * Send an HTTP redirect to the saved URL.
 * 
 * <p>If no saved URL exists, the method will return the ActionForward passed
 * in the <var>forward</var> parameter.</p>
 * 
 * @param request the current request
 * @param response the current response
 * @param forward the ActionForward to fall back to if no saved URl exists
 * @return ActionForward the forward to forward to
 * @throws MediaAlbumException if sending the redirect causes an java.io.IOException 
 * or java.lang.IllegalStateException
 */
protected final ActionForward redirectToSavedURL(
	HttpServletRequest request, 
	HttpServletResponse response,
	ActionForward forward)
throws MediaAlbumException
{
	try {
		String savedUrl = ServletUtil.getSavedRequestURL(request);
		if ( savedUrl != null ) {
			request.getSession().removeAttribute(
					ServletConstants.SES_KEY_SAVED_URL);
			response.sendRedirect(savedUrl);
			request.setAttribute(REQ_ATTR_NO_MARSHALL,Boolean.TRUE);
			return null;
		}
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to redirect",e);
	}
	// no saved URL, so use forward instead
	return forward;
}

/**
 * Issue a redirect response header to a URL represented by an ActionForward along
 * with a URL parameter for an album ID.
 * 
 * @param request the request
 * @param response the response
 * @param forward the ActionForward to generate the redirect URL from
 * @return <em>null</em>, so can be returned by calling action class
 * @param paramName the name of the URL parameter
 * @param paramValue the value of the <var>paramName</var> URL parameter
 * @throws IOException if an IO error occurs
 */
protected final ActionForward redirectWithParam(
		HttpServletRequest request,
		HttpServletResponse response,
		ActionForward forward,
		String paramName,
		Object paramValue)
throws IOException
{
	StringBuffer buf = new StringBuffer(request.getScheme());
	buf.append("://").append(request.getServerName());
	if ( (request.getScheme().equals("http") && request.getServerPort() != 80) 
			|| (request.getScheme().equals("https") && request.getServerPort()  != 443 ) ) {
		buf.append(':').append(request.getServerPort());
	}
	buf.append(request.getContextPath());
	buf.append(forward.getPath());
	buf.append("?");
	buf.append(paramName);
	buf.append("=").append(paramValue);
	response.sendRedirect(buf.toString());
	request.setAttribute(REQ_ATTR_NO_MARSHALL,Boolean.TRUE);
	return null;
}



/**
 * Issue a redirect response header to a URL represented by an ActionForward along
 * with a URL parameter for an album ID.
 * 
 * @param albumId the album ID to add as a URL parameter
 * @param request the request
 * @param response the response
 * @param forward the ActionForward to generate the redirect URL from
 * @return <em>null</em>, so can be returned by calling action class
 * @throws IOException if an IO error occurs
 */
protected final ActionForward redirectToAlbum(
	Object albumId, 
	HttpServletRequest request,
	HttpServletResponse response,
	ActionForward forward)
	throws IOException
{
	return redirectWithParam(request,response,forward,
			ServletConstants.REQ_KEY_ALBUM_ID,albumId);
}

/**
 * Issue a redirect response header to a URL represented by an ActionForward along
 * with a URL parameter for a collection ID.
 * 
 * @param collectionId the collection ID to add as a URL parameter
 * @param request the request
 * @param response the response
 * @param forward the ActionForward to generate the redirect URL from
 * @return <em>null</em>, so can be returned by calling action class
 * @throws IOException if an IO error occurs
 */
protected final ActionForward redirectToCollection(
	Object collectionId, 
	HttpServletRequest request,
	HttpServletResponse response,
	ActionForward forward)
	throws IOException
{
	return redirectWithParam(request,response,forward,
			ServletConstants.REQ_KEY_COLLECTION_ID,collectionId);
}


protected final void reSaveRequestURL(HttpServletRequest request)
{
	if ( request.getSession().getAttribute(ServletConstants.SES_KEY_SAVED_URL) != null ) {
		request.setAttribute(ServletConstants.REQ_ATTR_SAVED_URL,"t");
	}
}


/**
 * Populate items into the Album with an ID equal to <var>id</var>.
 * 
 * @param id the ID to populate
 * @param albums a non-null array of albums
 * @param actingUser acting user
 * @param populateMode the item populate data mode
 * @return the Album that was populated, or <em>null</em> if none
 * @throws MediaAlbumException if an error occurs
 */
protected Album populateItems(int id, Album[] albums, User actingUser, int populateMode) 
throws MediaAlbumException
{
	for ( int i = 0; i < albums.length; i++ ) {
		if ( id == albums[i].getAlbumId().intValue() ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Adding media items to Album " + albums[i].getAlbumId());
			}
			AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
			MediaItem[] items = albumBiz.getMediaItemsForAlbum(albums[i].getAlbumId(),
					populateMode, ApplicationConstants.CACHED_OBJECT_ALLOWED, actingUser);
			albumBiz.sortAlbumItems(albums[i],items);
			albums[i].setItem(items);
			return albums[i];
		}
		// check for nested albums
		if ( albums[i].getAlbumCount() > 0 ) {
			Album a = populateItems(id,albums[i].getAlbum(),actingUser,populateMode);
			if ( a != null ) {
				return a;
			}
		}
	}
	return null;
}

protected Biz getBiz(String bizName) throws MediaAlbumException {
	BizFactory bizFactory = ServletUtil.getBizIntfFactory(
			servlet.getServletContext());
	return bizFactory.getBizInstance(bizName);
}

/**
 * Borrow a pooled object using the {@link ConfigObjectPoolFactory}.
 * 
 * @param objectClass the class of the object to get a pooled instance for
 * @return the pooled instance
 * @throws MediaAlbumException if an error occurs
 */
protected Object borrowPooledObject(Class objectClass) throws MediaAlbumException 
{
	ConfigObjectPoolFactory poolFactory = ConfigObjectPoolFactory.getInstance(
			ApplicationConstants.CONFIG_ENV);
	ObjectPool pool = poolFactory.getObjectPool(objectClass);
	try {
		return pool.borrowObject();
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to borrow pooled object: "
				+objectClass.getName(),e);
	}
}

/**
 * Return an object obtained via {@link #borrowPooledObject(Class)}.
 * 
 * @param object the object to return
 */
protected void returnPooledObject(Object object) {
	if ( object == null ) return;
	ConfigObjectPoolFactory poolFactory = ConfigObjectPoolFactory.getInstance(
			ApplicationConstants.CONFIG_ENV);
	ObjectPool pool = poolFactory.getObjectPool(object.getClass());
	try {
		pool.returnObject(object);
	} catch ( Exception e ) {
		LOG.warn("Unable to return pooled object: "
				+object.getClass().getName() +": " 
				+e.getMessage());
	}
}

/**
 * Set the proper ActionForward onto the ActionResult for a "bounce back".
 * 
 * <p>If the form's <var>key</var> field is not set, then the 
 * {@link StrutsConstants#DEFAULT_OK_FORWARD} forward will be used.</p>
 * @param form the bounce back form data
 * @param mapping action mapping
 * @param result the action result
 * @param request the current request
 */
protected void setBounceBackActionForward(
			BaseBounceBackForm form,
			ActionMapping mapping,
			ActionResult result, 
			HttpServletRequest request) 
{
	if ( form.getKey() != null ) {
		// ok, in KEY mode, so in album slideshow OR browse
		if ( form.getBrowsePage() != null ) {
			String forward = StrutsConstants.BROWSE_ALBUMS_FORWARD;
			if ( form.getBrowseMode() != null && form.getBrowseMode().intValue() > 0 ) {
				forward += "-virtual-"+form.getBrowseMode();
			}
			result.setForward(mapping.findForward(forward));
		} else {
			// slideshow
			result.setForward(mapping.findForward(
					StrutsConstants.ALBUM_SLIDESHOW_FORWARD));
		}
	} else {
		MediaItemQuery query = form.getQuery();
		if ( query != null && (query.getSimple() != null || query.getName() != null 
				|| query.getKeyword() != null || query.getText() != null) ) {
			// ok, in search mode
			result.setForward(mapping.findForward(StrutsConstants.SEARCH_FORWARD));
		} else {
			result.setForward(mapping.findForward(
					StrutsConstants.DEFAULT_OK_FORWARD));
		}
	}
	request.getSession().setAttribute(ServletConstants.SES_KEY_BOUNCING_BACK,
			Boolean.TRUE);
}

/**
 * Set the proper ActionForward onto the ActionResult for a "bounce back".
 * @param form the bounce back form data
 * @param mapping action mapping
 * @param result the action result
 * @param request the current request
 */
protected void setBounceBackActionForward(
			BaseBounceBackHomeForm form,
			ActionMapping mapping,
			ActionResult result, 
			HttpServletRequest request) 
{
	MediaItemQuery query = form.getQuery();
	if ( query != null && (query.getSimple() != null || query.getName() != null 
			|| query.getKeyword() != null || query.getText() != null) ) {
		// ok, in search mode
		result.setForward(mapping.findForward(StrutsConstants.SEARCH_FORWARD));
	} else {
		result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
	}
	request.getSession().setAttribute(ServletConstants.SES_KEY_BOUNCING_BACK,
			Boolean.TRUE);
}

/* (non-Javadoc)
 * @see org.apache.struts.action.Action#isCancelled(javax.servlet.http.HttpServletRequest)
 */
protected boolean isCancelled(HttpServletRequest request) {
	// overridden to provide for "bounce back" request
	HttpSession session = request.getSession();
	if ( session.getAttribute(ServletConstants.SES_KEY_BOUNCING_BACK) != null ) {
		session.removeAttribute(ServletConstants.SES_KEY_BOUNCING_BACK);
		return false;
	}
	return super.isCancelled(request);
}
} 
