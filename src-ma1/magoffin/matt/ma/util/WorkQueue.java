/* ===================================================================
 * WorkQueue.java
 *
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: WorkQueue.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import org.apache.log4j.Logger;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.UnboundedFifoBuffer;

/**
 * A queue for scheduling work.
 * 
 * <p>This queue is a FIFO queue for scheduling work. To use, call the 
 * {@link #submitWork(WorkRequest)} method, which will add the job to the end of
 * the work queue and immediately return. When the work is ready to be run, the
 * work scheduler will call the WorkRequest's startWork() method.</p>
 * 
 * <p> Created on Dec 18, 2002 2:41:00 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class WorkQueue 
{
	
	private static final Logger log = Logger.getLogger(WorkQueue.class);
	
	private Buffer queue = null;
	private WorkStarter[] workers = null;
	
	/**
	 * Interface for submitting a job to the work queue.
	 *
	 * <p> Created on Dec 18, 2002 2:44:55 PM.</p>
	 *
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static interface WorkRequest
	{
		public void startWork() throws Exception;
	}


	private static class WorkStarter extends Thread
	{
		private Buffer queue = null;
		
		/**
		 * Construct a WorkStarter thread.
		 * 
		 * <p>This will call the {@link Thread#setDaemon(boolean)} method to
		 * turn this thread into a daemon thread, as the {@link #run()} method
		 * never returns. For this to work correctly it is assumed the Buffer
		 * will block when the {@link Buffer#remove()} method is called.</p>
		 * 
		 * @param queue
		 */
		public WorkStarter(Buffer queue)
		{
			this.queue = queue;
			this.setDaemon(true);
		}
		
		/**
		 * Process work requests.
		 * 
		 * <p>This method will never return. It will block 
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			while (true) {
				if ( log.isDebugEnabled() ) {
					log.debug("Waiting for work");
				}
				WorkRequest work = (WorkRequest)queue.remove();
				try {
					if ( log.isDebugEnabled() ) {
						log.debug("Starting work on " +work);
					}
					work.startWork();
				} catch ( Exception e ) {
					log.error("WorkRequest threw exception",e);
				}
			}
		}
		
	}


/**
 * Construct a WorkQueue object.
 * @param numWorkers the number of workers to monitor the queue
 */
public WorkQueue(int numWorkers)
{
	if ( numWorkers < 1 ) {
		throw new IllegalArgumentException("Number of queues must be greater than 0");
	}
	queue = BufferUtils.blockingBuffer(new UnboundedFifoBuffer());
	workers = new WorkStarter[numWorkers];
	for ( int i = 0; i < numWorkers; i++ ) {
		workers[i] = new WorkStarter(queue);
		workers[i].start();
	}
}

	
/**
 * Submit a work request into the queue.
 * @param request
 */
public void submitWork(WorkRequest request)
{
	queue.add(request);
}

} // class WorkQueue
