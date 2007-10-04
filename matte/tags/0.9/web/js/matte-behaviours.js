var startDateCal;
var endDateCal;

function unselectSelectedAlbumCollection() {
	// unselect currently selected item
	document.getElementsByClassName('selected','left-pane').each(function(selectedElement) {
		Element.removeClassName(selectedElement,'selected');
	});
}

function applyAlbumCollectionListLogic(el) {
	var myA = $A(el.getElementsByTagName('a')).first();
	if ( myA && myA.href.match(/(album|collection)Id=(\d+)/) ) {
		var data = myA.href.match(/(album|collection)Id=(\d+)/);
		
		Element.cleanWhitespace(myA.parentNode);

		// wrap A with a <div> and make the <div class="a"> droppable, checking for showhide arrow
		var spanNode = Builder.node('div',{'class':'a', 'id':'link-'+data[1]+'-'+data[2]});
		if ( myA.previousSibling && Element.hasClassName(myA.previousSibling,'showhide') ) {
			spanNode.appendChild(myA.previousSibling); // move node into spanNode
		}
		spanNode.appendChild(myA.firstChild.cloneNode(true));
		myA.parentNode.replaceChild(spanNode, myA);
		
		Event.observe(spanNode,'click',function(evt) {
			unselectSelectedAlbumCollection();
			
			// select new item
			Element.addClassName(spanNode.parentNode,'selected');
			
			// update UI data
			doUpdateUI(data[0]);
		},true);
		
		// make albums draggable so can setup parents
		if ( data[1] == 'album' ) {
			new Draggable(spanNode.parentNode,{revert:true});
			Element.addClassName(spanNode.parentNode, 'can-drag');
		}
			
		Droppables.add(spanNode,{
			/*accept : 'selected',*/
			hoverclass : 'can-drop',
			greedy : 'true',
			
			onDrop : function(draggable,droppable) {
				//alert("draggable = " +draggable.firstChild.id);

				// find out what was dropped... item or album
				var dragType = draggable.firstChild 
					? draggable.firstChild.id 
					: draggable.id;
				if ( dragType.match(/^item/) ) {
					if ( data[1] == 'album' ) {
						// adding to album
						handleItemDroppedOnAlbum(data[2]);
					} else {
						handleItemDroppedOnCollection(data[2]);
					}
				} else if ( dragType.match(/album-\d+/) ) {
					var droppedAlbumId = dragType.match(/album-(\d+)/)[1];
					handleAlbumDroppedOnAlbum(droppedAlbumId, data[2], 
						draggable, droppable);
				} else {
					alert("Unknown draggable: " +draggable +" (" +dragType +")");
				}
				//alert("Agh, you got me! " +data[1] +':' +data[2] +':' 
				//	+AppState.selected.toString());
			}
		});
	}
}

