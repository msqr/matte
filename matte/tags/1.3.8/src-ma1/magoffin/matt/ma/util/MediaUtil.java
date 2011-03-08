/* ===================================================================
 * MediaUtil.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Mar 2, 2003.
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
 * $Id: MediaUtil.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.StringUtil;

import org.apache.log4j.Logger;

/**
 * Static helper methods for media file.
 * 
 * <p>Created Mar 2, 2003 6:28:35 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public final class MediaUtil 
{
	private static Logger LOG = Logger.getLogger(MediaUtil.class);
	
	public static final MediaUtil.MediaItemSortByName MEDIA_ITEM_SORT_BY_NAME
		= new MediaUtil.MediaItemSortByName();
		
	public static final MediaUtil.MediaItemSortByCreationDate MEDIA_ITEM_SORT_BY_CREATION_DATE
		= new MediaUtil.MediaItemSortByCreationDate();

	public static final MediaUtil.MediaItemSortByPath MEDIA_ITEM_SORT_BY_PATH 
		= new MediaUtil.MediaItemSortByPath();
		
	public static final Comparator MEDIA_ALBUM_SORT_BY_DATE
		= new ComparatorUtil.AlbumDateSort();
		
/**
 * Find a Album with the given ID within a MedialAlbum tree.
 * 
 * @param rootAlbum the album to begin search in
 * @param albumId the album ID to locate
 * @return the found Album, or <em>null</em> if not found
 */
public static Album findAlbum(Album rootAlbum, Integer albumId)
{
	if ( rootAlbum == null || albumId == null ) {
		return null;
	}
	
	if ( rootAlbum.getAlbumId().equals(albumId) ) {
		return rootAlbum;
	}
	
	Album[] children = rootAlbum.getAlbum();
	for ( int i = 0; i < children.length; i++ ) {
		Album child = findAlbum(children[i],albumId);
		if ( child != null ) {
			return child;
		}
	}
	
	return null;
}


/**
 * Find a Album with the given name and parent ID within a MedialAlbum tree.
 * 
 * @param rootAlbum the album to begin search in
 * @param name the name of the album to find
 * @param parentId the parent ID to match (may be <em>null</em>)
 * @return the found Album, or <em>null</em> if not found
 */
public static Album findAlbumByName(Album rootAlbum, String name, Integer parentId)
{
	if ( rootAlbum == null || name == null ) {
		return null;
	}
	
	Integer rootId = rootAlbum.getParentId();
	
	if ( rootAlbum.getName().equals(name) && 
		((parentId == null && rootId == null) || (parentId != null && parentId.equals(rootId) ) ) ) {
		return rootAlbum;
	}
	
	Album[] children = rootAlbum.getAlbum();
	for ( int i = 0; i < children.length; i++ ) {
		Album child = findAlbumByName(children[i],name,parentId);
		if ( child != null ) {
			return child;
		}
	}
	
	return null;
}

/**
 * Search for a MediaItem within a set of albums.
 * 
 * @param albums the albums to search
 * @param itemId the ID of the MediaItem to find
 * @return the MediaItem, or <em>null</em> if not found
 */
public static MediaItem findItem(Album[] albums, Integer itemId) 
{
	if ( albums == null || albums.length < 1 ) return null;
	for ( int i = 0; i < albums.length; i++ ) {
		if ( albums[i].getItemCount() > 0 ) {
			int max = albums[i].getItemCount();
			for ( int j = 0; j < max; j++ ) {
				if ( itemId.equals(albums[i].getItem(j).getItemId()) ) {
					return albums[i].getItem(j);
				}
			}
		} 
		if ( albums[i].getAlbumCount() > 0 ) {
			MediaItem item = findItem(albums[i].getAlbum(),itemId);
			if ( item != null ) {
				return item;
			}
		}
	}
	return null;
}

/**
 * Generate an array of all user IDs for owners of all albums.
 * 
 * @param albums the albums to search
 * @return array of user IDs, or <em>null</em> if none available
 */
public static Integer[] getOwners(Album[] albums)
{
	if ( albums == null || albums.length < 1 ) {
		return null;
	}
	
	Set owners = new HashSet(albums.length);
	getOwners(albums,owners);
	if ( owners.size() < 1 ) {
		return null;
	}
	return (Integer[])owners.toArray(new Integer[owners.size()]);
}

/**
 * Internal recursive method to add owners to list.
 * @param albums
 * @param owners
 */
