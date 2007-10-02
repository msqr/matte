/* ===================================================================
 * MediaItemIndexInterceptor.java
 * 
 * Created Oct 8, 2006 6:27:25 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: MediaItemIndexInterceptor.java,v 1.6 2007/06/20 21:51:25 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.aop;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.WorkBiz.WorkRequest;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.MediaInfoCommand;
import magoffin.matt.ma2.support.ShareAlbumCommand;

import org.springframework.context.MessageSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Interceptor to support automatic indexing of updated MediaItem domain objects.
 * 
 * <p>This interceptor supports any of the following:</p>
 * 
 * <ol>
 *   <li>A {@link magoffin.matt.ma2.domain.MediaItem} domain object as the
 *   <em>returnValue</em></li>
 *   
 *   <li>A <code>Long</code> MediaItem ID as the <em>returnValue</em></li>
 *   
 *   <li>A {@link MediaInfoCommand} intance as a method argument</li>
 * </ol>
 * 
 * <p>Using the <code>itemId</code> of the found object, or the list of 
 * item IDs, this class will call the 
 * {@link magoffin.matt.ma2.biz.IndexBiz#indexMediaItem(Long)} 
 * method to index the item.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.6 $ $Date: 2007/06/20 21:51:25 $
 */
public class MediaItemIndexInterceptor extends AbstractIndexInterceptor {
	
	private WorkBiz workBiz;
	private MessageSource messages;
	private AlbumDao albumDao;
	private TransactionTemplate transactionTemplate;
	
	/* (non-Javadoc)
	 * @see org.springframework.aop.AfterReturningAdvice#afterReturning(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		Long[] itemIds = null;
		BizContext context = null;
		if ( args != null ) {
			for ( Object o : args ) {
				if ( o instanceof BizContext ) {
					context = (BizContext)o;
					break;
				}
			}
		}
		
		if ( returnValue instanceof MediaItem ) {
			itemIds = new Long[] {((MediaItem)returnValue).getItemId()};
		} else if ( returnValue instanceof Long 
				&& !method.getName().contains("Album") ) {
			itemIds = new Long[] {(Long)returnValue};
		} else if ( returnValue instanceof WorkInfo ) {
			// submit a new work request to index the item IDs returned
			// from the WorkInfo
			final WorkInfo work = (WorkInfo)returnValue;
			final Locale locale = context != null ? context.getLocale() : null;
			workBiz.submitWork(new WorkRequest() {
				
				private float completed = 0.0f;
				private int numIndexed = 0;

				public float getAmountCompleted() {
					return completed;
				}

				public String getDisplayName() {
					return messages.getMessage(
							"index.media.work.displayName", null,
							"Indexing media", locale);
				}

				public String getMessage() {
					return messages.getMessage(
							"index.media.work.message", 
							new Object[]{numIndexed, work.getObjectIds().size()},
							"Indexed " +numIndexed +" items", locale);
				}

				public List<Long> getObjectIdList() {
					return work.getObjectIds();
				}

				public Integer getPriority() {
					return WorkBiz.LOW_PRIORITY;
				}

				public boolean canStart() {
					return work.isDone() && work.getException() == null;
				}

				public boolean isTransactional() {
					return true;
				}

				public void startWork() throws Exception {
					for ( Long itemId : work.getObjectIds() ) {
						getIndexBiz().indexMediaItem(itemId);
						numIndexed++;
						completed = ((float)numIndexed) 
							/ ((float)work.getObjectIds().size());
					}
				}
				
			});
		} else if ( args != null) {
			// look through args for MediaInfoCommand
			Long albumId = null;
			for ( Object arg : args ) {
				if ( arg instanceof MediaInfoCommand ) {
					itemIds = ((MediaInfoCommand)arg).getItemIds();
					break;
				} else if ( arg instanceof ShareAlbumCommand ) {
					final ShareAlbumCommand cmd = (ShareAlbumCommand)arg;
					albumId = cmd.getAlbumId();
					break;
				} else if ( method.getName().startsWith("storeMediaItem")
						&& arg instanceof Long[]) {
					itemIds = (Long[])arg;
					break;
				} else if ( method.getName().endsWith("Album") 
						&& arg instanceof Long[] ) {
					itemIds = (Long[])arg;
					break;
				} else if ( method.getName().endsWith("Album") 
						&& arg instanceof Album ) {
					albumId = ((Album)arg).getAlbumId();
					break;
				} else if ( method.getName().equals("unShareAlbum")
						&& arg instanceof Long) {
					albumId = (Long)arg;
					break;
				}
			}
			if ( albumId != null ) {
				final Long tmpId = albumId;
				final List<Long> itemIdList = new LinkedList<Long>();
				getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						Album a = albumDao.getAlbumWithItems(tmpId);
						for ( MediaItem item : (List<MediaItem>)a.getItem() ) {
							itemIdList.add(item.getItemId());
						}
					}
				});
				itemIds = itemIdList.toArray(new Long[itemIdList.size()]);
			}
		}

		if ( itemIds != null ) {
			for ( Long id  : itemIds ) {
				getIndexBiz().indexMediaItem(id);
			}
		} else if ( log.isDebugEnabled() ) {
			log.debug("MediaItem ID(s) not found from Method [" +method +"]");
		}
	}
	
	/**
	 * @return the messages
	 */
	public MessageSource getMessages() {
		return messages;
	}
	
	/**
	 * @param messages the messages to set
	 */
	public void setMessages(MessageSource messages) {
		this.messages = messages;
	}
	
	/**
	 * @return the workBiz
	 */
	public WorkBiz getWorkBiz() {
		return workBiz;
	}
	
	/**
	 * @param workBiz the workBiz to set
	 */
	public void setWorkBiz(WorkBiz workBiz) {
		this.workBiz = workBiz;
	}
	
	/**
	 * @return the albumDao
	 */
	public AlbumDao getAlbumDao() {
		return albumDao;
	}
	
	/**
	 * @param albumDao the albumDao to set
	 */
	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}
	
	/**
	 * @return the transactionTemplate
	 */
	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}
	
	/**
	 * @param transactionTemplate the transactionTemplate to set
	 */
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

}
