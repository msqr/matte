/* ===================================================================
 * AlbumsBrowseModePlugin.java
 * 
 * Created Sep 19, 2007 6:18:11 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

package magoffin.matt.ma2.dao.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndex;
import magoffin.matt.ma2.domain.PaginationIndexSection;
import magoffin.matt.ma2.domain.PosterSearchResult;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.plugin.BrowseModePlugin;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;

import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Implementation of {@link BrowseModePlugin} for a
 * "browse user albums by date".
 * 
 * <p>
 * Also supports the "album feed" mode, for returning only items shareable in
 * feed.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class AlbumsByDateBrowseModePlugin extends AbstractJdbcBrowseModePlugin {

	private static final String[] SUPPORTED_MODES = new String[] { BrowseAlbumsCommand.MODE_ALBUMS };

	private String sqlBrowse;
	private String sqlBrowseTopLevelOrderByClause;
	private String sqlBrowseChildOrderByClause;
	private String sqlBrowseDateRangeWhereClause;
	private String sqlBrowseAllowFeedWhereClause;
	private String sqlBrowseAllowBrowseWhereClause;
	private String sqlBrowseFindYears;

	private MessageFormat sqlBrowseTemplate;

	public boolean supportsMode(String mode) {
		return BrowseAlbumsCommand.MODE_ALBUMS.equals(mode)
				|| BrowseAlbumsCommand.MODE_ALBUM_FEED.equals(mode);
	}

	@Override
	protected void init(ApplicationContext application) {
		super.init(application);
		init();
	}

	public String[] getMessageResourceNames() {
		return null;
	}

	public String[] getSupportedModes() {
		return SUPPORTED_MODES;
	}

	/**
	 * Manual initialization method.
	 * 
	 * <p>
	 * This is used by unit tests.
	 * </p>
	 */
	public void init() {
		this.sqlBrowseTemplate = new MessageFormat(this.sqlBrowse);
	}

	@SuppressWarnings("unchecked")
	public SearchResults find(final BrowseAlbumsCommand command, PaginationCriteria pagination) {
		final SearchResults results = getDomainObjectFactory().newSearchResultsInstance();
		final PaginationIndex index = getDomainObjectFactory().newPaginationIndexInstance();
		results.setIndex(index);
		final User user = getUserBiz().getUserByAnonymousKey(command.getUserKey());
		final List<AlbumSearchResult> albums = new LinkedList<AlbumSearchResult>();
		final Map<Long, AlbumSearchResult> albumMap = new HashMap<Long, AlbumSearchResult>();
		if ( BrowseAlbumsCommand.MODE_ALBUMS.equals(command.getMode()) ) {
			if ( command.getSection() != null ) {
				String latestYear = getJdbcTemplate().query(new PreparedStatementCreator() {

					public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
						PreparedStatement stmt = con.prepareStatement(sqlBrowseFindYears);
						stmt.setLong(1, user.getUserId().longValue());
						stmt.setBoolean(2, true); // anonymous
						stmt.setBoolean(3, true); // browsable
						return stmt;
					}
				}, new ResultSetExtractor<String>() {

					public String extractData(ResultSet rs) throws SQLException, DataAccessException {
						String result = null;
						while ( rs.next() ) {
							PaginationIndexSection section = getDomainObjectFactory()
									.newPaginationIndexSectionInstance();
							String year = rs.getString(1);
							section.setIndexKey(year);
							if ( result == null ) {
								result = year;
								if ( BrowseAlbumsCommand.SECTION_LATEST.equalsIgnoreCase(command
										.getSection()) ) {
									section.setSelected(true);
								}
							}
							if ( year.equals(command.getSection()) ) {
								section.setSelected(true);
							}
							index.getIndexSection().add(section);
						}
						return result;
					}
				});
				if ( BrowseAlbumsCommand.SECTION_LATEST.equalsIgnoreCase(command.getSection()) ) {
					command.setSection(latestYear);
				}
			}
			handleSearchForAlbumsForUserByDate(user.getUserId(), command, true, false, albums, albumMap,
					null);
		} else {
			handleSearchForAlbumsForUserByDate(user.getUserId(), command, false, true, albums, albumMap,
					null);
		}
		results.getAlbum().addAll(albums);
		results.setTotalResults(Long.valueOf(albums.size()));
		results.setReturnedResults(results.getTotalResults());
		return results;
	}

	/**
	 * Generate browse results for a user by recersively querying for each level
	 * of albums, starting with top-level albums, followed by their children,
	 * then their children, etc.
	 * 
	 * <p>
	 * This recursive behaviour allows for the date ordering/filtering to be
	 * just applied to the top-level, and child albums to maintain their inherit
	 * album order within it's parent album. Call this method with a
	 * <em>null</em> <code>parentAlbumIds</code> list to find starting from the
	 * top-level.
	 * </p>
	 * 
	 * @param userId
	 *        the user ID to find
	 * @param cmd
	 *        the search criteria
	 * @param browseOnly
	 *        true for browse only
	 * @param feedOnly
	 *        true for feed only
	 * @param results
	 *        the result list to add top-level results to
	 * @param albumMap
	 *        a Map of album IDs to AlbumSearchResult instances, for populating
	 *        child albums with
	 * @param parentAlbumIds
	 *        a list of parent album IDs to find the children albums for
	 */
	private void handleSearchForAlbumsForUserByDate(final Long userId, final BrowseAlbumsCommand cmd,
			final boolean browseOnly, final boolean feedOnly, final List<AlbumSearchResult> results,
			final Map<Long, AlbumSearchResult> albumMap, final List<Long> parentAlbumIds) {
		final List<Long> currentAlbumIds = new LinkedList<Long>();
		getJdbcTemplate().query(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				Object[] sqlBrowseParameters = new String[3];
				if ( parentAlbumIds == null ) {
					sqlBrowseParameters[0] = "is null";
					sqlBrowseParameters[2] = sqlBrowseTopLevelOrderByClause;
				} else {
					StringBuilder buf = new StringBuilder();
					for ( Long parentId : parentAlbumIds ) {
						if ( buf.length() > 0 ) {
							buf.append(',');
						}
						buf.append(parentId);
					}
					buf.insert(0, "in (");
					buf.append(")");
					sqlBrowseParameters[0] = buf.toString();
					sqlBrowseParameters[2] = sqlBrowseChildOrderByClause;
				}

				StringBuilder where = new StringBuilder();
				if ( browseOnly ) {
					where.append(" ").append(sqlBrowseAllowBrowseWhereClause);
				}
				if ( feedOnly ) {
					where.append(" ").append(sqlBrowseAllowFeedWhereClause);
				}
				if ( cmd.getSection() != null && parentAlbumIds == null ) {
					where.append(" ").append(sqlBrowseDateRangeWhereClause);
				}
				sqlBrowseParameters[1] = where.toString();

				String sql = sqlBrowseTemplate.format(sqlBrowseParameters);
				List<Object> sqlParams = new ArrayList<Object>(8);
				sqlParams.add(userId);
				sqlParams.add(true);
				if (browseOnly) {
					sqlParams.add(browseOnly);
				}
				if (feedOnly) {
					sqlParams.add(feedOnly);
				}
				if (cmd.getSection() != null && parentAlbumIds == null) {
					Integer year = null;
					try {
						year = Integer.valueOf(cmd.getSection());
					} catch (NumberFormatException e) {
						// ignore this
					}
					if (year != null) {
						Calendar c = Calendar.getInstance();
						c.set(year, Calendar.JANUARY, 1, 0, 0, 0);
						c.set(Calendar.MILLISECOND, 0);
						sqlParams.add(new Timestamp(c.getTimeInMillis()));
						c.add(Calendar.YEAR, 1);
						sqlParams.add(new Timestamp(c.getTimeInMillis()));
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("searchForAlbumsForUserByDate with SQL [" + sql
							+ "]\n\tparams: " + sqlParams);
				}
				PreparedStatement psmt = con.prepareStatement(sql);
				int pos = 0;
				for (Object param : sqlParams) {
					psmt.setObject(++pos, param);
				}
				if (parentAlbumIds == null && cmd.getMaxEntries() > 0) {
					psmt.setMaxRows(cmd.getMaxEntries());
				}
				return psmt;
			}
		}, new ResultSetExtractor<Object>() {

			@SuppressWarnings("unchecked")
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				while ( rs.next() ) {
					AlbumSearchResult result = getDomainObjectFactory().newAlbumSearchResultInstance();
					result.setAlbumId(rs.getLong("album_id"));
					albumMap.put(result.getAlbumId(), result);
					currentAlbumIds.add(result.getAlbumId());

					result.setAnonymousKey(rs.getString("album_key"));
					result.setName(rs.getString("album_name"));
					result.setComment(rs.getString("album_comment"));
					Long parentId = rs.getLong("parent_album_id");
					if ( !rs.wasNull() ) {
						AlbumSearchResult parent = albumMap.get(parentId);
						parent.getSearchAlbum().add(result);
					} else {
						parentId = null;
					}

					Calendar albumDate = Calendar.getInstance();
					albumDate.setTimeInMillis(rs.getTimestamp("album_date").getTime());
					result.setAlbumDate(albumDate);

					Timestamp ts = rs.getTimestamp("album_creation_date");
					if ( ts != null ) {
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(ts.getTime());
						result.setCreationDate(cal);
					}

					ts = rs.getTimestamp("album_modify_date");
					if ( ts != null ) {
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(ts.getTime());
						result.setModifyDate(cal);
					}

					Long posterId = rs.getLong("posterid");
					if ( !rs.wasNull() ) {
						PosterSearchResult psr = getDomainObjectFactory()
								.newPosterSearchResultInstance();
						psr.setItemId(posterId);
						psr.setName(rs.getString("poster_name"));
						result.setSearchPoster(psr);
					}

					long itemCount = rs.getLong("item_count");
					result.setItemCount(itemCount);
					if ( itemCount > 0 ) {
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(rs.getDate("item_min_date").getTime());
						result.setItemMinDate(cal);
						cal = Calendar.getInstance();
						cal.setTimeInMillis(rs.getDate("item_max_date").getTime());
						result.setItemMaxDate(cal);
					}

					if ( parentId == null ) {
						results.add(result);
					}
				}
				return null;
			}
		});
		if ( currentAlbumIds.size() > 0 ) {
			// for children albums we don't use browse/feed settings, just
			// pull in all anonymous child albums
			handleSearchForAlbumsForUserByDate(userId, cmd, false, false, results, albumMap,
					currentAlbumIds);
		}
	}

	public String getSqlBrowse() {
		return sqlBrowse;
	}

	public void setSqlBrowse(String sqlBrowse) {
		this.sqlBrowse = sqlBrowse;
	}

	public String getSqlBrowseTopLevelOrderByClause() {
		return sqlBrowseTopLevelOrderByClause;
	}

	public void setSqlBrowseTopLevelOrderByClause(String sqlBrowseTopLevelOrderByClause) {
		this.sqlBrowseTopLevelOrderByClause = sqlBrowseTopLevelOrderByClause;
	}

	public String getSqlBrowseChildOrderByClause() {
		return sqlBrowseChildOrderByClause;
	}

	public void setSqlBrowseChildOrderByClause(String sqlBrowseChildOrderByClause) {
		this.sqlBrowseChildOrderByClause = sqlBrowseChildOrderByClause;
	}

	public String getSqlBrowseAllowFeedWhereClause() {
		return sqlBrowseAllowFeedWhereClause;
	}

	public void setSqlBrowseAllowFeedWhereClause(String sqlBrowseAllowFeedWhereClause) {
		this.sqlBrowseAllowFeedWhereClause = sqlBrowseAllowFeedWhereClause;
	}

	public String getSqlBrowseAllowBrowseWhereClause() {
		return sqlBrowseAllowBrowseWhereClause;
	}

	public void setSqlBrowseAllowBrowseWhereClause(String sqlBrowseAllowBrowseWhereClause) {
		this.sqlBrowseAllowBrowseWhereClause = sqlBrowseAllowBrowseWhereClause;
	}

	public String getSqlBrowseDateRangeWhereClause() {
		return sqlBrowseDateRangeWhereClause;
	}

	public void setSqlBrowseDateRangeWhereClause(String sqlBrowseDateRangeWhereClause) {
		this.sqlBrowseDateRangeWhereClause = sqlBrowseDateRangeWhereClause;
	}

	public String getSqlBrowseFindYears() {
		return sqlBrowseFindYears;
	}

	public void setSqlBrowseFindYears(String sqlBrowseFindYears) {
		this.sqlBrowseFindYears = sqlBrowseFindYears;
	}

}
