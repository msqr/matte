/*jslint browser: true */
/**
 * @require jQuery 2.1
 */

(function() {
'use strict';

var pswp,
	albumImageData,
	mosaic,
	webContext,
	albumKey,
	resizeDelay = 300,
	resizeTimer,
	windowWidth;

$.createEventCapturing(['playing', 'pause']);

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
	
	$('#pswp').on('click', 'button.video-play-button', function() {
		var button = $(this),
			video = button.siblings('video').get(0);
		if ( video ) {
			console.log('Play video!');
			$('audio, video').not(video).each(function() {
        		this.pause();
    		});
			video.play();
		}
	}).on('playing', function(event) {
		var video = $(event.target),
			button = video.siblings('.video-play-button');
		console.log('Video playing: %s', video.attr('src'));
		if ( button ) {
			button.hide();
		}
	}).on('pause', function(event) {
		var video = $(event.target),
			button = video.siblings('.video-play-button');
		console.log('Video paused: %s', video.attr('src'));
		if ( button ) {
			button.show();
		}
	});
	
	$('#play-slideshow').on('click', function(event) {
		event.preventDefault();
		startSlideshow();
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
	var dim = aspectScaleToMediaSpec(item.width, item.height, mediaSpec.size),
		url = imageURL(item.itemId, albumKey, mediaSpec.size, mediaSpec.quality)
	var result = {
		src : url,
		//msrc : imageURL(item.itemId, albumKey, thumbSpec.size, thumbSpec.quality),
		w : dim.w,
		h : dim.h,
		url : url // preserve URL for sildes we convert to html later on
	};
	
	// set the title to the item name, but only if the item name isn't set to the item's file name
	if ( item.name && item.path.search(new RegExp(item.name + '(\\.\\w+)?$')) === -1 ) {
		result.title = item.name;
	}
	
	// if an item description is available, use that as the title unless a title already configured
	// in which case add a custom 'comment' property with the description
	if ( item.description && item.description.length > 0 ) {
		if ( result.title ) {
			result.comment = item.description;
		} else {
			result.title = item.description;
		}
	}

	// handle video items
	if ( item.mime.match(/^video/i) ) {
		var container = $('<div class="video-slide"/>');
		$('<video/>').attr('src', result.src+'&original=true').appendTo(container);
		$('<button type="button" class="btn btn-default video-play-button"><span class="glyphicon glyphicon-play play-button"></span></button>')
			.appendTo(container);
		result.html = container.get(0);
		delete result.src;
	}
	
	return result;
}

function launchPhotoSwipe(startIndex) {
	if ( Array.isArray(albumImageData) == false ) {
		return;
	}
	var singleSpec = configValue('singleSpec', { size: 'NORMAL', quality : 'GOOD' });
	var pswpContainer = $('#pswp').get(0);
	var pswpData = albumImageData.map(function(d) {
		return getPhotoSwipeItemForMediaItem(d, albumKey, singleSpec);
	});
	var options = {};
	if ( startIndex < albumImageData.length ) {
		options.index = startIndex;
		if ( mosaic !== undefined ) {
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
	}
	if ( options.isClickableElement === undefined ) {
		options.isClickableElement = function(el) {
			return (el.tagName === 'A' || el.tagName === 'BUTTON');
		};
	}
	pswp = new PhotoSwipe(pswpContainer, PhotoSwipeUI_Default, pswpData, options);
	pswp.init();
	pswp.listen('gettingData', function(index, item) {
		preparePhotoSlide(index, item, albumImageData);
	});
	pswp.listen('destroy', handlePhotoSwipeDestroy);
	pswp.listen('beforeChange', handlePhotoSwipeBeforeChange);
	handlePhotoSwipeBeforeChange(); // PhotoSwipe does not call this for initial image
}

function setupMosaic(imageData) {
	if ( Array.isArray(imageData) === false || imageData.length < 1 ) {
		return;
	}
	var gridSize = Math.min(12, Math.floor(Math.sqrt(imageData.length)));
	
	albumImageData = imageData;
	
	function tileClickHandler(event, data) {
		console.log('Clicked on image %d: %s', data.index, data.image.attr('src'));
	
		// stop flipping
		mosaic.stopEyeCatcher();
	
		launchPhotoSwipe(data.index);
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

function handlePhotoSwipeDestroy() {
	// start flipping again
	mosaic.startEyeCatcher();
	$('audio, video').each(function() {
        this.pause();
    });
}

function handlePhotoSwipeBeforeChange() {
	var item = pswp.currItem;
	$('audio, video').each(function() {
        this.pause();
    });
    $('li.item-action-download a').attr('href', item.url + '&download=true');
    $('li.item-action-download-original a').attr('href', item.url + '&download=true&original=true');
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

function setupAlbum(album) {
	if ( album && album.allowOriginal ) {
		$('li.item-action-download-original').show();
	} else {
		$('li.item-action-download-original').hide();
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
		setupAlbum(album);
		if ( container.hasClass('filled') === false 
				&& album.comment !== undefined && album.comment.length > 0 ) {
			container.append($('<p>').text(album.comment)).addClass('filled');
		}
		if ( Array.isArray(album.item) ) {
			setupMosaic(album.item);
		}
	}
	
	if ( a.hasClass('root') ) {
		setupAlbum(app.albumData);
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
		setupAlbum(app.albumData);
		setupMosaic(app.imageData);
	}
	$('#album-hierarchy').toggleClass('dropdown-menu', showAlbumDropDown);
}

function startSlideshow() {
	launchPhotoSwipe(0);
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
