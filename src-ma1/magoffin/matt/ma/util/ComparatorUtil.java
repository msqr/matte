/* ===================================================================
 * ComparatorUtil.java
 *
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: ComparatorUtil.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.util.Comparator;
import java.util.Date;

import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumMedia;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.ItemRating;
import magoffin.matt.ma.xsd.MediaItem;

/**
 * Utility class for comparing Media Album objects.
 * 
 * <p> Created on Jan 31, 2003 1:42:59 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public final class ComparatorUtil 
{
	
	/**
	 * Compares two themes by their authors, case insensitive.
	 * 
	 * <p> Created on Jan 31, 2003 1:47:43 PM.</p>
	 *
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class ThemeAuthorSort implements Comparator
	{
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			AlbumTheme t1 = (AlbumTheme)o1;
			AlbumTheme t2 = (AlbumTheme)o2;
			return t1.getAuthor().compareToIgnoreCase(t2.getAuthor());
		}

	}

	/**
	 * Compares two themes by their modification/creation.
	 * 
	 * <p> Created on Jan 31, 2003 1:47:43 PM.</p>
	 *
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class ThemeDateSort implements Comparator
	{
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			AlbumTheme t1 = (AlbumTheme)o1;
			AlbumTheme t2 = (AlbumTheme)o2;
			Date d1 = t1.getModificationDate() != null ? t1.getModificationDate() : t1.getCreationDate();
			Date d2 = t2.getModificationDate() != null ? t2.getModificationDate() : t2.getCreationDate();
			return d1.compareTo(d2);
		}

	}

	/**
	 * Compares two themes by their names, insensitive to case.
	 * 
	 * <p> Created on Jan 31, 2003 1:47:43 PM.</p>
	 *
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class ThemeNameSort implements Comparator
	{
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			AlbumTheme t1 = (AlbumTheme)o1;
			AlbumTheme t2 = (AlbumTheme)o2;
			return t1.getName().compareToIgnoreCase(t2.getName());
		}

	}

	/**
	 * Compare two AlbumMedia objects based on display order.
	 * 
	 * <p>Created Nov 13, 2002 12:49:42 AM.</p>
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class AlbumMediaDisplayOrderSort implements Comparator
	{
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if ( o1 == null ) return 1;
			if ( o2 == null ) return -1;
			Integer i1 = ((AlbumMedia)o1).getDisplayOrder();
			Integer i2 = ((AlbumMedia)o2).getDisplayOrder();
			if ( i1 == null && i2 == null ) return 0;
			if ( i1 == null ) return 1;
			if ( i2 == null ) return -1;
			return i1.compareTo(i2);
		}

	} // class MediaAlbumMediaSorter
	
	/**
	 * Compare two MediaItem objects based on creation date.
	 * 
	 * <p>If both dates are null, the comparator will compare
	 * the MediaItem <var>itemId</var> field.</p>
	 * 
	 * <p>Created Nov 13, 2002 12:49:42 AM.</p>
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class MediaItemDateSort implements Comparator
	{
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if ( o1 == null ) return 1;
			if ( o2 == null ) return -1;
			Date d1 = ((MediaItem)o1).getCreationDate();
			Date d2 = ((MediaItem)o2).getCreationDate();
			if ( d1 == null && d2 == null ) {
				// compare IDs
				Integer i1 = ((MediaItem)o1).getItemId();
				Integer i2 = ((MediaItem)o2).getItemId();
				if ( i1 == null && i2 == null ) return 0;
				if ( i1 == null ) return 1;
				if ( i2 == null ) return -1;
				return i1.compareTo(i2);
			}
			if ( d1 == null ) return 1;
			if ( d2 == null ) return -1;
			return d1.compareTo(d2);
		}

	} // class MediaItemDateSort
	
	/**
	 * Compare two MediaItem objects based on their IDs.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class MediaItemItemIdSort implements Comparator
	{
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Integer i1 = ((MediaItem)o1).getItemId();
			Integer i2 = ((MediaItem)o2).getItemId();
			return i1.compareTo(i2);
		}

	} // class MediaItemItemIdSort
	
	/**
	 * Compare two AlbumMedia objects based on their media IDs.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class AlbumMediaMediaIdSort implements Comparator
	{
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Integer i1 = ((AlbumMedia)o1).getMediaId();
			Integer i2 = ((AlbumMedia)o2).getMediaId();
			return i1.compareTo(i2);
		}

	} // class AlbumMediaMediaIdSort
	
	/**
	 * Compare two MediaItem objects based on their hit fields.
	 * 
	 * <p>If both hits are equal, the comparator will compare
	 * the MediaItem <var>itemId</var> field. This Comparator
	 * sorts in reverse (highest to lowest) order.</p>
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class MediaItemHitSort implements Comparator
	{
		private static final Integer NO_HITS = new Integer(0);
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if ( o1 == null ) return 1;
			if ( o2 == null ) return -1;
			Integer i1 = ((MediaItem)o1).getHits();
			Integer i2 = ((MediaItem)o2).getHits();
			if ( i1 == null ) {
				i1 = NO_HITS;
			}
			if ( i2 == null ) {
				i2 = NO_HITS;
			}
			if ( i1.equals(i2) ) {
				// compare IDs, assuming not null
				i1 = ((MediaItem)o1).getItemId();
				i2 = ((MediaItem)o2).getItemId();
				return i1.compareTo(i2);
			}
			return i2.compareTo(i1);
		}

	} // class MediaItemDateSort
	
	/**
	 * Compare two ItemRating objects based on item ID.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class ItemRatingItemIdSort implements Comparator
	{
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Integer i1 = ((ItemRating)o1).getItemId();
			Integer i2 = ((ItemRating)o2).getItemId();
			return i1.compareTo(i2);
		}

	} // class ItemRatingItemIdSort
	
	/**
	 * Compare two MediaItem objects based on their average user rating.
	 * 
	 * <p>This comparator sorts in ascending order.</p>
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class MediaItemAverageRatingSort implements Comparator
	{
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			MediaItem i1 = (MediaItem)o1;
			MediaItem i2 = (MediaItem)o2;
			int c1 = i1.getUserRatingCount();
			int c2 = i2.getUserRatingCount();
			
			if ( c1 == 0 && c2 == 0 ) {
				// compare by ID
				return i1.getItemId().compareTo(i2.getItemId());
			}
			
			if ( c1 == 0 ) return 1;
			if ( c2 == 0 ) return -1;
			
			float a1 = 0;
			for ( int i = 0; i < c1; i++ ) {
				a1 += i1.getUserRating(i).getRating().shortValue();
			}
			a1 = a1 / c1;
			
			float a2 = 0;
			for ( int i = 0; i < c2; i++ ) {
				a2 += i2.getUserRating(i).getRating().shortValue();
			}
			a2 = a2 / c2;
			
			if ( a1 == a2 ) {
				// compare by ID
				return i1.getItemId().compareTo(i2.getItemId());
			}
			
			return a1 > a2 ? -1 : 1;
		}

	} // class MediaItemAverageRatingSort
	
	/**
	 * Compare two Collection objects based on their names.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class CollectionNameSort implements Comparator {
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if ( o1 == null ) return 1;
			if ( o2 == null ) return -1;
			String s1 = ((Collection)o1).getName();
			String s2 = ((Collection)o2).getName();
			if ( s1 == null && s2 == null ) return 0;
			if ( s1 == null ) return 1;
			if ( s2 == null ) return -1;
			return s1.compareToIgnoreCase(s2);
		}

	}

	/**
	 * Compare two Album objects based on their date and/or creation date.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class AlbumDateSort implements Comparator {
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if ( o1 == null ) return 1;
			if ( o2 == null ) return -1;
			Date d1 = ((Album)o1).getAlbumDate();
			Date d2 = ((Album)o2).getAlbumDate();
			
			if ( d1 == null ) {
				d1 = ((Album)o1).getCreationDate();
			}
			if ( d2 == null ) {
				d2 = ((Album)o2).getCreationDate();
			}
			
			if ( d1 == null && d2 == null ) return 0;
			if ( d1 == null ) return 1;
			if ( d2 == null ) return -1;
			return d1.compareTo(d2);
		}

	}

	/**
	 * Compare two Comparable objects in reverse.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	public static class ComparableReverseSort implements Comparator
	{
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			return ((Comparable)o2).compareTo(o1);
		}

	} // class ComparableReverseSort
	
}
