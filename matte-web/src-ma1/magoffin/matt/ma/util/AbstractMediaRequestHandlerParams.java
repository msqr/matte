/* ===================================================================
 * AbstractMediaRequestHandlerParams.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: AbstractMediaRequestHandlerParams.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.util.HashMap;
import java.util.Map;

import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.biz.WorkBiz;
import magoffin.matt.util.ResetableObject;

/**
 * Simple MediaRequestHandlerParams implementation using a Map 
 * to hold any/all parameter values.
 * 
 * <p>Created on Sep 30, 2002 4:46:15 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractMediaRequestHandlerParams
implements MediaRequestHandlerParams, ResetableObject
{
	
	protected Map options = new HashMap(5);
	protected WorkBiz workBiz = null;

/**
 * Sets parameter <var>name</var> to <var>value</var>, unless
 * either are <em>null</em>.
 * 
 * @see magoffin.matt.ma.MediaRequestHandlerParams#setParam(String, Object)
 */
public void setParam(String name, Object value) 
{
	if ( name != null && value != null ) {
		options.put(name,value);
	}
}

/**
 * Gets the value of the parameter <var>name</var>, or <em>null</em>
 * if that parameter is not set.
 * 
 * @see magoffin.matt.ma.MediaRequestHandlerParams#getParam(String)
 */
public Object getParam(String name) 
{
	return options.get(name);
}

/**
 * Returns <em>true</em> if any parameters have been set.
 * 
 * @see magoffin.matt.ma.MediaRequestHandlerParams#hasParamsSet()
 */
public boolean hasParamsSet() 
{
	return options.size() > 0 ? true : false;
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandlerParams#hasParamSet(java.lang.String)
 */
public boolean hasParamSet(String name) 
{
	return options.containsKey(name) ? true : false;
}

/**
 * Return a String representation of this object.
 *  * @see java.lang.Object#toString() */
public String toString()
{
	return "MediaRequestHandlerParams" +
		(options == null ? "{}" : options.toString());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandlerParams#getWorkBiz()
 */
public WorkBiz getWorkBiz() {
	return workBiz;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandlerParams#setWorkBiz(magoffin.matt.ma.biz.WorkBiz)
 */
public void setWorkBiz(WorkBiz workBiz) {
	this.workBiz = workBiz;
}

/* (non-Javadoc)
 * @see magoffin.matt.util.ResetableObject#reset()
 */
public void reset() 
{
	options.clear();
	workBiz = null;
}

}
