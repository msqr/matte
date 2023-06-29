/* ===================================================================
 * ThemeForm.java
 * 
 * Created Sep 19, 2006 9:33:49 PM
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
 */

package magoffin.matt.ma2.web.admin;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.support.AddThemeCommand;
import magoffin.matt.ma2.web.AbstractForm;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.util.TemporaryFile;
import magoffin.matt.util.TemporaryFileMultipartFileEditor;

import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form for editing/creating a Theme.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class ThemeForm extends AbstractForm {

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		getWebHelper().getAdminBizContext(request);
		AddThemeCommand cmd = new AddThemeCommand();
		cmd.setTheme(getDomainObjectFactory().newThemeInstance());

		// see if trying to populate existing theme
		ServletRequestDataBinder binder = createBinder(request, cmd);
		binder.bind(request);
		
		if ( cmd.getThemeId() != null ) {
			Theme domainTheme = getSystemBiz().getThemeById(cmd.getThemeId());
			BeanUtils.copyProperties(domainTheme,cmd.getTheme());
		}
		
		return cmd;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) 
	throws Exception {
		Map<String,Object> ref =  new LinkedHashMap<String,Object>();
		
		Model model = getDomainObjectFactory().newModelInstance();
		model.getTimeZone().addAll(getSystemBiz().getAvailableTimeZones());
		
		AddThemeCommand cmd = (AddThemeCommand)command;
		model.getTheme().add(cmd.getTheme());
		
		ref.put(WebConstants.DEFALUT_REFERENCE_DATA_OBJECT, model);
		return ref;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command,	BindException errors)
			throws Exception {
		AddThemeCommand cmd = (AddThemeCommand)command;
		BizContext context = getWebHelper().getAdminBizContext(request);
		
		boolean isNew = cmd.getTheme().getThemeId() == null;
		
		Long themeId = getSystemBiz().storeTheme(cmd, context);
		
		Theme savedTheme = getSystemBiz().getThemeById(themeId);
		
		Map<String,Object> viewModel = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = null;
		if ( isNew ) {
			msg = new DefaultMessageSourceResolvable(
				new String[] {"add.theme.success"}, 
				new Object[]{savedTheme.getName()},
				"The theme has been added.");
		} else {
			msg = new DefaultMessageSourceResolvable(
				new String[] {"update.theme.success"}, 
				new Object[]{savedTheme.getName()},
				"The theme has been saved.");
		}
		viewModel.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);

		return new ModelAndView(getSuccessView(),viewModel);
	}

	@Override
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		// register our Multipart TemporaryFile binder...
		binder.registerCustomEditor(TemporaryFile.class, new TemporaryFileMultipartFileEditor());
	}

}
