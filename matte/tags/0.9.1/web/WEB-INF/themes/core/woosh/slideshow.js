/* JavaScript for Woosh theme slideshow elements.
 * ===================================================================
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ===================================================================
 * $Id: slideshow.js,v 1.26 2007/09/09 02:06:46 matt Exp $
 * ===================================================================
 */

var currentInfoItem = 0;

// Define key launcher pages here
var key = new Array();
var theKeys = new Array();
theKeys ['n'] = "javascript:showNextImage();";
theKeys ['p'] = "javascript:showPrevImage();";
theKeys ['f'] = "javascript:showNextIndex();";
theKeys ['b'] = "javascript:showPrevIndex();";
theKeys ['i'] = "javascript:Element.toggle('ii-frame');";
theKeys ['h'] = "javascript:toggleDialog('help-dialog');";
theKeys ['1'] = "javascript:selectThumb(1);";
theKeys ['2'] = "javascript:selectThumb(2);";
theKeys ['3'] = "javascript:selectThumb(3);";
theKeys ['4'] = "javascript:selectThumb(4);";
theKeys ['5'] = "javascript:selectThumb(5);";

function enableKeys() {
	key = theKeys;
}

function disableKeys() {
	key = new Array();
}

var currSlide = 'a';
var nextSlide = 'b';

/* Index functions */

function showPrevIndex() {
	if ( sliderLock != 0 ) return;
	Element.setStyle('tb-lsbContainer',{
		'background-position': '0px 0px'
	});
	if ( currentIndex == 1 ) {
		showIndex(totalIndexes);
	} else {
		showIndex(currentIndex-1);
	}
}
function showNextIndex() {
	if ( sliderLock != 0 ) return;
	Element.setStyle('tb-rsbContainer',{
		'background-position': '-18px 0px'
	});
	if ( currentIndex == totalIndexes ) {
		showIndex(1);
	} else {
		showIndex(currentIndex+1);
	}
}

function slideComplete() {
	currSlide = currSlide == 'a' ? 'b' : 'a';
	nextSlide = currSlide == 'a' ? 'b' : 'a';
	sliderLock = 0;
	Element.setStyle('tb-lsbContainer',{
		'background-position': '-18px 0px'
	});
	Element.setStyle('tb-rsbContainer',{
		'background-position': '0px 0px'
	});
	preLoadPages();
}

function preLoadPages() {
	// now some trickery to load next / prev slides
	var leftIndex = currentIndex == 1 ? totalIndexes : currentIndex - 1;
	var rightIndex = currentIndex == totalIndexes ? 1 : currentIndex + 1;
	setupShowIndex(leftIndex);
	setupShowIndex(rightIndex,$('slider_page_c'));
}

function showIndex(indexNumber) {
	if (sliderLock == 0) {
		sliderLock = 1;
		var showingElem = $('slider_page_' + currSlide).style;
		var hiddenElem = $('slider_page_' +nextSlide).style;
		fromIndex = currentIndex;
		currentIndex = indexNumber;
		if (currentIndex < fromIndex) {
			setupShowIndex(indexNumber);
			hiddenElem.top = parseInt(showingElem.top) + 'px';
			hiddenElem.left = (parseInt(showingElem.left) - parseInt(showingElem.width)) + 'px';
			clearCurrentThumb();
			slideprev(fromIndex, currentIndex);
		} else if (currentIndex > fromIndex) {
			setupShowIndex(indexNumber);
			// move the next element into place
			hiddenElem.top = parseInt(showingElem.top) + 'px';
			hiddenElem.left = (parseInt(showingElem.left) + parseInt(showingElem.width)) + 'px';
	 			clearCurrentThumb();
			slidenext(fromIndex, currentIndex);
		} else {
			markCurrentThumb();
			sliderLock = 0;
		}
	}
}

