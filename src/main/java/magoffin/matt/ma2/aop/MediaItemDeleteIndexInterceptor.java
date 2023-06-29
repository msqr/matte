/* ===================================================================
 * MediaItemDeleteIndexInterceptor.java
 * 
 * Created Feb 25, 2007 11:54:18 AM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 */

package magoffin.matt.ma2.aop;

import java.lang.reflect.Method;
import java.util.List;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * Interceptor to support removal of {@link MediaItem} domain objects from 
 * the search index.
 * 
 * <p>This interceptor will first inspect the returned object. If this is a 
 * {@link MediaItem} or array of {@link MediaItem} then those the item IDs
 * of those items will be passed to
 * {@link magoffin.matt.ma2.biz.IndexBiz#removeMediaItemFromIndex(Long)}.</p>
 * 
 * <p>Otherwise if a <code>Long</code> array value exists on the 
 * <code>args</code> parameter passed to the 
 * {@link #afterReturning(Object, Method, Object[], Object)} method, it will
 * assume the first Long[] object found is an array of {@link MediaItem#getItemId()}
 * values that should be removed from the media item index via 
 * {@link magoffin.matt.ma2.biz.IndexBiz#removeMediaItemFromIndex(Long)}.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class MediaItemDeleteIndexInterceptor extends AbstractIndexInterceptor {

	/* (non-Javadoc)
	 * @see org.springframework.aop.AfterReturningAdvice#afterReturning(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		if ( returnValue instanceof MediaItem[] ) {
			MediaItem[] items = (MediaItem[]) returnValue;
			for ( MediaItem item : items ) {
				if ( item.getItemId() != null ) {
					getIndexBiz().removeMediaItemFromIndex(item.getItemId());
				}
			}
			return;
		} else if ( returnValue instanceof List ) {
			List<?> resultList = (List<?>)returnValue;
			if ( resultList.size() > 0 && resultList.get(0) instanceof MediaItem ) {
				for ( MediaItem item : (List<MediaItem>)resultList ) {
					if ( item.getItemId() != null ) {
						getIndexBiz().removeMediaItemFromIndex(item.getItemId());
					}
				}
				return;
			}
		} else if ( returnValue instanceof MediaItem ) {
			MediaItem item = (MediaItem)returnValue;
			if ( item.getItemId() != null ) {
				getIndexBiz().removeMediaItemFromIndex(item.getItemId());
			}
			return;
		}
		
		// assume one parameter is the Long[] of the items to delete
		Long[] itemIds = null;
		for ( int i = 0; i < args.length && itemIds == null; i++ ) {
			if ( args[i] instanceof Long[] ) {
				itemIds = (Long[])args[i];
			}
		}
		
		if ( itemIds == null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("No MediaItem IDs to delete available in method arguments");
			}
			return;
		}
		
		for ( Long itemId : itemIds ) {
			getIndexBiz().removeMediaItemFromIndex(itemId);
		}
	}

}
