/* ===================================================================
 * LoginCommand.java
 * 
 * Created Oct 4, 2004 12:46:32 PM
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
 * $Id: LogonCommand.java,v 1.1 2006/10/29 01:32:50 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

/**
 * Command object for logging into the application.
 * 
 * <p>The <code>code</code> property is used for registration.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/10/29 01:32:50 $
 */
public class LogonCommand {
	
	private String login = null;
	private String password = null;
	private String code = null;
	
	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * @param code The code to set.
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * @return Returns the login.
	 */
	public String getLogin() {
		return login;
	}
	
	/**
	 * @param login The login to set.
	 */
	public void setLogin(String login) {
		this.login = login;
	}
	
	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
}
