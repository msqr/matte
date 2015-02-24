/* ===================================================================
 * GenericVideoMediaRequestHandler.java
 * 
 * Created Jul 14, 2004 5:16:30 PM
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
 * $Id: GenericVideoMediaRequestHandler.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.video.jmf;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Duration;
import javax.media.Manager;
import javax.media.NotConfiguredError;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.Renderer;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.UnsupportedPlugInException;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.ImageMetadata;
import magoffin.matt.ma.video.AbstractVideoMediaRequestHandler;
import magoffin.matt.ma.video.BasicVideoMetadata;
import magoffin.matt.ma.video.VideoMetadata;
import magoffin.matt.ma.xsd.MediaItem;

import org.apache.log4j.Logger;

/**
 * Base class for JMF based video media request handlers.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class GenericVideoMediaRequestHandler 
extends AbstractVideoMediaRequestHandler 
{
	private static final Logger LOG = Logger.getLogger(GenericVideoMediaRequestHandler.class);

    private static class Loader implements ControllerListener 
	{
    	private Processor processor = null;
    	private Object waitSync = new Object();
        private boolean stateTransitionOK = true;
        
        public Loader(Processor p) {
        	this.processor = p;
        	this.processor.addControllerListener(this);
        }
        
        public Processor getProcessor() {
        	return this.processor;
        }
        
    	/* (non-Javadoc)
    	 * @see javax.media.ControllerListener#controllerUpdate(javax.media.ControllerEvent)
    	 */
    	public void controllerUpdate(ControllerEvent event) 
    	{
    		if (event instanceof ConfigureCompleteEvent ||
    				event instanceof RealizeCompleteEvent ||
    				event instanceof PrefetchCompleteEvent) {
    			synchronized (waitSync) {
    				stateTransitionOK = true;
    				waitSync.notifyAll();
    			}
    		} else if (event instanceof ResourceUnavailableEvent) {
    			LOG.warn("ResourceUnavailableEvent: " +event +", source = "
    					+event.getSource() +", controller = "
						+event.getSourceController());
       			synchronized (waitSync) {
    				stateTransitionOK = false;
    				waitSync.notifyAll();
    			}
    		}
    	}
    	
    	/**
    	 * Block until the processor has transitioned to the given state.
    	 * Return false if the transition failed.
    	 */
    	public boolean waitForState(int state) 
    	{
    		//stateTransitionOK = true;
    		synchronized (waitSync) {
    		    try {
    		    	while (processor.getState() != state && stateTransitionOK) {
    		    		waitSync.wait();
    		    	}
    		    } catch (Exception e) {
    		    	LOG.warn("Exception waiting for state " +state,e);
    		    }
    		}
    		return stateTransitionOK;
    	}

    }

private TrackControl getVideoTrackControl(File mediaFile, Loader loader)
throws MediaAlbumException
{
	Processor processor = loader.getProcessor();
	
	// Put the Processor into configured state.
	processor.configure();
	if (!loader.waitForState(Processor.Configured)) {
	    LOG.error("Failed to configure the processor for video file "
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException(
				"Failed to configure the processor for video file");
	}

	// So I can use it as a player.
	processor.setContentDescriptor(null);

	// Obtain the track controls.
	TrackControl tc[] = processor.getTrackControls();

	if (tc == null) {
	    LOG.error("Failed to obtain track controls from the processor for video file "
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException(
			"Failed to obtain track controls from the processor for video file");
	}

	// Search for the track control for the video track.
	TrackControl videoTrack = null;

	for (int i = 0; i < tc.length; i++) {
	    if (tc[i].getFormat() instanceof VideoFormat) {
	    	videoTrack = (TrackControl)tc[i];
	    	break;
	    }
	}

	if (videoTrack == null) {
	    LOG.error("Media does not contain a video track for video file "
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException(
			"Media does not contain a video track");
	}
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Got video format " +videoTrack.getFormat() 
				+" for file " +mediaFile.getAbsolutePath());
	}
	
	return videoTrack;
}

/**
 * This method assumes the Processor is configured.
 * @return
 */
private TrackControl getAudioTrack(Processor processor, File mediaFile) 
throws MediaAlbumException
{
	// Obtain the track controls.
	TrackControl tc[] = processor.getTrackControls();

	if (tc == null) {
	    LOG.error("Failed to obtain track controls from the processor for video file "
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException(
			"Failed to obtain track controls from the processor for video file");
	}

	// Search for the track control for the audio track.
	TrackControl audioTrack = null;

	for (int i = 0; i < tc.length; i++) {
	    if (tc[i].getFormat() instanceof AudioFormat) {
	    	audioTrack = (TrackControl)tc[i];
	    	break;
	    }
	}

	if (audioTrack == null) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Media does not contain an audio track for video file "
	    		+mediaFile.getAbsolutePath());
		}
		return null;
	}
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Got audio format " +audioTrack.getFormat() 
				+" for file " +mediaFile.getAbsolutePath());
	}
	
	return audioTrack;
}

