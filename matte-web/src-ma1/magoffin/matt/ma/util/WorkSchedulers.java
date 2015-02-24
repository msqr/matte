/* ===================================================================
 * WorkSchedulers.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Mar 2, 2003.
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
 * $Id: WorkSchedulers.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

/**
 * A grouping of WorkScheduler objects to allow for simultaneous scheduling.
 * 
 * <p>Created Mar 2, 2003 5:12:34 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 * @see WorkScheduler
 */
public final class WorkSchedulers 
{
	private int nextScheduler;
	private WorkScheduler[] schedulers;
	

/**
 * Construct a WorkSchedulers with <var>numSchedulers</var> schedulers.
 * @param numSchedulers positive number of work schedulers
 */
public WorkSchedulers(int numSchedulers)
{
	if ( numSchedulers < 1 ) {
		throw new IllegalArgumentException("Number of work schedulers must be greater than 0: "
			+ numSchedulers);
	}
	schedulers = new WorkScheduler[numSchedulers];

	for ( int i = 0; i < schedulers.length; i++ ) {
		schedulers[i] = new WorkScheduler();
	}
	nextScheduler = -1;	
}


/**
 * Get a WorkScheduler object.
 * @return WorkScheduler
 */
public WorkScheduler getScheduler() {
	if ( schedulers.length < 2 ) {
		return schedulers[0];
	}
	return getNextScheduler();
}


/**
 * Get the next work scheduler.
 * 
 * @return WorkScheduler
 */
synchronized private WorkScheduler getNextScheduler()
{
	nextScheduler++;
	if ( nextScheduler >= schedulers.length ) {
		nextScheduler = 0;
	}
	return schedulers[nextScheduler];
}
	
} // class WorkSchedulers
