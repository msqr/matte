/* ===================================================================
 * SearchBizLuceneImpl.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 29, 2004 11:01:53 AM.
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
 * $Id: SearchBizLuceneImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DAOSearchCallback;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.SearchBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.dao.DAOConstants;
import magoffin.matt.ma.dao.MediaItemCriteria;
import magoffin.matt.ma.search.IndexParams;
import magoffin.matt.ma.search.MediaItemDAOIndexCallback;
import magoffin.matt.ma.search.MediaItemIndexUpdateThread;
import magoffin.matt.ma.search.MediaItemMatch;
import magoffin.matt.ma.search.MediaItemQuery;
import magoffin.matt.ma.search.MediaItemResults;
import magoffin.matt.ma.search.SearchConstants;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.ItemComment;
import magoffin.matt.ma.xsd.ItemRating;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemSearchResults;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.ArrayUtil;
import magoffin.matt.util.StringUtil;
import magoffin.matt.util.config.Config;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Search biz implementation using Lucene.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class SearchBizLuceneImpl extends AbstractBiz implements SearchBiz 
{
	/** 
	 * A base path added to {@link ApplicationConstants#ENV_BASE_FILE_PATH_INDEX}:
	 * <code>lucene</code>
	 */
	public static final String INDEX_BASE_PATH = "lucene";
	
	/** 
	 * A base path added to {@link #INDEX_BASE_PATH} for media item index:
	 * <code>media</code>
	 */
	public static final String MEDIA_ITEM_BASE_PATH = "media";
	
	/**
	 * The Config key from {@link ApplicationConstants#CONFIG_ENV}
	 * for the number of MediaItem index updates to allow before optimizing 
	 * the index: <code>lucene.optimize.trigger.item</code>.
	 */
	public static final String ENV_MEDIA_ITEM_OPTIMIZE_TRIGGER = 
		"lucene.optimize.trigger.item";
	
	/**
	 * The default MediaItem optimize trigger if not supplied via Config: 
	 * <code>100</code>
	 */
	public static final int MEDIA_ITEM_OPTIMIZE_TRIGGER_DEFAULT = 100;
	
	public static final char FIELD_DELIM = ':';

	public static final String FIELD_ITEM_ID = "id";
	public static final String FIELD_OWNER_ID = "own";
	public static final String FIELD_CREATION_DATE = "date";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_MISC_TEXT = "txt";
	public static final String FIELD_KEYWORD = "kwd";
	public static final String FIELD_CATEGORY = "cat";
	public static final String FIELD_RATING = "rate";
	
	public static final String NECESSARY = "+";
	public static final String PROHIBITED = "-";
	
	public static final String AND = "AND";
	public static final String OR = "OR";
	
	private static final Logger LOG = Logger.getLogger(SearchBizLuceneImpl.class);
	
	private File indexDirectory = null;

	private Directory itemDirectory = null;
	private IndexSearcher itemSearcher = null;
	private IndexReader itemReader = null;
	private IndexWriter itemWriter = null;
	private int itemUpdateOptimizeTrigger = 0;
	private int itemUpdateCount = 0;
	private MediaItemIndexUpdateThread itemUpdateThread = null;
	private Analyzer itemAnalyzer = null;
	
	private static class MediaItemAnalyzer extends Analyzer
	{
		private static final char FIELD_NAME_CHAR = 'n';
		private static final char FIELD_MISC_TEXT_CHAR = 't';
		private static final char FIELD_KEYWORD_CHAR = 'k';
		private static final char FIELD_CATEGORY_CHAR = 'c';
		
		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
		 */
		public TokenStream tokenStream(String field, Reader reader) {
			char fieldChar = field.charAt(0);
			TokenStream result = null;
			switch (fieldChar) {
				case FIELD_NAME_CHAR:
				case FIELD_MISC_TEXT_CHAR:
					result = new StandardTokenizer(reader);
					result = new StandardFilter(result);
					result = new LowerCaseFilter(result);
					result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS);
					result = new PorterStemFilter(result);
					break;
					
				case FIELD_CATEGORY_CHAR:
				case FIELD_KEYWORD_CHAR:
					result = new StandardTokenizer(reader);
					result = new StandardFilter(result);
					result = new LowerCaseFilter(result);
					break;
					
				default:
					result = new StandardTokenizer(reader);
					result = new StandardFilter(result);
					break;
			}
			return result;
		}
}
	
	private static class LuceneIndexParams implements IndexParams
	{
		private SearchBiz searchBiz = null;
		private boolean addOnly = true;
		private Integer deleteId = null;
		
		public boolean isAddOnly() { return addOnly; }
		
		public void setAddOnly(boolean addOnly) { this.addOnly = addOnly; }
		
		public Integer getDeleteId() { return deleteId; }
		
		public void setDeleteId(Integer deleteId) { this.deleteId = deleteId; }
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.search.IndexParams#getParam(java.lang.String)
		 */
		public Object getParam(String key) {
			return null;
		}
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.search.IndexParams#getSearchBiz()
		 */
		public SearchBiz getSearchBiz() {
			return searchBiz;
		}
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.search.IndexParams#setParam(java.lang.String, java.lang.Object)
		 */
		public void setParam(String key, Object param) {	}
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.search.IndexParams#setSearchBiz(magoffin.matt.ma.biz.SearchBiz)
		 */
		public void setSearchBiz(SearchBiz searchBiz) {
			this.searchBiz = searchBiz;
		}
	}
	
	private static class LuceneMediaItemMatch implements MediaItemMatch
	{
		private Integer itemId;
		private Date creationDate;
		private String name;
		
		public LuceneMediaItemMatch(Document doc)
		{
			this.itemId = Integer.valueOf(doc.get(FIELD_ITEM_ID));
			this.creationDate = DateField.stringToDate(doc.get(FIELD_CREATION_DATE));
			this.name = doc.get(FIELD_NAME);
		}
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.search.MediaItemMatch#getCreationDate()
		 */
		public Date getCreationDate() {
			return creationDate;
		}
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.search.MediaItemMatch#getItemId()
		 */
		public Integer getItemId() {
			return itemId;
		}
		
		/* (non-Javadoc)
		 * @see magoffin.matt.ma.search.MediaItemMatch#getName()
		 */
		public String getName() {
			return name;
		}
	}
	
