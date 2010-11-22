/* ===================================================================
 * TimezoneOptionsTag.java
 * 
 * Created Jun 29, 2004 10:09:56 AM
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
 * $Id: TimezoneOptionsTag.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.struts;

import java.io.IOException;
import java.util.TimeZone;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import magoffin.matt.ma.util.MediaUtil;

import org.apache.struts.taglib.html.SelectTag;
import org.apache.struts.util.RequestUtils;

/**
 * JSP tag to render HTML &lt;option&gt; elements for time zones.
 * 
 * <p>This tag must be nested inside a Struts &lt;html:select&gt; tag.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class TimezoneOptionsTag extends TagSupport 
{

/* (non-Javadoc)
 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
 */
public int doStartTag() throws JspException 
{
	SelectTag sTag = null;
	Tag parentTag = findAncestorWithClass(this,SelectTag.class);
	
	if ( parentTag == null ) {
		throw new JspException("TimezoneOptionsTag must be nested inside a Struts SelectTag");
	}
	
	sTag = (SelectTag)parentTag;
	
	String selectedTimezone = (String)RequestUtils.lookup(pageContext, 
			sTag.getName(), sTag.getProperty(), null);
	
	if ( selectedTimezone == null ) {
		selectedTimezone = TimeZone.getDefault().getID();
	}
	
	JspWriter out = pageContext.getOut();
	
	try {
		for ( int currOffset = -12; currOffset < 15; currOffset++ ) {
			String[] ids = TimeZone.getAvailableIDs(currOffset*MediaUtil.MS_PER_HOUR);
			
			String value = null;
			String text = null;
			
			if ( currOffset == 0 ) {
				text = value = "GMT";
			} else {
				value = "GMT"+(currOffset<0?"+":"")+(0-currOffset);
				text = "GMT " +currOffset;
			}
			
			outputOption(out,value,text,selectedTimezone,null);
			
			for ( int i = 0; i < ids.length; i++ ) {
				if ( ids[i].startsWith("Etc/") ) continue;
				outputOption(out,ids[i],ids[i],selectedTimezone,
						"margin-left: 1em;");
			}
		}
	} catch ( IOException e ) {
		throw new JspException("Unable to generate time zone options",e);
	}

	return EVAL_PAGE;
}

private void outputOption(JspWriter out, String value, String text, String selected,
		String style) 
throws IOException 
{
	out.print("<option value=\""+value+"\"");
	if ( value.equals(selected) ) {
		out.print(" selected=\"selected\"");
	}
	if ( style != null ) {
		out.print(" style=\"");
		out.print(style);
		out.print("\"");
	}
	out.print(">");
	out.print(text);
	out.print("</option>");
}

}
