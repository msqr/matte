/* ===================================================================
 * BitwiseAndSQLFunction.java
 * 
 * Created May 21, 2006 9:58:13 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: BitwiseAndSQLFunction.java,v 1.4 2007/07/13 22:17:51 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.hbm;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * SQLFunction for bitwise and operation.
 * 
 * <p>Adapted from http://forum.hibernate.org/viewtopic.php?t=942395&view=previous&sid=f13df7759a4a9abd374b406a35ffc3c2</p>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.4 $ $Date: 2007/07/13 22:17:51 $
 */
public class BitwiseAndSQLFunction extends StandardSQLFunction {
	
	/** The default function name. */
	public static final String DEFAULT_NAME = "bitwise_and";
	
	/** The default typeValue. */
	public static final Type DEFAULT_TYPE = Hibernate.INTEGER;
	
	/**
	 * Constructor using defaults.
	 */
	public BitwiseAndSQLFunction() {
		this(DEFAULT_NAME, DEFAULT_TYPE);
	}

	/**
	 * Constructor.
	 * @param name the HQL field
	 * @param typeValue the HQL type
	 */
	public BitwiseAndSQLFunction(String name, Type typeValue) {
		super(name, typeValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String render(List args, SessionFactoryImplementor factory) {
		if (args.size() != 2) {
			throw new IllegalArgumentException("Function must be passed 2 arguments");
		}
		StringBuilder buffer = new StringBuilder(args.get(0).toString());
		buffer.append(" & ").append(args.get(1));
		return buffer.toString();
	}

}
