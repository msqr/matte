function doDisplayUserDetails(userId) {
	var requestData = 'userId='+userId;
	new Ajax.Updater(
		{success : 'main-pane'}, 
		AppState.context+'/userView.do', {
			method : 'get',
			parameters : requestData, 
			onSuccess : function(t) {
				clearChildren('main-pane');
				if ( AppState.selected.length > 0 ) {
					AppState.reset();
				}
				AppState.updateUI('user', userId);
				AppState.select(userId);
			},
			onComplete : function(t) {
				Behaviour.apply();
			}, 
			onFailure : function(t) {
				alert('[i18n]error getting user: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}
		});
}

function buildUserIndexFromXml(xmlRequest, selectedIndex, parent) {
	parent = parent || $('user-list');
	var xpath = '/x:x-data/x:x-model[1]/m:model[1]/m:search-results[1]/m:index[1]/m:index-section';
	var sectionNodes = xpathDomEval(xpath, xmlRequest.responseXML).nodeSetValue();
	clearChildren(parent);
	for ( var i = 0; i < sectionNodes.length; i++ ) {
		var indexKey = sectionNodes[i].getAttribute('index-key');
		var indexCount = sectionNodes[i].getAttribute('count');
		var spanNode = Builder.node('span',{
			'class':'a', 
			'id':'link-userindex-'+indexKey },
			indexKey);
		setupUserIndexBehaviour(spanNode, indexKey);
		var liNode = Builder.node('li');
		liNode.appendChild(spanNode);
		liNode.appendChild(Builder.node('span',{}, '('+indexCount +')'));
		parent.appendChild(liNode);
		
		if ( indexKey == selectedIndex ) {
			// build list of users for this index key
			Element.addClassName(liNode, 'selected');
			//AppState.updateUI('user', indexKey);
			if ( indexCount > 0 ) {
				var xpath = '/x:x-data/x:x-model[1]/m:model[1]/m:search-results[1]/m:user';
				var userNodes = xpathDomEval(xpath, xmlRequest.responseXML).nodeSetValue();
				var olNode = Builder.node('ol');
				for ( var j = 0; j < userNodes.length; j++ ) {
					olNode.appendChild(buildUserIndexNodeFromXml(userNodes[j]));
				}
				liNode.appendChild(olNode);
			}
		}
	}
}

function setupUserIndexBehaviour(spanNode, indexKey) {
	Event.observe(spanNode,'click',function() {
		doRefreshUserIndex(indexKey);
	},true);
}

function buildUserIndexNodeFromXml(userNode) {
	var userId = userNode.getAttribute('user-id');
	var userSpanNode = Builder.node('span', {
		'class': 'a'},
		 userNode.getAttribute('login'));
	Event.observe(userSpanNode,'click',function(userEvt) {
		doDisplayUserDetails(userId);
		AppState.reset();
		AppState.select(userId);
	},true);
	var userLiNode = Builder.node('li');
	userLiNode.appendChild(userSpanNode);
	userLiNode.appendChild(Builder.node('span',{},'('+userNode.getAttribute('name')+')'));
	return userLiNode;
}

function doRefreshUserIndex(selectedIndex) {
	var requestData = selectedIndex ? 'indexKey='+selectedIndex : '';
	new Ajax.Request(AppState.context+'/userIndex.do', {
		parameters: requestData, 
		onSuccess: function(t) {
			if ( isAjaxLogonRedirect(t) ) return;
			buildUserIndexFromXml(t, selectedIndex);
		}});
}

function doDisplayThemeDetails(themeId) {
	var requestData = 'themeId='+themeId;
	new Ajax.Updater(
		{success : 'main-pane'}, 
		AppState.context+'/themeView.do', {
			method : 'get',
			parameters : requestData, 
			onSuccess : function(t) {
				clearChildren('main-pane');
				if ( AppState.selected.length > 0 ) {
					AppState.reset();
				}
				AppState.updateUI('theme', themeId);
				AppState.select(themeId);
			},
			onComplete : function(t) {
				Behaviour.apply();
			}, 
			onFailure : function(t) {
				alert('[i18n]error getting theme: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}
		});
}

function buildThemeListFromXml(xmlRequest, parent) {
	parent = parent || $('theme-list');
	var xpath = '/x:x-data/x:x-model[1]/m:model[1]/m:theme';
	var themeNodes = xpathDomEval(xpath, xmlRequest.responseXML).nodeSetValue();
	for ( var i = 0; i < themeNodes.length; i++ ) {
		buildThemeIndexNodeFromXml(themeNodes[i], parent);
	}
}

function buildThemeIndexNodeFromXml(themeNode, parent) {
	var themeId = themeNode.getAttribute('theme-id');
	var spanNode = Builder.node('div',{
		'class':'a', 
		'id':'link-theme-'+themeId },
		themeNode.getAttribute('name'));
	Event.observe(spanNode,'click',function(evt) {
		doDisplayThemeDetails(themeId);
	},true);
	
	var liNode = Builder.node('li');
	liNode.appendChild(spanNode);
	parent.appendChild(liNode);
}

function doRefreshThemeList() {
	new Ajax.Request(AppState.context+'/themes.do', {
		parameters: '', 
		onSuccess: function(t) {
			if ( isAjaxLogonRedirect(t) ) return;
			buildThemeListFromXml(t);
		}, 
		onFailure: function(t) {
			alert('[i18n]error getting user index data: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});

}

function handleEditUserSubmit() {
	var theForm = $('edit-user-form');
	new Ajax.Request(theForm.action, {
		parameters : Form.serialize(theForm), 
		onSuccess : function(t) {
			if ( isAjaxLogonRedirect(t) ) return;
			// TODO need to handle error condition (i.e. display form again)
			doStandardAjaxResult(t);
		}, 
		onFailure : function(t) {
			alert('[i18n]error updating: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	return false;
}

function showThemePreviewPopup(el) {
	var data = el.id.match(/theme-.*(\d+)/);
	var themeId = data[1];
	el.onclick = function() {
		popup(AppState.context+'/themeResource.do?themeId='
			+themeId +'&resource=preview.png','theme-preview', 1024, 768);
	}
	Element.addClassName(el, 'a');
}

function downloadThemePak(el) {
	el.onclick = function() {
		window.location = AppState.context
			+'/downloadTheme.do?themeId='
			+AppState.selected.first();
	}
}

function deleteTheme(el) {
	el.onclick = function() {
		var themeId = AppState.selected.first();
		if ( confirm(MatteLocale.i18n('delete.theme.confirm')) ) {
			new Ajax.Request(AppState.context+'/deleteTheme.do', {
				parameters : 'themeId='+themeId, 
				onSuccess : function(t) {
					doStandardAjaxResult(t);
				}, 
				onFailure : function(t) {
					alert('[i18n]error deleting: '
						+t.status +' -- ' +t.statusText +': ' 
						+t.responseText);
				}});
		}
	}
}

var myRules = Object.extend({
	'.theme-thumbnail' : function(el) {
		if ( isBehaved(el) ) return;
		showThemePreviewPopup(el);
		makeBehaved(el);
	}
	
}, GlobalMatteBehaviours);

// Contextual links configured here
MatteStateMenuBehaviours = {
	'.link-update-user' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-user-form', 
				AppState.context+'/editUser.do', 
				'userId='+AppState.selected.first(),
				handleEditUserSubmit);
		}
	},

	'.link-delete-theme' : function (el) {
		deleteTheme(el);
	},
	
	'.link-download-theme' : function (el) {
		downloadThemePak(el);
	},
	
	'.link-update-theme' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-theme-form', 
				AppState.context+'/editTheme.do', 
				'themeId='+AppState.selected.first());
		}
	}
	
}

var initRules = Object.extend({
	body : function(el) {
		init();
	},
	
	// Non-contextal links configured here
	
	'.link-reindex-items' : function(el) {
		showStandardForm(el,'item','reindex');
	},
	
	'.link-reindex-users' : function(el) {
		showStandardForm(el,'user','reindex');
	},
	
	'.link-add-user' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-user-form',
			AppState.context+'/editUser.do',
			null,
			handleEditUserSubmit);
		}
	},
	
	'.link-add-theme' : function(el) {
		el.onclick = function() {
			showServiceDialog('edit-theme-form',
			AppState.context+'/editTheme.do');
		}
	},
	
	'.link-setup-wizard' : function(el) {
		el.onclick = function() {
			window.location = AppState.context +'/setupWizard.do';
		}
	},

	'#reindex-item-form' : showProgressInfo,

	'#reindex-user-form' : showProgressInfo,

	'#listMenuRoot': function(el) {
		'action,item,theme,user'.split(',').each(function(type) {
			document.getElementsByClassName('action-'+type,el).each(function(menu) {
				AppState.initMenu(type,menu);
			});
		});
	},
	
	'#theme-list' : function(el) {
		doRefreshThemeList();
	},
	
	'#user-list' : function(el) {
		doRefreshUserIndex();
	}
}, GlobalMatteInitBehaviours);

function init() {
	// 1: initialize locale messages
	doInitXmsg();
	
	// 2: initialize app menu...
	doInitMenu();

	// 3: unregister init rules for performance
	Behaviour.unregister(initRules);
}

var LIST_MENU = new FSMenu('LIST_MENU', true, 'visibility', 'visible', 'hidden');
Behaviour.register(initRules);
Behaviour.register(myRules);
