/* ===================================================================
 * GenericIndexableHibernateDao.java
 * 
 * Created Jul 13, 2006 8:29:01 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.dao.hbm;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import magoffin.matt.dao.BasicIndexData;
import magoffin.matt.dao.IndexCallback;
import magoffin.matt.dao.IndexableDao;
import magoffin.matt.dao.hbm.GenericHibernateDao;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

/**
 * Extension of GenericHibernateDao with indexing support.
 * 
 * @param <T> the domain objec type
 * @param <PK> the primary key type
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public abstract class GenericIndexableHibernateDao<T, PK extends Serializable> 
extends GenericHibernateDao<T,PK> implements IndexableDao<PK> {

	/** The default value for the <code>indexBatchSize</code> property. */
	public static final int DEFAULT_INDEX_BATCH_SIZE = 1000;
	
	/** The default SQL paramter index for the object ID. */
	private static final int DEFAULT_SQL_PARAM_ID_WITH_DATE_RANGE = 3;

	/** Find all objects and related data for reindexing. */
	private String sqlIndexAll;
	
	/** Find all objects and related data for reindexing, within a date range. */
	private String sqlIndexDateRange;
	
	/** The maximum number of objects to index at one time, to conserve memory. */
	private int indexBatchSize = DEFAULT_INDEX_BATCH_SIZE;
	
	/** The SQL parameter index for the object ID in the date range SQL query. */
	private int sqlParamIdxWithDateRange = DEFAULT_SQL_PARAM_ID_WITH_DATE_RANGE;
	
	private RowMapper indexRowMapper = new ColumnMapRowMapper();
	
	/** The JdbcTemplate to use for exectuing raw SQL. */
	private JdbcTemplate jdbcTemplate;
	
	/** The TimeZone to used for indexing operations. */
	private TimeZone indexTimeZone = TimeZone.getDefault();
	
	/** The SQL column name of the object ID. */
	private String indexObjectIdColumnName = "id";
	
	/** The SQL table alias used by the main object table. */
	private String indexObjectTableAlias;
	
	private static final Pattern INSERT_BEFORE_ORDER_BY = Pattern.compile("^(.+)(\\sorder\\sby.+)",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	
	/**
	 * Constructor.
	 * @param type the domain object class
	 */
	public GenericIndexableHibernateDao(Class<T> type) {
		super(type);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.dao.IndexableDao#index(magoffin.matt.dao.IndexCallback)
	 */
	public void index(final IndexCallback<PK> callback) {
		// flush Session in case not flushed to table yet
		getSession().flush();
		
		final BasicIndexData<PK> callbackData = new BasicIndexData<PK>();
		int totalRowsProcessed = 0;
		final MutableInt numRowsProcessed = new MutableInt(0);
		do {
			totalRowsProcessed += numRowsProcessed.intValue();
			numRowsProcessed.setValue(0); // reset count to 0
			jdbcTemplate.query(new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection con) 
				throws SQLException {
					PreparedStatement psmt = null;
					if ( callback.getStartDate() != null && callback.getEndDate() != null ) {
						String sql = sqlIndexDateRange;
						if ( callbackData.getId() != null ) {
							// append "and id > x" to query
							sql = INSERT_BEFORE_ORDER_BY.matcher(sql).replaceFirst(
									"$1 and "+indexObjectTableAlias+"."
									+indexObjectIdColumnName+" > ?$2");
						}
						Format ymdFormat = FastDateFormat.getInstance("yyyy-MM-dd", indexTimeZone);
						if ( log.isInfoEnabled() ) {
							log.info("Executing query to index leads with batch size [" 
									+indexBatchSize +"] and id > [" 
									+(callbackData.getId() == null 
											? "anything" : callbackData.getId())
									+"] between [" 
									+ymdFormat.format(callback.getStartDate()) +"] and ["
									+ymdFormat.format(callback.getEndDate()) +"] with SQL: "
									+sql);
						}
						psmt = con.prepareStatement(sql);
						psmt.setString(1, ymdFormat.format(callback.getStartDate()));
						psmt.setString(2, ymdFormat.format(callback.getEndDate()));
						if ( callbackData.getId() != null ) {
							psmt.setObject(sqlParamIdxWithDateRange, 
									callbackData.getId());
						}
					} else {
						// default sql to index everything
						String sql = sqlIndexAll;
						if ( callbackData.getId() != null ) {
							// append "where id > x" to query
							sql = INSERT_BEFORE_ORDER_BY.matcher(sql).replaceFirst(
									"$1 where "+indexObjectTableAlias+"."
									+indexObjectIdColumnName+" > ?$2");
						}
						if ( log.isInfoEnabled() ) {
							log.info("Executing query to index  with batch size [" 
									+indexBatchSize +"] and id > ["  
									+(callbackData.getId() == null 
											? "anything" : callbackData.getId())
									+"] with SQL: " +sql);
						}
						psmt = con.prepareStatement(sql);
						if ( callbackData.getId() != null ) {
							psmt.setObject(1, callbackData.getId());
						}
					}
					psmt.setMaxRows(indexBatchSize);
					return psmt;
				}	
			}, new RowCallbackHandler() {
				private ResultSet myRs;
				public void processRow(ResultSet rs) throws SQLException {
					numRowsProcessed.setValue(numRowsProcessed.intValue()+1);
					if ( myRs == null ) {
						myRs = rs;
					}
					populateIndexRow(rs,numRowsProcessed.intValue(),callbackData,indexRowMapper);
					callback.handle(callbackData);
				}
			});
		} while ( numRowsProcessed.intValue() > 0 );
		callback.finish();
		if (log.isDebugEnabled() ) {
			log.debug("Processed total of " +totalRowsProcessed +" database rows.");
		}
	}
	
	/**
	 * Default handler for extracting index data.
	 * 
	 * <p>If the RowMapper returns a Map, that Map will be set on the 
	 * BasicIndexData's mapRow property. Otherwise the returned object 
	 * will be set on the mapRow Map under the key <code>rowObject</code>.</p>
	 * 
	 * @param rs the current ResultSet
	 * @param rowNum the row number
	 * @param callbackData the IndexData to populate
	 * @param rowMapper the row mapper
	 * @throws SQLException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	protected void populateIndexRow(ResultSet rs, int rowNum, @SuppressWarnings("rawtypes") BasicIndexData callbackData, RowMapper rowMapper) 
	throws SQLException {
		callbackData.setId(new Long(rs.getLong(this.indexObjectIdColumnName)));
		callbackData.getDataMap().clear();
		Object o = rowMapper.mapRow(rs, rowNum);
		if ( o instanceof Map ) {
			callbackData.setDataMap((Map<String,Object>)o);
		} else {
			callbackData.getDataMap().put(IndexCallback.IndexData.DOMAIN_OBJECT_KEY, o);
		}
	}
	
	/**
	 * @return the indexBatchSize
	 */
	public int getIndexBatchSize() {
		return indexBatchSize;
	}
	
	/**
	 * @param indexBatchSize the indexBatchSize to set
	 */
	public void setIndexBatchSize(int indexBatchSize) {
		this.indexBatchSize = indexBatchSize;
	}
	
	/**
	 * @return the indexRowMapper
	 */
	public RowMapper getIndexRowMapper() {
		return indexRowMapper;
	}
	
	/**
	 * @param indexRowMapper the indexRowMapper to set
	 */
	public void setIndexRowMapper(RowMapper indexRowMapper) {
		this.indexRowMapper = indexRowMapper;
	}
	
	/**
	 * @return the indexTimeZone
	 */
	public TimeZone getIndexTimeZone() {
		return indexTimeZone;
	}
	
	/**
	 * @param indexTimeZone the indexTimeZone to set
	 */
	public void setIndexTimeZone(TimeZone indexTimeZone) {
		this.indexTimeZone = indexTimeZone;
	}
	
	/**
	 * @return the jdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	/**
	 * @return the sqlIndexAll
	 */
	public String getSqlIndexAll() {
		return sqlIndexAll;
	}
	
	/**
	 * @param sqlIndexAll the sqlIndexAll to set
	 */
	public void setSqlIndexAll(String sqlIndexAll) {
		this.sqlIndexAll = sqlIndexAll;
	}
	
	/**
	 * @return the sqlIndexDateRange
	 */
	public String getSqlIndexDateRange() {
		return sqlIndexDateRange;
	}
	
	/**
	 * @param sqlIndexDateRange the sqlIndexDateRange to set
	 */
	public void setSqlIndexDateRange(String sqlIndexDateRange) {
		this.sqlIndexDateRange = sqlIndexDateRange;
	}
	
	/**
	 * @return the sqlParamIdxWithDateRange
	 */
	public int getSqlParamIdxWithDateRange() {
		return sqlParamIdxWithDateRange;
	}
	
	/**
	 * @param sqlParamIdxWithDateRange the sqlParamIdxWithDateRange to set
	 */
	public void setSqlParamIdxWithDateRange(int sqlParamIdxWithDateRange) {
		this.sqlParamIdxWithDateRange = sqlParamIdxWithDateRange;
	}
	
	/**
	 * @return the indexObjectIdColumnName
	 */
	public String getIndexObjectIdColumnName() {
		return indexObjectIdColumnName;
	}
	
	/**
	 * @param indexObjectIdColumnName the indexObjectIdColumnName to set
	 */
	public void setIndexObjectIdColumnName(String indexObjectIdColumnName) {
		this.indexObjectIdColumnName = indexObjectIdColumnName;
	}
	
	/**
	 * @return the indexObjectTableAlias
	 */
	public String getIndexObjectTableAlias() {
		return indexObjectTableAlias;
	}
	
	/**
	 * @param indexObjectTableAlias the indexObjectTableAlias to set
	 */
	public void setIndexObjectTableAlias(String indexObjectTableAlias) {
		this.indexObjectTableAlias = indexObjectTableAlias;
	}
	
}
