/* ===================================================================
 * VideoMetadata.java
 * 
 * Created Jul 16, 2004 12:30:37 PM
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
 * $Id: VideoMetadata.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.video;

import magoffin.matt.ma.MediaMetadata;

/**
 * Metadata interface for videos.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface VideoMetadata extends MediaMetadata {

	public static final String META_DURATION = "Duration";
	public static final String META_FRAMES_PER_SECOND = "FPS";
	public static final String META_VIDEO_FORMAT = "Video format";
	public static final String META_AUDIO_FORMAT = "Audio format";

/**
 * Get the duration as a time code (hh:mm:ss);
 * 
 * @return the time code duration
 */
public String getDurationAsTimeCode();

/**
 * Get the frames/second value.
 * 
 * @return frames per second
 */
public String getFramesPerSecond();

/**
 * Get the video format.
 * 
 * @return the video format
 */
public String getVideoFormat();

/**
 * Get the audio format.
 * 
 * @return the audio format
 */
public String getAudioFormat();
	
}
