/* ===================================================================
 * HibernateMediaItemDao.java
 * 
 * Created Sep 19, 2005 7:19:59 PM
 * 
 * Copyright (c) 2005 Matt Magoffin.
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
 * $Id: HibernateMediaItemDao.java,v 1.20 2007/05/17 01:02:05 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.hbm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import magoffin.matt.dao.BasicBatchResult;
import magoffin.matt.dao.hbm.CriteriaBuilder;
import magoffin.matt.dao.hbm.GenericHibernateDao;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * Hibernate implementation of {@link magoffin.matt.ma2.dao.MediaItemDao}.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.20 $ $Date: 2007/05/17 01:02:05 $
 */
public class HibernateMediaItemDao extends GenericHibernateDao<MediaItem,Long> 
implements MediaItemDao {

	/** Find all MediaItems for a path. */
	public static final String QUERY_MEDIA_ITEMS_FOR_PATH = "MediaItemsForPath";
	
	/** Find all MediaItems for a Collection. */
	public static final String QUERY_MEDIA_ITEMS_FOR_COLLECTION = "MediaItemsForCollection";

	/** Find all MediaItems for a Album. */
	public static final String QUERY_MEDIA_ITEMS_FOR_ALBUM = "MediaItemsForAlbum";

	/** Find all MediaItems for a list of IDs. */
	public static final String QUERY_MEDIA_ITEMS_FOR_IDS = "MediaItemsForIds";

	/** Find all MediaItems and Albums for a Collection. */
	public static final String QUERY_MEDIA_ITEMS_OF_COLLECTION_FOR_ALBUM 
		= "MediaItemsAndAlbumsForCollection";

	private String removeItemsFromAlbumsQuery = 
		"select MediaItem, Album from magoffin.matt.ma2.domain.MediaItem MediaItem, "
				+"magoffin.matt.ma2.domain.Album Album "
			+"where MediaItem in elements(Album.Item) "
			+"and MediaItem.id in ({0})";
	
	private String removeItemsFromCollectionsQuery = 
		"select MediaItem, Collection from magoffin.matt.ma2.domain.MediaItem MediaItem, "
				+"magoffin.matt.ma2.domain.Collection Collection "
			+"where MediaItem in elements(Collection.Item) "
			+"and MediaItem.id in ({0})";
	
	/**
	 * Default constructor.
	 */
	public HibernateMediaItemDao() {
		super(MediaItem.class);
	}

	@Override
	protected Long getPrimaryKey(MediaItem domainObject) {
		if ( domainObject == null ) return null;
		return domainObject.getItemId();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.MediaItemDao#findItemsForAlbum(java.lang.Long)
	 */
	public List<MediaItem> findItemsForAlbum(Long albumId) {
		return findByNamedQuery(QUERY_MEDIA_ITEMS_FOR_ALBUM, new Object[]{albumId});
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.MediaItemDao#findItemsForCollection(java.lang.Long)
	 */
	public List<MediaItem> findItemsForCollection(Long collectionId) {
		return findByNamedQuery(QUERY_MEDIA_ITEMS_FOR_COLLECTION, new Object[]{collectionId});
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.MediaItemDao#getItemForPath(java.lang.Long, java.lang.String)
	 */
	public MediaItem getItemForPath(Long collectionId, String path) {
		List<MediaItem> results = findByNamedQuery(QUERY_MEDIA_ITEMS_FOR_PATH, 
				new Object[]{collectionId,path});
		if ( results.size() < 1 ) return null;
		return results.get(0);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.MediaItemDao#getMediaItemWithInfo(java.lang.Long)
	 */
	public MediaItem getMediaItemWithInfo(Long itemId) {
		MediaItem item = get(itemId);
		if ( item != null ) {
			fillInMediaItem(item);
		}
		return item;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.MediaItemDao#removeItemsFromAlbums(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public List<MediaItem> removeItemsFromAlbums(Long[] itemIds) {
		MessageFormat hqlTemplate = new MessageFormat(removeItemsFromAlbumsQuery);
		StringBuilder buf = new StringBuilder();
		for ( int i = 0; i < itemIds.length; i++ ) {
			if ( i > 0 ) {
				buf.append(',');
			}
			buf.append(itemIds[i]);
		}
		String hql = hqlTemplate.format(new String[] {buf.toString()});
		List<Object[]> queryResults = getHibernateTemplate().find(hql);
		return removeItemsFromAlbums(queryResults);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.MediaItemDao#removeItemsFromCollections(java.lang.Long[])
	 */
	@SuppressWarnings("unchecked")
	public List<MediaItem> removeItemsFromCollections(Long[] itemIds) {
		MessageFormat hqlTemplate = new MessageFormat(removeItemsFromCollectionsQuery);
		StringBuilder buf = new StringBuilder();
		for ( int i = 0; i < itemIds.length; i++ ) {
			if ( i > 0 ) {
				buf.append(',');
			}
			buf.append(itemIds[i]);
		}
		String hql = hqlTemplate.format(new String[] {buf.toString()});
		List<Object[]> queryResults = getHibernateTemplate().find(hql);
		return removeItemsFromCollections(queryResults);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.MediaItemDao#removeItemsOfCollectionFromAlbums(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public List<MediaItem> removeItemsOfCollectionFromAlbums(Long collectionId) {
		List<Object[]> queryResults = getHibernateTemplate().findByNamedQuery(
				QUERY_MEDIA_ITEMS_OF_COLLECTION_FOR_ALBUM, collectionId);
		return removeItemsFromAlbums(queryResults);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.dao.BatchableDao#batchProcess(magoffin.matt.dao.BatchableDao.BatchCallback, magoffin.matt.dao.BatchableDao.BatchOptions)
	 */
	public BatchResult batchProcess(BatchCallback<MediaItem> callback, BatchOptions options) {
		if ( callback == null || options == null ) {
			throw new IllegalArgumentException("Batch parameters and options are required");
		}
		BatchResult result = null;
		if ( BATCH_NAME_PROCESS_MEDIA_IDS.equals(options.getName()) ) {
			result = handleBatchWithIds(callback, options);
		} else if ( BATCH_NAME_INDEX.equals(options.getName()) ) {
			result = handleBatchIndex(callback, options);
		}
		return result;
	}

	private BatchResult handleBatchIndex(BatchCallback<MediaItem> callback,
			BatchOptions batchOptions) {
		Integer numProcessed = executeLiveCriteriaBatchCallback(
				new CriteriaBuilder() {
					public void buildCriteria(Criteria criteria) {
						criteria.addOrder(Order.asc("id"));
					}
				}, callback, batchOptions);
		return new BasicBatchResult(numProcessed);
	}

	@SuppressWarnings("unchecked")
	private BatchResult handleBatchWithIds(final BatchCallback<MediaItem> callback, 
			BatchOptions batchOptions) {
		Object o = batchOptions.getParameters().get(BATCH_PROCESS_PARAM_MEDIA_IDS_LIST);
		final List<Long> itemIdList = new LinkedList<Long>();
		if ( o instanceof Long[] ) {
			Long[] list = (Long[])o;
			for ( Long id : list ) {
				itemIdList.add(id);
			}
		} else if ( o instanceof java.util.Collection ) {
			java.util.Collection<Long> col = (java.util.Collection<Long>)o;
			itemIdList.addAll(col);
		} else {
			throw new UnsupportedOperationException(
					"Must provide Long[] or Collection<Long> as parameter " 
					+BATCH_PROCESS_PARAM_MEDIA_IDS_LIST);
		}
		
		Integer numProcessed = executeLiveCriteriaBatchCallback(
				new CriteriaBuilder() {
					public void buildCriteria(Criteria criteria) {
						criteria.add(Restrictions.in("id", itemIdList));
					}
				}, callback, batchOptions);
		return new BasicBatchResult(numProcessed);
	}

	private void fillInMediaItem(MediaItem item) {
		// FIXME why does this not work: getHibernateTemplate().initialize(a.getItem());
		item.getMetadata().size();
		item.getUserComment().size();
		item.getUserRating().size();
		item.getUserTag().size();
	}

	@SuppressWarnings("unchecked")
	private List<MediaItem> removeItemsFromAlbums(List<Object[]> queryResults) {
		Map<Long, MediaItem> removedItems = new LinkedHashMap<Long, MediaItem>();
		for ( Object[] tuple : queryResults ) {
			MediaItem item = (MediaItem)tuple[0];
			Album album = (Album)tuple[1];
			if ( album.getPoster() != null && item.getItemId().equals(album.getPoster().getItemId()) ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Removing item " +item.getItemId() +" as album poster "
							+album.getAlbumId());
				}
				album.setPoster(null);
			}
			for ( ListIterator<MediaItem> itr = album.getItem().listIterator(); itr.hasNext(); ) {
				MediaItem albumItem = itr.next();
				if ( item.getItemId().equals(albumItem.getItemId()) ) {
					if ( log.isDebugEnabled() ) {
						log.debug("Removing item " +item.getItemId() +" from album "
								+album.getAlbumId());
					}
					removedItems.put(item.getItemId(),item);
					itr.remove();
					break;
				}
			}
		}
		List<MediaItem> results = new ArrayList<MediaItem>(removedItems.size());
		results.addAll(removedItems.values());
		return results;
	}
	
	@SuppressWarnings("unchecked")
	private List<MediaItem> removeItemsFromCollections(List<Object[]> queryResults) {
		Map<Long, MediaItem> removedItems = new LinkedHashMap<Long, MediaItem>();
		for ( Object[] tuple : queryResults ) {
			MediaItem item = (MediaItem)tuple[0];
			Collection collection = (Collection)tuple[1];
			for ( ListIterator<MediaItem> itr = collection.getItem().listIterator(); itr.hasNext(); ) {
				MediaItem collectionItem = itr.next();
				if ( item.getItemId().equals(collectionItem.getItemId()) ) {
					if ( log.isDebugEnabled() ) {
						log.debug("Removing item " +item.getItemId() +" from collection "
								+collection.getCollectionId());
					}
					removedItems.put(item.getItemId(),item);
					itr.remove();
					break;
				}
			}
		}
		List<MediaItem> results = new ArrayList<MediaItem>(removedItems.size());
		results.addAll(removedItems.values());
		return results;
	}
	
	/**
	 * @return Returns the removeItemsFromAlbumsQuery.
	 */
	public String getRemoveItemsFromAlbumsQuery() {
		return removeItemsFromAlbumsQuery;
	}

	/**
	 * @param removeItemsFromAlbumsQuery The removeItemsFromAlbumsQuery to set.
	 */
	public void setRemoveItemsFromAlbumsQuery(String removeItemsFromAlbumsQuery) {
		this.removeItemsFromAlbumsQuery = removeItemsFromAlbumsQuery;
	}
	
	/**
	 * @return the removeItemsFromCollectionsQuery
	 */
	protected String getRemoveItemsFromCollectionsQuery() {
		return removeItemsFromCollectionsQuery;
	}
	
	/**
	 * @param removeItemsFromCollectionsQuery the removeItemsFromCollectionsQuery to set
	 */
	protected void setRemoveItemsFromCollectionsQuery(
			String removeItemsFromCollectionsQuery) {
		this.removeItemsFromCollectionsQuery = removeItemsFromCollectionsQuery;
	}
	
}
