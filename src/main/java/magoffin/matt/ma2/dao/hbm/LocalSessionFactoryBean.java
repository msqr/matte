/* ===================================================================
 * LocalSessionFactoryBean.java
 * 
 * Created Jan 9, 2007 9:19:55 AM
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

package magoffin.matt.ma2.dao.hbm;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunction;
import org.springframework.util.CollectionUtils;

/**
 * Extension of Spring's LocalSessionFactoryBean to add support for registering
 * custom SQL functions.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>sqlFunctions</dt>
 *   <dd>A mapping of SQL function names to implementations of 
 *   {@link SQLFunction} so that custom SQL functions can be configured.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class LocalSessionFactoryBean extends
		org.springframework.orm.hibernate3.LocalSessionFactoryBean {
	
	private Map<String, SQLFunction> sqlFunctions;

	@Override
	protected void postProcessConfiguration(Configuration config) throws HibernateException {
		super.postProcessConfiguration(config);
		if ( !CollectionUtils.isEmpty(sqlFunctions) ) {
			for ( Map.Entry<String, SQLFunction> me : sqlFunctions.entrySet() ) {
				if ( logger.isInfoEnabled() ) {
					logger.info("Registering SQL function [" +me.getKey() +"] class ["
							+me.getValue().getClass().getName() +"]");
				}
				config.addSqlFunction(me.getKey(), me.getValue());
			}
		}
	}
	
	/**
	 * @return the sqlFunctions
	 */
	public Map<String, SQLFunction> getSqlFunctions() {
		return sqlFunctions;
	}
	
	/**
	 * @param sqlFunctions the sqlFunctions to set
	 */
	public void setSqlFunctions(Map<String, SQLFunction> sqlFunctions) {
		this.sqlFunctions = sqlFunctions;
	}
	
}
