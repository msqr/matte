/* ===================================================================
 * AbstractJdbcBrowseModePlugin.java
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.dao.support;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.plugin.BrowseModePlugin;
import magoffin.matt.ma2.plugin.Plugin;
import magoffin.matt.ma2.support.AbstractPlugin;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base class for JDBC based implementations of {@link BrowseModePlugin}.
 * 
 * <p>The class is a useful class to extend for JDBC-based implementations of
 * the {@link BrowseModePlugin} API. This class provides a common framework for 
 * initializing the plugin instance with a Spring context file. 
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public abstract class AbstractJdbcBrowseModePlugin extends AbstractPlugin 
implements BrowseModePlugin {

	private UserBiz userBiz;
	private JdbcTemplate jdbcTemplate;
	private DomainObjectFactory domainObjectFactory;

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.plugin.Plugin#getPluginType()
	 */
	public Class<? extends Plugin> getPluginType() {
		return BrowseModePlugin.class;
	}

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	/**
	 * @return the jdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @return the domainObjectFactory
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory the domainObjectFactory to set
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

}
