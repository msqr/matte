/* ===================================================================
 * ThemeDao.java
 * 
 * Created May 20, 2006 8:05:13 PM
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
 */

package magoffin.matt.ma2.dao;

import java.util.List;

import magoffin.matt.dao.GenericDao;
import magoffin.matt.ma2.domain.Theme;

/**
 * DAO for Theme objects.
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public interface ThemeDao extends GenericDao<Theme, Long> {
	
	/**
	 * Get all available themes.
	 * 
	 * @return the themes
	 */
	List<Theme> findAllThemes();
	
	/**
	 * Get a Theme by its name.
	 * 
	 * @param name the name
	 * @return the Theme, or <em>null</em> if not found
	 */
	Theme getThemeForName(String name);
	
}
