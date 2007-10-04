/* ===================================================================
 * UserBiz.java
 * 
 * Created Nov 12, 2005 8:04:45 PM
 * 
 * Copyright (c) 2005 Matt Magoffin.
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
 * $Id: UserBiz.java,v 1.17 2007/09/09 10:38:25 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.springframework.core.io.Resource;

import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AlbumFeedCommand;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.support.PreferencesCommand;

/**
 * Business interface for users.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.17 $ $Date: 2007/09/09 10:38:25 $
 */
public interface UserBiz {

	/** Bit flag for Admin user access level. */
	public static final int ACCESS_ADMIN = 0x1;
	
	/** The metadata key for the user watermark path. */
	public static final String WATERMARK_META_KEY = "watermark";
	
	/** 
	 * Flag for a String value that should not change.
	 * 
	 * <p>For example, when updating a User, the password field can be 
	 * left unchagned when set to this value.</p>
	 */
	public static final String DO_NOT_CHANGE_VALUE = "**DO_NOT_CHANGE**";
	
	/**
	 * Register a new user.
	 * 
	 * <p>Use this method to register a new user. After registration
	 * the user will be stored in the back end, but the user will
	 * require confirmation before they can officially log into the 
	 * application (see {@link #confirmRegisteredUser(String, String, BizContext)}).
	 * </p>
	 * 
	 * @param user the new user to register
	 * @param context the BizContext
	 * 
	 * @return a confirmation string suitable to pass to 
	 * {@link #confirmRegisteredUser(String, String, BizContext)}
	 * @throws AuthorizationException if the desired login is taken already, 
	 * this exception will be thrown with the reason code 
	 * {@link AuthorizationException.Reason#DUPLICATE_LOGIN}
	 */
	public String registerUser(User user, BizContext context) throws AuthorizationException;
	
	/**
	 * Confirm a registered user.
	 * 
	 * <p>After a user has registered (see {@link #registerUser(User, BizContext)}) they 
	 * must confirm the registration via this method. After confirmation the 
	 * user can login via {@link #logonUser(String, String)} as a normal
	 * user.</p>
	 * 
	 * @param login the login to confirm
	 * @param confirmationCode the confirmation code
	 * @param context the BizContext
	 * @return the confirmed user
	 * @throws AuthorizationException if the confirmationCode does not match
	 * then the reason code will be set to {@link AuthorizationException.Reason#REGISTRATION_NOT_CONFIRMED}, 
	 * if the login is not found then {@link AuthorizationException.Reason#UNKNOWN_LOGIN}, if 
	 * the account has already been confirmed then 
	 * {@link AuthorizationException.Reason#REGISTRATION_ALREADY_CONFIRMED}
	 */
	public User confirmRegisteredUser(String login, String confirmationCode, BizContext context)
	throws AuthorizationException;
	
	/**
	 * Store a User in the back end.
	 * 
	 * <p>This method will accept new users as well as updates 
	 * to existing users.</p>
	 * 
	 * <p>When updating an existing user, if the User's password field
	 * is set to {@link #DO_NOT_CHANGE_VALUE} then the User's password
	 * will not be updated in the back end.</p>
	 * 
	 * @param user the user to store
	 * @param context the current context
	 * @return the stored user's primary key
	 * @throws AuthorizationException if the current user is not authorized to update 
	 * this user
	 */
	public Long storeUser(User user, BizContext context) throws AuthorizationException;
	
	/**
	 * Store user preferences in the back end.
	 * 
	 * @param command the preferences to store
	 * @param context the current context
	 * @throws AuthorizationException if the current user is not authorized to update 
	 * this user
	 */
	public void storeUserPreferences(PreferencesCommand command, BizContext context)
	throws AuthorizationException;
	
	/**
	 * Get a Resource for a user's watermark.
	 * 
	 * @param userId the ID of the user to get the watermark resource for
	 * @return a Resource, or <em>null</em> if none available
	 */
	public Resource getUserWatermark(Long userId);
	
	/**
	 * Remove a User from the back end.
	 * @param userId the ID of the User to remove
	 * @param context the current context
	 */
	public void removeUser(Long userId, BizContext context);

	/**
	 * Authenticate a user by their username and password.
	 * 
	 * @param login the login name of the user to logon
	 * @param password the attempted password
	 * @return the User if found and password matches
	 * @throws AuthorizationException if user not found or password
	 * does not match
	 */
	public User logonUser(String login, String password) throws AuthorizationException;
	
	/**
	 * Get a User by its ID.
	 * @param userId the ID of the user to get
	 * @param context the current context
	 * @return User
	 */
	public User getUserById(Long userId, BizContext context);
	
	/**
	 * Get a User by its anonymous key.
	 * @param key the key of the user to get
	 * @return the user, or <em>null</em> if not found
	 */
	public User getUserByAnonymousKey(String key);
	
	/**
	 * Get the Locale for a User.
	 * @param user the user to get the Locale for
	 * @param context the current context
	 * @return Locale
	 */
	public Locale getUserLocale(User user, BizContext context);
	
	/**
	 * Get the directory for a given collection.
	 * @param collection the collection to get the root directory for
	 * @param context the current context
	 * @return the directory
	 */
	public File getCollectionDirectory(Collection collection, BizContext context);
	
