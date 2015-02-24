/* ===================================================================
 * UserLucenePlugin.java
 * 
 * Created May 25, 2006 9:18:03 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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

package magoffin.matt.ma2.lucene;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import magoffin.matt.dao.BasicBatchOptions;
import magoffin.matt.dao.BatchableDao.BatchCallbackResult;
import magoffin.matt.lucene.IndexEvent;
import magoffin.matt.lucene.IndexResults;
import magoffin.matt.lucene.LuceneService.IndexWriterOp;
import magoffin.matt.lucene.LuceneServiceUtils;
import magoffin.matt.lucene.SearchCriteria;
import magoffin.matt.lucene.SearchMatch;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.dao.UserDao;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserSearchResult;
import magoffin.matt.util.DelegatingInvocationHandler;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

/**
 * Lucene search plugin implementation for User objects.
 * 
 * @author matt.magoffin
 * @version 1.1
 */
public class UserLucenePlugin extends AbstractLucenePlugin {

	private UserDao userDao = null;
	private boolean singleThreaded = false;
	private final Logger log = Logger.getLogger(UserLucenePlugin.class);

	/**
	 * Default constructor.
	 */
	public UserLucenePlugin() {
		super();
		setIndexType(IndexType.USER.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magoffin.matt.lucene.LucenePlugin#getIdForObject(java.lang.Object)
	 */
	public Object getIdForObject(Object object) {
		if ( object instanceof User ) {
			return ((User) object).getUserId();
		}
		return null;
	}

	public void index(Object objectId, IndexWriter writer) {
		User user = userDao.get((Long) objectId);
		indexUser(user, writer);
	}

	public void indexObject(Object object, IndexWriter writer) {
		indexUser((User) object, writer);
	}

	public IndexResults reindex() {
		final UserIndexResultsCallback results = new UserIndexResultsCallback();
		final BasicBatchOptions batchOptions = new BasicBatchOptions(MediaItemDao.BATCH_NAME_INDEX);
		if ( this.singleThreaded ) {
			try {
				getLucene().doIndexWriterOp(getIndexType(), true, false, true, new IndexWriterOp() {

					public void doWriterOp(String type, IndexWriter writer) {
						results.setWriter(writer);
						userDao.batchProcess(results, batchOptions);
					}
				});
			} finally {
				results.setFinished(true);
			}
		} else {
			new Thread(new Runnable() {

				public void run() {
					try {
						getLucene().doIndexWriterOp(getIndexType(), true, false, true,
								new IndexWriterOp() {

									public void doWriterOp(String type, IndexWriter writer) {
										results.setWriter(writer);
										userDao.batchProcess(results, batchOptions);
									}
								});
					} finally {
						results.setFinished(true);
					}
				}
			}, "UserReindex").start();
		}
		return results;
	}

	public IndexResults reindex(SearchCriteria criteria) {
		throw new UnsupportedOperationException();
	}

	public void index(Iterable<?> data) {
		throw new UnsupportedOperationException();
	}

	public List<SearchMatch> search(SearchCriteria criteria) {
		throw new UnsupportedOperationException();
	}

	public Object getNativeQuery(SearchCriteria criteria) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * magoffin.matt.lucene.LucenePlugin#build(org.apache.lucene.document.Document
	 * )
	 */
	public SearchMatch build(Document doc) {
		UserSearchResult searchResult = getDomainObjectFactory().newUserSearchResultInstance();
		searchResult.setUserId(Long.valueOf(doc.get(IndexField.ITEM_ID.getFieldName())));
		searchResult.setName(doc.get(IndexField.ITEM_NAME.getFieldName()));
		searchResult.setLogin(doc.get(IndexField.USER_LOGIN.getFieldName()));
		if ( SearchMatch.class.isAssignableFrom(searchResult.getClass()) ) {
			return (SearchMatch) searchResult;
		}
		SearchMatch match = (SearchMatch) DelegatingInvocationHandler.wrapObject(searchResult,
				SearchMatch.class);
		return match;
	}

	private List<Object> indexUser(User user, IndexWriter writer) {
		List<Object> errors = new LinkedList<Object>();

		if ( user == null || user.getUserId() == null ) {
			// don't bother trying to index null or empty user
			String msg = "Null User passed to indexUser()... perhaps not available in transaction?";
			log.debug(msg);
			errors.add(msg);
			return errors;
		}

		if ( log.isDebugEnabled() ) {
			log.debug("Indexing User " + user.getUserId() + " (" + user.getName() + ")");
		}

		Document doc = new Document();
		doc.add(new Field(IndexField.ITEM_ID.getFieldName(), user.getUserId().toString(),
				Field.Store.YES, Field.Index.NOT_ANALYZED));

		if ( user.getName() != null ) {
			doc.add(new Field(IndexField.ITEM_NAME.getFieldName(), user.getName(), Field.Store.YES,
					Field.Index.ANALYZED));
		}

		if ( user.getLogin() != null ) {
			doc.add(new Field(IndexField.USER_LOGIN.getFieldName(), user.getLogin(), Field.Store.YES,
					Field.Index.NOT_ANALYZED));

			// note we tokenize the following index key assuming a tokenizer that 
			// outputs  a single Term is configured since sorting only works on 
			// fields with a single term
			doc.add(new Field(IndexField.ITEM_INDEX_KEY.getFieldName(), user.getLogin(), Field.Store.NO,
					Field.Index.ANALYZED));
		}
		if ( user.getCreationDate() != null ) {
			String dateStr = getLucene().formatDateToDay(user.getCreationDate().getTime());
			doc.add(new Field(IndexField.CREATED_DATE.getFieldName(), dateStr, Field.Store.YES,
					Field.Index.NOT_ANALYZED));
		}
		if ( user.getEmail() != null ) {
			doc.add(new Field(IndexField.EMAIL.getFieldName(), user.getEmail(), Field.Store.YES,
					Field.Index.ANALYZED));
		}

		try {
			writer.addDocument(doc);
		} catch ( IOException e ) {
			throw new RuntimeException("IOException adding user to index", e);
		}

		return errors;
	}

	private final class UserIndexResultsCallback extends AbstractIndexResultCallback<User, Long> {

		private UserIndexResultsCallback() {
			super(UserLucenePlugin.super.getMessages());
		}

		@Override
		protected BatchCallbackResult doHandle(User item) throws Exception {
			if ( item == null )
				return BatchCallbackResult.CONTINUE;
			if ( log.isInfoEnabled() && (getNumProcessed() % getInfoReindexCount()) == 1 ) {
				log.info("Indexing User row " + (getNumProcessed() + 1) + " {itemId=" + item.getUserId()
						+ ",created=" + item.getCreationDate() + "}");
			}
			List<Object> indexErrors = UserLucenePlugin.this.indexUser(item, getWriter());
			LuceneServiceUtils.publishIndexEvent(new IndexEvent(item.getUserId(),
					IndexEvent.EventType.UPDATE, getIndexType()), getIndexEventListeners());
			if ( indexErrors.size() > 0 ) {
				getErrorMap().put(item.getUserId(),
						UserLucenePlugin.super.getIndexErrorMessage(indexErrors));
			}
			return BatchCallbackResult.CONTINUE;
		}

		@Override
		protected String getIndexErrorMessage(User domainObject, Exception e) {
			return getSingleIndexErrorMessage(e);
		}

		@Override
		protected Long getPrimaryKey(User domainObject) {
			if ( domainObject == null )
				return null;
			return domainObject.getUserId();
		}

	}

	/**
	 * @return the userDao
	 */
	public UserDao getUserDao() {
		return userDao;
	}

	/**
	 * @param userDao
	 *        the userDao to set
	 */
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	/**
	 * @return the singleThreaded
	 */
	public boolean isSingleThreaded() {
		return singleThreaded;
	}

	/**
	 * @param singleThreaded
	 *        the singleThreaded to set
	 */
	public void setSingleThreaded(boolean singleThreaded) {
		this.singleThreaded = singleThreaded;
	}

}
