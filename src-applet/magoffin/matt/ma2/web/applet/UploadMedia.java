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
 */

package magoffin.matt.ma2.web.applet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
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
 * @version 1.0
 */
public class UploadMedia extends javax.swing.JApplet {

	/** The parameter that specifies the development code base to use. */
	public static final String PARAM_DEVEL_CODEBASE = "develCodeBase";

	private static final long serialVersionUID = -2493449216309735272L;
	private static final String DEFAULT_DATA_URL = "../addServiceXml.do";
	private static final String DEV_DATA_URL = "/test-add-media-data.xml";
	private static final String DEFAULT_UPLOAD_URL = DEFAULT_DATA_URL;
	private static final String DEV_UPLOAD_URL = "/test-add-media-data.zip";

	private static final NamespaceContext XPATH_NS_CONTEXT = new XmlNamespaceContext();
	private static final String TZ_XPATH = "/x:x-data/x:x-auxillary/m:model/m:time-zone";
	private static final String COLLECTION_XPATH = "/x:x-data/x:x-auxillary/m:model/m:collection";
	private static final String SESSION_ID_XPATH = "/x:x-data/x:x-session/@session-id";
	private static final String USER_TZ_PATH = "/x:x-data/x:x-session/m:session/m:acting-user/m:tz/@code";
	private static final Set<Integer> EXPECTED_POST_RESULT_HTTP_STATUS 
		= Collections.unmodifiableSet(new HashSet<Integer>(
			Arrays.asList(new Integer[] {200, 302})));
	
	static final Logger LOG = Logger.getLogger(UploadMedia.class.getName());
	static final DocumentBuilderFactory DOC_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
	static final TransformerFactory XFORMER_FACTORY = TransformerFactory.newInstance();
	static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

	private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root Node");
	private DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
	private String sessionId = null;
	private boolean zipSelectionWarningShown = false;
	private ResourceBundle msgBundle = null;

