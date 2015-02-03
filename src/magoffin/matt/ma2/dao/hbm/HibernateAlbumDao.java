/* ===================================================================
 * HibernateAlbumDao.java
 * 
 * Created Sep 19, 2005 7:19:59 PM
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

package magoffin.matt.ma2.dao.hbm;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import magoffin.matt.dao.hbm.GenericIndexableHibernateDao;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Theme;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate implementation of {@link magoffin.matt.ma2.dao.AlbumDao}.
 * 
 * @author matt.magoffin
 * @version 1.1
 */
public class HibernateAlbumDao extends GenericIndexableHibernateDao<Album, Long> implements AlbumDao {

	/** Find all Albums for a User ID. */
	public static final String QUERY_ALBUMS_FOR_USER_ID = "AlbumsForUserId";

	/** Find all Albums for a User ID and name. */
	public static final String QUERY_ALBUMS_FOR_USER_ID_AND_NAME = "AlbumsForUserIdAndName";

	/** Find all Albums for a User ID, sorted by date in descending order. */
	public static final String QUERY_ALBUMS_FOR_USER_ID_BY_DATE = "AlbumsForUserIdByDate";

	/**
	 * Find all Albums for a User ID with 'allowAnonymous' = TRUE, sorted by
	 * date in descending order.
	 */
	public static final String QUERY_ALBUMS_FOR_USER_ID_FOR_ANONYMOUS_BY_DATE = "AlbumsForUserIdForAnonymousByDate";

	/**
	 * Find all Albums for a User ID with 'allowBrowse' = TRUE, sorted by date
	 * in descending order.
	 */
	public static final String QUERY_ALBUMS_FOR_USER_ID_FOR_BROWSE_BY_DATE = "AlbumsForUserIdForBrowseByDate";

	/**
	 * Find all Albums for a User ID with 'allowFeed' = TRUE, sorted by date in
	 * descending order.
	 */
	public static final String QUERY_ALBUMS_FOR_USER_ID_FOR_FEED_BY_DATE = "AlbumsForUserIdForFeedByDate";

	/**
	 * Find album search results for a User ID with 'allowFeed' = TRUE, sorted
	 * by date in descending order.
	 */
	public static final String SEARCH_ALBUMS_FOR_USER_ID_FOR_FEED_BY_DATE = "AlbumSearchResultsForUserIdForFeedByDate";

	/**
	 * Find all Albums for a User ID, sorted by date in descending order, newer
	 * than a given date.
	 */
	public static final String QUERY_ALBUMS_FOR_USER_ID_BY_DATE_SINCE = "AlbumsForUserIdByDateSince";

	/** Find an Album based on its anonymous key. */
	public static final String QUERY_ALBUM_FOR_KEY = "AlbumForKey";

	/** Find an Album based on a theme ID. */
	public static final String QUERY_ALBUMS_FOR_THEME_ID = "AlbumsForThemeId";

	/**
	 * Find all Albums that are shared and contain a MediaItem.
	 */
	public static final String QUERY_SHARED_ALBUMS_FOR_MEDIA_ITEM = "AlbumsForMediaItem";

	/**
	 * Find the parent Album for a given Album.
	 */
	public static final String QUERY_PARENT_ALBUM_FOR_ALBUM = "AlbumParentForAlbum";

	/** The HQL parameter name for a user ID. */
	public static final String QUERY_PARAM_USER_ID = "userId";

	/** The HQL parameter name for a MediaItem ID. */
	public static final String QUERY_PARAM_ITEM_ID = "itemId";

	/** The HQL parameter name for the "since" date. */
	public static final String QUERY_PARAM_SINCE_DATE = "sinceDate";

	/**
	 * Default constructor.
	 */
	public HibernateAlbumDao() {
		super(Album.class);
	}

	/**
	 * Initialize after properties configured.
	 */
	public void init() {
		// nothing
	}

