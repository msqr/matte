/* ===================================================================
 * AlbumSorter.java
 * 
 * Created Jun 29, 2007 2:01:04 PM
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
 */

package magoffin.matt.ma2.util;

import java.util.Calendar;
import java.util.Comparator;
import magoffin.matt.ma2.domain.Album;

/**
 * Comparator for Album objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class AlbumSorter implements Comparator<Album> {

	/** Sort mode. */
	public enum SortMode {

		/** Do not sort. */
		NONE(-1),

		/** Sort by date. */
		DATE(1);

		private int modeFlag;

		SortMode(int modeFlag) {
			this.modeFlag = modeFlag;
		}

		/**
		 * Get an integer constant for this sort mode.
		 * 
		 * @return the modeFlag
		 */
		public int getModeFlag() {
			return modeFlag;
		}

		/**
		 * Get a SortMode from a mode flag.
		 * 
		 * @param modeFlag
		 *        the mode flag
		 * @return SortMode (defaults to {@link SortMode#NONE})
		 */
		public static SortMode fromModeFlag(int modeFlag) {
			switch (modeFlag) {
				case 1:
					return DATE;

				default:
					return NONE;
			}
		}

	}

	private SortMode mode = SortMode.DATE;
	private boolean reverse = false;

	/**
	 * Construct from a SortMode and reverse flag.
	 * 
	 * @param mode
	 *        the sorting mode
	 * @param reverse
	 *        flag indicating a reverse sort
	 */
	public AlbumSorter(SortMode mode, boolean reverse) {
		super();
		this.mode = mode;
		this.reverse = reverse;
	}

	@Override
	public int compare(Album o1, Album o2) {
		switch (mode) {
			case DATE:
				return compareDates(o1, o2);

			case NONE:
				return -1;
		}
		return compareDates(o1, o2);
	}

	private int compareDates(Album o1, Album o2) {
		Calendar c1 = o1.getAlbumDate() != null ? o1.getAlbumDate() : o1.getCreationDate();
		Calendar c2 = o2.getAlbumDate() != null ? o2.getAlbumDate() : o2.getCreationDate();
		return this.reverse ? c2.compareTo(c1) : c1.compareTo(c2);
	}

}
