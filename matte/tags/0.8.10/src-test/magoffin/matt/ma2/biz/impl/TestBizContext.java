/* ===================================================================
 * TestBizContext.java
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: TestBizContext.java,v 1.4 2007/07/21 10:20:14 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;

import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BasicBizContext;
import magoffin.matt.xweb.XAppContext;
import magoffin.matt.xweb.util.AppContextSupport;

/**
 * BizContext for test cases.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/21 10:20:14 $
 */
public class TestBizContext extends BasicBizContext {
	
	private final Logger log = Logger.getLogger(TestBizContext.class);
	
	/**
	 * Constructor.
	 * @param context the application context
	 * @param actingUser the acting user
	 */
	@SuppressWarnings({"unchecked"})
	public TestBizContext(ApplicationContext context, User actingUser) {
		super();
		
		Map<String,? extends DomainObjectFactory> map = 
			BeanFactoryUtils.beansOfTypeIncludingAncestors(
				context,DomainObjectFactory.class,false,false);
		if ( map == null || map.size() < 1 ) {
			throw new RuntimeException(DomainObjectFactory.class.getName() 
					+" implementation not found.");
		}
		if ( map.size() > 1 ) {
			log.warn("More than one implementation of " +DomainObjectFactory.class.getName()
					+" found in ApplicationContext, bean names are "
					+map.keySet().toString() +"; using [" 
					+map.keySet().iterator().next() +"]");
		}
		DomainObjectFactory domainObjectFactory = map.values().iterator().next();
		XAppContext appContext = domainObjectFactory.newXAppContextInstance();
		AppContextSupport appContextSupport = new AppContextSupport(appContext);
		setAppContextSupport(appContextSupport);
		setActingUser(actingUser);
	}
	
	/**
	 * Construct from an {@link AppContextSupport}.
	 * @param appContextSupport the application context support
	 * @param actingUser the acting user
	 */
	public TestBizContext(AppContextSupport appContextSupport, User actingUser) {
		super(appContextSupport);
		setActingUser(actingUser);
	}

	@Override
	public boolean isFeatureEnabled(Feature feature) {
		return true;
	}
	
}
