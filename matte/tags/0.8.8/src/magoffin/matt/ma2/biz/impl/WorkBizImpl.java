/* ===================================================================
 * WorkBizImpl.java
 * 
 * Created Feb 28, 2006 8:28:16 AM
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
 * $Id: WorkBizImpl.java,v 1.22 2007/09/07 08:34:09 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import magoffin.matt.ma2.biz.WorkBiz;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Implementation of WorkBiz that uses an {@link java.util.concurrent.ExecutorService}
 * to schedule the work in different threads.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>executor</dt>
 *   <dd>The {@link java.util.concurrent.ExecutorService} to use for executing
 *   the submitted work in {@link #submitWork(WorkRequest)}. If not configured, 
 *   
 *   <dt>completedJobMinRememberTimeMs</dt>
 *   <dd>The length of time, in milliseconds, to maintain a reference to 
 *   completed jobs. Defaults to 10 minutes.</dd>
 *   
 *   <dt>delayedJobMaxRememberTimeMs</dt>
 *   <dd>The length of time, in milliseconds, to maintain a refernce to 
 *   a delayed job. If a delayed job is found to be delayed longer than this
 *   amount of time, it will be cancelled and removed.</dd>
 *   
 *   <dt>scanJobTimerMs</dt>
 *   <dd>The frequency at which to look for completed jobs to purge as well
 *   as delayed jobs to start. Completed jobs will have to be completed longer 
 *   than <em>completedJobMinRememberTimeMs</em> milliseconds ago. Defaults 
 *   to 2 minutes.</dd>
 *   
 *   <dt>transactionManager</dt>
 *   <dd>The <code>PlatformTransactionManager</code> to manage transactions
 *   with. Each job is executed in a transaction, either by joining an 
 *   exiting one or starting a new one. If left <em>null</em> then no 
 *   transactions will be used.</dd>
 *   
 *   <dt>forceTransactionRollback</dt>
 *   <dd>Force all transactions to be rolled back when the job completes.
 *   This can be used for testing. Defaults to <em>false</em>.</dd>
 * </dl>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.22 $ $Date: 2007/09/07 08:34:09 $
 */
public class WorkBizImpl implements WorkBiz {
	
	private ExecutorService executor = null;
	private long completedJobMinRememberTimeMs = 600000; // 10 minutes
	private long delayedJobMaxRememberTimeMs = 600000; // 10 minutes
	private long scanJobTimerMs = 240000; // 2  minutes
	private PlatformTransactionManager transactionManager;
	private boolean forceTransactionRollback = false;

	private Timer scanJobCleanupTimer = null;
	private Map<Long,MyWorkInfo> jobs = new LinkedHashMap<Long,MyWorkInfo>();
	private Map<Long,MyWorkInfo> delayedJobs = new LinkedHashMap<Long,MyWorkInfo>();

	private final Logger log = Logger.getLogger(WorkBizImpl.class);
	
	/**
	 * Initialize this instance.
	 */
	public void init() {
		if ( log.isInfoEnabled() ) {
			log.info("Starting WorkBiz [" +this +"]");
		}
		// start cleanup thread to purge jobs list
		this.scanJobCleanupTimer = new Timer("ScanJobsTimer", true);
		this.scanJobCleanupTimer.schedule(
				new ScanJobsTask(),
				new Date(),
				this.scanJobTimerMs);
		if ( log.isInfoEnabled() ) {
			log.info("WorkBiz [" +this +"] ready");
		}
	}
	