	@Override
	protected Long getPrimaryKey(Album domainObject) {
		if ( domainObject == null )
			return null;
		return domainObject.getAlbumId();
	}

	public List<Album> findAlbumsForUser(Long userId) {
		return findByNamedQuery(QUERY_ALBUMS_FOR_USER_ID, new Object[] { userId });
	}

	public List<Album> findAlbumsForUserAndName(Long userId, String name) {
		return findByNamedQuery(QUERY_ALBUMS_FOR_USER_ID_AND_NAME,
				new Object[] { userId, name.toLowerCase() });
	}

	public Album getParentAlbum(Long childAlbumId) {
		List<Album> results = findByNamedQuery(QUERY_PARENT_ALBUM_FOR_ALBUM,
				new Object[] { childAlbumId });
		if ( results.size() < 1 ) {
			return null;
		}
		return results.get(0);
	}

	@Override
	public void delete(Album domainObject) {
		Album parent = getParentAlbum(domainObject.getAlbumId());
		if ( parent != null ) {
			// get album to make sure have persistent instance
			domainObject = get(domainObject.getAlbumId());
			parent.getAlbum().remove(domainObject);
		}
		super.delete(domainObject);
	}

	public List<Album> findSharedAlbumsContainingItem(MediaItem item) {
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put(QUERY_PARAM_ITEM_ID, item.getItemId());
		return findByNamedQuery(QUERY_SHARED_ALBUMS_FOR_MEDIA_ITEM, params);
	}

	public List<Album> findAlbumsForUserByDate(Long userId, Calendar since, boolean anonymousOnly,
			boolean browseOnly, boolean feedOnly) {
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put(QUERY_PARAM_USER_ID, userId);
		params.put(QUERY_PARAM_SINCE_DATE, since);
		return findByNamedQuery(QUERY_ALBUMS_FOR_USER_ID_BY_DATE_SINCE, params);
	}

	public List<Album> findAlbumsForUserByDate(Long userId, int max, boolean anonymousOnly,
			boolean browseOnly, boolean feedOnly) {
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put(QUERY_PARAM_USER_ID, userId);
		String queryName = feedOnly ? QUERY_ALBUMS_FOR_USER_ID_FOR_FEED_BY_DATE
				: (browseOnly ? QUERY_ALBUMS_FOR_USER_ID_FOR_BROWSE_BY_DATE
						: (anonymousOnly ? QUERY_ALBUMS_FOR_USER_ID_FOR_ANONYMOUS_BY_DATE
								: QUERY_ALBUMS_FOR_USER_ID_BY_DATE));
		return findByNamedQuery(queryName, params, 0, max);
	}

	public Album getAlbumForKey(String anonymousKey) {
		List<Album> results = findByNamedQuery(QUERY_ALBUM_FOR_KEY, new Object[] { anonymousKey });
		if ( results.size() < 1 )
			return null;
		return results.get(0);
	}

	public Album getAlbumWithItems(Long albumId) {
		Album a = get(albumId);
		if ( a != null ) {
			fillInAlbumItems(a);
		}
		return a;
	}

	public int reassignAlbumsUsingTheme(final Theme oldTheme, final Theme newTheme) {
		return getHibernateTemplate().execute(new HibernateCallback<Integer>() {

			public Integer doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults albums = session.getNamedQuery(QUERY_ALBUMS_FOR_THEME_ID)
						.setLong("themeId", oldTheme.getThemeId()).setCacheMode(CacheMode.IGNORE)
						.scroll(ScrollMode.FORWARD_ONLY);
				int count = 0;
				while ( albums.next() ) {
					Album album = (Album) albums.get(0);
					album.setTheme(newTheme);
					if ( ++count % 20 == 0 ) {
						session.flush();
						session.clear();
					}
				}
				return count;
			}
		});
	}

	private void fillInAlbumItems(Album a) {
		// FIXME why does this not work: getHibernateTemplate().initialize(a.getItem());
		a.getItem().size();
	}

}