function setupShowIndex(indexNumber,slideElem) {
	if ( !slideElem ) {
		// populate "other" slide with next set of images
		slideElem = $('slider_page_' +nextSlide);
	}
	var i;
	var idx = 0;
	var cnt = 0;
	for ( i = (indexNumber-1) * pageSize +1; cnt < pageSize && i < imageData.length; i++, cnt++, idx++ ) {
		//alert(i +"," +idx +"," +cnt);
		var data = imageData[i];
		var div = slideElem.childNodes[idx];
		while ( !div.tagName || div.tagName.toUpperCase() != 'DIV' ) {
			div = div.nextSibling;
			idx++;
		}
		var a = div.firstChild;
		while ( !a.tagName || a.tagName.toUpperCase() != 'A' ) {
			a = a.nextSibling;
		}
		a.setAttribute("href","javascript:displayImage(" +i +")");
		try {
			while ( a.hasChildNodes() ) {
				a.removeChild(a.firstChild);
			}
		} catch (err) {
			alert("oops: " +err);
		}
		var url = webContext +"/media.do?id="+data[0]
			+"&albumKey=" +albumKey
			+"&size=THUMB_NORMAL&quality=GOOD";
		var alt = data[4] ? data[4] : data[3];
		//alert("appendImg: " +url);
		appendImg(a,url,'',alt,'','','tb-thumbnail');
	}
	if ( cnt < pageSize ) {
		//alert(slideElem.id + ": " +cnt);
		while ( cnt < pageSize ) {
			var div = slideElem.childNodes[idx];
			while ( !div.tagName || div.tagName.toUpperCase() != 'DIV' ) {
				div = div.nextSibling;
				idx++;
			}
			var a = div.firstChild;
			while ( !a.tagName || a.tagName.toUpperCase() != 'A' ) {
				a = a.nextSibling;
			}
			a.removeAttribute("href");
			try {
				while ( a.hasChildNodes() ) {
					a.removeChild(a.firstChild);
				}
			} catch ( err ) {
				alert("oops! " +err);
			}
			cnt++;
			idx++;
		}
	}
}

function slideprev(fromID, toID) {
	if (window.slideright) clearTimeout(slideright);

	var fromMenu = $('slider_page_' + currSlide).style;
	var toMenu = $('slider_page_' + nextSlide).style;

	if (distanceMoved >= slider_width * 0.85) {
		if (distanceMoved >= slider_width * 0.95) {
		targetStep = max_step / 4;
		} else {
		targetStep = max_step / 2;
		}
		slider_step = (targetStep > min_step)?targetStep:min_step;
	}

	if (slider_width < (distanceMoved + slider_step)) {
		slider_step = slider_width - distanceMoved;
	}

	fromMenu.left = (parseInt(fromMenu.left) + slider_step) + 'px';
	toMenu.left = (parseInt(toMenu.left) + slider_step) + 'px';

	slideright = setTimeout("slideprev(" + fromID + "," + toID+ ");", slider_speed);

	if (distanceMoved < slider_width) {
		distanceMoved += slider_step;
	} else {
		clearTimeout(slideright);
		distanceMoved = 0;
		slider_step = max_step;

		fromMenu.top = offscreenTop;
		fromMenu.left = offscreenLeft;

		updateDots(fromID, toID);
		markCurrentThumb();
		slideComplete();
	}
}

function updateDots(fromID, toID) {
	$('index'+fromID).src = webContext+'/themeResource.do?themeId=' +themeId 
		+'&resource=img/tb_13.gif';
	$('index'+toID).src = webContext+'/themeResource.do?themeId=' +themeId 
		+'&resource=img/tb_12.gif';
}

