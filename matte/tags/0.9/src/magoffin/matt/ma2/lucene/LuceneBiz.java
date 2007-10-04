/* ===================================================================
 * LuceneBiz.java
 * 
 * Created May 27, 2006 12:10:08 PM
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
 * $Id: LuceneBiz.java,v 1.18 2007/09/04 01:11:29 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.lucene;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import magoffin.matt.lucene.IndexEvent;
import magoffin.matt.lucene.IndexListener;
import magoffin.matt.lucene.IndexStatusCallback;
import magoffin.matt.lucene.LuceneService;
import magoffin.matt.lucene.SearchMatch;
import magoffin.matt.lucene.IndexEvent.EventType;
import magoffin.matt.lucene.LuceneService.IndexSearcherOp;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IndexBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.WorkBiz.WorkRequest;
import magoffin.matt.ma2.biz.impl.AbstractSearchBiz;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndex;
import magoffin.matt.ma2.domain.PaginationIndexSection;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserSearchResult;
import magoffin.matt.ma2.support.BasicMediaItemSearchCriteria;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.context.MessageSource;

/**
 * Lucene implementation of SearchBiz and IndexBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.18 $ $Date: 2007/09/04 01:11:29 $
 */
public class LuceneBiz extends AbstractSearchBiz implements SearchBiz, IndexBiz {
	
	/** Default value for the {@code reindexWaitForFinishThreadSleep} property. */
	public static final long DEFAULT_THREAD_SLEEP = 5000;
	
