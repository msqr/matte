/* ===================================================================
 * IndexField.java
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: IndexField.java,v 1.13 2007/06/17 08:16:29 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.lucene;

/**
 * An enumeration of Matte Lucene index fields.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.13 $ $Date: 2007/06/17 08:16:29 $
 */
public enum IndexField {
	
	/** The index field for an item's unique ID. */
	ITEM_ID,
	
	/** The owner of an item. */
	ITEM_OWNER,

	/** The index field for an item's date (search date, i.e. created, modified, etc). */
	ITEM_DATE,
	
	/** The index field for an item's date consisting of only year and month. */
	ITEM_DATE_MONTH,
	
	/** The time zone for the item date. */
	ITEM_DATE_TIME_ZONE,
	
	/** The index field for an item's name. */
	ITEM_NAME,
	
	/** An index key appropriate for this item. */
	ITEM_INDEX_KEY,
	
	/** A shared album key for an item (so can tell if item is shared). */
	MEDIA_SHARED_ALBUM_KEY,
	
	/** A shared album name for an item. */
	MEDIA_SHARED_ALBUM_NAME,
	
	/** A flag indicating the item is part of some shared album. */
	MEDIA_SHARED_FLAG,
	
	/** The index field for an item's creation date. */
	CREATED_DATE,

	/** 
	 * The index field for an item's creation date, consisting 
	 * of only year and month.
	 */
	CREATED_DATE_MONTH,
	
	/** An item's description or comments. */
	DESCRIPTION,
	
	/** An item's modified date. */
	MODIFIED_DATE,
	
	/** The index field (not stored) for tokenized free-form text. */
	GENERAL_TEXT,

	/** The given name of a person (first name). */
	GIVEN_NAME,
	
	/** The height of an item. */
	MEDIA_HEIGHT,
	
	/** The MIME type of an item. */
	MEDIA_MIME,
	
	/** The user media ratings. */
	MEDIA_RATING,
	
	/** The width of an item. */
	MEDIA_WIDTH,

	/** Metadata. */
	METADATA,

	/** The surname of a person (last, family name). */
	SURNAME,

	/** A tag. */
	TAG,

	/** A person's email. */
	EMAIL,

	/** A phone number. */
	PHONE,

	/** An address. */
	ADDRESS,

	/** A city. */
	CITY,

	/** A state or province. */
	STATE,

	/** The postal code of an address. */
	POSTAL_CODE,

	/** A country. */
	COUNTRY,
	
	/** The login name for a user login. */
	USER_LOGIN;

	/**
	 * Get a Lucene field name for this enum.
	 * @return field name
	 * @see #fromFieldName(String)
	 */
	public String getFieldName() {
		switch ( this ) {
			case ADDRESS: return "addr";
			case CITY: return "city";
			case COUNTRY: return "c";
			case CREATED_DATE: return "cdate";
			case CREATED_DATE_MONTH: return "cdatem";
			case DESCRIPTION: return "desc";
			case EMAIL: return "mail";
			case GENERAL_TEXT: return "Gtext";
			case GIVEN_NAME: return "gn";
			case ITEM_DATE: return "date";
			case ITEM_DATE_MONTH: return "datem";
			case ITEM_DATE_TIME_ZONE: return "tz";
			case ITEM_ID: return "id";
			case ITEM_INDEX_KEY: return "idx";
			case ITEM_NAME: return "name";
			case ITEM_OWNER: return "owner";
			case MEDIA_SHARED_ALBUM_KEY: return "msak";
			case MEDIA_SHARED_ALBUM_NAME: return "msan";
			case MEDIA_SHARED_FLAG : return "msf";
			case TAG: return "tag";
			case MEDIA_HEIGHT: return "h";
			case MEDIA_MIME: return "mime";
			case MEDIA_RATING: return "rating";
			case MEDIA_WIDTH: return "w";
			case METADATA: return "meta";
			case MODIFIED_DATE: return "mdate";
			case PHONE: return "phone";
			case POSTAL_CODE: return "zip";
			case SURNAME: return "sn";
			case STATE: return "state";
			case USER_LOGIN: return "login";
		}
		throw new AssertionError(this);
	}
	
	/**
	 * Get an IndexField from a Lucene field name.
	 * @param field field name
	 * @return the IndexField
	 * @throws IllegalArgumentException if the field name is not supported
	 * @see IndexField#getFieldName()
	 */
	public static IndexField fromFieldName(String field) {
		if ( "addr".equals(field) ) return ADDRESS;
		if ( "city".equals(field) ) return CITY;
		if ( "c".equals(field) ) return COUNTRY;
		if ( "cdate".equals(field) ) return CREATED_DATE;
		if ( "cdatem".equals(field) ) return CREATED_DATE_MONTH;
		if ( "date".equals(field) ) return ITEM_DATE;
		if ( "datem".equals(field) ) return ITEM_DATE_MONTH;
		if ( "desc".equals(field) ) return DESCRIPTION;
		if ( "mail".equals(field) ) return EMAIL;
		if ( "Gtext".equals(field) ) return GENERAL_TEXT;
		if ( "gn".equals(field) ) return GIVEN_NAME;
		if ( "h".equals(field) ) return MEDIA_HEIGHT;
		if ( "id".equals(field) ) return ITEM_ID;
		if ( "idx".equals(field) ) return ITEM_INDEX_KEY;
		if ( "login".equals(field) ) return USER_LOGIN;
		if ( "name".equals(field) ) return ITEM_NAME;
		if ( "mdate".equals(field) ) return MODIFIED_DATE;
		if ( "mime".equals(field) ) return MEDIA_MIME;
		if ( "meta".equals(field) ) return METADATA;
		if ( "msak".equals(field) ) return MEDIA_SHARED_ALBUM_KEY;
		if ( "msan".equals(field) ) return MEDIA_SHARED_ALBUM_NAME;
		if ( "msf".equals(field) ) return MEDIA_SHARED_FLAG;
		if ( "owner".equals(field) ) return ITEM_OWNER;
		if ( "phone".equals(field) ) return PHONE;
		if ( "rating".equals(field) ) return MEDIA_RATING;
		if ( "sn".equals(field) ) return SURNAME;
		if ( "state".equals(field) ) return STATE;
		if ( "tag".equals(field) ) return TAG;
		if ( "tz".equals(field) ) return ITEM_DATE_TIME_ZONE;
		if ( "w".equals(field) ) return MEDIA_WIDTH;
		if ( "zip".equals(field) ) return POSTAL_CODE;
		throw new IllegalArgumentException(field);
	}
	
}
