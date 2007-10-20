/* ===================================================================
 * AlbumSecurityInterceptor.java
 * 
 * Created Jun 6, 2007 7:51:01 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.aop;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.support.ExportItemsCommand;
import magoffin.matt.ma2.support.ShareAlbumCommand;
import magoffin.matt.ma2.support.SortAlbumsCommand;
import magoffin.matt.ma2.support.SortMediaItemsCommand;

import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP security interceptor for Album instances.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class AlbumSecurityInterceptor extends AbstractSecurityInterceptor {
	
	private AlbumDao albumDao;

	@Override
	protected boolean isAllowed(MethodInvocation invocation, BizContext context) {
		
		// look for Album or Long in arguments...
		if ( invocation.getArguments() != null ) {
			
			// keep track if we saw an Album argument, and if so, don't 
			// look at Long arguments that follow, to allow things like
			// addMediaItemsToAlbum(Album, Long[]) where the Longs are 
			// MediaItems, not Albums
			boolean foundAlbumInstance = false;
			
			for ( Object o : invocation.getArguments() ) {
				if ( o instanceof Album[] ) {
					foundAlbumInstance = true;
					for ( Album a : (Album[])o ) {
						if ( !allowed(invocation, a, context) ) {
							return false;
						}
					}
				} else if ( o instanceof Album ) {
					foundAlbumInstance = true;
					if ( !allowed(invocation, (Album)o, context) ) {
						return false;
					}
				} else if ( !foundAlbumInstance && o instanceof Long[] ) {
					// treat as album IDs
					for ( Long id : (Long[])o ) {
						Album a = albumDao.get(id);
						if ( a != null ) {
							foundAlbumInstance = true;
						}
						if ( !allowed(invocation, a, context)) {
							return false;
						}
					}
				} else if ( !foundAlbumInstance && o instanceof Long ) {
					// treat as album ID
					Album a = albumDao.get((Long)o);
					if ( a != null ) {
						foundAlbumInstance = true;
					}
					if ( !allowed(invocation, a, context)) {
						return false;
					}
				} else if ( o instanceof ShareAlbumCommand ) {
					ShareAlbumCommand cmd = (ShareAlbumCommand)o;
					Album a = albumDao.get(cmd.getAlbumId());
					if ( !allowed(invocation, a, context)) {
						return false;
					}
				} else if ( o instanceof SortAlbumsCommand ) {
					SortAlbumsCommand cmd = (SortAlbumsCommand)o;
					Album a = albumDao.get(cmd.getAlbumId());
					if ( !allowed(invocation, a, context)) {
						return false;
					}
				} else if ( o instanceof SortMediaItemsCommand ) {
					SortMediaItemsCommand cmd = (SortMediaItemsCommand)o;
					Album a = albumDao.get(cmd.getAlbumId());
					if ( !allowed(invocation, a, context)) {
						return false;
					}
				} else if ( o instanceof ExportItemsCommand ) {
					ExportItemsCommand cmd = (ExportItemsCommand)o;
					if ( cmd.getAlbumId() != null ) {
						Album a = albumDao.get(cmd.getAlbumId());
						if ( !allowed(invocation, a, context) ) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private boolean allowed(MethodInvocation invocation, Album album, 
			BizContext context) {
		if ( album == null ) {
			return true;
		}
		String name = invocation.getMethod().getName();
		boolean getter = name.startsWith("get") || name.startsWith("export");
		if ( getter && album.isAllowAnonymous() ) {
			// if album allows anonymous access, always allow
			return true;
		}
		
		// if album ID is null, allow
		if ( album.getAlbumId() == null ) {
			return true;
		}
		
		// otherwise, user must be owner or admin
		if ( getUserBiz().hasAccessLevel(context.getActingUser(), 
				UserBiz.ACCESS_ADMIN) ) {
			return true;
		}
		return album.getOwner() != null && context.getActingUser() != null
			&& album.getOwner().getUserId().equals(
				context.getActingUser().getUserId());
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