function slidenext(fromID, toID) {
	if (window.slideleft) clearTimeout(slideleft);
	//clearCurrentThumb();

	var fromMenu = $('slider_page_' + currSlide).style;
	var toMenu = $('slider_page_' + nextSlide).style;

	if (distanceMoved >= slider_width * 0.85) {
		if (distanceMoved >= slider_width * 0.95) {
		targetStep = max_step / 4;
		} else {
		targetStep = max_step / 2;
		}
		slider_step = (targetStep > min_step)?targetStep:min_step;
	}

	if (slider_width < (distanceMoved + slider_step)) {
		slider_step = slider_width - distanceMoved;
	}

	fromMenu.left = (parseInt(fromMenu.left) - slider_step) + 'px';
	toMenu.left = (parseInt(toMenu.left) - slider_step) + 'px';

	slideleft = setTimeout("slidenext(" + fromID + "," + toID+ ");", slider_speed);

	if (distanceMoved < slider_width) {
		distanceMoved += slider_step;
	} else {
		clearTimeout(slideleft);
		distanceMoved = 0;
		slider_step = max_step;

		fromMenu.top = offscreenTop;
		fromMenu.left = offscreenLeft;

		updateDots(fromID, toID);
		markCurrentThumb();
		slideComplete();
	 	}
}

function markCurrentThumb() {
	var pageNumber = parseInt(((currentImage - 1) / pageSize) + 1);
	var markStyle = $('tb-thumbnailMark').style;
	if (pageNumber == currentIndex) {
		var thumbNumber = ((currentImage - 1) % pageSize) + 1;
		markStyle.left = (52 + ((thumbNumber-1) * 92)) + 'px';
		markStyle.display = 'block';
	} else {
		markStyle.display = 'none';
	}
}

function clearCurrentThumb() {
	var markStyle = $('tb-thumbnailMark').style;
	markStyle.display = 'none';
}

function displayImage(imageNumber) {
	workingUpdaterAjaxHandler.manualStart();
	currentImage = imageNumber;
	
	var newImg = document.createElement("img");
	var newImgUrl = webContext +"/media.do?id="+imageData[imageNumber][0]
			+"&albumKey=" +albumKey
			+"&size=" +imgSize 
			+"&quality=" +imgCompress;
	var title = imageData[imageNumber][4]
		? imageData[imageNumber][4]
		: imageData[imageNumber][3];
	newImg.title = title;
	newImg.alt = title;
	newImg.onload = function() {
		handleDisplayImageLoad(newImg, imageNumber);
	}
	newImg.src = newImgUrl;
	
	var pageNumber = parseInt(((imageNumber - 1) / pageSize) + 1);
	showIndex(pageNumber);

	/* FIXME var commentForm = document.forms["addCommentForm"];
	commentForm.elements["mitem"].value = imageData[imageNumber][0];
	*/
}

function handleDisplayImageLoad(newImg, imageNumber) {
	var imgContainer = $('image-frame');
	
	// if existing item is an QT object, call Stop() on it
	if ( $('image-content') && typeof($('image-content').Stop) == 'function' ) {
		$('image-content').Stop();
	}
	
	// clear out existing item
	while ( imgContainer.hasChildNodes() ) {
		imgContainer.removeChild(imgContainer.firstChild);
	}
	
	if ( typeof(AC) == "object" 
			&& imageData[imageNumber][9].indexOf("video") == 0
			&& haveQuickTime ) {
 		var qtUrl = webContext +'/media.do?id='+imageData[imageNumber][0]
			+'&albumKey=' +albumKey
			+'&original=true';
		
		var width = imageData[imageNumber][1];
		var height = imageData[imageNumber][2];
		
		if ( !width || !height ) {
			width = mediaSizes[imgSize].width;
			height = mediaSizes[imgSize].height;
		}
		
		var qtEmbed = AC.Quicktime.packageMovie('image-content', qtUrl, {
				width: width,
				height: height + 20,
				autoplay: true});
		$('image-frame').appendChild(qtEmbed);
	} else {
		newImg.id = 'image-content';
		imgContainer.appendChild(newImg);
	}
	

	// update image info
	updateImageInfo(imageNumber)
}

