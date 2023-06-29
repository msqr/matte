/**
 * MatteState: class to keep track of current state of application,
 * e.g. selected objects. The 'selected' property is an array of 
 * keys, manipulated via the select() and unselect() functions.
 */
var MatteStateMenuBehaviours = MatteStateMenuBehaviours || new Object();
var MatteState = Class.create();
MatteState.prototype = {
	initialize: function() {
		this.selected = new Array(); // selected items
		this.context = '/ma'; // default context
		this.dialogVisible = false;
		
		this.actionLists = new Object();
		this.actionLists.action = new Array();
		this.actionLists.item = new Array();
		this.actionLists.album = new Array();
		this.actionLists.collection = new Array();
		this.actionLists.user = new Array();
		this.actionLists.theme = new Array();
		
		this.selectedContext = null;
		this.selectedContextObjectId = 0;
		this.inDrag = false;
		
		this.thumbSpec = {size:'THUMB_NORMAL',quality:'GOOD'};
		this.viewSpec = {size:'NORMAL',quality:'GOOD'};
		
		if ((typeof Prototype=='undefined') ||
				parseFloat(Prototype.Version.split(".")[0] + "." +
		         Prototype.Version.split(".")[1]) < 1.4)
			throw("MatteState requires the Prototype JavaScript framework >= 1.4.0");
			
		var me = this;
		
		/* AUGH, Firefox fails on this with Scriptaculous there, too. Safari OK
		$A(document.getElementsByTagName("script")).findAll( function(s) {
			return (s.src && s.src.match(/behaviours\.js(\?.*)?$/))
		}).each( function(s) {
			var path = s.src.replace(/behaviours\.js(\?.*)?$/,'');
			var myContext = s.src.match(/\?.*context=([^ &]+)/);
			if ( myContext ) me.setContext(myContext[1]);
		});
		*/
		var s = $('matte-classes-js');
		var path = s.src.replace(/matte-classes\.js(\?.*)?$/,'');
		var myContext = s.src.match(/\?.*context=([^ &]+)/);
		if ( myContext ) me.setContext(myContext[1]);

		this.itemInfoPopulator = new ItemInfoPopulator(this);
		
		// listen for key presses
		//Event.observe(document,'keydown',this.handleKeyDown);
		//Event.observe(document,'keyup',this.handleKeyUp);
	},
	
	/*handleKeyDown: function(evt) {
		window.status = "got keydown: " +evt;
		alert(evt);
	},
	
	handleKeyUp: function(evt) {
		window.status = "got keyup: " +evt;
	},*/
	
	startDrag: function() {
		this.inDrag = true;
	},
	
	endDrag: function() {
		this.inDrag = false;
	},
	
	/**
	 * Init menu items for a given type.
	 */
	initMenu: function(type, menu) {
		//  make sure menu is visible
		Element.setStyle(menu, {'display':'block'});
		
		// add menu to appropriate array
		var menuItem = { item: menu, parent: menu.parentNode, behaved: false };
		var isContextual = Element.hasClassName(menu,'context-album')
			|| Element.hasClassName(menu,'context-collection')
			|| Element.hasClassName(menu,'context-theme')
			|| Element.hasClassName(menu,'context-user');
		if ( isContextual ) {
			menuItem.contextual = Element.hasClassName(menu,'context-album')
				? 'album' : Element.hasClassName(menu,'context-collection') 
					? 'collection' : Element.hasClassName(menu,'context-theme')
						? 'theme' : 'user';
		} else {
			menuItem.contextual = null;
		}
		this.actionLists[type].push(menuItem);
		
		// remove menu from DOM
		menuItem.parent.removeChild(menu);
	},
	
	/**
	 * Show menu items for a given type.
	 */
	showMenus: function(type) {
		var me = this;
		//alert("show: " +type +"; " +this.actionLists[type]);
		if ( !this.actionLists[type] || this.actionLists[type].length < 1 ) return;
		this.actionLists[type].reverse(false).each(function(menuItem) {
			/*alert("selectedContext = " +me.selectedContext 
				+", contextual = " +menuItem.contextual 
				+", type = " +type
				+", yes = " +(me.selectedContext == menuItem.contextual));*/
			if ( !me.selectedContext || (me.selectedContext && (!menuItem.contextual 
					|| (menuItem.contextual && menuItem.contextual == me.selectedContext))) ) {
				//alert("Showing menu: " +menuItem.item);
				menuItem.parent.insertBefore(menuItem.item, menuItem.parent.firstChild);
				if ( !menuItem.behaved ) {
					Behaviour.applySheet(MatteStateMenuBehaviours, menuItem.item);
				}
			}
		});
	},
	
	/**
	 * Hide menu items for a given type.
	 */
	hideMenus: function(type) {
		if ( !this.actionLists[type] ) return;
		this.actionLists[type].each(function(menuItem) {
			if ( menuItem.item.parentNode ) {
				menuItem.parent.removeChild(menuItem.item);
			}
		});
	},
	
	/**
	 * Update the UI menu objects.
	 * @param type {String} the type of container selected (album, collection, user, theme, or null)
	 * @param objectId {Number} the unique ID of the container selected
	 */
	updateUI: function(type, objectId) {
		this.selectedContext = type;
		this.selectedContextObjectId = objectId;
		
		if ( type == 'album' ) {
			// album selected
			this.showMenus('album');
			this.hideMenus('collection');
		} else if ( type == 'collection' ) {
			// collection selected
			this.showMenus('collection');
			this.hideMenus('album');
		} else if ( type == 'user' ) {
			this.showMenus('user');
			this.hideMenus('theme');
		} else if ( type == 'theme' ) {
			this.showMenus('theme');
			this.hideMenus('user');
		} else {
			// neither album nor collection selected, hide those
			this.hideMenus('album');
			this.hideMenus('collection');
			this.hideMenus('user');
			this.hideMenus('theme');
		}
	},
	
	/**
	 * Set the web context path (eg. '/matte').
	 * @param theContext {String} the web context path to set
	 */
	setContext: function(theContext) {
		this.context = theContext;
	},
	
	/**
	 * Make an object "selected" by adding it's key
	 * to array of selected keys.
	 * 
	 * @param key {Number} the key of the object to select
	 */
	select: function(key) {
		if ( this.selected.indexOf(key) < 0 ) {
			this.selected.push(key);
			
			// display action-item contextual menu if items are selected
			this.showMenus('item');
		}
	},
	
	/**
	 * Make an object "unselected" by removing it's key
	 * from array of selected keys.
	 * 
	 * @param key the key of the object to unselect
	 */
	unselect: function(key) {
		this.selected = this.selected.without(key);
		
		if ( this.selected.length < 1 ) {
			// hide action-item contextual menu if no items are selected
			this.hideMenus('item');
		}

	},
	
	/**
	 * Reset so no items are selected.
	 */
	reset: function() {
		this.selected.clear();
		this.hideMenus('item');
		var infoPane = $('info-pane');
		if ( infoPane ) {
			infoPane.hide();
		}
		var previewPane = $('preview-pane');
		if ( previewPane ) {
			previewPane.hide();
		}
	},
	
	/**
	 * Select a single item.
	 * 
	 * @param selectableItem the item node of the thing selected
	 * @param itemId the ID of the item selected
	 */
	selectItem: function(selectableItem, itemId, event) {
		if ( Element.hasClassName(selectableItem, 'selected') ) {
			Element.removeClassName(selectableItem,'selected');
			this.unselect(itemId);
		} else {
			//alert("alt: " +event.altKey +", ctrl: " +event.ctrlKey 
			//	+", meta: " +event.metaKey +", shift: " +event.shiftKey);
			
			// if nothing currently selected, does not matter what modifier key was used, 
			// or if Alt key was used, always select item, for un-continguous selections
			if ( !event || this.inDrag || this.selected.length == 0 || event.altKey ) {
				Element.addClassName(selectableItem,'selected');
				this.select(itemId);
				this.endDrag();
			} else if ( event && event.shiftKey ) {
				// select everything between this one and the last one that was selected
				var lastSelectedId = this.selected.last();
				var lastSelectedItem = $('item-'+lastSelectedId);
				var lastSelectedItemPosition = Number(lastSelectedItem.parentNode.id.match(/(\d+)$/)[1]);
				var selectedItemPosition = Number(selectableItem.parentNode.id.match(/(\d+)$/)[1]);
				var start = lastSelectedItemPosition < selectedItemPosition 
					? lastSelectedItemPosition : selectedItemPosition;
				var end = lastSelectedItemPosition > selectedItemPosition 
					? lastSelectedItemPosition : selectedItemPosition;
				for ( var currIdx = start; currIdx <= end; currIdx++ ) {
					var currItem = $('item-position-'+currIdx).firstChild;
					if ( !Element.hasClassName(currItem,'selected') ) {
						Element.addClassName(currItem,'selected');
						var itemId = currItem.src.match(/id=(\d+)/)[1];
						this.select(itemId);
					}
				}
			} else {
				// unselect currenlty selected, then select this one
				this.selected.each(function(selectedItemId) {
					var currentlySelectedItem = $('item-'+selectedItemId);
					Element.removeClassName(currentlySelectedItem,'selected');
				});
				this.selected.clear();
				Element.addClassName(selectableItem,'selected');
				this.select(itemId);
			}
		}
		
		// adjust selected menu
		if ( this.selected.length > 0 ) {
			// show selected menu
			this.showMenus('item');
		} else {
			this.hideMenus('item');
		}
		
		if ( event ) {
			this._updateInfoPane();
		}
	},
	
	setThumbnailSetting: function(size, quality) {
		this.thumbSpec.size = size;
		this.thumbSpec.quality = quality;
	},
	
	setViewSetting: function(size, quality) {
		this.viewSpec.size = size;
		this.viewSpec.quality = quality;
	},
	
	_updateInfoPane: function() {
		// adjust info pane, and preview pane, as needed
		var infoPane = $('info-pane');
		var previewPane = $('preview-pane');
		
		if ( this.selected.length > 1 ) {
			// hide the preview pane for multi items
			previewPane.hide();
			
			// show multi items info pane
			document.getElementsByClassName('multi',infoPane).each(function(node) {
				Element.setStyle(node,{display:'block'});
			});
			document.getElementsByClassName('single',infoPane).each(function(node) {
				if ( !Element.hasClassName(node,'multi') ) {
					Element.setStyle(node,{display:'none'});
				}
			});
			Element.setStyle(infoPane,{display:'block'});
			this.itemInfoPopulator.updateItemInfo();
		} else if ( this.selected.length == 1 ) {
			// show the preview pane for single item
			previewPane.show();

			// show  single item info pane
			document.getElementsByClassName('single',infoPane).each(function(node) {
				Element.setStyle(node,{display:'block'});
			});
			document.getElementsByClassName('multi',infoPane).each(function(node) {
				if ( !Element.hasClassName(node,'single') ) {
					Element.setStyle(node,{display:'none'});
				}
			});
			Element.setStyle(infoPane,{display:'block'});
			this.itemInfoPopulator.updateItemInfo();
		} else {
			// hide info, preivew panes
			infoPane.hide();
			previewPane.hide();
		}	
	},
	
	selectAll: function() {
		var me = this;
		document.getElementsByClassName('thumb','main-pane').each(function(el) {
			if ( el.nodeName.toLowerCase() != 'img' ) return;
			if ( !Element.hasClassName(el, 'selected') ) {
				var myId = el.src.match(/id=(\d+)/);
				if ( myId ) {
					myId = myId[1];
				} else {
					myId = -1;
				}
				me.selectItem(el,myId);
			}
		});
		this._updateInfoPane();
	},
	
	selectNone: function() {
		var me = this;
		document.getElementsByClassName('selected','main-pane').each(function(el) {
			if ( el.nodeName.toLowerCase() != 'img' ) return;
			var myId = el.src.match(/id=(\d+)/);
			if ( myId ) {
				myId = myId[1];
			} else {
				myId = -1;
			}
			me.selectItem(el,myId);
		});
		this._updateInfoPane();
	}/*,
	
	selectContextNone: function() {
		this.selectedContext = 'none';
		this.selectedContextAlbumId = 0;
		this.selectedContextCollectionId = 0;
	},

	selectContextAlbum: function(albumId) {
		this.selectedContext = 'album';
		this.selectedContextAlbumId = albumId;
		this.selectedContextCollectionId = 0;
	},
	
	selectContextCollection: function(collectionId) {
		this.selectedContext = 'collection';
		this.selectedContextCollectionId = collectionId;
		this.selectedContextAlbumId = 0;
	}*/
}

