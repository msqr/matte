/* ===================================================================
 * ShadowServer.java
 * 
 * Created Apr 21, 2004 10:32:52 PM
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
 * $Id: ShadowServer.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.util.ResetableObject;
import magoffin.matt.util.ResetablePoolableFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.log4j.Logger;

import com.sun.glf.goodies.GaussianKernel;

/**
 * Generate shadow backgrounds for images.
 * 
 * <p>Accepts the following parameters:</p>
 * 
 * <dl>
 * <dt>w</dt>
 * <dd>The width, in pixels.</dd>
 * 
 * <dt>h</dt>
 * <dd>The height, in pixels.</dd>
 * 
 * <dt>b</dt>
 * <dd>The blur radius, in pixels.</dd>
 * 
 * <dt>r</dt>
 * <dd>The radius of the rectangle corners.</dd>
 * 
 * <dt>c</dt>
 * <dd>(optional) RGB color, with R<<16|G<<8|B</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ShadowServer extends HttpServlet
{
	private static final long serialVersionUID = -1085877857429883670L;

	public static final String OUTPUT_MIME = "image/png";
	
	public static final class ShadowParams implements ResetableObject {
		int width;
		int height;
		int blurRadius;
		int cornerRadius;
		int color;
		int opacity;
		
		/* (non-Javadoc)
		 * @see magoffin.matt.util.ResetableObject#reset()
		 */
		public void reset() {
			width = height = cornerRadius = opacity = 0;
			blurRadius = 1;
			color = Color.LIGHT_GRAY.getRGB();
		}
	}
	
	private static final Logger LOG = Logger.getLogger(ShadowServer.class);
	
	private Cache cache;
	private ObjectPool paramObjectPool;
	
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
	if ( cache != null ) {
		String w = req.getParameter("w");
		String h = req.getParameter("h");
		String b = req.getParameter("b");
		String r = req.getParameter("r");
		String c = req.getParameter("c");
		String o = req.getParameter("o");
		String cacheKey = w+h+b+r+(c==null?"":c)+(o==null?"":o);
		
		Element cacheElement;
		try {
			cacheElement = cache.getQuiet(cacheKey);
			if ( cacheElement != null ) {
				return cacheElement.getCreationTime();
			}
		} catch ( Exception e ) {
			LOG.warn("Exception reading cache key [" +cacheKey 
					+"], ignoring cache: " +e.toString());
		}
		
	}
	return super.getLastModified(req);
}

private void setResponseHeaders(HttpServletResponse res) 
{
	res.setContentType(OUTPUT_MIME);
}

protected void generateShadow(HttpServletRequest req, HttpServletResponse res,
		ShadowParams params, OutputStream out) throws IOException
{
	// 1: create gray-scale rounded rect for shadow mask
	BufferedImage image = new BufferedImage(params.width+params.blurRadius*4,
			params.height +params.blurRadius*4, BufferedImage.TYPE_BYTE_GRAY);
	Graphics2D g = image.createGraphics();
	g.setColor(Color.BLACK);
	g.fillRect(0,0,image.getWidth(),image.getHeight());
	RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
	g.setRenderingHints(rh);
	g.setPaint(Color.WHITE);
	g.fill(new RoundRectangle2D.Double(params.blurRadius*2,params.blurRadius*2,
			params.width,params.height,params.cornerRadius*2,params.cornerRadius*2));
	g.dispose();
	
	// 2: blur image
	ConvolveOp blur = new ConvolveOp(new GaussianKernel(params.blurRadius));
	image = blur.filter(image,null);
	image = image.getSubimage(params.blurRadius,params.blurRadius,
			params.width+params.blurRadius*2,params.height+params.blurRadius*2);
	
	// 3: create alpha image
	BufferedImage image2 = new BufferedImage(image.getWidth(),image.getHeight(),
			BufferedImage.TYPE_4BYTE_ABGR);
	g = image2.createGraphics();
	Color fillColor = params.color < 0 ? Color.GRAY : new Color(params.color);
	g.setColor(fillColor);
	g.fillRect(0,0,image2.getWidth(),image2.getHeight());
	g.dispose();
    
	// 4: copy shadow mask to alpha
	WritableRaster alphaRaster = image2.getAlphaRaster();
	alphaRaster.setDataElements(0,0,image.getRaster());
	
	image = image2;
	
	Iterator writers = ImageIO.getImageWritersByMIMEType(OUTPUT_MIME);
	if ( !writers.hasNext() ) {
		return;
	}
	ImageWriter writer = writer = (ImageWriter)writers.next();
	
	try {
		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
		writer.setOutput(ios);
		ImageWriteParam param = writer.getDefaultWriteParam();
		IIOImage iioi = new IIOImage(image,null,null);
		writer.write(null,iioi,param);
	} finally {
		if ( writer != null ) {
			writer.dispose();
		}
	}
	
}

