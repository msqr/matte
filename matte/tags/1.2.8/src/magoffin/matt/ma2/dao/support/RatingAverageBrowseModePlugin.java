/* ===================================================================
 * RatingAverageBrowseModePlugin.java
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

package magoffin.matt.ma2.dao.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.MediaItemSearchResult;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndex;
import magoffin.matt.ma2.domain.PaginationIndexSection;
import magoffin.matt.ma2.domain.PosterSearchResult;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;

import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.StringUtils;

/**
 * Browse mode based on average media item ratings.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>sqlBrowse</dt>
 *   <dd><p>The SQL to generate the browse results. This must return the aggregate
 *   count of all browsable items, grouped by average user ratings each
 *   item has, e.g. "0.5", "1", etc. These "buckets" will be turned 
 *   into index keys. The SQL must return two columns: a float {@code avg_rating} 
 *   for the average rating bucket, and an integer {@code item_count}
 *   for the count of items that fall within this bucket.</p>
 *   
 *   <p>The following positional SQL parameters will be set:</p>
 *   
 *   <ol>
 *     <li>User ID (long) - the user to display the browsable items for</li>
 *     <li>allow anonymous flag (boolean) - will be set to <em>true</em></li>
 *     <li>allow browse flag (boolean) - will be set to <em>true</em></li>
 *   </ol>
 *   
 *   <p>The boolean flags are set as positional parameters for cross-database
 *   support for boolean values.</p></dd>
 *   
 *   <dt>sqlBrowseSection</dt>
 *   <dd><p>The SQL to generate the results for a single index section (i.e. 
 *   average rating) and also to populate the items in a single album
 *   for viewing a single album. The following positional SQL parameters will be 
 *   set:</p>
 *   
 *   <ol>
 *     <li>User ID (long) - the user to display the browsable items for</li>
 *     <li>allow anonymous flag (boolean) - will be set to <em>true</em></li>
 *     <li>allow browse flag (boolean) - will be set to <em>true</em></li>
 *     <li>average rating section (float) - the average rating section to display</li>
 *     <li>average rating section (float) - the average rating section to display</li>
 *   </ol>
 *   
 *   <p>The average rating section is set twice to allow the SQL to set lower and 
 *   upper bounds for that section.</p>
 *   
 *   <p>The SQL must return the following columns:</p>
 *   
 *   <dl>
 *     <dt>item_id</dt>
 *     <dd>The Long item ID.</dd>
 *     
 *     <dt>item_name</dt>
 *     <dd>The String item name.</dd>
 *     
 *     <dt>item_mime</dt>
 *     <dd>The String item MIME value.</dd>
 *   </dl>
 *   </dd>
 *   
 *   <dt>sectionAlbumMaxSize</dt>
 *   <dd>The maximum album size for items within a single section. This is to break
 *   up large sections into smaller albums. This plugin will generate albums with 
 *   at most this many items in them.</dd>
 *   
 *   <dt>messages</dt>
 *   <dd>The plugin message resources.</dd>
 * </dl>
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class RatingAverageBrowseModePlugin extends AbstractJdbcBrowseModePlugin {
	
	/** Browse mode key for average rating. */
	public static final String MODE_RATING_AVERAGE = "avg-rating";
	
	/** The default value for the {@code sectionAlbumMaxSize} property. */
	public static final int DEFAULT_SECTION_ALBUM_MAX_SIZE = 25;
	
	/** The message key for an album title with just one item in it. */
	public static final String MESSAGE_KEY_ALBUM_TITLE_SINGLE 
		= "rating.album.single.title";

	/** The message key for an album title with more than one item in it. */
	public static final String MESSAGE_KEY_ALBUM_TITLE_MULTI 
		= "rating.album.range.title";
	private static final String[] SUPPORTED_MODES 
		= new String[] {MODE_RATING_AVERAGE};
	private static final String[] MESSAGE_RESOURCE_NAMES 
		= new String[] {"META-INF/RatingAverageBrowseModePlugin-messages"};
	private static final Pattern VIRTUAL_ALBUM_INDEX_KEY 
		= Pattern.compile("(^[0-9.]+):(\\d+)$");

	private String sqlBrowse;
	private String sqlBrowseSection;
	private int sectionAlbumMaxSize = DEFAULT_SECTION_ALBUM_MAX_SIZE;

	private MessageSource messages;

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.BrowseModePlugin#supportsMode(java.lang.String)
	 */
	public boolean supportsMode(String mode) {
		return MODE_RATING_AVERAGE.equalsIgnoreCase(mode);
	}

	@Override
	protected void init(ApplicationContext application) {
		super.init(application);
		init();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#getMessageResourceNames()
	 */
	public String[] getMessageResourceNames() {
		return MESSAGE_RESOURCE_NAMES;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.BrowseModePlugin#getSupportedModes()
	 */
	public String[] getSupportedModes() {
		return SUPPORTED_MODES;
	}

	/**
	 * Manual initialization method.
	 * 
	 * <p>This is used by unit tests.</p>
	 */
	public void init() {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.BrowseModePlugin#find(magoffin.matt.ma2.support.BrowseAlbumsCommand, magoffin.matt.ma2.domain.PaginationCriteria)
	 */
	@SuppressWarnings("unchecked")
	public SearchResults find(BrowseAlbumsCommand command,
			PaginationCriteria pagination) {
		final SearchResults results = getDomainObjectFactory().newSearchResultsInstance();
		final PaginationIndex index = getDomainObjectFactory().newPaginationIndexInstance();
		results.setIndex(index);
		final User user = getUserBiz().getUserByAnonymousKey(command.getUserKey());
		final List<AlbumSearchResult> albums = new LinkedList<AlbumSearchResult>();
		Matcher m = VIRTUAL_ALBUM_INDEX_KEY.matcher(pagination != null 
				&& StringUtils.hasText(pagination.getIndexKey())
				? pagination.getIndexKey() : "");			
		if ( m.matches() ) {
			float avgRatingKey = Float.parseFloat(m.group(1));
			int pageOffset = Integer.parseInt(m.group(2));
			populateAlbums(user.getUserId(), albums, 
					command.getLocale(), m.group(1), avgRatingKey, pageOffset);
		} else {
			handleSearchForAverageRating(user.getUserId(), 
				pagination, albums, index, command.getLocale());
		}

		results.getAlbum().addAll(albums);
		results.setTotalResults(Long.valueOf(albums.size()));
		results.setReturnedResults(results.getTotalResults());
		return results;
	}

	@SuppressWarnings("unchecked")
	private void handleSearchForAverageRating(final Long userId,
			final PaginationCriteria paginationCriteria,
			final List<AlbumSearchResult> albums, final PaginationIndex index,
			final Locale locale) {
		
		// first populate index
		getJdbcTemplate().query(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				if ( log.isDebugEnabled() ) {
					log.debug("AvgRating search with SQL ["
							+sqlBrowse +"]");
				}
				PreparedStatement psmt = con.prepareStatement(sqlBrowse);
				int pos = 1;
				psmt.setLong(pos++, userId.longValue());
				psmt.setBoolean(pos++, true);
				psmt.setBoolean(pos++, true);
				return psmt;
			}	
		}, new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				PaginationIndexSection section = null;
				while ( rs.next() ) {
					float rating = rs.getFloat("avg_rating");
					String albumName = String.format("%.1f", rating);
					section = getDomainObjectFactory().newPaginationIndexSectionInstance();

					long itemCount = rs.getLong("item_count");
					section.setCount((int)itemCount);
					section.setIndexKey(albumName);
					index.getIndexSection().add(section);
				}
				return null;
			}
		});
		
		// now populate section
		final String sectionKey = getSectionKey(paginationCriteria, index);
		if ( sectionKey == null ) {
			return;
		}
		for ( PaginationIndexSection section : (List<PaginationIndexSection>)index.getIndexSection() ) {
			if ( sectionKey.equals(section.getIndexKey()) ) {
				section.setSelected(true);
			}
		}
		final float sectionRating = Float.parseFloat(sectionKey);	
		populateAlbums(userId, albums, locale, sectionKey, sectionRating, -1);
	}

	private void populateAlbums(final Long userId,
			final List<AlbumSearchResult> albums, final Locale locale,
			final String sectionKey, final float sectionRating,
			final int singleAlbumOffset) {
		getJdbcTemplate().query(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				if ( log.isDebugEnabled() ) {
					log.debug("AvgRating section search with SQL ["
							+sqlBrowseSection +"]");
				}
				PreparedStatement psmt = con.prepareStatement(sqlBrowseSection, 
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				int pos = 1;
				psmt.setLong(pos++, userId.longValue());
				psmt.setBoolean(pos++, true);
				psmt.setBoolean(pos++, true);
				psmt.setFloat(pos++, sectionRating);
				psmt.setFloat(pos++, sectionRating);
				
				psmt.setFetchSize(sectionAlbumMaxSize);
				psmt.setFetchDirection(ResultSet.FETCH_FORWARD);
				
				return psmt;
			}	
		}, new ResultSetExtractor() {
			@SuppressWarnings("unchecked")
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				AlbumSearchResult currAlbum = null;
				int currAlbumItemCount = 0;
				if ( singleAlbumOffset > 0 ) {
					rs.relative(singleAlbumOffset*sectionAlbumMaxSize);
				}
				while ( rs.next() ) {
					if ( currAlbum == null || currAlbumItemCount >= sectionAlbumMaxSize ) {
						if ( currAlbum != null ) {
							setAlbumInfo(albums, currAlbum, currAlbumItemCount, 
									sectionKey, locale);
							albums.add(currAlbum);
							currAlbumItemCount = 0;
						}
						currAlbum = getDomainObjectFactory().newAlbumSearchResultInstance();
						
						Long posterId = rs.getLong("item_id");
						PosterSearchResult psr 
							= getDomainObjectFactory().newPosterSearchResultInstance();
						psr.setItemId(posterId);
						psr.setName(rs.getString("item_name"));
						currAlbum.setSearchPoster(psr);
					}

					if ( singleAlbumOffset != -1 ) {
						// populate items into album
						MediaItemSearchResult item = getDomainObjectFactory()
							.newMediaItemSearchResultInstance();
						item.setItemId(rs.getLong("item_id"));
						item.setName(rs.getString("item_name"));
						item.setMime(rs.getString("item_mime"));
						currAlbum.getItem().add(item);
					}
					
					currAlbumItemCount++;
					// rs.relative(sectionAlbumMaxSize);
				}
				if ( currAlbum != null ) {
					setAlbumInfo(albums, currAlbum, currAlbumItemCount, sectionKey, locale);
					albums.add(currAlbum);
				}
				return null;
			}
		});
	}
	
	private String getSectionKey(PaginationCriteria paginationCriteria,
			PaginationIndex index) {
		String sectionKey = null;
		if ( paginationCriteria != null ) {
			sectionKey = paginationCriteria.getIndexKey();
		}
		if ( sectionKey == null &&  index.getIndexSection().size() > 0 ) {
			sectionKey = ((PaginationIndexSection)index.getIndexSection().get(0)).getIndexKey();
		}
		return sectionKey;
	}

	private void setAlbumInfo(List<AlbumSearchResult> albums, AlbumSearchResult album, 
			int albumItemCount, String sectionKey, Locale locale ) {
		int startPos = (1+(albums.size() * sectionAlbumMaxSize));
		int endPos = startPos + albumItemCount - 1;
		if ( albumItemCount == 1 ) {
			album.setName(messages.getMessage(MESSAGE_KEY_ALBUM_TITLE_SINGLE,
					new Object[] {sectionKey, startPos}, locale));
		} else {
			album.setName(messages.getMessage(MESSAGE_KEY_ALBUM_TITLE_MULTI,
					new Object[] {sectionKey, startPos, endPos}, locale));
		}
		album.setItemCount(Long.valueOf(albumItemCount));
		album.setAnonymousKey(sectionKey+":"+albums.size());
	}

	/**
	 * @return the sqlBrowse
	 */
	public String getSqlBrowse() {
		return sqlBrowse;
	}

	/**
	 * @param sqlBrowse the sqlBrowse to set
	 */
	public void setSqlBrowse(String sqlBrowse) {
		this.sqlBrowse = sqlBrowse;
	}

	/**
	 * @return the sqlBrowseSection
	 */
	public String getSqlBrowseSection() {
		return sqlBrowseSection;
	}

	/**
	 * @param sqlBrowseSection the sqlBrowseSection to set
	 */
	public void setSqlBrowseSection(String sqlBrowseSection) {
		this.sqlBrowseSection = sqlBrowseSection;
	}

	/**
	 * @return the sectionAlbumMaxSize
	 */
	public int getSectionAlbumMaxSize() {
		return sectionAlbumMaxSize;
	}

	/**
	 * @param sectionAlbumMaxSize the sectionAlbumMaxSize to set
	 */
	public void setSectionAlbumMaxSize(int sectionAlbumMaxSize) {
		this.sectionAlbumMaxSize = sectionAlbumMaxSize;
	}

	/**
	 * @return the messages
	 */
	public MessageSource getMessages() {
		return messages;
	}

	/**
	 * @param messages the messages to set
	 */
	public void setMessages(MessageSource messages) {
		this.messages = messages;
	}

}