private static void getOwners(Album[] albums, Set owners) 
{
	for ( int i = 0; i < albums.length; i++ ) {
		Album a = albums[i];
		if ( a.getOwner() != null ) {
			owners.add(a.getOwner());
		}
		if ( a.getAlbumCount() > 0 ) {
			getOwners(a.getAlbum(),owners);
		}
	}
}


/**
 * Compares MediaItem objects by their path.
 */
public static class MediaItemSortByPath implements Comparator
{
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return ((MediaItem)o1).getPath().compareTo(((MediaItem)o2).getPath());
	}

}

/**
 * Compare MediaItem object by their name and path ignoring case
 * (assums path is never <em>null</em>).
 */
public static class MediaItemSortByName implements Comparator
{
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		String s1 = ((MediaItem)o1).getName();
		String s2 = ((MediaItem)o2).getName();
		if ( s1 == null ) {
			s1 = ((MediaItem)o1).getPath();
		}
		if ( s2 == null ) {
			s2 = ((MediaItem)o2).getPath();
		}
		return s1.compareToIgnoreCase(s2);
	}

}

/**
 * Compare MediaItem object by their creation date.
 */
public static class MediaItemSortByCreationDate implements Comparator
{
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		Date d1 = ((MediaItem)o1).getCreationDate();
		Date d2 = ((MediaItem)o2).getCreationDate();
		if ( d1 == null && d2 == null ) {
			return 0;
		}
		if ( d1 == null ) {
			return -1;
		}
		if ( d2 == null ) {
			return 1;
		}
		return d1.compareTo(d2);
	}

}

/**
 * Translate a date by an offset from the application time zone.
 * 
 * @param tzOffset
 * @param date
 * @param calendar
 * @return
 */
/*public static Date translateToAppTime(int tzOffset, Date date, Calendar calendar)
{
	int myTz = ApplicationConstants.TIME_ZONE.getRawOffset();
	if ( tzOffset != myTz ) {
		int diff = myTz - tzOffset;
		calendar.setTime(date);
		calendar.add(Calendar.MILLISECOND,diff);
		return calendar.getTime();
	}
	return date;
}*/

private static final Map TZ_MAP = new HashMap(8);

public static final int MS_PER_HOUR = 1000 *60 *60;

public static String getTzName(Integer tzOffset) {
	if ( TZ_MAP.containsKey(tzOffset) ) {
		return (String)TZ_MAP.get(tzOffset);
	}
	synchronized ( TZ_MAP ) {
		if ( tzOffset == null ) {
			tzOffset = ApplicationConstants.TIME_ZONE_OFFSET;
		}
		StringBuffer buf = new StringBuffer(ApplicationConstants.TIME_ZONE_PREFIX);
		if ( tzOffset.intValue() % MS_PER_HOUR == 0 ) {
			int hours = tzOffset.intValue() / MS_PER_HOUR;
			if ( hours >= 0 ) {
				buf.append('+');
			}
			buf.append(hours);
		} else {
			double absMs = Math.abs(tzOffset.intValue());
			int hours = (int)Math.floor(absMs / MS_PER_HOUR);
			int min = (int)Math.floor((absMs - hours * MS_PER_HOUR)/ 60000d);
			if ( tzOffset.intValue() < 0 ) {
				buf.append('-');
			}
			buf.append(hours).append(':').append(min);
		}
		TimeZone tz = TimeZone.getTimeZone(buf.toString());
		TZ_MAP.put(tzOffset,tz.getID());
	}
	return (String)TZ_MAP.get(tzOffset);
}

public static void translateItemTime(String tzCode, MediaItem item, Calendar calendar)
{
	if ( item.getCreationDate() == null ) return;
	String itemTzCode = item.getTzCode();
	TimeZone userTz = TimeZone.getTimeZone(tzCode);
	TimeZone itemTz = TimeZone.getTimeZone(item.getTzCode());
	if ( !tzCode.equals(itemTzCode) ) {
		int diff = itemTz.getRawOffset()-userTz.getRawOffset();
		calendar.setTime(item.getCreationDate());
		calendar.add(Calendar.MILLISECOND,diff);
		Date newDate = calendar.getTime();
		item.setCreationDate(newDate);
		item.setTzName(itemTz.getDisplayName(false,TimeZone.SHORT));
	}
	
	if ( itemTz.inDaylightTime(item.getCreationDate()) ) {
		calendar.setTime(item.getCreationDate());
		calendar.add(Calendar.MILLISECOND,itemTz.getDSTSavings());
		item.setCreationDate(calendar.getTime());
		item.setTzName(itemTz.getDisplayName(true,TimeZone.SHORT));
	}
}

