/* ===================================================================
 * MediaServer.java
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
 * $Id: MediaServer.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.MediaResponse;
import magoffin.matt.ma.NullMediaResponse;
import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaServerConfig;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.StringUtil;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

/**
 * Media Server servlet.
 * 
 * <p>The following parameters are recognized:</p>
 * 
 * <table border="1" cellpadding="2" cellspacing="1">
 * </table>
 * 
 * <p>Created Sep 29, 2002 4:02:16 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class MediaServer 
extends HttpServlet 
{	
	private final static Logger LOG = Logger.getLogger(MediaServer.class);
	
	private final static Logger LOG_PROFILE = Logger.getLogger(
		MediaServer.class.getName()+".profiler");
	
	private MediaItemBiz itemBiz = null;
	
	private ObjectPool mediaResponsePool = null;
	private ObjectPool nullResponsePool = null;
	
	private MediaServerHitThread hitThread = null;
	
	private String authUrl = null;
	

/**
 * Initialize the servlet.
 * 
 * <p>This servlet assumes the Initializer servlet has already been
 * initialized and placed the necessary objects into the servlet
 * context, available for this servlet to access.</p>
 *  * @param config the servlet config
 * @see javax.servlet.Servlet#init(ServletConfig) * @throws ServletException
 */
public void init(ServletConfig config)
throws ServletException
{
	super.init(config);
	
	LOG.info("Initializing MediaServer");
	
	ServletContext sc = config.getServletContext();
	PoolFactory mrPoolFactory = null;
	
	try {
		
		BizFactory bizFactory = ServletUtil.getBizIntfFactory(getServletContext());
		
		itemBiz = (MediaItemBiz)bizFactory.getBizInstance(BizConstants.MEDIA_ITEM_BIZ);
		
		mediaResponsePool = ServletUtil.getMediaRequestObjectPool(sc,HttpMediaResponse.class);
		nullResponsePool = ServletUtil.getMediaRequestObjectPool(sc,NullMediaResponse.class);
		
		mrPoolFactory = (PoolFactory)sc.getAttribute(ServletConstants.APP_KEY_MEDIA_REQUEST_POOL_FACTORY);
		
		if ( mrPoolFactory == null ) {
			throw new ServletException("Media request pool factory not initiailized.");
		}
		
		hitThread = new MediaServerHitThread(bizFactory,
				ServletUtil.getCacheFactory(config.getServletContext()));
		Thread t = new Thread(hitThread);
		t.setName("MediaServerHitThread");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		
		// get auth error URL
		MediaAlbumConfig appConfig = null;
		try {
			appConfig = (MediaAlbumConfig)sc.getAttribute(
					ServletConstants.APP_KEY_CONFIG);
		} catch ( Exception e ) {
			LOG.fatal("Unable to obtain MediaAlbumConfig object from application context");
			throw e;
		}
		
		MediaServerConfig serverConfig = appConfig.getMediaServer();
		authUrl = serverConfig.getAuthUrl();
		
		if ( LOG.isInfoEnabled() ) {
			LOG.info("MediaServer authorization URL set to " +authUrl);
		}
		
	} catch ( MediaAlbumException e ) {
		LOG.fatal("Can't configure MediaServer: " +e.toString(),e);
		throw new ServletException("Can't initialize MediaServer: " +e.getMessage());
	} catch ( Exception e ) {
		LOG.fatal("Unknown exception initializing MediaServer: " +e.toString(),e);
		throw new ServletException("Can't initialize MediaServer: " +e.getMessage());
	}
	
	LOG.info("MediaServer initialized");
}


public final void doGet(HttpServletRequest req, HttpServletResponse res) 
throws ServletException, IOException
{
	handleHttpRequest(req,res);
}

public final void doPost(HttpServletRequest req, HttpServletResponse res) 
throws ServletException, IOException
{
	handleHttpRequest(req,res);
}

/* (non-Javadoc)
 * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
 */