/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) {
	super.init(initializer);
	String indexDirPath = Config.getNotEmpty(ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_BASE_FILE_PATH_INDEX);
	File tmpFile = new File(indexDirPath,INDEX_BASE_PATH);
	if ( !tmpFile.exists() ) {
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Creating Lucene index directory " +tmpFile.getAbsolutePath());
		}
		if ( !tmpFile.mkdirs() ) {
			throw new MediaAlbumRuntimeException("Unable to create Lucene index directory " 
					+tmpFile.getAbsolutePath());
		}
	}
	if ( !tmpFile.isDirectory() ) {
		throw new MediaAlbumRuntimeException("Lucene index directory is not a directory: "
				+tmpFile.getAbsolutePath());
	}
	indexDirectory = tmpFile;
	
	// check for Media Item index
	
	File itemDir = new File(indexDirectory,MEDIA_ITEM_BASE_PATH);
	if ( !itemDir.exists() ) {
		if ( !itemDir.mkdirs() ) {
			throw new MediaAlbumRuntimeException("Unable to create Lucene Media Item index directory " 
					+itemDir.getAbsolutePath());
		}
	}
	
	try {
		itemDirectory = FSDirectory.getDirectory(itemDir,false);
	} catch ( IOException e ) {
		throw new MediaAlbumRuntimeException("Unable to open index",e);
	}

	// initialize media item index support
	itemUpdateCount = 0;
	itemUpdateOptimizeTrigger = Config.getInt(ApplicationConstants.CONFIG_ENV,
			ENV_MEDIA_ITEM_OPTIMIZE_TRIGGER,
			MEDIA_ITEM_OPTIMIZE_TRIGGER_DEFAULT);
	itemAnalyzer = new MediaItemAnalyzer();

	// recreate index if doesn't exist
	if ( !IndexReader.indexExists(itemDir) ) {
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Creating new Lucene index " +itemDir.getAbsolutePath());
		}
		try {
			recreateEntireIndex();
		} catch ( MediaAlbumException e ) {
			throw new MediaAlbumRuntimeException("Unable to recreate entire index",e);
		}
	}
	
	try {
		itemReader = IndexReader.open(itemDirectory);
		itemSearcher = new IndexSearcher(itemReader);
	} catch ( IOException e ) {
		throw new MediaAlbumRuntimeException("Unable to open item index for read",e);
	}
	itemUpdateThread = new MediaItemIndexUpdateThread(bizFactory);
	Thread t = new Thread(itemUpdateThread);
	t.setName(itemUpdateThread.getThreadName());
	t.setPriority(Thread.MIN_PRIORITY);
	t.start();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.SearchBiz#recreateEntireIndex()
 */