	/**
	 * Get a single collection.
	 * 
	 * <p>The collection items need not be populated here. See the 
	 * {@link MediaBiz#getCollectionWithItems(Long, BizContext)} method
	 * for that.</p>
	 * 
	 * @param collectionId the ID of the collection to get
	 * @param context the current context
	 * @return the collection, or <em>null</em> if not available
	 */
	public Collection getCollection(Long collectionId, BizContext context);
	
	/**
	 * Store a collection in the back end.
	 * 
	 * <p>This method will accept new collections as well as updates 
	 * to existing collections.</p>
	 * 
	 * @param collection the collection to store
	 * @param context the current context
	 * @return the stored collection's primary key
	 * @throws AuthorizationException if the current user is not authorized to update 
	 * this collection
	 */
	public Long storeCollection(Collection collection, BizContext context) 
	throws AuthorizationException;
	
	/**
	 * Get all collections for a given user.
	 * 
	 * <p>Note this method is not presumed to return Collection instances populated
	 * with the {@link magoffin.matt.ma2.domain.MediaItem} instances associated 
	 * with each Collection. That is assumed to be a very expensive operation, so 
	 * the implementation need not bother returning "complete" Collection instanes.</p>
	 * 
	 * @param user the user to get collections for
	 * @param context the current context
	 * @return non-null List of users's collections
	 */
	public List<Collection> getCollectionsForUser(User user, BizContext context);
	
	/**
	 * Create a new Collection for a given user.
	 * 
	 * @param collection the collection object to associate with the user
	 * @param user the user
	 * @param context the current context
	 * @return the saved Collection
	 */
	public Collection newCollectionForUser(Collection collection, User user, BizContext context);
	
	/**
	 * Get all albums for a given user.
	 * 
	 * <p>Note this method is not presumed to return Album instances populated
	 * with the {@link magoffin.matt.ma2.domain.MediaItem} instances associated 
	 * with each Album. That is assumed to be a very expensive operation, so 
	 * the implementation need not bother returning "complete" Album instanes.</p>
	 * 
	 * @param user the user to get albums for
	 * @param context the current context
	 * @return non-null List of users's albums
	 */
	public List<Album> getAlbumsForUser(User user, BizContext context);
	
	/**
	 * Get a List of Albums for a user's album feed.
	 * 
	 * @param command the command
	 * @return the albums
	 */
	public List<Album> getAlbumFeedForUser(AlbumFeedCommand command);
	
	/**
	 * Get a List of Albums for a browse command.
	 * 
	 * @param command the browse command
	 * @return the albums
	 */
	public List<Album> getSharedAlbumsForUser(BrowseAlbumsCommand command);
	
	/**
	 * Return <em>true</em> if the supplied user has the specified access level.
	 * @param user the user to test
	 * @param level the level (use the <code>ACCESS_*</code> constants)
	 * @return <em>true</em> if the supplied user has the specified access level
	 */
	public boolean hasAccessLevel(User user, int level);
	
	/**
	 * Call to email a user a link with a one-time password.
	 * 
	 * <p>Calling this method will do the following:</p>
	 * <ol>
	 * <li>Generate a random password for the user</li>
	 * <li>Update the user with the new password</li>
	 * <li>Generate a confirmation code for the user to pass later
	 * to {@link #confirmForgotPassword(String, String, String, BizContext)}</li>
	 * </ol>
	 * 
	 * @param login the login of the user that forgot their password
	 * @param context the current context
	 * @return a confirmation string suitable to pass to 
	 * {@link #confirmForgotPassword(String, String, String, BizContext)}
	 * @throws AuthorizationException if the <var>login</var> is not found the reason 
	 * code will be set to {@link AuthorizationException.Reason#UNKNOWN_LOGIN}
	 */
	public String forgotPassword(String login, BizContext context) throws AuthorizationException;
	
	/**
	 * Confirm a forgotten password.
	 * 
	 * @param login the login of the user being confirmed
	 * @param confirmationCode the confirmation code issued by a previous 
	 * call to {@link #forgotPassword(String, BizContext)}
	 * @param newPassword the new password to set
	 * @param context the current context
	 * @return the confirmed user
	 * @throws AuthorizationException if the <var>login</var> is not found the reason 
	 * code will be set to {@link AuthorizationException.Reason#UNKNOWN_LOGIN}, if the confirmationCode 
	 * does not match then the reason code will be set to 
	 * {@link AuthorizationException.Reason#FORGOTTEN_PASSWORD_NOT_CONFIRMED}
	 */
	public User confirmForgotPassword(String login, String confirmationCode, String newPassword, BizContext context)
	throws AuthorizationException;
	
	/**
	 * Get a list of User objects that have a specific access level.
	 * @param level the access level
	 * @return list of users
	 */
	public List<User> getUsersWithAccess(int level);
	
	/**
	 * Get an anonymous user object.
	 * 
	 * @return user instance
	 */
	public User getAnonymousUser();
	
	/**
	 * Test if a User is an anonoymous user.
	 * 
	 * @param user the user to test
	 * @return true if the user is an anonymous user
	 */
	public boolean isAnonymousUser(User user);
	
}
