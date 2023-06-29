/* ===================================================================
 * SmetaMediaMetadata.java
 * 
 * Created Jan 21, 2007 3:47:12 PM
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
 */

package magoffin.matt.ma2.support;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import magoffin.matt.ma2.MediaMetadata;
import magoffin.matt.ma2.image.EmbeddedImageMetadata;
import magoffin.matt.meta.MetadataImage;
import magoffin.matt.meta.MetadataNotSupportedException;
import magoffin.matt.meta.MetadataResource;
import magoffin.matt.meta.MetadataResourceFactory;
import magoffin.matt.meta.MetadataResourceFactoryManager;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * General purpose {@link MediaMetadata} using sMeta.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class SmetaMediaMetadata extends AbstractMediaMetadata 
implements MediaMetadata, EmbeddedImageMetadata {
	
	private static final Logger LOG = Logger.getLogger(SmetaMediaMetadata.class);
	
	private MetadataResourceFactoryManager factoryManager
		= MetadataResourceFactoryManager.getDefaultManagerInstance();
	private String creationDateKey = "DATE_TAKEN";
	private String embeddedImageKey = null;
	private MetadataResource metaResource = null;
	private Set<Class<?>> treatAsStrings = new LinkedHashSet<Class<?>>(
			Arrays.asList(new Class<?>[] {
			String.class, Integer.class, Long.class, Float.class,
			Double.class
			}));
	private Locale locale = Locale.getDefault();
	
	/**
	 * Default consstructor.
	 */
	public SmetaMediaMetadata() {
		super();
	}
	
	/**
	 * Construct with a specific locale.
	 * @param locale the Locale to use
	 */
	public SmetaMediaMetadata(Locale locale) {
		this.locale = locale;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.EmbeddedImageMetadata#hasEmbeddedImage()
	 */
	public boolean hasEmbeddedImage() {
		return findMetadataImage() != null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaMetadata#setMediaResource(org.springframework.core.io.Resource)
	 */
	public MediaMetadata setMediaResource(Resource resource) {
		try {
			File mediaFile = resource.getFile();
			MetadataResourceFactory resourceFactory 
				= factoryManager.getMetadataResourceFactory(mediaFile);
			if ( resourceFactory == null ) {
				return this;
			}
			metaResource 
				= resourceFactory.getMetadataResourceInstance(mediaFile);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} catch ( MetadataNotSupportedException e ) {
			String fileName = resource.getFilename();
			try {
				fileName = resource.getFile().getAbsolutePath();
			} catch ( IOException ioe ) {
				// ignore this
			}
			LOG.debug("Smeta metadata not supported for [" +fileName +"]", e);
			return this;
		}
		
		// parse out all primitive types and creation date, if possible
		for ( String key : metaResource.getParsedKeys() ) {
			Object value = metaResource.getValue(key, this.locale);
			if ( creationDateKey != null && creationDateKey.equals(key) 
					&& (value instanceof Date) ) {
				setCreationDate((Date)value);
				continue;
			}
			if ( treatAsStrings.contains(value.getClass()) ) {
				addToMap(key, value.toString());
				continue;
			}
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Ignoring metadata key [" +key +"]; value ["
						+value +"]");
			}
		}
		
		return this;
	}
	
	public int getEmbeddedImageHeight() {
		MetadataImage metaImage = findMetadataImage();
		if ( metaImage == null ) {
			return -1;
		}
		return metaImage.getAsBufferedImage().getHeight();
	}

	public int getEmbeddedImageWidth() {
		MetadataImage metaImage = findMetadataImage();
		if ( metaImage == null ) {
			return -1;
		}
		return metaImage.getAsBufferedImage().getWidth();
	}

	public Resource getEmbeddedImageResource() {
		MetadataImage metaImage = findMetadataImage();
		if ( metaImage == null ) {
			return null;
		}
		
		File tmpFile = null;
		OutputStream out = null;
		try {
			// write to a temp file
			tmpFile = File.createTempFile("SmetaMediaMetadata-image-", ".dat");
			tmpFile.deleteOnExit();
			out = new BufferedOutputStream(new FileOutputStream(tmpFile));
			metaImage.writeToStream(out);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} finally {
			if ( out != null ) {
				try {
					out.flush();
					out.close();
				} catch ( IOException e ) {
					LOG.warn("IOException closing image stream: " +e);
				}
			}
		}
		
		// return file resource
		return new FileSystemResource(tmpFile);
	}

	public String getEmbeddedImageMimeType() {
		MetadataImage metaImage = findMetadataImage();
		if ( metaImage == null ) {
			return null;
		}
		return metaImage.getMimeType();
	}

	public BufferedImage getEmbeddedImage() {
		MetadataImage metaImage = findMetadataImage();
		if ( metaImage == null ) {
			return null;
		}
		
		try {
			return metaImage.getAsBufferedImage();
		} catch ( UnsupportedOperationException e ) {
			LOG.warn("MetadataImage found, but getAsBufferedImage() threw UnsupportedOperationException: "
					+e);
			return null;
		}
	}

	private MetadataImage findMetadataImage() {
		if ( metaResource == null ) {
			return null;
		}
		MetadataImage metaImage = null;
		if ( embeddedImageKey != null ) {
			Object o = metaResource.getValue(embeddedImageKey, Locale.getDefault());
			if ( o instanceof MetadataImage ) {
				metaImage = (MetadataImage)o;
			}
		} else {
			// search for image
			for ( String key : metaResource.getParsedKeys() ) {
				Object o = metaResource.getValue(key, Locale.getDefault());
				if ( o instanceof MetadataImage ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Found MetadataImage at key [" +key +"]");
					}
					metaImage = (MetadataImage)o;
					break;
				}
			}
		}
		if ( metaImage == null ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("No MetadataImage found in MetadataResource ["
						+metaResource +"]");
			}
		}
		return metaImage;
	}

	/**
	 * @return the factoryManager
	 */
	public MetadataResourceFactoryManager getFactoryManager() {
		return factoryManager;
	}
	
	/**
	 * @param factoryManager the factoryManager to set
	 */
	public void setFactoryManager(MetadataResourceFactoryManager factoryManager) {
		this.factoryManager = factoryManager;
	}
	
	/**
	 * @return the creationDateKey
	 */
	public String getCreationDateKey() {
		return creationDateKey;
	}
	
	/**
	 * @param creationDateKey the creationDateKey to set
	 */
	public void setCreationDateKey(String creationDateKey) {
		this.creationDateKey = creationDateKey;
	}
	
	/**
	 * @return the treatAsStrings
	 */
	public Set<Class<?>> getTreatAsStrings() {
		return treatAsStrings;
	}
	
	/**
	 * @param treatAsStrings the treatAsStrings to set
	 */
	public void setTreatAsStrings(Set<Class<?>> treatAsStrings) {
		this.treatAsStrings = treatAsStrings;
	}
	
	/**
	 * @return the embeddedImageKey
	 */
	public String getEmbeddedImageKey() {
		return embeddedImageKey;
	}
	
	/**
	 * @param embeddedImageKey the embeddedImageKey to set
	 */
	public void setEmbeddedImageKey(String embeddedImageKey) {
		this.embeddedImageKey = embeddedImageKey;
	}

}
