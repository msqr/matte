/* ===================================================================
 * UploadMediaForm.java
 * 
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: UploadMediaForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.Collection;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.apache.struts.validator.ValidatorForm;

/**
 * Form bean for uploading media.
 * 
 * <p>Created Nov 2, 2002 4:21:18 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UploadMediaForm extends ValidatorForm 
{

	private FormFile file = null;
	private Integer collection = null;
	private boolean overwrite = false;
	private String newCollection = null;
	private boolean autoCollection = false;
	private boolean autoAlbum = false;
	private String submitAction = null;
	private Collection[] collections = null;

/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) {
	super.reset(mapping, request);
	autoCollection = autoAlbum = overwrite = false;
	submitAction = null;
}
	
/**
 * Returns the file.
 * @return FormFile
 */
public FormFile getFile() {
	return file;
}

/**
 * Returns the collection.
 * @return Integer
 */
public Integer getCollection() {
	return collection;
}

/**
 * Sets the file.
 * @param file The file to set
 */
public void setFile(FormFile file) {
	this.file = file;
}

/**
 * Sets the collection.
 * @param collection The collection to set
 */
public void setCollection(Integer collection) {
	this.collection = collection;
}

/**
 * Returns the overwrite.
 * @return boolean
 */
public boolean isOverwrite() {
	return overwrite;
}

/**
 * Sets the overwrite.
 * @param overwrite The overwrite to set
 */
public void setOverwrite(boolean overwrite) {
	this.overwrite = overwrite;
}

/**
 * @return String
 */
public String getNewCollection() {
	return newCollection;
}

/**
 * Sets the newCollection.
 * @param newCollection The newCollection to set
 */
public void setNewCollection(String newCollection) {
	this.newCollection = newCollection;
}

/**
 * @return boolean
 */
public boolean isAutoAlbum() {
	return autoAlbum;
}

/**
 * @return boolean
 */
public boolean isAutoCollection() {
	return autoCollection;
}

/**
 * Sets the autoAlbum.
 * @param autoAlbum The autoAlbum to set
 */
public void setAutoAlbum(boolean autoAlbum) {
	this.autoAlbum = autoAlbum;
}

/**
 * Sets the autoCollection.
 * @param autoCollection The autoCollection to set
 */
public void setAutoCollection(boolean autoCollection) {
	this.autoCollection = autoCollection;
}

/**
 * @return Returns the submitAction.
 */
public String getSubmitAction() {
	return submitAction;
}

/**
 * @param submitAction The submitAction to set.
 */
public void setSubmitAction(String submitAction) {
	this.submitAction = submitAction;
}

/**
 * @return Returns the collections.
 */
public Collection[] getCollections() {
	return collections;
}

/**
 * @param collections The collections to set.
 */
public void setCollections(Collection[] collections) {
	this.collections = collections;
}

}