var ItemInfoPopulator = Class.create();
ItemInfoPopulator.prototype = {
	initialize: function(appState,action) {
		this.appState   = appState;
		this.action = action || appState.context+'/mediaItemInfo.do';
		this.numUpdateRequests = 0;
		this.xmlRequest = null;
	},
	
	updateItemInfo: function() {
		// only allow 1 buffered request, so don't generate tons
		// of update requests for a series of quick selections
		if ( this.numUpdateRequests > 1 ) return;
		var num = ++this.numUpdateRequests;
		if ( num == 1 ) this._doRequest();
	},
	
	_doRequest: function() {
		if ( this.appState.selected.length == 0 ) {
			this.numUpdateRequests--;
			return;
		}
		var me = this;
		var params = '';
		this.appState.selected.each(function(itemId) {
			if ( params != '' ) params += '&';
			params += 'itemIds='+itemId;
		});
		new Ajax.Request(this.action, {
			parameters : params, 
			onSuccess : function(t) {
				if ( isAjaxLogonRedirect(t) ) return;
				me.numUpdateRequests--;
				if ( me.numUpdateRequests > 0 ) {
					me._doRequest();
				} else {
					me._updateInfoDisplay(t);
				}
			}, 
			
			onFailure : function(t) {
				me.numUpdateRequests--;
				alert('[i18n]error getting info: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
	},
	
	_updateInfoDisplay: function(xmlRequest) {
		var item = xpathDomEval(
			'/x:x-data/x:x-model[1]/m:model[1]/m:item[1]',
			xmlRequest.responseXML).nodeSetValue();
		if ( item.length == 0 ) return;
		
		item = item[0];

		// insert item image into preview pane
		var itemId = item.getAttribute('item-id');
		var itemName = item.getAttribute('name');
		
		var itemPreviewSrc = AppState.context +'/media.do?id=' +itemId 
			+'&size=THUMB_BIGGER&quality='+AppState.thumbSpec.quality;
		var previewImage = $('preview-image');
		if ( previewImage ) {
			previewImage.setAttribute('src', itemPreviewSrc);
			previewImage.setAttribute('alt', itemName);
		} else {
			var newImg = Builder.node('img',{
				'alt'   : itemName,
				'src'   : itemPreviewSrc,
				'id'	: 'preview-image'
				});
			newImg.onclick = function() {
				var src = $('preview-image').getAttribute('src');
				var match = src.match(/id=(\d+)/);
				if ( match ) {
					var itemId = match[1];
					var previewSrc = AppState.context +'/media.do?id='
						+itemId +'&size=' +AppState.viewSpec.size
						+'&quality=' +AppState.viewSpec.quality;
					var winWidth = mediaSizes[AppState.viewSpec.size].width + 40;
					var winHeight = mediaSizes[AppState.viewSpec.size].height + 40;
					var previewWindow = window.open(previewSrc, "matte_item_preview",
						'width='+winWidth+',height='+winHeight
						+',menubar=no,toolbar=no,resizable,personalbar=no,directories=no,hotkeys=no');
					previewWindow.focus();
				}
			}
			$('preview-container').appendChild(newImg);
		}
			
		$('item-name').value = itemName;
		$('item-date').value = (item.hasAttribute('item-date')
			? item.getAttribute('item-date')
			: item.getAttribute('creation-date'))
				.substring(0,19).replace('T', ' ');
		
		var tzName = xpathDomEval('m:tz-display/@name', item).stringValue();
		if ( !tzName ) {
			tzName = xpathDomEval('m:tz/@name', item).stringValue();
		}
		var tzCode = xpathDomEval('m:tz/@code', item).stringValue();
		var tzDisplayCode = xpathDomEval('m:tz-display/@code', item).stringValue();
		$('item-tz').value = tzCode;
		$('item-tz-display').value = tzDisplayCode;
		
		Element.update('item-tz-container', tzName);
		
		var description = xpathDomEval('m:description[1]', item);
		if ( description.nodeSetValue().length > 0 ) {
			var desriptionContentNode = description.nodeSetValue()[0].firstChild;
			if ( desriptionContentNode ) {
				$('item-comments').value = desriptionContentNode.nodeValue;
			}
		} else {
			$('item-comments').value = '';
		}
		
		// get tags from metadata
		var userId = xpathDomEval(
			'/x:x-data/x:x-session[1]/m:session[1]/m:acting-user[1]/@user-id',
			xmlRequest.responseXML).stringValue();
		var tagXpath = "m:user-tag[m:tagging-user[@user-id="+userId+"]]/m:tag/text()";
		var tags = xpathDomEval(tagXpath, item).nodeSetValue();
		if ( tags.length < 1 ) {
			// test for 0.4 tag metadata
			tags = xpathDomEval("m:metadata[@key='ma2.tag']/text()", 
				item).nodeSetValue();
		}
		if ( tags.length > 0 ) {
			var tagsStr = '';
			for ( var i = 0; i < tags.length; i++ ) {
				if ( i > 0 ) {
					tagsStr += ', ';
				}
				tagsStr += tags[i].nodeValue;
			}
			$('item-tags').value = tagsStr;
		} else {
			$('item-tags').value = '';
		}
		
		if ( this.appState.selected.length == 1 ) {
			// update single item rating
			
			// XPath lib doesn't seem to handle this as a single XPath, but works with 
			// hard-coded user ID on XPath
			
			var actingUserId = xpathDomEval('/x:x-data/x:x-session[1]/m:session[1]/m:acting-user[1]/@user-id',
				xmlRequest.responseXML).numberValue();
			var ownerRating = xpathDomEval(
				'm:user-rating[./m:rating-user[@user-id = '+actingUserId +']]/@rating', item);
			$('item-rating').starRating.resetRating(ownerRating.numberValue());
		} else {
			$('item-rating').starRating.resetRating(0);
		}
	}
}

/**
 * NumberDecorationObserver: class to keep track of selected drag operations
 * and to display the number of dragged objects decoration. When dragging
 * starts, if AppState's selected array has more than 1 element in it then
 * a <div id="drag-number">X</div> element is appended as a child to the 
 * dragged element and the top and left positioned to the bottom-right corner
 * of the selectableElement's dimensions. For this to work it is assumed
 * the CSS for #drag-number is absolutely positioned using top,left coordinates
 * and has visibility:hidden.
 */
var NumberDecorationObserver = Class.create();
NumberDecorationObserver.prototype = {
	initialize: function(selectableElementName) {
		this.selectableElementName = selectableElementName || 'img';
	},

	/**
	 * Display the number decoration if more than one item selected.
	 */
	onStart: function(eventName,draggable) {
		var data = draggable.element.id 
			? draggable.element.id.match(/item-position-(\d+)/)
			: null;
		if ( data ) {
			AppState.startDrag();
			
			var selectableElement = this._getSelectableElement(draggable);
			var selectableElementId = selectableElement.id.match(/^item-(\d+)/)[1];

			if ( !Element.hasClassName(selectableElement, 'selected') ) {
				Element.addClassName(selectableElement,'selected');
				AppState.select(selectableElementId);
			}
			if ( AppState.selected.length > 1 ) {
				var num = Builder.node('div',{id:'drag-number'},AppState.selected.length);
				
				// Safari does not seem to make width available until after element inserted into DOM,
				// so assume CSS starts with visibility:hidden, insert into DOM, then reposition to lower-right
	
				draggable.element.appendChild(num);
				
				if ( Element.getStyle(num,'width') ) {
					var dimensions = Element.getDimensions(selectableElement);
					var numWidth = Element.getStyle(num,'width').match(/^\d+/); // remove 'px'
					var numHeight = Element.getStyle(num,'height').match(/^\d+/);
					Element.setStyle(num,{top:(dimensions.height-(numHeight/2)-4)+'px', 
						left:(dimensions.width-(numWidth/2)-4)+'px'});
				}
				
				Element.setStyle(num,{visibility:'visible'});
			}
		}
	},
	
	/**
	 * Remove the number decoration if previously shown.
	 */
	onEnd: function(eventName,draggable) {
		var data = draggable.element.id 
			? draggable.element.id.match(/item-position-(\d+)/)
			: null;
		if ( data ) {
			//this.appState.endDrag();
			var num = $('drag-number');
			if ( num ) {
				num.parentNode.removeChild(num);
			}
			var selectableElement = this._getSelectableElement(draggable);		
			if ( Element.hasClassName(selectableElement, 'selected') ) {
				Element.removeClassName(selectableElement,'selected');
			}
		}
	},
	
	_getSelectableElement: function(draggable) {
		var selectableElement = draggable.element.getElementsByTagName(
			this.selectableElementName);
		if ( selectableElement.length < 1 ) {
			selectableElement = draggable.element;
		} else {
			selectableElement = selectableElement[0];
		}
		return selectableElement;
	}
}


var WorkingPeriodicalExecutor = Class.create();
WorkingPeriodicalExecutor.prototype = {
  initialize: function(frequency, callback) {
    this.frequency = frequency || 2;
    this.callback = callback || function () {
		new Insertion.Bottom('system-working','.');
	};
    this.currentlyExecuting = false;
	this.working = $('system-working');
	this.originalWorkingValue = this.working.firstChild.nodeValue;
    this.registerCallback();
  },

  registerCallback: function() {
  	if ( !this.timer ) {
	    this.timer = setInterval(this.onTimerEvent.bind(this), this.frequency * 1000);
	}
  },
  
  start: function() {
  	this.registerCallback();
  },

  stop: function() {
  	clearTimeout(this.timer);
  	this.timer = null;
    Element.update(this.working,this.originalWorkingValue);
  },

  onTimerEvent: function() {
    if (!this.currentlyExecuting) {
      try {
        this.currentlyExecuting = true;
        this.callback();
      } finally {
        this.currentlyExecuting = false;
      }
    }
  }
}


var ProgressMonitor = Class.create();
ProgressMonitor.prototype = {
	initialize: function(taskId, options) {
		this.taskId = taskId;
		this.options = {
			frequency: 5,			// secs for updating
			amountComplete: 0.0		// 0<=1 amount of work complete
		};
		Object.extend(this.options, options || {});
		
		this.progress = $('task-'+taskId);
		this.progressPane = $('progress-pane');
		
		//alert("Task " +taskId +": " +this.progress +", " +this.options.frequency);
	},
  
	start: function() {
		if ( !Element.visible(this.progressPane) ) {
			new Effect.Appear(this.progressPane,{duration: 0.5});
		}
		if ( this.options.amountComplete < 1.0 ) {
			this.timer = setInterval(this.onTimerEvent.bind(this), 
				this.options.frequency * 1000);
		} else {
			// we are 100% complete already!
			this.stop();
		}
	},

	stop: function() {
		if ( this.timer != null ) {
			clearTimeout(this.timer);
			this.timer = null;
		}
		
		// wait a few seconds, then remove progress
		var me = this; // save reference to self
		setTimeout(function() {
			me.removeProgress();
		}, 5000);
	},

	onTimerEvent: function() {
		if ( this.timer == null ) {
			return;
		}
		var me = this; // so can access this from Ajax.Request
		new Ajax.Request(AppState.context+'/job.do', {
			parameters: 'ticket='+this.taskId, 
			onSuccess: function(t) {
				if ( isAjaxLogonRedirect(t) ) return;
				me.processResponse(t);
				//me.stop();
			}, 
			onFailure: function(t) {
				alert('[i18n]error getting job info: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
				me.stop();
			}});
	},
	
	removeProgress: function() {
		if ( Element.visible(this.progress) ) {
			var me = this;
			new Effect.Fade(this.progress, {duration: 0.5, afterFinish: function() {
					// remove myself
					me.progress.parentNode.removeChild(me.progress);
					me.updateProgressList();
				}
			});
			return
		}
		if ( this.progress.parentNode ) {
			this.progress.parentNode.removeChild(me.progress);
		}
		this.updateProgressList();
	},
	
	updateProgressList: function() {
		if ( $('progress-list').getElementsByTagName('li').length == 0 ) {
			// bye to progress pane
			setTimeout(function() {
				// test again in case new job was added by chance...
				if ( $('progress-list').getElementsByTagName('li').length == 0 ) {
					new Effect.Fade('progress-pane', {duration: 0.5});
				}
			}, 1000);
			
		}
	},
	
	processResponse: function(xmlRequest) {
		var error = xpathDomEval('/x:x-data/x:x-model[1]/m:job-info[1]/m:error[1]',xmlRequest.responseXML);
		if ( error.nodeSetValue().length > 0 ) {
			doStandardMessageDisplay('<div class="error">' +error.stringValue() +'</div>');
			this.stop();
		}
		
		var msg = xpathDomEval('/x:x-data/x:x-model[1]/m:job-info[1]/m:message[1]',xmlRequest.responseXML);
		
		var jobInfo = xpathDomEval('/x:x-data/x:x-model[1]/m:job-info[1]',
			xmlRequest.responseXML).nodeSetValue()[0];
		var amountCompleted = jobInfo.getAttribute('amount-completed');
		amountCompleted = Math.round(amountCompleted*100.0);
		Element.update('task.complete-'+this.taskId, amountCompleted+'%');
		
		if ( msg.nodeSetValue().length > 0 ) {
			Element.update('task.message-'+this.taskId, msg.stringValue());
		}
		
		if ( amountCompleted >= 100 ) {
			this.stop();
		}
		
		//alert("OK: " +xmlRequest.responseXML);'
	}
}

var StarRating = Class.create();
StarRating.prototype = {
	initialize: function(container, currentRating, objectId, imagePath, wsUrl, maxRating, appState) {
		this.container = container;
		this.currentRating = currentRating;
		this.objectId = objectId;
		this.maxRating = maxRating || 5;
		this.imgArray = new Array();
		this.appState = appState || AppState;
		this.wsUrl = wsUrl || this.appState.context+'/setMediaItemRating.do';
		this.imagePath = imagePath || this.appState.context+'/img/';
		
		var me = this;
		for ( var i = 0; i < this.maxRating; i++ ) {
			var myRating = i+1;
			var starImg = Builder.node('img',{
				alt: myRating,
				src: this.imagePath+'star-off.png',
				'class': 'star-rating'});
				
			starImg.onmouseover = function() {
				me.highlightRating(this.alt);
			}
			
	    	starImg.onmouseout = function() {
	    		me.resetRating();
	    	}
	    	starImg.onclick = function() {
	    		me.setRating(this.alt);
	    	}

			this.imgArray.push(starImg);
			this.container.appendChild(starImg);
		}
		
		this.resetRating();
		this.container.starRating = this;
	},
	
	setRating: function(rating) {
		this.currentRating = rating;
		var requestData = 'rating='+rating;
		AppState.selected.each(function(objectId) {
			requestData += '&itemIds='+objectId;
		});
		new Ajax.Request(this.wsUrl, {
			parameters: requestData, 
			onSuccess: function(t) {
				if ( isAjaxLogonRedirect(t) ) return;
			}, 
			onFailure: function(t) {
				alert('[i18n]error setting rating: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
	},
	
	highlightRating: function(rating) {
		for ( var i = 0; i < rating; i++ ) {
			this.imgArray[i].src = this.imagePath + 'star-active.png';
		}
		for ( var i = rating; i < this.maxRating; i++ ) {
			this.imgArray[i].src = this.imagePath + 'star-off.png';
		}
	},
	
	resetRating: function(rating) {
		if ( typeof rating == 'number' ) {
			this.currentRating = rating;
		}
		for ( var i = 0; i < this.currentRating; i++ ) {
			this.imgArray[i].src = this.imagePath + 'star-on.png';
		}
		for ( var i = this.currentRating; i < this.maxRating; i++ ) {
			this.imgArray[i].src = this.imagePath + 'star-off.png';
		}
	}

}
