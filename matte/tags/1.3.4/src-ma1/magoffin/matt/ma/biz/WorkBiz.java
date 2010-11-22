/* ===================================================================
 * WorkBiz.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 9, 2004 3:27:55 PM.
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
 * $Id: WorkBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.util.WorkQueue;
import magoffin.matt.ma.util.WorkScheduler;

/**
 * Biz interface for scheduling and queueing work.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface WorkBiz {

/**
 * Queue some work to be done.
 * 
 * <p>This method will immediately return as long as work queues are 
 * configured in the applicaiton. If work queues are not configured
 * then this method simply calls the <code>WorkQueue.WorkRequest#startWork()</code>
 * method and returns after that completes.</p>
 * 
 * @param work the work to do
 * @throws MediaAlbumException if an error occurs
 */
public void queue(WorkQueue.WorkRequest work) throws MediaAlbumException;

/**
 * Schedule some work to be done.
 * 
 * <p>To schedule a piece of work, call this method with the ordering 
 * desired. The method will return when the work is ready to be performed.
 * Once you have completed the work, you <em>must</em> call the 
 * {@link #done(WorkScheduler)} method to release the scheduler.</p>
 * 
 * <p>Typical use of the scheduler is thus:</p>
 * 
 * <pre> WorkScheduler scheduler = null;
 * try {
 *   scheduler = workBiz.schedule(myOrdering);
 *   // do work here
 * } finally {
 *   workBiz.done(scheduler);
 * }</pre>
 * 
 * @param ordering the work to do
 * @return the WorkScheduler, which must be passed back via {@link #done(WorkScheduler)}
 * @throws MediaAlbumException if an error occurs
 */
public WorkScheduler schedule(WorkScheduler.ScheduleOrdering ordering) throws MediaAlbumException;

/**
 * Signal that the work has been completed for the given scheduler.
 * 
 * <p>After completing the work after obtaining a scheduler via 
 * {@link #schedule(WorkScheduler.ScheduleOrdering)}, call tihs method
 * to release the scheduler.</p>
 * 
 * @param scheduler the scheulder as returned from {@link #schedule(WorkScheduler.ScheduleOrdering)}
 * @throws MediaAlbumException if an error occurs
 */
public void done(WorkScheduler scheduler) throws MediaAlbumException;

}