public static void translateItemTime(String tzCode, MediaItem item)
{
	translateItemTime(tzCode,item,Calendar.getInstance());
}

public static void translateItemTime(String tzCode, MediaItem[] items)
{
	if ( items == null ) return;
	Calendar userCalendar = Calendar.getInstance();
	for ( int i = 0; i < items.length; i++ ) {
		translateItemTime(tzCode,items[i],Calendar.getInstance());
	}
}

public static void untranslateItemTime(String tzCode, MediaItem item, Calendar calendar)
{
	if ( item.getCreationDate() == null ) return;
	String itemTzCode = item.getTzCode();
	TimeZone userTz = TimeZone.getTimeZone(tzCode);
	TimeZone itemTz = TimeZone.getTimeZone(item.getTzCode());

	if ( itemTz.inDaylightTime(item.getCreationDate()) ) {
		calendar.setTime(item.getCreationDate());
		calendar.add(Calendar.MILLISECOND,-itemTz.getDSTSavings());
		item.setCreationDate(calendar.getTime());
		item.setTzName(itemTz.getDisplayName(false,TimeZone.SHORT));
	}

	if ( !tzCode.equals(itemTzCode) ) {
		int diff = itemTz.getRawOffset()-userTz.getRawOffset();
		calendar.setTime(item.getCreationDate());
		calendar.add(Calendar.MILLISECOND,-diff);
		Date newDate = calendar.getTime();
		item.setCreationDate(newDate);
		item.setTzName(userTz.getDisplayName(false,TimeZone.SHORT));
	}
}

public static void untranslateItemTime(String tzCode, MediaItem item)
{
	untranslateItemTime(tzCode,item,Calendar.getInstance());
}

/*
public static void translateItemTime(Integer tzOffset, MediaItem item, Calendar calendar)
{
	if ( item.getCreationDate() == null ) return;
	if ( item.getTz() != null ) {
		int myTz = ApplicationConstants.TIME_ZONE_OFFSET.intValue() ;
		int tzOff = item.getTz().intValue();
		if ( tzOff != myTz || tzOffset != null && tzOffset.intValue() != myTz ) {
			int diff = tzOff - myTz;
			if ( diff != 0 ) {
				calendar.setTime(item.getCreationDate());
				calendar.add(Calendar.MILLISECOND,diff);
				item.setCreationDate(calendar.getTime());
				item.setTzName(getTzName(item.getTz()));
			} else {
				item.setTzName(getTzName(null));
			}
		}
	} else if ( tzOffset != null ) {
		int myTz = ApplicationConstants.TIME_ZONE_OFFSET.intValue();
		int tzOff = tzOffset.intValue();
		if ( tzOff != myTz ) {
			int diff = tzOff - myTz;
			calendar.setTime(item.getCreationDate());
			calendar.add(Calendar.MILLISECOND,diff);
			item.setCreationDate(calendar.getTime());
			item.setTzName(getTzName(tzOffset));
		}
	} else {
		item.setTzName(getTzName(null));
	}
}

public static void translateItemTime(Integer tzOffset, MediaItem[] items)
{
	if ( items == null ) return;
	Calendar cal = Calendar.getInstance();
	for ( int i = 0; i < items.length; i++ ) {
		translateItemTime(tzOffset,items[i],cal);
	}
}

public static void untranslateItemTime(Integer tzOffset, MediaItem item, Calendar calendar)
{
	if ( item.getCreationDate() == null ) return;
	if ( item.getTz() != null ) {
		int myTz = ApplicationConstants.TIME_ZONE_OFFSET.intValue() ;
		int tzOff = item.getTz().intValue();
		if ( tzOff != myTz ) {
			int diff = tzOff - myTz;
			calendar.setTime(item.getCreationDate());
			calendar.add(Calendar.MILLISECOND,-(diff));
			item.setCreationDate(calendar.getTime());
		}
	} else if ( tzOffset != null ) {
		int myTz = ApplicationConstants.TIME_ZONE_OFFSET.intValue();
		int tzOff = tzOffset.intValue();
		if ( tzOff != myTz ) {
			int diff = tzOff - myTz;
			calendar.setTime(item.getCreationDate());
			calendar.add(Calendar.MILLISECOND,-(diff));
			item.setCreationDate(calendar.getTime());
		}
	}
	item.setTzName(null);
}*/

