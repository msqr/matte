/* ===================================================================
 * UserTagTokenizer.java
 * 
 * Created Mar 5, 2007 5:26:16 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 */

package magoffin.matt.ma2.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

/**
 * Lucene tokenizer for UserTag values.
 * 
 * <p>Splits on whitespace and commas, and normalizes to lower case.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class UserTagTokenizer extends CharTokenizer {

	/**
	 * Construct from a reader.
	 * @param reader the reader
	 */
	public UserTagTokenizer(Reader reader) {
		super(reader);
	}

	@Override
	protected boolean isTokenChar(char c) {
		return !Character.isWhitespace(c) && c != ',';
	}

	@Override
	protected char normalize(char c) {
		return Character.toLowerCase(c);
	}

}
