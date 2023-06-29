/* ===================================================================
 * JAXBDomainObjectFactory.java
 * 
 * Created Sep 19, 2005 3:23:22 PM
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
 */

package magoffin.matt.ma2.biz.impl;

import javax.xml.bind.JAXBException;
import org.springframework.beans.BeanUtils;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumImportType;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.CollectionImport;
import magoffin.matt.ma2.domain.CollectionListItemType;
import magoffin.matt.ma2.domain.Edit;
import magoffin.matt.ma2.domain.GetCollectionListRequest;
import magoffin.matt.ma2.domain.GetCollectionListResponse;
import magoffin.matt.ma2.domain.ItemImportType;
import magoffin.matt.ma2.domain.JobInfo;
import magoffin.matt.ma2.domain.KeyNameType;
import magoffin.matt.ma2.domain.Locale;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.MediaItemSearchResult;
import magoffin.matt.ma2.domain.MediaSizeDefinition;
import magoffin.matt.ma2.domain.MediaSpec;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.MetadataImportType;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.ObjectFactory;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndex;
import magoffin.matt.ma2.domain.PaginationIndexSection;
import magoffin.matt.ma2.domain.PosterSearchResult;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.Session;
import magoffin.matt.ma2.domain.SharedAlbumSearchResult;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserComment;
import magoffin.matt.ma2.domain.UserSearchResult;
import magoffin.matt.ma2.domain.UserTag;
import magoffin.matt.xweb.XAppContext;
import magoffin.matt.xweb.XwebParameter;

/**
 * JAXB implementation of {@link magoffin.matt.ma2.biz.DomainObjectFactory}.
 * 
 * @author matt.magoffin
 * @version 1.1
 */
public class JAXBDomainObjectFactory implements DomainObjectFactory {

	private static final ObjectFactory MA2_OBJECT_FACTORY = new ObjectFactory();
	private static final magoffin.matt.xweb.ObjectFactory XWEB_OBJECT_FACTORY = new magoffin.matt.xweb.ObjectFactory();

	private String[] albumPropertiesDoNotClone = new String[] { "item", "album", "poster" };
	private String[] collectionPropertiesDoNotClone = new String[] { "item" };
	private String[] mediaItemPropertiesDoNotClone = new String[] { "metadata", "userComment",
			"userRating", "userTag" };
	private final String[] userPropertiesDoNotClone = new String[0];

