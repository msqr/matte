/* ===================================================================
 * BasicAudioMetadata.java
 * 
 * Created Jul 18, 2004 4:02:01 PM
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
 * $Id: BasicAudioMetadata.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.audio;

import magoffin.matt.ma.util.AbstractMediaMetadata;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.util.ArrayUtil;

import org.apache.log4j.Logger;

/**
 * Metadata object from an audio source.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class BasicAudioMetadata extends AbstractMediaMetadata 
implements AudioMetadata 
{
	/** The delimiter used to separate meta values when serialized. */
	public static final char FIELD_DELIM = '|';
	
	/** The album name (string). */
	public static final char ALBUM = 'a';
	
	/** The artist name (string). */
	public static final char ARTIST = 'A';
	
	/** The audio format (string). */
	public static final char AUDIO_FORMAT = 'f';
	
	/** The bitrate (String). */
	public static final char BIT_RATE = 'b';
	
	/** The disc number (integer). */
	public static final char DISC_NUM = 'd';
	
	/** The disc total (integer). */
	public static final char DISC_TOTAL = 'D';
	
	/** The duration, in milliseconds (long). */
	public static final char DURATION = 'l';
	
	/** The genre (String). */
	public static final char GENRE = 'g';
	
	/** The sample rate (String). */
	public static final char SAMPLE_RATE = 's';
	
	/** The song name (string). */
	public static final char SONG_NAME = 'n';
	
	/** The track number (integer). */
	public static final char TRACK_NUM = 't';
	
	/** The track total (integer). */
	public static final char TRACK_TOTAL = 'T';
	
	/** The year (number). */
	public static final char YEAR = 'y';
	
	private static final Logger LOG = Logger.getLogger(BasicAudioMetadata.class);
	
	private String album = null;
	private String artist = null;
	private String audioFormat = null;
	private String bitRate = null;
	private int discNum = -1;
	private int numberOfDiscs = -1;
	private long durationMs = -1;
	private String genre;
	private String sampleRate;
	private String songName;
	private int trackNum;
	private int numberOfTracks;
	private int songYear;

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaMetadata#serializeToString()
 */
public String serializeToString() 
{
	StringBuffer buf = new StringBuffer();
	if ( album != null && album.length() > 0 ) {
		buf.append(FIELD_DELIM).append(ALBUM).append(album);
	}
	if ( artist != null && artist.length() > 0 ) {
		buf.append(FIELD_DELIM).append(ARTIST).append(artist);
	}
	if ( audioFormat != null && audioFormat.length() > 0 ) {
		buf.append(FIELD_DELIM).append(AUDIO_FORMAT).append(audioFormat);
	}
	if ( bitRate != null && bitRate.length() > 0 ) {
		buf.append(FIELD_DELIM).append(BIT_RATE).append(bitRate);
	}
	if ( discNum > 0 ) {
		buf.append(FIELD_DELIM).append(DISC_NUM).append(discNum);
	}
	if ( numberOfDiscs > 0 ) {
		buf.append(FIELD_DELIM).append(DISC_TOTAL).append(numberOfDiscs);
	}
	if ( durationMs > 0 ) {
		buf.append(FIELD_DELIM).append(DURATION).append(durationMs);
	}
	if ( genre != null && genre.length() > 0 ) {
		buf.append(FIELD_DELIM).append(GENRE).append(genre);
	}
	if ( sampleRate != null && sampleRate.length() > 0 ) {
		buf.append(FIELD_DELIM).append(SAMPLE_RATE).append(sampleRate);
	}
	if ( songName != null && songName.length() > 0 ) {
		buf.append(FIELD_DELIM).append(SONG_NAME).append(songName);
	}
	if ( trackNum > 0 ) {
		buf.append(FIELD_DELIM).append(TRACK_NUM).append(trackNum);
	}
	if ( numberOfTracks > 0 ) {
		buf.append(FIELD_DELIM).append(TRACK_TOTAL).append(numberOfTracks);
	}
	if ( songYear > 0 ) {
		buf.append(FIELD_DELIM).append(YEAR).append(songYear);
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
			case ALBUM:
				this.album = val;
				break;
			case ARTIST:
				this.artist = val;
				break;
			case AUDIO_FORMAT:
				this.audioFormat = val;
				break;
			case BIT_RATE:
				this.bitRate = val;
				break;
			case DISC_NUM:
				try {
					this.discNum = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse disc num from value: " +val);
					}
				}
				break;
			case DISC_TOTAL:
				try {
					this.numberOfDiscs = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse disc total from value: " +val);
					}
				}
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
			case GENRE:
				this.genre = val;
				break;
			case SAMPLE_RATE:
				this.sampleRate = val;
				break;
			case SONG_NAME:
				this.songName = val;
				break;
			case TRACK_NUM:
				try {
					this.trackNum = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse track num from value: " +val);
					}
				}
				break;
			case TRACK_TOTAL:
				try {
					this.numberOfTracks = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse track total from value: " +val);
					}
				}
				break;
			case YEAR:
				try {
					this.songYear = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse year from value: " +val);
					}
				}
				break;
		}
	}
}

