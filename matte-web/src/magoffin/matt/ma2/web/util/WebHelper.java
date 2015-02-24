/* ===================================================================
 * WebHelper.java
 * 
 * Created Oct 4, 2004 12:22:10 PM
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

package magoffin.matt.ma2.web.util;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import magoffin.matt.ma2.ApplicationNotConfiguredException;
import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.AuthorizationException.Reason;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.domain.JobInfo;
import magoffin.matt.ma2.domain.MediaSizeDefinition;
import magoffin.matt.ma2.domain.MediaSpec;
import magoffin.matt.ma2.domain.Session;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.Geometry;
import magoffin.matt.ma2.web.NoUserSessionException;
import magoffin.matt.xweb.util.AppContextSupport;
import magoffin.matt.xweb.util.BasicXwebHelper;
import org.springframework.context.MessageSource;

/**
 * Utility methods for web layer.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>userBiz</dt>
 * <dd>The {@link magoffin.matt.ma2.biz.UserBiz} implementation to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public final class WebHelper extends BasicXwebHelper {

	private static ThreadLocal<Theme> themeHolder = new ThreadLocal<Theme>();

	private UserBiz userBiz;
	private MediaBiz mediaBiz;
	private WorkBiz workBiz;
	private SystemBiz systemBiz;
	private DomainObjectFactory domainObjectFactory;
	private String datePattern = "dd MMM yyyy HH:mm:ss";
	private TimeZone dateTimeZone = TimeZone.getTimeZone("UTC");
	private MessageSource messageSource;

	/**
	 * Create a JobInfo instance from a WorkInfo instance.
	 * 
	 * @param context
	 *        the context
	 * @param ticket
	 *        the requested ticket number
	 * @return JobInfo
	 */
	public JobInfo createJobInfo(BizContext context, Long ticket) {
		WorkInfo info = workBiz.getInfo(ticket);
		JobInfo job = getDomainObjectFactory().newJobInfoInstance();

		if ( info != null ) {
			job.setDisplayName(info.getDisplayName());
			job.setMessage(info.getMessage());

			SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
			sdf.setTimeZone(dateTimeZone);

			job.setAmountCompleted(info.getAmountCompleted());
			if ( info.getException() != null ) {
				job.setError(info.getException().getMessage());
			}
			job.setPriority(info.getPriority());
			job.setTicket(info.getTicket());
			if ( info.getCompleteTime() > 0 ) {
				job.setTimeCompleted(info.getCompleteTime() + " "
						+ sdf.format(new Date(info.getCompleteTime())));
			}
			if ( info.getStartTime() > 0 ) {
				job.setTimeStarted(info.getStartTime() + " " + sdf.format(new Date(info.getStartTime())));
			}
			job.setTimeSubmitted(info.getSubmitTime() + " " + sdf.format(new Date(info.getSubmitTime())));
		} else {
			// job not found
			job.setAmountCompleted(0f);
			job.setError(getMessageSource().getMessage("jobinfo.not.available", new Object[] { ticket },
					"Job not available.", context.getLocale()));
			job.setTicket(ticket);
			job.setTimeSubmitted(getMessageSource().getMessage("not.available", null, "N/A",
					context.getLocale()));
		}
		return job;
	}

	/**
	 * Save a theme for the current request. The
	 * {@link #clearSavedRequestTheme()} method must be called when the current
	 * request has finished processing, on the same thread that called this
	 * method.
	 * 
	 * @param theme
	 *        the theme to save for
	 */
	public void saveRequestTheme(Theme theme) {
		themeHolder.set(theme);
	}

	/**
	 * Get the saved theme from the current request.
	 * 
	 * @return the Theme, or <em>null</em> if none saved
	 */
	public Theme getRequestTheme() {
		return themeHolder.get();
	}

	/**
	 * Get a BizContext for the current request, checking that a {@link Session}
	 * object exists on the HttpSession.
	 * 
	 * @param request
	 *        the request
	 * @return BizContext
	 * @throws NoUserSessionException
	 *         if no {@link Session} is available in the HttpSession
	 */
	public BizContext getBizContextWithViewSettings(HttpServletRequest request) {
		BizContext context = getAnonymousBizContext(request);
		Session session = getUserSession(request);
		if ( session == null ) {
			throw new NoUserSessionException();
		}
		return context;
	}

	/**
	 * Get a BizContext for the current request.
	 * 
	 * <p>
	 * If the <code>userRequired</code> parameter is <em>true</em> then an
	 * <code>AuthorizationException</code> will be thrown with a
	 * {@link Reason#ANONYMOUS_ACCESS_DENIED} if the current request is for an
	 * anonymous user.
	 * </p>
	 * 
	 * @param request
	 *        the current request
	 * @param userRequired
	 *        if <em>true</em> then a logged-in user must be on the current
	 *        request, otherwise an {@link AuthorizationException} will be
	 *        thrown
	 * @return the BizContext
	 * @throws AuthorizationException
	 *         if <var>userRequired</var> is <em>true</em> and no user exists on
	 *         the current request
	 */
	public BizContext getBizContext(HttpServletRequest request, boolean userRequired) {
		BizContext context = getAnonymousBizContext(request);
		if ( userBiz.isAnonymousUser(context.getActingUser()) && userRequired ) {
			throw new AuthorizationException(context.getActingUser().getLogin(),
					Reason.ANONYMOUS_ACCESS_DENIED);
		}
		return context;
	}

	/**
	 * Get an admin-level BizContext for the current request.
	 * 
	 * <p>
	 * If the current request is for an anonymous user, then an
	 * <code>AuthorizationException</code> will be thrown with a
	 * {@link Reason#ANONYMOUS_ACCESS_DENIED} reason. If the current request is
	 * for a logged in user that does not have admin priveleges, an
	 * <code>AuthorizationException</code> will be thrown with a
	 * {@link Reason#ACCESS_DENIED} reason.
	 * </p>
	 * 
	 * @param request
	 *        the current request
	 * @return the BizContext
	 */
	public BizContext getAdminBizContext(HttpServletRequest request) {
		BizContext context = getBizContext(request, true);
		if ( !userBiz.hasAccessLevel(context.getActingUser(), UserBiz.ACCESS_ADMIN) ) {
			throw new AuthorizationException(context.getActingUser().getLogin(), Reason.ACCESS_DENIED);
		}
		return context;
	}

	/**
	 * Get a BizContext for the current request that can be an anonymous user
	 * even if anonymous features are disabled.
	 * 
	 * <p>
	 * This can be used by actions that can be performed even if anonymous
	 * features are disabled, such as self registration.
	 * </p>
	 * 
	 * @param request
	 *        the current request
	 * @return a BizContext
	 */
	public BizContext getAnonymousBizContext(HttpServletRequest request) {
		// verify app is set up
		if ( !systemBiz.isApplicationConfigured() ) {
			throw new ApplicationNotConfiguredException();
		}

		AppContextSupport appContextSupport = getAppContextSupport(request);
		WebBizContext context = new WebBizContext(request, appContextSupport);
		context.setActingUser(userBiz.getAnonymousUser());
		Session userSession = getUserSession(request);
		if ( userSession != null && userSession.getActingUser() != null ) {
			context.setActingUser(userSession.getActingUser());
		}
		String baseUrl = request.getScheme()
				+ "://"
				+ request.getServerName()
				+ (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":"
						+ String.valueOf(request.getServerPort())) + request.getContextPath();
		context.setAttribute(WebBizContext.URL_BASE, baseUrl);
		return context;
	}

	/**
	 * Save the action user to session.
	 * 
	 * <p>
	 * This method will save the <code>user</code> instance to session, allowing
	 * the application to maintain a logged-in state for the user. The user is
	 * added to a {@link Session} instance as the <code>actingUser</code>
	 * property. If the user has the {@link UserBiz#ACCESS_ADMIN} permission bit
	 * set the <code>admin</code> property of the <code>Session</code> will be
	 * set to <em>true</em>.
	 * </p>
	 * 
	 * @param request
	 *        the current request
	 * @param user
	 *        the User to save in session
	 */
	public void saveUserSession(HttpServletRequest request, User user) {
		Session userSession = getDomainObjectFactory().newSessionInstance();
		userSession.setActingUser(user);
		if ( userBiz.hasAccessLevel(user, UserBiz.ACCESS_ADMIN) ) {
			userSession.setAdmin(true);
		}
		if ( user != null && user.getThumbnailSetting() != null ) {
			userSession.setThumbnailSetting(user.getThumbnailSetting());
		} else {
			MediaSpec spec = getDomainObjectFactory().newMediaSpecInstance();
			spec.setQuality(MediaQuality.GOOD.name());
			spec.setSize(MediaSize.THUMB_NORMAL.name());
			userSession.setThumbnailSetting(spec);
		}
		if ( user != null && user.getViewSetting() != null ) {
			userSession.setViewSetting(user.getViewSetting());
		} else {
			MediaSpec spec = getDomainObjectFactory().newMediaSpecInstance();
			spec.setQuality(MediaQuality.GOOD.name());
			spec.setSize(MediaSize.NORMAL.name());
			userSession.setViewSetting(spec);
		}
		HttpSession session = request.getSession(true);
		session.setAttribute(WebConstants.SES_KEY_USER_SESSION_DATA, userSession);
	}

	/**
	 * Save an anonymous user session.
	 * 
	 * @param request
	 *        the request
	 * @param thumbnailSpec
	 *        the MediaSpec to use for thumbnail items
	 * @param viewSpec
	 *        the MediaSpec to use for full-size items
	 */
	public void saveAnonymousUserSession(HttpServletRequest request, MediaSpec thumbnailSpec,
			MediaSpec viewSpec) {
		HttpSession session = request.getSession(true);
		Session userSession = getUserSession(request);
		if ( userSession == null ) {
			userSession = getDomainObjectFactory().newSessionInstance();
			session.setAttribute(WebConstants.SES_KEY_USER_SESSION_DATA, userSession);
		}
		userSession.setThumbnailSetting(thumbnailSpec);
		userSession.setViewSetting(viewSpec);
	}

	/**
	 * Remove all user session data.
	 * 
	 * <p>
	 * This method simply invalidates the current session (if available).
	 * </p>
	 * 
	 * @param request
	 *        the current reqeust
	 */
	public void clearUserSessionData(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if ( session == null )
			return;
		session.invalidate();
	}

	/**
	 * Get the <code>Session</code> from the current session.
	 * 
	 * @param request
	 *        the current request
	 * @return the UiSessionData instance, or <em>null</em> if not available
	 */
	public Session getUserSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if ( session == null )
			return null;
		return (Session) session.getAttribute(WebConstants.SES_KEY_USER_SESSION_DATA);
	}

	/**
	 * Set a flag indicating the saved session URL should not be cleared on this
	 * request.
	 * 
	 * @param request
	 *        the current request
	 */
	public static final void setSaveRequestURLFlag(HttpServletRequest request) {
		request.setAttribute(WebConstants.REQ_KEY_SAVE_SAVED_URL, Boolean.TRUE);
	}

	/**
	 * Return <em>true</em> if the save request URL flag is currently set.
	 * 
	 * @param request
	 *        the current request
	 * @return <em>true</em> if the save request URL flag is currently set
	 */
	public static final boolean isSaveRequestURL(HttpServletRequest request) {
		if ( request.getAttribute(WebConstants.REQ_KEY_SAVE_SAVED_URL) != null ) {
			return true;
		}
		return false;
	}

	/**
	 * Clear the flag indicating the saved session URL should not be cleared on
	 * this request.
	 * 
	 * @param request
	 *        the current request
	 */
	public static final void clearSaveRequestURLFlag(HttpServletRequest request) {
		request.removeAttribute(WebConstants.REQ_KEY_SAVE_SAVED_URL);
	}

	/**
	 * Remove the saved session URL.
	 * 
	 * @param request
	 *        the current request
	 */
	public void clearSavedRequestURL(HttpServletRequest request) {
		request.getSession().removeAttribute(WebConstants.SES_KEY_SAVED_URL);
		request.removeAttribute(WebConstants.REQ_KEY_SAVE_SAVED_URL);
	}

	/**
	 * Release any saved request theme, previously saved on the calling thread
	 * via {@link #saveRequestTheme(Theme)}.
	 * 
	 * @see WebHelper#saveRequestTheme(Theme)
	 */
	public void clearSavedRequestTheme() {
		themeHolder.remove();
	}

	/**
	 * Get a list of work tickets for the current user.
	 * 
	 * <p>
	 * This method will only return tickets for jobs that the {@link WorkBiz}
	 * returns job information for. Tickets that do not have any job information
	 * associated with them will be pruned from the user's list of tickets and
	 * not returned.
	 * </p>
	 * 
	 * @param request
	 *        the request
	 * @return list of tickets
	 */
	@SuppressWarnings("unchecked")
	public Long[] getUserWorkTickets(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		Set<Long> tickets = Collections.emptySet();
		if ( session != null ) {
			Set<Long> sessionTickets = (Set<Long>) session
					.getAttribute(WebConstants.SES_KEY_WORK_TICKETS);
			if ( sessionTickets != null ) {
				tickets = sessionTickets;
			}

			synchronized ( tickets ) {
				// prune non-existing (completed) tickets from session
				for ( Long ticket : tickets ) {
					if ( !workBiz.infoExists(ticket) ) {
						tickets.remove(ticket);
					}
				}
			}
		}

		return tickets.toArray(new Long[tickets.size()]);
	}

	/**
	 * Populate data from a WorkInfo instance onto a model Map.
	 * 
	 * @param request
	 *        the request, so can save the ticket into session
	 * @param workInfo
	 *        the WorkInfo
	 * @param model
	 *        the model Map
	 */
	@SuppressWarnings("unchecked")
	public void populateModelWorkInfo(HttpServletRequest request, WorkInfo workInfo,
			Map<String, Object> model) {
		HttpSession session = request.getSession(false);
		if ( session != null ) {
			Set<Long> tickets = (Set<Long>) session.getAttribute(WebConstants.SES_KEY_WORK_TICKETS);
			if ( tickets == null ) {
				tickets = Collections.synchronizedSet(new LinkedHashSet<Long>());
				session.setAttribute(WebConstants.SES_KEY_WORK_TICKETS, tickets);
			}
			tickets.add(workInfo.getTicket());
		}
		model.put("work.ticket", workInfo.getTicket());
		model.put("work.displayName", workInfo.getDisplayName());

		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		sdf.setTimeZone(dateTimeZone);
		model.put("work.submitTime",
				workInfo.getSubmitTime() + " " + sdf.format(new Date(workInfo.getSubmitTime())));

		model.put("work.message", workInfo.getMessage());
		model.put("work.completed", String.valueOf(workInfo.getAmountCompleted()));
	}

	/**
	 * Add a {@link MediaSizeDefinition} for every {@link MediaSize} in the
	 * system.
	 * 
	 * @param list
	 *        the list to add MediaSizeDefinition objects to
	 */
	public void populateMediaSizeAndQuality(List<MediaSizeDefinition> list) {
		for ( MediaSize size : MediaSize.values() ) {
			Geometry geo = mediaBiz.getGeometry(size);
			MediaSizeDefinition def = domainObjectFactory.newMediaSizeDefinitionInstance();
			def.setSize(size.name());
			def.setHeight(geo.getHeight());
			def.setWidth(geo.getWidth());
			list.add(def);
		}
	}

	/**
	 * @return Returns the userBiz.
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz
	 *        The userBiz to set.
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	/**
	 * @return the datePattern
	 */
	public String getDatePattern() {
		return datePattern;
	}

	/**
	 * @param datePattern
	 *        the datePattern to set
	 */
	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	/**
	 * @return the dateTimeZone
	 */
	public TimeZone getDateTimeZone() {
		return dateTimeZone;
	}

	/**
	 * @param dateTimeZone
	 *        the dateTimeZone to set
	 */
	public void setDateTimeZone(TimeZone dateTimeZone) {
		this.dateTimeZone = dateTimeZone;
	}

	/**
	 * @return the domainObjectFactory
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory
	 *        the domainObjectFactory to set
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	/**
	 * @return the workBiz
	 */
	public WorkBiz getWorkBiz() {
		return workBiz;
	}

	/**
	 * @param workBiz
	 *        the workBiz to set
	 */
	public void setWorkBiz(WorkBiz workBiz) {
		this.workBiz = workBiz;
	}

	/**
	 * @return the messageSource
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * @param messageSource
	 *        the messageSource to set
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @return the systemBiz
	 */
	public SystemBiz getSystemBiz() {
		return systemBiz;
	}

	/**
	 * @param systemBiz
	 *        the systemBiz to set
	 */
	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}

	/**
	 * @return the mediaBiz
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz
	 *        the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

}
