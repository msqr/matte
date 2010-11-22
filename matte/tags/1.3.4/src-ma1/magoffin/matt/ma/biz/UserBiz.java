/* ===================================================================
 * UserBizIntf.java
 * 
 * Created Nov 30, 2003.
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
 * $Id: UserBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import java.net.URL;
import java.util.Comparator;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.Permissions;
import magoffin.matt.ma.xsd.Registration;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.ma.xsd.UserSearchData;
import magoffin.matt.ma.xsd.UserSearchResults;
import magoffin.matt.util.config.Config;

/**
 * Biz interface for Media Album user maintenance actions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface UserBiz extends Biz {
	
	/** The URL parameter name used for invitation/registration keys: <code>key</code> */
	public static final String KEY_URL_PARAM = "key";
	
	/**
	 * The error message key for when a user is not found: <code>user.not.found</code>
	 */
	public static final String ERROR_USER_NOT_FOUND = "user.not.found";
	
	/**
	 * The error message key for when a user is not allowed to search other users:
	 * <code>user.search.forbidden</code>
	 */
	public static final String ERROR_AUTH_SEARCH_USERS = "user.search.forbidden";
	
	/**
	 * The error message key for when an unknown search type is specified:
	 * <code>user.search.unknown</code>
	 */
	public static final String ERROR_UNKNOWN_SEARCH_TYPE = "user.search.unknown";
	
	/**
	 * The error message key for when a user is not allowed to create other users:
	 * <code>user.create.forbidden</code>
	 */
	public static final String ERROR_AUTH_CREATE_USER = "user.create.forbidden";
	
	/**
	 * The error message key for when a user is not allowed to delete a user:
	 * <code>user.delete.forbidden</code>
	 */
	public static final String ERROR_AUTH_DELETE_USER = "user.delete.forbidden";
	
	/**
	 * The error message key for when a user is not allowed to update a user:
	 * <code>user.update.forbidden</code>
	 */
	public static final String ERROR_AUTH_UPDATE_USER = "user.update.forbidden";
	
	/**
	 * The error message key for when a user is not allowed to delete a group:
	 * <code>group.delete.forbidden</code>
	 */
	public static final String ERROR_AUTH_DELETE_GROUP = "group.delete.forbidden";
	
	/**
	 * The error message key for when a user is not allowed to update free data:
	 * <code>fdata.update.forbidden</code>
	 */
	public static final String ERROR_AUTH_UPDATE_FREE_DATA = "fdata.update.forbidden";
	
	/**
	 * The error message key for when a user is not allowed to update a group:
	 * <code>group.update.forbidden</code>
	 */
	public static final String ERROR_AUTH_UPDATE_GROUP = "group.update.forbidden";
	
	/** 
	 * The error message key when creating a new user and the username is taken 
	 * already: <code>user.create.username.taken</code>
	 */
	public static final String ERROR_USERNAME_TAKEN = "user.create.username.taken";
	
	/** 
	 * The error message key when creating a new user and the email is taken 
	 * already: <code>user.create.email.taken</code>
	 */
	public static final String ERROR_EMAIL_TAKEN = "user.create.email.taken";
	
	/**
	 * The erorr message key when inviting a user to be a friend that is already
	 * a friend: <code>invite.friend.already.friend</code>
	 */
	public static final String ERROR_USER_ALREADY_FRIEND = "invite.friend.already.friend";
	
	/**
	 * The error message key when inviting a user to be a friend after already
	 * inviting them: <code>invite.friend.pending</code>
	 */
	public static final String ERROR_USER_ALREADY_INVITED = "invite.friend.pending";
	
	/**
	 * The erorr message key when inviting oneself as 
	 * a friend: <code>invite.self</code>
	 */
	public static final String ERROR_INVITE_SELF = "invite.self";
	
	/**
	 * The error message key when inviting a friend but sending the email 
	 * failed: <code>invite.email.failed</code>
	 */
	public static final String ERROR_INVITE_EMAIL_FAILURE = "invite.email.failed";
	
	/**
	 * Generic error message key when some data validation fails:
	 * <code>error.invalid.data</code>
	 */
	public static final String ERROR_INVALID_DATA = "error.invalid.data";
	
	/**
	 * The error message key while registering and the email address
	 * is already a valid user: <code>register.already.user</code>
	 */
	public static final String ERROR_ALREADY_USER = "register.already.user";
	
	/**
	 * The error message key while registering and the email address
	 * is already pending registration: <code>register.pending</code>
	 */
	public static final String ERROR_PENDING_REGISTRATION = "register.pending";
	
	/**
	 * The error message key when registering but sending the email 
	 * failed: <code>register.email.failed</code>
	 */
	public static final String ERROR_REGISTRATION_EMAIL_FAILURE = "register.email.failed";
	
	public static final int USER_SEARCH_EMAIL = 1;
	
	public static final int USER_SEARCH_NAME = 2;
	
	public static final int USER_SEARCH_USERNAME = 3;
	
	/**
	 * The mail merge template resource path for inviting a friend, from
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.template.invite.friend</code>
	 */
	public static final String[] MAIL_TEMPLATE_INVITE_FRIEND = 
		Config.getStrings(ApplicationConstants.CONFIG_ENV,"mail.template.invite.friend");
	
	/**
	 * The mail subject for inviting a friend, from
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.subject.invite.friend</code>
	 */
	public static final String MAIL_SUBJECT_INVITE_FRIEND = 
		Config.get(ApplicationConstants.CONFIG_ENV,"mail.subject.invite.friend");
	
	/**
	 * The mail merge template resource path when a friend accepts the invitation
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.template.invite.friend.accepted</code>
	 */
	public static final String[] MAIL_TEMPLATE_INVITE_FRIEND_ACCPETED = 
		Config.getStrings(ApplicationConstants.CONFIG_ENV,"mail.template.invite.friend.accepted");
	
	/**
	 * The mail subject for when a friend declines the invitation
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.subject.invite.friend.declined</code>
	 */
	public static final String MAIL_SUBJECT_INVITE_FRIEND_DECLINED = 
		Config.get(ApplicationConstants.CONFIG_ENV,
				"mail.subject.invite.friend.declined");
	
	/**
	 * The mail merge template resource path when a friend declines the invitation
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.template.invite.friend.declined</code>
	 */
	public static final String[] MAIL_TEMPLATE_INVITE_FRIEND_DECLINED = 
		Config.getStrings(ApplicationConstants.CONFIG_ENV,
				"mail.template.invite.friend.declined");
	
	/**
	 * The mail subject for when a friend accepts the invitation
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.subject.invite.friend.accepted</code>
	 */
	public static final String MAIL_SUBJECT_INVITE_FRIEND_ACCPETED = 
		Config.get(ApplicationConstants.CONFIG_ENV,"mail.subject.invite.friend.accepted");
	
	/**
	 * The mail merge template resource path for confirming registration, from
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.template.register.confirm</code>
	 */
	public static final String[] MAIL_TEMPLATE_CONFIRM_REGISTRATION = 
		Config.getStrings(ApplicationConstants.CONFIG_ENV,"mail.template.register.confirm");
	
	/**
	 * The mail subject for confirming registration, from
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.subject.register.confirm</code>
	 */
	public static final String MAIL_SUBJECT_CONFIRM_REGISTRATION = 
		Config.get(ApplicationConstants.CONFIG_ENV,"mail.subject.register.confirm");
	
	/**
	 * The mail merge template resource path for welcoming a new user
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.template.welcome</code>
	 */
	public static final String[] MAIL_TEMPLATE_WELCOME = 
		Config.getStrings(ApplicationConstants.CONFIG_ENV,"mail.template.welcome");
	
	/**
	 * The mail subject for welcoming a new user
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.subject.welcome</code>
	 */
	public static final String MAIL_SUBJECT_WELCOME = 
		Config.get(ApplicationConstants.CONFIG_ENV,"mail.subject.welcome");
	
	/**
	 * Not really a virtual view, but the normal "alubm view" mode.
	 */
	public static final int VIRTUAL_VIEW_MODE_NORMAL_ALBUMS = 0;
	
	/**
	 * The virtual view mode for by date.
	 */
	public static final int VIRTUAL_VIEW_MODE_DATE = 1;
	
	/**
	 * The virtual view mode for by average user rating.
	 */
	public static final int VIRTUAL_VIEW_MODE_AVERAGE_RATING = 2;
	
	/**
	 * The virtual view mode for by popularity (hits).
	 */
	public static final int VIRTUAL_VIEW_MODE_POPULARITY = 3;
	
	/**
	 * The virtual view mode for by my rating.
	 */
	public static final int VIRTUAL_VIEW_MODE_MY_RATING = 4;
	
	/**
	 * The virtual view mode for by owner rating.
	 */
	public static final int VIRTUAL_VIEW_MODE_OWNER_RATING = 5;
	
	/**
	 * The ID assigned to the album containing items without 
	 * creation dates during the virutal view date mode.
	 */
	public static final Integer UNKNOWN_DATE_ALBUM_ID = new Integer(-1);
	
	/**
	 * The ID assigned to the album containing items without 
	 * ratings.
	 */
	public static final Short NO_RATING_VALUE = new Short((short)0);
	
	/**
	 * The name assigned to the album containing items without 
	 * creation dates during the virutal view date mode, from 
	 * {@link ApplicationConstants#CONFIG_MSG}:
	 * <code>album.name.nodate</code>
	 */
	public static final String UNKNOWN_DATE_ALBUM_NAME = 
			Config.get(ApplicationConstants.CONFIG_MSG,"album.name.nodate");
	
	/**
	 * The name assigned to the album containing items without 
	 * a rating during the virutal view rating mode, from 
	 * {@link ApplicationConstants#CONFIG_MSG}:
	 * <code>album.name.norating</code>
	 */
	public static final String NO_RATING_ALBUM_NAME = 
			Config.get(ApplicationConstants.CONFIG_MSG,"album.name.norating");
	
	/**
	 * The name given to albums without any hits, from 
	 * {@link ApplicationConstants#CONFIG_MSG}:
	 * <code>album.name.nohits</code>
	 */
	public static final String NO_HITS_ALBUM_NAME = Config.getNotEmpty(
			ApplicationConstants.CONFIG_MSG,"album.name.nohits");
	
