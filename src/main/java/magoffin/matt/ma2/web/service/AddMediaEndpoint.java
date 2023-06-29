/* ===================================================================
 * AddMediaEndpoint.java
 * 
 * Created Dec 1, 2007 10:23:29 AM
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
 */

package magoffin.matt.ma2.web.service;

import java.io.File;


import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.Source;

import magoffin.matt.ma2.SystemConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.util.FileBasedTemporaryFile;

import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.AbstractSaxPayloadEndpoint;
import org.springframework.xml.transform.StringSource;
import org.xml.sax.ContentHandler;

/**
 * Web service endpoint for adding media.
 * 
 * <p>Uses SAX to handle potentially large amount of data encoded into the request.</p>
 * 
 * <p>Note that a {@link BizContext} must be available via {@link BizContextUtil#getBizContext()}
 * prior to invoking this service, to pass the user authentication to the import.</p> 
 *
 * @see BizContextUtil
 * @author matt
 * @version 1.0
 */
public class AddMediaEndpoint extends AbstractSaxPayloadEndpoint {
	
	private IOBiz ioBiz = null;
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	
	final Logger log = Logger.getLogger(getClass());

	@Override
	protected ContentHandler createContentHandler() throws Exception {
		File xmlFile = File.createTempFile("matte-add-media-", ".xml");
		if ( log.isDebugEnabled() ) {
			log.debug("Creating <m:collection-import> document at " 
					+xmlFile.getAbsolutePath());
		}
		File mediaFile = File.createTempFile("matte-add-media-", ".zip");
		if ( log.isDebugEnabled() ) {
			log.debug("Decoding <m:media-data> to "  +mediaFile.getAbsolutePath());
		}
		return new AddMediaContentHandler(xmlFile, mediaFile, this.outputFactory);
	}

	@Override
	protected Source getResponse(ContentHandler contentHandler) throws Exception {
		boolean success = true;
		final AddMediaContentHandler addContentHander = (AddMediaContentHandler)contentHandler;
		log.debug("Submitting AddMediaCommand work");
		AddMediaCommand command = new AddMediaCommand();
		command.setAutoAlbum(true);
		command.setCollectionId(addContentHander.getCollectionId());
		command.setLocalTz(addContentHander.getLocalTz());
		command.setMediaTz(addContentHander.getMediaTz());
		command.setTempFile(new FileBasedTemporaryFile(addContentHander.getMediaFile(), 
				"application/zip"));
		command.setMetaXmlFile(new FileBasedTemporaryFile(addContentHander.getXmlFile(), 
				"text/xml"));
		BizContext context = BizContextUtil.getBizContext();
		WorkInfo workInfo = ioBiz.importMedia(command, context);
		if ( log.isDebugEnabled() ) {
			log.debug("AddMediaCommand work submitted: " +workInfo.getTicket() 
					+", constructing <m:AddMediaResponse> response ");
		}
		StringBuilder buf = new StringBuilder();
		buf.append("<m:AddMediaResponse xmlns:m=\"")
			.append(SystemConstants.MATTE_XML_NAMESPACE_URI)
			.append("\" success=\"")
			.append(success).append("\" ticket=\"")
			.append(workInfo.getTicket()).append("\">");
		
		buf.append("</m:AddMediaResponse>");
		return new StringSource(buf.toString());
	}
	
	/**
	 * @return the ioBiz
	 */
	public IOBiz getIoBiz() {
		return ioBiz;
	}

	/**
	 * @param ioBiz the ioBiz to set
	 */
	public void setIoBiz(IOBiz ioBiz) {
		this.ioBiz = ioBiz;
	}

	/**
	 * @return the outputFactory
	 */
	public XMLOutputFactory getOutputFactory() {
		return outputFactory;
	}

	/**
	 * @param outputFactory the outputFactory to set
	 */
	public void setOutputFactory(XMLOutputFactory outputFactory) {
		this.outputFactory = outputFactory;
	}

}
