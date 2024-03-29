/* ===================================================================
 * MediaServerController.java
 * 
 * Created Mar 15, 2006 9:37:16 PM
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

package magoffin.matt.ma2.web;

import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.ma2.web.util.WebMediaResponse;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.mutable.MutableLong;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.LastModified;

/**
 * Controller for serving up media items.
 * 
 * @author matt.magoffin
 * @version 1.1
 */
public class MediaServerController extends AbstractCommandController implements LastModified {

	private IOBiz ioBiz;
	private MediaBiz mediaBiz;
	private ExecutorService executorService;
	private long workTimeoutSeconds = 300;

	private static final EnumSet<MediaSize> THUMB_SIZES = EnumSet.of(MediaSize.THUMB_BIG,
			MediaSize.THUMB_BIGGER, MediaSize.THUMB_NORMAL, MediaSize.THUMB_SMALL);

	public long getLastModified(HttpServletRequest request) {
		BizContext context = getWebHelper().getBizContext(request, false);
		Command cmd = new Command();
		try {
			@SuppressWarnings("deprecation")
			ServletRequestDataBinder binder = createBinder(request, cmd);
			binder.bind(request);
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}

		BasicMediaRequest mediaRequest = getMediaRequest(request, cmd);
		final MutableLong result = new MutableLong(System.currentTimeMillis());
		WorkInfo info = ioBiz.exportMedia(mediaRequest, new MediaResponse() {

			public void setMimeType(String mime) {
				// ignore
			}

			public void setMediaLength(long length) {
				// ignore
			}

			public void setModifiedDate(long date) {
				result.setValue(date);
			}

			public void setItem(MediaItem item) {
				// ignore
			}

			public OutputStream getOutputStream() {
				return null;
			}

			public void setFilename(String filename) {
				// ignore
			}

			public void setPartialResponse(long start, long end, long total) {
				// ignore
			}

			public boolean hasOutputStream() {
				return false;
			}
		}, context);

		// wait for export to complete...
		try {
			info.get(workTimeoutSeconds, TimeUnit.SECONDS);
		} catch ( Exception e ) {
			logger.warn("Exception getting last modified for item " + cmd.getId(), e);
		}

		return result.longValue();
	}

	@SuppressWarnings("unchecked")
	private BasicMediaRequest getMediaRequest(HttpServletRequest request, Command cmd) {
		MediaSize size = MediaSize.valueOf(cmd.size);
		MediaQuality quality = MediaQuality.valueOf(cmd.quality);
		BasicMediaRequest mediaRequest = new BasicMediaRequest();
		mediaRequest.setMediaItemId(cmd.id);
		mediaRequest.setOriginal(cmd.original);
		mediaRequest.getParameters().putAll(request.getParameterMap());
		mediaRequest.setQuality(quality);
		mediaRequest.setSize(size);

		// extract some header info, which is case-insensitve so we have to
		// enumerate through headers and compare in case-insensitive manner

		StringBuilder headerDebugBuf = null;
		if ( logger.isTraceEnabled() ) {
			headerDebugBuf = new StringBuilder("HTTP headers:\n");
		}

		Enumeration<String> headers = request.getHeaderNames();
		while ( headers.hasMoreElements() ) {
			String header = headers.nextElement();
			if ( headerDebugBuf != null ) {
				headerDebugBuf.append(header).append(" = ").append(request.getHeader(header))
						.append("\n");
			}
			if ( HTTP_USER_AGENT_HEADER.equalsIgnoreCase(header) ) {
				// copy the the user-agent parameter
				String ua = request.getHeader(header);
				if ( StringUtils.hasText(ua) ) {
					mediaRequest.getParameters().put(MediaRequest.USER_AGENT_KEY, ua);
				}
			} else if ( HTTP_RANGE_HEADER.equalsIgnoreCase(header) ) {
				// partial request support
				String range = request.getHeader(header);
				if ( range.toLowerCase().startsWith("bytes=") ) {
					range = range.substring(6);
					int idx = range.indexOf('-');
					long start = 0;
					if ( idx > 0 ) {
						start = Long.valueOf(range.substring(0, idx));
					}
					long end = Long.MAX_VALUE;
					if ( (idx + 1) < range.length() ) {
						end = Long.valueOf(range.substring(idx + 1));
					}
					mediaRequest.setPartialContentByteRange(new LongRange(start, end));
				}
			}
		}

		if ( headerDebugBuf != null ) {
			logger.trace(headerDebugBuf.toString());
		}

		return mediaRequest;
	}

