/* ===================================================================
 * GenerateUploadDataThread.java
 * 
 * Copyright (c) 2008 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.web.applet;

import static magoffin.matt.ma2.web.applet.UploadMedia.*;
import static magoffin.matt.ma2.web.applet.XmlNamespaceContext.MATTE_NAMESPACE_URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.ProgressMonitor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class GenerateUploadDataThread extends Thread {

	private static final String DATE_FORMAT_PAT = "yyyy-MM-dd";
	private static final String ELEMENT_ITEM_IMPORT = "item";
	private static final String ELEMENT_ALBUM_IMPORT = "album";
	private static final String ELEMENT_COLLECTION_IMPORT = "collection-import";
	
	private OutputStream out;
	private TreeModel fileSelections;
	private DateFormat dateFormat;
	private ProgressMonitor monitor;
	
	private int numFilesToUpload = 0;
	private int numFilesComplete = 0;

	/**
	 * Construct with an OutputStream and TreeModel of FileSelection instances.
	 * 
	 * @param out the output stream to generate the upload zip archive to
	 * @param fileSelectionModel the selected file data
	 * @pram monitor a progress monitor
	 */
	GenerateUploadDataThread(OutputStream out, TreeModel fileSelectionModel, 
			ProgressMonitor monitor) {
		this.out = out;
		this.fileSelections = fileSelectionModel;
		this.dateFormat = new SimpleDateFormat(DATE_FORMAT_PAT);
		this.monitor = monitor;
	}

	@Override
	public void run() {
		ZipOutputStream zout = null;
		try {
			zout = new ZipOutputStream(this.out);
			
			// create metadata element first
			zout.putNextEntry(new ZipEntry("metadata.xml"));
			Document meta = createMetadataXml();
			Transformer copy = XFORMER_FACTORY.newTransformer();
			copy.transform(new DOMSource(meta), new StreamResult(zout));
			
			// update progress monitor stats
			monitor.setMaximum(this.numFilesToUpload);
			
			// now copy actual files
			copyFiles(zout, (DefaultMutableTreeNode)fileSelections.getRoot());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				zout.flush();
				zout.closeEntry();
				zout.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	/**
	 * Get the percent complete.
	 * 
	 * @return the percent complete, as a float
	 */
	public float getPercentComplete() {
		if ( numFilesToUpload < 1 ) {
			return 0f;
		}
		return (float)numFilesComplete / (float)numFilesToUpload;
	}
	
	private void copyFiles(ZipOutputStream zout, DefaultMutableTreeNode root) 
	throws IOException {
		int count = root.getChildCount();
		TreeNode[] path = root.getPath();
		
		// create archive path, starting from level 1 (ignore root) to length
		StringBuilder buf = new StringBuilder();
		for ( int i = 1; i < path.length; i++ ) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path[i];
			FileSelection fs = (FileSelection)node.getUserObject();
			if ( buf.length() > 0 ) {
				buf.append('/');
			}
			buf.append(fs.getFile().getName());
		}
		String basePath = buf.toString();
		
		for ( int i = 0; i < count; i++ ) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
			FileSelection fs = (FileSelection)node.getUserObject();
			if ( fs.getFile().isFile() ) {
				String archivePath = basePath +'/' +fs.getFile().getName();
				zout.putNextEntry(new ZipEntry(archivePath));
				
				if ( LOG.isLoggable(Level.INFO) ) {
					LOG.info("Processing file [" +archivePath +"] (" 
							+(this.numFilesComplete+1) +'/' +this.numFilesToUpload +')');
				}
				monitor.setNote("Uploading " +archivePath);
				
				copy(new FileInputStream(fs.getFile()), zout);
				
				this.numFilesComplete++;
				monitor.setProgress(this.numFilesComplete);
			}
			if ( !node.isLeaf() ) {
				copyFiles(zout, node);
			}
		}
	}

	// borrowed from Spring's FileCopyUtils, but don't close output stream
	private int copy(InputStream in, OutputStream os) throws IOException {
		try {
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			os.flush();
			return byteCount;
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				// ignore
			}
		}
	}
	
	private Document createMetadataXml() throws ParserConfigurationException {
		Document dom = DOC_BUILDER_FACTORY.newDocumentBuilder().newDocument();
		Element domRoot = dom.createElementNS(MATTE_NAMESPACE_URI, ELEMENT_COLLECTION_IMPORT);
		dom.appendChild(domRoot);
		DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)fileSelections.getRoot();
		int count = treeRoot.getChildCount();
		for ( int i = 0; i < count; i++ ) {
			DefaultMutableTreeNode topNode = (DefaultMutableTreeNode)treeRoot.getChildAt(i);
			FileSelection topFile = (FileSelection)topNode.getUserObject();
			if ( topFile.getFile().isDirectory() ) {
				populateAlbumMetadata(domRoot, topNode, "");
			}
		}
		return dom;
	}

	private void populateAlbumMetadata(Element domRoot, DefaultMutableTreeNode rootNode, 
			String archiveRoot) {
		FileSelection rootFile = (FileSelection)rootNode.getUserObject();
		File dir = rootFile.getFile();
		// create album
		Element album = domRoot.getOwnerDocument().createElementNS(MATTE_NAMESPACE_URI, 
				ELEMENT_ALBUM_IMPORT);
		album.setAttribute("name", dir.getName());
		album.setAttribute("sort", "date"); // TODO capture this setting in GUI
		album.setAttribute("album-date", dateFormat.format(new Date(dir.lastModified())));
		domRoot.appendChild(album);
		
		int numChildren = rootNode.getChildCount();
		List<DefaultMutableTreeNode> subdirs = new LinkedList<DefaultMutableTreeNode>();
		
		String albumArchivePath = archiveRoot 
			+(archiveRoot.length() > 0 ? "/" : "") 
			+dir.getName() 
			+'/';
		
		for ( int i = 0; i < numChildren; i++ ) {
			DefaultMutableTreeNode oneNode = (DefaultMutableTreeNode)rootNode.getChildAt(i);
			FileSelection oneFile = (FileSelection)oneNode.getUserObject();
			if ( oneFile.getFile().isDirectory() ) {
				subdirs.add(oneNode);
				continue;
			}
			Element item = domRoot.getOwnerDocument().createElementNS(MATTE_NAMESPACE_URI, 
					ELEMENT_ITEM_IMPORT);
			item.setAttribute("name", oneFile.getFile().getName());
			item.setAttribute("archive-path", albumArchivePath +oneFile.getFile().getName());
			album.appendChild(item);
			
			this.numFilesToUpload++;
		}
		
		// handle nested albums
		for ( DefaultMutableTreeNode oneNode : subdirs ) {
			populateAlbumMetadata(album, oneNode, archiveRoot);
		}
		
	}

}