/**
 * Get a user by username.
 * 
 * @param username the username to get
 * @return the user, or <em>null<em> if user not found
 * @throws MediaAlbumException if an error occurs
 */
public User getUserByUsername(String username) throws MediaAlbumException;

/**
 * Get a user by ID.
 * 
 * <p>If the specified ID is not found, a MediaAlbumException will be thrown
 * with the {@link #ERROR_USER_NOT_FOUND} message key.</p>
 * 
 * @param id the ID of the user to get
 * @param allowCached if <em>true</em> then allow returning a cached object
 * @return the user
 * @throws MediaAlbumException if an error occurs or the user is not found
 */
public User getUserById(Object id, boolean allowCached) throws MediaAlbumException;

/**
 * Get a user by anonymous key.
 * 
 * <p>If the specified ID is not found, a MediaAlbumException will be thrown
 * with the {@link #ERROR_USER_NOT_FOUND} message key.</p>
 * 
 * @param key the key of the user to get
 * @return the user
 * @throws MediaAlbumException if an error occurs
 */
public User getUserByAnonymousKey(String key) throws MediaAlbumException;

public User[] getAllUsers() throws MediaAlbumException;

public Group getGroupById(Object id) throws MediaAlbumException;

public Group[] getGroupsForUserId(Object userId) throws MediaAlbumException;

