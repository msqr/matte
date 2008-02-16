/* ===================================================================
 * EmailOptions.java
 * 
 * Created Apr 16, 2004 1:42:21 PM
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
 * $Id: EmailOptions.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.io.Serializable;

/**
 * Utility object for email options.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class EmailOptions implements Serializable
{
	private String to;
	private String from;
	private String subject;
	private String body;
	
/**
 * @return Returns the body.
 */
public String getBody()
{
	return body;
}
/**
 * @param body The body to set.
 */
public void setBody(String body)
{
	this.body = body;
}
/**
 * @return Returns the from.
 */
public String getFrom()
{
	return from;
}
/**
 * @param from The from to set.
 */
public void setFrom(String from)
{
	this.from = from;
}
/**
 * @return Returns the subject.
 */
public String getSubject()
{
	return subject;
}
/**
 * @param subject The subject to set.
 */
public void setSubject(String subject)
{
	this.subject = subject;
}
/**
 * @return Returns the to.
 */
public String getTo()
{
	return to;
}
/**
 * @param to The to to set.
 */
public void setTo(String to)
{
	this.to = to;
}

}