	private LuceneService lucene;
	private String userIndexType = IndexType.USER.toString();
	private String mediaItemIndexType = IndexType.MEDIA_ITEM.toString();
	private MessageSource messages;
	private WorkBiz workBiz;
	private long reindexWaitForFinishThreadSleep = DEFAULT_THREAD_SLEEP;
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SearchBiz#findUsersForIndex(magoffin.matt.ma2.domain.PaginationCriteria, magoffin.matt.ma2.biz.BizContext)
	 */
	public SearchResults findUsersForIndex(final PaginationCriteria pagination, BizContext context) {
		
		final Set<String> indexKeys = lucene.getFieldTerms(
				userIndexType, IndexField.ITEM_INDEX_KEY.getFieldName());
		final SearchResults results = getDomainObjectFactory().newSearchResultsInstance();
		final PaginationIndex index = getDomainObjectFactory().newPaginationIndexInstance();
		results.setIndex(index);
		
		lucene.doIndexSearcherOp(userIndexType, new IndexSearcherOp() {
			@SuppressWarnings("unchecked")
			public void doSearcherOp(String type, IndexSearcher searcher) throws IOException {
				for ( String indexKey : indexKeys ) {
					Query indexQuery = new TermQuery(
							new Term(IndexField.ITEM_INDEX_KEY.getFieldName(),indexKey));
					Hits hits = searcher.search(indexQuery);
					PaginationIndexSection indexSection =
						getDomainObjectFactory().newPaginationIndexSectionInstance();
					indexSection.setCount(hits.length());
					indexSection.setIndexKey(indexKey);
					indexSection.setSelected(indexKey.equals(pagination.getIndexKey()));
					index.getIndexSection().add(indexSection);
					
					if ( indexSection.isSelected() ) {
						// fill in users for this section
						List<SearchMatch> matches = lucene.build(
								userIndexType,hits,0,hits.length());
						for ( SearchMatch match : matches ) {
							if ( UserSearchResult.class.isAssignableFrom(match.getClass()) ) {
								results.getUser().add(match);
							}
						}
					}
				}
			}
		});
		
		return results;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SearchBiz#findMediaItems(magoffin.matt.ma2.biz.SearchBiz.MediaItemSearchCriteria, magoffin.matt.ma2.domain.PaginationCriteria, magoffin.matt.ma2.biz.BizContext)
	 */
	@SuppressWarnings("unchecked")
	public SearchResults findMediaItems(MediaItemSearchCriteria criteria, PaginationCriteria pagination, BizContext context) {
		final SearchResults results = getDomainObjectFactory().newSearchResultsInstance();
		
		// create media item criteria with user id set
		BasicMediaItemSearchCriteria sc = new BasicMediaItemSearchCriteria();
		sc.setCountOnly(criteria.isCountOnly());
		sc.setEndDate(criteria.getEndDate());
		sc.setMediaItemTemplate(criteria.getMediaItemTemplate());
		sc.setQuickSearch(criteria.getQuickSearch());
		sc.setStartDate(criteria.getStartDate());
		
		User actingUser = context.getActingUser();
		if ( actingUser == null ) {
			actingUser = getUserBiz().getAnonymousUser();
		}
		
		Long userId = criteria.getUserId();
		if ( criteria.getUserAnonymousKey() != null ) {
			User u = getUserBiz().getUserByAnonymousKey(criteria.getUserAnonymousKey());
			if ( u != null ) {
				userId = u.getUserId();
			}
		}
		if ( userId != null && (criteria.getUserAnonymousKey() != null || !actingUser.getUserId().equals(userId))) {
			// limit to just public items
			sc.setSharedOnly(true);
		} else {
			sc.setSharedOnly(false);
		}
		if ( userId == null && actingUser != null) {
			sc.setUserId(actingUser.getUserId());
		} else {
			sc.setUserId(userId);
		}

		MediaItemLuceneSearchCriteria luceneCriteria 
			= new MediaItemLuceneSearchCriteria(sc, pagination);
		long start = System.currentTimeMillis();
		magoffin.matt.lucene.SearchResults luceneResults = lucene.find(
				mediaItemIndexType, luceneCriteria);
		results.setSearchTime(new Long(System.currentTimeMillis() - start));
		results.setPagination(pagination);
		results.setTotalResults(new Long(luceneResults.getTotalMatches()));
		results.setReturnedResults(new Long(luceneResults.getMatches().size()));
		results.getItem().addAll(luceneResults.getMatches());
		return results;
	}

	public void indexUser(Long userId) {
		lucene.indexObjectById(userIndexType, userId);
	}

	public WorkInfo recreateUserIndex(BizContext context) {
		return workBiz.submitWork(new ReindexWorkRequest(context, userIndexType));
	}

	public void removeUserFromIndex(Long userId) {
		lucene.deleteObjectById(userIndexType, userId);
	}
	
	public void indexMediaItem(Long itemId) {
		lucene.indexObjectById(mediaItemIndexType, itemId);
	}

	public WorkInfo recreateMediaItemIndex(BizContext context) {
		return workBiz.submitWork(new ReindexWorkRequest(context, mediaItemIndexType));
	}

	public void removeMediaItemFromIndex(Long itemId) {
		lucene.deleteObjectById(mediaItemIndexType, itemId);
	}

	private class ReindexWorkRequest implements WorkRequest {
		
		private final BizContext context;
		private final String indexType;
		private IndexStatusCallback indexingCallback;
		private List<Long> updatedObjectIds = new LinkedList<Long>();
		
		private ReindexWorkRequest(BizContext context, String indexType) {
			this.context = context;
			this.indexType = indexType;
		}
		
		public String getDisplayName() {
			return messages.getMessage(
					"reindex.work.displayName", null,
					"Reindexing [" +this.indexType +"]", context.getLocale());
		}
	
		public String getMessage() {
			Object[] args = new Object[] {0};
			if ( this.indexingCallback != null
					&& this.indexingCallback.getIndexResults() != null ) {
				args[0] = this.indexingCallback.getIndexResults().getNumProcessed();
			}
			return messages.getMessage(
					"reindex.work.message", args,
					"Reindexing [" +this.indexType +"]", context.getLocale());
		}

		public Integer getPriority() {
			return WorkBiz.DEFAULT_PRIORITY;
		}
	
		public List<Long> getObjectIdList() {
			return Collections.unmodifiableList(this.updatedObjectIds);
		}
	
		public boolean canStart() {
			return true;
		}

		public boolean isTransactional() {
			// transactions managed not by work biz, but by BatchableDao
			return false;
		}

		public void startWork() throws Exception {
			final Set<Long> updatedIds = new LinkedHashSet<Long>();
			IndexListener listener = new IndexListener() {
				public void onIndexEvent(IndexEvent event) {
					if ( event.getType() == EventType.UPDATE && indexType.equals(event.getIndexType()) ) {
						Object source = event.getSource();
						if ( source instanceof Long ) {
							updatedIds.add((Long)source);
						}
					}
				}
			};
			lucene.addIndexEventListener(listener);
			try {
				this.indexingCallback = lucene.reindex(indexType);
				while ( true ) {
					if ( indexingCallback.getIndexResults() != null 
							&& indexingCallback.getIndexResults().isFinished() ) {
						break;
					}
					// sleep for a bit...
					synchronized ( this ) {
						Thread.sleep(reindexWaitForFinishThreadSleep);
					}
				}
			} finally {
				lucene.removeIndexEventListener(listener);
				this.updatedObjectIds.addAll(updatedIds);
			}
		}
	
		public float getAmountCompleted() {
			return this.indexingCallback != null 
				&& this.indexingCallback.getIndexResults() != null
				&& this.indexingCallback.getIndexResults().isFinished() ? 1 : 0;
		}
		
	}
	
	/**
	 * @return the lucene
	 */
	public LuceneService getLucene() {
		return lucene;
	}
	
	/**
	 * @param lucene the lucene to set
	 */
	public void setLucene(LuceneService lucene) {
		this.lucene = lucene;
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
	
	/**
	 * @return the userIndexType
	 */
	public String getUserIndexType() {
		return userIndexType;
	}
	
	/**
	 * @param userIndexType the userIndexType to set
	 */
	public void setUserIndexType(String userIndexType) {
		this.userIndexType = userIndexType;
	}
	
	/**
	 * @return the mediaItemIndexType
	 */
	public String getMediaItemIndexType() {
		return mediaItemIndexType;
	}
	
	/**
	 * @param mediaItemIndexType the mediaItemIndexType to set
	 */
	public void setMediaItemIndexType(String mediaItemIndexType) {
		this.mediaItemIndexType = mediaItemIndexType;
	}

	/**
	 * @return the workBiz
	 */
	public WorkBiz getWorkBiz() {
		return workBiz;
	}
	
	/**
	 * @param workBiz the workBiz to set
	 */
	public void setWorkBiz(WorkBiz workBiz) {
		this.workBiz = workBiz;
	}

	/**
	 * @return the reindexWaitForFinishThreadSleep
	 */
	public long getReindexWaitForFinishThreadSleep() {
		return reindexWaitForFinishThreadSleep;
	}

	/**
	 * @param reindexWaitForFinishThreadSleep the reindexWaitForFinishThreadSleep to set
	 */
	public void setReindexWaitForFinishThreadSleep(
			long reindexWaitForFinishThreadSleep) {
		this.reindexWaitForFinishThreadSleep = reindexWaitForFinishThreadSleep;
	}

}
