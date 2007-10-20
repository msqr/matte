/* ===================================================================
 * StandardMatteAnalyzer.java
 * 
 * Created May 26, 2006 4:46:30 PM
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

package magoffin.matt.ma2.lucene;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import magoffin.matt.lucene.KeyTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Standard implementation of Analyzer for Matte.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class StandardMatteAnalyzer extends Analyzer {
	
	private static final Pattern WORD_WITH_PERIOD = 
		Pattern.compile("\\w+\\.\\w+", Pattern.CASE_INSENSITIVE);
	
	private String snowballStemmerName = "English";
	private Set<String> stopWords = null;
	private int indexKeyLength = 1;

	@Override
	public TokenStream tokenStream(String field, Reader reader) {
		TokenStream result = null;
		
		IndexField idxField = null;
		if ( field.startsWith(IndexField.TAG.getFieldName()) ) {
			idxField = IndexField.TAG;
		} else {
			try {
				idxField = IndexField.fromFieldName(field);
			} catch ( Exception e ) {
				// ignore and fallback to default
			}
		}
		
		if ( idxField == null ) {
			return standardFilters(reader);
		}
		
		switch ( idxField ) {
			case ITEM_ID:
				result = new KeywordTokenizer(reader);
				break;
				
			case ITEM_INDEX_KEY:
				result = new KeyTokenizer(reader, this.indexKeyLength);
				result = new LowerCaseFilter(result);
				break;
				
			case DESCRIPTION:
			case ITEM_NAME:
				result = standardFilters(reader);
				if ( this.stopWords == null ) {
					result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS);
				} else {
					result = new StopFilter(result, this.stopWords);
				}
				result = new SnowballFilter(result, snowballStemmerName);				
				break;
				
			case TAG:
				result = new UserTagTokenizer(reader);
				break;
				
			default:
				result = standardFilters(reader);
		}
		
		return result;
	}

	private TokenStream standardFilters(Reader reader) {
		TokenStream result = new StandardTokenizer(reader);
		result = new StandardFilter(result);
		
		// split words with periods, which StandardTokenizer does not do
		result = new TokenFilter(result) {
			
			Queue<Token> queue = new LinkedList<Token>();
			
			@Override
			public Token next() throws IOException {
				if ( queue.size() > 0 ) {
					return queue.poll();
				}
				Token t = input.next();
				if ( t == null ) {
					return null;
				}
				if ( !WORD_WITH_PERIOD.matcher(t.termText()).find() ) {
					return t;
				}
				String[] split = t.termText().split("\\.");
				int startPos = t.startOffset();
				for ( int i = 0; i < split.length; i++ ) {
					Token next = new Token(split[i], startPos, 
							startPos+split[i].length());
					queue.offer(next);
					startPos = startPos+split[i].length()+1;
				}
				return queue.poll();
			}
		};
		result = new LowerCaseFilter(result);
		return result;
	}
	
	/**
	 * @return the snowballStemmerName
	 */
	public String getSnowballStemmerName() {
		return snowballStemmerName;
	}
	
	/**
	 * @param snowballStemmerName the snowballStemmerName to set
	 */
	public void setSnowballStemmerName(String snowballStemmerName) {
		this.snowballStemmerName = snowballStemmerName;
	}
	
	/**
	 * @return the stopWords
	 */
	public Set<String> getStopWords() {
		return stopWords;
	}
	
	/**
	 * @param stopWords the stopWords to set
	 */
	public void setStopWords(Set<String> stopWords) {
		this.stopWords = stopWords;
	}
	
	/**
	 * @return the indexKeyLength
	 */
	public int getIndexKeyLength() {
		return indexKeyLength;
	}

	/**
	 * @param indexKeyLength the indexKeyLength to set
	 */
	public void setIndexKeyLength(int indexKeyLength) {
		this.indexKeyLength = indexKeyLength;
	}

}
