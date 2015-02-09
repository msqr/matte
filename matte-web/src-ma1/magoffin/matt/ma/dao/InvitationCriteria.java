/* ===================================================================
 * InvitationCriteria.java
 * 
 * Created Jan 21, 2004 3:13:38 PM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: InvitationCriteria.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria for Invitation objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public interface InvitationCriteria extends AbstractCriteria 
{
	/** 
	 * Search type for finding a pending invitation. 
	 * 
	 * <p>Specify the inviting user ID and invitee's email as an Object[]
	 * with {@link #setQuery(Object)}.</p>
	 */
	public static final int PENDING_INVITATION_SEARCH = 1;
	
}
