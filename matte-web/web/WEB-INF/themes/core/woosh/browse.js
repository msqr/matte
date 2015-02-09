function isBehaved(element) {
	return Element.hasClassName(element, 'behave');
}

function makeBehaved(element) {
	Element.addClassName(element, 'behave');
}

function clearChildren(node) {
	if ( !node ) return;
	node = $(node);
	if ( !node || !node.hasChildNodes ) return;
	while ( node.hasChildNodes() ) {
		node.removeChild(node.firstChild);
	}
}

var startDateCal;
var endDateCal;
var wooshBehaviours = {
	'#search-link' : function(el) {
		if ( isBehaved(el) ) return;
		Element.show(el);
		el.onclick = function() {
			Effect.toggle('search-frame', 'appear', {
				afterFinish : function() {
					if ( $('search-form').visible() ) {
						$('search-form').focusFirstElement();
					}
				}
			});
		}
		makeBehaved(el);
	},
	
	'#search-form' : function(el) {
		if ( isBehaved(el) ) return;
		setupSearchForm(el);
		makeBehaved(el);
	},
	
	'#date-range-calendar-toggle' : function(el) {
		if ( isBehaved(el) ) return;
		el.onclick = function() {
			Element.toggle('date-range-calendar-container');
			Element.toggle('date-range-caption');
		}
		makeBehaved(el);
	},
	
	'#start-date-calendar' : function(el) {
		if ( isBehaved(el) ) return;
		var myCalendar = new YAHOO.widget.Calendar('start-date-cal', 
			'start-date-calendar');
		myCalendar.render();
		myCalendar.selectEvent.subscribe(function(evt,args) {
			var date = args[0][0];
			$('search-start-date').value = date[0] +"-" 
				+(date[1] < 10 ? "0"+date[1] : date[1]) +"-" 
				+(date[2] < 10 ? "0"+date[2] : date[2]);
		});
		startDateCal = myCalendar;
		makeBehaved(el);
	},
	
	'#end-date-calendar' : function(el) {
		if ( isBehaved(el) ) return;
		var myCalendar = new YAHOO.widget.Calendar('end-date-cal', 
			'end-date-calendar');
		myCalendar.render();
		myCalendar.selectEvent.subscribe(function(evt,args) {
			var date = args[0][0];
			$('search-end-date').value = date[0] +"-" 
				+(date[1] < 10 ? "0"+date[1] : date[1]) +"-" 
				+(date[2] < 10 ? "0"+date[2] : date[2]);
		});
		endDateCal = myCalendar;
		makeBehaved(el);
	},
	
	'#search-start-date': function(el) {
		if ( isBehaved(el) ) return;
		el.onchange = function() {
			if ( !startDateCal ) return;
			var date = validateDate(el);
			if ( el.value.match(/\S+/) && !date ) {
				alert(i18n['search.date.not.valid']);
				return;
			}
			startDateCal.select(date);
			var theDate = startDateCal.getSelectedDates()[0]; 
			startDateCal.cfg.setProperty("pagedate", 
				(theDate.getMonth()+1) +"/" +theDate.getFullYear());
			startDateCal.render();
		}
		makeBehaved(el);
	},
	
	'#search-end-date': function(el) {
		if ( isBehaved(el) ) return;
		el.onchange = function() {
			if ( !endDateCal ) return;
			var date = validateDate(el);
			if ( el.value.match(/\S+/) && !date ) {
				alert(i18n['search.date.not.valid']);
				return;
			}
			endDateCal.select(date);
			var theDate = endDateCal.getSelectedDates()[0]; 
			endDateCal.cfg.setProperty("pagedate", 
				(theDate.getMonth()+1) +"/" +theDate.getFullYear());
			endDateCal.render();
		}
		makeBehaved(el);
	},
	
	'#browse-mode-link': function(el) {
		if ( isBehaved(el) ) return;
		el = $(el);
		el.show();
		el.onclick = function() {
			$('browse-modes').toggle();
		}
		makeBehaved(el);
	},
	
	'span.browse-mode-link': function(el) {
		if ( isBehaved(el) ) return;
		el = $(el);
		el.onclick = function() {
			var mode = el.id.match(/browsemodelink-(.*)/)[1];
			window.location = webContext +'/browse.do?userKey='
				+userKey +'&mode='+mode;
		}
		makeBehaved(el);
	}
	
}

function validateDate(el) {
	var dateStr = el.value;
	if ( !dateStr.match(/\d+-\d+-\d+/) ) return null;
	var formattedDate = dateStr.replace(/(\d+)-(\d+)-(\d+)/, '$2/$3/$1');
	return formattedDate;
}

