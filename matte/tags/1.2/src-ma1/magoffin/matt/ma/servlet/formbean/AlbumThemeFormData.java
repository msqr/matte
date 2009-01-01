/* ===================================================================
 * AlbumThemeFormData.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 8, 2004 4:21:05 PM.
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
 * $Id: AlbumThemeFormData.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import magoffin.matt.ma.biz.ThemeBiz;

/**
 * Struts form file implementation of AlbumThemeData.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class AlbumThemeFormData implements ThemeBiz.AlbumThemeData 
{
	private static final Logger log = Logger.getLogger(AlbumThemeFormData.class);
	
	private InputStream css = null;
	private String cssName = null;
	private InputStream xsl = null;
	private String xslName = null;
	private InputStream previewThumbnail = null;
	private String previewThumbnailName = null;
	private InputStream preview = null;
	private String previewName = null;
	private ZipInputStream supportZip = null;
	private String supportZipName = null;
	private long size = 0;
	
public AlbumThemeFormData(UploadThemeForm themeForm) 
{
	try {
		if ( themeForm.getCss().getFileSize() > 0 ) {
			css = themeForm.getCss().getInputStream();
			cssName = themeForm.getCss().getFileName();
			size += themeForm.getCss().getFileSize();
		}
		
		if ( themeForm.getXsl().getFileSize() > 0 ) {
			xsl = themeForm.getXsl().getInputStream();
			xslName = themeForm.getXsl().getFileName();
			size += themeForm.getXsl().getFileSize();
		}
		
		if ( themeForm.getIcon().getFileSize() > 0 ) {
			previewThumbnail = themeForm.getIcon().getInputStream();
			previewThumbnailName = themeForm.getIcon().getFileName();
			size += themeForm.getIcon().getFileSize();
		}
		
		if ( themeForm.getPreview().getFileSize() > 0 ) {
			preview = themeForm.getPreview().getInputStream();
			previewName = themeForm.getPreview().getFileName();
			size += themeForm.getPreview().getFileSize();
		}
		
		if ( themeForm.getZipData().getFileSize() > 0 ) {
			supportZip = new ZipInputStream(themeForm.getZipData().getInputStream());
			supportZipName = themeForm.getZipData().getFileName();
			size += themeForm.getZipData().getFileSize();
		}
	} catch ( Exception e ) {
		log.warn("Exception getting theme form data: " +e.toString());
	}
}
	
/**
 * @return Returns the css.
 */
public InputStream getCss() {
	return css;
}
/**
 * @return Returns the cssName.
 */
public String getCssName() {
	return cssName;
}
/**
 * @return Returns the preview.
 */
public InputStream getPreview() {
	return preview;
}
/**
 * @return Returns the previewName.
 */
public String getPreviewName() {
	return previewName;
}
/**
 * @return Returns the previewThumbnail.
 */
public InputStream getPreviewThumbnail() {
	return previewThumbnail;
}
/**
 * @return Returns the previewThumbnailName.
 */
public String getPreviewThumbnailName() {
	return previewThumbnailName;
}
/**
 * @return Returns the supportZip.
 */
public ZipInputStream getSupportZip() {
	return supportZip;
}
/**
 * @return Returns the supportZipName.
 */
public String getSupportZipName() {
	return supportZipName;
}
/**
 * @return Returns the xsl.
 */
public InputStream getXsl() {
	return xsl;
}
/**
 * @return Returns the xslName.
 */
public String getXslName() {
	return xslName;
}

/**
 * @return Returns the size.
 */
public long getSize() {
	return size;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.ThemeBiz.AlbumThemeData#close()
 */
public void close() 
{
	if ( css != null ) {
		try {
			css.close();
		} catch ( Exception e ) {
			// ignore
		}
		css = null;
	}
	if ( xsl != null ) {
		try {
			xsl.close();
		} catch ( Exception e ) {
			// ignore
		}
		xsl = null;
	}
	if ( previewThumbnail != null ) {
		try {
			previewThumbnail.close();
		} catch ( Exception e ) {
			// ignore
		}
		previewThumbnail = null;
	}
	if ( preview != null ) {
		try {
			preview.close();
		} catch ( Exception e ) {
			// ignore
		}
		preview = null;
	}
	if ( supportZip != null ) {
		try {
			supportZip.close();
		} catch ( Exception e ) {
			// ignore
		}
		supportZip = null;
	}
}
}