public void recreateEntireIndex() throws MediaAlbumException
{
	recreateMediaItemIndex();
}

private synchronized void recreateMediaItemIndex() throws MediaAlbumException
{
	/*
	closeAll();
	
	if ( indexDirectory != null ) {
		if ( IndexReader.indexExists(indexDirectory) ) {
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Deleting Lucene index at " +indexDirectory);
			}
			FileUtil.deleteRecursive(indexDirectory, false);
		}
	}*/

	// search entire media item table, indexing each row
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		crit.setSearchType(MediaItemCriteria.ALL_MEDIA_ITEMS_INDEX);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		DAOSearchCallback callback = dao.getCallbackInstance(
				DAOConstants.SEARCH_CALLBACK_MEDIA_ITEM);
		
		if ( !(callback instanceof MediaItemDAOIndexCallback) ) {
			throw new MediaAlbumException("Index callback not configured properly");
		}
		
		MediaItemDAOIndexCallback mCallback = (MediaItemDAOIndexCallback)callback;
		mCallback.setCriteria(crit);
		
		// create new writer to overwrite existing index
		getItemIndexWriter(true);
		
		LuceneIndexParams params = new LuceneIndexParams();
		params.setSearchBiz(this);
		params.setAddOnly(true);
		mCallback.setIndexParams(params);
		
		dao.find(mCallback);
		
		mCallback.finish();
		
		itemWriter.optimize();
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch (IOException e) {
		throw new MediaAlbumException("IOException",e);
	} finally {
		returnObjectToPool(pool,crit);
		if ( itemWriter != null ) {
			try {
				closeItemIndexWriter();
				closeItemIndexReader();
			} catch ( IOException e ) {
				LOG.warn("IOException closing IndexWriter: " +e.toString());
			}
		}
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.SearchBiz#search(magoffin.matt.ma.search.MediaItemQuery, magoffin.matt.ma.xsd.User)
 */
public MediaItemResults search(MediaItemQuery query, User actingUser)
throws MediaAlbumException 
{
	Query luceneQuery = getQuery(query, actingUser);
	if ( LOG.isDebugEnabled() && luceneQuery != null ) {
		LOG.debug("Searching for MediaItems with Lucene query: " +luceneQuery);
	}
	IndexSearcher searcher = null;
	try {
		searcher = getItemIndexSearcher();
		long start = System.currentTimeMillis();
		Hits hits = luceneQuery != null ? searcher.search(luceneQuery) : null;
		long time = System.currentTimeMillis() - start;
		int numHits = hits == null ? 0 : hits.length();
		MediaItemMatch[] matches = null;
		if ( numHits > 0 ) {
			matches = new LuceneMediaItemMatch[numHits];
			for ( int i = 0; i < numHits; i++ ) {
				matches[i] = new LuceneMediaItemMatch(hits.doc(i));
			}
		}
		MediaItemSearchResults sr = new MediaItemSearchResults();
		sr.setIsPartialResult(false);
		sr.setReturnedResults(numHits);
		sr.setSearchTime(time);
		sr.setStartingOffset(0);
		sr.setTotalResults(numHits);
		MediaItemResults results = new MediaItemResults(query,matches,sr);
		return results;
	} catch ( IOException e ) {
		LOG.error("IOException searching for media items",e);
		throw new MediaAlbumException("Unable to search: " +e.getMessage());
	}
}

private Query getQuery(MediaItemQuery itemQuery, User actingUser)
throws MediaAlbumException
{
	String queryStr = getQueryString(itemQuery);
	if ( queryStr == null || queryStr.length() < 1 ) {
		return null;
	}
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	if ( !userBiz.isUserSuperUser(actingUser.getUserId()) ) {
		queryStr = "("+queryStr+") " +AND +" " +NECESSARY+FIELD_OWNER_ID+":"+actingUser.getUserId();
	}
	QueryParser parser = new QueryParser(FIELD_MISC_TEXT,new MediaItemAnalyzer());
	try {
		return parser.parse(queryStr);
	} catch (ParseException e) {
		throw new MediaAlbumException("Unable to parse query",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.SearchBiz#index(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.search.IndexParams)
 */
public void index(MediaItem item, IndexParams params)
throws MediaAlbumException 
{
	if ( !(params instanceof LuceneIndexParams) ) {
		throw new MediaAlbumException("IndexParam object not supported: " +params);
	}
	
	LuceneIndexParams lParams = (LuceneIndexParams)params;
	
	boolean deleted = false;
	
	try {
		if ( lParams.getDeleteId() != null ) {
			deleted = deleteItemFromIndex(lParams.getDeleteId());
		} else {
			deleted = index(item,lParams.isAddOnly());
		}
	} finally {
		if ( deleted ) {
			try {
				closeItemIndexReader();
				closeItemIndexWriter();
			} catch ( IOException e ) {
				throw new MediaAlbumException("IOException adding document to index",e);
			}
		}
	}
}

private boolean deleteItemFromIndex(Integer itemId) throws MediaAlbumException
{
	try {
		Term idTerm = new Term(FIELD_ITEM_ID,itemId.toString());
		if ( getItemIndexReader().docFreq(idTerm) > 0 ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Deleting MediaItem " +itemId +" from index");
			}
			
			closeItemIndexWriter();
			
			// get new reader
			IndexReader reader = IndexReader.open(itemDirectory);
			reader.delete(idTerm);
			reader.close();
			
			return true;
		}
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException looking for existing item in index",e);
	}
	
	return false;
}

/**
 * Index a MediaItem.
 * 
 * <p>This method will optimize the index after every X updates.</p>
 * 
 * <p>If <var>addOnly</var> is <em>true</em> then this method will 
 * <b>not</b> attempt to delete the item from the index before indexing, 
 * and it will <b>not</b> attempt to optimize the index.</p>
 * 
 * <p>This method is not synchronized but assumes only one thread 
 * at a time calls it, for example from 
 * {@link MediaItemIndexUpdateThread}.</p>
 * 
 * @param item the MediaItem to index
 * @param addOnly if <em>true</em> then do not check if item exists already, 
 * otherwise check if item exists in index first and delete before indexing 
 * if found
 * @throws MediaAlbumException if an error occurs
 */
private boolean index(MediaItem item, boolean addOnly) 
throws MediaAlbumException
{
	boolean deleted = false;
	
	if ( !addOnly) {
		deleted = deleteItemFromIndex(item.getItemId());
	}
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Indexing MediaItem " +item.getItemId());
	}
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection c = collectionBiz.getCollectionById(item.getCollection(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	Document doc = new Document();
	doc.add(Field.Keyword(FIELD_ITEM_ID,item.getItemId().toString()));
	if ( c != null ) {
		doc.add(Field.Keyword(FIELD_OWNER_ID,c.getOwner().toString()));
	}
	if ( item.getCreationDate() != null ) {
		doc.add(Field.Keyword(FIELD_CREATION_DATE,item.getCreationDate()));
	}
	doc.add(Field.Text(FIELD_NAME,item.getName() != null 
			? item.getName() : item.getPath() ));
	
	if ( item.getComment() != null ) {
		doc.add(Field.Text(FIELD_MISC_TEXT,item.getComment()));
	}
	
	// misc text is combo of comments, user comments
	int size = item.getUserCommentCount();
	for ( int i = 0; i < size; i++ ) {
		ItemComment comm = item.getUserComment(i);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Indexing MediaItem " + item.getItemId() +" user comment " 
					+comm.getCommentId());
		}
		doc.add(Field.UnStored(FIELD_MISC_TEXT, comm.getContent()));
	}

	size = item.getUserRatingCount();
	for ( int i = 0; i < size; i++ ) {
		ItemRating rating = item.getUserRating(i);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Indexing MediaItem " + item.getItemId() +" rating " 
					+rating.getRatingId());
		}
		doc.add(Field.Keyword(FIELD_RATING,rating.getRating().toString()));
	}
	
	// index keywords, copyright, categories
	size = item.getDataCount();
	for ( int i = 0; i < size; i++ ) {
		FreeData data = item.getData(i);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Indexing MediaItem " + item.getItemId() +" free data " 
					+data.getDataId());
		}
		Integer dataType = data.getDataTypeId();
		if ( ApplicationConstants.FREE_DATA_TYPE_CATEGORY.equals(dataType) ) {
			addKeywords(doc,FIELD_CATEGORY,data.getDataValue(), ',');
		} else if ( ApplicationConstants.FREE_DATA_TYPE_KEYWORD.equals(dataType) ) {
			addKeywords(doc,FIELD_KEYWORD,data.getDataValue(), ',');
		} else {
			doc.add(Field.Text(FIELD_MISC_TEXT,data.getDataValue()));
		}
	}
	
	try {
		getItemIndexWriter(false).addDocument(doc);
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException adding document to index",e);
	}
	
	if ( !addOnly ) {
		itemUpdateCount++;
		if ( itemUpdateCount > itemUpdateOptimizeTrigger ) {
			synchronized ( itemDirectory ) {
				if ( itemUpdateCount > itemUpdateOptimizeTrigger ) {
					try {
						getItemIndexWriter(false).optimize();
					} catch ( IOException e ) {
						throw new MediaAlbumException("IOException optimizing item index",e);
					} finally {
						try {
							closeItemIndexWriter();
							closeItemIndexReader();
						} catch ( IOException e ) {
							LOG.error("Unable to close item index writer",e);
						}
					}
					itemUpdateCount = 0;
				}
			}
		}
	}
	
	return deleted;
}

