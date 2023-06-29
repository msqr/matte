/* ===================================================================
 * AddMediaMessageEndpoint.java
 * 
 * Created Dec 4, 2007 11:45:15 AM
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
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import magoffin.matt.ma2.SystemConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.ma2.util.XmlHelper;
import magoffin.matt.util.FileBasedTemporaryFile;

import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.ws.server.endpoint.MessageEndpoint;

/**
 * Implementation of AddMediaRequest web service that uses SOAP attachments for
 * the media data.
 * 
 * <p>This works better than {@link AddMediaEndpoint} in the default configuration 
 * of Spring-WS, which does not seem to handle large text element content well
 * (as in, large base64 encoded element content).</p>
 *
 * @author matt
 * @version 1.0
 */
public class AddMediaMessageEndpoint implements MessageEndpoint {
	
	private final Logger log = Logger.getLogger(getClass());

	private IOBiz ioBiz = null;
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	private XmlHelper xmlHelper = null;

	/* (non-Javadoc)
	 * @see org.springframework.ws.server.endpoint.MessageEndpoint#invoke(org.springframework.ws.context.MessageContext)
	 */
	@SuppressWarnings("unchecked")
	public void invoke(MessageContext messageContext) throws Exception {
		WebServiceMessage msg = messageContext.getRequest();
		if ( !(msg instanceof MimeMessage) ) {
			throw new IllegalArgumentException("Only MimeMessage is supported");
		}
		
		File xmlFile = File.createTempFile("matte-add-media-", ".xml");
		if ( log.isDebugEnabled() ) {
			log.debug("Creating <m:collection-import> document at " 
					+xmlFile.getAbsolutePath());
		}
		File mediaFile = File.createTempFile("matte-add-media-", ".zip");
		if ( log.isDebugEnabled() ) {
			log.debug("Decoding <m:media-data> to "  +mediaFile.getAbsolutePath());
		}
		
		// re-use the SAX content handler used by AddMediaEndpoint, even though
		// it won't find the <m:media-data> data, as it's an attachment here
		AddMediaContentHandler addContentHander = new AddMediaContentHandler(
				xmlFile, mediaFile, this.outputFactory);
		SAXResult result = new SAXResult(addContentHander);
		xmlHelper.transformXml(msg.getPayloadSource(), result);

		MimeMessage mimeRequest = (MimeMessage)msg;
		Iterator<Attachment> itr = mimeRequest.getAttachments();
		Attachment media = null;
		while ( itr.hasNext() ) {
			Attachment a = itr.next();
			String contentType = a.getContentType();
			// take the first non-XML attachment
			if ( !contentType.equalsIgnoreCase("text/xml") ) {
				media = a;
				break;
			}
		}
		if ( media == null ) {
			throw new IllegalArgumentException("Media not attached.");
		}
		
		// copy media to tmp file
		FileCopyUtils.copy(media.getInputStream(), new FileOutputStream(mediaFile));
		
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
		StringBuilder buf = new StringBuilder();
		buf.append("<m:AddMediaResponse xmlns:m=\"")
			.append(SystemConstants.MATTE_XML_NAMESPACE_URI)
			.append("\" success=\"")
			.append(true).append("\" ticket=\"")
			.append(workInfo.getTicket()).append("\">");
		
		buf.append("</m:AddMediaResponse>");
		
		Result response = messageContext.getResponse().getPayloadResult();
		xmlHelper.transformXml(new StreamSource(
				new StringReader(buf.toString())), response);
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

	/**
	 * @return the xmlHelper
	 */
	public XmlHelper getXmlHelper() {
		return xmlHelper;
	}

	/**
	 * @param xmlHelper the xmlHelper to set
	 */
	public void setXmlHelper(XmlHelper xmlHelper) {
		this.xmlHelper = xmlHelper;
	}

}
