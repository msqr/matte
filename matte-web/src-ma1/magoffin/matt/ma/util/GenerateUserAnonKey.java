/* ===================================================================
 * GenerateAnonKey.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Oct 23, 2003.
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
 * $Id: GenerateUserAnonKey.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import magoffin.matt.ma.dao.UserAnonKeyGenerationMode;
import magoffin.matt.ma.xsd.User;

/**
 * Command line application to generate an anonymous key for a user.
 * 
 * <p>Pass a username as the command line argument.</p>
 * 
 * <p>Created Oct 23, 2003 1:32:02 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public final class GenerateUserAnonKey {
	
	private static final UserAnonKeyGenerationMode userKeyGen = new UserAnonKeyGenerationMode();

	public static void main(String[] args) {
		User user = new User();
		user.setUsername(args[0]);
		try {
			String key = (String)userKeyGen.generate(null,null,user,null);
			System.out.println("Key = " +key);
		} catch ( Exception e ) {
			System.err.println("Exception generating key: " +e);
		}
	}
}
