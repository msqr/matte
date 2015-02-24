/* ===================================================================
 * BasicVideoMetadata.java
 * 
 * Created Jul 16, 2004 12:30:02 PM
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
 * $Id: BasicVideoMetadata.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.video;

import magoffin.matt.ma.util.AbstractMediaMetadata;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.util.ArrayUtil;

import org.apache.log4j.Logger;

/**
 * Metadata object from a video source.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class BasicVideoMetadata extends AbstractMediaMetadata 
implements VideoMetadata 
{
	/** The delimiter used to separate meta values when serialized. */
	public static final char FIELD_DELIM = '|';
	
	/** The audio format (string). */
	public static final char AUDIO_FORMAT = 'a';
	
	/** The duration, in milliseconds (long). */
	public static final char DURATION = 'd';
	
	/** The frames per second (float). */
	public static final char FRAMES_PER_SECOND = 'f';
	
	/** The video format (string). */
	public static final char VIDEO_FORMAT = 'v';
	
	
	private static final Logger LOG = Logger.getLogger(BasicVideoMetadata.class);
	
	/** The audio format. */
	private String audioFormat = null;

	/** The duration, in milliseconds. */
	private long durationMs = -1;
	
	/** The frames per second. */
	private float fps = -1;
	
	/** The video format. */
	private String videoFormat = null;
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaMetadata#serializeToString()
 */
public String serializeToString() 
{
	StringBuffer buf = new StringBuffer();
	if ( audioFormat != null && audioFormat.length() > 0 ) {
		buf.append(FIELD_DELIM).append(AUDIO_FORMAT).append(audioFormat);
	}
	if ( durationMs > 0 ) {
		buf.append(FIELD_DELIM).append(DURATION).append(durationMs);
	}
	if ( fps > 0 ) {
		buf.append(FIELD_DELIM).append(FRAMES_PER_SECOND).append(fps);
	}
	if ( videoFormat != null && videoFormat.length() > 0 ) {
		buf.append(FIELD_DELIM).append(VIDEO_FORMAT).append(videoFormat);
	}
	return buf.length() > 0 ? buf.toString() : null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaMetadata#deserializeFromString(java.lang.String)
 */
public void deserializeFromString(String s) 
{
	if ( s == null || s.length() < 0 ) {
		return;
	}
	String[] fields = ArrayUtil.split(s,FIELD_DELIM,-1);
	for ( int i = 1; i < fields.length; i++ ) { // start at 1 because first will be empty
		char tag = fields[i].charAt(0);
		String val = fields[i].substring(1);
		switch (tag) {
			case AUDIO_FORMAT:
				this.audioFormat = val;
				break;
			case DURATION:
				try {
					this.durationMs = Long.parseLong(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse duration from value: " +val);
					}
				}
				break;
			case FRAMES_PER_SECOND:
				try {
					this.fps = Float.parseFloat(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse frames per second from value: " +val);
					}
				}
				break;
			case VIDEO_FORMAT:
				this.videoFormat = val;
				break;
		}
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.video.VideoMetadata#getAudioFormat()
 */
public String getAudioFormat() {
	return audioFormat;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.video.VideoMetadata#getDurationAsTimeCode()
 */
public String getDurationAsTimeCode() {
	return MediaUtil.getTimeCodeFromMilliseconds(durationMs);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.video.VideoMetadata#getFramesPerSecond()
 */
public String getFramesPerSecond() {
	if ( fps < 1 ) { 
		return null;
	}
	return String.valueOf(fps);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.video.VideoMetadata#getVideoFormat()
 */
public String getVideoFormat() {
	return videoFormat;
}

/* ---------------------------------------------
 * Getters/Setters
 * --------------------------------------------- */

/**
 * @return Returns the durationMs.
 */
public long getDurationMs() {
	return durationMs;
}
/**
 * @param durationMs The durationMs to set.
 */
public void setDurationMs(long durationMs) {
	this.durationMs = durationMs;
}
/**
 * @return Returns the fps.
 */
public float getFps() {
	return fps;
}
/**
 * @param fps The fps to set.
 */
public void setFps(float fps) {
	this.fps = fps;
}
/**
 * @param audioFormat The audioFormat to set.
 */
public void setAudioFormat(String audioFormat) {
	this.audioFormat = audioFormat;
}
/**
 * @param videoFormat The videoFormat to set.
 */
public void setVideoFormat(String videoFormat) {
	this.videoFormat = videoFormat;
}
}
