/*jslint browser: true */
/**
 * @require jQuery 2.1
 */

(function() {
'use strict';

var pswp,
	mosaic,
	webContext,
	albumKey;

if ( 'app' in window === false ) {
	window.app = {};
}

function init() {
	webContext = configValue('webContext', '');
	albumKey = configValue('albumKey');
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

function setupMosaic(imageData) {
	if ( Array.isArray(imageData) === false || imageData.length < 1 ) {
		return;
	}
	var singleSpec = configValue('singleSpec', { size: 'NORMAL', quality : 'GOOD' });
	var thumbSpec = configValue('thumbSpec', { size: 'THUMB_NORMAL', quality : 'GOOD' });
	var gridSize = Math.min(12, Math.floor(Math.sqrt(imageData.length)));
	var pswpData = imageData.map(function(d) {
		return {
			src : imageURL(d.id, albumKey, singleSpec.size, singleSpec.quality),
			msrc : imageURL(d.id, albumKey, thumbSpec.size, thumbSpec.quality),
			w : d.w,
			h : d.h
		};
	});
	mosaic = matte.imageMosaic('.mosaic:first')
		.gridColumnCount(gridSize)
		.images(imageData.map(function(d) {
			return imageURL(d.id, albumKey, 'THUMB_BIGGER', 'GOOD');
		}))
		.tileClickHandler(function(event, data) {
			console.log('Clicked on image %d: %s', data.index, data.image.attr('src'));
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
		})
		.render();
}

$(function() {
	init();
	if ( Array.isArray(app.imageData) ) {
		setupMosaic(app.imageData);
	}
});

}());
