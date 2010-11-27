/* ===================================================================
 * ID3AudioMetadata.java
 * 
 * Created Jul 19, 2004 11:37:44 AM
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
 * $Id: ID3AudioMetadata.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.audio;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import magoffin.matt.util.ArrayUtil;

import org.apache.log4j.Logger;
import org.farng.mp3.MP3File;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

/**
 * Metadata for ID3.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class ID3AudioMetadata extends BasicAudioMetadata 
{
	public static final byte LAYER_III = 1;
	public static final byte LAYER_II = 2;
	public static final byte LAYER_I = 3;
	
	public static final byte VERSION_MPEG1 = 1;
	public static final byte VERSION_MPEG2 = 0;
	
	private static final Logger LOG = Logger.getLogger(ID3AudioMetadata.class);
	
/**
 * Default constructor.
 */
public ID3AudioMetadata() {
	// nothing
}

/**
 * Construct and parse metadata from a file.
 * @param mediaFile the file to parse ID3 data from
 */
public ID3AudioMetadata(File mediaFile)
{
	try{
		parse(mediaFile);
	} catch ( IOException e ) {
		LOG.warn("IOException trying to parse ID3 from " 
				+mediaFile.getAbsolutePath(),e);
	}
}

/**
 * Parse an MPEG audio file for ID3 data.
 * @param mediaFile the file to parse
 * @throws IOException if an error occurs
 */
public void parse(File mediaFile) throws IOException {
	try {
		MP3File mp3 = new MP3File(mediaFile);
		
		if ( mp3.hasID3v1Tag() ) {
			// process v1 fields
			ID3v1 id3 = mp3.getID3v1Tag();
			setAlbum(id3.getAlbum());
			setArtist(id3.getArtist());
			setSongName(id3.getTitle());
		}
		if ( mp3.hasID3v2Tag() ) {
			// process v2 fields
			AbstractID3v2 id3 = mp3.getID3v2Tag();
			
			
			for ( Iterator itr = id3.iterator(); itr.hasNext(); ) {
				Object o = itr.next();
				System.out.println(o);
			}
			
			// album
			if ( id3.hasFrame("TALB") ) {
				setAlbum(id3.getFrame("TALB").getBody().getBriefDescription());
			}
			
			// artist
			if ( id3.hasFrame("TPE1") ) {
				setArtist(id3.getFrame("TPE1").getBody().getBriefDescription());
			}
			
			// song name
			if ( id3.hasFrame("TIT2") ) {
				setSongName(id3.getFrame("TIT2").getBody().getBriefDescription());
			}
			
			// genre
			if ( id3.hasFrame("TCON") ) {
				setGenre(id3.getFrame("TCON").getBody().getBriefDescription());
			}
			
			// track #
			if ( id3.hasFrame("TRCK") ) {
				String y = id3.getFrame("TRCK").getBody().getBriefDescription();
				String[] tracks = ArrayUtil.split(y,'/',2);
				try {
					setTrackNum(Integer.parseInt(tracks[0]));
					if ( tracks.length == 2 ) {
						setNumberOfTracks(Integer.parseInt(tracks[1]));
					}
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse ID3v2 track # from file " 
								+mediaFile.getAbsolutePath() +": " +y);
					}
				}
			}
			
			
			// year
			if ( id3.hasFrame("TDRC") ) {
				String y = id3.getFrame("TDRC").getBody().getBriefDescription();
				try {
					setSongYear(Integer.parseInt(y));
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse ID3v2 year from file " 
								+mediaFile.getAbsolutePath() +": " +y);
					}
				}
			}
		}
		
		mp3.seekMP3Frame();
		
		// bit rate
		int bitRate = mp3.getBitRate();
		if ( mp3.isVariableBitRate() ) {
			setBitRate(bitRate +" kbps (VBR)");
		} else if ( bitRate > 0 ) {
			setBitRate(bitRate +" kbps");
		}
		
		if ( mp3.getFrequency() > 0 ) {
			setSampleRate(mp3.getFrequency() +" kHz");
		}
		
		// format
		byte version = (byte) (mp3.getMpegVersion() & 0x1); // get around bug in lib?
		byte layer = mp3.getLayer();
		
		StringBuffer buf = new StringBuffer();
		buf.append("MPEG");
		switch ( version ) {
			case VERSION_MPEG1:
				buf.append("-1");
				break;
			case VERSION_MPEG2:
				buf.append("-2");
				break;	
		}
		switch (layer) {
			case LAYER_I:
				buf.append(", Layer 1");
				break;
			case LAYER_II:
				buf.append(", Layer 2");
				break;
			case LAYER_III:
				buf.append(", Layer 3");
				break;
		}
		
		setAudioFormat(buf.toString());
		
	} catch ( Exception e ) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Unable to parse MP3 meta: ",e);
		}
	}
}

}
