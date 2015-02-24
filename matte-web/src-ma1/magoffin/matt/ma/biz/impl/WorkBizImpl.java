/* ===================================================================
 * WorkBizImpl.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 9, 2004 3:37:38 PM.
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
 * $Id: WorkBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.WorkBiz;
import magoffin.matt.ma.util.WorkQueue;
import magoffin.matt.ma.util.WorkScheduler;
import magoffin.matt.ma.util.WorkSchedulers;
import magoffin.matt.util.config.Config;

import org.apache.log4j.Logger;

/**
 * Implementation of WorkBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class WorkBizImpl extends AbstractBiz implements WorkBiz
{
	private static final Logger LOG = Logger.getLogger(WorkBizImpl.class);
	
	private WorkQueue queue = null;
	private WorkSchedulers schedulers = null;

/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) 
{
	int numQueues = Config.getInt(
		ApplicationConstants.CONFIG_ENV,ApplicationConstants.ENV_APP_WORK_QUEUES,1);
	queue = new WorkQueue(numQueues);
	
	int numSchedulers = Config.getInt(
		ApplicationConstants.CONFIG_ENV,ApplicationConstants.ENV_APP_WORK_SCHEDULERS,1);
	schedulers = new WorkSchedulers(numSchedulers);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.WorkBiz#queue(magoffin.matt.ma.util.WorkQueue.WorkRequest)
 */
public void queue(WorkQueue.WorkRequest work) throws MediaAlbumException {
	if ( queue != null ) {
		queue.submitWork(work);
	} else {
		try {
			work.startWork();
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unknown exception performing work",e);
		}
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.WorkBiz#schedule(magoffin.matt.ma.util.WorkScheduler.ScheduleOrdering)
 */
public WorkScheduler schedule(WorkScheduler.ScheduleOrdering ordering) throws MediaAlbumException {
	if ( schedulers == null ) return null;
	WorkScheduler scheduler = schedulers.getScheduler();
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Scheduling work: " +ordering +" with scheduler " +scheduler);
	}
	try {
		scheduler.enter(ordering);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Work scheduled: " +ordering +" with scheduler " +scheduler);
		}
		return scheduler;
	} catch ( InterruptedException e ) {
		LOG.warn("Interrupted scheduling work",e);
		throw new MediaAlbumException("Interrupted scheduling work");
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.WorkBiz#done(magoffin.matt.ma.util.WorkScheduler)
 */
public void done(WorkScheduler scheduler) throws MediaAlbumException {
	if ( scheduler == null ) return;
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Finishing work from scheduler " +scheduler);
	}
	scheduler.done();
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Work finished from scheduler " +scheduler);
	}
}

}
