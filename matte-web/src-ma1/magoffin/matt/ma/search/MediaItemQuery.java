/* ===================================================================
 * MediaItemSearchQuery.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 29, 2004 10:53:36 AM.
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
 * $Id: MediaItemQuery.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.search;

import java.io.Serializable;

import magoffin.matt.util.ArrayUtil;
import magoffin.matt.util.StringUtil;

/**
 * Class for a Media Item search query.
 * 
 * <p>This class is not search implementation specific. The {@link #toString()} 
 * method generates a pseudo search query string to demonstrate how 
 * the fields of this class can be used.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaItemQuery implements Serializable 
{
	private String simple;
	private String name;
	private String text;
	private String keyword;
	private String category;
	private Integer rating;
	private char necessity = SearchConstants.UNSPECIFIED;

/**
 * @return Returns the category.
 */
public String getCategory() {
	return category;
}

/**
 * @param category The category to set.
 */
public void setCategory(String category) {
	if ( category != null && category.length() > 0 ) {
		this.category = category;
	} else {
		this.category = null;
	}
}

/**
 * @return Returns the keyword.
 */
public String getKeyword() {
	return keyword;
}

/**
 * @param keyword The keyword to set.
 */
public void setKeyword(String keyword) {
	if ( keyword != null && keyword.length() > 0 ) {
		this.keyword = keyword;
	} else {
		this.keyword = null;
	}
}

/**
 * @return Returns the rating.
 */
public Integer getRating() {
	return rating;
}

/**
 * @param rating The rating to set.
 */
public void setRating(Integer rating) {
	this.rating = rating;
}

/**
 * @return Returns the text.
 */
public String getText() {
	return text;
}

/**
 * @param text The text to set.
 */
public void setText(String text) {
	if ( text != null && text.length() > 0 ) {
		this.text = text;
	} else {
		this.text = null;
	}
}

public void appendToString(StringBuffer buf) {
	StringBuffer myBuf = new StringBuffer();
	if ( name != null ) {
		// add quotes if contains a space
		buf.append("name:");
		String txt = StringUtil.normalizeWhitespace(name);
		int space = txt.indexOf(' ');
		if ( space > 0 ) {
			myBuf.append('"').append(txt).append('"');
		} else {
			myBuf.append(txt);
		}
	}
	if ( text != null ) {
		// add quotes if contains a space
		String txt = StringUtil.normalizeWhitespace(text);
		int space = txt.indexOf(' ');
		if ( space > 0 ) {
			myBuf.append('"').append(txt).append('"');
		} else {
			myBuf.append(txt);
		}
	}
	appendMultiField("keyword",keyword,' ', myBuf);
	appendMultiField("category",category,' ',myBuf);
	
	switch ( necessity ) {
		case SearchConstants.NECESSARY:
		case SearchConstants.PROHIBITED:
			addNecessity(necessity,myBuf);
			break;
	}
	buf.append(myBuf);
}

private void addNecessity(char necessity, StringBuffer buf)
{
	if ( buf.length() < 1 ) return;
	int space = buf.indexOf(" ");
	if ( space > 0 ) {
		buf.insert(0,'(');
		buf.insert(0,necessity);
		buf.append(')');
	} else {
		buf.insert(0,necessity);
	}
}

private void appendMultiField(String field, String txt, char delimiter, StringBuffer buf)
{
	if ( txt == null ) return;
	
	if ( buf.length() > 0 ) {
		buf.append(" ").append(SearchConstants.AND).append(" ");
	}
	buf.append(field).append(':');
	txt = StringUtil.normalizeWhitespace(txt);
	int space = txt.indexOf(' ');
	if ( space > 0 ) {
		String[] words = ArrayUtil.split(txt,delimiter,-1);
		buf.append("(");
		for ( int i = 0; i < words.length; i++ ) {
			if ( i > 0 ) {
				buf.append(" ").append(SearchConstants.OR).append(" ");
			}
			buf.append(words[i]);
		}
		buf.append(")");
	} else {
		buf.append(txt);
	}
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
	StringBuffer buf = new StringBuffer();
	appendToString(buf);
	return buf.toString();
}

/**
 * @return Returns the necessity.
 */
public char getNecessity() {
	return necessity;
}

/**
 * @param necessity The necessity to set.
 */
public void setNecessity(char necessity) {
	this.necessity = necessity;
}

/**
 * @return Returns the name.
 */
public String getName() {
	return name;
}

/**
 * @param name The name to set.
 */
public void setName(String name) {
	if ( name != null && name.length() > 0 ) {
		this.name = name;
	} else {
		this.name = null;
	}
}

/**
 * @return Returns the simple.
 */
public String getSimple() {
	return simple;
}

/**
 * @param simple The simple to set.
 */
public void setSimple(String simple) {
	if ( simple != null && simple.length() > 0 ) {
		this.simple = simple;
	} else {
		this.simple = null;
	}
}

}
