/* ===================================================================
 * UserCriteria.java
 * 
 * Created Dec 2, 2003 7:12:06 PM
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
 * $Id: UserCriteria.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria object for User DAO.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public interface UserCriteria extends AbstractCriteria 
{
	/** 
	 * Search for a username.
	 * 
	 * <p>Specify the username to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int USERNAME_SEARCH = 1;
	
	/** 
	 * Search type for finding all friends of a specific user. 
	 * 
	 * <p>Specify the user ID with {@link #setQuery(Object)}.</p>
	 */
	public static final int FRIENDS_OF_USER_SEARCH = 2;
	
	/**
	 * Search type for finding all users within a specific group.
	 * 
	 * <p>Specify the group ID with {@link #setQuery(Object)}.</p>
	 */
	public static final int USERS_IN_GROUP_SEARCH = 3;
	
	/**
	 * Search for a user by anonymous key.
	 * 
	 * <p>Specify the anonymous key with {@link #setQuery(Object)}.</p>
	 */
	public static final int ANONYMOUS_KEY_SEARCH = 4;
	
	/**
	 * Search for a user by their email, matching substrings.
	 * 
	 * <p>Specify the email with {@link #setQuery(Object)}.</p>
	 */
	public static final int EMAIL_SEARCH = 5;
	
	/**
	 * Search for a user by their name, matching substrings.
	 * 
	 * <p>Specify the name with {@link #setQuery(Object)}.</p>
	 */
	public static final int NAME_SEARCH = 6;
	
	/**
	 * Search for a user by their username, matching substrings.
	 * 
	 * <p>Specify the username with {@link #setQuery(Object)}.</p>
	 */
	public static final int USERNAME_SUB_SEARCH = 7;
	
	/**
	 * Search for a user by their email.
	 * 
	 * <p>Specify the email with {@link #setQuery(Object)}.</p>
	 */
	public static final int EMAIL_SEARCH_EXACT = 8;
	
	/**
	 * Search type for returning the count of users using a particular
	 * theme.
	 */
	public static final int USERS_USING_THEME_COUNT = 9;
	
	/**
	 * Search type for returning the count of users using the default theme.
	 */
	public static final int USERS_USING_DEFAULT_THEME_COUNT = 10;
	
}