/**
 * @return Returns the discNum.
 */
public int getDiscNum() {
	return discNum;
}
/**
 * @param discNum The discNum to set.
 */
public void setDiscNum(int discNum) {
	this.discNum = discNum;
}
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
 * @return Returns the numberOfDiscs.
 */
public int getNumberOfDiscs() {
	return numberOfDiscs;
}
/**
 * @param numberOfDiscs The numberOfDiscs to set.
 */
public void setNumberOfDiscs(int numberOfDiscs) {
	this.numberOfDiscs = numberOfDiscs;
}
/**
 * @return Returns the numberOfTracks.
 */
public int getNumberOfTracks() {
	return numberOfTracks;
}

/**
 * @param numberOfTracks The numberOfTracks to set.
 */
public void setNumberOfTracks(int numberOfTracks) {
	this.numberOfTracks = numberOfTracks;
}
/**
 * @return Returns the songYear.
 */
public int getSongYear() {
	return songYear;
}
/**
 * @param songYear The songYear to set.
 */
public void setSongYear(int songYear) {
	this.songYear = songYear;
}
/**
 * @return Returns the trackNum.
 */
public int getTrackNum() {
	return trackNum;
}
/**
 * @param trackNum The trackNum to set.
 */
public void setTrackNum(int trackNum) {
	this.trackNum = trackNum;
}
/**
 * @param album The album to set.
 */
public void setAlbum(String album) {
	this.album = album;
}
/**
 * @param artist The artist to set.
 */
public void setArtist(String artist) {
	this.artist = artist;
}
/**
 * @param audioFormat The audioFormat to set.
 */
public void setAudioFormat(String audioFormat) {
	this.audioFormat = audioFormat;
}
/**
 * @param bitRate The bitRate to set.
 */
public void setBitRate(String bitRate) {
	this.bitRate = bitRate;
}
/**
 * @param genre The genre to set.
 */
public void setGenre(String genre) {
	this.genre = genre;
}
/**
 * @param sampleRate The sampleRate to set.
 */
public void setSampleRate(String sampleRate) {
	this.sampleRate = sampleRate;
}
/**
 * @param songName The songName to set.
 */
public void setSongName(String songName) {
	this.songName = songName;
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getAlbum()
 */
public String getAlbum() {
	return album;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getArtist()
 */
public String getArtist() {
	return artist;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getAudioFormat()
 */
public String getAudioFormat() {
	return audioFormat;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getBitRate()
 */
public String getBitRate() {
	return bitRate;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getDiscNumber()
 */
public String getDiscNumber() {
	if ( discNum > 0 ) {
		return String.valueOf(discNum);
	}
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getDiscTotal()
 */
public String getDiscTotal() {
	if ( numberOfDiscs > 0 ) {
		return String.valueOf(numberOfDiscs);
	}
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getDurationAsTimeCode()
 */
public String getDurationAsTimeCode() {
	return MediaUtil.getTimeCodeFromMilliseconds(durationMs);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getGenre()
 */
public String getGenre() {
	return genre;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getSampleRate()
 */
public String getSampleRate() {
	return sampleRate;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getSongName()
 */
public String getSongName() {
	return songName;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getTrackNumber()
 */
public String getTrackNumber() {
	if ( trackNum > 0 ) {
		return String.valueOf(trackNum);
	}
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getTrackTotal()
 */
public String getTrackTotal() {
	if ( numberOfTracks > 0 ) {
		return String.valueOf(numberOfTracks);
	}
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AudioMetadata#getYear()
 */
public String getYear() {
	if ( songYear > 0 ) {
		return String.valueOf(songYear);
	}
	return null;
}

}