public Group[] getAllGroups() throws MediaAlbumException;

public User[] getUsersForGroup(Object groupId) throws MediaAlbumException;

public void setMembersForGroup(Integer groupId, Integer[] memberIds) throws MediaAlbumException;

public User[] getFriendsForUser(Object userId) throws MediaAlbumException;


/**
 * Get all collections a specific user owns.
 * 
 * @param userId the ID of the user to get the collections for
 * @return array of collections for user
 * @throws MediaAlbumException if an error occurs
 */
public Collection[] getCollectionsForUser(Object userId) throws MediaAlbumException;

/**
 * Get all the albums owned by a particular user.
 * 
 * <p>This method takes care of arranging the Album objects
 * according to their nesting heirarchy, so the resulting array could
 * be smaller than the actual number of MediaAlbums returned from
 * the back end.</p>
 * 
 * @param userId the ID of the owner to get the albums for
 * @return the albums owned by this user, or <em>null</em> if none exist
 * @throws MediaAlbumException if an error occurs
 */
public Album[] getAlbumsOwnedByUser(Object userId) throws MediaAlbumException;

/**
 * Get all the albums a user can view.
 * 
 * <p>This method takes care of arranging the Album objects
 * according to their nesting heirarchy, so the resulting array could
 * be smaller than the actual number of MediaAlbums returned from
 * the back end.</p>
 * 
 * @param userId the ID of the user to get the albums for
 * @return the albums viewable by this user, or <em>null</em> if none exist
 * @throws MediaAlbumException if an error occurs
 */
