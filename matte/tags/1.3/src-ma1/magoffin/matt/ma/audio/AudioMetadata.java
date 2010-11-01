/* ===================================================================
 * AudioMetadata.java
 * 
 * Created Jul 18, 2004 3:32:07 PM
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
 * $Id: AudioMetadata.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.audio;

import magoffin.matt.ma.MediaMetadata;

/**
 * Metadata interface for audio.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface AudioMetadata extends MediaMetadata 
{
	public static final String META_ALBUM = "Album";
	public static final String META_ARTIST = "Artist";
	public static final String META_AUDIO_FORMAT = "Audio format";
	public static final String META_BIT_RATE = "Bit rate";
	public static final String META_DISC_NUM = "Disc #";
	public static final String META_DISC_TOTAL = "Total discs";
	public static final String META_DURATION = "Duration";
	public static final String META_GENRE = "Genre";
	public static final String META_SAMPLE_RATE = "Sample rate";
	public static final String META_SONG_NAME = "Song name";
	public static final String META_TRACK_NUM = "Track #";
	public static final String META_TRACK_TOTAL = "Total tracks";
	public static final String META_YEAR = "Year";

/**
 * Get the album name.
 * @return album name
 */
public String getAlbum();

/**
 * Get the artist name.
 * @return artist name
 */
public String getArtist();

/**
 * Get the audio format (MP3, AIFF, etc).
 * 
 * @return the audio format
 */
public String getAudioFormat();

/**
 * Get the bit rate.
 * 
 * @return the bit rate
 */
public String getBitRate();

/**
 * Get the disc number (for multi-disc sets).
 * @return the disc number
 */
public String getDiscNumber();

/**
 * Get total number of disc in multi-disk set.
 * @return total number of discs
 */
public String getDiscTotal();

/**
 * Get the duration as a time code (hh:mm:ss);
 * 
 * @return the time code duration
 */
public String getDurationAsTimeCode();

/**
 * Get the song genre.
 * @return genre
 */
public String getGenre();

/**
 * Get the sample rate (eg. 44 KHz).
 * @return sample rate
 */
public String getSampleRate();

/**
 * Get the song name.
 * @return song name
 */
public String getSongName();

/**
 * Get the album track number.
 * @return the track number
 */
public String getTrackNumber();

/**
 * Get the total number of tracks on album.
 * @return total tracks
 */
public String getTrackTotal();

/**
 * Get the year.
 * @return year
 */
public String getYear();

}
