/* ===================================================================
 * BaseIntegerQueueThread.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 31, 2004 9:09:40 AM.
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
 * $Id: BaseQueueThread.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

/**
 * Base thread class for managing a queue of objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public abstract class BaseQueueThread implements Runnable 
{
	private static final Integer QUEUE_STOP = new Integer(Integer.MIN_VALUE);
	
	private static final Logger LOG = Logger.getLogger(BaseQueueThread.class);
	
	private Buffer queue;
	private boolean keepGoing;
	
/**
 * Construct the queue thread.
 *
 * <p>This method will create a new queue for processing.</p>
 */
public BaseQueueThread()
{
	queue = BufferUtils.blockingBuffer(
			BufferUtils.synchronizedBuffer(new UnboundedFifoBuffer()));
	keepGoing = true;
}

/**
 * Return a name for this thread.
 * 
 * <p>This name is used in log statements.</p>
 * @return name
 */
public abstract String getThreadName();

/**
 * Handle a new item from the queue.
 * 
 * <p>This method is called by {@link #run()} after a new object has 
 * been added to the queue.</p>
 * 
 * @param o the enqueued item
 */
protected abstract void handleItem(Object o);

/**
 * Method called by {@link #run()} when leaving that method.
 * <p>Extending classes may want to override this method to perform some
 * last-minute cleanup.</p>
 */
protected void exiting() {
	// nothing here
}
	
/* (non-Javadoc)
 * @see java.lang.Runnable#run()
 */
public final void run() {
	if ( LOG.isInfoEnabled() ) {
		LOG.info("Starting "+getThreadName() +" thread " 
				+Integer.toHexString(hashCode()));
	}
	
	while (keepGoing) {
		try {
			Object o = queue.remove();
			
			if (queue.size() < 1 && !keepGoing ) {
				break;
			}
			
			handleItem(o);
			
		} catch ( Exception e ) {
			LOG.error("Unhandled exception in integer queue thread",e);
		}
	}
	
	if ( LOG.isInfoEnabled() ) {
		LOG.info("Exiting "+getThreadName() +" thread " 
				+Integer.toHexString(hashCode()));
	}
	exiting();
}

/**
 * Stop this thread.
 * 
 * <p>The thread will finish processing any items in the queue, 
 * then exit.</p>
 */
public final void stop() {
	if ( LOG.isInfoEnabled() ) {
		LOG.info("Stopping "+getThreadName() +" thread " 
				+Integer.toHexString(hashCode()));
	}
	keepGoing = false;
	queue.add(QUEUE_STOP); // in case nothing in queue
}

/**
 * Add an object to the queue.
 * @param o the object to add
 */
public final void enqueue(Object o) {
	if ( keepGoing && o != null ) {
		queue.add(o);
	}
}

}
