/*jslint browser: true */
/**
 * @require jQuery 2.1
 */

(function() {
'use strict';

var pswp,
	mosaic,
	webContext,
	albumKey,
	resizeDelay = 300,
	resizeTimer,
	windowWidth;

if ( 'app' in window === false ) {
	window.app = {};
}

function init() {
	webContext = configValue('webContext', '');
	albumKey = configValue('albumKey');
	windowWidth = $(window).width();
	
	$('a.child-album').on('click', function(event) {
		var key = getChildAlbumKey(this);
		selectChildAlbumLink(key, $(this));
	});
}

function configValue(key, defaultValue) {
	return (app.config && app.config[key] ? app.config[key] : defaultValue);
}

function imageURL(itemId, itemAlbumKey, size, quality) {
	return (webContext +"/media.do?id="+itemId
			+(itemAlbumKey ? "&albumKey=" + encodeURIComponent(itemAlbumKey) : '')
			+(size ? "&size=" + encodeURIComponent(size) : '')
			+(quality ? "&quality=" + encodeURIComponent(quality) : ''));
}

function aspectScaleToMediaSpec(width, height, specName) {
	var specs = configValue('specs'),
		result = { w : width, h : height },
		spec, r1, r2;
	if ( specs && specs[specName] ) {
		spec = specs[specName];
		result.w = spec.width;
		result.h = spec.height;
		r1 = width / height;
		r2 = spec.width / spec.height;
		if ( r1 > r2 ) {
			result.h = Math.min(spec.height, Math.round(spec.width / r1));
		} else {
			result.w = Math.min(spec.width, Math.round(spec.height * r1));
		}
	}
	return result; 
}

/**
 * Convert a MediaItem object into one suitable for passing to PhotoSwipe.
 *
 * @param {Object} item - The MediaItem to convert.
 * @param {String} albumKey - The anonymous album key the image is in.
 * @param {Object} mediaSpec - The media size and quality specification to use.
 */
function getPhotoSwipeItemForMediaItem(item, albumKey, mediaSpec) {
	var dim = aspectScaleToMediaSpec(item.width, item.height, mediaSpec.size);
	var result = {
		src : imageURL(item.itemId, albumKey, mediaSpec.size, mediaSpec.quality),
		//msrc : imageURL(item.itemId, albumKey, thumbSpec.size, thumbSpec.quality),
		w : dim.w,
		h : dim.h
	};
	if ( item.name && item.path.search(new RegExp(item.name + '$')) === -1 ) {
		result.title = item.name;
	}
	if ( item.description && item.description.length > 0 ) {
		if ( result.title ) {
			result.comment = item.description;
		} else {
			result.title = item.description;
		}
	}
	return result;
}

function setupMosaic(imageData) {
	if ( Array.isArray(imageData) === false || imageData.length < 1 ) {
		return;
	}
	var singleSpec = configValue('singleSpec', { size: 'NORMAL', quality : 'GOOD' });
	var thumbSpec = configValue('thumbSpec', { size: 'THUMB_NORMAL', quality : 'GOOD' });
	var gridSize = Math.min(12, Math.floor(Math.sqrt(imageData.length)));
	var pswpData = imageData.map(function(d) {
		return getPhotoSwipeItemForMediaItem(d, albumKey, singleSpec);
	});
	function tileClickHandler(event, data) {
		console.log('Clicked on image %d: %s', data.index, data.image.attr('src'));
	
		// stop flipping
		mosaic.stopEyeCatcher();
	
		var pswpContainer = $('#pswp').get(0);
		var options = {};
		if ( data.index < imageData.length ) {
			options.index = data.index;
			options.getThumbBoundsFn = function(index) {
				var pageYScroll = window.pageYOffset || document.documentElement.scrollTop;
				var img = mosaic.imageElementForIndex(index),
					rect;
				if ( img === undefined ) {
					return undefined;
				}
				rect = img.get(0).getBoundingClientRect();
				return {x:rect.left, y:rect.top + pageYScroll, w:rect.width};
			};
		}
		pswp = new PhotoSwipe(pswpContainer, PhotoSwipeUI_Default, pswpData, options);
		pswp.init();
		pswp.listen('destroy', function() {
			// start flipping again
			mosaic.startEyeCatcher();
		});
		pswp.listen('gettingData', function(index, item) {
			preparePhotoSlide(index, item, imageData);
		});
	}
	if ( mosaic === undefined ) {
		mosaic = matte.imageMosaic('.mosaic:first');
	}
	mosaic.gridColumnCount(gridSize)
		.tileClickHandler(tileClickHandler)
		.images(imageData.map(function(d) {
			return imageURL(d.itemId, albumKey, 'THUMB_BIGGER', 'GOOD');
		}))
		.render()
		.startEyeCatcher();
}

function preparePhotoSlide(index, item, imageData) {
	console.log('Preparing slide %d with %s', index, item);
}

function handleResize() {
	var currWindowWidth = $(window).width();
	resizeTimer = undefined;
	if ( currWindowWidth !== windowWidth ) {
		windowWidth = currWindowWidth;
		displayAppropriateMosaic();
	}
}

function selectChildAlbumLink(key, a) {
	var container;
	$('#album-hierarchy li.selected').removeClass('selected');
	container = a.closest('li');
	if ( a.hasClass('root') === false ) {
		container.addClass('selected');
	}
	function populateAlbumDetails(album) {
		if ( album === undefined ) {
			return;
		}
		if ( container.hasClass('filled') === false 
				&& album.comment !== undefined && album.comment.length > 0 ) {
			container.append($('<p>').text(album.comment)).addClass('filled');
		}
		if ( Array.isArray(album.item) ) {
			setupMosaic(album.item);
		}
	}
	
	if ( a.hasClass('root') ) {
		if ( Array.isArray(app.imageData) ) {
			setupMosaic(app.imageData);
		}
	} else {
		$.getJSON(webContext +'/api/v1/album/' +key).done(function(json) {
			if ( json.success !== true || json.data === undefined ) {
				console.log('Error getting child album %s', key);
				return;
			}
			populateAlbumDetails(json.data);
		});
	}
}

function getChildAlbumKey(location) {
	var key;
	if ( location && location.hash ) {
		key = location.hash.substring(1);
	}
	return key;
}

function getChildAlbumLinkElement(key) {
	var anchor,
		hash = '#' + key;
	$('a.child-album').each(function(idx, a) {
		if ( a.hash === hash ) {
			anchor = $(a);
		}
		return (anchor === undefined);
	});
	return anchor;
}

function displayAppropriateMosaic() {
	var childAlbumKey = getChildAlbumKey(window.location),
		childAlbumLink = getChildAlbumLinkElement(childAlbumKey),
		showAlbumDropDown = (windowWidth < 992);
	if ( childAlbumLink ) {
		selectChildAlbumLink(childAlbumKey, childAlbumLink);
	} else if ( Array.isArray(app.imageData) ) {
		setupMosaic(app.imageData);
	}
	$('#album-hierarchy').toggleClass('dropdown-menu', showAlbumDropDown);
}

$(function() {
	init();
	displayAppropriateMosaic();
	$(window).on('resize', function() {
		// re-calculate mosaic, but only if resize events have died down
		if ( resizeTimer ) {
			clearTimeout(resizeTimer);
		}
		if ( mosaic ) {
			mosaic.stopEyeCatcher();
		}
		resizeTimer = setTimeout(handleResize, resizeDelay);
	});
});

}());
