/* ===================================================================
 * EmailItemForm.java
 * 
 * Created Apr 16, 2004 11:36:48 AM
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
 * $Id: EmailItemForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionMapping;

/**
 * Form bean for emailing media items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class EmailItemForm extends BaseBounceBackForm
{
	// email values
	private String body;
	private String from;
	private String subject;
	private String to;
	private boolean attach;
	
	// display logic
	private boolean multi;
	
	// acting user (null if anonymous)
	private User user;
	
	// calculated attachment size
	private String attachmentSize;
	
	// set to true if attachment size is large and in charge
	private boolean largeAttachment;
	
/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request)
{
	super.reset(mapping,request);
	multi = false;
	attach = false;
	largeAttachment = false;
}

/**
 * @return Returns the attachmentSize.
 */
public String getAttachmentSize() {
	return attachmentSize;
}
/**
 * @param attachmentSize The attachmentSize to set.
 */
public void setAttachmentSize(String attachmentSize) {
	this.attachmentSize = attachmentSize;
}
/**
 * @return Returns the largeAttachment.
 */
public boolean isLargeAttachment() {
	return largeAttachment;
}
/**
 * @param largeAttachment The largeAttachment to set.
 */
public void setLargeAttachment(boolean largeAttachment) {
	this.largeAttachment = largeAttachment;
}
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
 * @return Returns the multi.
 */
public boolean isMulti()
{
	return multi;
}
/**
 * @param multi The multi to set.
 */
public void setMulti(boolean multi)
{
	this.multi = multi;
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

/**
 * @return Returns the user.
 */
public User getUser()
{
	return user;
}
/**
 * @param user The user to set.
 */
public void setUser(User user)
{
	this.user = user;
}

/**
 * @return Returns the attach.
 */
public boolean isAttach()
{
	return attach;
}
/**
 * @param attach The attach to set.
 */
public void setAttach(boolean attach)
{
	this.attach = attach;
}

}