private IndexSearcher getItemIndexSearcher() throws IOException {
	if ( itemSearcher == null ) {
		synchronized ( itemDirectory ) {
			if ( itemSearcher == null ) {
				IndexReader reader = getItemIndexReader();
				itemSearcher = new IndexSearcher(reader);
				return itemSearcher;
			}
		}
	}
	return itemSearcher;
}

private void closeItemIndexSearcher() throws IOException {
	if ( itemSearcher != null ) {
		synchronized ( itemDirectory ) {
			if ( itemSearcher != null ) {
				itemSearcher.close();
				itemSearcher = null;
			}
		}
	}
}

/**
 * @return IndexReader for Media Item objects
 */
private IndexReader getItemIndexReader() throws IOException {
	if ( itemReader == null ) {
		synchronized ( itemDirectory ) {
			if ( itemReader == null ) {
				itemReader = IndexReader.open(itemDirectory);
				return itemReader;
			}
		}
	}
	return itemReader;
}

private void closeItemIndexReader() throws IOException {
	if ( itemReader != null ) {
		synchronized ( itemDirectory ) {
			if ( itemReader != null ) {
				closeItemIndexSearcher();
				itemReader.close();
				itemReader = null;
			}
		}
	}
}

/**
 * @return IndexWriter for Media Item objects
 */
