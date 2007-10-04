/* ===================================================================
 * RecreateIndexWorkRequest.java
 * 
 * Created Jun 11, 2004 2:05:46 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: RecreateIndexWorkRequest.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.search;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.SearchBiz;
import magoffin.matt.ma.util.WorkQueue.WorkRequest;

/**
 * Work request for recreating the application's search indicies.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class RecreateIndexWorkRequest implements WorkRequest 
{
	private BizFactory bizFactory;

public RecreateIndexWorkRequest(BizFactory bizFactory)
{
	this.bizFactory = bizFactory;
}
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.util.WorkQueue.WorkRequest#startWork()
 */
public void startWork() throws Exception {
	SearchBiz searchBiz = (SearchBiz)bizFactory.getBizInstance(
			BizConstants.SEARCH_BIZ);
	searchBiz.recreateEntireIndex();
}

}
