/* ===================================================================
 * WorkBizImplTest.java
 * 
 * Created Feb 28, 2006 9:46:10 AM
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

package magoffin.matt.ma2.biz.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.WorkBiz.WorkRequest;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

/**
 * Test case for the {@link magoffin.matt.ma2.biz.impl.WorkBizImpl} class.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class WorkBizImplTest extends TestCase {

	private final Logger log = Logger.getLogger(WorkBizImplTest.class);
	
	/**
	 * Test that a single worker can complete the work.
	 * @throws Exception if an error occurs
	 */
	public void testSingleWorkerThread() throws Exception {
		WorkBizImpl workBiz = getSingleThreadPriorityQueue();
		doWorkCycle(workBiz,new DoIt() {
			public void go(@SuppressWarnings("hiding") WorkBizImpl workBiz) throws Exception {
				List<WorkInfo> infos = doSomeWorkNoPriority(workBiz);
				for ( WorkInfo info : infos ) {
					log.debug("Status: " +info.get());
				}
			}
		});
	}
	
	/**
	 * Test that a single worker can complete the work when priorities
	 * are used.
	 * @throws Exception if an error occurs
	 */
	public void testSingleWorkerThreadWithPriority() throws Exception {
		WorkBizImpl workBiz = getSingleThreadPriorityQueue();
		doWorkCycle(workBiz,new DoIt() {
			public void go(@SuppressWarnings("hiding") WorkBizImpl workBiz) throws Exception {
				List<WorkInfo> infos = doSomeWorkIncreasingPriority(workBiz);
				for ( WorkInfo info : infos ) {
					log.debug("Status: " +info.get());
				}
			}
		});
	}
	
	/**
	 * Test that a long-running job correctly returns status information
	 * while running and the WorkBiz correctly returns the WorkInfo
	 * from the ticket number.
	 * @throws Exception if an error occurs
	 */
	public void testLongJobStatusInfo() throws Exception {
		WorkBizImpl workBiz = getSingleThreadPriorityQueue();
		doWorkCycle(workBiz,new DoIt() {
			public void go(@SuppressWarnings("hiding") WorkBizImpl workBiz) throws Exception {
				WorkInfo info = workBiz.submitWork(new CountTo100());
				
				boolean gotDoneTicket = false;
				for ( int i = 0; i < 11; i++ ) {
					WorkInfo testInfo = workBiz.getInfo(info.getTicket());
					assertNotNull("Work ticket should be available", testInfo);
					assertEquals(info.getTicket(),testInfo.getTicket());
					if ( info.isDone() ) {
						gotDoneTicket = true;
					}
					Thread.sleep(205);
					if ( log.isDebugEnabled() ) {
						log.debug(String.format("Percent complete: %3.0f%%",
								(info.getAmountCompleted()*100)));
					}
				}
				// at this point work should be 100% complete!
				assertTrue("Work should be 100% complete", info.getAmountCompleted() >= 1.0f);

				WorkInfo testInfo = workBiz.getInfo(info.getTicket());
				if ( gotDoneTicket ) {
					assertNull("Work ticket should no longer be available", testInfo);
				} else {
					assertNotNull("Work ticket should be available", testInfo);
				}
			}
		});
	}
	
	/**
	 * Test that calling finish() still allows running jobs to complete.
	 * @throws Exception if an error occurs
	 */
	public void testShutdownWithLongRunningTask() throws Exception {
		WorkBizImpl workBiz = getSingleThreadPriorityQueue();
		doWorkCycle(workBiz,new DoIt() {
			public void go(@SuppressWarnings("hiding") WorkBizImpl workBiz) throws Exception {
				WorkInfo info = workBiz.submitWork(new CountTo100());
				
				workBiz.finish();
				
				// at this point work should be 100% complete!
				assertTrue("Work should be 100% complete", info.getAmountCompleted() >= 1.0f);
			}
		});
	}
	
	/**
	 * Test that completed jobs are "purged".
	 * @throws Exception if an error occurs
	 */
	public void testCompletedJobsPurged() throws Exception {
		WorkBizImpl workBiz = getSingleThreadPriorityQueue();
		
		// set very short cleanup and remember me times
		workBiz.setScanJobTimerMs(200);
		workBiz.setCompletedJobMinRememberTimeMs(100);
		
		doWorkCycle(workBiz,new DoIt() {
			public void go(@SuppressWarnings("hiding") WorkBizImpl workBiz) throws Exception {
				WorkInfo info = workBiz.submitWork(new CountTo100());
				
				Thread.sleep(3000);
				
				// at this point work should be 100% complete!
				assertTrue("Work should be 100% complete", info.getAmountCompleted() >= 1.0f);
			
				// and the ticket should NOT be returned from the WorkBiz
				WorkInfo testInfo = workBiz.getInfo(info.getTicket());
				assertNull("Test WorkInfo should not be available anymore", testInfo);
			}
		});
	}
	
	private static class CountTo100 implements WorkRequest {
		private float percentComplete = 0.0f;
		public Integer getPriority() {
			return WorkBiz.DEFAULT_PRIORITY;
		}
		public boolean canStart() {
			return true;
		}
		public boolean isTransactional() {
			return true;
		}
		public void startWork() throws Exception {
			// count to 100, sleeping 100ms after each count
			for ( int i = 0; i <= 100; i+=5 ) {
				Thread.sleep(100);
				percentComplete = i/100f;
			}
		}
		public float getAmountCompleted() {
			return percentComplete;
		}
		public String getDisplayName() {
			return "Count to 100";
		}
		public String getMessage() {
			return "Count to 100 message";
		}
		public List<Long> getObjectIdList() {
			return null;
		}
	}
	
	private WorkBizImpl getSingleThreadPriorityQueue() {
		WorkBizImpl workBiz = new WorkBizImpl();
		workBiz.setExecutor(new ThreadPoolExecutor(1,1,1,
				TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>(),
				new ThreadPoolExecutor.CallerRunsPolicy()));
		return workBiz;
	}
	
	private static interface DoIt {
		/** 
		 * Perform the stuff.
		 * @param workBiz the WorkBiz
		 * @throws Exception if an error occurs
		 */
		public void go(WorkBizImpl workBiz) throws Exception;
	}
	
	private void doWorkCycle(WorkBizImpl workBiz, DoIt doIt) throws Exception {
		workBiz.init();
		try {
			doIt.go(workBiz);
		} finally {
			workBiz.finish();
		}
	}

	private List<WorkInfo> doSomeWorkNoPriority(WorkBiz workBiz) {
		List<WorkInfo> results = new LinkedList<WorkInfo>();
		for ( int i = 0; i < 10; i++ ) {
			final String workId = String.valueOf(i);
			results.add(workBiz.submitWork(new WorkRequest() {
				public Integer getPriority() {
					return WorkBiz.DEFAULT_PRIORITY;
				}
				public boolean canStart() {
					return true;
				}
				public boolean isTransactional() {
					return true;
				}
				public void startWork() throws Exception {
					log.debug("Doing work " +workId);
				}
				public float getAmountCompleted() {
					return 0;
				}
				public String getDisplayName() {
					return "Test work 1";
				}
				public String getMessage() {
					return "Test work 1 message";
				}
				public List<Long> getObjectIdList() {
					return null;
				}
			}));
		}
		return results;
	}

	private List<WorkInfo> doSomeWorkIncreasingPriority(WorkBiz workBiz) {
		List<WorkInfo> results = new LinkedList<WorkInfo>();
		for ( int i = 0; i < 10; i++ ) {
			final Integer workId = i;
			results.add(workBiz.submitWork(new WorkRequest() {
				public Integer getPriority() {
					return workId;
				}
				public boolean canStart() {
					return true;
				}
				public boolean isTransactional() {
					return true;
				}
				public void startWork() throws Exception {
					log.debug("Doing work " +workId);
				}
				public float getAmountCompleted() {
					return 0;
				}
				public String getDisplayName() {
					return "Test work 2";
				}
				public String getMessage() {
					return "Test work 2 message";
				}
				public List<Long> getObjectIdList() {
					return null;
				}
			}));
		}
		return results;
	}

}