function updateImageInfo(imageNumber) {
	Element.addClassName('ii-imageMetaFrame', 'updating');
	new Ajax.Updater(
		{success : 'ii-imageMetaFrame'}, 
		webContext +'/viewMediaItemInfo.do', 
		{
			method : 'get',
			parameters : "albumKey="+albumKey +"&itemId=" +imageData[imageNumber][0] 
				+"&themeId=" +themeId, 
			onFailure : function(t) {
				alert('Error getting metadata: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			},
			onSuccess : function(t) {
				currentInfoItem = imageNumber;
				Element.removeClassName('ii-imageMetaFrame', 'updating');
				setTimeout('updateInfoBehaviour()',100); // work around problem in FF
			},
			onComplete : function() {
				workingUpdaterAjaxHandler.manualStop();
			}
		});
}

function updateInfoBehaviour() {
	Behaviour.apply($('ii-imageMetaFrame'));
}

function clearChildren(node) {
	if ( !node ) return;
	node = $(node);
	if ( !node || !node.hasChildNodes ) return;
	while ( node.hasChildNodes() ) {
		node.removeChild(node.firstChild);
	}
}

function doDisplayDialogShadow(dialogPane) {
	dialogPane = $(dialogPane||'dialog-pane');
	
	var dim = Element.getDimensions(dialogPane);
	var width = dim.width - 10;
	var height = dim.height -10;
	if ( width > 0 && height > 0 ) {
		var bgUrl = webContext +'/shadow.do?w=' +width 
			+'&h=' +height +'&b=6&r=3&c=3289650';
		dialogPane.style.backgroundImage = 'url(' +bgUrl +')';
		dialogPane.style.backgroundRepeat = 'no-repeat';
		dialogPane.style.backgroundPosition = '-3px -3px';
	}
}

function doServiceDialogDisplay(dialogPane,dialogContentPane) {
	dialogPane = $(dialogPane||'dialog-pane');
	Behaviour.apply(dialogContentPane); // apply behaviours just to dialog, not entire document, for performance
	centerWidget(dialogPane);
	if ( !Element.visible(dialogPane) ) {
		Element.show(dialogPane);
		centerWidget(dialogPane);
	}
	doDisplayDialogShadow(dialogPane);
}

function doServiceDialogHide(dialogPane,dialogContentPane) {
	dialogPane = $(dialogPane||'dialog-pane');
	if ( Element.visible(dialogPane) ) {
		Element.hide(dialogPane);
	}
}

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

function doStandardDialogHide(dialogPane, dialogContentPane) {
	dialogPane = $(dialogPane||'dialog-pane');
	dialogContentPane = $(dialogContentPane||'dialog-content-pane');
	if ( Element.visible(dialogPane) ) {
		Element.hide(dialogPane);
	}
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


function selectThumb(thumbNumber) {
	displayImage(((currentIndex - 1) * pageSize) + thumbNumber);
}

/* Image functions */

function showNextImage() {
	if (currentImage == totalImages) {
	displayImage(1);
	} else {
	displayImage(currentImage + 1);
	}
}
function showPrevImage() {
	if (currentImage == 1) {
	displayImage(totalImages);
	} else {
	displayImage(currentImage - 1);
	}
}

/********** MISC FUNCTIONS **********/

var showingTable = new Array();
showingTable['ii-frame'] = 1;
showingTable['ii-EXIF'] = 1;
showingTable['ii-downloadOptions'] = 1;
showingTable['ii-comments'] = 1;

function ma_resize_win() {
	//if ( $('msg-box-dialog') ) {
	//	msgBoxCenterDialog();
	//}
	//addCommentCenterDialog();
	//helpCenterDialog();
}

function setupIndexPageArrows() {
	Event.observe('tb-lsbContainer', 'click', showPrevIndex);
	Element.setStyle('tb-lsbContainer', {
		background: 'transparent url('+webContext+'/themeResource.do?themeId='+themeId
			+'&resource=img/tb_08.gif) no-repeat -18px 0px',
		cursor: 'pointer'
	});
	Event.observe('tb-rsbContainer', 'click', showNextIndex);
	Element.setStyle('tb-rsbContainer', {
		background: 'transparent url('+webContext+'/themeResource.do?themeId='+themeId
			+'&resource=img/tb_10.gif) no-repeat 0px 0px',
		cursor: 'pointer'
	});
}

function setupHelpDialog() {
	var dialog = $('help-dialog');
	$A(document.getElementsByClassName('close-x', dialog)).each(function(el) {
		Element.setStyle(el, {
			background: 'transparent url('+webContext+'/themeResource.do?themeId='+themeId
				+'&resource=img/close-x.png) no-repeat 0px 0px',
			cursor: 'pointer'
		});
		if ( !el.onclick ) {
			el.onclick = function() {
				Element.toggle(dialog);
			}
		}
	});
}

function toggleDialog(dialog) {
	var dialogPane = $(dialog)
	if ( !Element.visible(dialogPane) ) {
		Element.show(dialogPane);
		centerWidget(dialogPane);
	} else {
		Element.hide(dialogPane);
		return;
	}
	
	var dim = Element.getDimensions(dialogPane);
	var width = dim.width - 10;
	var height = dim.height -10;
	if ( width > 0 && height > 0 ) {
		var bgUrl = webContext +'/shadow.do?w=' +width 
			+'&h=' +height +'&b=6&r=3&c=3289650';
		Element.setStyle(dialogPane, {
			'backgroundImage': 'url(' +bgUrl +')',
			'backgroundRepeat': 'no-repeat',
			'backgroundPosition': '-3px -3px'
		});
	}
}

function init() {
	Event.observe(window, 'resize', ma_resize_win);
	setupIndexPageArrows();

	var imgNum = initialId > 0 ? getItemPosition(initialId) : 1;
	if ( imgNum > 0 ) {
		if ( imgNum <= pageSize ) {
			setupShowIndex(1,$('slider_page_' + currSlide));
			displayImage(imgNum);
		} else {
			displayImage(imgNum);
		}
	}
	if ( initialId < 0 ) {
		setupShowIndex(1,$('slider_page_' + currSlide));
		preLoadPages();
	}
	if ( $('msg-box-dialog') ) {
		msgBoxInitDialog();
		msgBoxCenterDialog();
		msgBoxToggleDialog();
	}

	if ( $('help-dialog') ) {
		setupHelpDialog();
	}

	enableKeys();
}

/**
 * Try to find an item by it's ID, if not found return the first item.
 */
function getItemPosition(itemId) {
	// loop from 1 to end...
	var i;

	for ( i = 1; i < imageData.length; i++ ) {
		if ( imageData[i][0] == itemId ) {
			return i;
		}
	}
	return 1;
}

/**
 * Get the name for a media item, falling back to path if name not defined.
 */
function getItemName(idx) {
	var data = imageData[idx];
	if (data[4].length > 0 ) {
		return data[4];
	} else {
		return data[3];
	}
}

/**
 * Create HTML <img> element.
 */
function appendImg(parent,href,id,alt,width,height,styleClass)
{
	var img = document.createElement("img");

	img.setAttribute("src",href);
	if ( id ) {
		img.setAttribute("id",id);
	}
	if ( alt ) {
		img.setAttribute("alt",alt);
	}
	if ( width ) {
		img.setAttribute("width",width);
	}
	if ( height ) {
		img.setAttribute("height",height);
	}
	if ( styleClass ) {
		img.className = styleClass;
	}
	parent.appendChild(img);
	return img;
}

/**
 * Return a Map with 'width' and 'height' parameters set to the 
 * output size of a media item set to a specific size.
 * 
 * @param size the desired size key
 * @param origWidth the item natural width
 * @param origHeight the item natural height
 * @return Map
 */
function getMediaItemWidthHeight(size,origWidth,origHeight,setWidth,setHeight) {
	var width;
	var height;

	if ( setWidth && setHeight ) {
		width = setWidth;
		height = setHeight;
	} else {

		var maxWidthF;
		var maxHeightF;

		// calculate max dimensions
		if ( size == 'huge' ) {
			maxWidthF = 1600;
			maxHeightF = 1200;
		} else if ( size == 'big' ) {
			maxWidthF = 1024;
			maxHeightF = 768;
		} else if ( size == 'medium' ) {
			maxWidthF = 800;
			maxHeightF = 600;
		} else if ( size == 'normal' ) {
			maxWidthF = 640;
			maxHeightF = 480;
		} else if ( size == 'small' ) {
			maxWidthF = 480;
			maxHeightF = 360;
		} else if ( size == 'tiny' ) {
			maxWidthF = 320;
			maxHeightF = 240;
		} else if ( size == 'thuge' ) {
			maxWidthF = 240;
			maxHeightF = 180;
		} else if ( size == 'tbig' ) {
			maxWidthF = 160;
			maxHeightF = 120;
		} else if ( size == 'tnormal' ) {
			maxWidthF = 120;
			maxHeightF = 90;
		} else if ( size == 'tsmall' ) {
			maxWidthF = 80;
			maxHeightF = 60;
		} else {
			// treat as normal
			maxWidthF = 640;
			maxHeightF = 480;
		}

		// don't scale larger than natural size
		if ( maxWidthF > origWidth ) {
			maxWidthF = origWidth;
		}
		if ( maxHeightF > origHeight ) {
			maxHeightF =origHeight;
		}

		var imageRatio = origWidth / origHeight;

		if ( (maxWidthF / imageRatio) > maxHeightF ) {
			width = Math.round(maxHeightF * imageRatio);
		} else {
			width = maxWidthF;
		}
		height = Math.round(width / imageRatio);

		// double check for rounding
		if ( height > maxHeightF ) {
			height = maxHeightF;
		}
		if ( width > maxWidthF ) {
			width = maxWidthF;
		}
	}

	//alert("w x h = " +width +" x " +height);
	
	var data = new Array();
	data['width'] = new Number(width);
	data['height'] = new Number(height);
	
	return data;
}

function centerWidget(element) {
	try {
		element = $(element);
	} catch(e) {
		return;
	}

	var my_width	= 0;
	var my_height = 0;

	if ( typeof( window.innerWidth ) == 'number' )	{
		my_width	= window.innerWidth;
		my_height = window.innerHeight;
	} else if ( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
		my_width	= document.documentElement.clientWidth;
		my_height = document.documentElement.clientHeight;
	} else if ( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
		my_width	= document.body.clientWidth;
		my_height = document.body.clientHeight;
	}

	//element.style.position = 'absolute';
	//element.style.display	= 'block';
	//element.style.zIndex	 = 99;

	var scrollY = 0;

	if ( document.documentElement && document.documentElement.scrollTop )
	{
		scrollY = document.documentElement.scrollTop;
	}
	else if ( document.body && document.body.scrollTop )
	{
		scrollY = document.body.scrollTop;
	}
	else if ( window.pageYOffset )
	{
		scrollY = window.pageYOffset;
	}
	else if ( window.scrollY )
	{
		scrollY = window.scrollY;
	}

	var elementDimensions = Element.getDimensions(element);

	var setX = ( my_width	- elementDimensions.width	) / 2;
	var setY = ( my_height - elementDimensions.height ) / 2 + scrollY;

	setX = ( setX < 0 ) ? 0 : setX;
	setY = ( setY < 0 ) ? 0 : setY;

	element.style.left = setX + "px";
	element.style.top	= setY + "px";

}

function showDownloadAlbumDialog() {
	var itemParameters = 'albumKey=' +albumKey +'&direct=true';
	if ( userKey && browseMode ) {
		itemParameters += '&userKey=' +userKey +'&mode=' +browseMode;
	}
	showServiceDialog('item-download-form',
		webContext +'/downloadItems.do',
		itemParameters, handleDownloadItemsSubmit);
}

function handleDownloadItemsSubmit() {
	var theForm = $('item-download-form');
	doServiceDialogHide();
	return true;
}

function showAddCommentDialog() {
	var itemParameters = 'itemId=' +imageData[currentInfoItem][0];
	showServiceDialog('add-comment-form',
		webContext +'/addComment.do',
		itemParameters, handleAddCommentSubmit);
}

function handleAddCommentSubmit() {
	var theForm = $('add-comment-form');
	new Ajax.Request(theForm.action, {
		parameters : Form.serialize(theForm), 
		onSuccess : function(t) {
			//if ( doStandardAjaxResult(t) ) {
			updateImageInfo(currentInfoItem);
			doServiceDialogHide();
		}});
	return false;
}

var WorkingPeriodicalExecuter = Class.create();
WorkingPeriodicalExecuter.prototype = {
		initialize: function(callback, frequency) {
		this.callback = callback;
		this.frequency = frequency;
		this.currentlyExecuting = false;
		this.working = $('system-working');
		this.originalWorkingValue = this.working.firstChild.nodeValue;
		this.registerCallback();
	},

	registerCallback: function() {
		this.timer = setInterval(this.onTimerEvent.bind(this), this.frequency * 1000);
	},
	
	start: function() {
		if ( !this.timer ) {
			this.registerCallback();
		}
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

function isAjaxLogonRedirect(request) {
	if ( request.getResponseHeader("X-Matte-Logon") == "true" ) {
		return true;
	}
	return false;
}

function popup(url,winName,w,h) {
	if ( !winName ) {
		winName = "woosh_popup";
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
	return window.open(url,winName,opts);
}

var PopupHandlerClass = Class.create();
PopupHandlerClass.prototype = {
	initialize: function() {
		// nothing
	},
	
	complete: function(win) {
		if ( win && win.name == 'rating_logon' ) {
			if ( CurrStarRating != null ) {
				win.close();
				
				setTimeout('CurrStarRating.setRatingAfterLogin()', 250);
			} else {
				alert("Error: CurrStarRating is null");
			}
		}
	}
}

var PopupHandler = new PopupHandlerClass();

var StarRating = Class.create();
StarRating.prototype = {
	initialize: function(container, currentRating, objectId, imagePath, maxRating) {
		this.container = container;
		this.currentRating = currentRating;
		this.objectId = objectId;
		this.maxRating = maxRating || 5;
		this.imgArray = new Array();
		this.wsUrl = webContext+'/setMediaItemRating.do';
		this.imagePath = webContext+'/img/';
		this.loggedOutRating = -1;
		
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
		var requestData = 'rating='+rating
			+'&itemIds=' +imageData[currentImage][0];
		var me = this;
		new Ajax.Request(this.wsUrl, {
			parameters: requestData, 
			onSuccess: function(t) {
				if ( isAjaxLogonRedirect(t) ) {
					me.loggedOutRating = me.currentRating;
					me.resetRating(0);
					popup(webContext+'/logonPop.do?errorMsg=login.setrating', 'rating_logon', 450, 200);
				}
			}, 
			onFailure: function(t) {
				alert('[i18n]error setting rating: '
					+t.status +' -- ' +t.statusText +': ' 
					+t.responseText);
			}});
	},
	
	setRatingAfterLogin: function() {
		if ( this.loggedOutRating > 0 ) {
			var theRating = this.loggedOutRating;
			this.loggedOutRating = -1;
			this.highlightRating(theRating);
			this.setRating(theRating);
		}
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


var workingUpdater = null;
var workingUpdaterAjaxHandler = {

	manualStarts: 0,
	
	manualStart: function() {
		this.manualStarts++;
		this.onCreate();
	},
	
	manualStop: function() {
		if ( this.manualStarts > 0 ) {
			this.manualStarts--;
		}
		this.onComplete();
	},
	
	onCreate: function() {
		Element.show('system-working');
		if ( workingUpdater == null ) {
			workingUpdater = new WorkingPeriodicalExecuter(function () {
				new Insertion.Bottom('system-working','.');
			}, 2);
		} else {
			workingUpdater.start();
		}
	},

	onComplete: function() {
		if ( this.manualStarts == 0 && Ajax.activeRequestCount == 0 ) {
			Element.hide('system-working');
			if ( workingUpdater != null ) {
				workingUpdater.stop();
			}
		}
	},
	
	onException: function(xmlRequest, exception) {
		if ( this.manualStarts == 0 && Ajax.activeRequestCount == 0 ) {
			Element.hide('system-working');
			if ( workingUpdater != null ) {
				workingUpdater.stop();
			}
		}
		alert("An exception occurred processing the request: " +exception);
	}
}

var wooshBehaviours = {
	'.frame-t' : function(el) {
		Element.setStyle(el, {
			background: '#fff url('+webContext+'/themeResource.do?themeId='+themeId
				+'&resource=img/tb_05.gif) repeat-x left top'
		});
	},
	
	'.frame-ml' : function(el) {
		Element.setStyle(el, {
			background: '#fff url('+webContext+'/themeResource.do?themeId='+themeId
				+'&resource=img/tb_18.gif)'
		});
	},
	
	'.frame-mr'	: function(el) {
		Element.setStyle(el, {
			background: '#fff url('+webContext+'/themeResource.do?themeId='+themeId
				+'&resource=img/tb_23.gif) repeat-y'
		});
	},
	
	'.frame-b' : function(el ) {
		Element.setStyle(el, {
			background: '#fff url('+webContext+'/themeResource.do?themeId='+themeId
				+'&resource=img/tb_27.gif) repeat-x left bottom'
		});
	},
	
	'.close-x' : function(el) {
		if ( el.onclick ) return;
		var parentNode = el.parentNode;
		var contentPane = document.getElementsByClassName('message-box', parentNode);
		if ( !contentPane || contentPane.length < 1 ) {
			contentPane = document.getElementsByClassName('dialog-box', parentNode);
		}
		Element.setStyle(el, {
			background: 'transparent url('+webContext+'/themeResource.do?themeId='+themeId
				+'&resource=img/close-x.png) no-repeat 0px 0px',
			cursor: 'pointer'
		});
		el.onclick = function() {
			doStandardDialogHide(parentNode, contentPane);
		};
	},
	
	'#download-album-link' : function(el) {
		el.onclick = showDownloadAlbumDialog;
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
	},
	
	'#add-comment-link' : function(el) {
		el.onclick = showAddCommentDialog;
	},
	
	'textarea' : function(el) {
		el = $(el);
		if ( el.hasClassName('behaved') ) return;
		// remove blank whitespace from textarea elements, because of XSL putting
		// empty space in them so the don't end up like <textarea/>
		if ( $F(el).search(/^[\s\n]+$/) == 0 ) {
			el.clear();
		}
		el.onfocus = disableKeys;
		el.onblur = enableKeys;
		// the following throws exception in FF
		//el.observe('focus', 'disableKeys');
		//el.observe('blur', 'enableKeys');
		el.addClassName('behaved');
	},
	
	'input[type=text]' : function(el) {
		el = $(el);
		if ( el.hasClassName('behaved') ) return;
		el.onfocus = disableKeys;
		el.onblur = enableKeys;
		el.addClassName('behaved');
	},
	
	'#link-toggle-info' : function(el) {
		el.onclick = function() {
			Element.toggle('ii-frame');
		}
	},
	
	'#link-toggle-help' : function(el) {
		el.onclick = function() {
			toggleDialog('help-dialog');
		}
	},
	
	'#item-rating' : function(el) {
		if ( typeof Builder == 'object' ) {
			Builder.xmlMode = xmlMode;
		}
		// get rating data... my,total,count
		var data = el.firstChild.nodeValue.split(',',3);
		el.removeChild(el.firstChild);
		CurrStarRating = new StarRating(el,0);
		if ( Number(data[0]) != Number.NaN ) {
			CurrStarRating.resetRating(Number(data[0]));
		}
		/*if ( Number(data[1]) > 0 ) {
			el.appendChild(document.createTextNode(
				' (' +new Number(data[1] / data[2]).toFixed(1) + ' - ' +data[2] +')'));
		}*/
		$('item-rating-container').show();
	}
	
}

var CurrStarRating = null;
Ajax.Responders.register(workingUpdaterAjaxHandler);
Behaviour.register(wooshBehaviours);
Event.observe(window, 'load', init);
