/* ===================================================================
 * MediaItemTag.java
 * 
 * Created Apr 15, 2004 8:11:10 PM
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
 * $Id: MediaItemTag.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.jsp;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.ServletUtil;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.util.HashMapPoolableFactory;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.struts.util.RequestUtils;

/**
 * JSP tag to render the HTML for the display of a media item.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class MediaItemTag extends TagSupport
{
	/** Object pool for HashMap objects. */
	private static ObjectPool MAP_POOL = new StackObjectPool(
			new HashMapPoolableFactory());
	
	private String name;
	private String property;
	private String size;
	private String compression;
	private boolean thumbnail;
	
/* (non-Javadoc)
 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
 */
public int doStartTag() throws JspException
{
	Object o = RequestUtils.lookup(pageContext,name,property,null); // get bean
	
	if ( o == null ) {
		throw new JspException("Object not found: name = " +name 
				+", property = " +property);
	}
	
	MediaItem item = null;
	
	if ( o instanceof Integer ) {
		BizFactory bizFactory = (BizFactory)pageContext.getServletContext().getAttribute(
				ServletConstants.APP_KEY_BIZ_INTF_FACTORY);
		MediaItemBiz itemBiz = (MediaItemBiz)bizFactory.getBizInstance(
				BizConstants.MEDIA_ITEM_BIZ);
		Integer itemId = (Integer)o;
		try {
			item = itemBiz.getMediaItemById(itemId,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
		} catch ( MediaAlbumException e ) {
			throw new JspException(e.getMessage(),e);
		}
	} else if ( o instanceof MediaItem ) {
		item = (MediaItem)o;
	} else {
		throw new JspException("Object not supported: " +o.getClass().getName());
	}
	
	String mime = item.getMime();
	try {
		renderImg(item);
	} catch ( IOException e ) {
		throw new JspException(e.getMessage(),e);
	}
	
	return EVAL_PAGE;
}

private void renderImg(MediaItem item) throws IOException, JspException {
	StringBuffer buf = new StringBuffer();
	buf.append("<img src=\"");
	Map m = null;
	try {
		m = borrowDefaultMap(item);
		buf.append(RequestUtils.computeURL(pageContext,
				StrutsConstants.MEDIA_SERVER_FORWARD,
				null,null,null,m,null,false));
		
		// get width/height attributes
		BizFactory bizFactory = ServletUtil.getBizIntfFactory(
				pageContext.getServletContext());
		MediaItemBiz itemBiz = (MediaItemBiz)bizFactory.getBizInstance(
				BizConstants.MEDIA_ITEM_BIZ);
		Geometry geo = itemBiz.getMediaItemGeometry(
				item,
				(String)m.get(MediaRequestHandlerParams.SIZE), 
				(String)m.get(MediaRequestHandlerParams.COMPRESSION));
		
		buf.append("\" width=\"").append(geo.getWidth())
			.append("\" height=\"").append(geo.getHeight())
			.append("\" />");
	} catch ( Exception e ) {
		throw new JspException("Exception generating Media Server URL",e);
	} finally {
		returnMap(m);
	}
	pageContext.getOut().print(buf.toString());
}

/**
 * Borrow a Map from a pool, pre-populated with the media item ID, size, 
 * and compression values for this request.
 * 
 * <p>If the size and/or compression are not set in the tag fields, 
 * this method attempts to get them from the UserSessionData 
 * <code>thumbSpec</code> in session. If that is not available it 
 * resorts to a normal size and normal compression.</p>
 * 
 * @param item the media item
 * @return Map, which should be returned via {@link #returnMap(Map)}
 * @throws JspException if an error occurs
 */
private Map borrowDefaultMap(MediaItem item) throws JspException
{
	Map m = null;
	MediaSpec thumbSpec = null;
	if ( pageContext.getSession().getAttribute(ServletConstants.SES_KEY_USER) != null ) {
		UserSessionData usd = (UserSessionData) pageContext.getSession().getAttribute(
				ServletConstants.SES_KEY_USER);
		thumbSpec = usd.getThumbSpec();
	}
	try {
		m = (Map)MAP_POOL.borrowObject();
		m.put(ServletConstants.REQ_KEY_MEDIA_SERVER_ITEM_ID,item.getItemId());
		String mySize = size;
		if ( mySize == null || mySize.length() < 1 ) {
			if ( thumbSpec != null ) {
				mySize = thumbSpec.getSize();
			} else {
				mySize = MediaSpecUtil.SIZE_NORMAL;
			}
		}
		m.put(MediaRequestHandlerParams.SIZE, thumbnail 
					? MediaRequestHandlerParams.THUMBNAIL_SIZE_PREFIX +mySize
					: mySize);
		String myCompression = compression;
		if ( compression == null || compression.length() < 1 ) {
			if ( thumbSpec != null ) {
				myCompression = thumbSpec.getCompress();
			} else {
				myCompression = MediaSpecUtil.COMPRESS_NORMAL;
			}
		}
		m.put(MediaRequestHandlerParams.COMPRESSION,myCompression);
	} catch ( Exception e ) {
		throw new JspException("Unable to borrow Map",e);
	}
	return m;
}

/**
 * Return a Map borrowed via {@link #borrowDefaultMap(MediaItem)}.
 * @param m the Map to return (<em>null</em> is allowed)
 */
private void returnMap(Map m) {
	if ( m != null ) {
		try {
			MAP_POOL.returnObject(m);
		} catch ( Exception e ) {
			// ignore
		}
	}
}

/**
 * @return Returns the name.
 */
public String getName()
{
	return name;
}
/**
 * @param name The name to set.
 */
public void setName(String name)
{
	this.name = name;
}
/**
 * @return Returns the property.
 */
public String getProperty()
{
	return property;
}
/**
 * @param property The property to set.
 */
public void setProperty(String property)
{
	this.property = property;
}

/**
 * @return Returns the compression.
 */
public String getCompression()
{
	return compression;
}

/**
 * @param compression The compression to set.
 */
public void setCompression(String compression)
{
	this.compression = compression;
}

/**
 * @return Returns the size.
 */
public String getSize()
{
	return size;
}

/**
 * @param size The size to set.
 */
public void setSize(String size)
{
	this.size = size;
}

/**
 * @return Returns the thumbnail.
 */
public boolean isThumbnail()
{
	return thumbnail;
}

/**
 * @param thumbnail The thumbnail to set.
 */
public void setThumbnail(boolean thumbnail)
{
	this.thumbnail = thumbnail;
}
}
