/* ===================================================================
 * MediaAlbumExportTool.java
 * 
 * Created Jun 24, 2007 9:12:38 AM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.stream.StreamResult;

import magoffin.matt.ma2.domain.AlbumImportType;
import magoffin.matt.ma2.domain.CollectionImport;
import magoffin.matt.ma2.domain.ItemImportType;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Tool to export albums from a Media Album database into a file/folder
 * structure for importing into Matte.
 * 
 * <p><b>Known limitations:</b> non-ASCII characters in file paths are not 
 * handled well in some situations. In this case the files might not be 
 * copied, and the <code>metadata.xml</code> for the album will 
 * be created but may have incorrect archive paths set.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class MediaAlbumExportTool {
	
	private static final String ALBUM_COMMENT = "comment";
	private static final String ALBUM_ID = "album_id";
	private static final String ALBUM_NAME = "name";
	private static final String ALBUM_CREATION_DATE = "creation_date";
	private static final String ALBUM_ALBUM_DATE = "album_date";
	private static final String ALBUM_MODIFICATION_DATE = "mod_date";

	private final String TOP_LEVEL_ALBUM_SQL = "select album_id, name, "
		+"comment, creation_date, album_date from pa_albums "
		+"where owner = ? and p_album_id is null order by album_id";
	
	private final String CHILD_ALBUM_SQL = "select album_id, name, "
		+"comment, creation_date, album_date, mod_date from pa_albums "
		+"where p_album_id = ? order by album_id";
	
	private final String ITEMS_FOR_ALBUM_SQL = 
		"select item.path, item.name, item.comment, item.hits, item.creation_date, "
		+"rating.rating, collections.path as cpath, freedata.data_value as keywords "
		+"from pa_album_media album "
		+"left join pa_media item on item.media_id = album.media_id "
		+"left join pa_media_sources collections on collections.source_id = item.source_id "
		+"left outer join pa_media_ratings rating on rating.media_id = item.media_id "
		+"left outer join pa_free_data as freedata on freedata.media_id = item.media_id "
		+"where album.album_id = ? "
		+"and (rating.rate_id is null or rating.user_id = ?) "
		+"and (freedata.data_id is null or (freedata.data_type_id = 2 and freedata.user_id = ?)) "
		+"order by album.disporder";
		
	
	private final Logger log = Logger.getLogger(getClass());
	
	private JdbcTemplate mediaAlbumJdbcTemplate = null;
	private Integer ownerId = 1;
	private File outputDir = new File("/var/tmp");
	private XmlHelper xmlHelper;
	private File collectionDir = new File("/var/ma/collections");
	
	private Resource baseDir = null;
	
	/**
	 * Run the tool.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		File baseOutputDir = new File(outputDir, "MediaAlbumExport");
		baseOutputDir.mkdirs();
		List<Map<String, ?>> albums = mediaAlbumJdbcTemplate.queryForList(
				TOP_LEVEL_ALBUM_SQL, new Object[] {ownerId});
		int largestId = 0;
		for ( Map<String, ?> albumData : albums ) {
			Number albumId = (Number)albumData.get(ALBUM_ID);
			if ( albumId == null ) continue;
			if ( albumId.intValue() > largestId ) {
				largestId = albumId.intValue();
			}
		}
		String albumOutputDirTemplate = "%0" +String.valueOf(largestId).length() +"d - %s";
		for ( Map<String, ?> albumData : albums ) {
			try {
				CollectionImport xml = xmlHelper.getObjectFactory().createCollectionImport();
				
				// make top output dir a sub-directory of baseOutputDir, for metadata.xml
				String albumOutputDirName = String.format(albumOutputDirTemplate, 
						albumData.get(ALBUM_ID), getFileName((String)albumData.get(ALBUM_NAME)));
				File albumOutputDir = new File(baseOutputDir, albumOutputDirName);
				albumOutputDir.mkdir();
				
				this.baseDir = new FileSystemResource(albumOutputDir);
				exportAlbum(albumOutputDir, albumData, xml, null);
				
				File xmlFile = new File(albumOutputDir, "metadata.xml");
				xmlHelper.transformXml(new JAXBSource(xmlHelper.getJaxbContext(),xml), 
						new StreamResult(new FileWriter(xmlFile)));
			} catch ( JAXBException e ) {
				throw new RuntimeException(e);
			} catch ( IOException e ) {
				throw new RuntimeException(e);
			}
		}
		if ( log.isInfoEnabled() ) {
			log.info("Exported " +albums.size() +" top-level albums");
		}
	}
	
	private String getFileName(String albumName) {
		String result = albumName;
		if ( result.indexOf(File.separatorChar) >= 0 ) {
			result = result.replace(File.separatorChar, '-');
		}
		if ( result.indexOf('/') >= 0 ) {
			result = result.replace('/', '-');
		}
		if ( result.indexOf(':') >= 0 ) {
			result = result.replace(':', '-');
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private File exportAlbum(File dir, Map<String, ?> albumData, 
			CollectionImport xml, AlbumImportType parent) 
	throws JAXBException, IOException {
		String albumName = (String)albumData.get(ALBUM_NAME);
		Integer albumId = (Integer)albumData.get(ALBUM_ID);
		String albumExportName = getFileName(albumName);
		File albumDir = new File(dir, albumExportName);
		albumDir.mkdir();

		if ( log.isInfoEnabled() ) {
			log.info("Exporting album [" +albumName +"], ID " +albumId
					+" to " +albumDir);
		}
		
		AlbumImportType albumXml = xmlHelper.getObjectFactory().createAlbumImportType();
		albumXml.setName(albumName);
		albumXml.setComment((String)albumData.get(ALBUM_COMMENT));
		albumXml.setCreationDate(getCalendar(albumData.get(ALBUM_CREATION_DATE)));
		albumXml.setModifyDate(getCalendar(albumData.get(ALBUM_MODIFICATION_DATE)));
		albumXml.setAlbumDate(getCalendar(albumData.get(ALBUM_ALBUM_DATE)));

		if ( parent == null ) {
			xml.getAlbum().add(albumXml);
		} else {
			parent.getAlbum().add(albumXml);
		}
		
		// handle album items
		exportAlbumItems(albumId, albumXml, albumDir);
		
		// handle chilren albums
		List<Map<String, ?>> childAlbums = mediaAlbumJdbcTemplate.queryForList(
				CHILD_ALBUM_SQL, new Object[] {albumId});
		for ( Map<String, ?> childAlbumData : childAlbums ) {
			exportAlbum(albumDir, childAlbumData, xml, albumXml);
		}
		return albumDir;
	}

	private Calendar getCalendar(Object date) {
		if ( date == null ) {
			return null;
		}
		if ( date instanceof Calendar ) {
			return (Calendar)date;
		}
		if ( date instanceof Date ) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date)date);
			return cal;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void exportAlbumItems(Integer albumId, AlbumImportType albumXml, 
			File albumDir) throws JAXBException, IOException {
		List<Map<String, ?>> items = mediaAlbumJdbcTemplate.queryForList(
				ITEMS_FOR_ALBUM_SQL, new Object[] {albumId, ownerId, ownerId});
		for ( Map<String, ?> itemData : items ) {
			ItemImportType itemXml = xmlHelper.getObjectFactory().createItemImportType();
			itemXml.setName((String)itemData.get("name"));
			itemXml.setComment((String)itemData.get("comment"));
			if ( itemData.containsKey("rating") ) {
				Number rating = (Number)itemData.get("rating");
				if ( rating != null ) {
					// divide Media Album ratings by 2 for Matte
					itemXml.setRating(Math.round(rating.floatValue() / 2.0f));
				}
			}
			if ( itemData.containsKey("keywords") ) {
				String keywords = (String)itemData.get("keywords");
				if ( keywords != null ) {
					itemXml.setKeywords(keywords);
				}
			}
			
			String itemPath = (String)itemData.get("path");
			File itemFile = new File(collectionDir, 
					itemData.get("cpath")+itemPath);
			Resource itemResource = new FileSystemResource(new File(albumDir, 
					StringUtils.getFilename(itemPath)));
			
			File outFile = itemResource.getFile();
			if ( itemFile.exists() && (!outFile.exists() 
					|| (outFile.exists() && itemFile.length() != outFile.length()))) {
				if ( log.isDebugEnabled() ) {
					log.debug("Copying file [" +itemFile +"]");
				}
				// copy file
				FileCopyUtils.copy(itemFile, outFile);
			}
			
			String basePath = this.baseDir.getFile().getAbsolutePath();
			String archivePath = itemResource.getFile().getAbsolutePath().substring(
					basePath.length()+1);
			itemXml.setArchivePath(archivePath);
			albumXml.getItem().add(itemXml);
		}
		if ( log.isInfoEnabled() ) {
			log.info("Exported " +items.size() +" items for album ["
					+albumXml.getName() +"]");
		}
	}

	/**
	 * Run tool from command line.
	 * @param args pass the path to the tool's Spring configuration file
	 */
	public static void main(String[] args) {
		String configLocation = args != null && args.length > 0 
			? args[0] : "MediaAlbumExportToolContext.xml";
		ApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
		MediaAlbumExportTool tool = (MediaAlbumExportTool)context.getBean(
				"mediaAlbumExportTool", MediaAlbumExportTool.class);
		tool.run();
	}

	/**
	 * @param collectionDir the collectionDir to set
	 */
	public void setCollectionDir(File collectionDir) {
		this.collectionDir = collectionDir;
	}
	
	/**
	 * @param mediaAlbumJdbcTemplate the mediaAlbumJdbcTemplate to set
	 */
	public void setMediaAlbumJdbcTemplate(JdbcTemplate mediaAlbumJdbcTemplate) {
		this.mediaAlbumJdbcTemplate = mediaAlbumJdbcTemplate;
	}
	
	/**
	 * @param outputDir the outputDir to set
	 */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	
	/**
	 * @param ownerId the ownerId to set
	 */
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	
	/**
	 * @param xmlHelper the xmlHelper to set
	 */
	public void setXmlHelper(XmlHelper xmlHelper) {
		this.xmlHelper = xmlHelper;
	}
	
}
