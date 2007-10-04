/* ===================================================================
 * HibernateThemeDao.java
 * 
 * Created May 20, 2006 8:06:20 PM
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
 * $Id: HibernateThemeDao.java,v 1.6 2006/11/19 06:57:22 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.hbm;

import java.util.List;

import magoffin.matt.dao.hbm.GenericIndexableHibernateDao;
import magoffin.matt.ma2.dao.ThemeDao;
import magoffin.matt.ma2.domain.Theme;

/**
 * Hibernate impolementation of ThemeDao.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.6 $ $Date: 2006/11/19 06:57:22 $
 */
public class HibernateThemeDao extends GenericIndexableHibernateDao<Theme, Long> 
implements ThemeDao {

	/** Find all themes. */
	public static final String QUERY_THEME_ALL = "ThemeAll";
	
	/** Find a theme by name. */
	public static final String QUERY_THEME_BY_NAME = "ThemeForName";
	
	/**
	 * Default constructor.
	 */
	public HibernateThemeDao() {
		super(Theme.class);
	}

	@Override
	protected Long getPrimaryKey(Theme domainObject) {
		if ( domainObject == null ) return null;
		return domainObject.getThemeId();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.ThemeDao#findAllThemes()
	 */
	public List<Theme> findAllThemes() {
		return findByNamedQuery(QUERY_THEME_ALL);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.ThemeDao#getThemeForName(java.lang.String)
	 */
	public Theme getThemeForName(String name) {
		List<Theme> results = findByNamedQuery(QUERY_THEME_BY_NAME, new Object[]{name});
		if ( results.size() < 1 ) return null;
		return results.get(0);
	}

}