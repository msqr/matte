function validateDate(el) {
	var dateStr = el.value;
	if ( !dateStr.match(/\d+-\d+-\d+/) ) return null;
	var formattedDate = dateStr.replace(/(\d+)-(\d+)-(\d+)/, '$2/$3/$1');
	return formattedDate;
}

function popup(url,winName,w,h) {
	if ( !winName ) {
		winName = "matte_popup";
	}
	var opts = "";
	if ( w ) {
		opts += 'width='+w;
	}
	if ( h ) {
		if ( opts.length > 0 ) {
			opts += ',';
		}
		opts += 'height='+h;
	}
	if ( opts.length > 0 ) {
		opts += ',';
	}
	opts += ',menubar=no,scrollbars=yes,resizable=yes,toolbar=no';
	window.open(url,winName,opts);
}

function createReadOnlyFormRow(label, value) {
	var div = Builder.node('div');
	var labelDiv = Builder.node('div', {'class' : 'label'}, MatteLocale.i18n(label));
	var valueDiv = Builder.node('div', {}, value);
	div.appendChild(labelDiv);
	div.appendChild(valueDiv);
	return div;
}

function doStandardAjaxResult(xmlRequest,dialogForm,dismissCallback,msgPane,msgContentPane) {
	if ( isAjaxLogonRedirect(xmlRequest) ) return false;
	msgPane = $(msgPane||'message-pane');
	msgContentPane = $(msgContentPane||'message-content-pane');
	
	var msg = xpathDomEval('/x:x-data/x:x-messages[1]/x:msg[1]',xmlRequest.responseXML);
	var fullMessage = msg.stringValue();

	var haveError = false;
	var errorIdx = 1;
	do {
		var error = xpathDomEval('/x:x-data/x:x-errors[1]/x:error['+errorIdx+']',xmlRequest.responseXML);
		if ( error.nodeSetValue().length > 0 ) {
			fullMessage += '<div class="error">' +error.stringValue() +'</div>';
			errorIdx++;
		} else {
			haveError = false;
		}
	} while ( haveError );
	
	if ( errorIdx == 1 ) { // no errors
		if ( AppState.dialogVisible ) {
			doStandardDialogHide(); // close dialog if showing
		}
		/*if ( dialogForm != null ) {
			dialogForm.reset(); // reset form if available
		}*/
	} else if ( AppState.dialogVisible && dialogForm != null ) {
		Form.focusFirstElement(dialogForm);
	}
	
	if ( dismissCallback && errorIdx != 1 ) {
		dismissCallback = null; // only handle this if no errors
	}
	
	doSetupProgressXml(xmlRequest);

	doStandardMessageDisplay(fullMessage,dismissCallback,msgPane,msgContentPane);
	
	return true;
}

function attachClickFunction(element, f) {
	if ( element.onclick ) {
		var prevOnclick = element.onclick;
		element.onclick = function() {
			f();
			prevOnclick();
			element.onclick = prevOnclick;
		}
	} else {
		element.onclick = function() {
			f();
			element.onclick = null;
		}
	}
}

function attachCloseXFunction(pane, f) {
	var closeWidget = document.getElementsByClassName('close-x', pane);
	if ( !closeWidget || closeWidget.length < 1 ) {
		alert("Close widget not found.");
		return;
	}
	closeWidget = closeWidget[0];
	attachClickFunction(closeWidget, f);
}

/**
 * Default method for displaying a message to the user.
 */
function doStandardMessageDisplay(fullMessage,dismissCallback,msgPane,msgContentPane) {
	msgPane = $(msgPane||'message-pane');
	msgContentPane = $(msgContentPane||'message-content-pane');

	Element.update(msgContentPane,fullMessage);
	if ( dismissCallback ) {
		attachCloseXFunction(msgPane, dismissCallback);
	}
	Effect.Center(msgPane);
	if ( !Element.visible(msgPane) ) {
		new Effect.Appear(msgPane, { 
			duration: 0.5,
			afterFinish: function() {
				if ( !dismissCallback ) {
					// wait a few seconds, then hide msg
					setTimeout(function() {
						if ( Element.visible(msgPane) ) {
							new Effect.Fade(msgPane, {duration: 0.5});
						}
					}, 5000);
				}
			}
			});
	}

	doDisplayDialogShadow(msgPane);
}