public Album[] getAlbumsViewableByUser(Object userId) throws MediaAlbumException;

/**
 * Get all of a user's anonymous albums.
 *  
 * @param userKey the key of the user to get the anonymous albums for
 * @return the albums, or <em>null</em> if none exist
 * @throws MediaAlbumException if an error occurs
 */
public Album[] getAnonymousAlbumsForUser(String userKey) throws MediaAlbumException;

/**
 * Get the album themes that a user owns.
 * 
 * @param userId the ID of the user to get the album themes for
 * @return the album themes owned by this user, or <em>null</em> if none exist
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme[] getAlbumThemesOwnedByUser(Object userId)
throws MediaAlbumException;

/**
 * Authenticate a user by their username and password.
 * 
 * @param username the username of the user to logon
 * @param password the attempted password
 * @return the User if found and password matches
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not found or password
 * does not match
 */
public User logonUser(String username, String password)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get a complete user object, with nested data filled in.
 * @param userId the ID of the user to get
 * @return the user
 * @throws MediaAlbumException if an error occurs
 */
public User getFullUser(Integer userId)
throws MediaAlbumException;

/**
 * Check if a user has permission to search for other users.
 * 
 * @param actingUser the user wishing to search for users
 * @return <em>true</em> if user has permission to search for users
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserSearchUsers(User actingUser) throws MediaAlbumException;

/**
 * Search for users.
 * 
 * @param searchData the search data
 * @param actingUser the acting user
 * @return user search results (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to search for users
 */