function setupSearchForm(form) {
	if ( typeof Builder == 'object' ) {
		Builder.xmlMode = xmlMode;
	}
	form.onsubmit = function() {
		var startDate = $('search-start-date');
		if ( startDate.value.match(/\S+/) && !validateDate(startDate) ) {
			startDate.focus();
			startDate.select();
			alert(i18n['search.date.not.valid']);
			return false;
		}
		var endDate = $('search-end-date');
		if ( endDate.value.match(/\S+/) && !validateDate(endDate) ) {
			endDate.focus();
			endDate.select();
			alert(i18n['search.date.not.valid']);
			return false;
		}
		if ( Element.visible('date-range-calendar-container') ) {
			Element.toggle('date-range-calendar-container');
			Element.toggle('date-range-caption');
		}
		new Ajax.Request(form.action, {
			parameters : Form.serialize(form), 
			onSuccess : function(t) {
				var searchResults = eval(t.responseText);
				handleSearchResults(searchResults);
			}, 
			onFailure : function(t) {
				alert('Error searching items: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	}
}
function handleSearchResults(results) {
	// num matches
	var totalResults = results.searchResults.totalResults;

	var srPane = $('search-results');
	clearChildren(srPane);
	
	var infoNode = Builder.node('div');
	infoNode.appendChild(Builder.node('span',{'class':'label'},'Results: '));
	infoNode.appendChild(document.createTextNode(totalResults));
	srPane.appendChild(infoNode);
	
	setupMediaItemsJson(results.searchResults.items, srPane);
}

function setupMediaItemsJson(itemsArray, pane) {
	// organize search results into albums they appear in
	var albums = $H(new Object());
	var albumNames = $H(new Object());
	for ( var i = 0; i < itemsArray.length; i++ ) {
		var albumKey = itemsArray[i].sharedAlbums[0].anonymousKey;
		if ( !albums[albumKey] ) {
			albums[albumKey] = new Array();
		}
		albums[albumKey].push(itemsArray[i]);
		albumNames[itemsArray[i].sharedAlbums[0].name] = albumKey;
	}
	
	/*var months = $H(new Object());
	for ( var i = 0; i < itemsArray.length; i++ ) {
		var yearMonth = itemsArray[i].itemDate.substring(0,7);
		if ( !months[yearMonth] ) {
			months[yearMonth] = new Array();
		}
		months[yearMonth].push(itemsArray[i]);
	}*/
	
	var globalIdx = 0;
	albumNames.keys().sort().each(function(key) {
		var header = key;
		var albumKey = albumNames[key];
		var monthItems = albums[albumKey];
		var hNode = Builder.node('h2',{},header);
		hNode.onclick = function() {
			window.location = webContext +'/album.do?key='+albumKey;
		}
		pane.appendChild(hNode);
		var iNode = Builder.node('div');
		pane.appendChild(iNode);
		monthItems.each(function(item, idx) {
			var newItem = createNewItemJson(item, globalIdx);
			newItem.onclick = function() {
				window.location = webContext +'/album.do?key='
					+albumKey +'&itemId=' +item.itemId;
			}
			iNode.appendChild(newItem);
			globalIdx++;
		});
	});
	
}

function createNewItemJson(item, position) {
	var itemId = item.itemId;
	
	var newImg = Builder.node('img',{
		'class' : 'thumb',
		//'alt'   : item.name,
		'src'   : webContext +'/media.do?id=' +itemId +'&size=' 
					+thumbSpec.size +'&quality='+thumbSpec.quality,
		'id'    : 'sr-item-'+itemId
		});
	var span = setupThumbnailImgContainer(newImg, position);
	setupThumbnailImgShadow(span, newImg);
	return span;
}

function setupThumbnailImgContainer(img, position) {
	var imgClass = is_safari ? 'thumb' : 'thumb-float';
	var span = Builder.node('div',{
		'class': imgClass,
		'id'   : 'item-position-'+position
		});
	if ( !is_safari ) {
		var height = mediaSizes[thumbSpec.size].height +'px';
		span.style.height = height;
	}
	span.appendChild(img);
	return span;
}

function setupThumbnailImgShadow(span, img) {
	img.onload = function() {
		var dim = Element.getDimensions(img);
		var width = dim.width;
		var height = dim.height;
		if ( width > 0 && height > 0 ) {
			var bgUrl = webContext +'/shadow.do?w=' +width 
				+'&h=' +height +'&b=6&r=3&c=3289650';
			// alert("setting shadow: " +bgUrl);
			span.style.backgroundImage = 'url(' +bgUrl +')';
			span.style.backgroundRepeat = 'no-repeat';
			span.style.backgroundPosition = '-3px -3px';
		}
	};
}

function setShadow(el) {
	var dim = Element.getDimensions(el);
	var width = dim.width;
	var height = dim.height;
	if ( width > 0 && height > 0 ) {
		var bgUrl = webContext+'/shadow.do?w=' +width 
			+'&h=' +height +'&b=6&r=3&c=3289650';
		//alert("setting shadow: " +bgUrl);
		// TODO check size of item for odd images, to adjust x offset
		var xOffset = '-3px';
		if ( Element.hasClassName(el, 'odd') ) {
			xOffset = (262 - width) +'px';
		}
		Element.setStyle(el.parentNode.parentNode, {
			'background-image' : 'url('+bgUrl+')',
			'background-repeat' : 'no-repeat',
			'background-position' : xOffset +' -3px'
		});
	}
}

var agt=navigator.userAgent.toLowerCase();
var is_safari = ((agt.indexOf('safari')!=-1))?true:false;

Behaviour.register(wooshBehaviours);
//Event.observe(window, 'load', init);