private IndexWriter getItemIndexWriter(boolean create) throws IOException {
	if ( itemWriter == null ) {
		synchronized ( itemDirectory ) {
			if ( itemWriter == null ) {
				itemWriter = new IndexWriter(itemDirectory,itemAnalyzer,create);
				return itemWriter;
			}
		}
	}
	return itemWriter;
}

private void closeItemIndexWriter() throws IOException {
	if ( itemWriter != null ) {
		LOG.info("Closing item index writer");
		synchronized ( itemDirectory ) {
			if ( itemWriter != null ) {
				itemWriter.close();
				itemWriter = null;
			}
		}
	}
}

/**
 * Add keywords seperated by a delimiter to a Document.
 * 
 * @param doc the Document
 * @param field the index field name
 * @param text the keyword value
 * @param delim the text delimiter
 */
private void addKeywords(Document doc, String field, String text, char delim) {
	int idx = text.indexOf(delim);
	if ( idx > -1 ) {
		String[] txts = StringUtil.normalizeWhitespace(ArrayUtil.split(text,delim,-1));
		for ( int i = 0; i < txts.length; i++ ) {
			doc.add(Field.Keyword(field,txts[i]));
		}
	} else {
		doc.add(Field.Keyword(field,text));
	}
}

private String getQueryString(MediaItemQuery query)
{
	StringBuffer myBuf = new StringBuffer();
	
	if ( query.getSimple() != null ) {
		String txt = StringUtil.normalizeWhitespace(query.getSimple());
		myBuf.append(FIELD_NAME).append(FIELD_DELIM).append('(')
			.append(txt).append(") ").append(OR).append(" ").append(FIELD_MISC_TEXT)
			.append(FIELD_DELIM).append('(').append(txt).append(')');
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Got MediaItem simple query string: " +myBuf.toString());
		}
		return myBuf.toString();
	}
	
	if ( query.getText() != null ) {
		// add quotes if contains a space
		String txt = StringUtil.normalizeWhitespace(query.getText());
		int space = txt.indexOf(' ');
		if ( space > 0 ) {
			myBuf.append('"').append(txt).append('"');
		} else {
			myBuf.append(txt);
		}
	}
	
	if ( query.getName() != null ) {
		String txt = StringUtil.normalizeWhitespace(query.getName());
		myBuf.append(FIELD_NAME).append(FIELD_DELIM);
		int space = txt.indexOf(' ');
		if ( space > 0 ) {
			myBuf.append('"').append(txt).append('"');
		} else {
			myBuf.append(txt);
		}
	}
	
	appendMultiField(FIELD_KEYWORD,query.getKeyword(),' ', myBuf);
	appendMultiField(FIELD_CATEGORY,query.getCategory(),' ',myBuf);
	
	switch ( query.getNecessity() ) {
		case SearchConstants.NECESSARY:
			addNecessity(NECESSARY,myBuf);
			break;
			
		case SearchConstants.PROHIBITED:
			addNecessity(PROHIBITED,myBuf);
			break;
	}
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Got MediaItem query string: " +myBuf.toString());
	}
	return myBuf.toString();
}

