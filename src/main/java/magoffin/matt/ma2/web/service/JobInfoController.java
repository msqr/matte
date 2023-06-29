/* ===================================================================
 * JobInfo.java
 * 
 * Created Mar 6, 2006 9:31:12 PM
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

package magoffin.matt.ma2.web.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.JobInfo;
import magoffin.matt.ma2.web.AbstractCommandController;
import magoffin.matt.ma2.web.util.WebConstants;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to get information about a job.
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public class JobInfoController extends AbstractCommandController {
	
	/**
	 * Command for requesting information about a job.
	 */
	public static class Command {
		private long ticket = -1;

		/**
		 * @return Returns the ticket.
		 */
		public long getTicket() {
			return ticket;
		}

		/**
		 * @param ticket The ticket to set.
		 */
		public void setTicket(long ticket) {
			this.ticket = ticket;
		}
		
	}

	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		Command cmd = (Command)command;
		
		JobInfo job = getWebHelper().createJobInfo(context, cmd.getTicket());
		
		Map<String,Object> model = new LinkedHashMap<String,Object>();
		model.put(WebConstants.DEFALUT_MODEL_OBJECT,job);
		return new ModelAndView(getSuccessView(),model);
	}

}