	@Override
	protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, false);
		final Command cmd = (Command) command;
		BasicMediaRequest mediaRequest = getMediaRequest(request, cmd);

		handleHitCount(request, cmd, mediaRequest);
		WebMediaResponse mediaResponse = new WebMediaResponse(response, cmd.isDownload(),
				cmd.isOriginal());
		WorkInfo info = ioBiz.exportMedia(mediaRequest, mediaResponse, context);

		// wait for export to complete...
		while ( true ) {
			try {
				info.get(workTimeoutSeconds, TimeUnit.SECONDS);
				break;
			} catch ( TimeoutException e ) {
				final long ts = mediaResponse.getLastDataWrite();
				if ( ts + TimeUnit.SECONDS.toMillis(workTimeoutSeconds) < System.currentTimeMillis() ) {
					// haven't written anything in workTimeoutSeconds, so bail
					throw e;
				}
				logger.info("Request not complete in " + workTimeoutSeconds + "s, but bytes written "
						+ (System.currentTimeMillis() - ts) + "ms ago so waiting longer");
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void handleHitCount(HttpServletRequest request, Command cmd, MediaRequest mediaRequest) {
		if ( !StringUtils.hasText(cmd.getAlbumKey()) ) {
			return;
		}
		if ( mediaRequest.getPartialContentByteRange() != null ) {
			// don't increment for byte range requests
			return;
		}
		if ( StringUtils.hasText(cmd.getSize()) ) {
			MediaSize requestedSize = MediaSize.valueOf(cmd.getSize());
			if ( THUMB_SIZES.contains(requestedSize) ) {
				// don't increment for thumbnails
				return;
			}
		}
		HttpSession session = request.getSession(false);
		if ( session == null ) {
			return;
		}
		Set<Long> viewedItems = (Set<Long>) session
				.getAttribute(WebConstants.SES_KEY_VIEWED_MEDIA_ITEMS);
		if ( viewedItems == null ) {
			viewedItems = new LinkedHashSet<Long>();
			session.setAttribute(WebConstants.SES_KEY_VIEWED_MEDIA_ITEMS, viewedItems);
		}
		final Long mediaItemId = cmd.getId();
		if ( viewedItems.contains(mediaItemId) ) {
			// don't increment
			return;
		}
		viewedItems.add(mediaItemId);
		if ( executorService == null ) {
			getMediaBiz().incrementMediaItemHits(mediaItemId);
			return;
		}
		executorService.submit(new Runnable() {

			public void run() {
				getMediaBiz().incrementMediaItemHits(mediaItemId);
			}
		});
	}

	/**
	 * Command for MediaServer.
	 */
	public static class Command {

		private Long id;
		private boolean download = false;
		private boolean original = false;
		private String size = MediaSize.NORMAL.toString();
		private String quality = MediaQuality.GOOD.toString();
		private String albumKey = null;

		public String getAlbumKey() {
			return albumKey;
		}

		public void setAlbumKey(String albumKey) {
			this.albumKey = albumKey;
		}

		public boolean isDownload() {
			return download;
		}

		public void setDownload(boolean download) {
			this.download = download;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getQuality() {
			return quality;
		}

		public void setQuality(String quality) {
			this.quality = quality;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public boolean isOriginal() {
			return original;
		}

		public void setOriginal(boolean original) {
			this.original = original;
		}

	}

	public IOBiz getIoBiz() {
		return ioBiz;
	}

	public void setIoBiz(IOBiz ioBiz) {
		this.ioBiz = ioBiz;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	public long getWorkTimeoutSeconds() {
		return workTimeoutSeconds;
	}

	public void setWorkTimeoutSeconds(long workTimeoutSeconds) {
		this.workTimeoutSeconds = workTimeoutSeconds;
	}

}