protected long getLastModified(HttpServletRequest req) 
{
	NullMediaResponse mResponse = null;
	try {
		mResponse = (NullMediaResponse)nullResponsePool.borrowObject();
		handleRequest(req,null,mResponse);
		return mResponse.getModifiedDate();
	} catch ( UserAccessException e ) {
		// ignore
	} catch ( Exception e ) {
		LOG.error("Exception getting HTTP last modified date",e);
	} finally {
		if ( mResponse != null ) {
			try {
				nullResponsePool.returnObject(mResponse);
			} catch ( Exception e ) {
				LOG.error("Exception returning MediaResponse to pool: " +e.getMessage());
			}
		}
	}
	return super.getLastModified(req);
}

private final void handleHttpRequest(HttpServletRequest req, HttpServletResponse res)
throws ServletException, IOException
{
	long startTime = System.currentTimeMillis();
	HttpMediaResponse mResponse = null;
	try {
		mResponse = (HttpMediaResponse)mediaResponsePool.borrowObject();
		mResponse.setHttpServletResponse(res);
		handleRequest(req,res,mResponse);
	} catch ( ServletException e ) {
		throw e;
	} catch ( IOException e ) {
		throw e;
	} catch ( UserAccessException e ) {
		// forward to logon page
		ServletUtil.saveRequestURL(req);
		getServletContext().getRequestDispatcher(authUrl).forward(
				req,res);
	} catch ( Exception e ) {
		throw new ServletException("Unknown error",e);
	} finally {
		if ( mResponse != null ) {
			if ( LOG_PROFILE.isInfoEnabled() && mResponse.getItem() != null) {
				long end = System.currentTimeMillis() - startTime;
				StringBuffer buf = new StringBuffer();
				buf.append(end).append("; Item ").append(
						mResponse.getItem().getItemId()).append("; {");
				Enumeration enum = req.getParameterNames();
				boolean multi = false;
				while ( enum.hasMoreElements() ) {
					if ( multi ) {
						buf.append(",");
					} else {
						multi = true;
					}
					String key = (String)enum.nextElement();
					buf.append(key).append("=")
						.append(req.getParameter(key));
				}
				buf.append("}");
				LOG_PROFILE.info(buf.toString());
			}
			try {
				mediaResponsePool.returnObject(mResponse);
			} catch ( Exception e ) {
				LOG.error("Exception returning MediaResponse to pool: " +e.getMessage());
			}
		}
	}
}


private final void handleRequest(
		HttpServletRequest req, 
		HttpServletResponse res, 
		MediaResponse mResponse)
