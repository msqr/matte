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
	autoPlay = false,
	autoPlayDelay = 5000,
	autoPlayTimer,
	activeVideo,
	autoPlayAfterVideoPause = false,
	resizeDelay = 300,
	resizeTimer,
	windowWidth,
	beforePhotoSwipeHash,
	backCount = 1;

$.createEventCapturing(['playing', 'pause', 'timeupdate', 'loadedmetadata', 'error']);

if ( 'app' in window === false ) {
	window.app = {};
}

function init() {
	webContext = configValue('webContext', '');
	albumKey = configValue('albumKey');
	windowWidth = $(window).width();
	
	$('a.back').on('click', function(event) {
		if ( window.history && window.history.length >= backCount ) {
			event.preventDefault();
			window.history.go(-backCount);
		}
	});
	
	$('#pswp').on('click', 'button.video-play-button', function() {
		var button = $(this),
			video = button.siblings('video').get(0);
		if ( video ) {
			if ( button.hasClass('transparent') ) {
				video.pause();
			} else {
				console.log('Play video!');
				$('audio, video').not(video).each(function() {
					this.pause();
				});
				video.play();
			}
		}
	});
	$(document).on('playing', function(event) {
		var video = $(event.target),
			button = video.siblings('.video-play-button');
		console.log('Video playing: %s', video.attr('src'));
		activeVideo = event.target;
		autoPlayAfterVideoPause = autoPlay;
		if ( autoPlay ) {
			if ( autoPlayTimer ) {
				clearTimeout(autoPlayTimer);
				autoPlayTimer = undefined;
			}
			autoPlay = false;
		}
		if ( button ) {
			button.addClass('transparent');
		}
	}).on('pause', function(event) {
		var video = $(event.target),
			button = video.siblings('.video-play-button');
		console.log('Video paused: %s', video.attr('src'));
		if ( button ) {
			button.removeClass('transparent');
		}
		if ( autoPlayAfterVideoPause ) {
			autoPlayAfterVideoPause = false;
			autoPlay = true;
			autoSlideshowNext();
		}
		activeVideo = undefined;
	}).on('loadedmetadata', function(event) {
		var video = $(event.target),
			info = video.siblings('.video-info'),
			position = Math.round(event.target.currentTime);
		console.log('Video metadata loaded: %s', video.attr('src'));
		video.data('display-time', position);
		info.find('.video-curr-time').text(formatVideoTime(position))
		info.find('.video-max-time').text(formatVideoTime(Math.round(event.target.duration)));
	}).on('timeupdate', function(event) {
		var video = $(event.target),
			info = video.siblings('.video-info'),
			position = Math.round(event.target.currentTime),
			shownPosition = video.data('display-time');
		if ( position !== shownPosition ) {
			video.data('display-time', position);
			info.find('.video-curr-time').text(formatVideoTime(position));
		}
	}).on('error', function(event) {
		console.log('Error event from a ' +event.target +': ' +(event.target.error ? event.target.error.code : 'N/A'));
	});
	
	$('#play-slideshow').on('click', function(event) {
		event.preventDefault();
		launchPhotoSwipe(0);
		startSlideshow();
	});
	
	$('#slideshow-toggle-button').on('click', function(event) {
		event.preventDefault();
		if ( autoPlay ) {
			stopSlideshow();
		} else {
			startSlideshow();
		}
	});
	
	$(document).on('keydown', handleKeyDown);
}

