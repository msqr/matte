/* ===================================================================
 * JobsInfoController.java
 * 
 * Created Jan 3, 2007 11:55:38 AM
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
 * $Id: JobsInfoController.java,v 1.2 2007/08/08 10:24:46 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.JobInfo;
import magoffin.matt.ma2.web.AbstractController;

import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to get the IDs of all work tickets for the current user.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/08/08 10:24:46 $
 */
public class JobsInfoController extends AbstractController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		Long[] tickets = getWebHelper().getUserWorkTickets(request);
		List<JobInfo> jobInfoList = new LinkedList<JobInfo>();
		for ( Long ticket : tickets ) {
			JobInfo info = getWebHelper().createJobInfo(context, ticket);
			if ( info.getAmountCompleted() < 1.0 ) {
				jobInfoList.add(info);
			}
		}
		Map<String, Object> viewModel = new LinkedHashMap<String, Object>();
		viewModel.put("jobinfo", jobInfoList);
		return new ModelAndView(getSuccessView(),viewModel);
	}
	
}
