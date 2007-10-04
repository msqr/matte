/* ===================================================================
 * MediaItemSecurityInterceptor.java
 * 
 * Created Jun 7, 2007 7:37:25 PM
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
 * $Id: MediaItemSecurityInterceptor.java,v 1.6 2007/09/05 10:34:02 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.aop;

import java.util.List;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.ExportItemsCommand;
import magoffin.matt.ma2.support.MoveItemsCommand;
import magoffin.matt.ma2.support.SortMediaItemsCommand;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP security interceptor for MediaItem instances.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.6 $ $Date: 2007/09/05 10:34:02 $
 */
public class MediaItemSecurityInterceptor extends AbstractSecurityInterceptor {
	
	private AlbumDao albumDao;
	private MediaItemDao mediaItemDao;
	private CollectionDao collectionDao;
	
	@Override
	protected boolean isAllowed(MethodInvocation invocation, BizContext context) {
		// look for MediaItem or Long in arguments...
		if ( invocation.getArguments() != null ) {
			for ( Object o : invocation.getArguments() ) {
				if ( o instanceof MediaRequest ) {
					MediaRequest request = (MediaRequest)o;
					if ( !allowed(invocation, request.getMediaItemId(), 
							request.isOriginal(), context) ) {
						return false;
					}
				} else if ( o instanceof MediaItem[] ) {
					for ( MediaItem item : (MediaItem[])o ) {
						if ( !allowed(invocation, item.getItemId(), 
								false, context) ) {
							return false;
						}
					}
				} else if ( o instanceof MediaItem ) {
					if ( !allowed(invocation, ((MediaItem)o).getItemId(), 
							false, context) ) {
						return false;
					}
				} else if ( o instanceof Long[] ) {
					// treat as mediaItem IDs
					for ( Long id : (Long[])o ) {
						if ( !allowed(invocation, id, false, context)) {
							return false;
						}
					}
				} else if ( o instanceof Long ) {
					
					if ( invocation.getMethod().getName().contains("Album") ) {
						// treat as album ID, skip
						continue;
					}
					
					// treat as mediaItem ID
					if ( !allowed(invocation, (Long)o, false, context)) {
						return false;
					}
					if ( invocation.getMethod().getName().equals(
							"storeMediaItemPoster")) {
						break; // don't check remainder arguments
					}
				} else if ( o instanceof SortMediaItemsCommand ) {
					SortMediaItemsCommand cmd = (SortMediaItemsCommand)o;
					for ( Long itemId : cmd.getItemIds() ) {
						if ( !allowed(invocation, itemId, false, context) ) {
							return false;
						}
					}
				} else if ( o instanceof ExportItemsCommand ) {
					ExportItemsCommand cmd = (ExportItemsCommand)o;
					if ( cmd.getItemIds() != null ) {
						for ( Long itemId : cmd.getItemIds() ) {
							if ( !allowed(invocation, itemId, cmd.isOriginal(), 
									context) ) {
								return false;
							}
						}
					}
				} else if ( o instanceof MoveItemsCommand ) {
					MoveItemsCommand cmd = (MoveItemsCommand)o;
					if ( cmd.getCollectionId() != null ) {
						// must be owner of collection to move to
						Collection c = collectionDao.get(cmd.getCollectionId());
						if ( c != null && !c.getOwner().getUserId().equals(
								context.getActingUser().getUserId())) {
							return false;
						}
					}
					if ( cmd.getItemIds() != null ) {
						for ( Long itemId : cmd.getItemIds() ) {
							if ( !allowed(invocation, itemId, false, context) ) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	private boolean allowed(MethodInvocation invocation, Long mediaItemId, 
			boolean wantOriginal, BizContext context) {
		if ( mediaItemId == null ) {
			return true;
		}
		
		Ehcache securityCache = getSecurityCache();
		String name = invocation.getMethod().getName();
		boolean getter = name.startsWith("get") || name.startsWith("export");
		String cacheKey = mediaItemId.toString();
		if ( wantOriginal ) {
			cacheKey += ":original";
		}
		
		// if a getter method, allow if is a public item
		if ( getter ) {
			// check in public cache first
			Element cachedElement = securityCache == null
				? null : securityCache.get(cacheKey);
			if ( cachedElement != null ) {
				return ((Boolean)cachedElement.getValue()).booleanValue();
			}
			
			// see if public, that is available in a shared album
			MediaItem mediaItem = mediaItemDao.get(mediaItemId);
			if ( mediaItem == null ) {
				return true;
			}
			List<Album> sharedAlbums 
				= albumDao.findSharedAlbumsContainingItem(mediaItem);
			if ( sharedAlbums.size() > 0 ) {
				boolean allowed = !wantOriginal && sharedAlbums.size() > 0;
				if ( wantOriginal ) {
					for ( Album a : sharedAlbums ) {
						if ( a.isAllowOriginal() ) {
							allowed = true;
							break;
						}
					}
				}
				if ( allowed ) {
					if ( securityCache != null ) {
						securityCache.put(new Element(cacheKey, 
								Boolean.valueOf(allowed)));
					}
					return allowed;
				}
			}
		}
		
		// otherwise, user must be admin or owner
		if ( getUserBiz().hasAccessLevel(context.getActingUser(), 
				UserBiz.ACCESS_ADMIN) ) {
			return true;
		}
		
		cacheKey = (context.getActingUser() != null
			? context.getActingUser().getUserId() : "ANONYMOUS")
			+":" +mediaItemId;
		Element cachedElement = securityCache == null
			? null : securityCache.get(cacheKey);
		if ( cachedElement != null ) {
			return ((Boolean)cachedElement.getValue()).booleanValue();
		}
		
		Collection c = collectionDao.getCollectionForMediaItem(
				mediaItemId);
		boolean result = c.getOwner() != null && context.getActingUser() != null
			&& c.getOwner().getUserId().equals(
				context.getActingUser().getUserId());
		if ( securityCache != null ) {
			securityCache.put(new Element(cacheKey, Boolean.valueOf(result)));
		}
		return result;
	}
	
	/**
	 * @return the mediaItemDao
	 */
	public MediaItemDao getMediaItemDao() {
		return mediaItemDao;
	}
	
	/**
	 * @param mediaItemDao the mediaItemDao to set
	 */
	public void setMediaItemDao(MediaItemDao mediaItemDao) {
		this.mediaItemDao = mediaItemDao;
	}

	/**
	 * @return the collectionDao
	 */
	public CollectionDao getCollectionDao() {
		return collectionDao;
	}
	
	/**
	 * @param collectionDao the collectionDao to set
	 */
	public void setCollectionDao(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
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

}
