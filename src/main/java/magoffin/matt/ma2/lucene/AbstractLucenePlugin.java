/* ===================================================================
 * AbstractLucenePlugin.java
 * 
 * Created May 30, 2006 12:46:41 PM
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
import java.util.Set;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.springframework.context.MessageSource;
import magoffin.matt.lucene.BasicSearchResults;
import magoffin.matt.lucene.IndexListener;
import magoffin.matt.lucene.LucenePlugin;
import magoffin.matt.lucene.LuceneService;
import magoffin.matt.lucene.LuceneService.IndexQueryOp;
import magoffin.matt.lucene.SearchCriteria;
import magoffin.matt.lucene.SearchResults;
import magoffin.matt.ma2.biz.DomainObjectFactory;

/**
 * Base implementation for LucenePlugin implementations.
 * 
 * @author matt.magoffin
 * @version 1.2
 */
public abstract class AbstractLucenePlugin implements LucenePlugin {

	/** The default value for the <code>infoReindexCount</code> property. */
	public static final int DEFAULT_REINDEX_COUNT = 50;

	/** Default number of error percent fraction digits. */
	public static final int DEFAULT_ERROR_PERCENT_MAX_FRACTION_DIGITS = 3;

	private int infoReindexCount = DEFAULT_REINDEX_COUNT;
	private LuceneService lucene = null;
	private Analyzer analyzer = new StandardMatteAnalyzer();
	private LuceneIndexConfig config = null;
	private Set<IndexListener> indexEventListeners;
	private MessageSource messages = null;
	private DomainObjectFactory domainObjectFactory;
	private String indexType = null;

	@Override
	public final LuceneIndexConfig init(LuceneService luceneService,
			Set<IndexListener> indexEventListenersSet) {
		this.lucene = luceneService;
		this.indexEventListeners = indexEventListenersSet;
		doAfterInit();
		return config;
	}

	/**
	 * Method for extending classes to override if needed, called during the
	 * {@link #init(LuceneService, Set)} method.
	 * 
	 * <p>
	 * This method implementation does not do anything.
	 * </p>
	 * 
	 * @see #init(LuceneService, Set)
	 */
	protected void doAfterInit() {
		// default implementation does nothing
	}

	/**
	 * Get a single index error message from a list of index errors.
	 * 
	 * @param indexErrors
	 *        list of errors
	 * @return single index error message
	 */
	protected String getIndexErrorMessage(List<Object> indexErrors) {
		if ( indexErrors.size() == 0 ) {
			return "";
		}
		if ( indexErrors.size() == 1 ) {
			return getSingleIndexErrorMessage(indexErrors.get(0));
		}
		String msg = indexErrors.size() + " errors: ";
		int i = 0;
		for ( Object object : indexErrors ) {
			if ( i > 0 ) {
				msg += "; ";
			}
			msg += getSingleIndexErrorMessage(object);
		}
		return msg;
	}

	/**
	 * Get an individual index error message.
	 * 
	 * @param object
	 *        the error message or exception
	 * @return error message string
	 */
	protected String getSingleIndexErrorMessage(Object object) {
		String msg = object.toString();
		if ( object instanceof Throwable ) {
			StackTraceElement[] stack = ((Throwable) object).getStackTrace();
			if ( stack != null && stack.length > 0 ) {
				msg += " at " + stack[0].getClassName() + ":" + stack[0].getLineNumber();
			}
		}
		return msg;
	}

	@Override
	public SearchResults find(final SearchCriteria criteria) {
		Query q = (Query) getNativeQuery(criteria);
		final MutableInt totalResults = new MutableInt(0);
		final List<Object> matches = new LinkedList<Object>();
		getLucene().doIndexQueryOp(getIndexType(), q, false, new IndexQueryOp() {

			@SuppressWarnings("deprecation")
			@Override
			public void doSearcherOp(String type, IndexSearcher searcher, Query query,
					org.apache.lucene.search.TopDocCollector hits) throws IOException {
				totalResults.setValue(hits.getTotalHits());
				int start = 0;
				if ( criteria.getPage() > 1 ) {
					start = (criteria.getPage() - 1) * criteria.getPageSize();
				}
				int end = hits.getTotalHits();
				if ( criteria.getMaxResults() > 0 ) {
					end = criteria.getMaxResults();
				}
				matches.addAll(getLucene().build(getIndexType(), hits, start, end));
			}
		});
		return new BasicSearchResults(matches, totalResults.intValue());
	}

	/**
	 * Get the LuceneService configured for this plugin.
	 * 
	 * @return the LuceneService instance
	 */
	protected LuceneService getLucene() {
		return lucene;
	}

	/**
	 * Get the list of IndexLister objects.
	 * 
	 * @return Returns the indexEventListeners.
	 */
	protected Set<IndexListener> getIndexEventListeners() {
		return indexEventListeners;
	}

	/**
	 * @return the infoReindexCount
	 */
	public int getInfoReindexCount() {
		return infoReindexCount;
	}

	/**
	 * @param infoReindexCount
	 *        the infoReindexCount to set
	 */
	public void setInfoReindexCount(int infoReindexCount) {
		this.infoReindexCount = infoReindexCount;
	}

	@Override
	public Analyzer getAnalyzer() {
		return this.analyzer;
	}

	/**
	 * @return the config
	 */
	public LuceneIndexConfig getConfig() {
		return config;
	}

	/**
	 * @param config
	 *        the config to set
	 */
	public void setConfig(LuceneIndexConfig config) {
		this.config = config;
	}

	/**
	 * @param analyzer
	 *        the analyzer to set
	 */
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * @return the messages
	 */
	public MessageSource getMessages() {
		return messages;
	}

	/**
	 * @param messages
	 *        the messages to set
	 */
	public void setMessages(MessageSource messages) {
		this.messages = messages;
	}

	/**
	 * @return Returns the domainObjectFactory.
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory
	 *        The domainObjectFactory to set.
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	@Override
	public String getIndexType() {
		return this.indexType;
	}

	/**
	 * @param indexType
	 *        the indexType to set
	 */
	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

}
