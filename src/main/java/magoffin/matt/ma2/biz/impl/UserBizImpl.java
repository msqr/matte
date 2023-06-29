/* ===================================================================
 * UserBizImpl.java
 * 
 * Created Dec 20, 2005 2:01:18 PM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.biz.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.ConfigurationException;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.ValidationException;
import magoffin.matt.ma2.AuthorizationException.Reason;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.BizContext.Feature;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.UserDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaSpec;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AlbumFeedCommand;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.support.InternalBizContext;
import magoffin.matt.ma2.support.PreferencesCommand;
import magoffin.matt.util.DataEncryption;
import magoffin.matt.util.MessageDigester;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Standard implementation of {@link magoffin.matt.ma2.biz.UserBiz}.
 * 
 * <p><b>Note:</b> the {@link #init()} method should be called after 
 * configuring this class but before calling any other methods; the 
 * {@link #finish()} should be called when finished using.</p> 
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>encryptor</dt>
 *   <dd>An optional {@link magoffin.matt.util.DataEncryption} instance 
 *   to use for encrypting the registration confirmation with.</dd>
 *   
 *   <dt>salt</dt>
 *   <dd>An optional <em>salt</em> to add to encrypted passwords.</dd>
 *   
 *   <dt>ioBiz</dt>
 *   <dd>The {@link IOBiz} implementation to ues.</dd>
 *   
 *   <dt>systemBiz</dt>
 *   <dd>The {@link magoffin.matt.ma2.biz.SystemBiz} implementation to use.</dd>
 *   
 *   <dt>userDao</dt>
 *   <dd>A {@link magoffin.matt.ma2.dao.UserDao} to use for managing
 *   persistent User objects.</dd>
 *   
 *   <dt>collectionDao</dt>
 *   <dd>A {@link magoffin.matt.ma2.dao.CollectionDao} to use for managing
 *   persistent Collection objects.</dd>
 *   
 *   <dt>mediaItemDao</dt>
 *   <dd>A {@link magoffin.matt.ma2.dao.MediaItemDao} to use for managing
 *   persistent MediaItem objects.</dd>
 *   
 *   <dt>userValidator</dt>
 *   <dd>An optional <code>Validator</code> instance to use for 
 *   validating User data with.</dd>
 *   
 *   <dt>messages</dt>
 *   <dd>A {@link org.springframework.context.MessageSource}.</dd>
 *   
 *   <dt>domainObjectFactory</dt>
 *   <dd>The {@link magoffin.matt.ma2.biz.DomainObjectFactory} for creating
 *   new domain objects.</dd>
 *   
 *   <dt>defaultCollectionNameMessageKey</dt>
 *   <dd>The MessageSource key that resolves to the default name to apply
 *   to a new user's first Collection. Defaults to <code>default.collection.name</code>.</dd>
 * 
 *   <dt>defaultCollectionCommentMessageKey</dt>
 *   <dd>The MessageSource key that resolves to the default name to apply
 *   to a new user's first Collection. Defaults to <code>default.collection.comment</code>.</dd>
 * 
 *   <dt>adminUserTemplate</dt>
 *   <dd>A map of properties to apply to the default initial admin user.</dd>
 *   
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class UserBizImpl implements UserBiz {
	
	private final Logger log = Logger.getLogger(UserBizImpl.class);
	
	/** The default value for the <code>albumFeedMaxLength</code> property. */
	public static final int DEFAULT_ALBUM_FEED_MAX_LENGHT = 25;

	private static final String[] COLLECTION_STORE_DO_NOT_CLONE = new String[] {
		"item"
	};
	
	private byte[] salt = new byte[0];
	private UserDao userDao = null;
	private AlbumDao albumDao = null;
	private CollectionDao collectionDao = null;
	private DataEncryption encryptor = null;
	private Validator userValidator = null;
	private SystemBiz systemBiz = null;
	private DomainObjectFactory domainObjectFactory = null;
	private MessageSource messages = null;
	private String defaultCollectionNameMessageKey = "default.collection.name";
	private String defaultCollectionCommentMessageKey = "default.collection.comment";
	private Map<String,Object> adminUserTemplate = null;
	private Map<String,Object> anonymousUserTemplate = null;
	private MediaSize defaultThumbSize = MediaSize.THUMB_NORMAL;
	private MediaSize defaultViewSize = MediaSize.NORMAL;
	private MediaQuality defaultThumbQuality = MediaQuality.GOOD;
	private MediaQuality defaultViewQuality = MediaQuality.GOOD;
	private int albumFeedMaxLength = DEFAULT_ALBUM_FEED_MAX_LENGHT;
	private User anonymousUser = null;
	private Set<MediaSize> watermarkSizes = EnumSet.complementOf(
			EnumSet.of(MediaSize.THUMB_BIGGER, MediaSize.THUMB_BIG, 
					MediaSize.THUMB_NORMAL, MediaSize.THUMB_SMALL));
	private IOBiz ioBiz;

	/**
	 * Call to initialize the class after configuring properties.
	 */
	public void init() {
		if ( this.userDao == null ) {
			throw new ConfigurationException(null,"userDao");
		}
		if ( this.systemBiz == null ) {
			throw new ConfigurationException(null,"systemBiz");
		}
		if ( domainObjectFactory == null ) {
			throw new ConfigurationException(null,"domainObjectFactory");
		}
		if ( albumDao == null ) {
			throw new ConfigurationException(null,"albumDao");
		}
		if ( collectionDao == null ) {
			throw new ConfigurationException(null,"collectionDao");
		}
		if ( messages == null ) {
			throw new ConfigurationException(null,"messages");
		}
		try {
			messages.getMessage(this.defaultCollectionNameMessageKey,null,null);
		} catch ( NoSuchMessageException e ) {
			log.warn("The message key [" +this.defaultCollectionNameMessageKey 
					+"] does not resolve, will use hard-coded default.");
		}

		if ( !systemBiz.isApplicationConfigured() ) {
			log.warn("Application not configured, not continuing initialization.");
			return;
		}
		
		// make sure admin user exits
		List<User> adminUsers = userDao.findUsersForAccess(UserBiz.ACCESS_ADMIN);
		if ( adminUsers.size() < 1 ) {
			// create default admin user now
			if ( this.adminUserTemplate == null ) {
				throw new ConfigurationException(null,"adminUserTemplate");
			}
			User adminUser = domainObjectFactory.newUserInstance();
			BeanWrapper wrapper = new BeanWrapperImpl(adminUser);
			wrapper.setPropertyValues(this.adminUserTemplate);
			adminUser.setAccessLevel(UserBiz.ACCESS_ADMIN);
			try {
				storeUser(adminUser,new InternalBizContext());
			} catch ( AuthorizationException e ) {
				throw new RuntimeException(e);
			}
		}
		
		// setup anonymous user
		User anonUser = domainObjectFactory.newUserInstance();
		BeanWrapper wrapper = new BeanWrapperImpl(anonUser);
		wrapper.setPropertyValues(this.anonymousUserTemplate);
		this.anonymousUser = anonUser;
	}
	
	/**
	 * Call to release any class resources when finished using.
	 */
	public void finish() {
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getCollection(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public Collection getCollection(Long collectionId, BizContext context) {
		Collection c = collectionDao.get(collectionId);
		return (Collection)getDomainObjectFactory().clone(c);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#storeCollection(magoffin.matt.ma2.domain.Collection, magoffin.matt.ma2.biz.BizContext)
	 */
	public Long storeCollection(Collection collection, BizContext context) throws AuthorizationException {
		if ( collection.getCollectionId() == null ) {
			return newCollectionForUser(collection, context.getActingUser(), 
					context).getCollectionId();
		}
		Collection collectionToStore = getCollectionDao().get(collection.getCollectionId());
		BeanUtils.copyProperties(collection, collectionToStore, 
					COLLECTION_STORE_DO_NOT_CLONE);
		prepareCollectionForStorage(collection, context.getActingUser());
		return collectionDao.store(collection);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#registerUser(magoffin.matt.ma2.domain.User, magoffin.matt.ma2.biz.BizContext)
	 */
	public String registerUser(User user, BizContext context)
			throws AuthorizationException {
		if ( context != null && 
				!context.isFeatureEnabled(Feature.REGISTRATION) ) {
			throw new AuthorizationException(user.getLogin(), 
					Reason.ACCESS_DENIED);
		}
				
		if ( this.userValidator != null ) {
			Errors errors = new BindException(user,"user");
			this.userValidator.validate(user,errors);
			if ( errors.hasErrors() ) {
				throw new ValidationException(errors);
			}
		}
		
		// generate random access level (negative), to use as confirmation
		Integer rnd = new Integer(-(int)(Math.random()*1000000000));
		user.setAccessLevel(rnd);
		
		// store user
		prepareUserForStorage(user);
		userDao.store(user);
		
		// generate confirmation string
		String conf = encryptor == null 
				? String.valueOf(user.getCreationDate().getTimeInMillis()) 
				: encryptor.encrypt(rnd.toString()
						+user.getCreationDate().getTimeInMillis());
		if ( log.isDebugEnabled() ) {
			log.debug("Registered user '" +user.getLogin() 
					+"' with confirmation '" +conf +"'");
		}
		return conf;
	}

	private void prepareUserForStorage(User user) throws AuthorizationException {
		// check for "unchanged" password value
		if ( user.getUserId() != null && DO_NOT_CHANGE_VALUE.equals(user.getPassword()) ) {
			// retrieve user from back-end and copy that password onto our user
			User realUser = userDao.get(user.getUserId());
			user.setPassword(realUser.getPassword());
		}
		
		// check password is encrypted
		if ( user.getPassword() != null && !(user.getPassword().startsWith("{SHA}") 
				|| user.getPassword().startsWith("{SSHA}")) ) {
			// encrypt the password now
			String encryptedPass = MessageDigester.generateDigest(
					user.getPassword(),salt);
			user.setPassword(encryptedPass);
		}
		if ( user.getCreationDate() == null ) {
			user.setCreationDate(Calendar.getInstance());
		}

		// verify username and/or email not already in use
		User existingUser = userDao.getUserByLogin(user.getLogin());
		if ( existingUser != null && !existingUser.getUserId().equals(user.getUserId()) ) {
			throw new AuthorizationException(user.getLogin(),
					AuthorizationException.Reason.DUPLICATE_LOGIN);
		}
		existingUser = userDao.getUserByEmail(user.getEmail());
		if ( existingUser != null && !existingUser.getUserId().equals(user.getUserId())) {
			throw new AuthorizationException(user.getLogin(),
					AuthorizationException.Reason.DUPLICATE_EMAIL);
		}

		// check country/lang set
		if ( user.getCountry() == null || user.getLanguage() == null ) {
			Locale l = Locale.getDefault();
			user.setCountry(l.getCountry());
			user.setLanguage(l.getLanguage());
		}
		
		// check TimeZone set
		if ( user.getTz() == null ) {
			user.setTz(systemBiz.getDefaultTimeZone());
		}
		
		// check Theme set
		if ( user.getDefaultTheme() == null ) {
			user.setDefaultTheme(systemBiz.getDefaultTheme());
		}
		if ( user.getBrowseTheme() == null ) {
			user.setBrowseTheme(systemBiz.getDefaultTheme());
		}
		
		// check MediaSpecs are set
		if ( user.getThumbnailSetting() == null ) {
			MediaSpec spec = domainObjectFactory.newMediaSpecInstance();
			spec.setQuality(defaultThumbQuality.name());
			spec.setSize(defaultThumbSize.name());
			user.setThumbnailSetting(spec);
		}
		if ( user.getViewSetting() == null ) {
			MediaSpec spec = domainObjectFactory.newMediaSpecInstance();
			spec.setQuality(defaultViewQuality.name());
			spec.setSize(defaultViewSize.name());
			user.setViewSetting(spec);
		}
		
		// check anonymous key set
		if ( user.getAnonymousKey() == null ) {
			String data = user.getEmail()+System.currentTimeMillis();
			String userKey = DigestUtils.md5Hex(data);
			user.setAnonymousKey(userKey);
		}
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#confirmRegisteredUser(java.lang.String, java.lang.String, magoffin.matt.ma2.biz.BizContext)
	 */
	public User confirmRegisteredUser(String login, String confirmationCode,
			BizContext context) throws AuthorizationException {
		if ( context != null && 
				!context.isFeatureEnabled(Feature.REGISTRATION) ) {
			throw new AuthorizationException(login, Reason.ACCESS_DENIED);
		}
		User user = userDao.getUserByLogin(login);
		if ( user == null ) {
			throw new AuthorizationException(login,
					AuthorizationException.Reason.UNKNOWN_LOGIN);
		}
		
		if ( user.getAccessLevel() > 0 ) {
			throw new AuthorizationException(login,AuthorizationException.Reason.REGISTRATION_ALREADY_CONFIRMED);
		}
		
		// validate confirmation string against user access number
		String decryptedConfCode = null;
		try {
			decryptedConfCode = encryptor.decrypt(confirmationCode);
		} catch ( RuntimeException e ) {
			log.warn("Exception decrypting confirmation code [" +confirmationCode +"]: " +e);
		}
		
		// MySQL does not store milliseconds in the DateTime field, so we only compare up to the 
		// second here
		String confCode = String.valueOf(user.getCreationDate().getTimeInMillis());
		confCode = user.getAccessLevel() +confCode.substring(0,confCode.length()-3);	
		if ( decryptedConfCode == null || !decryptedConfCode.startsWith(confCode) ) {
			throw new AuthorizationException(login,AuthorizationException.Reason.REGISTRATION_NOT_CONFIRMED);
		}
		
		// adjust access level to 0 (ordinary user)
		user.setAccessLevel(0);
		
		// store user
		Long userId = userDao.store(user);
		user = userDao.get(userId);
		
		// create a default collection for user
		Collection c = domainObjectFactory.newCollectionInstance();
		c.setName(messages.getMessage(this.defaultCollectionNameMessageKey,null,"Default",
				getUserLocale(user,context)));
		c.setComment(messages.getMessage(this.defaultCollectionCommentMessageKey,null,
				"Your default collection, created automatically for you.",
				getUserLocale(user,context)));
		c = saveNewCollection(c,user);
		
		return user;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getCollectionsForUser(magoffin.matt.ma2.domain.User, magoffin.matt.ma2.biz.BizContext)
	 */
	public List<Collection> getCollectionsForUser(User user, BizContext context) {
		List<Collection> collections = collectionDao.findCollectionsForUser(user.getUserId());
		
		// we don't want to return Collection instances populated with MediaItems here, 
		// so copy properties into dummy Collection instances
		List<Collection> results = new LinkedList<Collection>();
		for ( Collection c : collections ) {
			results.add((Collection)domainObjectFactory.clone(c));
		}
		return results;
	}
	
	private void prepareCollectionForStorage(Collection c, User owner) {
		if ( c.getCreationDate() == null ) {
			c.setCreationDate(Calendar.getInstance());
		}
		if ( c.getOwner() == null ) {
			c.setOwner(owner);
		}
		if ( c.getCollectionId() != null ) {
			c.setModifyDate(Calendar.getInstance());
		}
	}
	
	private Collection saveNewCollection(Collection c, User user) {
		prepareCollectionForStorage(c, user);

		// set the path to a non-empty value, but we'll set it again after the collection gets an ID
		c.setPath(getCollectionDirectory(c,user));
		Long collectionId = collectionDao.store(c);
		
		// now set the path again
		Collection savedCollection = collectionDao.get(collectionId);
		savedCollection.setPath(getCollectionDirectory(savedCollection,user));
		collectionDao.store(savedCollection);
		
		// now make the directories
		File dir = new File(systemBiz.getCollectionRootDirectory(), 
				savedCollection.getPath());
		dir.mkdirs();
		
		return savedCollection;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#newCollectionForUser(magoffin.matt.ma2.domain.Collection, magoffin.matt.ma2.domain.User, magoffin.matt.ma2.biz.BizContext)
	 */
	public Collection newCollectionForUser(Collection collection, User user, BizContext context) {
		Collection savedCollection = (Collection)domainObjectFactory.clone(collection);
		savedCollection = saveNewCollection(savedCollection, user);
		return (Collection)domainObjectFactory.clone(savedCollection);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getAlbumsForUser(magoffin.matt.ma2.domain.User, magoffin.matt.ma2.biz.BizContext)
	 */
	public List<Album> getAlbumsForUser(User user, BizContext context) {
		List<Album> albums = albumDao.findAlbumsForUser(user.getUserId());
		
		// we don't want to return Album instances populated with MediaItems here, 
		// so copy properties into dummy Album instances
		List<Album> results = new LinkedList<Album>();
		for ( Album a : albums ) {
			results.add((Album)domainObjectFactory.clone(a));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getCollectionDirectory(magoffin.matt.ma2.domain.Collection, magoffin.matt.ma2.biz.BizContext)
	 */
	public File getCollectionDirectory(Collection collection, BizContext context) {
		return new File(systemBiz.getCollectionRootDirectory(),collection.getPath());
	}
	
	/**
	 * Get a relative directory for a Collection.
	 * @param collection the collection
	 * @param user the user
	 * @return the path
	 */
	private String getCollectionDirectory(Collection collection, User user) {
		File dir = new File(user.getUserId().toString());
		if ( collection.getCollectionId() != null ) {
			dir = new File(dir,collection.getCollectionId().toString());
		}
		// the following seems a good way to get uniform path seperators in the path
		try {
			return new URL("file:"+dir.getPath()).getPath();
		} catch ( MalformedURLException e ) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getUserLocale(magoffin.matt.ma2.domain.User, magoffin.matt.ma2.biz.BizContext)
	 */
	public Locale getUserLocale(User user, BizContext context) {
		if ( user != null && user.getCountry() != null && user.getLanguage() != null ) {
			return new Locale(user.getLanguage(),user.getCountry());
		}
		if ( context != null && context.getLocale() != null) {
			return context.getLocale();
		}
		return Locale.getDefault();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#storeUser(magoffin.matt.ma2.domain.User, magoffin.matt.ma2.biz.BizContext)
	 */
	public Long storeUser(User user, BizContext context)
	throws AuthorizationException {
		prepareUserForStorage(user);
		return userDao.store(user);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#storeUserPreferences(magoffin.matt.ma2.support.PreferencesCommand, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public void storeUserPreferences(PreferencesCommand command, BizContext context) 
	throws AuthorizationException{
		User user = getUserById(command.getUserId(), context);
		if ( command.getThumb() != null ) {
			user.setThumbnailSetting(command.getThumb());
		}
		if ( command.getView() != null ) {
			user.setViewSetting(command.getView());
		}
		if ( command.getBrowseThemeId() != null ) {
			user.setBrowseTheme(getSystemBiz().getThemeById(command.getBrowseThemeId()));
		}
		if ( command.getTimeZone() != null ) {
			TimeZone tz = getSystemBiz().getTimeZoneForCode(command.getTimeZone());
			user.setTz(tz);
		}
		if ( command.getLocale() != null ) {
			String[] data = command.getLocale().split("_", 2);
			if ( data.length == 2 ) {
				user.setLanguage(data[0]);
				user.setCountry(data[1]);
			}
		}
		List<Metadata> metaList = user.getMetadata();
		Metadata watermarkMeta = null;
		boolean watermarkChanged = false;
		for ( Metadata meta : metaList ) {
			if ( WATERMARK_META_KEY.equals(meta.getKey()) ) {
				watermarkMeta = meta;
				break;
			}
		}
		if ( command.isDeleteWatermark() ) {
			if ( watermarkMeta != null ) {
				File watermarkFile = new File(getUserResourceDirectory(user),
						watermarkMeta.getValue());
				watermarkFile.delete();
				metaList.remove(watermarkMeta);
				watermarkChanged = true;
			}
		} else if ( command.getWatermarkFile() != null
				&& StringUtils.hasText(command.getWatermarkFile().getName()) ) {
			if ( watermarkMeta != null ) {
				File watermarkFile = new File(getUserResourceDirectory(user),
						watermarkMeta.getValue());
				watermarkFile.delete();
			}
			File watermarkFile = new File(getUserResourceDirectory(user), 
					"watermark_" +command.getWatermarkFile().getName());
			try {
				FileCopyUtils.copy(command.getWatermarkFile().getInputStream(), 
					new FileOutputStream(watermarkFile));
			} catch ( IOException e ) {
				throw new RuntimeException(e);
			}
			if ( watermarkMeta == null ) {
				watermarkMeta = domainObjectFactory.newMetadataInstance();
				watermarkMeta.setKey(WATERMARK_META_KEY);
				metaList.add(watermarkMeta);
			}
			watermarkMeta.setValue(watermarkFile.getName());
			watermarkChanged = true;
		}
		if ( watermarkChanged ) {
			ioBiz.clearCacheFiles(user, this.watermarkSizes);
		}
		storeUser(user, context);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getUserWatermark(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public Resource getUserWatermark(Long userId) {
		User user = userDao.get(userId);
		Metadata watermarkMeta = null;
		for ( Metadata meta : (List<Metadata>)user.getMetadata() ) {
			if ( WATERMARK_META_KEY.equals(meta.getKey()) ) {
				watermarkMeta = meta;
				break;
			}
		}
		if ( watermarkMeta == null ) {
			return null;
		}
		File file = new File(getUserResourceDirectory(user),
				watermarkMeta.getValue());
		if ( !file.exists() ) {
			return null;
		}
		return new FileSystemResource(file);
	}

	private File getUserResourceDirectory(User user) {
		File dir = new File(systemBiz.getResourceDirectory(), 
				user.getUserId().toString());
		if ( !dir.exists() ) {
			dir.mkdirs();
		}
		return dir;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#removeUser(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public void removeUser(Long userId, BizContext context) {
		userDao.delete(userDao.get(userId));
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#logonUser(java.lang.String, java.lang.String)
	 */
	public User logonUser(String login, String password)
			throws AuthorizationException {
		String attemptedPass = MessageDigester.generateDigest(password,salt);
		if ( log.isInfoEnabled() ) {
			log.info("Attempting to log in '" +login +"' with '" +attemptedPass +"'");
		}

		// get user from backend
		User user = userDao.getUserByLogin(login);
		if ( user == null ) {
			throw new AuthorizationException(login,
					AuthorizationException.Reason.UNKNOWN_LOGIN);
		}

		// verify not pending registration confirmation
		if ( user.getAccessLevel() < 0 ) {
			throw new AuthorizationException(login,
					AuthorizationException.Reason.REGISTRATION_NOT_CONFIRMED);
		}
		
		// see if encrypted passwords match
		if ( !attemptedPass.equals(user.getPassword()) ) {
			throw new AuthorizationException(login,AuthorizationException.Reason.BAD_PASSWORD);
		}
		
		// TODO support last login time
		// Calendar now = Calendar.getInstance();
		// user.setLastLoginDate(now);
		// userDao.updateUser(user);
		
		return user;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getAlbumFeedForUser(magoffin.matt.ma2.support.AlbumFeedCommand)
	 */
	public List<Album> getAlbumFeedForUser(AlbumFeedCommand command) {
		User user = getUserDao().getUserByKey(command.getUserKey());
		if ( command.getEntriesSince() != null ) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(command.getEntriesSince());
			return getAlbumDao().findAlbumsForUserByDate(user.getUserId(), 
					cal, true, false, true);
		}
		int max = command.getMaxEntries();
		if ( max > getAlbumFeedMaxLength() ) {
			max = getAlbumFeedMaxLength();
		}
		return getAlbumDao().findAlbumsForUserByDate(user.getUserId(), max, 
				true, false, true);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getSharedAlbumsForUser(magoffin.matt.ma2.support.BrowseAlbumsCommand)
	 */
	public List<Album> getSharedAlbumsForUser(BrowseAlbumsCommand command) {
		User user = getUserDao().getUserByKey(command.getUserKey());
		if ( BrowseAlbumsCommand.MODE_ALBUMS.equals(command.getMode()) ) {
			return getAlbumDao().findAlbumsForUserByDate(user.getUserId(), 0, 
					true, true, false);
		}
		throw new UnsupportedOperationException("Browse mode [" 
				+command.getMode() +"] not supported");
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getUserById(java.lang.Long, magoffin.matt.ma2.biz.BizContext)
	 */
	public User getUserById(Long userId, BizContext context) {
		return userDao.get(userId);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#hasAccessLevel(magoffin.matt.ma2.domain.User, int)
	 */
	public boolean hasAccessLevel(User user, int level) {
		return user != null && ((user.getAccessLevel() & level) == 1);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#forgotPassword(java.lang.String, magoffin.matt.ma2.biz.BizContext)
	 */
	public String forgotPassword(String login, BizContext context)
			throws AuthorizationException {
		User user = userDao.getUserByLogin(login);
		if ( user == null ) {
			throw new AuthorizationException(login,Reason.UNKNOWN_LOGIN);
		}
		
		// change their password to something random
		String rnd = String.valueOf((long)(Math.random()*100000000000L));
		user.setPassword(rnd);
		
		prepareUserForStorage(user);
		userDao.store(user);
		
		// generate confirmation string
		String data = user.getCreationDate().get(Calendar.DAY_OF_YEAR)+rnd;
		String conf = encryptor.encrypt(data);
		if ( log.isDebugEnabled() ) {
			log.debug("Generated user '" +user.getLogin() 
					+"' forgotten password confirmation with confirmation '" +conf +"'");
		}
		return conf;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#confirmForgotPassword(java.lang.String, java.lang.String, java.lang.String, magoffin.matt.ma2.biz.BizContext)
	 */
	public User confirmForgotPassword(String login, String confirmationCode,
			String newPassword, BizContext context)
			throws AuthorizationException {
		User user = userDao.getUserByLogin(login);
		if ( user == null ) {
			throw new AuthorizationException(login,Reason.UNKNOWN_LOGIN);
		}

		// validate confirmation string against user access number
		String decryptedConfCode = null;
		try {
			decryptedConfCode = encryptor.decrypt(confirmationCode);
		} catch ( RuntimeException e ) {
			if ( log.isDebugEnabled() ) {
				log.debug("Exception decrypting confirmation code: " +e.toString());
			}
			throw new AuthorizationException(login,Reason.FORGOTTEN_PASSWORD_NOT_CONFIRMED);
		}
		
		String dayOfYear = String.valueOf(user.getCreationDate().get(Calendar.DAY_OF_YEAR));
		String unencryptedPassword = decryptedConfCode.substring(dayOfYear.length());
		String encryptedPass = MessageDigester.generateDigest(unencryptedPassword,salt);
		
		if ( encryptedPass == null || !encryptedPass.equals(user.getPassword()) ) {
			throw new AuthorizationException(login,Reason.FORGOTTEN_PASSWORD_NOT_CONFIRMED);
		}
		
		// ok, reset user password to new password
		user.setPassword(newPassword);
		
		// in case they have not actually registered before, reset their access level
		if ( user.getAccessLevel() < 0 ) {
			user.setAccessLevel(0);
		}
		
		// store user
		prepareUserForStorage(user);
		return userDao.get(userDao.store(user));
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getUserByAnonymousKey(java.lang.String)
	 */
	public User getUserByAnonymousKey(String key) {
		return userDao.getUserByKey(key);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getUsersWithAccess(int)
	 */
	public List<User> getUsersWithAccess(int level) {
		return userDao.findUsersForAccess(level);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#getAnonymousUser()
	 */
	public User getAnonymousUser() {
		return anonymousUser;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.UserBiz#isAnonymousUser(magoffin.matt.ma2.domain.User)
	 */
	public boolean isAnonymousUser(User user) {
		return user == null || user == anonymousUser;
	}

	/**
	 * @return Returns the encryptor.
	 */
	public DataEncryption getEncryptor() {
		return encryptor;
	}

	/**
	 * @param encryptor The encryptor to set.
	 */
	public void setEncryptor(DataEncryption encryptor) {
		this.encryptor = encryptor;
	}

	/**
	 * @return Returns the salt.
	 */
	public byte[] getSalt() {
		return salt;
	}

	/**
	 * @param salt The salt to set.
	 */
	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	/**
	 * @return Returns the userDao.
	 */
	public UserDao getUserDao() {
		return userDao;
	}

	/**
	 * @param userDao The userDao to set.
	 */
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	/**
	 * @return Returns the userValidator.
	 */
	public Validator getUserValidator() {
		return userValidator;
	}

	/**
	 * @param userValidator The userValidator to set.
	 */
	public void setUserValidator(Validator userValidator) {
		this.userValidator = userValidator;
	}
	
	/**
	 * @return Returns the systemBiz.
	 */
	public SystemBiz getSystemBiz() {
		return systemBiz;
	}
	
	/**
	 * @param systemBiz The systemBiz to set.
	 */
	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}
	
	/**
	 * @return Returns the collectionDao.
	 */
	public CollectionDao getCollectionDao() {
		return collectionDao;
	}
	
	/**
	 * @param collectionDao The collectionDao to set.
	 */
	public void setCollectionDao(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
	}
	
	/**
	 * @return Returns the domainObjectFactory.
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}
	
	/**
	 * @param domainObjectFactory The domainObjectFactory to set.
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}
	
	/**
	 * @return Returns the messages.
	 */
	public MessageSource getMessages() {
		return messages;
	}
	
	/**
	 * @param messages The messages to set.
	 */
	public void setMessages(MessageSource messages) {
		this.messages = messages;
	}
	
	/**
	 * @return Returns the defaultCollectionNameMessageKey.
	 */
	public String getDefaultCollectionNameMessageKey() {
		return defaultCollectionNameMessageKey;
	}
	
	/**
	 * @param defaultCollectionNameMessageKey The defaultCollectionNameMessageKey to set.
	 */
	public void setDefaultCollectionNameMessageKey(
			String defaultCollectionNameMessageKey) {
		this.defaultCollectionNameMessageKey = defaultCollectionNameMessageKey;
	}
	
	/**
	 * @return Returns the defaultCollectionCommentMessageKey.
	 */
	public String getDefaultCollectionCommentMessageKey() {
		return defaultCollectionCommentMessageKey;
	}
	
	/**
	 * @param defaultCollectionCommentMessageKey The defaultCollectionCommentMessageKey to set.
	 */
	public void setDefaultCollectionCommentMessageKey(
			String defaultCollectionCommentMessageKey) {
		this.defaultCollectionCommentMessageKey = defaultCollectionCommentMessageKey;
	}

	/**
	 * @return Returns the albumDao.
	 */
	public AlbumDao getAlbumDao() {
		return albumDao;
	}
	
	/**
	 * @param albumDao The albumDao to set.
	 */
	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}
	
	/**
	 * @return Returns the adminUserTemplate.
	 */
	public Map<String, Object> getAdminUserTemplate() {
		return adminUserTemplate;
	}

	/**
	 * @param adminUserTemplate The adminUserTemplate to set.
	 */
	public void setAdminUserTemplate(Map<String, Object> adminUserTemplate) {
		this.adminUserTemplate = adminUserTemplate;
	}
	
	/**
	 * @return the defaultThumbMediaSize
	 */
	public MediaSize getDefaultThumbSize() {
		return defaultThumbSize;
	}
	
	/**
	 * @param defaultThumbMediaSize the defaultThumbMediaSize to set
	 */
	public void setDefaultThumbSize(MediaSize defaultThumbMediaSize) {
		this.defaultThumbSize = defaultThumbMediaSize;
	}
	
	/**
	 * @return the defaultThumbQuality
	 */
	public MediaQuality getDefaultThumbQuality() {
		return defaultThumbQuality;
	}
	
	/**
	 * @param defaultThumbQuality the defaultThumbQuality to set
	 */
	public void setDefaultThumbQuality(MediaQuality defaultThumbQuality) {
		this.defaultThumbQuality = defaultThumbQuality;
	}
	
	/**
	 * @return the defaultViewMediaSize
	 */
	public MediaSize getDefaultViewSize() {
		return defaultViewSize;
	}
	
	/**
	 * @param defaultViewMediaSize the defaultViewMediaSize to set
	 */
	public void setDefaultViewSize(MediaSize defaultViewMediaSize) {
		this.defaultViewSize = defaultViewMediaSize;
	}
	
	/**
	 * @return the defaultViewQuality
	 */
	public MediaQuality getDefaultViewQuality() {
		return defaultViewQuality;
	}
	
	/**
	 * @param defaultViewQuality the defaultViewQuality to set
	 */
	public void setDefaultViewQuality(MediaQuality defaultViewQuality) {
		this.defaultViewQuality = defaultViewQuality;
	}

	/**
	 * @return the albumFeedMaxLength
	 */
	public int getAlbumFeedMaxLength() {
		return albumFeedMaxLength;
	}

	/**
	 * @param albumFeedMaxLength the albumFeedMaxLength to set
	 */
	public void setAlbumFeedMaxLength(int albumFeedMaxLength) {
		this.albumFeedMaxLength = albumFeedMaxLength;
	}
	
	/**
	 * @return the anonymousUserTemplate
	 */
	public Map<String, Object> getAnonymousUserTemplate() {
		return anonymousUserTemplate;
	}
	
	/**
	 * @param anonymousUserTemplate the anonymousUserTemplate to set
	 */
	public void setAnonymousUserTemplate(Map<String, Object> anonymousUserTemplate) {
		this.anonymousUserTemplate = anonymousUserTemplate;
	}

	/**
	 * @return the watermarkSizes
	 */
	public Set<MediaSize> getWatermarkSizes() {
		return watermarkSizes;
	}

	/**
	 * @param watermarkSizes the watermarkSizes to set
	 */
	public void setWatermarkSizes(Set<MediaSize> watermarkSizes) {
		this.watermarkSizes = watermarkSizes;
	}

	/**
	 * @return the ioBiz
	 */
	public IOBiz getIoBiz() {
		return ioBiz;
	}

	/**
	 * @param ioBiz the ioBiz to set
	 */
	public void setIoBiz(IOBiz ioBiz) {
		this.ioBiz = ioBiz;
	}
	
}
