/* ===================================================================
 * DownloadItemsForm.java
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.IOBiz.TwoPhaseExportRequest;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.JobInfo;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;
import magoffin.matt.ma2.support.ExportItemsCommand;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.ma2.web.util.WebMediaResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller for downloading a set of media items.
 * 
 * @author matt.magoffin
 * @version 1.1
 */
@SuppressWarnings("deprecation")
public class DownloadItemsForm extends AbstractForm {

	private IOBiz ioBiz;
	private WorkBiz workBiz;
	private MediaBiz mediaBiz;
	private SearchBiz searchBiz;

	@Override
	protected boolean isFormSubmission(HttpServletRequest request) {
		if ( StringUtils.hasText(request.getParameter("ticket")) ) {
			return true;
		}
		return super.isFormSubmission(request);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
			throws Exception {
		BizContext context = getWebHelper().getBizContext(request, false);
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		Model model = getDomainObjectFactory().newModelInstance();
		ExportItemsCommand cmd = (ExportItemsCommand) command;
		Album album = getRequestAlbum(request, cmd, context);
		if ( album != null ) {
			model.getAlbum().add(album);
		}
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return viewModel;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors) throws Exception {
		BizContext context = getWebHelper().getBizContext(request, false);
		ExportItemsCommand cmd = (ExportItemsCommand) command;
		Album album = getRequestAlbum(request, cmd, context); // for album based requests
		String filename = null;
		if ( album != null ) {
			filename = sanatizeFilename(album.getName());
		} else {
			filename = getMessageSourceAccessor().getMessage("download.selected.items.zip.name");
		}
		filename += ".zip";

		if ( cmd.getTicket() != null ) {
			// phase 2 of 2-phase request
			WorkInfo info = workBiz.getInfo(cmd.getTicket());
			TwoPhaseExportRequest tper = (TwoPhaseExportRequest) info.getWorkRequest();
			tper.setMediaResponse(new WebMediaResponse(response, filename));
			info.get();
			return null;
		}

		BasicMediaRequest mediaRequest = new BasicMediaRequest();
		if ( StringUtils.hasText(cmd.getSize()) ) {
			try {
				mediaRequest.setSize(MediaSize.valueOf(cmd.getSize()));
			} catch ( Exception e ) {
				logger.warn("Unable to determine MediaSize from [" + cmd.getSize() + "]");
			}
		}
		if ( StringUtils.hasText(cmd.getQuality()) ) {
			try {
				mediaRequest.setQuality(MediaQuality.valueOf(cmd.getQuality()));
			} catch ( Exception e ) {
				logger.warn("Unable to determine MediaQualtiy from [" + cmd.getQuality() + "]");
			}
		}
		mediaRequest.setOriginal(cmd.isOriginal());

		if ( album instanceof AlbumSearchResult ) {
			// add item IDs directly to cmd
			Long[] mediaItemIds = getAlbumItemIds(album);

			ExportItemsCommand newCmd = new ExportItemsCommand();
			newCmd.setDirect(cmd.isDirect());
			newCmd.setDownload(cmd.isDownload());
			newCmd.setItemIds(mediaItemIds);
			cmd = newCmd;
		}

		// set the user-agent parameter
		String ua = request.getHeader(HTTP_USER_AGENT_HEADER);
		if ( StringUtils.hasText(ua) ) {
			mediaRequest.getParameters().put(MediaRequest.USER_AGENT_KEY, ua);
		}

		MediaResponse mediaResponse = null;
		if ( cmd.isDirect() ) {
			mediaResponse = new WebMediaResponse(response, filename);
		}
		WorkInfo info = ioBiz.exportItems(cmd, mediaRequest, mediaResponse, context);
		if ( cmd.isDirect() ) {
			info.get();
			return null;
		}
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		JobInfo job = getWebHelper().createJobInfo(context, info.getTicket());
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, job);
		MessageSourceResolvable msg = new DefaultMessageSourceResolvable(
				new String[] { "download.items.success" }, null, "The items are downloading.");
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT, msg);
		return new ModelAndView(getSuccessView(), viewModel);
	}

	@SuppressWarnings("unchecked")
	private Long[] getAlbumItemIds(Album album) {
		List<Long> itemIds = new LinkedList<Long>();
		for ( MediaItem item : (List<MediaItem>) album.getItem() ) {
			itemIds.add(item.getItemId());
		}
		return itemIds.toArray(new Long[itemIds.size()]);
	}

	@SuppressWarnings("unchecked")
	private Album getRequestAlbum(HttpServletRequest request, ExportItemsCommand cmd, BizContext context) {
		Album album = null;
		if ( cmd.getAlbumId() != null ) {
			album = mediaBiz.getAlbum(cmd.getAlbumId(), context);
		} else if ( cmd.getUserKey() != null && cmd.getMode() != null
				&& !BrowseAlbumsCommand.MODE_ALBUMS.equals(cmd.getMode()) ) {
			// virtual search results album
			BrowseAlbumsCommand baCmd = new BrowseAlbumsCommand();
			baCmd.setMode(cmd.getMode());
			baCmd.setLocale(request.getLocale());
			baCmd.setUserKey(cmd.getUserKey());
			PaginationCriteria pc = getDomainObjectFactory().newPaginationCriteriaInstance();
			pc.setIndexKey(cmd.getAlbumKey());
			SearchResults sr = searchBiz.findAlbumsForBrowsing(baCmd, pc, context);
			List<AlbumSearchResult> searchAlbums = sr.getAlbum();
			if ( searchAlbums.size() > 0 ) {
				album = searchAlbums.get(0);
			}
		} else if ( cmd.getAlbumKey() != null ) {
			album = mediaBiz.getSharedAlbum(cmd.getAlbumKey(), context);
		}
		return album;
	}

	private String sanatizeFilename(String name) {
		return name.replaceAll("[\\/:*?\"<>|]", "_");
	}

	/**
	 * @return the ioBiz
	 */
	public IOBiz getIoBiz() {
		return ioBiz;
	}

	/**
	 * @param ioBiz
	 *        the ioBiz to set
	 */
	public void setIoBiz(IOBiz ioBiz) {
		this.ioBiz = ioBiz;
	}

	/**
	 * @return the workBiz
	 */
	public WorkBiz getWorkBiz() {
		return workBiz;
	}

	/**
	 * @param workBiz
	 *        the workBiz to set
	 */
	public void setWorkBiz(WorkBiz workBiz) {
		this.workBiz = workBiz;
	}

	/**
	 * @return the mediaBiz
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz
	 *        the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	/**
	 * @return the searchBiz
	 */
	public SearchBiz getSearchBiz() {
		return searchBiz;
	}

	/**
	 * @param searchBiz
	 *        the searchBiz to set
	 */
	public void setSearchBiz(SearchBiz searchBiz) {
		this.searchBiz = searchBiz;
	}

}
