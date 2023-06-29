/* ===================================================================
 * ItemInfoForm.java
 * 
 * Created Jun 9, 2004 7:18:10 PM
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
 * $Id: ItemInfoForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.ArrayUtil;

import org.apache.struts.action.ActionMapping;

/**
 * Form bean for editing multiple item information.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class ItemInfoForm extends BaseBounceBackHomeForm 
{
	// fields for editing
	private MediaItem[] items;
	private FreeData[] fdata;
	private String[] customDate;
	private int size;

/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) 
{
	super.reset(mapping,request);
	items = new MediaItem[0];
	fdata = new FreeData[0];
	customDate = new String[0];
}

public MediaItem getItem(int idx)
{
	// create MediaItem on demand if doesn't exist (calledy by Struts)
	MediaItem item = (MediaItem)ArrayUtil.getItem(items,idx);
	if ( item == null ) {
		item = new MediaItem();
		items = (MediaItem[])ArrayUtil.setItem(items,idx,item,
				(size <= idx ? idx+1 : size));
	}
	return item;
}

public FreeData getFdata(int idx)
{
	// create FreeData on demand if doesn't exist (calledy by Struts)
	FreeData fd = (FreeData)ArrayUtil.getItem(fdata,idx);
	if ( fd == null ) {
		fd = new FreeData();
		fdata = (FreeData[])ArrayUtil.setItem(fdata,idx,fd,
				(size <= idx ? idx+1 : size));
	}
	return fd;
}

/**
 * @return Returns the size.
 */
public int getSize() {
	return size;
}
/**
 * @param size The size to set.
 */
public void setSize(int size) {
	this.size = size;
}
/**
 * @return Returns the items.
 */
public MediaItem[] getItems() {
	return items;
}
/**
 * @param items The items to set.
 */
public void setItems(MediaItem[] items) {
	this.items = items;
}
/**
 * @return Returns the fdata.
 */
public FreeData[] getFreeData() {
	return fdata;
}
/**
 * @param fdata The fdata to set.
 */
public void setFreeData(FreeData[] fdata) {
	this.fdata = fdata;
}

/**
 * @return Returns the customDate.
 */
public String[] getCustomDate() {
	return customDate;
}

/**
 * @param index the index to set
 * @param customDate The customDate to set.
 */
public void setCustomDate(int index, String customDate) {
	this.customDate = ArrayUtil.setItem(this.customDate,index,customDate);
}

}
