/* ===================================================================
 * AbstractIndexResultCallback.java
 * 
 * Created Oct 9, 2006 7:53:02 AM
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
 * $Id: AbstractIndexResultCallback.java,v 1.2 2006/10/09 23:02:41 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.lucene;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import magoffin.matt.dao.BatchableDao.BatchCallback;
import magoffin.matt.dao.BatchableDao.BatchCallbackResult;
import magoffin.matt.lucene.IndexResults;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.springframework.context.MessageSource;

/**
 * Abstract implementation that combines {@link IndexResults} and 
 * {@link BatchCallback}.
 * 
 * @param <T> the domain object type
 * @param <PK> the primary key type
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/10/09 23:02:41 $
 */
public abstract class AbstractIndexResultCallback<T, PK extends Serializable>
implements IndexResults, magoffin.matt.dao.BatchableDao.BatchCallback<T> {

	/** Default number of error percent fraction digits. */
	public static final int DEFAULT_ERROR_PERCENT_MAX_FRACTION_DIGITS = 3;

	private int numProcessed = 0;
	private Map<PK, String> errors = new LinkedHashMap<PK, String>();
	private IndexWriter writer = null;
	private boolean finished = false;
	private MessageSource messages = null;
	
	/** A class logger. */
	protected final Logger log = Logger.getLogger(getClass());

	/**
	 * Construct.
	 * 
	 * @param messages a MessageSource
	 */
	public AbstractIndexResultCallback(MessageSource messages) {
		this.messages = messages;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.dao.BatchableDao.BatchCallback#handle(java.lang.Object)
	 */
	public final BatchCallbackResult handle(T domainObject) {
		try {
			return doHandle(domainObject);
		} catch ( Exception e ) {
			StackTraceElement[] stack = e.getStackTrace();
			log.warn("Unable to index object [" +domainObject +"]: " +e
					+(stack != null && stack.length > 0 
							? " at " +stack[0].getClassName() +":" +stack[0].getLineNumber()
							: ""));
			getErrorMap().put(getPrimaryKey(domainObject), 
					getIndexErrorMessage(domainObject, e));
			return getExceptionResult(domainObject, e);
		} finally {
			numProcessed++;
		}
	}
	
	/**
	 * Get the result for when an exception occurs.
	 * 
	 * @param domainObject the domain object that caused the error
	 * @param e the exception
	 * @return the result
	 */
	protected BatchCallbackResult getExceptionResult(
			@SuppressWarnings("unused") T domainObject, 
			@SuppressWarnings("unused") Exception e ) {
		return BatchCallbackResult.CONTINUE;
	}
	
	/**
	 * Get an error message for an index operation.
	 * 
	 * @param domainObject the domain object that caused the error
	 * @param e the exception
	 * @return the message
	 */
	protected abstract String getIndexErrorMessage(T domainObject, Exception e);
	
	/**
	 * Get the primary key for a domain object.
	 * @param domainObject the domain object
	 * @return the primary key
	 */
	protected abstract PK getPrimaryKey(T domainObject);
	
	/**
	 * Handle the domain object callback.
	 * 
	 * @param domainObject the domain object
	 * @return the callback results
	 * @throws Exception if an error occurs
	 */
	protected abstract BatchCallbackResult doHandle(T domainObject) throws Exception;

	public Map<? extends Serializable, String> getErrors() {
		return errors;
	}

	public int getNumIndexed() {
		return numProcessed - errors.size();
	}

	public int getNumProcessed() {
		return numProcessed;
	}

	public boolean isFinished() {
		return finished;
	}

	/*public void finish() {
		indexObject(this.currItem);
		this.finished = true;
		if ( log.isInfoEnabled() ) {
			log.info("Processed " +count +" rows of data during indexing.");
		}
		if ( errors.size() > 0 ) {
			NumberFormat nf = DecimalFormat.getPercentInstance();
			nf.setMinimumFractionDigits(0);
			nf.setMaximumFractionDigits(DEFAULT_ERROR_PERCENT_MAX_FRACTION_DIGITS);
			double errorPercent = (double)errors.size() / (double)count;
			log.warn("Unable to index " +errors.size() + " leads (" 
					+nf.format(errorPercent) +"): "
					+errors.keySet());
		}
	}*/
	
	/**
	 * @param finished the finished to set
	 */
	protected void setFinished(boolean finished) {
		this.finished = finished;
	}

	/**
	 * Get the map of errors.
	 * @return error map
	 */
	protected Map<PK, String> getErrorMap() {
		return errors;
	}
	
	/**
	 * @return the messages
	 */
	protected MessageSource getMessages() {
		return messages;
	}
	
	/**
	 * @return the writer
	 */
	protected IndexWriter getWriter() {
		return writer;
	}
	
	/**
	 * @param messages the messages to set
	 */
	protected void setMessages(MessageSource messages) {
		this.messages = messages;
	}
	
	/**
	 * @param writer the writer to set
	 */
	protected void setWriter(IndexWriter writer) {
		this.writer = writer;
	}

}
