/* ===================================================================
 * ShadowServerJMagick.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 26, 2004 6:49:41 PM.
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
 * $Id: ShadowServerJMagick.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magick.ColorspaceType;
import magick.DrawInfo;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magick.PixelPacket;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.util.FileUtil;

/**
 * ShadowServer implementation using JMagick.
 * 
 * @see magoffin.matt.ma.servlet.ShadowServer
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ShadowServerJMagick extends ShadowServer 
{
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.ShadowServer#generateShadow(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ShadowServer.ShadowParams, java.io.OutputStream)
 */
protected void generateShadow(HttpServletRequest req,
		HttpServletResponse res, ShadowParams params, OutputStream out)
		throws IOException 
{
	try {
		ImageInfo info = new ImageInfo();
		info.setColorspace(ColorspaceType.TransparentColorspace);
		int memWidth = params.width+params.blurRadius*4;
		int memHeight = params.height +params.blurRadius*4;
		info.setSize(memWidth+"x" +memHeight);
		MagickImage image = new MagickImage();
		image.allocateImage(info);
		
		DrawInfo shape = new DrawInfo(info);
		StringBuffer buf = new StringBuffer();
		buf.append("roundRectangle")
			.append(" ").append(params.blurRadius*2)
			.append(",").append(params.blurRadius*2)
			.append(" ").append(params.blurRadius*2+params.width)
			.append(",").append(params.blurRadius*2+params.height)
			.append(" ").append(params.cornerRadius)
			.append(",").append(params.cornerRadius);
		shape.setPrimitive(buf.toString());
		int color = params.color < 0 ? Color.LIGHT_GRAY.getRGB() : params.color;
		int opacity = params.opacity < 0 ? 100 : params.opacity;
		PixelPacket fillColor = new PixelPacket(color>>16&0xFF,color>>8&0xFF,
				color&0xFF,opacity); // hmm IM colors 0 - 65535
		//PixelPacket fillColor = new PixelPacket(65535,0,0,200);
		shape.setFill(fillColor);
		image.drawImage(shape);

		// fill with transparency
		image.transparentImage(fillColor,0);
		
		image = image.blurImage(params.blurRadius,1);
		File tmpFile = File.createTempFile("shadow-server",".png");
		image.setFileName(tmpFile.getAbsolutePath());
		image.writeImage(info);
		
		// copy to output stream
		FileUtil.slurp(tmpFile,out);
		
		tmpFile.delete();
	} catch ( MagickException e ) {
		throw new MediaAlbumRuntimeException("JMagick exception",e);
	}
}
	
}
