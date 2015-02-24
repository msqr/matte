/* ===================================================================
 * AlbumAnonKeyGenerationMode.java
 * 
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: AbstractAnonKeyGenerationMode.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.gerdal.mode.gen.AbstractGenerationMode;
import magoffin.matt.util.MessageDigester;

/**
 * Base helper class for anonymous key generation mode.
 * 
 * <p>Created Sep 9, 2003</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public abstract class AbstractAnonKeyGenerationMode extends AbstractGenerationMode {

/**
 * Default constructor.
 */
public AbstractAnonKeyGenerationMode() {
	super();
}


/**
 * Generate a digest key from a string with optional salt.
 * 
 * <p>Created: Sep 9, 2003</p>
 * 
 * @param src the string to digest
 * @param salt the salt (optional)
 * @return digested string
 */
protected String generateKey(String src, byte[] salt) {
	String key = 	key = MessageDigester.generateDigest(src,salt);
	int idx = key.indexOf('}');
	if ( idx >= 0 ) {
		idx += 1;
	} else {
		idx = 0;
	}
	
	int endIdx = key.length() - 1;
	
	// remove any url-unfriendly characters
	char[] chars = key.toCharArray();
	
	if ( chars[endIdx] == '=' ) {
		// ignore the trailing =
		endIdx--;
	}
	
	StringBuffer buf = new StringBuffer();
	for ( int i = idx; i <= endIdx; i++ ) {
		switch ( chars[i] ) {
			case '+':
				buf.append("Plu");
				break;
			case '&':
				buf.append("Amp");
				break;
			case '=':
				buf.append("Equ");
				break;
			case '?':
				buf.append("Que");
				break;
			case '%':
				buf.append("Per");
				break;
			case ' ':
				buf.append("Spa");
				break;
			case '\\':
				buf.append("Sla");
				break;
			
			default:
				buf.append(chars[i]); 
		}
	}
	return buf.toString();
}

}
