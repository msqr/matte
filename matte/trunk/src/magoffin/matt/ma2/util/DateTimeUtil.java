/* ===================================================================
 * DateTimeUtil.java
 * 
 * Created Oct 21, 2012 9:13:18 PM
 * 
 * Copyright (c) 2012 Matt Magoffin.
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

package magoffin.matt.ma2.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import magoffin.matt.ma2.domain.MediaItem;
import org.apache.log4j.Logger;

/**
 * Date/time utility functions.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public final class DateTimeUtil {

	/** A date format pattern for re-parsing dates across time zones. */
	public static final String REPARSE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private static final Logger LOG = Logger.getLogger(DateTimeUtil.class);

	public static void adjustItemDateTimeZone(MediaItem item, TimeZone tz, TimeZone displayTz)
			throws ParseException {
		// format as String, then re-parse in correct zone
		SimpleDateFormat sdf = new SimpleDateFormat(REPARSE_DATE_FORMAT);
		String dateStr = sdf.format(item.getItemDate().getTime());
		sdf.setTimeZone(tz);
		Calendar newDate = Calendar.getInstance(tz);
		newDate.setTime(sdf.parse(dateStr));

		// now format date into display TZ
		sdf.setTimeZone(displayTz);
		dateStr = sdf.format(newDate.getTime());
		newDate = Calendar.getInstance();
		sdf.setTimeZone(newDate.getTimeZone());
		newDate.setTime(sdf.parse(dateStr));

		item.setItemDate(newDate);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Re-parsed date to [" + sdf.format(newDate.getTime())
					+ "] in time zone [" + displayTz.getDisplayName() + "]");
		}
	}

}
