/* ===================================================================
 * MediaItemSorter.java
 * 
 * Created Jan 2, 2007 7:58:36 PM
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
 * $Id: MediaItemSorter.java,v 1.6 2007/07/15 08:21:21 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.util;

import java.util.Calendar;
import java.util.Comparator;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * Comparator for MediaItem objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.6 $ $Date: 2007/07/15 08:21:21 $
 */
public class MediaItemSorter implements Comparator<MediaItem> {
	
	/** Sort mode. */
	public enum SortMode {
		
		/** Do not sort. */
		NONE(0),
		
		/** Sort by date. */
		DATE(1);
		
		private int modeFlag;
		
		SortMode(int modeFlag) {
			this.modeFlag = modeFlag;
		}

		/**
		 * Get an integer constant for this sort mode.
		 * @return the modeFlag
		 */
		public int getModeFlag() {
			return modeFlag;
		}
		
		/**
		 * Get a SortMode from a mode flag.
		 * @param modeFlag the mode flag
		 * @return SortMode (defaults to {@link SortMode#NONE})
		 */
		public static SortMode fromModeFlag(int modeFlag) {
			switch ( modeFlag ) {
				case 1:
					return DATE;
					
				default:
					return NONE;
			}
		}
		
	}
	
	private SortMode mode = SortMode.DATE;
	
	/**
	 * Default constructor.
	 */
	public MediaItemSorter() {
		super();
	}
	
	/**
	 * Construct with a SortMode.
	 * @param mode the mode
	 */
	public MediaItemSorter(SortMode mode) {
		this.mode = mode;
	}
	
	/**
	 * Construct with a SortMode mode flag.
	 * @param modeFlag the mode flag
	 */
	public MediaItemSorter(int modeFlag) {
		this.mode = SortMode.fromModeFlag(modeFlag);
	}

	public int compare(MediaItem o1, MediaItem o2) {
		switch ( mode ) {
			case DATE:
				return compareDates(o1, o2);
				
			case NONE:
				return 0;
		}
		return compareDates(o1, o2);
	}

	private int compareDates(MediaItem o1, MediaItem o2) {
		Calendar c1 = o1.getItemDate() != null 
			? o1.getItemDate()
			: o1.getCreationDate();
		Calendar c2 = o2.getItemDate() != null
			? o2.getItemDate()
			: o2.getCreationDate();
		return c1.compareTo(c2);
	}

}