private void addNecessity(String necessity, StringBuffer buf)
{
	if ( buf.length() < 1 ) return;
	int space = buf.indexOf(" ");
	if ( space > 0 ) {
		buf.insert(0,'(');
		buf.insert(0,necessity);
		buf.append(')');
	} else {
		buf.insert(0,necessity);
	}
}

private void appendMultiField(String field, String txt, char delimiter, StringBuffer buf)
{
	if ( txt == null ) return;
	
	if ( buf.length() > 0 ) {
		buf.append(" ").append(AND).append(" ");
	}
	buf.append(field).append(':');
	txt = StringUtil.normalizeWhitespace(txt);
	int space = txt.indexOf(' ');
	if ( space > 0 ) {
		String[] words = ArrayUtil.split(txt,delimiter,-1);
		buf.append("(");
		for ( int i = 0; i < words.length; i++ ) {
			if ( i > 0 ) {
				buf.append(" ").append(OR).append(" ");
			}
			buf.append(words[i]);
		}
		buf.append(")");
	} else {
		buf.append(txt);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.SearchBiz#index(java.lang.Integer)
 */
public void index(Integer itemId) throws MediaAlbumException {	
	LuceneIndexParams params = new LuceneIndexParams();
	params.setSearchBiz(this);
	params.setAddOnly(false);

	MediaItemIndexUpdateThread.Data data = new MediaItemIndexUpdateThread.Data(
			itemId,params,MediaItemIndexUpdateThread.UPDATE);
	itemUpdateThread.enqueue(data);
}

/* (non-Javadoc)
 * @see java.lang.Object#finalize()
 */
protected void finalize() throws Throwable {
	finish();
}

private void closeAll() 
{
	try {
		closeItemIndexSearcher();
	} catch ( IOException e ) {
		LOG.error("Unable to close item searcher",e);
	}
	
	try {
		closeItemIndexReader();
	} catch ( IOException e ) {
		LOG.error("Unable to close item reader",e);
	}
	
	try {
		closeItemIndexWriter();
	} catch ( IOException e ) {
		LOG.error("Unable to close item writer",e);
	}

}

/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#finish()
 */
public synchronized void finish() {
	// close out the index readers/writers
	closeAll();

	if ( itemUpdateThread != null ) {
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Sopping " +itemUpdateThread.getThreadName());
		}
		itemUpdateThread.stop();
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.SearchBiz#removeMediaItem(java.lang.Integer)
 */
public void removeMediaItem(Integer itemId) throws MediaAlbumException {
	LuceneIndexParams params = new LuceneIndexParams();
	params.setSearchBiz(this);
	params.setAddOnly(false);
	params.setDeleteId(itemId);

	MediaItemIndexUpdateThread.Data data = new MediaItemIndexUpdateThread.Data(
			itemId,params,MediaItemIndexUpdateThread.DELETE);
	itemUpdateThread.enqueue(data);
}

}
