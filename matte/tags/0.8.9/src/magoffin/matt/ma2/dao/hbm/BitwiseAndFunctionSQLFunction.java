/* ===================================================================
 * BitwiseAndFunctionSQLFunction.java
 * 
 * Created Jan 28, 2007 2:15:00 PM
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
 * $Id: BitwiseAndFunctionSQLFunction.java,v 1.4 2007/07/27 00:51:21 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.hbm;

import java.util.List;

import org.hibernate.engine.SessionFactoryImplementor;

/**
 * Hibernate bitwise AND operation using a custom SQL function.
 * 
 * <p>This is a Hibernate {@link org.hibernate.dialect.function.StandardSQLFunction}
 * that produces a bitwise AND SQL function call. This can be used for databases
 * that do not provide support for the <code>&amp;</code> bitwise AND operator
 * directly (such as Apache Derby). The generated SQL looks like:</p>
 * 
 * <pre>bitwise_and(param1, param2)</pre>
 * 
 * <p>Where param1 and param2 are presumed to be integer types.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/27 00:51:21 $
 * @see DerbyBitwiseAndSQLFunction
 */
public class BitwiseAndFunctionSQLFunction extends BitwiseAndSQLFunction {
	
	/** The default SQL function name. */
	public static final String DEFAULT_SQL_FUNCTION_NAME = "bitwise_and";
	
	private String sqlFunctionName = DEFAULT_SQL_FUNCTION_NAME;

	@SuppressWarnings("unchecked")
	@Override
	public String render(List args, SessionFactoryImplementor factory) {
		if (args.size() != 2) {
			throw new IllegalArgumentException("Function must be passed 2 arguments");
		}
		StringBuilder buffer = new StringBuilder(sqlFunctionName);
		buffer.append("(").append(args.get(0)).append(",")
			.append(args.get(1)).append(")");
		return buffer.toString();
	}
	
	/**
	 * @return the sqlFunctionName
	 */
	public String getSqlFunctionName() {
		return sqlFunctionName;
	}
	
	/**
	 * @param sqlFunctionName the sqlFunctionName to set
	 */
	public void setSqlFunctionName(String sqlFunctionName) {
		this.sqlFunctionName = sqlFunctionName;
	}

}