private final void handleHttpRequest(HttpServletRequest req, HttpServletResponse res)
throws IOException
{
	String w = req.getParameter("w");
	String h = req.getParameter("h");
	String b = req.getParameter("b");
	String r = req.getParameter("r");
	String c = req.getParameter("c");
	String o = req.getParameter("o");
	String cacheKey = w+h+b+r+(c==null?"":c)+(o==null?"":o);
	if ( cache != null ) {
		try {
			Element cacheElement = cache.get(cacheKey);
			if ( cacheElement != null ) {
				setResponseHeaders(res);
				byte[] bytes = (byte[])cacheElement.getValue();
				res.setContentLength(bytes.length);
				OutputStream out = res.getOutputStream();
				for ( int i = 0; i < bytes.length; i++ ) {
					out.write(bytes);
				}
				return;
			}
		} catch ( CacheException e ) {
			LOG.warn("Exception reading cache key [" +cacheKey +"], ignoring cache: " 
					+e.toString());
		}
	}
	
	OutputStream out = res.getOutputStream();
	
	if ( cache != null ) {
		out = new ByteArrayOutputStream();
	}
		
	ShadowParams params = null;
	try {
		params = borrowParams();
		params.width = Integer.parseInt(w);
		params.height = Integer.parseInt(h);
		params.cornerRadius = r != null ? Integer.parseInt(r) : 10;
		params.blurRadius = b != null ? Integer.parseInt(b) : 5;
		params.color =  c != null ? Integer.parseInt(c) : -1;
		params.opacity = o != null ? Integer.parseInt(c) : -1;
		generateShadow(req,res,params,out);
	} finally {
		returnParams(params);
	}
	
	setResponseHeaders(res);
	if ( cache != null ) {
		// cache and return cached data
		byte[] bytes = ((ByteArrayOutputStream)out).toByteArray();
		Element cacheElement = new Element(cacheKey,bytes);
		cache.put(cacheElement);
		res.setContentLength(bytes.length);
		res.getOutputStream().write(bytes);
	}
}

/* (non-Javadoc)
 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
 */
public void init(ServletConfig arg0) throws ServletException
{
	try {
		cache = CacheManager.getInstance().getCache(getClass().getName()+".CACHE");
	} catch ( Exception e ) {
		throw new ServletException("Unable to initialize shadow cache",e);
	}
	paramObjectPool = new StackObjectPool(new ResetablePoolableFactory(
			ShadowParams.class));
}

protected ShadowParams borrowParams() 
{
	try {
		return (ShadowParams)paramObjectPool.borrowObject();
	} catch ( Exception e ) {
		throw new MediaAlbumRuntimeException("Unable to borrow from pool",e);
	}
}

protected void returnParams(ShadowParams params) 
{
	if ( params == null ) return;
	try {
		paramObjectPool.returnObject(params);
	} catch ( Exception e ) {
		throw new MediaAlbumRuntimeException("Unable to return to pool",e);
	}
}

}