	@Override
	public Album newAlbumInstance() {
		try {
			return MA2_OBJECT_FACTORY.createAlbum();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AlbumImportType newAlbumImportTypeInstance() {
		try {
			return MA2_OBJECT_FACTORY.createAlbumImportType();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AlbumSearchResult newAlbumSearchResultInstance() {
		try {
			return MA2_OBJECT_FACTORY.createAlbumSearchResult();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection newCollectionInstance() {
		try {
			return MA2_OBJECT_FACTORY.createCollection();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CollectionImport newCollectionImportInstance() {
		try {
			return MA2_OBJECT_FACTORY.createCollectionImport();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public User newUserInstance() {
		try {
			return MA2_OBJECT_FACTORY.createUser();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UserSearchResult newUserSearchResultInstance() {
		try {
			return MA2_OBJECT_FACTORY.createUserSearchResult();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MediaItemSearchResult newMediaItemSearchResultInstance() {
		try {
			return MA2_OBJECT_FACTORY.createMediaItemSearchResult();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public XAppContext newXAppContextInstance() {
		try {
			return XWEB_OBJECT_FACTORY.createXAppContext();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Edit newEditInstance() {
		try {
			return MA2_OBJECT_FACTORY.createEdit();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JobInfo newJobInfoInstance() {
		try {
			return MA2_OBJECT_FACTORY.createJobInfo();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MediaItem newMediaItemInstance() {
		try {
			return MA2_OBJECT_FACTORY.createMediaItem();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MediaSpec newMediaSpecInstance() {
		try {
			return MA2_OBJECT_FACTORY.createMediaSpec();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MediaSizeDefinition newMediaSizeDefinitionInstance() {
		try {
			return MA2_OBJECT_FACTORY.createMediaSizeDefinition();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Metadata newMetadataInstance() {
		try {
			return MA2_OBJECT_FACTORY.createMetadata();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MetadataImportType newMetadataImportTypeInstance() {
		try {
			return MA2_OBJECT_FACTORY.createMetadataImportType();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Model newModelInstance() {
		try {
			return MA2_OBJECT_FACTORY.createModel();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Session newSessionInstance() {
		try {
			return MA2_OBJECT_FACTORY.createSession();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SharedAlbumSearchResult newSharedAlbumSearchResultInstance() {
		try {
			return MA2_OBJECT_FACTORY.createSharedAlbumSearchResult();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TimeZone newTimeZoneInstance() {
		try {
			return MA2_OBJECT_FACTORY.createTimeZone();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MediaItemRating newMediaItemRatingInstance() {
		try {
			return MA2_OBJECT_FACTORY.createMediaItemRating();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Theme newThemeInstance() {
		try {
			return MA2_OBJECT_FACTORY.createTheme();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PaginationCriteria newPaginationCriteriaInstance() {
		try {
			return MA2_OBJECT_FACTORY.createPaginationCriteria();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PaginationIndex newPaginationIndexInstance() {
		try {
			return MA2_OBJECT_FACTORY.createPaginationIndex();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PaginationIndexSection newPaginationIndexSectionInstance() {
		try {
			return MA2_OBJECT_FACTORY.createPaginationIndexSection();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PosterSearchResult newPosterSearchResultInstance() {
		try {
			return MA2_OBJECT_FACTORY.createPosterSearchResult();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SearchResults newSearchResultsInstance() {
		try {
			return MA2_OBJECT_FACTORY.createSearchResults();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UserComment newUserCommentInstance() {
		try {
			return MA2_OBJECT_FACTORY.createUserComment();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UserTag newUserTagInstance() {
		try {
			return MA2_OBJECT_FACTORY.createUserTag();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public KeyNameType newKeyNameTypeInstance() {
		try {
			return MA2_OBJECT_FACTORY.createKeyNameType();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Locale newLocaleInstance() {
		try {
			return MA2_OBJECT_FACTORY.createLocale();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public XwebParameter newXwebParameterInstance() {
		try {
			return XWEB_OBJECT_FACTORY.createXwebParameter();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public GetCollectionListRequest newGetCollectionListRequestInstance() {
		try {
			return MA2_OBJECT_FACTORY.createGetCollectionListRequest();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public GetCollectionListResponse newGetCollectionListResponseInstance() {
		try {
			return MA2_OBJECT_FACTORY.createGetCollectionListResponse();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ItemImportType newItemImportTypeInstance() {
		try {
			return MA2_OBJECT_FACTORY.createItemImportType();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CollectionListItemType newCollectionListItemTypeInstance() {
		try {
			return MA2_OBJECT_FACTORY.createCollectionListItemType();
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object clone(Object original) {
		if ( original == null ) {
			return null;
		}
		Class<?> clazz = original.getClass();
		if ( MediaItem.class.isAssignableFrom(clazz) ) {
			return clone((MediaItem) original);
		} else if ( User.class.isAssignableFrom(clazz) ) {
			return clone((User) original);
		} else if ( Collection.class.isAssignableFrom(clazz) ) {
			return clone((Collection) original);
		} else if ( Album.class.isAssignableFrom(clazz) ) {
			return clone((Album) original);
		}

		throw new IllegalArgumentException(
				"The object [" + original.getClass().getName() + "] is not supported for cloneing.");
	}

	@SuppressWarnings("unchecked")
	private Album clone(Album original) {
		Album cloned = newAlbumInstance();
		BeanUtils.copyProperties(original, cloned, albumPropertiesDoNotClone);

		// map time zones to local time, because no tz stored in DB
		if ( cloned.getAlbumDate() != null ) {
			cloned.getAlbumDate().setTimeZone(java.util.TimeZone.getDefault());
		}
		if ( cloned.getCreationDate() != null ) {
			cloned.getCreationDate().setTimeZone(java.util.TimeZone.getDefault());
		}
		if ( cloned.getModifyDate() != null ) {
			cloned.getModifyDate().setTimeZone(java.util.TimeZone.getDefault());
		}

		// clone children
		for ( int i = 0, length = original.getAlbum().size(); i < length; i++ ) {
			Album child = (Album) original.getAlbum().get(i);
			assert child != null : "Child album " + i + " is null in album [" + original.getAlbumId()
					+ " - " + original.getName() + ']';
			cloned.getAlbum().add(clone(child));
		}

		// clone poster
		if ( original.getPoster() != null ) {
			cloned.setPoster(clone(original.getPoster()));
		}

		return cloned;
	}

	private Collection clone(Collection original) {
		Collection cloned = newCollectionInstance();
		BeanUtils.copyProperties(original, cloned, collectionPropertiesDoNotClone);
		return cloned;
	}

	private MediaItem clone(MediaItem original) {
		MediaItem cloned = newMediaItemInstance();
		BeanUtils.copyProperties(original, cloned, mediaItemPropertiesDoNotClone);

		// map time zones to local time, because no tz stored in DB
		if ( cloned.getItemDate() != null ) {
			cloned.getItemDate().setTimeZone(java.util.TimeZone.getDefault());
		}
		if ( cloned.getCreationDate() != null ) {
			cloned.getCreationDate().setTimeZone(java.util.TimeZone.getDefault());
		}
		if ( cloned.getModifyDate() != null ) {
			cloned.getModifyDate().setTimeZone(java.util.TimeZone.getDefault());
		}

		return cloned;
	}

	private User clone(User original) {
		User cloned = newUserInstance();
		BeanUtils.copyProperties(original, cloned, userPropertiesDoNotClone);
		return cloned;
	}

	/**
	 * @return the albumPropertiesDoNotClone
	 */
	public String[] getAlbumPropertiesDoNotClone() {
		return albumPropertiesDoNotClone;
	}

	/**
	 * @param albumPropertiesDoNotClone
	 *        the albumPropertiesDoNotClone to set
	 */
	public void setAlbumPropertiesDoNotClone(String[] albumPropertiesDoNotClone) {
		this.albumPropertiesDoNotClone = albumPropertiesDoNotClone;
	}

	/**
	 * @return the collectionPropertiesDoNotClone
	 */
	public String[] getCollectionPropertiesDoNotClone() {
		return collectionPropertiesDoNotClone;
	}

	/**
	 * @param collectionPropertiesDoNotClone
	 *        the collectionPropertiesDoNotClone to set
	 */
	public void setCollectionPropertiesDoNotClone(String[] collectionPropertiesDoNotClone) {
		this.collectionPropertiesDoNotClone = collectionPropertiesDoNotClone;
	}

	/**
	 * @return the mediaItemPropertiesDoNotClone
	 */
	public String[] getMediaItemPropertiesDoNotClone() {
		return mediaItemPropertiesDoNotClone;
	}

	/**
	 * @param mediaItemPropertiesDoNotClone
	 *        the mediaItemPropertiesDoNotClone to set
	 */
	public void setMediaItemPropertiesDoNotClone(String[] mediaItemPropertiesDoNotClone) {
		this.mediaItemPropertiesDoNotClone = mediaItemPropertiesDoNotClone;
	}

}
