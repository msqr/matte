/* ===================================================================
 * ShadowServerController.java
 * 
 * Created Mar 22, 2006 8:48:10 AM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: ShadowServerController.java,v 1.4 2007/07/13 22:17:51 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web;

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.LastModified;

import com.sun.glf.goodies.GaussianKernel;

/**
 *  Controller for serving up media item shadows.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/13 22:17:51 $
 */
public class ShadowServerController extends AbstractCommandController implements LastModified {
	
	/** The MIME type of the generated image. */
	public static final String OUTPUT_MIME = "image/png";
	
	private Cache shadowCache = null;

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractCommandController#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		Command cmd = (Command)command;
		
		String cacheKey = getCacheKey(cmd);
		
		if ( shadowCache != null ) {
			try {
				Element cacheElement = shadowCache.get(cacheKey);
				if ( cacheElement != null ) {
					response.setContentType(OUTPUT_MIME);
					byte[] bytes = (byte[])cacheElement.getValue();
					response.setContentLength(bytes.length);
					OutputStream out = response.getOutputStream();
					for ( int i = 0; i < bytes.length; i++ ) {
						out.write(bytes);
					}
					return null;
				}
			} catch ( CacheException e ) {
				logger.warn("Exception reading cache key [" +cacheKey +"], ignoring cache: " 
						+e.toString());
			}
		}
		
		OutputStream out = response.getOutputStream();
		
		if ( shadowCache != null ) {
			out = new ByteArrayOutputStream();
		}
			
		generateShadow(cmd,out);
		
		response.setContentType(OUTPUT_MIME);
		if ( shadowCache != null ) {
			// cache and return cached data
			byte[] bytes = ((ByteArrayOutputStream)out).toByteArray();
			Element cacheElement = new Element(cacheKey,bytes);
			shadowCache.put(cacheElement);
			response.setContentLength(bytes.length);
			response.getOutputStream().write(bytes);
		}
		
		return null;
	}
	
	/**
	 * Generate a cache key for the given command.
	 * 
	 * @param cmd the command
	 * @return a cache key
	 */
	protected String getCacheKey(Command cmd) {
		return (cmd.width==null?"":cmd.width.toString())
			+(cmd.height==null?"":cmd.height.toString())
			+(cmd.blurRadius==null?"":cmd.blurRadius.toString())
			+(cmd.cornerRadius==null?"":cmd.cornerRadius.toString())
			+(cmd.color==null?"":cmd.color.toString())
			+(cmd.opacity==null?"":cmd.opacity.toString());
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	public long getLastModified(HttpServletRequest request) {
		if ( shadowCache != null ) {
			Command cmd = new Command();
			try {
				ServletRequestDataBinder binder = createBinder(request, cmd);
				binder.bind(request);
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
			
			String cacheKey = getCacheKey(cmd);
			Element cacheElement;
			try {
				cacheElement = shadowCache.getQuiet(cacheKey);
				if ( cacheElement != null ) {
					return cacheElement.getCreationTime();
				}
			} catch ( Exception e ) {
				logger.warn("Exception reading cache key [" +cacheKey 
						+"], ignoring cache: " +e.toString());
			}
			
		}
		return -1;
	}
	
	/**
	 * Generate the shadow and send to the output stream.
	 * 
	 * <p>Other classes might want to override this method to generate the shadow
	 * in a different way.</p>
	 * 
	 * @param cmd the command
	 * @param out the output stream
	 * @throws IOException if an IO error occurs
	 */
	protected void generateShadow(Command cmd, OutputStream out) throws IOException {
		// 1: create gray-scale rounded rect for shadow mask
		BufferedImage image = new BufferedImage(cmd.width+cmd.blurRadius*4,
				cmd.height +cmd.blurRadius*4, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0,0,image.getWidth(),image.getHeight());
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(rh);
		g.setPaint(Color.WHITE);
		g.fill(new RoundRectangle2D.Double(cmd.blurRadius*2,cmd.blurRadius*2,
				cmd.width,cmd.height,cmd.cornerRadius*2,cmd.cornerRadius*2));
		g.dispose();
		
		// 2: blur image
		ConvolveOp blur = new ConvolveOp(new GaussianKernel(cmd.blurRadius));
		image = blur.filter(image,null);
		image = image.getSubimage(cmd.blurRadius,cmd.blurRadius,
				cmd.width+cmd.blurRadius*2,cmd.height+cmd.blurRadius*2);
		
		// 3: create alpha image
		BufferedImage image2 = new BufferedImage(image.getWidth(),image.getHeight(),
				BufferedImage.TYPE_4BYTE_ABGR);
		g = image2.createGraphics();
		Color fillColor = cmd.color == null ? Color.GRAY : new Color(cmd.color);
		g.setColor(fillColor);
		g.fillRect(0,0,image2.getWidth(),image2.getHeight());
		g.dispose();
	    
		// 4: copy shadow mask to alpha
		WritableRaster alphaRaster = image2.getAlphaRaster();
		alphaRaster.setDataElements(0,0,image.getRaster());
		
		image = image2;
		
		Iterator<ImageWriter> writers 
			= ImageIO.getImageWritersByMIMEType(OUTPUT_MIME);
		if ( !writers.hasNext() ) {
			return;
		}
		ImageWriter writer = writers.next();
		
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

	/**
	 * Command class for shadow server.
	 */
	public static class Command {
		private Integer width = null;
		private Integer height = null;
		private Integer blurRadius = 5;
		private Integer cornerRadius = 10;
		private Integer color = null;
		private Integer opacity = null;
		
		/**
		 * @param blurRadius The blurRadius to set.
		 */
		public void setB(Integer blurRadius) {
			this.blurRadius = blurRadius;
		}
		
		/**
		 * @param color The color to set.
		 */
		public void setC(Integer color) {
			this.color = color;
		}
		
		/**
		 * @param cornerRadius The cornerRadius to set.
		 */
		public void setR(Integer cornerRadius) {
			this.cornerRadius = cornerRadius;
		}
		
		/**
		 * @param height The height to set.
		 */
		public void setH(Integer height) {
			this.height = height;
		}
		
		/**
		 * @param opacity The opacity to set.
		 */
		public void setO(Integer opacity) {
			this.opacity = opacity;
		}
		
		/**
		 * @param width The width to set.
		 */
		public void setW(Integer width) {
			this.width = width;
		}
		
		/**
		 * @param blurRadius The blurRadius to set.
		 */
		public void setBlurRadius(Integer blurRadius) {
			this.blurRadius = blurRadius;
		}
		
		/**
		 * @param color The color to set.
		 */
		public void setColor(Integer color) {
			this.color = color;
		}
		
		/**
		 * @param cornerRadius The cornerRadius to set.
		 */
		public void setCornerRadius(Integer cornerRadius) {
			this.cornerRadius = cornerRadius;
		}

		
		/**
		 * @param height The height to set.
		 */
		public void setHeight(Integer height) {
			this.height = height;
		}
		
		/**
		 * @param opacity The opacity to set.
		 */
		public void setOpacity(Integer opacity) {
			this.opacity = opacity;
		}
		
		/**
		 * @param width The width to set.
		 */
		public void setWidth(Integer width) {
			this.width = width;
		}

	}
	
	/**
	 * @return Returns the shadowCache.
	 */
	public Cache getShadowCache() {
		return shadowCache;
	}
	
	/**
	 * @param shadowCache The shadowCache to set.
	 */
	public void setShadowCache(Cache shadowCache) {
		this.shadowCache = shadowCache;
	}

}