public UserSearchResults searchForUsers(UserSearchData searchData, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Create a new User instance.
 * 
 * @param actingUser the user creating the new user
 * @return a new User instance
 * @throws MediaAlbumException if an error occurs
 */
public User getNewUserInstance(User actingUser)
throws MediaAlbumException;

/**
 * Check if a user has permission to create other users.
 * 
 * @param actingUser the user wishing to create other users
 * @return <em>true</em> if user has permission to create other users
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserCreateUsers(User actingUser) throws MediaAlbumException;

/**
 * Check if a user has permission to update a user.
 * 
 * @param user the user being updated
 * @param actingUser the user wishing to update the user
 * @return <em>true</em> if user has permission to update the user
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserUpdateUser(User user, User actingUser) throws MediaAlbumException;

/**
 * Save a new user to the back end.
 * 
 * @param user the new user
 * @param actingUser the acting user
 * @return the new user, with any populated data
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user is not allowed to create
 * users
 */
public User createUser(User user, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Save a user to the back end.
 * 
 * @param user the user to save
 * @param actingUser the acting user
 * @return the saved user, with any populated data
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user is not allowed to update
 * users
 */
public User updateUser(User user, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get the permissions for a user.
 * 
 * @param userId the ID of the user to get the permissions for
 * @return the Permissions object
 * @throws MediaAlbumException if an error occurs
 */
public Permissions getUserPermissions(Integer userId)
throws MediaAlbumException;

/**
 * Check if a user has permission to delete a user.
 * 
 * @param user the user being deleted
 * @param actingUser the user wishing to delete the user
 * @return <em>true</em> if user has permission to delete the user
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserDeleteUser(User user, User actingUser) 
throws MediaAlbumException;

/**
 * Delete a user from the back end, including all of their media items.
 * 
 * @param id the ID of the user to delete
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user is not allowed to delete
 * this user
 */
public void deleteUser(Object id, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Invite a user to be friends of one another.
 * 
 * <p>If the user specified by <var>email</var> is already a 
 * user on this system, they will be sent an email notifying 
 * them of the invitation. Otherwise the email address supplied 
 * will be sent an email inviting them to register with the 
 * application and then become a friend of the acting user.</p>
 * 
 * @param email the email address to invite as a friend
 * @param actingUser the acting user
 * @param accept the URL to accept the invitation
 * @param decline the URL to decline the invitation
 * @throws MediaAlbumException if an error occurs
 */
public void inviteFriend(String email, User actingUser, URL accept, URL decline)
throws MediaAlbumException;

/**
 * Confirm an invitation to be friends.
 * 
 * @param key the invitation key
 * @return the User confirmed as a friend
 * @throws MediaAlbumException if an error occurs
 */
public User confirmInvitation(String key)
throws MediaAlbumException;

/**
 * Decline an invitation to be friends.
 * 
 * @param key the invitation key
 * @return the inviter
 * @throws MediaAlbumException if an error occurs
 */
public User declineInvitation(String key)
throws MediaAlbumException;

/**
 * Get a registration based on its username.
 * 
 * @param username the username of the registration to get
 * @return Registration, or <em>null</em> if not found
 * @throws MediaAlbumException if an error occurs
 */
public Registration getRegistrationByUsername(String username) 
throws MediaAlbumException;

/**
 * Save a registration to the back end.
 * 
 * @param reg the registration to save
 * @return the new registration, populated with any data
 * @param accept the URL to accept the registration
 * @param decline the URL to decline the registration
 * @throws MediaAlbumException if an error occurs
 */
public Registration register(Registration reg, URL accept, URL decline)
throws MediaAlbumException;

/**
 * Delete a registration from the back end.
 * 
 * @param key the key of the registration to delete
 * @throws MediaAlbumException if an error occurs
 */
public void deleteRegistration(String key)
throws MediaAlbumException;

/**
 * Get a new registration instance with data populated from friend invitation.
 * 
 * @param inviteKey the invitation key
 * @return registration object
 * @throws MediaAlbumException if an error occurs
 */
public Registration getNewRegistrationForInvitation(String inviteKey)
throws MediaAlbumException;

/**
 * Confirm a registration and turn the registration into a valid user.
 * 
 * <p>The system will send a welcome email to the user's email address.</p>
 * 
 * @param regKey the registration key
 * @param access the URL to access Media Album for the welcome email
 * @return User the new user
 * @throws MediaAlbumException if an error occurs
 */
public User confirmRegistration(String regKey, URL access)
throws MediaAlbumException;

/**
 * Decline a registration.
 * 
 * @param regKey the registration key
 * @throws MediaAlbumException if an error occurs
 */
public void declineRegistration(String regKey)
throws MediaAlbumException;

/**
 * Create a new group.
 * 
 * @param name the group name
 * @param actingUser the acting user
 * @return the new group object
 * @throws MediaAlbumException
 */
public Group createGroup(String name, User actingUser)
throws MediaAlbumException;

/**
 * Check if a user has permission to delete a group.
 * 
 * @param groupId the ID of the group to delete
 * @param actingUser the user wishing to delete the group
 * @return <em>true</em> if user has permission to delete the group
 * @throws MediaAlbumException if an error occurs
 */

public boolean canUserDeleteGroup(Integer groupId, User actingUser)
throws MediaAlbumException;


/**
 * Delete a group from the back end, including all of their members.
 * 
 * @param groupId the ID of the group to delete
 * @param actingUser the acting user
 * @return the deleted group
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user is not allowed to delete
 * this group
 */
public Group deleteGroup(Integer groupId, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Check if a user has permission to update a group.
 * 
 * @param groupId the ID of the group to update
 * @param actingUser the user wishing to update the group
 * @return <em>true</em> if user has permission to updaet the group
 * @throws MediaAlbumException if an error occurs
 */

public boolean canUserUpdateGroup(Integer groupId, User actingUser)
throws MediaAlbumException;


/**
 * Update a group in the back end.
 * 
 * @param group the group to update
 * @param actingUser the acting user
 * @return the updated group
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user is not allowed to update
 * this group
 */
public Group updateGroup(Group group, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Check if a user is a member of a group.
 * 
 * @param userId the user ID to check
 * @param groupId the group to see if user is a member of
 * @return <em>true</em> if user is a member of this group
 * @throws MediaAlbumException if an error occurs
 */
public boolean isUserMemberOfGroup(Integer userId, Integer groupId)
throws MediaAlbumException;

/**
 * Get the total number of items viewable by one user owned by another
 * that reside in albums viewable by one user of another user.
 * 
 * @param user the owner to look for images for
 * @param actingUser the acting user
 * @return the count of images available
 * @throws MediaAlbumException if an error occurs
 */
public int getTotalAlbumItemsViewableForUser(User user, User actingUser)
throws MediaAlbumException;

/**
 * Get the total number of items viewable by one user owned by another.
 * 
 * @param user the owner to look for images for
 * @param actingUser the acting user
 * @return the count of images available
 * @throws MediaAlbumException if an error occurs
 */
public int getTotalItemsViewableForUser(User user, User actingUser)
throws MediaAlbumException;

/**
 * Get all albums viewable by one user owned by another arranged 
 * in a "virutal" view mode.
 * 
 * @param user the owner to look for albums for
 * @param mode the virutal view mode
 * @param actingUser the acting user
 * @return all albums, nested according to the virutal view mode
 * @throws MediaAlbumException if an error occurs
 */
public Album[] getVirtualAlbumsViewableForUser(User user, int mode, User actingUser)
throws MediaAlbumException;

/**
 * Get all albums viewable by one user owned by another.
 * 
 * @param user the owner to look for albums for
 * @param actingUser the acting user
 * @return all albums, nested according to their parent-child relationships
 * @throws MediaAlbumException if an error occurs
 */
public Album[] getAlbumsViewableForUser(User user, Comparator sort, User actingUser)
throws MediaAlbumException;

/**
 * Return <em>true</em> if the user specified by the user ID has super user
 * permissions.
 * 
 * @param userId the ID of the user to check
 * @return <em>true</em> if the user exists and has super user permissions
 * @throws MediaAlbumException if an error occurs
 */
public boolean isUserSuperUser(Integer userId) throws MediaAlbumException;

/**
 * Get all free data available for a user.
 * 
 * @param user the user to get the free data for
 * @param allowCached if <em>true</em> then allow returning cached objects
 * @return the free data (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 */
public FreeData[] getFreeData(User user, boolean allowCached) 
throws MediaAlbumException;

/**
 * Save the free data for a user, replacing any free data currently saved.
 * 
 * @param user the owner of the free data
 * @param data the free data
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have 
 * permission to update the user's free data
 */
public void setFreeData(User user, FreeData[] data, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get a set of album owners populated with their free data.
 * 
 * @param albums the albums to find the owners for
 * @param allowCached if <em>true</em> then allow returning cached objects
 * @return array of users (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 */
public User[] getOwnersWithFreeData(Album[] albums, boolean allowCached)
throws MediaAlbumException;

/**
 * Return the total number of bytes a user currently has stored in 
 * the form of media files in the application.
 * 
 * <p>Note this does not count any cache files. This number can be used
 * to enforce user quotas.</p>
 * 
 * @param userId the ID of the user to get the disk usage for
 * @return the number of bytes the user is taking up
 * @throws MediaAlbumException if an error occurs
 */
public long getDiskUsage(Integer userId) throws MediaAlbumException;

}
