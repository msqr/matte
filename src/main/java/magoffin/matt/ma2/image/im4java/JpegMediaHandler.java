/* ===================================================================
 * JpegMediaHandler.java
 * 
 * Created Oct 21, 2010 10:41:27 AM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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

package magoffin.matt.ma2.image.im4java;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import magoffin.matt.ma2.MediaHandlerDelegate;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.ImageConstants;

/**
 * JPEG media handler using IM4Java.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>useJpeg2000</dt>
 *   <dd>If <em>true</em> then serve re-sized images as JPEG2000 files (.jp2).
 *   Otherwise serve re-sized iamges as normal JPEG files. Defaults to 
 *   <em>false</em>.</dd>
 *   
 *   <dt>jpeg2000UserAgentPatterns</dt>
 *   <dd>If configured, then only serve JPEG2000 files to user agents that 
 *   match any of the regular expressions configured in this list. This list
 *   can also be set via the <code>jpeg2000UserAgentRegExp</code> property. 
 *   If not configured then all re-sized images will be served as JPEG2000
 *   (assuming the <code>useJpeg200</code> property is configured as 
 *   <em>true</em>). The regular expression matching will use the 
 *   {@link java.util.regex.Matcher#find()} method for evauluation.</dd>
 * </dl>
 *
 * @author matt
 * @version 1.0
 */
public class JpegMediaHandler extends BaseIM4JavaMediaHandler implements MediaHandlerDelegate {

	private boolean useJpeg2000 = false;
	private List<Pattern> jpeg2000UserAgentPatterns;
	
	/**
	 * Default constructor.
	 */
	public JpegMediaHandler() {
		super(ImageConstants.JPEG_MIME);
		setPreferredFileExtension(ImageConstants.DEFAULT_JPEG_FILE_EXTENSION);
	}
	
	/**
	 * Construct with a different MIME type.
	 * @param mimeType the MIME type
	 */
	protected JpegMediaHandler(String mimeType) {
		super(mimeType);
		setPreferredFileExtension(ImageConstants.DEFAULT_JPEG_FILE_EXTENSION);
	}
	
	@Override
	public String getFileExtension(MediaItem item, MediaRequest request) {
		return useJpeg2000(request) ? ImageConstants.DEFAULT_JPEG2000_FILE_EXTENSION 
				: super.getFileExtension(item, request);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandlerDelegate#handleDelegateMediaRequest(org.springframework.core.io.Resource, java.lang.String, magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse)
	 */
	public void handleDelegateMediaRequest(Resource mediaResource, String mimeType, 
			MediaItem item, MediaRequest request, MediaResponse response) {
		defaultHandleResource(item, request, response, mediaResource);
	}

	@Override
	protected String getResponseMime(MediaItem item, MediaRequest request, Resource itemResource) {
		return useJpeg2000(request) ? ImageConstants.JPEG2000_MIME : ImageConstants.JPEG_MIME;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandlerDelegate#getDelegateFileExtension(org.springframework.core.io.Resource, java.lang.String, magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest)
	 */
	public String getDelegateFileExtension(Resource mediaResource, String mimeType, 
			MediaItem item, MediaRequest request) {
		if ( useJpeg2000(request) ) {
			return ImageConstants.DEFAULT_JPEG2000_FILE_EXTENSION;
		}
		return getPreferredFileExtension();
	}

	private boolean useJpeg2000(MediaRequest request) {
		// check if serving JPEG2000 or JPEG
		if ( useJpeg2000 ) {
			if ( CollectionUtils.isEmpty(jpeg2000UserAgentPatterns) ) {
				return true;
			} else if ( request.getParameters().containsKey(MediaRequest.USER_AGENT_KEY) ) {
				String ua = (String)request.getParameters().get(MediaRequest.USER_AGENT_KEY);
				for ( Pattern pat : jpeg2000UserAgentPatterns ) {
					if ( pat.matcher(ua).find() ) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Set the JPEG2000 user agent regular expression matches as a String
	 * array.
	 * 
	 * <p>This will compile each string into a {@link Pattern} and place that onto
	 * the {@link #getJpeg2000UserAgentPatterns()} list. This is mostly for the 
	 * benefit of configuring the patterns. The regular expressions are compiled
	 * with the {@link Pattern#CASE_INSENSITIVE} flag.</p>
	 * 
	 * @param regExps the regular expression patterns to use
	 */
	public void setJpeg2000UserAgentRegExp(String[] regExps) {
		List<Pattern> pats = new LinkedList<Pattern>();
		for ( String regExp : regExps ) {
			Pattern pat = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
			pats.add(pat);
		}
		setJpeg2000UserAgentPatterns(pats);
	}
	
	/**
	 * @return the jpeg2000UserAgentPatterns
	 */
	public List<Pattern> getJpeg2000UserAgentPatterns() {
		return jpeg2000UserAgentPatterns;
	}
	
	/**
	 * @param jpeg2000UserAgentPatterns the jpeg2000UserAgentPatterns to set
	 */
	public void setJpeg2000UserAgentPatterns(
			List<Pattern> jpeg2000UserAgentPatterns) {
		this.jpeg2000UserAgentPatterns = jpeg2000UserAgentPatterns;
	}
	
	/**
	 * @return the useJpeg2000
	 */
	public boolean isUseJpeg2000() {
		return useJpeg2000;
	}
	
	/**
	 * @param useJpeg2000 the useJpeg2000 to set
	 */
	public void setUseJpeg2000(boolean useJpeg2000) {
		this.useJpeg2000 = useJpeg2000;
	}

}
