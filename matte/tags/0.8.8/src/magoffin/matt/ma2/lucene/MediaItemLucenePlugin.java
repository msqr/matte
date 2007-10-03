/* ===================================================================
 * MediaItemLucenePlugin.java
 * 
 * Created Oct 8, 2006 7:18:10 PM
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
 * $Id: MediaItemLucenePlugin.java,v 1.26 2007/10/01 00:27:25 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.lucene;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magoffin.matt.dao.BasicBatchOptions;
import magoffin.matt.dao.BatchableDao.BatchCallbackResult;
import magoffin.matt.dao.BatchableDao.BatchMode;
import magoffin.matt.lucene.IndexEvent;
import magoffin.matt.lucene.IndexResults;
import magoffin.matt.lucene.LuceneServiceUtils;
import magoffin.matt.lucene.SearchCriteria;
import magoffin.matt.lucene.SearchMatch;
import magoffin.matt.lucene.LuceneService.IndexWriterOp;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.MediaItemSearchResult;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.SharedAlbumSearchResult;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserTag;
import magoffin.matt.util.DelegatingInvocationHandler;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.util.StringUtils;

/**
 * Lucene search plugin implementation for {@link MediaItem} objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.26 $ $Date: 2007/10/01 00:27:25 $
 */
public class MediaItemLucenePlugin extends AbstractLucenePlugin {

	private MediaItemDao mediaItemDao = null;
	private AlbumDao albumDao = null;
	private CollectionDao collectionDao = null;
	private boolean singleThreaded = false;
	
	private final Logger log = Logger.getLogger(MediaItemLucenePlugin.class);