throws ServletException, UserAccessException
{
	Integer itemId = null;
	try {
		itemId = Integer.valueOf(req.getParameter(
				ServletConstants.REQ_KEY_MEDIA_SERVER_ITEM_ID));
	} catch ( Exception e ) {
		// ignore
	}
	
	if ( itemId == null ) {
		LOG.debug("No ID supplied to server.");
		if ( res != null ) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return;
	}
	
	MediaRequestHandler handler = null;
	MediaRequestHandlerParams params = null;
	MediaItem item = null;
	boolean postProcessParams = false;
	
	boolean wantOriginal = StringUtil.parseBoolean(req.getParameter(
			ServletConstants.REQ_KEY_MEDIA_SERVER_ORIGINAL));
	//String albumId = req.getParameter(ServletConstants.REQ_KEY_ALBUM_ID);
	
	UserSessionData usd = (UserSessionData)req.getSession()
			.getAttribute(ServletConstants.SES_KEY_USER);
	
	try {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Requested ID: " +itemId +", wantOriginal: " +wantOriginal);
		}
		
		// get the media item from the db
		try {
			item = itemBiz.getMediaItemById(itemId,ApplicationConstants.CACHED_OBJECT_ALLOWED);
			if ( item == null ) {
				LOG.error("Media ID " +itemId + " not found");
				if ( res != null ) {
					res.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
				return;
			}
		} catch ( Exception e ) {
			LOG.error("Unknown exception getting media item ID " +itemId
				+": " +e,e);
			throw new ServletException("Can't get media item");
		}
		
		mResponse.setItem(item);

		handler = itemBiz.getHandlerForItem(item);
		if ( handler == null ) {
			LOG.error("No handler defined for MIME type " +item.getMime());
			if ( res != null ) {
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return;
		}
		
		params = handler.getParamInstance();
		postProcessParams = true;
		
		User actingUser = usd != null ? usd.getUser() : null;
		
		// verify user has permission to see image
		if ( !itemBiz.canUserViewMediaItem(item.getItemId(), actingUser) ) {
			LOG.error("User " +(actingUser == null ? "(anonymous)" : actingUser.getUsername()) 
					+" not allowed to view "+item.getItemId());
			throw new UserAccessException(
					actingUser == null ? "(anonymous)" : actingUser.getUsername(),
					"Access denied");
		}
		
		if ( !wantOriginal && params != null ) {
			String[] optionNames = params.getSupportedParamNames();
			String[] adminNames = params.getAdminOnlyParamNames();
			boolean notAdmin = !(usd != null && usd.isAdmin());
			if ( optionNames != null ) {
				for ( int i = 0; i < optionNames.length; i++ ) {
					if ( notAdmin &&
						Arrays.binarySearch(adminNames,optionNames[i]) > -1 ) {
						if ( LOG.isDebugEnabled() ) {
							LOG.debug("Skipping admin-only option param "
								+optionNames[i]);
						}
						continue;
					}
					String val = req.getParameter(optionNames[i]);
					if ( val != null ) {
						if ( LOG.isDebugEnabled() ) {
							LOG.debug("Setting option param " +optionNames[i]
								+ " to " +val );
						}
						params.setParam(optionNames[i], val);
					}
				}
			}
		} else if ( wantOriginal && params != null ) {
			params.setParam(MediaRequestHandlerParams.WANT_ORIGINAL,Boolean.TRUE);
		}
		
		params.setParam(MediaRequestHandlerParams.LOCALE,req.getLocale());
		
		try {
			OutputStream out = null;
			if ( res != null ) {
				res.setContentType(handler.getOutputMime(item,params));
				if ( wantOriginal ) {
					String fileName = StringUtil.substringAfter(item.getPath(),'/');
					if ( fileName == null ) {
						fileName = item.getPath();
					}
					res.setHeader("Content-Disposition","filename=\"" +fileName+ "\"");
				}
				if ( params.hasParamSet(MediaRequestHandlerParams.SIZE) &&
						!params.getParam(MediaRequestHandlerParams.SIZE).toString().startsWith("t")) 
				{
					// not a thumbnail so increment hit and check if user wants watermark applied
					hitThread.enqueue(item.getItemId());
				}
				out = res.getOutputStream();
			}
			
			postProcessParams = false;
			itemBiz.handleMediaItem(out,item,wantOriginal,handler,params,mResponse);
		} catch ( FileNotFoundException e ) {
			LOG.error("File not found: " +e.getMessage());
			if ( res != null ) {
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch ( IOException e ) {
			// we don't log these errors for writing to the output stream
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("IOException on media " +item.getItemId() + ": "+ e.getMessage());
			}
		}
	} catch ( UserAccessException e ) {
		throw e;
	} catch ( MediaAlbumException e ) {
		Exception nestedException = e.getRootException();
		LOG.error("MediaAlbumException on media " +item.getItemId() + ": "
			+ e.getMessage(),e);
		if ( nestedException != null ) {
			LOG.error("Original exception on media  " +item.getItemId() +": " 
			+nestedException.getMessage(),nestedException);
		}
		throw new ServletException("Can't get media: " +e.getMessage());
	} finally {
		if ( postProcessParams && handler != null ) {
			try {
				handler.postProcessParams(item,params);
			} catch ( Exception e ) {
				LOG.warn("Exception on post-process params: " +e.toString());
			}
		}
	}
}

/* (non-Javadoc)
 * @see javax.servlet.Servlet#destroy()
 */
public void destroy() {
	super.destroy();
	hitThread.stop();
}

}
