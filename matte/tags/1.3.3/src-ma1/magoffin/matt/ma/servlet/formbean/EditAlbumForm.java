/* ===================================================================
 * EditAlbumForm.java
 * 
 * Created Apr 29, 2004 8:18:12 AM
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
 * $Id: EditAlbumForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.ArrayUtil;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Formbean for editing an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class EditAlbumForm extends ActionForm
{
	/** The date format for the date: <code>MM/dd/yyyy</code> */
	public static final DateFormat ALBUM_DATE_FORMAT = 
		new SimpleDateFormat("MM/dd/yyyy");

	private static Logger LOG = Logger.getLogger(EditAlbumForm.class);

	//private String name = null;
	//private String comment = null;
	//private Integer album = null;
	//private Integer theme = null;
	//private Integer parent = null;
	//private Integer poster = null;
	//private Integer sortMode = null;
	//private Date albumDate = null;
	private Integer collection = null;
	private String cmd = null;
	private Album a = null;
	private MediaItem[] items = null;
	private int size = 0;
	
	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request)
	{
		items = new MediaItem[0];
		collection = null;
		a = new Album();
		size = 0;
	}

	public void setAlbumDate(String albumDate)
	{
		try {
			a.setAlbumDate(ALBUM_DATE_FORMAT.parse(albumDate));	
		} catch ( Exception e) {
			if ( LOG.isDebugEnabled()) {
				LOG.debug("Unable to parse date: " +albumDate);
			}
		}
	}
	
	public MediaItem[] getItem() 
	{
		return items;
	}
	
	public MediaItem getItem(int idx)
	{
		// create AlbumMedia on demand if doesn't exist
		MediaItem item = (MediaItem)ArrayUtil.getItem(items,idx);
		if ( item == null ) {
			item = new MediaItem();
			items = (MediaItem[])ArrayUtil.setItem(items,idx,item,
					(size <= idx ? idx+1 : size));
		}
		return item;
	}
	
	public Integer getCollection()
	{
		return collection;
	}
	public void setCollection(Integer collectionId)
	{
		this.collection = collectionId;
	}
	public String getCmd()
	{
		return cmd;
	}
	public void setCmd(String cmd)
	{
		this.cmd = cmd;
	}
	public Album getA()
	{
		return a;
	}
	public void setA(Album a)
	{
		this.a = a;
	}
	public int getSize()
	{
		return size;
	}
	public void setSize(int size)
	{
		this.size = size;
	}
}
