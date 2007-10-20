/* ===================================================================
 * HomeViewTest.java
 * 
 * Created Mar 13, 2006 5:23:42 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.impl.TestBizContext;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.util.TemporaryFile;
import magoffin.matt.xweb.util.XwebJaxbView;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * Test the HomeController.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class HomeViewTest extends AbstractSpringEnabledTransactionalTest {
	
	/** The home controller. */
	protected HomeController testHomeController;
	
	/** The logon form. */
	protected LogonForm testLogonForm;
	
	/** The DomainObjectFactory. */
	protected DomainObjectFactory domainObjectFactory;
	
	/** The home view to test with. */
	protected XwebJaxbView testHomeView;
	
	/** The IOBiz. */
	protected IOBiz ioBiz;

	/**
	 * Test.
	 * @throws Exception
	 */
	public void testViewHomeNoParameters() throws Exception {
		MockHttpServletRequest request = setupUserRequest("GET","/home.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		ModelAndView mav = testHomeController.handleRequest(request,response);
		testHomeView.setStylesheetLocation(new FileSystemResource("web/WEB-INF/xsl/home.xsl"));
		testHomeView.render(mav.getModel(),request,response);
	}
	
	private MockHttpServletRequest setupUserRequest(String method, String path) throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(getContext(contextKey()),null);		
		String confKey = testHomeController.getUserBiz().registerUser(newUser,context);
		User confirmedUser = testHomeController.getUserBiz().confirmRegisteredUser(newUser.getLogin(),
				confKey,context);

		List<Collection> collections = testHomeController.getUserBiz().getCollectionsForUser(
				confirmedUser,context);

		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(false);
		addCmd.setCollectionId(collections.get(0).getCollectionId());
		final Resource testJpegImage = new ClassPathResource(
				"magoffin/matt/ma2/image/bee-action.jpg");
		addCmd.setTempFile(new TemporaryFile() {

			public InputStream getInputStream() throws IOException {
				return testJpegImage.getInputStream();
			}

			public String getName() {
				return testJpegImage.getFilename();
			}

			public String getContentType() {
				return "image/jpeg";
			}
			
			public long getSize() {
				try {
					return testJpegImage.getFile().length();
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
			}
		});
		
		WorkInfo info = ioBiz.importMedia(addCmd, context);
		info.get(600,TimeUnit.SECONDS);	

		MockHttpServletRequest logonRequest = new MockHttpServletRequest(
				"POST","/logon.do");
		logonRequest.addParameter("login","home-test");
		logonRequest.addParameter("password","home-test");
		MockHttpServletResponse response = new MockHttpServletResponse();
		testLogonForm.handleRequest(logonRequest,response);

		MockServletContext servletContext = new MockServletContext("web");
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext,method,path);
		request.setSession(logonRequest.getSession());
		return request;
	}
	
	private User getTestUser() {
		User newUser = domainObjectFactory.newUserInstance();
		newUser.setEmail("nobody@localhost");
		newUser.setName("Test User");
		newUser.setPassword("home-test");
		newUser.setLogin("home-test");
		return newUser;
	}
}
