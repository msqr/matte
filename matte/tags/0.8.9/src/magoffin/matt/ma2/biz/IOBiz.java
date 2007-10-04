/* ===================================================================
 * IOBiz.java
 * 
 * Created Mar 1, 2006 8:57:50 PM
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
 * $Id: IOBiz.java,v 1.13 2007/09/10 10:34:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz;

import java.util.List;
import java.util.Set;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.WorkBiz.WorkRequest;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.support.ExportItemsCommand;

/**
 * API for importing and exporting media.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.13 $ $Date: 2007/09/10 10:34:16 $
 */
public interface IOBiz {
	
	/** 
	 * The name of the XML metadata file within a zip archive used 
	 * by {@link #importMedia(AddMediaCommand, BizContext)}.
	 */
	public static final String IMPORT_MEDIA_XML_METADATA_NAME = "metadata.xml";

	/**
	 * Extension of {@link WorkRequest} to allow for two-phase export
	 * media reqeust handling.
	 * 
	 * <p>Two phase exports work by submitting a <em>null</em> 
	 * {@link MediaResponse} to 
	 * {@link IOBiz#exportItems(ExportItemsCommand, MediaRequest, MediaResponse, BizContext)}.
	 * When this is done, the export will not begin, but the {@link WorkRequest}
	 * instance returned by {@link WorkInfo#getWorkRequest()} will implement
	 * this interface as well. Calling the {@link #setMediaResponse(MediaResponse)}
	 * will then trigger the export to start. This can be useful in 
	 * situations where the work ticket is desired, to pass back to the UI layer,
	 * before then requesting the actual work to start.</p>
	 */
	public interface TwoPhaseExportRequest extends WorkRequest {
		
		/**
		 * Set the {@link MediaResponse}.
		 * 
		 * <p>This can be used when exporting media to handle two-phase
		 * export requests.</p>
		 * 
		 * @param response the MediaResponse to set
		 */
		public void setMediaResponse(MediaResponse response);
	}
	
	/**
	 * Import media into the application.
	 * 
	 * <p>This method is designed to allow for imports that take 
	 * some time to run, and thus should happen asynchronously. Thus 
	 * a <code>WorkInfo</code> object is returned, which will give
	 * details of the status of the import job. The 
	 * {@link WorkBiz#getInfo(long)} method can be used to query the 
	 * status of the job at a later point, using the ticket returned
	 * by {@link WorkInfo#getTicket()}.</p>
	 * 
	 * @param command the media to import
	 * @param context the biz context
	 * @return a WorkInfo with status of the import job
	 */
	WorkInfo importMedia(AddMediaCommand command, BizContext context);
	
	/**
	 * Export media from the application.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param context the biz context
	 * @return a WorkInfo with status of the export job
	 */
	WorkInfo exportMedia(MediaRequest request, MediaResponse response, BizContext context);
	
 	/**
	 * Export a set of media items as a Zip archive.
	 * 
	 * <p>If {@link ExportItemsCommand#getAlbumId()} or 
	 * {@link ExportItemsCommand#getAlbumKey()} is non-null, then this will
	 * export all the items of that album into a zip archive. Otherwise
	 * it will export all the items specified by 
	 * {@link ExportItemsCommand#getItemIds()}. The {@code quality} and 
	 * {@code size} parameters are not used, it will instead rely on 
	 * the setting in the {@link MediaRequest}.</p>
	 * 
	 * <p>This method should support two-phase procesing if the 
	 * {@link MediaResponse} is <em>null</em>. The method should return a 
	 * {@code WorkInfo} as normal but the {@code WorkRequest} returned by
	 * {@link WorkInfo#getWorkRequest()} must implement {@link TwoPhaseExportRequest}.
	 * The work to export the items should not begin until a later time when 
	 * {@link TwoPhaseExportRequest#setMediaResponse(MediaResponse)}
	 * is called. The implementation must wait only for a finite amount
	 * of time for {@code setMediaResponse} to be called, after which time 
	 * the job can be discarded.</p>
	 * 
	 * <p>This is to allow for flexibility with the calling applicaiton 
	 * GUI where the work ticket is needed before the actual OutputStream
	 * is ready for the exported items.</p>
	 * 
	 * @param command the export command
	 * @param request the request (for all media in the album)
	 * @param response the response
	 * @param context the context
	 * @return a WorkInfo with status of the export job
	 */
	WorkInfo exportItems(ExportItemsCommand command, MediaRequest request, 
			MediaResponse response, BizContext context);
	
	/**
	 * Delete media item files.
	 * 
	 * <p>This method deletes the files associated with the MediaItem
	 * instances, but does not delete the MediaItem instances from the 
	 * backend database. It should also clean up any cache files for 
	 * the associated items.</p>
	 * 
	 * @param itemsToDelete the items to delete
	 * @return number of items successfully deleted
	 */
	int deleteMedia(List<MediaItem> itemsToDelete);
	
	/**
	 * Move media item files to a new directory.
	 * 
	 * @param itemsToMove the items to move
	 * @param toCollection the Collection to move them to
	 * @return the number of files moved
	 */
	int moveMedia(List<MediaItem> itemsToMove, Collection toCollection);
	
	/**
	 * Clear all cache files for a particular user, optionally of 
	 * a particular size only.
	 * 
	 * @param user the user to clear cache files for
	 * @param ofSize if specified, a set of sizes to clear
	 * @return the number of files deleted
	 */
	int clearCacheFiles(User user, Set<MediaSize> ofSize);
	
}
