/* ===================================================================
 * MediaScanWorkRequest.java
 * 
 * Created Jun 10, 2004 8:06:15 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: MediaScanWorkRequest.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.scan;

import java.util.Iterator;
import java.util.Map;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.util.WorkQueue.WorkRequest;
import magoffin.matt.ma.xsd.Collection;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * Work request for media scan.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class MediaScanWorkRequest implements WorkRequest 
{
	private static final Logger LOG = Logger.getLogger(MediaScanWorkRequest.class);
	
	private Collection[] collections;
	private boolean forceRescan;
	private BizFactory bizFactory;
	
/**
 * Construct a MediaScanWorkRequest.
 * @param collections the collections to scan
 * @param forceRescan <em>true</em> to force a re-scan of all media items 
 * in the collection
 * @param bizFactory the BizFactory
 */
public MediaScanWorkRequest(Collection[] collections, boolean forceRescan,
		BizFactory bizFactory) 
{
	this.collections = collections;
	this.forceRescan = forceRescan;
	this.bizFactory = bizFactory;
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.util.WorkQueue.WorkRequest#startWork()
 */
public void startWork() throws Exception 
{
	MediaScan scanner = new MediaScan(bizFactory,false);
	for ( int i = 0; i < collections.length; i++ ) {
		try {
			if ( forceRescan ) {
				Collection c = (Collection)BeanUtils.cloneBean(collections[i]);
				c.setScandate(null);
				collections[i] = c;
			}
			scanner.doScan(collections[i]);
			if ( scanner.hasErrors() ) {
				StringBuffer buf = new StringBuffer("Error(s) scanning collections:\n");
				Map errMap = scanner.getErrors();
				for ( Iterator itr = errMap.entrySet().iterator(); itr.hasNext(); ) {
					Map.Entry me = (Map.Entry)itr.next();
					buf.append("The file '").append(me.getKey())
						.append("' caused the error: ").append(me.getValue())
						.append("\n");
				}
				LOG.warn(buf.toString());
			}
		} catch ( MediaAlbumException e ) {
			LOG.error("Exception scanning collection " +collections[i].getCollectionId(),e);
		}
	}
}

}
