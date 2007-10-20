/* ===================================================================
 * AbstractSearchBiz.java
 * 
 * Created Jun 27, 2007 8:30:31 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndex;
import magoffin.matt.ma2.domain.PosterSearchResult;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.plugin.BrowseModePlugin;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;

import org.springframework.beans.BeanUtils;

/**
 * Base implementation of {@link SearchBiz} that builds on DAO searching.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public abstract class AbstractSearchBiz implements SearchBiz {

	private DomainObjectFactory domainObjectFactory;
	private UserBiz userBiz;
	private AlbumDao albumDao;
	private SystemBiz systemBiz;

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SearchBiz#findAlbumsForBrowsing(magoffin.matt.ma2.support.BrowseAlbumsCommand, magoffin.matt.ma2.domain.PaginationCriteria, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public SearchResults findAlbumsForBrowsing(final BrowseAlbumsCommand command, PaginationCriteria pagination, BizContext context) {
		// look for BrowseModePlugin to support this mode
		List<BrowseModePlugin> browsePlugins = systemBiz.getPluginsOfType(
				BrowseModePlugin.class);
		for ( BrowseModePlugin plugin : browsePlugins ) {
			if ( plugin.supportsMode(command.getMode()) ) {
				return plugin.find(command, pagination);
			}
		}
		throw new UnsupportedOperationException("Browse mode [" 
				+command.getMode() +"] not supported");
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SearchBiz#findAlbums(magoffin.matt.ma2.biz.SearchBiz.AlbumSearchCriteria, magoffin.matt.ma2.domain.PaginationCriteria, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public SearchResults findAlbums(AlbumSearchCriteria criteria, PaginationCriteria pagination, BizContext context) {
		final SearchResults results = domainObjectFactory.newSearchResultsInstance();
		final PaginationIndex index = domainObjectFactory.newPaginationIndexInstance();
		results.setIndex(index);
		final long start = System.currentTimeMillis();
		List<AlbumSearchResult> albums = null;
		if ( criteria.getAlbumId() != null ) {
			// get specific album, and children
			Album album = albumDao.get(criteria.getAlbumId());
			AlbumSearchResult sr = createAlbumSearchResults(album);
			if ( sr != null ) {
				albums = new LinkedList<AlbumSearchResult>();
				albums.add(sr);
			}
		}
		if ( albums != null ) {
			results.getAlbum().addAll(albums);
			results.setTotalResults(Long.valueOf(albums.size()));
			results.setReturnedResults(results.getTotalResults());
		}
		results.setSearchTime(System.currentTimeMillis() - start);
		return results;
	}

	@SuppressWarnings("unchecked")
	private AlbumSearchResult createAlbumSearchResults(Album album) {
		AlbumSearchResult sr = domainObjectFactory.newAlbumSearchResultInstance();
		BeanUtils.copyProperties(album, sr, new String[] {
				"album", "item", "poster"
		});
		if ( album.getPoster() != null ) {
			PosterSearchResult psr = createPosterSearchResult(album.getPoster());
			sr.setSearchPoster(psr);
		} else if ( album.getItem().size() > 0 ) {
			PosterSearchResult psr = createPosterSearchResult(
				(MediaItem)album.getItem().get(0));
			sr.setSearchPoster(psr);
		}
		sr.setItemCount(Long.valueOf(album.getItem().size()));
		if ( album.getItem().size() > 0 ) {
			for ( MediaItem item : (List<MediaItem>)album.getItem() ) {
				Calendar itemDate = item.getItemDate() != null
					? item.getItemDate() : item.getCreationDate();
				if ( sr.getItemMinDate() == null 
						|| itemDate.before(sr.getItemMinDate()) ) {
					sr.setItemMinDate(itemDate);
				}
				if ( sr.getItemMaxDate() == null
						|| itemDate.after(sr.getItemMaxDate()) ) {
					sr.setItemMaxDate(itemDate);
				}
			}
		}
		for ( Album child : (List<Album>)album.getAlbum() ) {
			AlbumSearchResult srChild = createAlbumSearchResults(child);
			sr.getSearchAlbum().add(srChild);
		}
		return sr;
	}

	private PosterSearchResult createPosterSearchResult(MediaItem poster) {
		PosterSearchResult sr = domainObjectFactory
			.newPosterSearchResultInstance();
		sr.setItemId(poster.getItemId());
		sr.setName(poster.getName());
		return sr;
	}

	/**
	 * @return the domainObjectFactory
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory the domainObjectFactory to set
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}
	
	/**
	 * @return the albumDao
	 */
	public AlbumDao getAlbumDao() {
		return albumDao;
	}
	
	/**
	 * @param albumDao the albumDao to set
	 */
	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}

	/**
	 * @return the systemBiz
	 */
	public SystemBiz getSystemBiz() {
		return systemBiz;
	}

	/**
	 * @param systemBiz the systemBiz to set
	 */
	public void setSystemBiz(SystemBiz systemBiz) {
		this.systemBiz = systemBiz;
	}

}
