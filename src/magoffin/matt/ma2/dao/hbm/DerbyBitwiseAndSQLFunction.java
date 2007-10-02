/* ===================================================================
 * DerbyBitwiseAndSQLFunction.java
 * 
 * Created Jan 28, 2007 1:28:16 PM
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
 * $Id: DerbyBitwiseAndSQLFunction.java,v 1.2 2007/01/28 20:03:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.hbm;

/**
 * Derby SQL function to perform bitwise and operator.
 * 
 * <p>Derby does not provide a built-in bitwise AND operator. This custom
 * Java function can be used to achieve this by making this class available
 * to the Derby class loader and registering a function, like this:</p>
 * 
 * <pre>CREATE FUNCTION SA.bitwise_and( parm1 INTEGER, param2 INTEGER )
 *  RETURNS INTEGER
 *  LANGUAGE JAVA
 *  PARAMETER STYLE JAVA
 *  NO SQL
 *  EXTERNAL NAME 'magoffin.matt.ma2.dao.hbm.DerbyBitwiseAndSQLFunction.bitwiseAnd';</pre>
 *
 * <p>This can be used with Hibernate via the {@link BitwiseAndFunctionSQLFunction}
 * Hibernate SQL function generator class.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/01/28 20:03:19 $
 * @see BitwiseAndFunctionSQLFunction
 */
public class DerbyBitwiseAndSQLFunction {

	/**
	 * Derby function to perform the bitwise AND operation.
	 * 
	 * @param param1 the first param
	 * @param param2 the second param
	 * @return the bitwise AND result
	 */
	public final static int bitwiseAnd(int param1, int param2) {
		return param1 & param2;
	}

}