	/**
	 * Shutdown the work queue.
	 */
	public synchronized void finish() {
		if ( this.scanJobCleanupTimer != null ) {
			if ( this.executor != null && !this.executor.isShutdown() ) {
				if ( log.isInfoEnabled() ) {
					log.info("Shutting down WorkBiz [" +this +"]");
				}
				this.executor.shutdown();
				try {
					this.executor.awaitTermination(60,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					log.warn("Interrupted waiting for jobs to finish: " +e);
				}
			}
			this.scanJobCleanupTimer.cancel();
			this.scanJobCleanupTimer = null;
			processDeleayedJobs();
			this.jobs.clear();
			if ( log.isInfoEnabled() ) {
				log.info("WorkBiz [" +this +"] shut down");
			}
		} else {
			if ( log.isInfoEnabled() ) {
				log.info("WorkBiz [" +this +"] is already shut down");
			}
		}
	}
	
	/**
	 * FutureTask implementation that also implements Comparable so can
	 * work with PriorityBlockingQueue based ThreadPoolExecutor.
	 */
	private class WorkInfoFutureTask extends FutureTask<WorkInfo> 
	implements Comparable<WorkInfoFutureTask> {
		
		private MyWorkInfo myWorkInfo;
		
		private WorkInfoFutureTask(final MyWorkInfo workInfo) {
			super(new Callable<WorkInfo>() {
				public WorkInfo call() throws Exception {
					if ( log.isTraceEnabled() ) {
						log.trace("Thread starting work ticket " +workInfo.ticket);
					}
					try {
						handleWork(workInfo);
						return workInfo;
					} catch ( Exception e ) {
						workInfo.exception = e;
						if ( log.isDebugEnabled() ) {
							log.debug("Work [" +workInfo.ticket 
									+"] threw exception", e);
						}
						throw e;
					} finally {
						if ( log.isTraceEnabled() ) {
							log.trace("Thread finished work ticket " +workInfo.ticket);
						}
					}
				}
			});
			this.myWorkInfo = workInfo;
			this.myWorkInfo.future = this;
		}
		
		public int compareTo(WorkInfoFutureTask other) {
			return myWorkInfo.compareTo(other.myWorkInfo);
		}
		
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.WorkBiz#submitWork(magoffin.matt.ma2.biz.WorkBiz.WorkRequest)
	 */
	public WorkInfo submitWork(WorkRequest work) {
		MyWorkInfo workInfo = new MyWorkInfo(work);
		if ( log.isTraceEnabled() ) {
			log.trace("Submitting work ticket " +workInfo.ticket);
		}

		// add work info to jobs list
		if ( work.canStart() ) {
			this.jobs.put(workInfo.getTicket(),workInfo);
		} else {
			if ( log.isTraceEnabled() ) {
				log.trace("Work ticket " +workInfo.ticket +" cannot start yet");
			}
			this.delayedJobs.put(workInfo.getTicket(), workInfo);
			return workInfo;
		}
		
		processWorkInfo(workInfo);
		return workInfo;
	}

	private void processWorkInfo(final MyWorkInfo workInfo) {
		if ( executor != null && !executor.isShutdown() ) {
			WorkInfoFutureTask ftask = new WorkInfoFutureTask(workInfo);
			executor.execute(ftask);
		} else {
			// execute directly in this thread
			try {
				/*workInfo.future = new Future<WorkInfo>() {
					public boolean cancel(boolean mayInterruptIfRunning) {
						return false;
					}
					public WorkInfo get() throws InterruptedException,
							ExecutionException {
						if ( isDone() ) {
							return workInfo;
						}
						while ( !isDone() ) {
							Thread.sleep(1000);
						}
						return workInfo;
					}
					public WorkInfo get(long timeout, TimeUnit unit)
							throws InterruptedException, ExecutionException,
							TimeoutException {
						// Auto-generated method stub
						return null;
					}
					public boolean isCancelled() {
						return false;
					}
					public boolean isDone() {
						return workInfo.work.getAmountCompleted() >= 100.0;
					}
				};*/
				handleWork(workInfo);
			} catch ( Exception e ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Exception performing work " +workInfo.ticket, e);
				}
				workInfo.exception = e;
			} finally {
				workInfo.forcedDone = true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.WorkBiz#workReadyNow(long)
	 */
	public boolean workReadyNow(long ticket) {
		processDeleayedJobs();
		WorkInfo info = getInfo(ticket);
		if ( info != null && this.executor == null ) {
			// this is only called when single-threaded access is configured
			processWorkInfo((MyWorkInfo)info);
		}
		return info != null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.WorkBiz#getInfo(int)
	 */
	public WorkInfo getInfo(long ticket) {
		WorkInfo result = jobs.get(ticket);
		if ( result == null ) {
			result = delayedJobs.get(ticket);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.WorkBiz#infoExists(long)
	 */
	public boolean infoExists(long ticket) {
		return jobs.containsKey(ticket);
	}

	/**
	 * Timer task to periodically delete completed jobs from jobs list.
	 */
	private class ScanJobsTask extends TimerTask {
		@Override
		public void run() {
			if ( jobs != null ) {
				synchronized ( jobs ) {
					if ( log.isDebugEnabled() ) {
						log.debug("Scanning " +jobs.size() 
								+" remembered jobs for items to purge");
					}
					for ( Iterator<MyWorkInfo> itr = jobs.values().iterator(); 
							itr.hasNext(); ) {
						MyWorkInfo job = itr.next();
						long age = System.currentTimeMillis() - job.completeTime;
						if ( job.isDone() && age > completedJobMinRememberTimeMs ) {
							// ok to discard job now
							if ( log.isInfoEnabled() ) {
								log.info("Discarding " +job 
										+"; work complete and is " +age +"ms old");
							}
							itr.remove();
						}
					}
				}
			}
			processDeleayedJobs();
		}
	}

	private void handleWork(final MyWorkInfo workInfo) {
		TransactionStatus status = null;
		if ( transactionManager != null && workInfo.work.isTransactional() ) {
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			status = transactionManager.getTransaction(def);
		}
		workInfo.startTime = System.currentTimeMillis();
		try {
			if ( log.isDebugEnabled() ) {
				log.debug("Starting work ticket " +workInfo.ticket);
			}
			workInfo.work.startWork();
		} catch ( Throwable e ) {
			workInfo.exception = e;
			if ( status != null ) transactionManager.rollback(status);
		} finally {
			workInfo.completeTime = System.currentTimeMillis();
		}
		if ( workInfo.getException() != null ) {
			log.error("Work " +workInfo +" threw exception",workInfo.exception);
		} else {
			if ( status != null ) {
				if ( forceTransactionRollback ) {
					if ( log.isInfoEnabled() ) {
						log.info("Force rolling back transaction for ticket " 
								+workInfo.getTicket());
					}
					transactionManager.rollback(status);
				} else {
					if ( log.isDebugEnabled() ) {
						log.debug("Commiting transaction for ticket " 
								+workInfo.getTicket());
					}
					transactionManager.commit(status);
				}
			}
			log.debug("Completed work ticket " +workInfo.ticket);
		}
	}

	private void processDeleayedJobs() {
		if ( delayedJobs != null ) {
			List<MyWorkInfo> jobsToResubmit = new LinkedList<MyWorkInfo>();
			synchronized ( delayedJobs ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Scanning " +delayedJobs.size() 
							+" delayed jobs for items to start");
				}
				for ( Iterator<MyWorkInfo> itr = delayedJobs.values().iterator(); 
						itr.hasNext(); ) {
					MyWorkInfo job = itr.next();
					if ( job.work.canStart() ) {
						if ( log.isInfoEnabled() ) {
							log.info("Submitting delayed job ["
									+ job.ticket +"]");
						}
						itr.remove();
						if ( jobs != null ) {
							jobsToResubmit.add(job);
						}
						// only process in this thread if exectuor is defined,
						// otherwise in unit tests will occur in separate thread
						if ( this.executor != null ) {
							processWorkInfo(job);
						}
					} else if ( (job.submitTime+this.delayedJobMaxRememberTimeMs) 
							< System.currentTimeMillis() ) {
						// delayed job never started... remove
						log.warn("Delayed job [" +job.ticket +"] has not started in "
								+this.delayedJobMaxRememberTimeMs +"ms, removing");
						job.cancel(false);
						itr.remove();
					}
				}
			}
			if ( jobsToResubmit.size() > 0 ) {
				synchronized ( jobs ) {
					for ( MyWorkInfo job : jobsToResubmit ) {
						jobs.put(job.ticket, job);
					}
				}
			}
		}
	}

	private static class MyWorkInfo implements WorkInfo {

		private static AtomicLong TICKET_COUNTER = new AtomicLong(0);
		
		private long ticket;
		private long submitTime;
		private long startTime;
		private long completeTime;
		private boolean forcedDone;
		private List<Long> objectIdList;
		private Throwable exception;
		private WorkRequest work;
		private Integer priority;
		private Future<WorkInfo> future;
		
		private MyWorkInfo(WorkRequest work) {
			this.ticket = TICKET_COUNTER.incrementAndGet();
			this.work = work;
			this.submitTime = System.currentTimeMillis();
			this.startTime = completeTime = 0;
			this.exception = null;
			this.priority = work.getPriority() != null
				? work.getPriority() : DEFAULT_PRIORITY;
			this.forcedDone = false;
			this.objectIdList = work.getObjectIdList();
		}
		
		public WorkRequest getWorkRequest() {
			return work;
		}

		public int compareTo(WorkInfo other) {
			if ( !this.priority.equals(other.getPriority()) ) {
				return other.getPriority().compareTo(this.priority);
			}
			
			// tickets can never (nearly!) be equal here...
			return this.ticket < other.getTicket() ? -1 : 1;
		}
		
		@Override
		public String toString() {
			SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.S");
			return "WorkInfo{ticket="
				+ticket +",submit=" +df.format(new Date(submitTime))
				+",start=" +startTime +",complete=" +completeTime
				+",exception=" +exception +",work=" +work +"}";
		}

		public List<Long> getObjectIds() {
			return this.objectIdList;
		}

		public Integer getPriority() {
			return priority;
		}

		public float getAmountCompleted() {
			return work.getAmountCompleted();
		}

		public String getDisplayName() {
			return work.getDisplayName();
		}

		public String getMessage() {
			return work.getMessage();
		}

		public Throwable getException() {
			return exception;
		}

		public long getCompleteTime() {
			return completeTime;
		}

		public long getStartTime() {
			return startTime;
		}

		public long getSubmitTime() {
			return submitTime;
		}

		public long getTicket() {
			return ticket;
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			if ( future == null ) {
				this.forcedDone = true;
				return false;
			}
			return future.cancel(mayInterruptIfRunning);
		}

		public boolean isCancelled() {
			if ( future == null ) return forcedDone;
			return future.isCancelled();
		}

		public boolean isDone() {
			if ( forcedDone ) return true;
			if ( future == null ) return false;
			return future.isDone();
		}

		public WorkInfo get() throws InterruptedException, ExecutionException {
			if ( future == null ) return null;
			return future.get();
		}

		public WorkInfo get(long timeout, TimeUnit units) throws InterruptedException, ExecutionException, TimeoutException {
			if ( future == null ) return null;
			return future.get(timeout,units);
		}

	}

	/**
	 * @return Returns the executor.
	 */
	public ExecutorService getExecutor() {
		return executor;
	}

	/**
	 * @param executor The executor to set.
	 */
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * @return Returns the scanJobTimerMs.
	 */
	public long getScanJobTimerMs() {
		return scanJobTimerMs;
	}

	/**
	 * @param scanJobTimerMs The scanJobTimerMs to set.
	 */
	public void setScanJobTimerMs(long scanJobTimerMs) {
		this.scanJobTimerMs = scanJobTimerMs;
	}

	/**
	 * @return Returns the completedJobMinRememberTimeMs.
	 */
	public long getCompletedJobMinRememberTimeMs() {
		return completedJobMinRememberTimeMs;
	}

	/**
	 * @param completedJobMinRememberTimeMs The completedJobMinRememberTimeMs to set.
	 */
	public void setCompletedJobMinRememberTimeMs(long completedJobMinRememberTimeMs) {
		this.completedJobMinRememberTimeMs = completedJobMinRememberTimeMs;
	}

	/**
	 * @return Returns the transactionManager.
	 */
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * @param transactionManager The transactionManager to set.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * @return Returns the forceTransactionRollback.
	 */
	public boolean isForceTransactionRollback() {
		return forceTransactionRollback;
	}

	/**
	 * @param forceTransactionRollback The forceTransactionRollback to set.
	 */
	public void setForceTransactionRollback(boolean forceTransactionRollback) {
		this.forceTransactionRollback = forceTransactionRollback;
	}

	/**
	 * @return the delayedJobMaxRememberTimeMs
	 */
	public long getDelayedJobMaxRememberTimeMs() {
		return delayedJobMaxRememberTimeMs;
	}

	/**
	 * @param delayedJobMaxRememberTimeMs the delayedJobMaxRememberTimeMs to set
	 */
	public void setDelayedJobMaxRememberTimeMs(long delayedJobMaxRememberTimeMs) {
		this.delayedJobMaxRememberTimeMs = delayedJobMaxRememberTimeMs;
	}

}
