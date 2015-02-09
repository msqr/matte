/* ===================================================================
 * WorkScheduler.java
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
 * $Id: WorkScheduler.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.util.ArrayList;

/**
 * Class for scheduling media requests.
 * 
 * <p>Media request handlers wishing to have their work scheduled as to not
 * overload the host CPU can use this class to scheule the work so only a 
 * limited number of requests are handled simultaneously.</p>
 * 
 * <p>The scheduler works by first calling the {@link #enter(ScheduleOrdering)}
 * method, which will blcok until it is OK to perform work based on the supplied
 * {@link ScheduleOrdering}. After returning from this method and completing the
 * work to be done, the {@link #done()} method must be called to signal that the
 * work is complete.</p>
 * 
 * <pre>
 * try  {
 *   scheduler.enter(ordering); // get schedule
 *   
 *   // do work here
 * 
 * } catch ( InterruptedException e ) {
 *   // handle exception
 * } finally {
 *   scheduler.  done(); // release scheule
 * }
 * </pre>
 * 
 * <p>This code taken heavily from Mark Grand's Pattern in Java Volume 1
 * Scheduler pattern.</p>
 * 
 * <p> Created on Dec 3, 2002 2:15:18 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class WorkScheduler 
{
	private Thread runningThread;
	private ArrayList waitingRequests;
	private ArrayList waitingThreads;
	
	public static interface ScheduleOrdering
	{
		public boolean scheduleBefore(ScheduleOrdering s);
	}

/**
 * Constructor for WorkScheduler.
 */
public WorkScheduler() 
{
	super();
	this.runningThread = null;
	this.waitingRequests = new ArrayList();
	this.waitingThreads = new ArrayList();
}


/**
 * Enter a scheduled work request.
 * 
 * <p>This method will block until it is the calling thread's time to perform
 * the scheduled work. When the work is done, the calling thread must call
 * the {@link #done()} method.</p>
 * 
 * @param s the ScheudleOrdering instance for the request
 * @throws InterruptedException
 */
public void enter(ScheduleOrdering s) throws InterruptedException
{
	Thread thisThread = Thread.currentThread();
	synchronized (this)
	{
		if ( this.runningThread == null )
		{
			this.runningThread = thisThread;
			return;
		}
		waitingThreads.add(thisThread);
		waitingRequests.add(s);
	}
	synchronized (thisThread)
	{
		while ( thisThread != runningThread ) {
			thisThread.wait();
		}
	}
	synchronized (this)
	{
		int i = waitingThreads.indexOf(thisThread);
		waitingThreads.remove(i);
		waitingRequests.remove(i);
	}
}


/**
 * Signal to the scheuler that the work is complete.
 * 
 * <p>After a thread regains control after calling the 
 * {@link #enter(ScheduleOrdering)} method and has completed its work, it should
 * call this method.</p>
 */
synchronized public void done()
{
	if ( runningThread != Thread.currentThread() ) {
		throw new IllegalStateException("Wrong thread");
	}
	int waitCount = waitingThreads.size();
	if ( waitCount <= 0 ) {
		runningThread = null;
		return;
	} else if ( waitCount == 1 ) {
		runningThread = (Thread)waitingThreads.get(0);
	} else {
		int next = waitCount - 1;
		ScheduleOrdering nextRequest = (ScheduleOrdering)waitingRequests.get(next);
		for ( int i = waitCount-2; i >= 0; i-- ) {
			ScheduleOrdering r = (ScheduleOrdering)waitingRequests.get(i);
			if ( r.scheduleBefore(nextRequest) ) {
				next = i;
				nextRequest = (ScheduleOrdering)waitingRequests.get(next);
			}
		}
		runningThread = (Thread)waitingThreads.get(next);
	}
	synchronized ( runningThread ) {
		runningThread.notifyAll();
	}
}

} // class WorkScheduler
