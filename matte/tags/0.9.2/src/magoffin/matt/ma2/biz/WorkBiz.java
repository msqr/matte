/* ===================================================================
 * WorkBiz.java
 * 
 * Created Feb 28, 2006 8:10:18 AM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.biz;

import java.util.List;
import java.util.concurrent.Future;

/**
 * API for a work queue and scheduling tasks to keep the application
 * from overloading the system it's running on.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public interface WorkBiz {

	/** The default priority applied to work requests if none specified. */
	public static final Integer DEFAULT_PRIORITY = new Integer(0);

	/** A low priority. */
	public static final Integer LOW_PRIORITY = new Integer(10);

	/** A high priority. */
	public static final Integer HIGH_PRIORITY = new Integer(-10);

	/**
	 * Interface for submitting a job to the work queue.
	 */
	public static interface WorkRequest {
		
		/**
		 * Get a display name for this work request.
		 * @return display name
		 */
		public String getDisplayName();
		
		/**
		 * Get a message to pass to the result work info.
		 * @return message
		 */
		public String getMessage();
		
		/**
		 * Get a priority for the work.
		 * @return the priority
		 */
		public Integer getPriority();
		
		/**
		 * Return boolean indicator if this work is ready
		 * to be started.
		 * 
		 * <p>Some work requests might have dependancies on 
		 * other tasks. This method allows a work request to
		 * inform the WorkBiz if this task can be started. If
		 * not, the WorkBiz will schedule the task to be 
		 * run only after this method returns <em>true</em>.</p>
		 * 
		 * @return boolean
		 */
		public boolean canStart();
		
		/**
		 * Start performing the work.
		 * @throws Exception if an error occurs
		 */
		public void startWork() throws Exception;
		
		/**
		 * Get the amount of work that has been completed, from 0 to 1.
		 * @return the amount of work completed, as a fraction from 0 to 1
		 */
		public float getAmountCompleted();
		
		/**
		 * Get a List to use for holding object IDs.
		 * @return a List, or <em>null</em> if not used
		 */
		public List<Long> getObjectIdList();
		
		
		/**
		 * Return boolean flag indicating if this work request requires
		 * a transaction or not.
		 * 
		 * @return boolean
		 */
		public boolean isTransactional();
	}
	
	/**
	 * Work information created by submitting WorkRequest instances.
	 * 
	 * <p>This interface extends {@link Future} so calling code can choose
	 * to wait for the work to complete by calling {@link Future#get()} if you
	 * desire. The WorkInfo returned by {@link Future#get()} will be the 
	 * same instance as returned by {@link WorkBiz#submitWork(WorkRequest)},
	 * this is just a convenience.</p>
	 * 
	 * <p>In addition this interface extends {@link Comparable} so that 
	 * work can be prioritized according to the natural ording of the 
	 * implementing class. The {@link #getPriority()} should be treated 
	 * as such that higher values are given a higher priority over lower
	 * values. For jobs with equal priority, a FIFO ordering should be 
	 * used.</p>
	 */
	public static interface WorkInfo extends Comparable<WorkInfo>, Future<WorkInfo> {
		
		/**
		 * Get the original {@link WorkRequest} associated with this info.
		 * @return the work request
		 */
		public WorkRequest getWorkRequest();
		
		/**
		 * Get a display-friendly name for this task.
		 * @return display name
		 */
		public String getDisplayName();

		/**
		 * Get a message to pass to the result work info.
		 * @return message
		 */
		public String getMessage();
		
		/**
		 * Get the priority assigned to the work.
		 * @return Returns the priority.
		 */
		public Integer getPriority();

		/**
		 * Get the amount of work that has been completed, from 0 to 1.
		 * @return the amount of work completed, as a fraction from 0 to 1
		 */
		public float getAmountCompleted();
		
		/**
		 * @return Returns the exception.
		 */
		public Throwable getException();
		
		/**
		 * @return Returns the completeTime.
		 */
		public long getCompleteTime();
		
		/**
		 * @return Returns the startTime.
		 */
		public long getStartTime();
		
		/**
		 * @return Returns the submitTime.
		 */
		public long getSubmitTime();
		
		/**
		 * @return Returns the ticket.
		 */
		public long getTicket();
		
		/**
		 * Get a List of object IDs related to this work.
		 * @return List of object IDs, or <em>null</em> if none available
		 */
		public List<Long> getObjectIds();

	}

	/**
	 * Submit a work request for processing.
	 * @param work the work to perform
	 * @return a WorkInfo detailing the status of the work
	 */
	WorkInfo submitWork(WorkRequest work);
	
	/**
	 * Get information about a running or recently running
	 * job.
	 * 
	 * <p>If the job is still running calling this method must 
	 * always return the work info. If the job has completed, 
	 * it is up to the implementation of this API if the info
	 * is returned or not. For practical purposes implementations
	 * should be able to return information about completed jobs
	 * for a reasonable length of time after the job completes, 
	 * to give time for clients to query the status of the job.</p>
	 * 
	 * <p><b>Note:</b> calling this method might have side effects,
	 * like purging completed jobs. Use the {@link #infoExists(long)}
	 * method to test for the existance of a job without any of these
	 * potential side effects.</p>
	 * 
	 * @param ticket the work ticket number
	 * @return the WorkInfo for the requested ticket, or <em>null</em>
	 * if not available
	 */
	WorkInfo getInfo(long ticket);
	
	/**
	 * Check for the existance of a running or recently complete job.
	 * 
	 * <p>This method can be used to check if a job is known, without
	 * any side-effects calling {@link #getInfo} might include (such as 
	 * resetting access statitics, purging a complete job, etc).</p>
	 * 
	 * @param ticket the work ticket number
	 * @return boolean if info is available for the given work ticket number
	 */
	boolean infoExists(long ticket);

	/**
	 * Signal that a particular job is ready to begin work now, 
	 * presumably after being delayed.
	 * @param ticket the work ticket number that is ready
	 * @return boolean if that work was found
	 */
	boolean workReadyNow(long ticket);
}
