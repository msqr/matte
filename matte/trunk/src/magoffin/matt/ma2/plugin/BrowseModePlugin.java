/* ===================================================================
 * BrowseModePlugin.java
 * 
 * Created Sep 19, 2007 3:18:59 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

package magoffin.matt.ma2.plugin;

import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;

/**
 * Plugin API for browse-mode types.
 * 
 * <p>A "browse mode" is a mode of browsing all shared albums or items of 
 * a single user. It defines a SQL query to use to geneate the browse 
 * search results with.</p>
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public interface BrowseModePlugin extends Plugin {
	
	/**
	 * Test if this plugin supports a given browse mode.
	 * 
	 * @param mode the mode to test
	 * @return boolean
	 */
	boolean supportsMode(String mode);

	/**
	 * Perform the search, returning {@link SearchResults} populated with
	 * {@code AlbumSearchResult} objects.
	 * 
	 * @param command the browse command
	 * @param pagination  the pagination criteria
	 * @return the search results
	 */
	public SearchResults find(BrowseAlbumsCommand command, PaginationCriteria pagination);
	
	/**
	 * Get the supported modes of this plugin.
	 * @return the supported modes
	 */
	public String[] getSupportedModes();
	
}