/**
 * Copy changes from <var>eItem</var> into <var>item</var>,
 * and return <em>true</em> only if any one of the copied properties
 * has changed.
 * 
 * <p>The following properties are copied:</p>
 * 
 * <ol>
 * <li>name</li>
 * <li>comments</li>
 * <li>customDate</li>
 * </ol>
 * @param eItem the "edited" item
 * @param item the "real" item to copy changes into
 * @param itemBiz a MediaItemBiz instance
 * @return <em>true</em> if any of the the properties changed
 * @throws MediaAlbumException if an error occurs
 */
public static boolean copyChanges(MediaItem eItem, MediaItem item, MediaItemBiz itemBiz)
throws MediaAlbumException
{
	boolean itemChanged = false;

	eItem.setName(StringUtil.trimToNull(eItem.getName()));
	if ( (eItem.getName() == null && item.getName() != null) ||
		(eItem.getName() != null && !eItem.getName().equals(item.getName())) ) {
		itemChanged = true;
		item.setName(eItem.getName());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Changing item " +item.getItemId() +" name to " 
					+item.getName());
		}
	}
	
	eItem.setComment(StringUtil.trimToNull(eItem.getComment()));
	if ( (eItem.getComment() == null && item.getComment() != null) ||
		(eItem.getComment() != null && !eItem.getComment().equals(item.getComment())) ) {
		itemChanged = true;
		item.setComment(eItem.getComment());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Changing item " +item.getItemId() +" comment to " 
					+item.getComment());
		}
	}
	
	if ( !eItem.getCustomDate().equals(item.getCustomDate()) ) {
		item.setCustomDate(eItem.getCustomDate());
		if ( eItem.getCustomDate().booleanValue() ) {
			item.setCreationDate(eItem.getCreationDate());
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Changing item " +item.getItemId() +" customDate to " 
						+item.getCustomDate() +", creationDate to " 
						+item.getCreationDate());
			}
		} else {
			Date trueDate = itemBiz.getItemCreationDate(item.getItemId());
			item.setCreationDate(trueDate);
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Changing item " +item.getItemId() 
						+" creationDate to true date: " 
						+item.getCreationDate());
			}
		}
		itemChanged = true;
	} else if ( eItem.getCustomDate().booleanValue() && 
			!(eItem.getCreationDate().equals(item.getCreationDate())) ) {
		item.setCreationDate(eItem.getCreationDate());
		itemChanged = true;
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Changing item " +item.getItemId() +" creationDate to " 
					+item.getCustomDate());
		}
	}
	return itemChanged;
}

/**
 * Copy changes from a list of FreeData objects to a MediaItem.
 * 
 * <p>This method works by assuming all the FreeData elements of 
 * <var>fdata</var> will have appropriate <code>itemId</code>, 
 * <code>dataTypeId</code>, and <code>dataValue</code> fields set.
 * Then, for each FreeData object in <var>fdata</var> who's 
 * <code>itemId</code> matches <var>item</var>'s <code>itemId</code>, 
 * the method will look to see if it should be added, updated, or 
 * removed from <var>item</var>'s FreeData list.<p>
 * 
 * <p>Thus when returning from this method, <var>item</var>'s FreeData
 * list will have been updated if it returns <em>true</em>, signaling 
 * it should be saved. Thus it is important that <var>item</var>'s 
 * FreeData list be its <em>complete</em> current FreeData list so 
 * that if there are any changes the calling method can simply call 
 * {@link magoffin.matt.ma.biz.MediaItemBiz#setFreeData(Integer, FreeData[], User)}
 * to save the changes.</p>
 * 
 * @param fdata the list of edited FreeData
 * @param item the item to compare the FreeData list to and update
 * @return <3m>true</em> if any changes were found, <em>false</em> otherwise
 */