function doServiceDialogDisplay(dialogPane,dialogContentPane) {
	dialogPane = $(dialogPane||'dialog-pane');
	Behaviour.register(DialogBehaviours);
	Behaviour.apply(dialogContentPane); // apply behaviours just to dialog, not entire document, for performance
	Behaviour.unregister(DialogBehaviours);
	Effect.Center(dialogPane);
	if ( !Element.visible(dialogPane) ) {
		new Effect.Appear(dialogPane, { 
			duration: 0.8
			});
		AppState.dialogVisible = true;
	}
	doDisplayDialogShadow(dialogPane);
}

function doDisplayDialogShadow(dialogPane) {
	dialogPane = $(dialogPane||'dialog-pane');
	
	var dim = Element.getDimensions(dialogPane);
	var width = dim.width - 10;
	var height = dim.height -10;
	if ( width > 0 && height > 0 ) {
		var bgUrl = AppState.context +'/shadow.do?w=' +width 
			+'&h=' +height +'&b=6&r=3&c=3289650';
		dialogPane.style.backgroundImage = 'url(' +bgUrl +')';
		dialogPane.style.backgroundRepeat = 'no-repeat';
		dialogPane.style.backgroundPosition = '-3px -3px';
	}
}

/**
 * Default method for displaying a dialog box to user. It assumes
 * the dialog content comes from some element of the ui-elements
 * element, which is assumed to hide the content.
 */
function doStandardDialogDisplay(dialogContent,dialogPane,dialogContentPane) {
	dialogContent = $(dialogContent);
	dialogPane = $(dialogPane||'dialog-pane');
	dialogContentPane = $(dialogContentPane||'dialog-content-pane');
	
	// move dialog content into dialog-pane
	dialogContent.parentNode.removeChild(dialogContent);
	
	if ( dialogContentPane.hasChildNodes() ) {
		// move current child node back to ui-elements
		returnDialogElement(dialogContentPane);
	}
	dialogContentPane.appendChild(dialogContent);
	
	doServiceDialogDisplay(dialogPane,dialogContentPane);
}

function returnDialogElement(dialogContentPane) {
	dialogContentPane = $(dialogContentPane||'dialog-content-pane');

	if ( dialogContentPane && dialogContentPane.removeChild ) {
		var uiElements = $('ui-elements');
		uiElements.appendChild(dialogContentPane.removeChild(dialogContentPane.firstChild));
	}
}

function doStandardDialogHide(dialogPane, dialogContentPane) {
	dialogPane = $(dialogPane||'dialog-pane');
	dialogContentPane = $(dialogContentPane||'dialog-content-pane');
	if ( Element.visible(dialogPane) ) {
		new Effect.Fade(dialogPane, { 
			duration: 0.4,
			afterFinish: function() {
				AppState.dialogVisible = false;
				returnDialogElement(dialogContentPane);
			}
		});
	}
}

function isAjaxLogonRedirect(request) {
	if ( request.getResponseHeader("X-Matte-Logon") == "true" ) {
		// redirect to logon page
		var logonUrl = AppState.context +'/logon.do';
		setTimeout('window.location = "'+logonUrl +'"', 250);
		return true;
	}
	return false;
}