	/**
	 * Default constructor.
	 */
	public MediaItemLucenePlugin() {
		super();
		setIndexType(IndexType.MEDIA_ITEM.toString());
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#build(org.apache.lucene.document.Document)
	 */
	@SuppressWarnings("unchecked")
	public SearchMatch build(Document doc) {
		MediaItemSearchResult searchResult = getDomainObjectFactory().newMediaItemSearchResultInstance();
		searchResult.setItemId(Long.valueOf(doc.get(IndexField.ITEM_ID.getFieldName())));
		searchResult.setName(doc.get(IndexField.ITEM_NAME.getFieldName()));
		searchResult.setDescription(doc.get(IndexField.DESCRIPTION.getFieldName()));
		
		Date cDate = getLucene().parseDate(doc.get(IndexField.CREATED_DATE.getFieldName()));
		if ( cDate != null ) {
			Calendar cal = Calendar.getInstance(getLucene().getIndexTimeZone());
			cal.setTime(cDate);
			searchResult.setCreationDate(cal);
		}
		
		// handle item date
		String itemDate = doc.get(IndexField.ITEM_DATE.getFieldName());
		if ( itemDate != null ) {
			TimeZone tz = TimeZone.getTimeZone(doc.get(IndexField.ITEM_DATE_TIME_ZONE.getFieldName()));
			Date iDate = getLucene().parseDate(itemDate, tz);
			Calendar cal = Calendar.getInstance(tz);
			cal.setTime(iDate);
			searchResult.setItemDate(cal);
		}
		
		searchResult.setMime(doc.get(IndexField.MEDIA_MIME.getFieldName()));
		searchResult.setWidth(Integer.parseInt(
				doc.get(IndexField.MEDIA_WIDTH.getFieldName())));
		searchResult.setHeight(Integer.parseInt(
				doc.get(IndexField.MEDIA_HEIGHT.getFieldName())));
		
		// handle tags/ratings, have to find all tag_userId fields
		List<Field> fields = doc.getFields();
		for ( Field f : fields ) {
			Pattern fieldPat = Pattern.compile(IndexField.TAG.getFieldName()+"_(\\d+)");
			Matcher matcher = fieldPat.matcher(f.name());
			if ( matcher.matches() ) {
				UserTag userTag = getDomainObjectFactory().newUserTagInstance();
				String tags = f.stringValue();
				userTag.setTag(tags);

				Long userId = Long.valueOf(matcher.group(1));
				User taggingUser = getDomainObjectFactory().newUserInstance();
				taggingUser.setUserId(userId);
				userTag.setTaggingUser(taggingUser);
				searchResult.getUserTag().add(userTag);
				continue;
			}
			fieldPat = Pattern.compile(IndexField.MEDIA_RATING.getFieldName()+"_(\\d+)");
			matcher = fieldPat.matcher(f.name());
			if ( matcher.matches() ) {
				MediaItemRating rating = getDomainObjectFactory().newMediaItemRatingInstance();
				rating.setRating(Short.parseShort(f.stringValue()));

				Long userId = Long.valueOf(matcher.group(1));
				User ratingUser = getDomainObjectFactory().newUserInstance();
				ratingUser.setUserId(userId);
				rating.setRatingUser(ratingUser);
				searchResult.getUserRating().add(ratingUser);
			}
		}
		
		// handle metadata
		String[] metas = doc.getValues(IndexField.METADATA.getFieldName());
		if ( metas != null ) {
			for ( String metaStr : metas ) {
				int idx = metaStr.indexOf(':');
				Metadata meta = getDomainObjectFactory().newMetadataInstance();
				meta.setKey(metaStr.substring(0, idx));
				meta.setValue(metaStr.substring(idx+1));
				searchResult.getMetadata().add(meta);
			}
		}
		
		// TODO handle user ratings
		
		// handle shared albums
		String[] sharedAlbumKeys = doc.getValues(
				IndexField.MEDIA_SHARED_ALBUM_KEY.getFieldName());
		if ( sharedAlbumKeys != null ) {
			String[] sharedAlbumNames = doc.getValues(
					IndexField.MEDIA_SHARED_ALBUM_NAME.getFieldName());
			for ( int i = 0; i < sharedAlbumKeys.length; i++ ) {
				SharedAlbumSearchResult album = getDomainObjectFactory()
					.newSharedAlbumSearchResultInstance();
				album.setAnonymousKey(sharedAlbumKeys[i]);
				if ( i < sharedAlbumNames.length ) {
					album.setName(sharedAlbumNames[i]);
				}
				searchResult.getSharedAlbum().add(album);
			}
		}
		
		if ( SearchMatch.class.isAssignableFrom(searchResult.getClass()) ) {
			return (SearchMatch)searchResult;
		}
		SearchMatch match = (SearchMatch)DelegatingInvocationHandler.wrapObject(
				searchResult, SearchMatch.class);
		return match;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#getIdForObject(java.lang.Object)
	 */
	public Object getIdForObject(Object object) {
		if ( object instanceof MediaItem ) {
			return ((MediaItem)object).getItemId();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#getNativeQuery(magoffin.matt.lucene.SearchCriteria)
	 */
	@SuppressWarnings("unchecked")
	public Object getNativeQuery(SearchCriteria criteria) {
		MediaItemLuceneSearchCriteria crit = (MediaItemLuceneSearchCriteria)criteria;
		SearchBiz.MediaItemSearchCriteria itemCriteria = crit.getMediaItemCriteria();
		MediaItem template = crit.getMediaItemCriteria().getMediaItemTemplate();

		BooleanQuery root = new BooleanQuery();
		if ( itemCriteria.getUserId() != null ) {
			root.add(new TermQuery(new Term(IndexField.ITEM_OWNER.getFieldName(), 
					itemCriteria.getUserId().toString())), Occur.MUST);
		}
		
		if ( itemCriteria.isSharedOnly() ) {
			root.add(new TermQuery(new Term(
					IndexField.MEDIA_SHARED_FLAG.getFieldName(), "1")), Occur.MUST);
		}
		
		if ( template != null && template.getItemId() != null ) {
			// find by item ID
			root.add(new TermQuery(new Term(IndexField.ITEM_ID.getFieldName(), 
					template.getItemId().toString())), Occur.MUST);
			return root;
		}
		
		if ( itemCriteria.getStartDate() != null || itemCriteria.getEndDate() != null ) {
			// add date range, granular to day
			root.add(getDateRangeQuery(IndexField.ITEM_DATE, 
					IndexField.ITEM_DATE_MONTH, itemCriteria), Occur.MUST);
		}

		// now start "should" gropuing, of which one "must" match
		BooleanQuery should = new BooleanQuery();
		if ( StringUtils.hasText(itemCriteria.getQuickSearch()) ) {
			should.add(getLucene().parseQuery(getIndexType(), 
					itemCriteria.getQuickSearch()), Occur.SHOULD);
		}
		if ( template != null && template.getUserTag().size() > 0 ) {
			for ( UserTag tag : (List<UserTag>)template.getUserTag() ) {
				String tagField = getUserTagFieldName(tag);
				getLucene().addTokenizedTermQuery(should, tag.getTag(), 
						tagField, getIndexType());
			}
		}
		root.add(should, Occur.MUST);
		return root;
	}

	private String getUserTagFieldName(UserTag tag) {
		String tagField = IndexField.TAG.getFieldName();
		if ( tag.getTaggingUser() != null ) {
			tagField += "_" +tag.getTaggingUser().getUserId().toString();
		}
		return tagField;
	}

	private String getUserRatingFieldName(MediaItemRating rating) {
		String name = IndexField.MEDIA_RATING.getFieldName();
		if ( rating.getRatingUser() != null ) {
			name += "_" +rating.getRatingUser().getUserId().toString();
		}
		return name;
	}

	private Query getDateRangeQuery(IndexField dayField, IndexField monthField, 
			SearchBiz.MediaItemSearchCriteria criteria) {
		Calendar start = criteria.getStartDate();
		Calendar end = criteria.getEndDate();
		
		// check if using start and end of month, and thus can use month dates shortcut
		if ( start != null && end != null ) {
			if ( start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
					&& start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)) {
				// same day, use single day term
				return new TermQuery(new Term(dayField.getFieldName(),
						getLucene().formatDateToDay(start.getTime())));
			} else if ( start.get(Calendar.DAY_OF_MONTH) != 1 
					|| end.get(Calendar.DAY_OF_MONTH) 
						!= end.getActualMaximum(Calendar.DAY_OF_MONTH)) {
				// need to use day range
				return new ConstantScoreRangeQuery(
						dayField.getFieldName(),
						getLucene().formatDateToDay(start.getTime()),
						getLucene().formatDateToDay(end.getTime()),
						true, true);
			} else if (start.get(Calendar.MONTH) != end.get(Calendar.MONTH) 
					|| start.get(Calendar.YEAR) != end.get(Calendar.YEAR) ) {
				// multiple months, use month range
				return new ConstantScoreRangeQuery(
						monthField.getFieldName(),
						getLucene().formatDateToMonth(start.getTime()),
						getLucene().formatDateToMonth(end.getTime()),
						true, true);
			} else {
				// single month, use single month term
				return new TermQuery(
						new Term(monthField.getFieldName(),
						getLucene().formatDateToMonth(start.getTime())));
			}
		} else if ( end == null && start.get(Calendar.DAY_OF_MONTH) == 1) {
			// use open ended month range
			return new ConstantScoreRangeQuery(
					monthField.getFieldName(),
					getLucene().formatDateToMonth(start.getTime()),
					null, true, false);
		} else if ( end == null ) {
			// use open ended day range
			return new ConstantScoreRangeQuery(
					dayField.getFieldName(),
					getLucene().formatDateToDay(start.getTime()),
					null, true, false);
		} else if ( start == null && end.get(Calendar.DAY_OF_MONTH)
				== end.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			// use open starting month range
			return new ConstantScoreRangeQuery(
					monthField.getFieldName(),
					null,
					getLucene().formatDateToMonth(end.getTime()),
					false, true);
		}
		// use open starting day range
		return new ConstantScoreRangeQuery(
				dayField.getFieldName(),
				null,
				getLucene().formatDateToDay(end.getTime()),
				false, true);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#index(java.lang.Iterable)
	 */
	public void index(Iterable<?> data) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#index(java.lang.Object, org.apache.lucene.index.IndexWriter)
	 */
	public void index(Object objectId, IndexWriter writer) {
		MediaItem item = this.mediaItemDao.get((Long)objectId);
		indexMediaItem(item, writer);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#indexObject(java.lang.Object, org.apache.lucene.index.IndexWriter)
	 */
	public void indexObject(Object object, IndexWriter writer) {
		indexMediaItem((MediaItem)object, writer);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#reindex()
	 */
	public IndexResults reindex() {
		final MediaItemIndexResultsCallback results = new MediaItemIndexResultsCallback();
		final BasicBatchOptions batchOptions = new BasicBatchOptions(
				MediaItemDao.BATCH_NAME_INDEX, BatchMode.OFFLINE);
		if ( this.singleThreaded ) {
			try {
				getLucene().doIndexWriterOp(getIndexType(), true, false, true, new IndexWriterOp() {
					public void doWriterOp(String type, IndexWriter writer) {
						results.setWriter(writer);
						mediaItemDao.batchProcess(results, batchOptions);
					}
				});
			} finally {
				results.setFinished(true);
			}
		} else {
			new Thread(new Runnable() {
				public void run() {
					try {
						getLucene().doIndexWriterOp(getIndexType(), true, false, true, new IndexWriterOp() {
							public void doWriterOp(String type, IndexWriter writer) {
								results.setWriter(writer);
								mediaItemDao.batchProcess(results, batchOptions);
							}
						});
					} finally {
						results.setFinished(true);
					}
				}
			}, "MediaItemReindex").start();
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#reindex(magoffin.matt.lucene.SearchCriteria)
	 */
	public IndexResults reindex(SearchCriteria criteria) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LucenePlugin#search(magoffin.matt.lucene.SearchCriteria)
	 */
	public List<SearchMatch> search(SearchCriteria criteria) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	private List<Object> indexMediaItem(MediaItem item, IndexWriter writer) {
		List<Object> errors = new LinkedList<Object>();
		
		if ( item == null || item.getItemId() == null ) {
			// don't bother trying to index null or empty item
			String msg = "Null MediaItem passed to indexMediaItem()... perhaps not available in transaction?";
			log.debug(msg);
			errors.add(msg);
			return errors;
		}
		
		if ( log.isDebugEnabled() ) {
			log.debug("Indexing MediaItem " +item.getItemId()
					+" (" +item.getPath() +")");
		}
		
		Document doc = new Document();
		StringBuilder generalText = new StringBuilder();
		doc.add(new Field(IndexField.ITEM_ID.getFieldName(), item.getItemId().toString(), 
				Field.Store.YES, Field.Index.UN_TOKENIZED));

		if ( item.getName() != null ) {
			doc.add(new Field(IndexField.ITEM_NAME.getFieldName(), item.getName(), 
					Field.Store.YES, Field.Index.TOKENIZED));
			generalText.append(item.getName()).append(" ");
		}		
		if ( item.getDescription() != null ) {
			doc.add(new Field(IndexField.DESCRIPTION.getFieldName(),item.getDescription(),
					Field.Store.YES, Field.Index.TOKENIZED));
			generalText.append(item.getDescription()).append(" ");
		}
		if ( item.getCreationDate() != null ) {
			Date cDate = item.getCreationDate().getTime();
			String dateStr = getLucene().formatDateToDay(cDate);
			doc.add(new Field(IndexField.CREATED_DATE.getFieldName(), dateStr,
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			
			String monthStr = getLucene().formatDateToMonth(cDate);
			doc.add(new Field(IndexField.CREATED_DATE_MONTH.getFieldName(), monthStr,
					Field.Store.NO, Field.Index.UN_TOKENIZED));
		}
		
		Calendar itemDate = item.getItemDate();
		if ( itemDate == null ) {
			// if not item date, use the creation date instead
			// to enable searching in general just on item date
			itemDate = item.getCreationDate();
		}
		if ( itemDate != null ) {
			// item date always treated as local time
			TimeZone tz = TimeZone.getDefault();
			Date iDate = itemDate.getTime();
			String dateStr = getLucene().formatDateToDay(iDate, tz);
			String monthStr = getLucene().formatDateToMonth(iDate, tz);
			if ( dateStr != null ) {
				doc.add(new Field(IndexField.ITEM_DATE.getFieldName(), dateStr,
						Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(IndexField.ITEM_DATE_MONTH.getFieldName(), monthStr,
						Field.Store.NO, Field.Index.UN_TOKENIZED));
				doc.add(new Field(IndexField.ITEM_DATE_TIME_ZONE.getFieldName(), 
						tz.getID(), Field.Store.YES, Field.Index.NO));
			}
		}
		
		doc.add(new Field(IndexField.MEDIA_MIME.getFieldName(),
				item.getMime(),
				Field.Store.YES, Field.Index.TOKENIZED));
		generalText.append(item.getMime()).append(" ");
		
		doc.add(new Field(IndexField.MEDIA_HEIGHT.getFieldName(), 
				String.valueOf(item.getHeight()), 
				Field.Store.YES, Field.Index.NO));
		
		doc.add(new Field(IndexField.MEDIA_WIDTH.getFieldName(), 
				String.valueOf(item.getWidth()), 
				Field.Store.YES, Field.Index.NO));
		
		// metadata
		List<Metadata> metadata = item.getMetadata();
		for ( Metadata meta : metadata ) {
			doc.add(new Field(IndexField.METADATA.getFieldName(),
					meta.getKey() +':' +meta.getValue(),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			generalText.append(meta.getValue()).append(" ");
		}
		
		// tags
		List<UserTag> tags = item.getUserTag();
		for ( UserTag tag : tags ) {
			doc.add(new Field(IndexField.TAG.getFieldName(),
					tag.getTag(),
					Field.Store.YES, Field.Index.TOKENIZED));

			if ( tag.getTaggingUser() != null ) {
				doc.add(new Field(getUserTagFieldName(tag), tag.getTag(), 
						Field.Store.YES, Field.Index.TOKENIZED));
			}
			generalText.append(tag.getTag()).append(" ");
		}
		
		List<Album> sharedAlbums = 
			albumDao.findSharedAlbumsContainingItem(item);
		for ( Album album : sharedAlbums ) {
			doc.add(new Field(IndexField.MEDIA_SHARED_ALBUM_KEY.getFieldName(),
					album.getAnonymousKey(), Field.Store.YES, Field.Index.UN_TOKENIZED));
			doc.add(new Field(IndexField.MEDIA_SHARED_ALBUM_NAME.getFieldName(),
					album.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		}
		doc.add(new Field(IndexField.MEDIA_SHARED_FLAG.getFieldName(),
				sharedAlbums.size() > 0 ? "1" : "0", 
				Field.Store.NO, Field.Index.UN_TOKENIZED));
		
		// owner
		Collection c = collectionDao.getCollectionForMediaItem(item.getItemId());
		doc.add(new Field(IndexField.ITEM_OWNER.getFieldName(), 
				c.getOwner().getUserId().toString(),
				Field.Store.NO, Field.Index.UN_TOKENIZED));
		
		// user ratings
		List<MediaItemRating> ratings = item.getUserRating();
		float totalRating = 0f;
		for ( MediaItemRating rating : ratings ) {
			doc.add(new Field(getUserRatingFieldName(rating), 
					String.valueOf(rating.getRating()), 
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			totalRating += rating.getRating();
		}
		if ( totalRating > 0 ) {
			String averageRating = String.valueOf(Math.round((totalRating / ratings.size()) * 10));
			doc.add(new Field(IndexField.MEDIA_RATING.getFieldName()+"_average", averageRating, 
					Field.Store.YES, Field.Index.UN_TOKENIZED));
		}
		
		// general text
		doc.add(new Field(IndexField.GENERAL_TEXT.getFieldName(),
				generalText.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
		try {
			writer.addDocument(doc);
		} catch ( IOException e ) {
			throw new RuntimeException("IOException adding user to index",e);
		}
		
		return errors;
	}

	private final class MediaItemIndexResultsCallback 
	extends AbstractIndexResultCallback<MediaItem, Long> {

		private MediaItemIndexResultsCallback() {
			super(MediaItemLucenePlugin.super.getMessages());
		}

		@Override
		protected BatchCallbackResult doHandle(MediaItem item) throws Exception {
			if ( item == null ) return BatchCallbackResult.CONTINUE;
			if ( log.isInfoEnabled() && (getNumProcessed() % getInfoReindexCount()) == 1 ) {
				log.info("Indexing MediaItem row " +(getNumProcessed()+1)
						+" {itemId=" +item.getItemId()
						+",created=" +item.getCreationDate()
						+"}");
			}
			List<Object> indexErrors = MediaItemLucenePlugin.this.indexMediaItem(
					item, getWriter());
			LuceneServiceUtils.publishIndexEvent(new IndexEvent(item.getItemId(), 
					IndexEvent.EventType.UPDATE, getIndexType()), getIndexEventListeners());
			if ( indexErrors.size() > 0 ) {
				getErrorMap().put(item.getItemId(), 
						MediaItemLucenePlugin.super.getIndexErrorMessage(indexErrors));
			}
			return BatchCallbackResult.CONTINUE;
		}

		@Override
		protected String getIndexErrorMessage(MediaItem domainObject, Exception e) {
			return getSingleIndexErrorMessage(e);
		}

		@Override
		protected Long getPrimaryKey(MediaItem domainObject) {
			if ( domainObject == null ) return null;
			return domainObject.getItemId();
		}

	}
	
	/**
	 * @return the mediaItemDao
	 */
	public MediaItemDao getMediaItemDao() {
		return mediaItemDao;
	}
	
	/**
	 * @param mediaItemDao the mediaItemDao to set
	 */
	public void setMediaItemDao(MediaItemDao mediaItemDao) {
		this.mediaItemDao = mediaItemDao;
	}
	
	/**
	 * @return the singleThreaded
	 */
	public boolean isSingleThreaded() {
		return singleThreaded;
	}
	
	/**
	 * @param singleThreaded the singleThreaded to set
	 */
	public void setSingleThreaded(boolean singleThreaded) {
		this.singleThreaded = singleThreaded;
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
	 * @return the collectionDao
	 */
	public CollectionDao getCollectionDao() {
		return collectionDao;
	}
	
	/**
	 * @param collectionDao the collectionDao to set
	 */
	public void setCollectionDao(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
	}

}
