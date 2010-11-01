/* ===================================================================
 * NestedAlbumOptionsTag.java
 *
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: NestedAlbumOptionsTag.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.jsp;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import magoffin.matt.ma.xsd.Album;

/**
 * JSP tag to render a list of HTML options from an array of Album objects.
 * 
 * <p> Created on Dec 6, 2002 2:56:57 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class NestedAlbumOptionsTag extends TagSupport 
{
	//private static final String NEST_PREFIX = "&nbsp;&nbsp;";
	private static final float NEST_INDENT = 0.75f;
	
	/** The name of the attribute holding the array of albums. */
	private String albumCollectionName = null;
	
/**
 * Returns the name of the album collection attribute.
 * @return the attribute name
 */
public String getCollection() {
	return albumCollectionName;
}

/**
 * Sets the name of the album collection attribute.
 * @param albumCollectionName The attribute name
 */
public void setCollection(String albumCollectionName) {
	this.albumCollectionName = albumCollectionName;
}

/* (non-Javadoc)
 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
 */
public int doStartTag() throws JspException 
{
	Object o = this.pageContext.findAttribute(albumCollectionName);
	if ( !(o instanceof Album[]) ) {
		throw new JspException("MediaAblum array not found at attribute key "
			+albumCollectionName);
	}
	Album[] albums = (Album[])o;
	JspWriter out = this.pageContext.getOut();
	try {
		handleAlbumsOutput(albums,out,0);
	} catch (IOException e) {
		throw new JspException(e);
	}
	return EVAL_PAGE;
}

/**
 * Recursive method to generate the nested option tags.
 * 
 * @param albums the albums
 * @param out the output writer
 * @param level the nested level
 * @throws IOException if an error occurs
 */
private void handleAlbumsOutput(Album[] albums, JspWriter out, int level) 
throws IOException 
{
	for ( int i = 0; i < albums.length; i++ ) {
		out.print("<option value=\"");
		out.print(albums[i].getAlbumId());
		out.print("\"");
		if ( level > 0 ) {
			out.print(" style=\"position: relative; left: ");
			out.print(NEST_INDENT * level);
			out.print("em;\"");
		}
		out.print(">");

		out.print(albums[i].getName());
		out.println("</option>");
		if ( albums[i].getAlbumCount() > 0 ) {
			handleAlbumsOutput(albums[i].getAlbum(),out,level+1);
		}
	}
}

} // class NestedAlbumOptionsTag