/*
({"jobinfo": [
  {
    "amountCompleted": 1,
    "primaryInterface": "interface magoffin.matt.ma2.domain.JobInfo",
    "priority": 0,
    "ticket": 1,
    "timeCompleted": "1167785741839 03 Jan 2007 00:55:41",
    "timeStarted": "1167785741799 03 Jan 2007 00:55:41",
    "timeSubmitted": "1167785741781 03 Jan 2007 00:55:41"
  },
  {
    "amountCompleted": 1,
    "message": "Generating thumbnail 32/32",
    "primaryInterface": "interface magoffin.matt.ma2.domain.JobInfo",
    "priority": 0,
    "ticket": 65,
    "timeCompleted": "1167785900550 03 Jan 2007 00:58:20",
    "timeStarted": "1167785792640 03 Jan 2007 00:56:32",
    "timeSubmitted": "1167785792635 03 Jan 2007 00:56:32"
  }
]})
*/
function doSetupAllJobsProgress() {
	new Ajax.Request(AppState.context+'/jobs.do', {
		onSuccess : function(t) {
			if ( isAjaxLogonRedirect(t) ) return;
			var jobsInfo = eval(t.responseText);
			if ( jobsInfo ) {
				jobsInfo = jobsInfo.jobinfo;
			}
			if ( jobsInfo ) {
				for ( var i = 0; i < jobsInfo.length; i++ ) {
					if ( $('task.message-'+jobsInfo[i].ticket) ) {
						// don't add job we're already displaying
						continue;
					}
					var workInfo = new Object();
					workInfo.workTicket = jobsInfo[i].ticket;
					workInfo.workDisplayName = jobsInfo[i].displayName;
					workInfo.workMessage = jobsInfo[i].message;
					workInfo.workSubmitTime = jobsInfo[i].timeSubmitted;
					workInfo.workCompleted = jobsInfo[i].amountCompleted;
					doSetupProgress(workInfo);
				}
			}
		}, 
		onFailure : function(t) {
			alert('[i18n]error uploading: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
}

function doSetupProgress(workInfo) {
	var progressList = $('progress-list');
	var liNode = Builder.node('li',{id:'task-'+workInfo.workTicket});
	liNode.appendChild(Builder.node('div',{'class':'title'},workInfo.workDisplayName));

	liNode.appendChild(Builder.node('div',{'class':'data','id':'task.message-'+workInfo.workTicket},
		workInfo.workMessage));

	liNode.appendChild(Builder.node('div',{'class':'header'},MatteLocale.i18n('work.submitted')));
	var splitIdx = workInfo.workSubmitTime
		? workInfo.workSubmitTime.indexOf(' ')
		: -1;
	var dateVal = splitIdx == -1 
		? workInfo.workSubmitTime 
		: workInfo.workSubmitTime.substring(splitIdx+1);
	liNode.appendChild(Builder.node('div',{'class':'data'},
		splitIdx == 1 
			? new Date(dateVal).toLocaleString()
			: dateVal));
	
	liNode.appendChild(Builder.node('div',{'class':'header'},MatteLocale.i18n('work.completion')));
	liNode.appendChild(Builder.node('div',{'class':'data','id':'task.complete-'+workInfo.workTicket},
		Math.round(workInfo.workCompleted * 100.0)+'%'));
	
	progressList.appendChild(liNode);
	
	if ( workInfo.alertMessage ) {
		doStandardMessageDisplay(workInfo.alertMessage);
	}
	/*	+' (ticket = ' +workInfo.workTicket 
		+',submitted = ' +new Date(workInfo.workSubmitTime).toString()
		+')');*/
	new ProgressMonitor(workInfo.workTicket, 
		{amountComplete:workInfo.workCompleted}).start();
}

function doSetupProgressXml(xmlRequest) {
	var workInfo = getWorkInfoFromXmlJobInfo(xmlRequest);
	if ( workInfo ) {
		doSetupProgress(workInfo);
		return;
	}
	var workTicket = xpathDomEval(
		"/x:x-data/x:x-auxillary[1]/x:x-param[@key='work.ticket']",
		xmlRequest.responseXML);
	if ( workTicket.nodeSetValue().length < 1 ) {
		return;
	}
	workInfo = {workTicket:workTicket.numberValue()};

	var workDisplayName = xpathDomEval(
		"/x:x-data/x:x-auxillary[1]/x:x-param[@key='work.displayName']",
		xmlRequest.responseXML);
	if ( workDisplayName.nodeSetValue().length > 0 ) {
		workInfo.workDisplayName = workDisplayName.stringValue();
	}
	
	var workSubmitTime = xpathDomEval(
		"/x:x-data/x:x-auxillary[1]/x:x-param[@key='work.submitTime']",
		xmlRequest.responseXML);
	if ( workSubmitTime.nodeSetValue().length > 0 ) {
		workInfo.workSubmitTime = workSubmitTime.stringValue();
	}
	
	var workCompleted = xpathDomEval(
		"/x:x-data/x:x-auxillary[1]/x:x-param[@key='work.completed']",
		xmlRequest.responseXML);
	if ( workCompleted.nodeSetValue().length > 0 ) {
		workInfo.workCompleted = workCompleted.stringValue();
	}

	var workMessage = xpathDomEval(
		"/x:x-data/x:x-auxillary[1]/x:x-param[@key='work.message']",
		xmlRequest.responseXML);
	if ( workMessage.nodeSetValue().length > 0 ) {
		workInfo.workMessage = workMessage.stringValue();
	}

	doSetupProgress(workInfo);
}

function getWorkInfoFromXmlJobInfo(xmlRequest) {
	var workTicket = xpathDomEval(
			"/x:x-data/x:x-model[1]/m:job-info",
			xmlRequest.responseXML);
	if ( workTicket.nodeSetValue().length > 0 ) {
		return doSetupProgressXmlJobInfo(xmlRequest, 
			workTicket.nodeSetValue()[0]);
	}
	return null;
}

/* Support job-info model:
 * 
 * <x:x-model>
 *   <m:job-info amount-completed="0.0" displayName="Export Items" 
 *       priority="0" ticket="5" 
 *       time-submitted="1186211371125 04 Aug 2007 07:09:31">
 *     <m:message/>
 *   </m:job-info>
 * </x:x-model>
 */
function doSetupProgressXmlJobInfo(xmlRequest, workTicket) {
	workTicket.normalize();
	var msg = null;
	if ( workTicket.firstChild ) {
		msg = workTicket.firstChild;
		if ( msg.firstChild ) {
			msg = msg.firstChild.nodeValue();
		} else {
			msg = null;
		}
	}
	var workInfo = {
		workTicket: workTicket.getAttribute("ticket"),
		workDisplayName: workTicket.getAttribute("displayName"),
		workSubmitTime: workTicket.getAttribute("time-submitted"),
		workCompleted: workTicket.getAttribute("amount-completed"),
		workMessage: msg
	};
	return workInfo;
}

function showProgressInfo(theForm) {
	theForm.onsubmit = function() {
		new Ajax.Request(theForm.action, {
			parameters : Form.serialize(theForm), 
			onSuccess : function(t) {
				doStandardAjaxResult(t,theForm);
			}, 
			onFailure : function(t) {
				alert('[i18n]error uploading: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
		return false;
	};
}

function parseXmlDate(theDateString) {
	// this code adapted from http://www.merlyn.demon.co.uk/js-date3.htm#XML
	var D = theDateString.replace(
	  /^(\d{4})-(\d\d)-(\d\d)T([0-9:]*)([.0-9]*)(.)(\d\d):(\d\d)$/,
	  '$1/$2/$3 $4 $6$7$8'); // D = '2002/06/17 09:25:43 +0100'
	D = Date.parse(D); // ??
	D += 1000*RegExp.$5; // add ms
	return new Date(D); // Mon, 2002-06-17 07:25:43 UTC
}

function clearChildren(node) {
	if ( !node ) return;
	node = $(node);
	if ( !node || !node.hasChildNodes ) return;
	while ( node.hasChildNodes() ) {
		node.removeChild(node.firstChild);
	}
}

function doInitXmsg() {
	var request = new Ajax.Request(AppState.context+'/messages.json', {
		method: 'get',
		asynchronous: false,
		onException: function(xmlRequest, exception) {
			alert(exception);
		},
		onFailure: function(t) {
			alert('[i18n]error getting messages: '
				+t.status +' -- ' +t.statusText +': ' 
				+t.responseText);
		}});
	if ( isAjaxLogonRedirect(request.transport) ) return;
	var msgData = eval(request.transport.responseText);
	MatteLocale.initJson(msgData);
}

function doInitMenu() {
	//LIST_MENU.showDelay = 0;
	//LIST_MENU.switchDelay = 125;
	//LIST_MENU.hideDelay = 500;
	LIST_MENU.cssLitClass = 'highlighted';
	//LIST_MENU.showOnClick = 1;
	
	function animClipDown(ref, counter) {
		var cP = Math.pow(Math.sin(Math.PI*counter/200),0.75);
		ref.style.clip = (counter==100 ?
			((window.opera || navigator.userAgent.indexOf('KHTML') > -1) ? '':
			'rect(auto, auto, auto, auto)') :
			'rect(0, ' + ref.offsetWidth + 'px, '+(ref.offsetHeight*cP)+'px, 0)');
	};
	
	function animFade(ref, counter) {
		var f = ref.filters, done = (counter==100);
		if (f) {
			if (!done && ref.style.filter.indexOf("alpha") == -1) {
				ref.style.filter += ' alpha(opacity=' + counter + ')';
			} else if (f.length && f.alpha) {
				with (f.alpha) {
					if (done) {
						enabled = false;
					} else { 
						opacity = counter; 
						enabled = true;
					}
				}
			}
		} else {
			ref.style.opacity = ref.style.MozOpacity = counter/100.1;
		}
	};
	
	// I'm applying them both to this menu and setting the speed to 20%. Delete this to disable.
	LIST_MENU.animations[LIST_MENU.animations.length] = animFade;
	LIST_MENU.animations[LIST_MENU.animations.length] = animClipDown;
	LIST_MENU.animSpeed = 20;
	
	LIST_MENU.activateMenu("listMenuRoot");
}

function showStandardForm(link, type, kind, noFocus) {
	link.onclick = function() {
		doStandardDialogDisplay(kind+'-'+type+'-form');
		if ( !noFocus ) {
			Form.focusFirstElement(kind+'-'+type+'-form');
		}
	};
}

function showServiceDialog(contentId, url, params, formSubmit) {
	// plunk the form into the dialog container first so 
	// browser has chance to re-size for dialog later...
	clearChildren('service-dialog-container');
	new Ajax.Updater(
		{success : 'service-dialog-container'}, 
		url, {
			method : 'get',
			parameters : params || {}, 
			onSuccess : function(t) {
				clearChildren('dialog-content-pane');
			},
			onComplete : function(t) {
				if ( formSubmit ) {
					var form = $(contentId);
					if ( form ) {
						form.onsubmit = formSubmit;
					}
				}
				doStandardDialogDisplay(contentId);
			}, 
			onFailure : function(t) {
				alert('[i18n]error getting form: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}
		});
}

var workingUpdater = null;
var GlobalAjaxHandlers = {
	onCreate: function() {
		new Effect.Appear('system-working', { duration: 0.2, from: 0.0, to: 1.7 });
		//Element.show('system-working');
		if ( workingUpdater == null ) {
			workingUpdater = new WorkingPeriodicalExecutor();
		} /*else {
			workingUpdater.start();
		}*/
	},

	onComplete: function() {
		if ( Ajax.activeRequestCount == 0 ) {
			new Effect.Fade('system-working', { duration: 0.2, from: 1.0, to: 0.0 });
			//Element.hide('system-working');
			if ( workingUpdater != null ) {
				workingUpdater.stop();
			}
		}
	},
	
	onException: function(request,exception) {
		if ( Ajax.activeRequestCount == 0 ) {
			new Effect.Fade('system-working', { duration: 0.2 });
			//Element.hide('system-working');
			if ( workingUpdater != null ) {
				workingUpdater.stop();
			}
		}
		alert("An error occurred processing the request: " +exception);
		throw exception;
	}
}

function doShowHelp(helpElement) {
	if ( Element.hasClassName(helpElement, 'a') ) return;
	Element.addClassName(helpElement, 'a');
	helpElement.onclick = function() {
		new Ajax.Request(AppState.context+'/help.do', {
			parameters : 'helpId='+helpElement.id, 
			onSuccess : function(t) {
				doStandardMessageDisplay(t.responseText,function() {
					// empty to keep dialog open
				});
			}, 
			onFailure : function(t) {
				alert('[i18n]error accessing help: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
	}
}

function isBehaved(element) {
	return Element.hasClassName(element, 'behave');
}

function makeBehaved(element) {
	Element.addClassName(element, 'behave');
}

var DialogBehaviours = {
	'.close-x' : function(el) {
		if ( isBehaved(el) ) return;
		var parentNode = el.parentNode;
		var contentPane = document.getElementsByClassName('message-box', parentNode);
		if ( !contentPane || contentPane.length < 1 ) {
			contentPane = document.getElementsByClassName('dialog-box', parentNode);
		}
		el.onclick = function() {
			doStandardDialogHide(parentNode, contentPane);
		};
		makeBehaved(el);
	},
	
	'span.help' : function(el) {
		doShowHelp(el);
	}
}

var GlobalMatteBehaviours = {
	'#progress-list li' : function(el) {
		if ( isBehaved(el) ) return;
		
		if ( !el.id ) return;
		var taskId = el.id.match(/task-(\d+)/);
		if ( !taskId ) {
			alert("Unknown task ID.");
			return;
		}
		taskId = taskId[1];
		new ProgressMonitor(taskId).start();
		
		makeBehaved(el);
	}
}

var GlobalMatteInitBehaviours = Object.extend({

	'.collapsing' : function(el) {
		// first add the open arrow
		Element.cleanWhitespace(el.parentNode);
		Element.cleanWhitespace(el);
		var myHeader = el.previousSibling;
		if ( el.parentNode.nodeName.toLowerCase() == 'li' ) {
			myHeader = el.parentNode;
		}
		var arrowNode = Builder.node('img',{
			'src' : AppState.context+'/img/arrow-open.png','class':'showhide'});
		myHeader.insertBefore(arrowNode,myHeader.firstChild);
		Element.setStyle(myHeader, {cursor: 'pointer'});
		
		var isOpen = true;
		
		// create function to switch arrow from open/closed
		var replaceArrow = function() {
			arrowNode.src = AppState.context+'/img/arrow-'+(isOpen?'closed':'open') +'.png';
			isOpen = !isOpen;
		}
		
		// add the event handler for closing/opening
		var handleClick = function() {
			if ( isOpen ) {
				new Effect.BlindUp(el,{
					duration: .6,
					afterFinish : replaceArrow
				});
			} else {
				new Effect.BlindDown(el,{
					duration: .6,
					beforeStart: replaceArrow
				});
			}
		}
		
		
		if ( el.parentNode.nodeName.toLowerCase() != 'li' ) {
			myHeader.onclick = handleClick;
			if ( myHeader.nodeName.toLowerCase() == 'h3' ) {
				handleClick();
			}
		} else {
			arrowNode.onclick = handleClick;
			// also shut this arrow to start
			handleClick();
		}
	},
	
	'#app-js' : function(el) {
		if ( eval('typeof(APP_INFO)') != 'object' ) return;
		if ( APP_INFO.thumbSpec ) {
			AppState.thumbSpec = APP_INFO.thumbSpec;
		}
		if ( APP_INFO.viewSpec ) {
			AppState.viewSpec = APP_INFO.viewSpec;
		}
		if ( APP_INFO.workTicket ) {
			doSetupProgress(APP_INFO);
		} else {
			// find all available work tickets, and set those up
			doSetupAllJobsProgress();
		}
		if ( APP_INFO.alertMessage ) {
			doStandardMessageDisplay(APP_INFO.alertMessage);
		}
		if ( APP_INFO.displayAlbumId ) {
			doUpdateUI('albumId='+APP_INFO.displayAlbumId);
		} else if ( APP_INFO.displayCollectionId ) {
			doUpdateUI('collectionId='+APP_INFO.displayCollectionId);
		}
	},
	
	'#left-pane-tab' : function(el) {
		// set cursor to pointer when over tab
		el.style.cursor = 'pointer';
		
		var pane = $('left-pane');
		var main = $('main-pane');

		el.title = MatteLocale.i18n( Element.hasClassName(main, 'main-pane-normal') 
			? 'left.pane.tab.title.close' : 'left.pane.tab.title.open');
		
		// onclick: close or open the pane
		el.onclick = function() {
			var isOpen = Element.hasClassName(main, 'main-pane-normal');
			var moveBy = Element.getDimensions(pane).width -1;
			var moveToX = isOpen ? -moveBy : moveBy;
			new Effect.MoveBy( pane, 0, moveToX, {
					afterFinish : function(obj) {
						// update the title accordingly
						el.title = MatteLocale.i18n(!isOpen 
							? 'left.pane.tab.title.close' : 'left.pane.tab.title.open');
							
						// adjust main pane size
						var notMainClass = isOpen ? 'main-pane-normal' : 'main-pane-full';
						var  mainClass = isOpen ? 'main-pane-full' : 'main-pane-normal';
						if ( !Element.hasClassName(main,mainClass) ) {
							Element.addClassName(main,mainClass);
							Element.removeClassName(main,notMainClass);
						}
				
					}
				});
		}
	}
}, DialogBehaviours);

Ajax.Responders.register(GlobalAjaxHandlers);
var AppState = new MatteState();
