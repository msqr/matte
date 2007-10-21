/* ===================================================================
 * JAXBDomainObjectFactoryTest.java
 * 
 * Created Sep 19, 2005 3:28:30 PM
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

package magoffin.matt.ma2.biz.impl;

import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.Edit;
import magoffin.matt.ma2.domain.JobInfo;
import magoffin.matt.ma2.domain.KeyNameType;
import magoffin.matt.ma2.domain.Locale;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.MediaItemSearchResult;
import magoffin.matt.ma2.domain.MediaSizeDefinition;
import magoffin.matt.ma2.domain.MediaSpec;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndex;
import magoffin.matt.ma2.domain.PaginationIndexSection;
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
import junit.framework.TestCase;

/**
 * Unit test for {@link magoffin.matt.ma2.biz.impl.JAXBDomainObjectFactory}.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class JAXBDomainObjectFactoryTest extends TestCase {
	
	private static final JAXBDomainObjectFactory dof = new JAXBDomainObjectFactory();

	/**
	 * Test creation of Album.
	 */
	public void testCreateAlbum() {
		Album a = dof.newAlbumInstance();
		assertNotNull(a);
	}
	
	/**
	 * Test creation of Collection.
	 */
	public void testCreateCollection() {
		Collection c = dof.newCollectionInstance();
		assertNotNull(c);
	}
	
	/**
	 * Test creation of Edit.
	 */
	public void testCreateEdit() {
		Edit o = dof.newEditInstance();
		assertNotNull(o);
	}
	
	/**
	 * Test creation of Edit.
	 */
	public void testCreateJobInfo() {
		JobInfo o = dof.newJobInfoInstance();
		assertNotNull(o);
	}
	
	/**
	 * Test creation of MediaItem.
	 */
	public void testCreateMediaItem() {
		MediaItem o = dof.newMediaItemInstance();
		assertNotNull(o);
	}
	
	/**
	 * Test creation of MediaItem.
	 */
	public void testCreateMetadata() {
		Metadata m = dof.newMetadataInstance();
		assertNotNull(m);
	}
	
	/**
	 * Test creation of Model.
	 */
	public void testCreateModel() {
		Model o = dof.newModelInstance();
		assertNotNull(o);
	}
	
	/**
	 * Test creation of Session.
	 */
	public void testCreateSession() {
		Session o = dof.newSessionInstance();
		assertNotNull(o);
	}
	
	/**
	 * Test creation of SharedAlbumSearchResult.
	 */
	public void testCreateSharedAlbumSearchResult() {
		SharedAlbumSearchResult o = dof.newSharedAlbumSearchResultInstance();
		assertNotNull(o);
	}
	
	/**
	 * Test creation of TimeZone.
	 */
	public void testCreateTimeZone() {
		TimeZone o = dof.newTimeZoneInstance();
		assertNotNull(o);
	}
	
	/**
	 * Test creation of User.
	 */
	public void testCreateUser() {
		User u = dof.newUserInstance();
		assertNotNull(u);
	}
	
	/**
	 * Test creation of UserComment.
	 */
	public void testCreateUserComment() {
		UserComment uc = dof.newUserCommentInstance();
		assertNotNull(uc);
	}
	
	/**
	 * Test creation of UserTag.
	 */
	public void testCreateUserTag() {
		UserTag ut = dof.newUserTagInstance();
		assertNotNull(ut);
	}
	
	/**
	 * Test creation of UserSearchResult.
	 */
	public void testCreateUserSearchResult() {
		UserSearchResult u = dof.newUserSearchResultInstance();
		assertNotNull(u);
	}
	
	/**
	 * Test creation of MediaItemSearchResult.
	 */
	public void testCreateMediaItemSearchResult() {
		MediaItemSearchResult u = dof.newMediaItemSearchResultInstance();
		assertNotNull(u);
	}
	
	/**
	 * Test creation of XAppContext.
	 */
	public void testCreateXAppContext() {
		XAppContext xac = dof.newXAppContextInstance();
		assertNotNull(xac);
	}
	
	/**
	 * Test creation of MediaItemRating.
	 */
	public void testCreateMediaItemRating() {
		MediaItemRating rating = dof.newMediaItemRatingInstance();
		assertNotNull(rating);
	}
	
	/**
	 * Test creation of Theme.
	 */
	public void testCreateTheme() {
		Theme theme = dof.newThemeInstance();
		assertNotNull(theme);
	}
	
	/**
	 * Test creation of PaginationCriteria.
	 */
	public void testCreatePaginationCriteria() {
		PaginationCriteria pc = dof.newPaginationCriteriaInstance();
		assertNotNull(pc);
	}
	
	/**
	 * Test creation of PaginationIndex.
	 */
	public void testCreatePaginationIndex() {
		PaginationIndex pi = dof.newPaginationIndexInstance();
		assertNotNull(pi);
	}
	
	/**
	 * Test creation of PaginationIndexSection.
	 */
	public void testCreatePaginationIndexSection() {
		PaginationIndexSection pis = dof.newPaginationIndexSectionInstance();
		assertNotNull(pis);
	}
	
	/**
	 * Test creation of SearchResults.
	 */
	public void testCreateSearchResults() {
		SearchResults sr = dof.newSearchResultsInstance();
		assertNotNull(sr);
	}
	
	/**
	 * Test creation of MediaSpec.
	 */
	public void testCreateMediaSpec() {
		MediaSpec ms = dof.newMediaSpecInstance();
		assertNotNull(ms);
	}
	
	/**
	 * Test creation of MediaSizeDefinition.
	 */
	public void testCreateMediaSizeDefinition() {
		MediaSizeDefinition ms = dof.newMediaSizeDefinitionInstance();
		assertNotNull(ms);
	}
	
	/**
	 * Test creation of KeyNameType.
	 */
	public void testCreateKeyNameType() {
		KeyNameType knt = dof.newKeyNameTypeInstance();
		assertNotNull(knt);
	}
	
	/**
	 * Test creation of XwebParameter.
	 */
	public void testCreateXwebParameterInstance() {
		XwebParameter p = dof.newXwebParameterInstance();
		assertNotNull(p);
	}
	
	/**
	 * Test creation of Locale.
	 */
	public void testCreateLocaleInstance() {
		Locale l = dof.newLocaleInstance();
		assertNotNull(l);
	}
	
}
