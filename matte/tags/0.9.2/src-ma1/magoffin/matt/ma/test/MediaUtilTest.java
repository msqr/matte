/* ===================================================================
 * MediaUtilTest.java
 * 
 * Created Feb 13, 2004 9:21:04 AM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: MediaUtilTest.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;

import java.util.TimeZone;

import junit.framework.TestCase;
import magoffin.matt.ma.util.MediaUtil;

/**
 * Test cases for MediaUtil class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class MediaUtilTest extends TestCase {
	
	private static final int NY = -18000000;
	private static final Integer SF_OFF = new Integer(-28800000);
	private static final Integer SF_OFF_2 = new Integer(-30000000);
	private static final Integer SF_OFF_3 = new Integer(-28000000);
	
/*public void testTranslateToAppTime() {
	Date startDate = new Date();
	Calendar cal = Calendar.getInstance();
	Date newDate = MediaUtil.translateToAppTime(NY,startDate,cal);
	int diff = ApplicationConstants.TIME_ZONE.getRawOffset() - NY;
	assertEquals(diff,newDate.getTime() - startDate.getTime());
}*/

/*public void testGetTzName() {
	String defName = ApplicationConstants.TIME_ZONE.getID();
	String name = MediaUtil.getTzName(null);
	assertEquals(defName,name);
	
	defName = TimeZone.getTimeZone("GMT-8:00").getID();
	name = MediaUtil.getTzName(SF_OFF);
	assertEquals(defName,name);

	defName = TimeZone.getTimeZone("GMT-8:20").getID();
	name = MediaUtil.getTzName(SF_OFF_2);
	assertEquals(defName,name);

	defName = TimeZone.getTimeZone("GMT-7:46").getID();
	name = MediaUtil.getTzName(SF_OFF_3);
	assertEquals(defName,name);
}*/

public void testAvailableIDs() {
	//String[] ids = TimeZone.getAvailableIDs();
	for ( int currOffset = -12; currOffset < 15; currOffset++ ) {
		String[] ids = TimeZone.getAvailableIDs(currOffset*MediaUtil.MS_PER_HOUR);
		
		if ( currOffset == 0 ) {
			//System.out.println("<option value=\"GMT\">GMT</option>");
			System.out.print("Etc/GMT ");
		} else {
			//System.out.println("<option value=\"GMT"+(currOffset<0?"+":"")+(0-currOffset)+"\">GMT"
			//		+currOffset +"</option>");
			System.out.print("Etc/GMT"+(currOffset<0?"+":"")+(0-currOffset)+" ");
		}
		
		for ( int i = 0; i < ids.length; i++ ) {
			if ( ids[i].startsWith("Etc/") ) continue;
			
			System.out.print(ids[i]);
			System.out.print(" ");
			
			/*StringBuffer buf = new StringBuffer("<option value=\"");
			buf.append(ids[i]).append("\">");
			buf.append(ids[i]);
			buf.append(" ");
			buf.append(TimeZone.getTimeZone(ids[i]).getRawOffset());
			buf.append("</option>");
			System.out.println(buf.toString());*/
		}
	}
}

public void testTimeCode() 
{
	String code = MediaUtil.getTimeCodeFromMilliseconds(60000l);
	assertEquals("1:00",code);
	
	code = MediaUtil.getTimeCodeFromMilliseconds(93000l);
	assertEquals("1:33",code);
	
	code = MediaUtil.getTimeCodeFromMilliseconds(-1l);
	assertNull(code);
	
	code = MediaUtil.getTimeCodeFromMilliseconds(250l);
	assertEquals("0.250",code);
	
	code = MediaUtil.getTimeCodeFromMilliseconds(10655879l);
	assertEquals("2:57:35.879",code);
	
	code = MediaUtil.getTimeCodeFromMilliseconds(7385879l);
	assertEquals("2:03:05.879",code);
	
	code = MediaUtil.getTimeCodeFromMilliseconds(180879l);
	assertEquals("3:00.879",code);

	code = MediaUtil.getTimeCodeFromMilliseconds(12879l);
	assertEquals("12.879",code);
}

}
