/* ===================================================================
 * BumpMapEffect.java
 * 
 * Created Oct 28, 2010 12:27:09 PM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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

package magoffin.matt.ma2.image.im4java;

import java.io.File;
import java.io.IOException;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

import org.im4java.core.CompositeCmd;
import org.im4java.core.IMOperation;
import org.springframework.core.io.Resource;

/**
 * A watermark effect for IM4Java based processing, that creates a 3D bump map
 * from the watermark image.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class BumpMapEffect extends BaseIM4JavaMediaEffect {

	/**
	 * Default constructor.
	 */
	public BumpMapEffect() {
		super("bump." +MediaEffect.KEY_WATERMARK);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.im4java.IM4JavaMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, org.im4java.core.IMOperation)
	 */
	public ImageCommandAndOperation applyEffect(MediaItem item, MediaRequest request,
			IMOperation baseOperation) {
		Resource watermarkResource = (Resource)request.getParameters().get(
				MediaEffect.MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE);
		if ( watermarkResource == null || !watermarkResource.exists() ) {
			return null;
		}
		File watermarkFile = null;
		try {
			watermarkFile = watermarkResource.getFile();
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		IMOperation op = new IMOperation();
		op.compose("Bumpmap");
		op.gravity("SouthEast");
		op.addImage(watermarkFile.getAbsolutePath());
		op.addImage(2); // add source and destination image placeholders
		return new ImageCommandAndOperation(new CompositeCmd(), op);
	}

}
