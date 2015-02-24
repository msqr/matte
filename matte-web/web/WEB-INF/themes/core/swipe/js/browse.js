/*jslint browser: true */
/**
 * @require jQuery 2.1
 */

(function() {
'use strict';

var webContext,
	userKey,
	thumbSpec;

if ( 'app' in window === false ) {
	window.app = {};
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

function init() {
	webContext = configValue('webContext', '');
	userKey = configValue('userKey', '');
	thumbSpec = configValue('thumbSpec', { size: 'THUMB_NORMAL', quality : 'GOOD' });
	
	$('a.download-album').on('click', function(event) {
		var form = $('#album-download-modal'),
			link = $(this),
			albumKey = link.data('album-key');
		event.preventDefault();
		form.find('input[name=albumKey]').val(albumKey);
		form.modal('show');
	});
	
	$('#album-download-modal input[name=original]').on('change', function() {
		var selects = $(this.form).find('select');
		selects.prop('disabled', this.checked);
	});
	
	$('#search-form').on('submit', function(event) {
		event.preventDefault();
		executeSearch(this);
		return false;
	});
	
	// when running as webapp, don't kick out to Safari for internal links
	(function() {
		var stop = /^(a|html)$/i,
			relative = /^[a-z\+\.\-]+:/i;
		if ( ('standalone' in window.navigator) && window.navigator.standalone ) {
			$(document).on('click', 'a', function(event) {
				var curnode = event.target, 
					location = document.location, 
					dest;
				while ( !(stop).test(curnode.nodeName) ) {
					curnode = curnode.parentNode;
				}
				dest = curnode.href;
				if ( 'href' in curnode 
						&& event.originalEvent.defaultPrevented !== true
						&& dest.replace(location.href,'').indexOf('#') !== 0
						&& (relative.test(dest) === false || dest.indexOf(location.protocol+'//'+location.host) === 0) ) {
					event.preventDefault();
					location.href = dest;
				}
			});
		}
	}());
	
	gotoLocationHash(window.location);
}

function executeSearch(form) {
	var query = form.elements['query'],
		url;
	if ( query === undefined || query.value.length < 1 ) {
		return false;
	}
	window.location.hash = '&query=' + encodeURIComponent(query.value);
	url = webContext +'/api/v1/media/search/' +encodeURIComponent(userKey) 
		+'?query=' +encodeURIComponent(query.value);
	$.getJSON(url).done(function(json) {
		if ( json.success !== true || json.data === undefined ) {
			console.log('Error searching', key);
			return;
		}
		populateSearchResults(json.data);
	});
}

function gotoLocationHash(location) {
	var hash = location.hash,
		pairs;
	if ( hash && hash.length < 2 ) {
		return;
	}
	pairs = hash.substring(1).split('&');
	pairs.some(function(pair) {
		var kv = pair.split('=', 2);
		var searchForm;
		if ( kv.length !== 2 ) {
			return false;
		}
		if ( kv[0] === 'query' ) {
			searchForm = $('#search-form').get(0);
			searchForm.elements['query'].value = kv[1];
			executeSearch(searchForm);
			return true;
		}
		return false;
	});
}

function populateSearchResults(results) {
	var container = $('#search-results'),
		resultsContainer = $('#search-results-container'),
		noneContainer = $('#search-results-none');
	if ( results === undefined || Array.isArray(results.item) === false || results.item.length < 1 ) {
		noneContainer.show();
		container.hide();
		return;
	}
	noneContainer.hide();
	
	// TODO: populate search header, e.g. X results...
	
	resultsContainer.empty();
	results.item.forEach(function(item, idx) {
		var result = $('<a/>'),
			albumKey;
		if ( Array.isArray(item.sharedAlbum) === false || item.sharedAlbum.length < 1 ) {
			console.log('Search result has no shared album: %s', item);
			return;
		}
		albumKey = item.sharedAlbum[0].anonymousKey;
		result
			.attr('href', (webContext + '/album.do?key=' +encodeURIComponent(albumKey)));
		$('<img/>')
			.attr('src', imageURL(item.itemId, albumKey, thumbSpec.size, thumbSpec.quality))
			.appendTo(result);
		
		resultsContainer.append(result);
	});
	
	container.show();
}

$(init);

}());
