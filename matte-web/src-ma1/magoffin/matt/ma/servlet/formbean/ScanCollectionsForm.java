/* ===================================================================
 * ScanCollectionForm.java
 * 
 * Created Jun 10, 2004 9:40:13 PM
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
 * $Id: ScanCollectionsForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Form bean for performing scan on collections.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class ScanCollectionsForm extends ActionForm 
{
	private Integer[] collectionIds;
	private boolean forceScan;
	
/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) {
	collectionIds = new Integer[0];
	forceScan = false;
}

/**
 * @return Returns the collectionIds.
 */
public Integer[] getCollectionIds() {
	return collectionIds;
}
/**
 * @param collectionIds The collectionIds to set.
 */
public void setCollectionIds(Integer[] collectionIds) {
	this.collectionIds = collectionIds;
}
/**
 * @return Returns the forceScan.
 */
public boolean isForceScan() {
	return forceScan;
}
/**
 * @param forceScan The forceScan to set.
 */
public void setForceScan(boolean forceScan) {
	this.forceScan = forceScan;
}
}
