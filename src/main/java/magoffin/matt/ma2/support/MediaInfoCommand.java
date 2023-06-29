/* ===================================================================
 * MediaInfoCommand.java
 * 
 * Created Oct 4, 2006 4:10:43 PM
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
 */

package magoffin.matt.ma2.support;

import java.util.Calendar;
import java.util.TimeZone;


/**
 * Command object for storing media item info (metadata).
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class MediaInfoCommand {

	private Long[] itemIds;
	private String name;
	private Calendar date;
	private String comments;
	private String copyright;
	private String tags;
	private TimeZone mediaTimeZone;
	private TimeZone displayTimeZone;
	
	/**
	 * @return the copyright
	 */
	public String getCopyright() {
		return copyright;
	}
	
	/**
	 * @param copyright the copyright to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}
	
	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	/**
	 * @return the itemIds
	 */
	public Long[] getItemIds() {
		return itemIds;
	}
	
	/**
	 * @param itemIds the itemIds to set
	 */
	public void setItemIds(Long[] itemIds) {
		this.itemIds = itemIds;
	}
	
	/**
	 * @return the tags
	 */
	public String getTags() {
		return tags;
	}
	
	/**
	 * @param tags the tags to set
	 */
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	/**
	 * @return the date
	 */
	public Calendar getDate() {
		return date;
	}
	
	/**
	 * @param date the date to set
	 */
	public void setDate(Calendar date) {
		this.date = date;
	}

	/**
	 * @return the displayTimeZone
	 */
	public TimeZone getDisplayTimeZone() {
		return displayTimeZone;
	}
	
	/**
	 * @param displayTimeZone the displayTimeZone to set
	 */
	public void setDisplayTimeZone(TimeZone displayTimeZone) {
		this.displayTimeZone = displayTimeZone;
	}
	
	/**
	 * @return the mediaTimeZone
	 */
	public TimeZone getMediaTimeZone() {
		return mediaTimeZone;
	}
	
	/**
	 * @param mediaTimeZone the mediaTimeZone to set
	 */
	public void setMediaTimeZone(TimeZone mediaTimeZone) {
		this.mediaTimeZone = mediaTimeZone;
	}
	
}