	@Override
	public void init() {
		DOC_BUILDER_FACTORY.setNamespaceAware(true);
		msgBundle = getMessageResourceBundle();
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
	
	private ResourceBundle getMessageResourceBundle() {
		return ResourceBundle.getBundle(
				"magoffin/matt/ma2/web/applet/UploadApplet");
	}
	
	private String getMessage(String key, Object... params) {
		MessageFormat fmt = new MessageFormat(msgBundle.getString(key), getLocale());
		return fmt.format(params);
	}

	private void initData() {
		InputStream in = null;
		Document doc = null;
		try {
			URL url = getDataUrl();
			in = url.openStream();
			DocumentBuilder parser = DOC_BUILDER_FACTORY.newDocumentBuilder();
			doc = parser.parse(in);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Called URL [" + url + "], got XML: " + doc);
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Can't populate data", e);
			JOptionPane.showMessageDialog(this, 
					getMessage("alert.error.initdata", e.toString()), 
					msgBundle.getString("alert.title.error"), 
					JOptionPane.ERROR_MESSAGE);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		if ( doc == null ) {
			LOG.severe("Unable to load XML.");
			return;
		}

		if (LOG.isLoggable(Level.FINE)) {
			StringWriter out = new StringWriter();
			try {
				XFORMER_FACTORY.newTransformer().transform(new DOMSource(doc),
						new StreamResult(out));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			LOG.fine("Got XML: " + out);
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
		
		String userDefaultTz = (String) evaluateXPath(doc.getDocumentElement(),
				USER_TZ_PATH, XPathConstants.STRING);
		if ( userDefaultTz == null || userDefaultTz.length() < 1 ) {
			userDefaultTz = TimeZone.getDefault().getID();
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
			if ( id.equals(userDefaultTz) ) {
				localTzCombo.setSelectedIndex(localTzCombo.getItemCount() - 1);
				mediaTzCombo.setSelectedIndex(mediaTzCombo.getItemCount() - 1);
			}
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

	private void populateFileSelection(File[] files) {
		if (files == null || files.length < 1) {
			return;
		}
		if ( isSingleZipArchive() ) {
			JOptionPane.showMessageDialog(this, 
					msgBundle.getString("alert.zip.selection.msg"),
					msgBundle.getString("alert.title.warning"),
					JOptionPane.WARNING_MESSAGE);
				return;
		}
		this.zipSelectionWarningShown = false;
		populateFileSelection(treeRoot, files);
	}
	
	private boolean isSingleZipArchive() {
		if ( treeRoot.getChildCount() == 1 ) {
			// check if a zip archive is currently selected, and if so
			// do not allow any new selection
			DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)
				treeRoot.getChildAt(0);
			FileSelection fs = (FileSelection)currNode.getUserObject();
			if ( fs.getFile().getName().toLowerCase().endsWith(".zip") ) {
				return true;
			}
		}
		return false;
	}

	private void populateFileSelection(DefaultMutableTreeNode parent, File[] files) {
		if (files == null || files.length < 1) {
			return;
		}
		for (File file : files) {
			if ( file.getName().toLowerCase().endsWith(".zip") 
					&& (!parent.isRoot() || parent.getChildCount() > 0) ) {
				if ( !zipSelectionWarningShown ) {
					JOptionPane.showMessageDialog(this, 
							msgBundle.getString("alert.zip.selection.msg"),
							msgBundle.getString("alert.title.warning"),
							JOptionPane.WARNING_MESSAGE);
					this.zipSelectionWarningShown = true;
				}
				continue;
			}
			FileSelection selection = new FileSelection();
			selection.setFile(file);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(selection);
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
			JOptionPane.showMessageDialog(this, 
					msgBundle.getString("alert.no.selection.msg"),
					null, JOptionPane.INFORMATION_MESSAGE);

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

		final HttpClient client = new HttpClient();
		client.getState().addCookie(
				new Cookie(postUrl.getHost(), "JSESSIONID", this.sessionId,
						postUrl.getPath(), null, false));

		final PostMethod filePost = new PostMethod(postUrl.toString());
		filePost.getParams().setBooleanParameter(
				HttpMethodParams.USE_EXPECT_CONTINUE, false);
		final InputStream in;
		try {
			in = getUploadInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Part[] parts = { new StringPart("collectionId", collection.getId()),
				new StringPart("mediaTz", mediaTz.getId()),
				new StringPart("localTz", localTz.getId()),
				new StringPart("autoAlbum", Boolean.valueOf(
						autoAlbumCheckBox.isSelected()).toString()),
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
						// we don't know the actual length, but for HttpClient all that
						// matters is that this is > 0
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
					if ( !EXPECTED_POST_RESULT_HTTP_STATUS.contains(status) ) {
						JOptionPane.showMessageDialog(UploadMedia.this, 
								getMessage("alert.error.upload.unexpected.status", status), 
								msgBundle.getString("alert.title.error"), 
								JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(UploadMedia.this, 
								msgBundle.getString("alert.upload.complete"), 
								msgBundle.getString("alert.title.info"), 
								JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (IOException e) {
					JOptionPane.showMessageDialog(UploadMedia.this, 
							getMessage("alert.error.upload.ioexception", e.getMessage()), 
							msgBundle.getString("alert.title.error"), 
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}.start();
	}

	private InputStream getUploadInputStream() throws IOException {
		if ( isSingleZipArchive() ) {
			// for zip archive, upload directly
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				treeRoot.getChildAt(0);
			FileSelection zip = (FileSelection)node.getUserObject();
			String title = getMessage("upload.progress.reading.file", zip.getFile().getName());
			return new BufferedInputStream(new ProgressMonitorInputStream(this, 
					title, new FileInputStream(zip.getFile())));
		}
		
		// we'll be zipping ourselves, so create piped input stream we can
		// generate the zip archive to
		final ProgressMonitor monitor = new ProgressMonitor(this, 
				msgBundle.getString("upload.progress.title"), null, 0, 1);
		final PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out = new PipedOutputStream();
		try {
			in.connect(out);
			new GenerateUploadDataThread(out, treeModel, monitor).start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return in;
	}

	private void createLocalDevUploadFile() {
		ListSelection collection = (ListSelection) collectionCombo.getSelectedItem();
		ListSelection mediaTz = (ListSelection) mediaTzCombo.getSelectedItem();
		ListSelection localTz = (ListSelection) localTzCombo.getSelectedItem();
		if ( LOG.isLoggable(Level.INFO) ) {
			LOG.info("Got collection [" +collection.getId() +"], mediaTz ["
					+mediaTz.getId() +"], localTz [" +localTz +"]");
		}
		
		URL devUrl = null;
		try {
			devUrl = getUploadUrl();
			File outFile = new File(devUrl.toURI());
			if ( LOG.isLoggable(Level.INFO) ) {
				LOG.info("Creating test acrhive file: " +outFile.getAbsolutePath());
			}
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
			if ( isSingleZipArchive() ) {
				try {
					copy(getUploadInputStream(), out);
				} finally {
					try {
						out.flush();
						out.close();
					} catch ( IOException e ) {
						// ignore
					}
				}
			} else {
				final ProgressMonitor monitor = new ProgressMonitor(this, 
						"Saving test archive", null, 0, 1);
				new GenerateUploadDataThread(out, treeModel, monitor).start();
			}
		} catch ( URISyntaxException e ) {
			throw new RuntimeException(e);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method is called from within the init() method to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        fileSelectionPanel = new javax.swing.JPanel();
        chooseFilesButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        selectedFilesTree = new javax.swing.JTree(treeModel);
        selectedFilesTree.setRootVisible(false);
        selectedFilesTree.setShowsRootHandles(true);
        clearFilesButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(236, 236, 236));

        bgPanel.setBackground(new java.awt.Color(218, 217, 209));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("magoffin/matt/ma2/web/applet/UploadApplet"); // NOI18N
        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("label.pane.options"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        optionsPanel.setOpaque(false);

        collectionLabel.setText(bundle.getString("label.collection")); // NOI18N

        collectionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        mediaTzLabel.setText(bundle.getString("label.tz.media")); // NOI18N

        localTzLabel.setText(bundle.getString("label.tz.local")); // NOI18N

        autoAlbumLabel.setText("Auto Album");

        mediaTzCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        localTzCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        autoAlbumCheckBox.setActionCommand("autoalbum");
        autoAlbumCheckBox.setOpaque(false);
        autoAlbumCheckBox.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        autoAlbumCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextPane1.setBackground(bgPanel.getBackground());
        jTextPane1.setBorder(null);
        jTextPane1.setEditable(false);
        jTextPane1.setText(bundle.getString("label.autoalbum")); // NOI18N
        jScrollPane2.setViewportView(jTextPane1);

        org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(collectionLabel)
                    .add(mediaTzLabel)
                    .add(localTzLabel)
                    .add(autoAlbumLabel))
                .add(27, 27, 27)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(autoAlbumCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 276, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(localTzCombo, 0, 303, Short.MAX_VALUE)
                    .add(mediaTzCombo, 0, 303, Short.MAX_VALUE)
                    .add(collectionCombo, 0, 303, Short.MAX_VALUE))
                .add(39, 39, 39))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(collectionLabel)
                    .add(collectionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mediaTzLabel)
                    .add(mediaTzCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(localTzLabel)
                    .add(localTzCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(autoAlbumLabel)
                        .add(autoAlbumCheckBox))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                .addContainerGap())
        );

        fileSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("label.pane.files"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        fileSelectionPanel.setOpaque(false);

        chooseFilesButton.setText(bundle.getString("button.choose.files")); // NOI18N
        chooseFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFilesActionPerformed(evt);
            }
        });

        selectedFilesTree.setDragEnabled(true);
        selectedFilesTree.setEditable(true);
        jScrollPane1.setViewportView(selectedFilesTree);

        clearFilesButton.setText(bundle.getString("button.clear.files")); // NOI18N
        clearFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilesButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout fileSelectionPanelLayout = new org.jdesktop.layout.GroupLayout(fileSelectionPanel);
        fileSelectionPanel.setLayout(fileSelectionPanelLayout);
        fileSelectionPanelLayout.setHorizontalGroup(
            fileSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, fileSelectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(fileSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(clearFilesButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(chooseFilesButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        fileSelectionPanelLayout.setVerticalGroup(
            fileSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileSelectionPanelLayout.createSequentialGroup()
                .add(fileSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileSelectionPanelLayout.createSequentialGroup()
                        .add(chooseFilesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(clearFilesButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                .addContainerGap())
        );

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout bgPanelLayout = new org.jdesktop.layout.GroupLayout(bgPanel);
        bgPanel.setLayout(bgPanelLayout);
        bgPanelLayout.setHorizontalGroup(
            bgPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bgPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(bgPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addButton)
                    .add(optionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                    .add(fileSelectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        bgPanelLayout.setVerticalGroup(
            bgPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bgPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fileSelectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addButton)
                .add(17, 17, 17))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bgPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bgPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
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
		TreePath[] selected = selectedFilesTree.getSelectionPaths();
		if ( selected == null ) {
			treeRoot.removeAllChildren();
			treeModel.reload();
			return;
		}
		
		// remove selected item(s)
		List<TreePath> nonNestedPaths = new LinkedList<TreePath>();
		for ( int i = 0; i < selected.length; i++ ) {
			boolean nested = false;
			for ( int j = 0; j < selected.length; j++ ) {
				if ( i == j ) {
					continue;
				}
				if ( selected[j].isDescendant(selected[i]) ) {
					nested = true;
					break;
				}
			}
			if ( !nested ) {
				nonNestedPaths.add(selected[i]);
			}
		}
		for ( TreePath path : nonNestedPaths ) {
			MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
			treeModel.removeNodeFromParent(node);
		}
	}

    // borrowed from Spring's FileCopyUtils, but don't close output stream
	static int copy(InputStream in, OutputStream out) throws IOException {
		try {
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				// ignore
			}
		}
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JComboBox localTzCombo;
    private javax.swing.JLabel localTzLabel;
    private javax.swing.JComboBox mediaTzCombo;
    private javax.swing.JLabel mediaTzLabel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTree selectedFilesTree;
    // End of variables declaration//GEN-END:variables

}
