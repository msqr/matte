/* ===================================================================
 * DomainObjectFactory.java
 * 
 * Created Sep 19, 2005 3:21:15 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.biz;

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
 * Object factory interface for domain objects.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public interface DomainObjectFactory {

	/**
	 * Get a new Album instance.
	 * @return new Album instance
	 */
	Album newAlbumInstance();
	
	/**
	 * Get a new AlbumImportType instance.
	 * @return new AlbumImportType instance
	 */
	AlbumImportType newAlbumImportTypeInstance();
	
	/**
	 * Get a new AlbumSearchResult instance.
	 * @return new AlbumSearchResult instance
	 */
	AlbumSearchResult newAlbumSearchResultInstance();
	
	/**
	 * Get a new Collection instance.
	 * @return new Collection instance
	 */
	Collection newCollectionInstance();
	
	/**
	 * Get a new CollectionImport instance.
	 * @return new CollectionImport instance
	 */
	CollectionImport newCollectionImportInstance();
	
	/**
	 * Get a new CollectionListItemType instance.
	 * @return new CollectionListItemType instance
	 */
	CollectionListItemType newCollectionListItemTypeInstance();
	
	/**
	 * Get a new Edit instance.
	 * @return new Edit instance
	 */
	Edit newEditInstance();
	
	/**
	 * Get a new GetCollectionListRequest instance.
	 * @return the new GetCollectionListRequest instance
	 */
	GetCollectionListRequest newGetCollectionListRequestInstance();
	
	/**
	 * Get a new GetCollectionListResponse instance.
	 * @return the new GetCollectionListResponse instance
	 */
	GetCollectionListResponse newGetCollectionListResponseInstance();
	
	/**
	 * Get a new ItemImportType instance.
	 * @return new ItemImportType instance
	 */
	ItemImportType newItemImportTypeInstance();
	
	/**
	 * Return a new JobInfo instance.
	 * @return new JobInfo instance
	 */
	JobInfo newJobInfoInstance();
	
	/**
	 * Return a new KeyNameType instance.
	 * @return new KeyNameType instance
	 */
	KeyNameType newKeyNameTypeInstance();
	
	/**
	 * Return a new Locale instance.
	 * @return new Locale instance
	 */
	Locale newLocaleInstance();
	
	/**
	 * Get a new MediaItem instance.
	 * @return new MediaItem instance
	 */
	MediaItem newMediaItemInstance();
	
	/**
	 * Get a new MediaItemRating instance.
	 * @return new MediaItemRating instance
	 */
	MediaItemRating newMediaItemRatingInstance();
	
	/**
	 * Get a new MediaSpec instance.
	 * @return new MediaSpec instance
	 */
	MediaSpec newMediaSpecInstance();
	
	/**
	 * Get a new MediaSizeDefinition instance.
	 * @return new MediaSizeDefinition
	 */
	MediaSizeDefinition newMediaSizeDefinitionInstance();
	
	/**
	 * Get a new Metadata instance.
	 * @return new Metadata instance
	 */
	Metadata newMetadataInstance();
	
	/**
	 * Get a new MetadataImportType instance.
	 * @return new MetadataImportType instance
	 */
	MetadataImportType newMetadataImportTypeInstance();
	
	/**
	 * Get a new Model instance.
	 * @return new Model instance
	 */
	Model newModelInstance();
	
	/**
	 * Get a new PaginationCriteria instance.
	 * 
	 * @return new PaginationCriteria
	 */
	PaginationCriteria newPaginationCriteriaInstance();
	
	/**
	 * Get a new PaginationIndex instance.
	 * 
	 * @return new PaginationIndex
	 */
	PaginationIndex newPaginationIndexInstance();
	
	/**
	 * Get a new PaginationIndexSection instance.
	 * @return new PaginationIndexSection instance
	 */
	PaginationIndexSection newPaginationIndexSectionInstance();
	
	/**
	 * Get a new PosterSearchResult instance.
	 * @return new PosterSearchResult instance
	 */
	PosterSearchResult newPosterSearchResultInstance();
	
	/**
	 * Get a new SearchResults instance.
	 * @return search results
	 */
	SearchResults newSearchResultsInstance();
	
	/**
	 * Get a new Session instance.
	 * @return new Session instance
	 */
	Session newSessionInstance();
	
	/**
	 * Get a new SharedAlbumSearchResult instance.
	 * @return new SharedAlbumSearchResult
	 */
	SharedAlbumSearchResult newSharedAlbumSearchResultInstance();
	
	/**
	 * Get a new Theme instance.
	 * @return new Theme instance
	 */
	Theme newThemeInstance();
	
	/**
	 * Get a new TimeZone instance.
	 * @return new TimeZone instance
	 */
	TimeZone newTimeZoneInstance();
	
	/**
	 * Get a new User instance.
	 * @return new User instance
	 */
	User newUserInstance();
	
	/**
	 * Get a new UserComment instance.
	 * @return new UserComment instance
	 */
	UserComment newUserCommentInstance();
	
	/**
	 * Get a new UserTag instance.
	 * @return new UserTag instance
	 */
	UserTag newUserTagInstance();
	
	/**
	 * Get a new UserSearchResult instance.
	 * @return new UserSearchResult instance
	 */
	UserSearchResult newUserSearchResultInstance();
	
	/**
	 * Get a new MediaItemSearchResult instance.
	 * @return new MediaItemSearchResult instance
	 */
	MediaItemSearchResult newMediaItemSearchResultInstance();
	
	/**
	 * Get a new XAppContext instance.
	 * @return new XAppContext instance
	 */
	XAppContext newXAppContextInstance();
	
	/**
	 * Get a new XwebParameter instance.
	 * @return the XwebParameter instance
	 */
	XwebParameter newXwebParameterInstance();
	
	/**
	 * Clone a domain object.
	 * @param o the object to clone
	 * @return the cloned object
	 */
	Object clone(Object o);
	
}