private TrackControl getVideoTrackControl(File mediaFile, Loader loader, Renderer renderer)
throws MediaAlbumException
{
	Processor processor = loader.getProcessor();
	
	// Put the Processor into configured state.
	processor.configure();
	if (!loader.waitForState(Processor.Configured)) {
	    LOG.error("Failed to configure the processor for video file "
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException(
				"Failed to configure the processor for video file");
	}
	
	// So I can use it as a player.
	processor.setContentDescriptor(null);

	// Obtain the track controls.
	TrackControl tc[] = processor.getTrackControls();

	if (tc == null) {
	    LOG.error("Failed to obtain track controls from the processor for video file "
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException(
			"Failed to obtain track controls from the processor for video file");
	}

	// Search for the track control for the video track.
	TrackControl videoTrack = null;

	for (int i = 0; i < tc.length; i++) {
	    if (tc[i].getFormat() instanceof VideoFormat) {
	    	videoTrack = (TrackControl)tc[i];
	    	break;
	    }
	}

	if (videoTrack == null) {
	    LOG.error("Media does not contain a video track for video file "
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException(
			"Media does not contain a video track");
	}
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Got video format " +videoTrack.getFormat() 
				+" for file " +mediaFile.getAbsolutePath());
	}
	
	try {
		videoTrack.setRenderer(renderer);
	} catch (UnsupportedPlugInException e) {
		throw new MediaAlbumException("Unable to set renderer to video track",e);
	} catch (NotConfiguredError e) {
		LOG.error("Unable to set renderer to video track",e);
		throw new MediaAlbumException("Unable to set renderer to video track");
	}

	return videoTrack;
}

private Processor getProcessor(File mediaFile) throws MediaAlbumException
{
   Processor processor = null;
	try {
		processor = Manager.createProcessor(mediaFile.toURL());
	} catch (Exception e) {
	    LOG.error("Failed to get processor for video file " 
	    		+mediaFile.getAbsolutePath());
		throw new MediaAlbumException("Unable to get processor for video file",
				e);
	}
	return processor;
}
    
/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#setMediaItemParameters(java.io.File, magoffin.matt.ma.xsd.MediaItem)
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item)
throws MediaAlbumException 
{
    Processor processor = getProcessor(mediaFile);
	Loader l = new Loader(processor);
	BasicVideoMetadata meta = new BasicVideoMetadata();
	
	try {

		TrackControl videoTrack = getVideoTrackControl(mediaFile,l);
		
		// get dimensions
		VideoFormat vf = (VideoFormat)videoTrack.getFormat();
		
		Dimension dim = vf.getSize();
		item.setWidth(new Integer((int)dim.getWidth()));
		item.setHeight(new Integer((int)dim.getHeight()));
		
		Time duration = processor.getDuration();
		if (duration != Duration.DURATION_UNKNOWN) {
		    if ( LOG.isDebugEnabled() ) {
		    	LOG.debug("Movie duration " + duration.getSeconds()
		    			+" for " +mediaFile.getAbsolutePath());
		    }
		    long ns = duration.getNanoseconds();
		    long ms = ns / 1000000;
		    meta.setDurationMs(ms);
		    
		    /*
			FramePositioningControl fpc = (FramePositioningControl)
				processor.getControl("javax.media.control.FramePositioningControl");

			if (fpc == null) {
				if ( LOG.isDebugEnabled() ) {
				    LOG.error("The processor does not support FramePositioningControl for "
				    		+mediaFile.getAbsolutePath());
				}
			} else {
			    int totalFrames = fpc.mapTimeToFrame(duration);
			    if (totalFrames != FramePositioningControl.FRAME_UNKNOWN) {
			    	if ( LOG.isDebugEnabled() ) {
			    		LOG.debug("Total # of " +totalFrames +" in video " 
			    				+mediaFile.getAbsolutePath());
			    	}
			    	double fps = (double)totalFrames / (double)( ms / 1000 );
			    	meta.setFps((float)fps);
			    }
			}*/
		}
		
		VideoFormat videoFormat = (VideoFormat)videoTrack.getFormat();
		meta.setVideoFormat(videoFormat.getEncoding());
		meta.setFps(videoFormat.getFrameRate());
		
		TrackControl audioTrack = getAudioTrack(processor,mediaFile);
		if ( audioTrack != null ) {
			AudioFormat audioFormat = (AudioFormat)audioTrack.getFormat();
			StringBuffer buf = new StringBuffer();
			buf.append(audioFormat.getEncoding()).append(", ");
			if ( audioFormat.getSampleRate() != AudioFormat.NOT_SPECIFIED ) {
				buf.append(audioFormat.getSampleRate()).append(" Hz, ");
			}
			if ( audioFormat.getSampleSizeInBits() != AudioFormat.NOT_SPECIFIED ) {
				buf.append(audioFormat.getSampleSizeInBits()).append(" bit, ");
			}
			switch ( audioFormat.getChannels() ) {
				case 1:
					buf.append("mono");
					break;
				case 2:
					buf.append("stereo");
					break;
				default:
					buf.append(audioFormat.getChannels()).append(" channels");
			}
			meta.setAudioFormat(buf.toString());
		}
		
		// see if can process, or will need to use icon
		// Prefetch the player.
		processor.prefetch();
		if (!l.waitForState(Processor.Prefetched)) {
		    LOG.error("Failed to prefetch the player for " 
		    		+mediaFile.getAbsolutePath());
		    // assume will be using icons
		    item.setUseIcon(Boolean.TRUE);
		}


		
	} finally {
		processor.close();
	}
	
	return meta;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#getImageMetadataInstance(magoffin.matt.ma.xsd.MediaItem)
 */
protected ImageMetadata getImageMetadataInstance(MediaItem item) {
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getBufferedImage(magoffin.matt.ma.xsd.MediaItem, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public BufferedImage getBufferedImage(MediaItem item, InputStream in, MediaRequestHandlerParams params)
throws MediaAlbumException {
	
	File mediaFile = (File)params.getParam(MediaRequestHandlerParams.INPUT_FILE);
	
    Processor processor = getProcessor(mediaFile);
	Loader loader = new Loader(processor);
	
	try {

		TrackControl videoTrack = getVideoTrackControl(mediaFile,loader);
		
		// Prefetch the player.
		processor.prefetch();
		if (!loader.waitForState(Processor.Prefetched)) {
		    LOG.error("Failed to prefetch the player for " 
		    		+mediaFile.getAbsolutePath());
		    throw new MediaAlbumException("Unable to prefetch video");
		}

		FrameGrabbingControl fgc = (FrameGrabbingControl)
				processor.getControl("javax.media.control.FrameGrabbingControl" );
		if (fgc == null) {
		    LOG.error("The processor " +processor +" does not support FrameGrabbingControl for "
		    		+mediaFile.getAbsolutePath());
		    throw new MediaAlbumException("Unable to extract frame in video track");
		}
		
		Buffer bufferFrame = fgc.grabFrame();  // Grab the frame and pass it to a Frame buffer
		// convert it to an Image
		BufferToImage bufferToImage = new BufferToImage((VideoFormat)bufferFrame.getFormat());
		Image image = bufferToImage.createImage(bufferFrame);
		return getBufferedImage(image);
		
	} finally {
		processor.close();
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#writeMedia(magoffin.matt.ma.xsd.MediaItem, java.io.OutputStream, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void writeMedia(MediaItem item, OutputStream out, InputStream in,
		MediaRequestHandlerParams params) 
throws MediaAlbumException, IOException 
{
	if ( item.getUseIcon().booleanValue() ) {
		writeFileIcon(item,out,params);
	} else {
		// try to extract frame, but if that fails return file icon
		try {
			super.writeMedia(item, out, in, params);
		} catch ( MediaAlbumException e ) {
			LOG.warn("Exception extracting frame from video, returning icon instead.");
			writeFileIcon(item,out,params);
		}
	}
}
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.video.AbstractVideoMediaRequestHandler#addMediaItemMetadata(magoffin.matt.ma.video.VideoMetadata, magoffin.matt.ma.xsd.MediaItem, java.util.List)
 */
protected void addMediaItemMetadata(VideoMetadata meta, MediaItem item, List list)
{
	// TODO Auto-generated method stub
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.video.AbstractVideoMediaRequestHandler#getVideoMetadataInstance(magoffin.matt.ma.xsd.MediaItem)
 */
protected VideoMetadata getVideoMetadataInstance(MediaItem item) {
	return new BasicVideoMetadata();
}

}