public static boolean copyFreeDataChanges(FreeData[] fdata, MediaItem item) 
{
	boolean fdChanged = false;
	
	// check for simple cases first
	if ( fdata.length < 1 && item.getDataCount() < 1 ) {
		return false;
	}
	
	// iterate through fdata, looking for item IDs that match item's ID
	Integer id = item.getItemId();
	FreeData[] itemFd = item.getData();
	
	for ( int i = 0; i < fdata.length; i++ ) {
		FreeData fd = fdata[i];
		if ( !id.equals(fd.getItemId()) ) {
			continue;
		}
		
		fd.setDataValue(StringUtil.normalizeWhitespace(fd.getDataValue()));
		
		// if data has dataId, find matching one in mitem and either
		// update or remove
		if ( fd.getDataId() != null && fd.getDataId().intValue() > 0 ) {
			Integer dataId = fd.getDataId();
			FreeData updateData = null;
			for ( int j = 0; j < itemFd.length; j++ ) {
				FreeData ifd = itemFd[j];
				if ( !dataId.equals(ifd.getDataId()) ) {
					continue;
				}
				updateData = ifd;
				break;
			}
			if ( updateData != null ) {
				if ( fd.getDataValue() == null ) {
					item.removeData(updateData);
					fdChanged = true;
				} else {
					if ( !fd.getDataValue().equals(updateData.getDataValue()) ) {
						updateData.setDataValue(fd.getDataValue());
						fdChanged = true;
					}
				}
			} else {
				// hmmm!?
				item.addData(fd);
				fdChanged = true;
			}
			continue;
		}
		
		if ( fd.getDataValue() == null ) {
			continue;
		}
		
		// just add as new
		item.addData(fd);
		fdChanged = true;
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Added new FreeData " +fd.getDataTypeId() +" " 
					+fd.getDataValue() +" to item " +id);
		}
		continue;
		
		/*
		// find matching free data
		boolean foundMatch = false;
		for ( int j = 0; j < itemFd.length; j++ ) {
			FreeData ifd = itemFd[j];
			if ( ifd == null || !fd.getDataTypeId().equals(ifd.getDataTypeId()) ) {
				continue;
			}
			// now check if changed
			fd.setDataValue(StringUtil.trimToNull(fd.getDataValue()));
			if ( (fd.getDataValue() == null && ifd.getDataValue() != null) ||
					(fd.getDataValue() != null ) ) {
				if ( !fd.getDataValue().equals(ifd.getDataValue()) ) {
					fdChanged = true;
					if ( fd.getDataValue() != null && fd.getDataValue().length() > 0 ) {
						ifd.setDataValue(fd.getDataValue());
					} else {
						item.removeData(ifd);
					}
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Changing item FreeData " 
								+ifd.getDataTypeId() +" to " 
								+ifd.getDataValue());
					}
				}
				foundMatch = true;
				break;
			}	
		}
		if ( !foundMatch && fd.getDataValue() != null && fd.getDataValue().length() > 0 ) {
			// add as new
			item.addData(fd);
			fdChanged = true;
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Added new FreeData " +fd.getDataTypeId() +" " 
						+fd.getDataValue() +" to item " +id);
			}
		}*/
	}
	return fdChanged;
}

/**
 * Get a time-code style string for a duration in milliseconds.
 * 
 * <p>This method converts a time duration in milliseonds to a 
 * string time-code in the form of <code>h:mm:ss.SSS</code> where
 * <code>SSS</code> represents milliseconds (not fracional seconds!).</p>
 * 
 * <p>For example, a duration of <code>60,000</code> (one minute)
 * would return <code>1:00</code>; a duration of <code>180879</code>
 * (three minutes, 879 milliseconds) would return 
 * <code>3:00.879</code>.</p>
 * 
 * @param durationMs the time duration, in milliseconds
 * @return a time code value of the duration
 */
public static String getTimeCodeFromMilliseconds(long durationMs)
{
	if ( durationMs < 1 ) {
		return null;
	}
	int hours = (int) Math.floor(durationMs / 3600000);
	int minutes = (int) Math.floor(durationMs / 60000) - (hours * 60);
	int seconds = (int) Math.floor(durationMs / 1000) - (hours * 3600) - (minutes * 60);
	int ms = (int) durationMs - (hours*3600000) - (minutes * 60000) - (seconds * 1000);
	StringBuffer buf = new StringBuffer();
	if ( hours > 0 ) {
		buf.append(hours).append(":");
	}
	if ( minutes > 0 ) {
		if ( minutes < 10 && hours > 0 ) {
			buf.append("0");
		}
		buf.append(minutes).append(":");
	} else if ( hours > 0 ) {
		buf.append("00:");
	}
	if ( seconds > 0 ) {
		if ( seconds < 10 && minutes > 0 ) {
			buf.append("0");
		}
		buf.append(seconds);
	} else {
		if ( buf.length() > 0 ) {
			buf.append("00");
		} else {
			buf.append("0");
		}
	}
	if ( ms > 0 ) {
		buf.append(".").append(ms);
	}
	return buf.toString();
}

}
