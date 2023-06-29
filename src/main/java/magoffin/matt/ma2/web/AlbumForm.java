/* ===================================================================
 * AlbumForm.java
 * 
 * Created Nov 25, 2006 5:35:17 PM
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
 */

package magoffin.matt.ma2.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Edit;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.util.SimpleThreadSafeDateFormat;
import magoffin.matt.util.ThreadSafeDateFormat;

import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller for administering album details.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>albumDateFormat</dt>
 *   <dd>The date format to use for parsing album date strings.</dd>
 *   
 *   <dt>mediaBiz</dt>
 *   <dd>The {@link MediaBiz} to use for storing the album.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class AlbumForm extends AbstractForm {
	
	/** The default value for the <code>albumDateFormat</code> property. */
	public static final String DEFAULT_ALBUM_DATE_FORMAT = "yyyy-MM-dd";
	
	/** A UiMetadata key for removing an album parent, if the value is "true". */
	public static final String UI_METADATA_REMOVE_PARENT = "removeAlbumParent";

	private MediaBiz mediaBiz;
	private ThreadSafeDateFormat albumDateFormat 
		= new SimpleThreadSafeDateFormat(DEFAULT_ALBUM_DATE_FORMAT);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) 
	throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		Model ui = getDomainObjectFactory().newModelInstance();
		ui.getAlbumSort().addAll(mediaBiz.getAlbumSortTypes(context));
		
		Edit model = (Edit)command;
		if ( model.getAlbum().getAlbumId() != null ) {
			Album parent = mediaBiz.getAlbumParent(
					model.getAlbum().getAlbumId(), context);
			if ( parent != null ) {
				ui.getAlbum().add(parent);
			}
		}
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		viewModel.put(WebConstants.DEFALUT_REFERENCE_DATA_OBJECT, ui);
		return viewModel;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		Command cmd = new Command();
		BizContext context = getWebHelper().getBizContext(request,true);

		// see if trying to populate album
		ServletRequestDataBinder binder = createBinder(request, cmd);
		binder.bind(request);
		
		Album album = getDomainObjectFactory().newAlbumInstance();
		if ( cmd.getAlbumId() != null ) {
			Album domainAlbum = mediaBiz.getAlbum(cmd.getAlbumId(), context);
			BeanUtils.copyProperties(domainAlbum,album);
		}

		Edit model = getDomainObjectFactory().newEditInstance();
		model.setAlbum(album);
		return model;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command,	BindException errors)
			throws Exception {
		Edit model = (Edit)command;
		BizContext context = getWebHelper().getBizContext(request,true);
		
		boolean isNew = model.getAlbum().getAlbumId() == null;
		
		// save album
		Long albumId = mediaBiz.storeAlbum(model.getAlbum(), context);
		
		// look if should make album top-level
		for ( Metadata meta : (List<Metadata>)model.getUiMetadata() ) {
			if ( UI_METADATA_REMOVE_PARENT.equals(meta.getKey())
					&& Boolean.TRUE.toString().equals(meta.getValue()) ) {
				mediaBiz.storeAlbumParent(albumId, null, context);
			}
		}
		
		Album savedAlbum = mediaBiz.getAlbum(albumId, context);
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		Model ui = getDomainObjectFactory().newModelInstance();
		ui.getAlbum().add(savedAlbum);
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT,ui);

		MessageSourceResolvable msg = null;
		if ( isNew ) {
			msg = new DefaultMessageSourceResolvable(
				new String[] {"add.album.success"}, 
				new Object[]{savedAlbum.getName()},
				"The album has been added.");
		} else {
			msg = new DefaultMessageSourceResolvable(
				new String[] {"update.album.success"}, 
				new Object[]{savedAlbum.getName()},
				"The album has been saved.");
		}
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);

		return new ModelAndView(getSuccessView(),viewModel);
	}

	@Override
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		BizContext context = getWebHelper().getBizContext(request, true);
		registerCalendarEditor(binder, context, this.albumDateFormat, null);
	}
	
	
	/** The command class. */
	public static class Command {
		private Long albumId;

		/**
		 * @return the albumId
		 */
		public Long getAlbumId() {
			return albumId;
		}
		
		/**
		 * @param albumId the albumId to set
		 */
		public void setAlbumId(Long albumId) {
			this.albumId = albumId;
		}
		
	}
	
	/**
	 * @return the albumDateFormat
	 */
	public ThreadSafeDateFormat getAlbumDateFormat() {
		return albumDateFormat;
	}
	
	/**
	 * @param albumDateFormat the albumDateFormat to set
	 */
	public void setAlbumDateFormat(ThreadSafeDateFormat albumDateFormat) {
		this.albumDateFormat = albumDateFormat;
	}

	/**
	 * @return the mediaBiz
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}
	
	/**
	 * @param mediaBiz the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}
	
}
