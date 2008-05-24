/* ===================================================================
 * UploadMedia.java
 * 
 * Created May 10, 2008, 7:49 PM
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An applet for uploading media into Matte.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class UploadMedia extends javax.swing.JApplet {

	/** The parameter that specifies the development code base to use. */
	public static final String PARAM_DEVEL_CODEBASE = "develCodeBase";

	private static final long serialVersionUID = -2493449216309735272L;
	private static final String DEFAULT_DATA_URL = "/../addServiceXml.do";
	private static final String DEV_DATA_URL = "/test-add-media-data.xml";
	private static final String DEFAULT_UPLOAD_URL = DEFAULT_DATA_URL;
	private static final String DEV_UPLOAD_URL = "/test-add-media-data.zip";

	private static final NamespaceContext XPATH_NS_CONTEXT = new XmlNamespaceContext();
	private static final String TZ_XPATH = "/x:x-data/x:x-auxillary/m:model/m:time-zone";
	private static final String COLLECTION_XPATH = "/x:x-data/x:x-auxillary/m:model/m:collection";
	private static final String SESSION_ID_XPATH = "/x:x-data/x:x-session/@session-id";

	static final Logger LOG = Logger.getLogger(UploadMedia.class.getName());
	static final DocumentBuilderFactory DOC_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
	static final TransformerFactory XFORMER_FACTORY = TransformerFactory.newInstance();
	static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

	private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(
			"Root Node");
	private DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
	private String sessionId = null;

	@Override
	public void init() {
		DOC_BUILDER_FACTORY.setNamespaceAware(true);
		initParameters();
		try {
			java.awt.EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					initComponents();
					initData();
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void initParameters() {
		// nothing yet
	}

	private boolean isLocalDev() {
		return super.getCodeBase().getProtocol().equals("file");
	}

	private URL getDataUrl() throws MalformedURLException {
		return new URL(getCodeBase()
				+ (isLocalDev() ? DEV_DATA_URL : DEFAULT_DATA_URL));
	}

	private URL getUploadUrl() throws MalformedURLException {
		return new URL(getCodeBase()
				+ (isLocalDev() ? DEV_UPLOAD_URL : DEFAULT_UPLOAD_URL));
	}

	private void initData() {
		InputStream in = null;
		Document doc = null;
		try {
			URL url = getDataUrl();
			in = url.openStream();
			DocumentBuilder parser = DOC_BUILDER_FACTORY.newDocumentBuilder();
			doc = parser.parse(in);
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("Called URL [" + url + "], got XML: " + doc);
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Can't populate data", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		if (LOG.isLoggable(Level.INFO)) {
			StringWriter out = new StringWriter();
			try {
				XFORMER_FACTORY.newTransformer().transform(new DOMSource(doc),
						new StreamResult(out));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			LOG.info("Got XML: " + out);
		}

		NodeList colList = (NodeList) evaluateXPath(doc, COLLECTION_XPATH,
				XPathConstants.NODESET);
		collectionCombo.removeAllItems();
		int max = colList.getLength();
		if (LOG.isLoggable(Level.INFO)) {
			LOG.info("Received " + max + " collections");
		}
		for (int i = 0; i < max; i++) {
			Element el = (Element) colList.item(i);
			String id = el.getAttribute("collection-id");
			String name = el.getAttribute("name");
			collectionCombo.addItem(new ListSelection(id, name));
		}

		NodeList tzList = (NodeList) evaluateXPath(doc.getDocumentElement(),
				TZ_XPATH, XPathConstants.NODESET);
		localTzCombo.removeAllItems();
		mediaTzCombo.removeAllItems();
		max = tzList.getLength();
		if (LOG.isLoggable(Level.INFO)) {
			LOG.info("Received " + max + " time zones");
		}
		for (int i = 0; i < max; i++) {
			Element el = (Element) tzList.item(i);
			String id = el.getAttribute("code");
			String name = el.getAttribute("name");
			localTzCombo.addItem(new ListSelection(id, name));
			mediaTzCombo.addItem(new ListSelection(id, name));
		}

		this.sessionId = (String) evaluateXPath(doc.getDocumentElement(),
				SESSION_ID_XPATH, XPathConstants.STRING);
	}

	private Object evaluateXPath(Node node, String xpath, QName returnType) {
		Object value;
		try {
			XPath xpathx = XPATH_FACTORY.newXPath();
			xpathx.setNamespaceContext(XPATH_NS_CONTEXT);
			value = xpathx.evaluate(xpath, node, returnType);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(
					"Error evaluating XPath [" + xpath + "]", e);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private void populateFileSelection(File[] files) {
		if (files == null || files.length < 1) {
			return;
		}
		populateFileSelection(treeRoot, files);
	}

	private void populateFileSelection(MutableTreeNode parent, File[] files) {
		if (files == null || files.length < 1) {
			return;
		}
		for (File file : files) {
			FileSelection selection = new FileSelection();
			selection.setFile(file);
			MutableTreeNode node = new DefaultMutableTreeNode(selection);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Adding [" + selection + "] selection to tree model");
			}
			treeModel.insertNodeInto(node, parent, parent.getChildCount());
			if (file.isDirectory()) {
				populateFileSelection(node, file.listFiles());
			}
		}
	}

	private void uploadFileSelection() {
		if (treeRoot.getChildCount() < 1) {
			// TODO handle no files selected
			return;
		}
		
		if ( isLocalDev() ) {
			createLocalDevUploadFile();
			return;
		}

		URL postUrl;
		try {
			postUrl = getUploadUrl();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		ListSelection collection = (ListSelection) collectionCombo.getSelectedItem();
		ListSelection mediaTz = (ListSelection) mediaTzCombo.getSelectedItem();
		ListSelection localTz = (ListSelection) localTzCombo.getSelectedItem();

		final ProgressMonitor monitor = new ProgressMonitor(this, "Uploading files to Matte",
				null, 0, 1);
		monitor.setMillisToPopup(500);
		final HttpClient client = new HttpClient();
		client.getState().addCookie(
				new Cookie(postUrl.getHost(), "JSESSIONID", this.sessionId,
						postUrl.getPath(), null, false));

		final PostMethod filePost = new PostMethod(postUrl.toString());
		filePost.getParams().setBooleanParameter(
				HttpMethodParams.USE_EXPECT_CONTINUE, false);
		final PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out = new PipedOutputStream();
		try {
			in.connect(out);
			new GenerateUploadDataThread(out, treeModel, monitor).start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Part[] parts = { new StringPart("collectionId", collection.getId()),
				new StringPart("mediaTz", mediaTz.getId()),
				new StringPart("localTz", localTz.getId()),
				new StringPart("autoAlbum", "true"), // TODO get from checkbox
				new FilePart("tempFile", new PartSource() {
					public InputStream createInputStream() throws IOException {
						return in;
					}

					public String getFileName() {
						return "MatteUploadAppletFile-" 
							+UUID.randomUUID().toString()
							+"+.zip";
					}

					public long getLength() {
						return Long.MAX_VALUE;
					}
				}) };
		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost
				.getParams()));

		// start new thread to upload... then we can track progress in UI
		new Thread() {
			@Override
			public void run() {
				try {
					if (LOG.isLoggable(Level.INFO)) {
						LOG.info("Posting HTTP request to: " + filePost.getURI());
					}
					int status = client.executeMethod(filePost);
					if (LOG.isLoggable(Level.INFO)) {
						LOG.info("Got HTTP status: " + status);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}.start();
	}

	private void createLocalDevUploadFile() {
		ListSelection collection = (ListSelection) collectionCombo.getSelectedItem();
		ListSelection mediaTz = (ListSelection) mediaTzCombo.getSelectedItem();
		ListSelection localTz = (ListSelection) localTzCombo.getSelectedItem();
		if ( LOG.isLoggable(Level.INFO) ) {
			LOG.info("Got collection [" +collection.getId() +"], mediaTz ["
					+mediaTz.getId() +"], localTz [" +localTz +"]");
		}
		
		final ProgressMonitor monitor = new ProgressMonitor(this, "Saveing test archive",
				null, 0, 1);
		monitor.setMillisToPopup(500);
		URL devUrl = null;
		Thread copyThread = null;
		try {
			devUrl = getUploadUrl();
			OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(
					URLDecoder.decode(devUrl.getFile()))));
			copyThread = new GenerateUploadDataThread(out, treeModel, monitor);
			copyThread.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		// start progress bar, get % complete from thread
		/*try {
			copyThread.join();
		} catch (InterruptedException e) {
			// ignore
		}*/
	}

	/**
	 * This method is called from within the init() method to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		bgPanel = new javax.swing.JPanel();
		optionsPanel = new javax.swing.JPanel();
		collectionLabel = new javax.swing.JLabel();
		collectionCombo = new javax.swing.JComboBox();
		mediaTzLabel = new javax.swing.JLabel();
		localTzLabel = new javax.swing.JLabel();
		autoAlbumLabel = new javax.swing.JLabel();
		mediaTzCombo = new javax.swing.JComboBox();
		localTzCombo = new javax.swing.JComboBox();
		autoAlbumCheckBox = new javax.swing.JCheckBox();
		fileSelectionPanel = new javax.swing.JPanel();
		chooseFilesButton = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		selectedFilesTree = new javax.swing.JTree(treeModel);
		selectedFilesTree.setRootVisible(false);
		selectedFilesTree.setShowsRootHandles(true);
		clearFilesButton = new javax.swing.JButton();
		addButton = new javax.swing.JButton();

		setBackground(new java.awt.Color(236, 236, 236));

		optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, "Options",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		collectionLabel.setText("Collection");

		collectionCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

		mediaTzLabel.setText("Media Time Zone");

		localTzLabel.setText("Local Time Zone");

		autoAlbumLabel.setText("Auto Album");

		mediaTzCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

		localTzCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

		autoAlbumCheckBox
				.setText("<html>Selecting this option when uploading a zip archive<br>\nwill turn folders in the archive into albums.</html>");
		autoAlbumCheckBox.setActionCommand("autoalbum");
		autoAlbumCheckBox
				.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

		org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(
				optionsPanel);
		optionsPanel.setLayout(optionsPanelLayout);
		optionsPanelLayout
				.setHorizontalGroup(optionsPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								optionsPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												optionsPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(collectionLabel)
														.add(mediaTzLabel).add(
																localTzLabel)
														.add(autoAlbumLabel))
										.add(27, 27, 27)
										.add(
												optionsPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(autoAlbumCheckBox)
														.add(
																optionsPanelLayout
																		.createParallelGroup(
																				org.jdesktop.layout.GroupLayout.TRAILING,
																				false)
																		.add(
																				org.jdesktop.layout.GroupLayout.LEADING,
																				localTzCombo,
																				0,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.add(
																				org.jdesktop.layout.GroupLayout.LEADING,
																				mediaTzCombo,
																				0,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.add(
																				org.jdesktop.layout.GroupLayout.LEADING,
																				collectionCombo,
																				0,
																				178,
																				Short.MAX_VALUE)))
										.addContainerGap(13, Short.MAX_VALUE)));
		optionsPanelLayout
				.setVerticalGroup(optionsPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								optionsPanelLayout
										.createSequentialGroup()
										.add(
												optionsPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(collectionLabel)
														.add(
																collectionCombo,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												optionsPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(mediaTzLabel)
														.add(
																mediaTzCombo,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												optionsPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(localTzLabel)
														.add(
																localTzCombo,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												optionsPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(autoAlbumLabel)
														.add(autoAlbumCheckBox))
										.addContainerGap(
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		fileSelectionPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "Files",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		chooseFilesButton.setText("Choose...");
		chooseFilesButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						chooseFilesActionPerformed(evt);
					}
				});

		jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
		jScrollPane1.setViewportView(selectedFilesTree);

		clearFilesButton.setText("Clear");
		clearFilesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearFilesButtonActionPerformed(evt);
			}
		});

		org.jdesktop.layout.GroupLayout fileSelectionPanelLayout = new org.jdesktop.layout.GroupLayout(
				fileSelectionPanel);
		fileSelectionPanel.setLayout(fileSelectionPanelLayout);
		fileSelectionPanelLayout
				.setHorizontalGroup(fileSelectionPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								fileSelectionPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												jScrollPane1,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												282, Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(
												fileSelectionPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING,
																false)
														.add(
																clearFilesButton,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.add(
																chooseFilesButton,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));
		fileSelectionPanelLayout
				.setVerticalGroup(fileSelectionPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								fileSelectionPanelLayout
										.createSequentialGroup()
										.add(
												fileSelectionPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																fileSelectionPanelLayout
																		.createSequentialGroup()
																		.add(
																				chooseFilesButton)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.UNRELATED)
																		.add(
																				clearFilesButton))
														.add(
																jScrollPane1,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																134,
																Short.MAX_VALUE))
										.addContainerGap()));

		addButton.setText("Add");
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});

		org.jdesktop.layout.GroupLayout bgPanelLayout = new org.jdesktop.layout.GroupLayout(
				bgPanel);
		bgPanel.setLayout(bgPanelLayout);
		bgPanelLayout
				.setHorizontalGroup(bgPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								bgPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												bgPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING,
																false)
														.add(
																fileSelectionPanel,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.add(addButton)
														.add(
																optionsPanel,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap(
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		bgPanelLayout.setVerticalGroup(bgPanelLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				bgPanelLayout.createSequentialGroup().addContainerGap().add(
						fileSelectionPanel,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE).addPreferredGap(
						org.jdesktop.layout.LayoutStyle.RELATED).add(
						optionsPanel,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.UNRELATED).add(
								addButton).addContainerGap()));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(bgPanel,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
		layout.setVerticalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(bgPanel,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
	}// </editor-fold>//GEN-END:initComponents

	private void chooseFilesActionPerformed(@SuppressWarnings("unused")
	java.awt.event.ActionEvent evt) {// GEN-FIRST:event_chooseFilesActionPerformed
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int chooseResult = chooser.showOpenDialog(null);
		if (chooseResult != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File[] files = chooser.getSelectedFiles();
		if (LOG.isLoggable(Level.INFO)) {
			LOG.info("Got " + Arrays.toString(files));
		}
		populateFileSelection(files);
		treeModel.reload();
	}// GEN-LAST:event_chooseFilesActionPerformed

	private void addButtonActionPerformed(@SuppressWarnings("unused")
	java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addButtonActionPerformed
		uploadFileSelection();
	}// GEN-LAST:event_addButtonActionPerformed

	private void clearFilesButtonActionPerformed(@SuppressWarnings("unused")
	java.awt.event.ActionEvent evt) {
		treeRoot.removeAllChildren();
		treeModel.reload();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton addButton;
	private javax.swing.JCheckBox autoAlbumCheckBox;
	private javax.swing.JLabel autoAlbumLabel;
	private javax.swing.JPanel bgPanel;
	private javax.swing.JButton chooseFilesButton;
	private javax.swing.JButton clearFilesButton;
	private javax.swing.JComboBox collectionCombo;
	private javax.swing.JLabel collectionLabel;
	private javax.swing.JPanel fileSelectionPanel;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JComboBox localTzCombo;
	private javax.swing.JLabel localTzLabel;
	private javax.swing.JComboBox mediaTzCombo;
	private javax.swing.JLabel mediaTzLabel;
	private javax.swing.JPanel optionsPanel;
	private javax.swing.JTree selectedFilesTree;
	// End of variables declaration//GEN-END:variables

}
