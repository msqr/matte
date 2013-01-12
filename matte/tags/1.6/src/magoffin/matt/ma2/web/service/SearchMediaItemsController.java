/* ===================================================================
 * SearchMediaItemsController.java
 * 
 * Created Mar 8, 2007 5:48:05 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.support.MediaSearchCommand;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.util.SimpleThreadSafeDateFormat;
import magoffin.matt.util.ThreadSafeDateFormat;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for searching for media items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class SearchMediaItemsController extends AbstractCommandController {

	/** The default value for the <code>searchDateFormat</code> property. */
	public static final String DEFAULT_SEARCH_DATE_FORMAT = "yyyy-MM-dd";

	private SearchBiz searchBiz;
	private ThreadSafeDateFormat  searchDateFormat 
		= new SimpleThreadSafeDateFormat(DEFAULT_SEARCH_DATE_FORMAT);

	@Override
	protected Object getCommand(HttpServletRequest request) throws Exception {
		MediaSearchCommand cmd = new MediaSearchCommand();
		cmd.setMediaItemTemplate(getDomainObjectFactory().newMediaItemInstance());
		return cmd;
	}

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		MediaSearchCommand cmd = (MediaSearchCommand)command;
		BizContext context = getWebHelper().getBizContext(request, false);
		SearchResults results = searchBiz.findMediaItems(cmd, null, context);
		
		Model model = getDomainObjectFactory().newModelInstance();
		model.setSearchResults(results);
		
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		viewModel.put(WebConstants.DEFALUT_MODEL_OBJECT, model);
		return new ModelAndView(getSuccessView(), viewModel);
	}

	@Override
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		BizContext context = getWebHelper().getBizContext(request, false);
		registerCalendarEditor(binder, context, this.searchDateFormat, null);
	}
	
	/**
	 * @return the searchBiz
	 */
	public SearchBiz getSearchBiz() {
		return searchBiz;
	}
	
	/**
	 * @param searchBiz the searchBiz to set
	 */
	public void setSearchBiz(SearchBiz searchBiz) {
		this.searchBiz = searchBiz;
	}
	
	/**
	 * @return the searchDateFormat
	 */
	public ThreadSafeDateFormat getSearchDateFormat() {
		return searchDateFormat;
	}
	
	/**
	 * @param searchDateFormat the searchDateFormat to set
	 */
	public void setSearchDateFormat(ThreadSafeDateFormat searchDateFormat) {
		this.searchDateFormat = searchDateFormat;
	}

}