function formatVideoTime(totalSeconds) {
	var hours, minutes, seconds, result;
	if ( isNaN(totalSeconds) ) {
		return 'N/A';
	}
	hours = parseInt(totalSeconds / 3600 ) % 24;
	minutes = parseInt(totalSeconds / 60 ) % 60;
	seconds = totalSeconds % 60;
	result = '';
	if ( hours > 0 ) {
		result += String(hours) + ':';
	}
	result += (minutes < 10 && hours > 0 ? '0' + minutes : minutes) + ':' + (seconds  < 10 ? '0' + seconds : seconds);
	return result;
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
		url : url, // preserve URL for sildes we convert to html later on
		mime : item.mime
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
	
	if ( item.mime.match(/^video/i) ) { 
		delete result.src;
		result.html = 
				'<div class="video-slide"><video></video>'
				+'<button type="button" class="btn btn-default video-play-button"><span class="glyphicon glyphicon-play play-button"></span></button>'
				+'<div class="video-info"><span class="video-curr-time">0:00</span> / <span class="video-max-time">0:00</span></div>'
				+'</div>';
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

	// stop flipping
	mosaic.stopEyeCatcher();
	
	// remember current hash
	beforePhotoSwipeHash = window.location.hash;

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
	pswp.listen('gettingData', function(index, item) {
		preparePhotoSlide(index, item, albumImageData);
	});
	pswp.listen('destroy', handlePhotoSwipeDestroy);
	pswp.listen('beforeChange', handlePhotoSwipeBeforeChange);
	pswp.listen('afterChange', handlePhotoSwipeAfterChange);
	pswp.listen('imageLoadComplete', handlePhotoSwipeImageLoadComplete);
	pswp.init();
}

function setupMosaic(imageData) {
	if ( Array.isArray(imageData) === false || imageData.length < 1 ) {
		return;
	}
	var gridSize = Math.min(12, Math.floor(Math.sqrt(imageData.length)));
	
	albumImageData = imageData;
	
	function tileClickHandler(event, data) {
		console.log('Clicked on image %d: %s', data.index, data.image.attr('src'));
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
		.render();
	if ( pswp === undefined ) {
		mosaic.startEyeCatcher();
	}
}

function handlePhotoSwipeDestroy() {
	// start flipping again
	mosaic.startEyeCatcher();
	$('audio, video').each(function() {
		this.pause();
	});
	stopSlideshow();
	pswp = undefined;
}

function handlePhotoSwipeBeforeChange() {
	var item = pswp.currItem,
		index = pswp.getCurrentIndex(),
		downloadLink = $('li.item-action-download a'),
		video = $(item.container).find('video'),
		videoURL;
	$('audio, video').not(video).each(function() {
		this.pause();
	});
	downloadLink.attr('href', item.url + '&download=true');
	if ( albumImageData[index].mime.match(/^video/i) ) {
		downloadLink.hide();
	} else {
		downloadLink.show();
	}
	$('li.item-action-download-original a').attr('href', item.url + '&download=true&original=true');
	if ( video.length > 0 ) {
		videoURL = item.url+'&original=true';
		if ( video.attr('src') !== videoURL ) {
			console.log('Changing video %d source from %s to %s', index, video.attr('src'), videoURL);
			video.attr('src',  videoURL);
		}
	}
}

function handlePhotoSwipeAfterChange() {
	var index = pswp.getCurrentIndex(),
		item = pswp.currItem;
	setupAutoSlideshowNext();
}

function preparePhotoSlide(index, item) {
	console.log('Preparing slide %d', index);
}

function handlePhotoSwipeImageLoadComplete(index) {
	console.log('Slide %d image loaded', index);
	setupAutoSlideshowNext();
}

function setupAutoSlideshowNext() {
	var index, item, video;
	if ( pswp === undefined ) {
		return;
	}
	index = pswp.getCurrentIndex();
	item = pswp.currItem;
	if ( autoPlay && autoPlayTimer === undefined && (item.html !== undefined || item.loaded) ) {
		if ( item.html ) {
			video = $(item.container).find('video'); 
		}
		if ( video && video.length > 0 ) {
			video.get(0).play();
			// FIXME: why does Safari sometimes not play the video, or report an error when it doesn't?
		} else {
			autoPlayTimer = setTimeout(autoSlideshowNext, autoPlayDelay);
		}
	}
}

function startSlideshow() {
	var btn = $('#slideshow-toggle-button');
	autoPlay = true;
	btn.attr('title', btn.data('title-pause'));
	btn.find('.glyphicon')
		.toggleClass('glyphicon-play', false)
		.toggleClass('glyphicon-pause', true);
	setupAutoSlideshowNext();
}

function stopSlideshow() {
	var btn = $('#slideshow-toggle-button');
	autoPlay = false;
	btn.attr('title', btn.data('title-play'));
	btn.find('.glyphicon')
		.toggleClass('glyphicon-play', true)
		.toggleClass('glyphicon-pause', false);
	if ( autoPlayTimer ) {
		clearTimeout(autoPlayTimer);
		autoPlayTimer = undefined;
	}
}

function autoSlideshowNext() {
	if ( autoPlay && pswp ) {
		if ( autoPlayTimer ) {
			clearTimeout(autoPlayTimer);
			autoPlayTimer = undefined;
		}
		pswp.next();
	}
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
	} else {
		$('#album-hierarchy li.selected').removeClass('selected');
		if ( Array.isArray(app.imageData) ) {
			setupAlbum(app.albumData);
			setupMosaic(app.imageData);
		}
	}
	$('#album-hierarchy').toggleClass('dropdown-menu', showAlbumDropDown);
}

function handleKeyDown(event) {
	var letter = String.fromCharCode(event.keyCode).toLowerCase(),
		video;
	console.log('Keydown: %d [%s]', event.keyCode, letter);
	if ( letter === ' ' ) {
		if ( pswp ) {
			if ( pswp.currItem.html ) {
				video = $(pswp.currItem.html).find('video');
			}
			if ( activeVideo ) {
				console.log('Pausing active video from space key tap');
				activeVideo.pause();
			} else if ( video !== undefined && video.length > 0 ) {
				console.log('Starting video from space key tap');
				video.get(0).play();
			} else if ( autoPlay ) {
				stopSlideshow();
			} else {
				startSlideshow();
			}
			event.preventDefault();
		}
	}
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

$(window).on('hashchange', function(event) {
	if ( beforePhotoSwipeHash !== undefined && beforePhotoSwipeHash === window.location.hash ) {
		beforePhotoSwipeHash = undefined;
	} else if ( pswp === undefined ) {
		displayAppropriateMosaic();
		backCount += 1;
	}
});

}());