function handleItemDroppedOnAlbum(albumId) {
	var requestData = 'albumId='+albumId;
	AppState.selected.each(function(e) {
		requestData += '&itemIds='+e;
	});
	//alert(AppState.context+'/addToAlbum.do request = ' +requestData);
	new Ajax.Request(AppState.context+'/addToAlbum.do', {
		parameters:requestData, 
		onSuccess:function(t) {
			doStandardAjaxResult(t);
		}, 
		onFailure:function(t) {
			alert('[i18n]error adding to album: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
}

function handleItemDroppedOnCollection(collectionId) {
	var requestData = 'collectionId='+collectionId;
	AppState.selected.each(function(e) {
		requestData += '&itemIds='+e;
	});
	showServiceDialog('move-items-form',
			AppState.context+'/moveItems.do',
			requestData,
			handleMoveItemsSubmit);
}

function handleMoveItemsSubmit() {
	var theForm = $('move-items-form');
	new Ajax.Request(theForm.action, {
		parameters : Form.serialize(theForm), 
		onSuccess : function(t) {
			if ( doStandardAjaxResult(t) ) {
				doDeleteSelectedItems();
			}
		}, 
		onFailure : function(t) {
			alert('[i18n]error moving: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	return false;
}

function handleAlbumDroppedOnAlbum(srcAlbumId, destAlbumId, 
		draggable, droppable) {
	if ( !(srcAlbumId && destAlbumId && srcAlbumId != destAlbumId) ) {
		return;
	}
	//alert("Dropped album [" +srcAlbumId +"] onto album ["
	//	+destAlbumId +"]...");
	
	var requestData = 'albumId='+srcAlbumId
		+'&parentAlbumId=' +destAlbumId;
	new Ajax.Request(AppState.context+'/setAlbumParent.do', {
		parameters:requestData, 
		onSuccess:function(t) {
			doStandardAjaxResult(t);
			
			// move draggable to child of droppable
			// FIXME alert("draggable = " +draggable
			//	+", droppable = " +droppable);
			//droppable.appendChild(draggable);
		}, 
		onFailure:function(t) {
			alert('[i18n]error setting album parent: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
}

function setupThumbnailImgDrag(span, img, itemId) {
	new Draggable(span,{revert:true});
	Event.observe(span, 'click', function(evt) {
		AppState.selectItem(img, itemId, evt);
	});
	Element.addClassName(span, 'can-drag');
}

function setupThumbnailImgShadow(span, img) {
	img.onload = function() {
		var dim = Element.getDimensions(img);
		var width = dim.width;
		var height = dim.height;
		if ( width > 0 && height > 0 ) {
			var bgUrl = AppState.context +'/shadow.do?w=' +width 
				+'&h=' +height +'&b=6&r=3&c=3289650';
			// alert("setting shadow: " +bgUrl);
			span.style.backgroundImage = 'url(' +bgUrl +')';
			span.style.backgroundRepeat = 'no-repeat';
			span.style.backgroundPosition = '-3px -3px';
		}
	};
}

function setupThumbnailImgSpan(img, position) {
	var imgClass = is_safari ? 'thumb' : 'thumb-float';
	var span = Builder.node('span',{
		'class': imgClass,
		'id'   : 'item-position-'+position
		});
	span.appendChild(img);
	if ( !is_safari ) {
		var height = mediaSizes[APP_INFO.thumbSpec.size].height +'px';
		span.style.height = height;
	}
	return span;
}

function createNewItemJson(item, pane, position) {
	var itemId = item.itemId;
	var newImg = Builder.node('img',{
		'class' : 'thumb',
		'id'    : 'item-'+itemId
		});
	var span = setupThumbnailImgSpan(newImg, position);
	setupThumbnailImgShadow(span, newImg);
	newImg.src = AppState.context +'/media.do?id=' +itemId +'&size=' 
		+AppState.thumbSpec.size +'&quality='+AppState.thumbSpec.quality;
	pane.appendChild(span);
	setupThumbnailImgDrag(span, newImg, itemId);
}

function doSetupMediaItemsJson(itemsArray, pane) {
	var mainPane = $('main-pane');
	clearChildren(mainPane);
	
	// clear any selected items
	AppState.reset();
	
	for ( var i = 0, length = itemsArray.length; i < length; i++ ) {
		createNewItemJson(itemsArray[i], mainPane, i);
	}

	if ( itemsArray.length > 0 ) {
		AppState.showMenus('action');
	} else {
		AppState.hideMenus('action');
	}

}

function doSetupMediaItemsMonthsJson(itemsArray, pane) {
	var mainPane = $('main-pane');
	clearChildren(mainPane);
	
	// clear any selected items
	AppState.reset();
	
	if ( itemsArray.length > 0 ) {
		AppState.showMenus('action');
	} else {
		AppState.hideMenus('action');
	}

	var months = $H(new Object());
	for ( var i = 0, length = itemsArray.length; i < length; i++ ) {
		var yearMonth = itemsArray[i].itemDate.substring(0,7);
		if ( !months[yearMonth] ) {
			months[yearMonth] = new Array();
		}
		months[yearMonth].push(itemsArray[i]);
	}
	
	setupMediaItemMonthsJson(months, mainPane);
}

function setupMediaItemMonthsJson(months, mainPane) {
	var globalIdx = 0;
	months.keys().sort().reverse().each(function(key) {
		var date = new Date(key.substring(0,4),(key.substring(5,7))-1);
		var header = MatteLocale.i18n('month.' +date.getMonth())
			+", " +date.getFullYear();
		var monthItems = months[key];
		var hNode = Builder.node('h2',{},header);
		mainPane.appendChild(hNode);
		var iNode = Builder.node('div');
		mainPane.appendChild(iNode);
		monthItems.each(function(item, idx) {
			createNewItemJson(item, iNode, globalIdx);
			globalIdx++;
		});
	});
}

/**
 * Update the UI status display of an album shared stauts.
 * 
 * @param xmlRequest the XML data
 */
function updateAlbumShareStatusDisplay(xmlRequest) {
	var spanNode = $('share-album-status-display');
	if ( !spanNode ) return;
	var objectNode = xpathDomEval(
		'/x:x-data/x:x-model[1]/m:model[1]/m:album[1]',
		xmlRequest.responseXML).nodeSetValue()[0];
	var isShared = 'true' == objectNode.getAttribute('allow-anonymous');
	clearChildren(spanNode);
	if ( isShared ) {
		var shareLink = Builder.node('a',{'href':AppState.context+'/album.do?key='
			+objectNode.getAttribute('anonymous-key'),
			'title':MatteLocale.i18n('share.album.shared.link.title'),
			'target':'_blank'},
			MatteLocale.i18n('share.album.shared.displayName'));
		spanNode.appendChild(shareLink);
	} else {
		spanNode.appendChild(
			Builder.node('span',{'class':'data'}, 
			MatteLocale.i18n('share.album.notshared.displayName')));
	}
}

/**
 * Update the UI for the display of an album or collection.
 * 
 * @param data the JSON album or collection data
 * @param type either 'album' or 'collection'
 */
function doSetupAlbumCollectionDisplayJson(data, type) {
	var subNavData = $('sub-nav-data');
	clearChildren(subNavData);
	
	// object name
	subNavData.appendChild(Builder.node('span',{'class':'header'},
		MatteLocale.i18n(type+'.displayName')));
	var objectName = data.name;
	subNavData.appendChild(Builder.node('span',{'class':'data'}, 
		objectName));
	
	// last updated date
	subNavData.appendChild(Builder.node('span',{'class':'header'},
		MatteLocale.i18n('last.updated')));
	var objectDate = data.modifyDate
			? data.modifyDate
			: data.creationDate;
	var displayDate = parseXmlDate(objectDate);
	var displayDateFormat = MatteLocale.i18n('datetime.display.format');
	subNavData.appendChild(Builder.node('span',{'class':'data'}, 
		displayDate.format(displayDateFormat)));
	
	// item count
	subNavData.appendChild(Builder.node('span',{'class':'header'},
		MatteLocale.i18n('items')));
	subNavData.appendChild(Builder.node('span',{'class':'data'}, 
		data.items.length > 0 
			? data.items.length 
			: MatteLocale.i18n('none') ));
		
	// update associated form data
	var objectId = data[type+'Id'];
	$('delete-'+type+'-id').value = objectId; // delete ID
	Element.update('delete-'+type+'-name', objectName); // delete name
	$('removefrom-'+type+'-id').value = objectId; // remove from ID
	
	if ( type == 'album' ) {
		// display album shared setting
		subNavData.appendChild(Builder.node('span',{'class':'header'},
			MatteLocale.i18n('share.album.sharing.displayName')));
		var isShared = updateShareAlbumFormSettingsJson(data);
		if ( isShared ) {
			var shareLink = Builder.node('a',{'href':AppState.context+'/album.do?key='
				+data.anonymousKey,
				'title':MatteLocale.i18n('share.album.shared.link.title'),
				'target':'_blank'},
				MatteLocale.i18n('share.album.shared.displayName'));
			var shareSpan = Builder.node('span',
				{'class':'data', 'id':'share-album-status-display'});
			shareSpan.appendChild(shareLink);
			subNavData.appendChild(shareSpan);
		} else {
			subNavData.appendChild(Builder.node('span',
				{'class':'data', 'id':'share-album-status-display'}, 
				MatteLocale.i18n('share.album.notshared.displayName')));
		}
		
	}
	
}

function updateShareAlbumFormSettingsJson(data) {
	var allowAnonymous = data.allowAnonymous;
	var allowBrowse = data.allowBrowse;
	var allowFeed = data.allowFeed;
	var allowOriginal = data.allowOriginal;
	$('share-album-shared').checked = allowAnonymous;
	$('share-album-browse').checked = allowBrowse;
	$('share-album-original').checked = allowOriginal
	$('share-album-feed').checked = allowFeed;
	$('share-album-apply-children').checked = false;
	
	if ( data.theme ) {
		var albumThemeId = data.theme.themeId;
		selectMenuValue('share-album-theme', albumThemeId);
	} else {
		// TODO select default theme
	}

	if ( !allowAnonymous ) {
		disableAlbumShareControls();
	} else {
		enableAlbumShareControls();
	}

	return allowAnonymous;
}


function selectMenuValue(menu, value) {
	menu = $(menu);
	var options = menu.getElementsByTagName('option');
	var optionToSelect = $A(options).detect(function(option) {
		if ( value == option.value ) return true;
	});
	if ( !optionToSelect ) {
		alert("Error: unable to find [" +value +"] in [" +menu.name +"]");
		return;
	}
	menu.selectedIndex = optionToSelect.index;
}

function doDisplayMediaItems(parameters) {
	if ( !parameters ) {
		// nothing to display, so remove sub nav data and media items
		clearChildren('sub-nav-data');
		clearChildren('main-pane');
		AppState.hideMenus('action');
		return;
	}
	new Ajax.Request(AppState.context+'/mediaItems.json', {
		method: 'get',
		parameters: parameters, 
		onSuccess: function(t) {
			if ( isAjaxLogonRedirect(t) ) return;
			var data = eval(t.responseText);
			if ( data.album ) {
				doSetupMediaItemsJson(data.album.items);
				doSetupAlbumCollectionDisplayJson(data.album,'album');
			} else if ( data.collection ) {
				doSetupMediaItemsMonthsJson(data.collection.items);
				doSetupAlbumCollectionDisplayJson(data.collection,'collection');
			}
		}, 
		onFailure: function(t) {
			alert('[i18n]error getting media items: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
}

function doUpdateUI(parameters) {
	// show selected media items
	doDisplayMediaItems(parameters);

	var type = null;
	var objectId = null;
	if ( parameters ) {
		var data = parameters.match(/(album|collection)Id=(\d+)/);
		if ( data ) {
			type = data[1];
			objectId = data[2];
		}
	}
	
	AppState.updateUI(type,objectId);
}

function doDeleteSelectedAlbumOrCollection(context, objectId) {
	// remove link node from DOM
	var linkNode = $('link-'+context+'-'+objectId);
	linkNode.parentNode.parentNode.removeChild(linkNode.parentNode);
	doUpdateUI();
}

/**
 * Delete the selected items from the DOM.
 */
function doDeleteSelectedItems() {
	var main = $('main-pane');
	var numRemoved = 0;
	$A(main.getElementsByTagName('img')).each(function(thumbImg) {
		var objectId = thumbImg.src.match(/id=(\d+)/);
		if ( objectId ) {
			objectId = objectId[1];
		} else {
			objectId = -1;
		}

		if ( AppState.selected.indexOf(objectId) < 0 ) return;

		var thumbSpan = thumbImg.parentNode; // go to to <span> node to remove
		thumbSpan.parentNode.removeChild(thumbSpan);
		numRemoved++;
	});
	AppState.reset();
	
	// update item count
	var subNavData = $('sub-nav-data');
	
	// find the header for items, then update next sibling
	var headers = document.getElementsByClassName('header',subNavData);
	var itemCountHeaderValue = MatteLocale.i18n('items');
	var itemCountHeader = $A(headers).detect(function(header) {
		return header.hasChildNodes() && header.firstChild.nodeValue == itemCountHeaderValue;
	});
	if ( itemCountHeader && itemCountHeader.nextSibling != null ) {
		var currCount = itemCountHeader.nextSibling.firstChild.nodeValue;
		Element.update(itemCountHeader.nextSibling, String(currCount - numRemoved));
	}
	
}

/**
 * Handler to submit delete form, display result, and 
 * update the UI to remove the deleted item.
 *
 * @param deleteForm the form to submit
 */
function deleteAlbumOrCollection(deleteForm) {
	if ( isBehaved(deleteForm) ) return;
	deleteForm.onsubmit = function() {
		var selectedContext = AppState.selectedContext;
		var selectedContextObjectId = AppState.selectedContextObjectId;
		new Ajax.Request(deleteForm.action, {
			parameters : Form.serialize(deleteForm), 
			onSuccess : function(t) {
				doStandardAjaxResult(t,deleteForm,function() {
					doDeleteSelectedAlbumOrCollection(
						selectedContext, selectedContextObjectId);
				});
			}, 
			onFailure : function(t) {
				alert('[i18n]error deleting: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	};
	makeBehaved(deleteForm);
}

function deleteItemsFromAlbumOrCollection(deleteForm) {
	if ( isBehaved(deleteForm) ) return;
	deleteForm.onsubmit = function() {
		var removeParameters = Form.serialize(deleteForm);
		
		// add selected item IDs to parameters
		AppState.selected.each(function(id) {removeParameters += '&itemIds='+id});
		
		new Ajax.Request(deleteForm.action, {
			parameters : removeParameters, 
			onSuccess : function(t) {
				doStandardAjaxResult(t,deleteForm,function() {
					doDeleteSelectedItems();
				});
			}, 
			onFailure : function(t) {
				alert('[i18n]error deleting: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	};
	makeBehaved(deleteForm);
}

function saveShareAlbumSettings(shareForm) {
	if ( isBehaved(shareForm) ) return;
	shareForm.onsubmit = function() {
		$('share-album-id').value = AppState.selectedContextObjectId;
		var shareParameters = Form.serialize(shareForm);
		new Ajax.Request(shareForm.action, {
			parameters : shareParameters,
			onSuccess: function(xmlRequest) {
				doStandardAjaxResult(xmlRequest, shareForm, function() {
					updateAlbumShareStatusDisplay(xmlRequest);
					});
				},
			onFailure : function(xmlRequest) {
				alert('[i18n]error sharing: '
					+xmlRequest.status +' -- ' +xmlRequest.statusText +': ' 
					+xmlRequest.responseText);
				}
			});
		return false;
	};
	makeBehaved(shareForm);
}

function handleEditAlbumSubmit() {
	var theForm = $('edit-album-form');
	new Ajax.Request(theForm.action, {
		parameters : Form.serialize(theForm), 
		onSuccess : function(t) {
			// TODO need to handle error condition (i.e. display form again)
			if ( doStandardAjaxResult(t) ) {
				var paramData = updateUiFromNewAlbumOrCollection('album',t);
				
				// re-display album items, in case sort order changed
				if ( paramData ) {
					doDisplayMediaItems(paramData);
				}
			}
		}, 
		onFailure : function(t) {
			alert('[i18n]error updating: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	return false;
}

function handleSortAlbumsSubmit() {
	var childAlbums = $A($('sort-albums-container').getElementsByTagName('img'));
	var params = 'albumId=' +$F('sort-albums-parent');
	childAlbums.each(function(el, idx) {
		var albumId = el.id.substring(el.id.lastIndexOf('-')+1);
		params += ('&children[' +idx +'].albumId=' +albumId 
			+'&children[' +idx +'].order='+idx);
	});
	var theForm = $('sort-albums-form');
	new Ajax.Request(theForm.action, {
		parameters : params, 
		onSuccess : function(t) {
			// TODO need to handle error condition (i.e. display form again)
			if ( doStandardAjaxResult(t) ) {
				// hi
			}
		}, 
		onFailure : function(t) {
			alert('[i18n]error updating: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	return false;
}

function handleEditCollectionSubmit() {
	var theForm = $('edit-collection-form');
	new Ajax.Request(theForm.action, {
		parameters : Form.serialize(theForm), 
		onSuccess : function(t) {
			// TODO need to handle error condition (i.e. display form again)
			if ( doStandardAjaxResult(t) ) {
				updateUiFromNewAlbumOrCollection('collection',t);
			}
		}, 
		onFailure : function(t) {
			alert('[i18n]error updating: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	return false;
}

function handleDownloadItemsSubmit() {
	var theForm = $('item-download-form');
	var theAction = theForm.action;
	new Ajax.Request(theAction, {
		parameters : Form.serialize(theForm), 
		onSuccess : function(t) {
			if ( doStandardAjaxResult(t) ) {
				// get ticket ID and update window.location to download URI
				var workInfo = getWorkInfoFromXmlJobInfo(t);
				if ( workInfo ) {
					var ticket = workInfo.workTicket;
					var url = theAction +'?ticket=' +ticket;
					window.location = url;
				}
			}
		}, 
		onFailure : function(t) {
			alert('[i18n]error downloading items: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	return false;
}

function handleUserPreferencesSubmit() {
	/*var theForm = $('user-prefs-form');
	new Ajax.Request(theForm.action, {
		parameters : Form.serialize(theForm), 
		onSuccess : function(t) {
			if ( doStandardAjaxResult(t) ) {
				// reset current thumb/view settings
				AppState.setThumbnailSetting(
					$F('viewsettings-thumb-size'), $F('viewsettings-thumb-quality'));
				AppState.setViewSetting(
					$F('viewsettings-view-size'), $F('viewsettings-view-quality'));
			}
		}, 
		onFailure : function(t) {
			alert('[i18n]error updating: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	return false;*/
	return true;
}

// @deprecated
function newAlbumOrCollection(newObjectForm, type) {
	newObjectForm.onsubmit = function() {
		new Ajax.Request(newObjectForm.action, {
			parameters : Form.serialize(newObjectForm), 
			onSuccess : function(t) {
				if ( doStandardAjaxResult(t, newObjectForm) ) {
					updateUiFromNewAlbumOrCollection(type, t);
				}
			}, 
			onFailure : function(t) {
				alert('[i18n]error creating new album: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	};
}

/**
 * Update the UI after adding a new album or collection.
 * 
 * @param type either 'album' or 'collection'
 * @param xmlRequest the XMLRequest object
 */
function updateUiFromNewAlbumOrCollection(type, xmlRequest) {
	// get new object name and ID and add to end of object list
	var objectResult = xpathDomEval(
		'/x:x-data/x:x-model[1]/m:model[1]/m:'+type+'[1]', xmlRequest.responseXML);
	var objectNodeArray = objectResult.nodeSetValue();
	var paramData;
	if ( objectNodeArray.length > 0 ) {
		var objectNode = objectNodeArray[0];
		var objectName = objectNode.getAttribute('name');
		var objectId = objectNode.getAttribute(type+'-id');
		paramData = type+'Id=' +objectId;
		
		var aHref = AppState.context +'/home.do?'+paramData;
		var objectList = $(type+'-list');

		// see if only updated item (i.e. already in list)
		var currObjectNode = $('link-'+paramData);
		if ( currObjectNode ) {
			currObjectNode.title = objectName;
			Element.update(currObjectNode, objectName);
			doSetupAlbumCollectionDisplay(xmlRequest, type);
			return paramData;
		}
		
		// create new <li> and child <a>
		var liNode = Builder.node('li');
		var aNode = Builder.node('a',{
			title : objectName,
			href : aHref}, objectName);
		liNode.appendChild(aNode);
		objectList.appendChild(liNode);
		
		applyAlbumCollectionListLogic(liNode);
	}
	return null; // don't return paramData for new item
}

function changeViewSettings(form) {
	if ( isBehaved(form) ) return;
	form.onsubmit = function() {
		AppState.selectNone();
		new Ajax.Request(form.action, {
			parameters : Form.serialize(form), 
			onSuccess : function(t) {
				doStandardAjaxResult(t,form,function() {
					AppState.thumbSpec.size = $F('viewsettings-thumb-size');
					AppState.thumbSpec.quality = $F('viewsettings-thumb-quality');
					AppState.viewSpec.size = $F('viewsettings-view-size');
					AppState.viewSpec.quality = $F('viewsettings-view-quality');
					
					doUpdateUI(AppState.selectedContext+'Id='+AppState.selectedContextObjectId);
				});
			}, 
			onFailure : function(t) {
				alert('[i18n]error deleting: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	};
	makeBehaved(form);
}

function submitInfoForm(form) {
	form.onsubmit = function() {
		// only submit form elements that are visible
		var submitData = '';
		AppState.selected.each(function(itemId) {
			if ( submitData.length > 0 ) {
				submitData += '&';
			}
			submitData += 'itemIds='+itemId;
		});
		var formElements = Form.getElements(form);
		formElements.each(function(el) {
			if ( el.name && Element.visible(el) && Element.visible(el.parentNode) ) {
				submitData += '&';
				submitData += Form.Element.serialize(el);
			}
		});
		//alert("got data: " +submitData);
		new Ajax.Request(form.action, {
			parameters : submitData, 
			onSuccess : function(t) {
				doStandardAjaxResult(t,form);
			}, 
			onFailure : function(t) {
				alert('[i18n]error saving info: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	};
}

function showSearchItemForm() {
	Element.addClassName('main-pane', 'main-pane-search');
	Element.show('search-pane');
	Field.activate('quick-search');
}

function hideSearchItemForm() {
	Element.hide('search-pane');
	Element.removeClassName('main-pane', 'main-pane-search');
}

function handleSearchResults(results) {
	var subNavData = $('sub-nav-data');
	clearChildren(subNavData);
	
	// num matches
	subNavData.appendChild(Builder.node('span',{'class':'header'},
		MatteLocale.i18n('search.match.displayName')));
	subNavData.appendChild(Builder.node('span',{'class':'data'}, 
		MatteLocale.i18n('search.match.count', [results.searchResults.totalResults])));
	
	unselectSelectedAlbumCollection();
	doSetupMediaItemsMonthsJson(results.searchResults.items);
}

function searchItemForm(form) {
	form.onsubmit = function() {
		new Ajax.Request(form.action, {
			parameters : Form.serialize(form), 
			onSuccess : function(t) {
				if ( isAjaxLogonRedirect(t) ) return;
				var searchResults = eval(t.responseText);
				handleSearchResults(searchResults);
			}, 
			onFailure : function(t) {
				alert('[i18n]error searching items: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	}
}

function setAlbumPoster() {
	var params = $H(new Object());
	params.itemId = AppState.selected.first();
	params.albumId = AppState.selectedContextObjectId;
	new Ajax.Request(AppState.context +"/setAlbumPoster.do", {
			parameters : params.toQueryString(), 
			onSuccess : function(t) {
				doStandardAjaxResult(t);
			}, 
			onFailure : function(t) {
				alert('[i18n]error setting poster: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
}

function setupTzItemForm(form) {
	form.onsubmit = function() {
		$('item-tz').value = $F('home.mediaTz');
		$('item-tz-display').value = $F('home.localTz');
		Element.update('item-tz-container', $F('home.localTz'));
		doStandardDialogHide();
		return false;
	}
}

function setupTzContainer(el) {
	el.onclick = function() {
		var itemTz = $F('item-tz');
		var itemDisplayTz = $F('item-tz-display');
		
		// set form TZ selection to current item values
		$A($('home.mediaTz').options).find(function(option, idx) {
			if ( itemTz == option.value ) {
				$('home.mediaTz').selectedIndex = idx;
				return true;
			}
			return false;
		});
		$A($('home.localTz').options).find(function(option, idx) {
			if ( itemDisplayTz == option.value ) {
				$('home.localTz').selectedIndex = idx;
				return true;
			}
			return false;
		});
		doStandardDialogDisplay('tz-item-form');
		Form.focusFirstElement('tz-item-form');
	};
}

function disableAlbumShareControls() {
	$('share-album-browse').checked = false;
	$('share-album-feed').checked = false;
	$('share-album-original').checked = false;
	$('share-album-apply-children').checked = false;
	$('share-album-theme').checked = false;
	$('share-album-browse').disable();
	$('share-album-feed').disable();
	$('share-album-original').disable();
	$('share-album-theme').disable();
}

function enableAlbumShareControls() {
	$('share-album-browse').enable();
	$('share-album-feed').enable();
	$('share-album-original').enable();
	$('share-album-theme').enable();
}

var myRules = Object.extend({
		
	'#change-viewsetting-form' : changeViewSettings,
	
	'textarea' : function(el) {
		if ( isBehaved(el) ) return;
		// remove blank whitespace from textarea elements, because of XSL putting
		// empty space in them so the don't end up like <textarea/>
		if ( $F(el).search(/^[\s\n]+$/) == 0 ) {
			Field.clear(el);
		}
		makeBehaved(el);
	},
		
	'#shared-albums-url' : function(el) {
		if ( isBehaved(el) ) return;
		el.onclick = function() {
			window.open(el.firstChild.nodeValue);
		}
		Element.addClassName(el, 'a');
		makeBehaved(el);
	},
	
	'#sort-albums-container' : function(el) {
		Sortable.create(el, {
			tag: 'div',
			overlap: 'horizontal',
			constraint: false/*,
			onUpdate:function(){
		    if(Sortable.serialize("puzzle")==
		    }
		  }*/
		});
	},
	
	'#download-originals' : function(el) {
		el.onchange = function() {
			var size = $('download-size');
			var quality = $('download-quality');
			if ( !(size && quality) ) return;
			if ( el.checked ) {
				size.disabled = true;
				quality.disabled = true;
			} else {
				size.disabled = false;
				quality.disabled = false;
			}	
		}
	}
}, GlobalMatteBehaviours);
	
var initRules = Object.extend({
	'body' : function(el) {
		init();
	},
		
	'#listMenuRoot': function(el) {
		'action,item,album,collection'.split(',').each(function(type) {
			document.getElementsByClassName('action-'+type,el).each(function(menu) {
				AppState.initMenu(type,menu);
			});
		});
	},
	
	'#share-album-form' : saveShareAlbumSettings,
	
	'#share-album-shared' : function(el) {
		el.onchange = function() {
			if ( $F(el) != 'true' && ($F('share-album-feed') == 'true'
				|| $F('share-album-browse') == 'true') ) {
				$('share-album-feed').checked = false;
				$('share-album-browse').checked = false;
				alert(MatteLocale.i18n('share.album.anon.disables.others'));
			}
		}
	},
	
	'#share-album-feed' : function(el) {
		el.onchange = function() {
			if ( $F(el) == 'true' && $F('share-album-shared') != 'true' ) {
				$('share-album-shared').checked = true;
				alert(MatteLocale.i18n('share.album.feed.requires.shared'));
			}
		}
	},

	'#share-album-browse' : function(el) {
		el.onchange = function() {
			if ( $F(el) == 'true' && $F('share-album-shared') != 'true' ) {
				$('share-album-shared').checked = true;
				alert(MatteLocale.i18n('share.album.browse.requires.shared'));
			}
		}
	},

	'#item-tz-container' : function(el) {
		setupTzContainer(el);
	},
	
	'#tz-item-form' : function(el) {
		setupTzItemForm(el);
	},
	
	'#info-form' : function(el) {
		submitInfoForm(el);
	},
	
	'#search-pane .close-x' : function(el) {
		el.onclick = function() {
			hideSearchItemForm();
		}
		makeBehaved(el);
	},
	
	'#search-item-form' : function(el) {
		searchItemForm(el);
	},
	
	'#delete-album-form' : deleteAlbumOrCollection,
	
	'#delete-collection-form' : deleteAlbumOrCollection,
	
	'#removefrom-album-form' : deleteItemsFromAlbumOrCollection,
	
	'#removefrom-collection-form' : deleteItemsFromAlbumOrCollection,
	
	'span.rating-stars' : function(el) {
		new StarRating(el,0);
		Element.setStyle(el,{visibility:'visible'});
	},
	
	'#collection-list li' : function(el) {
		applyAlbumCollectionListLogic(el);
	},

	'#album-list li' : function(el) {
		applyAlbumCollectionListLogic(el);
	},

	// Non-contextal links configured here
	
	'.link-search-item' : function(el) {
		el.onclick = function() {
			showSearchItemForm();
		}
	},
	
	'.link-user-prefs' : function(el) {
		el.onclick = function() {
			showServiceDialog('user-prefs-form',
			AppState.context+'/preferences.do',
			null,
			handleUserPreferencesSubmit);
		}
	},
	
	'.link-add-album' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-album-form',
			AppState.context+'/editAlbum.do',
			null,
			handleEditAlbumSubmit);
		}
	},
	
	'.link-add-collection' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-collection-form',
			AppState.context+'/editCollection.do',
			null,
			handleEditCollectionSubmit);
		}
	},
	
	'#share-album-shared' : function(el) {
		el.onclick = function() {
			if ( el.checked ) {
				enableAlbumShareControls();
			} else {
				disableAlbumShareControls();
			}
		}
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
				alert(MatteLocale.i18n('search.date.not.valid'));
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
				alert(MatteLocale.i18n('search.date.not.valid'));
				return;
			}
			endDateCal.select(date);
			var theDate = endDateCal.getSelectedDates()[0]; 
			endDateCal.cfg.setProperty("pagedate", 
				(theDate.getMonth()+1) +"/" +theDate.getFullYear());
			endDateCal.render();
		}
		makeBehaved(el);
	}

}, GlobalMatteInitBehaviours);

// Contextual links configured here
MatteStateMenuBehaviours = {
	'.link-delete-album' : function(el) {
		showStandardForm(el,'album','delete',true);
	},

	'.link-delete-collection' : function(el) {
		showStandardForm(el,'collection','delete',true);
	},

	'.link-share-album' : function(el) {
		showStandardForm(el,'album','share');
	},
	
	'.link-sort-album' : function(el) {
		el.onclick = function() {
			showServiceDialog('sort-albums-form',
				AppState.context +'/sortAlbums.do',
				'albumId='+AppState.selectedContextObjectId,
				handleSortAlbumsSubmit);
		}
	},
	
	'.link-update-album' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-album-form', 
				AppState.context+'/editAlbum.do', 
				'albumId='+AppState.selectedContextObjectId,
				handleEditAlbumSubmit);
		}
	},

	'.link-update-collection' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-collection-form', 
				AppState.context+'/editCollection.do', 
				'collectionId='+AppState.selectedContextObjectId,
				handleEditCollectionSubmit);
		}
	},

	'.link-upload-collection' : function(el) {
		el.onclick = function() {
			showServiceDialog('upload-media-form', 
				AppState.context+'/addService.do', 
				'collectionId='+AppState.selectedContextObjectId,
				null);
		}
	},
	
	'.link-download-items' : function(el) {
		el.onclick = function() {
			// add selected item IDs to parameters
			var itemParameters = '';
			AppState.selected.each(function(id) {
				itemParameters += '&itemIds='+id});

			showServiceDialog('item-download-form',
				AppState.context+'/downloadItems.do',
				itemParameters, handleDownloadItemsSubmit);
		}
	},

	'.link-download-album' : function(el) {
		el.onclick = function() {
			// add selected album ID to parameters
			var albumParameters = 'albumId='
				+AppState.selectedContextObjectId;

			showServiceDialog('item-download-form',
				AppState.context+'/downloadItems.do',
				albumParameters, handleDownloadItemsSubmit);
		}
	},

	'.link-removefrom-album' : function(el) {
		showStandardForm(el,'album','removefrom',true);
	},
	
	'.link-setposter-album' : function(el) {
		el.onclick = function() {
			setAlbumPoster();
		}
	},
	
	'.link-sort-items' : function(el) {
		el.onclick = function() {
			window.location = AppState.context 
				+'/sortItems.do?albumId='
				+AppState.selectedContextObjectId;
		}
	},
	
	'.link-removefrom-collection' : function(el) {
		showStandardForm(el,'collection','removefrom',true);
	},
	
	/*'.link-change-viewsettings' : function(el) {
		if ( isBehaved(el) ) return;
		showStandardForm(el,'viewsetting','change',false);
		makeBehaved(el);
	},*/
	
	'.link-select-all' : function(el) {
		el.onclick = function() {
			AppState.selectAll();
		}
	},
	
	'.link-select-none' : function(el) {
		el.onclick = function() {
			AppState.selectNone();
		}
	}
};

function init() {
	// 1: initialize locale messages
	doInitXmsg();
	
	// 2: initialize app menu...
	doInitMenu();
	
	// 3: unregister init rules for performance
	Behaviour.unregister(initRules);
	
	var myObserver = new NumberDecorationObserver();
	Draggables.addObserver(myObserver);
}

var LIST_MENU = new FSMenu('LIST_MENU', true, 'visibility', 'visible', 'hidden');
Behaviour.register(initRules);
Behaviour.register(myRules);
