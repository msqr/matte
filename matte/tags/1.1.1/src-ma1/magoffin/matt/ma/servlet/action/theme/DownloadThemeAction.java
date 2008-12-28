/* ===================================================================
 * DownloadThemeAction.java
 *
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: DownloadThemeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.ThemeData;
import magoffin.matt.util.FileUtil;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Allow user to download theme files.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class DownloadThemeAction extends AbstractThemeAction 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.theme.AbstractThemeAction#goTheme(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.ThemeData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goTheme(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, ThemeData data, UserSessionData usd) throws Exception 
{	
	DynaActionForm dForm = (DynaActionForm)form;
	
	Integer themeId = (Integer)dForm.get(ServletConstants.REQ_KEY_THEME_ID);
	if ( themeId == null ) {
		notFound(mapping, request, response);
		return;
	}
	
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme theme = themeBiz.getAlbumThemeById(themeId,usd.getUser(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	ThemeBiz.AlbumThemeData themeData = themeBiz.getAlbumThemeData(themeId,
			usd.getUser());
	
	response.setContentType("application/zip");
	response.setHeader("Content-Disposition","filename=\"" +theme.getName()+ ".zip\"");
	getThemeFiles(theme,themeData,response.getOutputStream());
	themeData.close();
	
	return; // no mapping to return
}

protected void getThemeFiles(
	AlbumTheme theme, 
	ThemeBiz.AlbumThemeData themeData,
	OutputStream out) 
	throws MediaAlbumException
{
	ZipOutputStream zOut = new ZipOutputStream(out);
	String zipPath = theme.getName();
	try {
		// get xsl
		if ( themeData.getXsl() != null ) {
			this.getFileDownload(zipPath, themeData.getXsl(), 
					themeData.getXslName(), zOut);
		}
			
		// get css
		if ( themeData.getCss() != null ) {
			this.getFileDownload(zipPath, themeData.getCss(), 
					themeData.getCssName(), zOut);
		}
			
		// get preview thumbnail
		if ( themeData.getPreviewThumbnail() != null ) {
			this.getFileDownload(zipPath, themeData.getPreviewThumbnail(), 
					themeData.getPreviewThumbnailName(), zOut);
		}
			
		// get preview
		if ( themeData.getPreview() != null ) {
			this.getFileDownload(zipPath, themeData.getPreview(), 
					themeData.getPreviewName(), zOut);
		}
			
		// get support files
		if ( themeData.getSupportZip() != null ) {
			zipPath += "/support-files";
			ZipInputStream zin = themeData.getSupportZip();
			while ( true ) {
				ZipEntry entry = zin.getNextEntry();
				if ( entry == null ) break;
				if ( entry.isDirectory() ) continue;
				this.getFileDownload(zipPath, zin, entry.getName(), zOut);
				zin.closeEntry();
			}
		}
			
		zOut.closeEntry();
		zOut.finish();
	} catch ( IOException e ) {
		MediaAlbumException ex = new MediaAlbumException(
				"download.theme.error.file",
				new Object[]{e.getMessage()});
		ex.setNestedException(e);
		throw ex;
	} finally {
		if ( out != null ) {
			try {
				out.flush();
				out.close();
			} catch ( Exception e ) {
				// ignore
			}
		}
	}
			
}

/**
 * Append a file as a ZipEntry to a ZipOutputStream.
 * 
 * <p>This method does not close the input stream.</p>
 * 
 * @param zipPath
 * @param in
 * @param name the file name
 * @param out
 * @throws IOException
 */
private void getFileDownload(String zipPath, InputStream in, String name, 
		ZipOutputStream out) 
throws IOException
{
	ZipEntry entry = new ZipEntry((zipPath==null?"":zipPath+"/")+name);
	out.putNextEntry(entry);
	FileUtil.copy(in,out,false,false);
}

}
