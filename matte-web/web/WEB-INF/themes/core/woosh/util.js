/* JavaScript for global utilities.
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
 * $Id: util.js,v 1.2 2006/12/01 22:52:20 matt Exp $
 * ===================================================================
 */

var DEFAULT_LANG = 'en';

/**
 * Get a JavaScript message variable for a given language.
 *
 * This method will attempt to return the variable
 * associated with 'key' and 'lang'. The 'lang' format
 * can be 'en_US' or just 'en'. If the variable is found
 * it's value is returned, otherwise an empty string
 * is returned.
 *
 * If 'params' is passed, it should be an array of String
 * objects, which will be substituted into the message
 * value by index, starting at 1, with brace delimiters.
 * For example "The {1} value is required" along
 * with an array ['name'] would result in the message
 * "The name value is required."
 *
 * This is a crude way to store internationalized messages
 * for JavaScript functions.
 *
 * GLOBAL VARIABLE: myLang
 *
 * @param key the JavaScript variable name, without langugage
 * @param lang the desired language (optional)
 * @param params an array of parameter values
 *
function getMessage(key,lang,params) {
	if ( !lang ) {
		lang = myLang;
	} else {
		if ( typeof(lang) != 'string' ) {
			if ( lang.length ) {
				// already an array
				params = lang;
			} else {
				params = new Array();
				params[0] = lang;
			}
			lang = myLang;
		}
	}
	if ( !lang ) {
		alert("! Language not available");
		return '';
	}
	var res;
	if ( eval('typeof ' +key+'_'+lang) != 'undefined' ) {
		res = eval(key+'_'+lang);
	} else {
		var idx = lang.indexOf('_')
		if ( idx > 0 ) {
			if ( eval('typeof ' +key+'_'+lang.substring(0,idx)) != 'undefined' ) {
				res = eval(key+'_'+lang.substring(0,idx));
			}
		}
	}
	if ( !res ) {
		res = '';
	} else if ( params ) {
		var i = 0;
		for ( i = 0; i < params.length; i++ ) {
			res = res.replace(new RegExp('\\{'+(i+1)+'\\}','g'),params[i]);
		}
	}
	return res;
}*/

/* =================================================================
 * HTML DOM HELPER METHODS
 * ================================================================= */

/**
 * Create HTML <a> element.
 */
function appendA(parent,href,linkText)
{
	var a = document.createElement("a");
	a.setAttribute("href",href);
	a.appendChild(document.createTextNode(linkText));
	parent.appendChild(a);
	return a;
}

/**
 * Create HTML <button> element.
 */
function appendButton(parent,click,title,value)
{
	var but = document.createElement("button");
	but.setAttribute("type","button");
	if ( value ) {
		but.setAttribute("value",value);
	}
	if ( click ) {
		but.setAttribute("onClick",click);
	}
	but.appendChild(document.createTextNode(title));
	parent.appendChild(but);
	return but;
}

/**
 * Create HTML <div> element.
 */
function appendDiv(parent,text,id,styleClass)
{
	var newDiv = document.createElement("div");
	newDiv.appendChild(document.createTextNode(text));
	parent.appendChild(newDiv);

	if ( id ) {
		newDiv.setAttribute("id",id);
	}

	if ( cssClass ) {
		newDiv.className = styleClass;
	}

	return newDiv;
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
 * This method was the result from many a Howard Dean like yowl of
 * frustration trying to get IE to behave. Only after paying for
 * a subscription to the Expert Exchange, where the problem was posted
 * the the answers hidden, did I find out why IE didn't honor the
 * DOM-created form elements.
 */
function createFormElementTakingIntoConsiderationStupidIE(element,name) {
	var formObject;
	if ( navigator.appName.toLowerCase().indexOf("microsoft") < 0 ) {
		// this is the sane way
		formObject = document.createElement(element);
		formObject.setAttribute("name",name);
	} else {
		// this is the absurd way
		var tmp = "<" +element +" name=\"" +name +"\">";
		//alert("INSANE: " +tmp);
		formObject = document.createElement(tmp);
	}
	return formObject;
}

/**
 * Create HTML <input type="checkbox"> element.
 */
function appendFormCheckboxElement(parent,name,value,id,onclick)
{
	var input = createFormElementTakingIntoConsiderationStupidIE("input",name);
	input.setAttribute("type","checkbox");
	if ( value != null ) {
		input.setAttribute("value",value);
	}
	if ( id ) {
		input.setAttribute("id",id);
	}
	if ( onclick ) {
		input.setAttribute("onclick",onclick);
	}

	parent.appendChild(input);
	return input;
}

/**
 * Create HTML <input type="hidden"> element.
 */
function appendFormHiddenElement(parent,name,value,id)
{
	var input = createFormElementTakingIntoConsiderationStupidIE("input",name);
	input.setAttribute("type","hidden");
	if ( value != null ) {
		input.setAttribute("value",value);
	}
	if ( id ) {
		input.setAttribute("id",id);
	}
	parent.appendChild(input);
	return input;
}

/**
 * Create HTML <input type="radio"> element.
 */
function appendFormRadioElement(parent,name,value,id)
{
	var input = createFormElementTakingIntoConsiderationStupidIE("input",name);
	input.setAttribute("type","radio");
	if ( value != null ) {
		input.setAttribute("value",value);
	}
	if ( id ) {
		input.setAttribute("id",id);
	}

	parent.appendChild(input);
	return input;
}

/**
 * Create HTML <textarea> element.
 */
function appendFormTextarea(parent,name,value,id)
{
	var ta = createFormElementTakingIntoConsiderationStupidIE("textarea",name);
	if ( value != null ) {
		ta.setAttribute("value",value);
	}
	if ( id ) {
		input.setAttribute("id",id);
	}

	if ( value != null ) {
		ta.appendChild(document.createTextNode(value));
	}

	parent.appendChild(ta);
	return ta;
}

/**
 * Create HTML <input type="text"> element.
 */
function appendFormTextElement(parent,name,value,id)
{
	var input = createFormElementTakingIntoConsiderationStupidIE("input",name);
	input.setAttribute("type","text");
	if ( value != null ) {
		input.setAttribute("value",value);
	}
	if ( id ) {
		input.setAttribute("id",id);
	}

	parent.appendChild(input);
	return input;
}

/**
 * Create HTML <span> element.
 */
function appendSpan(parent,text,styleClass)
{
	var newData = document.createElement("span");
	if ( text ) {
		newData.appendChild(document.createTextNode(text));
	}
	if ( styleClass ) {
		newData.className = styleClass;
	}
	parent.appendChild(newData);
	return newData;
}

/**
 * Create HTML <table> and <tbody> elements, return <tbody>.
 */
function appendTable(parent,styleClass)
{
	var newTable = document.createElement("table");
	if ( styleClass ) {
		newTable.className = styleClass;
	}
	var tbody = document.createElement("tbody");
	newTable.appendChild(tbody);
	parent.appendChild(newTable);
	return tbody;
}

/**
 * Create HTML <td> element.
 */
function appendTd(row,text,colspan)
{
	var newData = document.createElement("td");
	if ( text ) {
		newData.appendChild(document.createTextNode(text));
	}
	if ( colspan ) {
		newData.colSpan = colspan;
	}
	row.appendChild(newData);
	return newData;
}

/**
 * Create HTML <th> element.
 */
function appendTh(row,text,colspan)
{
	var newHead = document.createElement("th");
	if ( text ) {
		newHead.appendChild(document.createTextNode(text));
	}
	if ( colspan ) {
		newHead.colSpan = colspan;
	}
	row.appendChild(newHead);
	return newHead;
}

/**
 * Create HTML <tr> element.
 */
function appendTr(tbody,styleClass)
{
	var newRow = document.createElement("tr");
	if ( styleClass ) {
		newRow.setAttribute("class",styleClass);
	}
	tbody.appendChild(newRow);
	return newRow;
}

/* =================================================================
 * OTHER DOM HELPER METHODS
 * ================================================================= */

/**
 * Set the inner text of a node.
 *
 * If 'theNode' has children, the first child will be replaced by a new text node,
 * otherwise a new child text node will be appended to 'theNode'.
 */
function setInnerText(theNode, text) {
	if ( theNode.hasChildNodes() ) {
		theNode.replaceChild(document.createTextNode(text),
			theNode.firstChild);
	} else {
		theNode.appendChild(document.createTextNode(text));
	}
}

/* =================================================================
 * MEDIA ITEM HELPER METHODS
 * ================================================================= */

/**
 * Create an HTML <img> element for a MediaServer image.
 *
 * @param size the MediaServer size constant (e.g. 'normal')
 * @param compress the MediaServer compression constant (e.g. 'normal')
 */
function appendMediaServerImg(parent,id,alt,width,height,size,compress,styleClass)
{
	var sizeData = getMediaItemWidthHeight(size,width,height);

	var href = getMediaServerURL(id,size,compress);

	var img = appendImg(parent,href,id,alt,sizeData['width'],sizeData['height'],styleClass);
	//setMediaItemSize(width,height,size,img); // because IE doesn't get width/height?
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


/**
 * Get an URL for a MediaServer item.
 * 
 * @param itemId the media item ID
 * @param size the size key
 * @param compress the compress key
 * @return URL
 */
function getMediaServerURL(itemId,size,compress) {
	return webContext + "/MediaServer?id=" +itemId
			+"&s=" +size
			+(compress ? "&c="+compress : "");
}

/**
 * Generate a file size string from a file size number (bytes).
 *
 * @param fileSize an integer, treated as bytes
 * @return the size of the file, as text like "23.45 KB"
 */
function getFileSize(fileSize) {
	if ( !fileSize ) {
		return "0 bytes";
	}

	if (fileSize > 1048576) {
		return formatFloat(fileSize/1048576.0,2) +" MB";
	} else if ( fileSize > 1024 ) {
		return formatFloat(fileSize/1024.0,2) +" KB";
	} else {
		return fileSize +" bytes";
	}
}

/**
 * Format a floating-point number to a specified number of places.
 *
 * Note this method is not 100% accurate when rounding.
 *
 * @param num the floating point number to format
 * @param places integer number of places to round the number to
 * @return the rounded number
 */
function formatFloat(num,places) {
	var parts = new String(num).split('.',2);
	var decimal = parseInt(parts[1].substring(0,places));
	var round = parseInt(parts[1].substring(places,places+1));
	var incWhole = false;
	var oldLength = new String(decimal).length;

	if ( round > 4 ) {
		decimal = decimal + 1;
	}

	if ( new String(decimal).length > oldLength ) {
		incWhole = true;
	}

	if ( decimal != 0 ) {
		return (incWhole ? parseInt(parts[0])+1 : parts[0]) +"." +decimal;
	}
	return (incWhole ? parts[0]+1 : parts[0]);
}

/* =================================================================
 * CSS POSITIONING HELPER METHODS
 * ================================================================= */

/* =======================================
 * getAbsoluteLeft and getAbsoluteTop
 * code modified from Benjamin Lupu,
 * http://1ppl.free.fr/html/article077.html
 * which is distributed under the LGPL.
 * ======================================= */

function getAbsoluteLeft(xbElement) {
	if ( !xbElement ) return;
	var o = xbElement.object;
	var oLeft = o.offsetLeft;
	while ( o.offsetParent != null ) {
		var oParent = o.offsetParent;
		oLeft += oParent.offsetLeft;
		o = oParent;
	}
	return oLeft;
}

function getAbsoluteTop(xbElement) {
	if ( !xbElement ) return;
	var o = xbElement.object;
	var oTop = o.offsetTop;
	while ( o.offsetParent != null ) {
		var oParent = o.offsetParent;
		oTop += oParent.offsetTop;
		o = oParent;
	}
	return oTop;
}

/**
 * Adjust the position of "corner" elements.
 *
 * This function will look for elements named 'name' appended with
 * -tl, -tr, br, and -bl, representing the top left, top right,
 * bottom right, and bottom left "corners" of some main content
 * area. These corner elements will be repositioned so they
 * are aligned in their respective corner.
 *
 * @param xbElement the xbStyle element representing the main content area
 * @param name the prefix of the corner elements
 */
function adjustCorners(xbElement,name,x,y) {
	if ( !xbElement ) return;

	if ( typeof(x) != 'number' ) {
		x = getAbsoluteLeft(xbElement);
	}
	if ( typeof(y) != 'number' ) {
		y = getAbsoluteTop(xbElement);
	}
	var width = xbElement.getWidth();
	var height = xbElement.getHeight();

	//alert("x = " +x +", y = " +y +", width = " +width
	//	+", height = " +height);

	var tl = xbGetElementById(name+'-tl');
	if ( tl ) {
		var xb_tl = new xbStyle(tl);
		xb_tl.moveTo(x,y);
	}

	var tr = xbGetElementById(name+'-tr');
	if ( tr ) {
		var xb_tr = new xbStyle(tr);
		xb_tr.moveTo(x+width-xb_tr.getWidth()+xbElement.getBorderLeftWidth()
			+xbElement.getBorderRightWidth(),y);
	}

	var br = xbGetElementById(name+'-br');
	if ( br ) {
		var xb_br = new xbStyle(br);
		xb_br.moveTo(x+width-xb_br.getWidth()+xbElement.getBorderLeftWidth()
			+xbElement.getBorderRightWidth(),
			y+height-xb_br.getHeight()+xbElement.getBorderTopWidth()
			+xbElement.getBorderBottomWidth());
	}

	var bl = xbGetElementById(name+'-bl');
	if ( bl ) {
		var xb_bl = new xbStyle(bl);
		xb_bl.moveTo(x,y+height-xb_bl.getHeight()+xbElement.getBorderTopWidth()
			+xbElement.getBorderBottomWidth());
	}
}

/**
 * Center an xb object within the current window.
 *
 * Only the xbElement parameter is required.
 *
 * @param xbElement an xb object
 * @param cornerElementName the name of the corner elements to adjust
 * @param xOffset an X pixel offset
 * @param yOffset a Y pixel offset
 */
function centerWidget(xbElement,cornerElementName,xOffset,yOffset) {
	if ( !xbElement ) return;

	var width = xbElement.getWidth();
	var height = xbElement.getHeight();

	if ( !xOffset ) xOffset = 0;
	if ( !yOffset ) yOffset = 0;

	var x = Math.round((xbGetWindowWidth()-width-xOffset)/2);
	if ( x < 0 ) { x = 0; }
	x += xOffset;
	var y = Math.round((xbGetWindowHeight()-height-yOffset)/2);
	if ( y < 0 ) { y = 0; }
	y += yOffset;

	//alert("x = " +x +", y = " +y +", ww = " +xbGetWindowWidth() +", wh = " +xbGetWindowHeight());

	xbElement.moveTo(x,y);
	if ( cornerElementName ) {
		adjustCorners(xbElement,cornerElementName,x,y);
	}
}

/**
 * Hide a "widget" and any "corners" it might have.
 *
 * @param xbElement the xbStyle object to hide
 * @param cornerElementName the name of the "corner" elements
 */
function hideWidget(xbElement,cornerElementName) {
	if ( !xbElement ) return;
	hideElement(xbGetElementById(cornerElementName+'-tl'));
	hideElement(xbGetElementById(cornerElementName+'-tr'));
	hideElement(xbGetElementById(cornerElementName+'-br'));
	hideElement(xbGetElementById(cornerElementName+'-bl'));
	hideElement(xbElement);
}

/**
 * Show a "widget" and any "corners" it might have.
 *
 * @param xbElement the xbStyle object to show
 * @param cornerElementName the name of the "corner" elements
 */
function showWidget(xbElement,cornerElementName) {
	if ( !xbElement ) return;
	showElement(xbGetElementById(cornerElementName+'-tl'));
	showElement(xbGetElementById(cornerElementName+'-tr'));
	showElement(xbGetElementById(cornerElementName+'-br'));
	showElement(xbGetElementById(cornerElementName+'-bl'));
	showElement(xbElement);
}

function hideElement(element) {
	if ( element ) {
		var xbElement;
		if ( element.setVisibility) {
			xbElement = element;
		} else {
			xbElement = new xbStyle(element);
		}
		xbElement.setVisibility("hidden");
	}
}

function showElement(element) {
	if ( element ) {
		var xbElement;
		if ( element.setVisibility) {
			xbElement = element;
		} else {
			xbElement = new xbStyle(element);
		}
		xbElement.setVisibility("visible");
	}
}

// *******************************************************************
// MODAL DIALOG FUNCTIONS
// *******************************************************************

var xbModalDialogDiv;

function init_modal_dialog_page() {

	// center the login div in the window
	var logonDiv = xbGetElementById('logon');
	xbModalDialogDiv = new xbStyle(logonDiv);
	
	// check for message dialog
	if ( xbGetElementById('msg-box-dialog') ) {
		init_msgBox_widget(0);
	}
	
	centerLogonWidget();
	window.onresize = centerLogonWidget;
}

function centerLogonWidget() {
	centerWidget(xbModalDialogDiv,'logon');
	if ( xbGetElementById('msg-box-dialog') ) {
		msgBoxCenterWidget();
	}
}

// *******************************************************************
// MISC UTILITY FUNCTIONS
// *******************************************************************

/**
 * Open a pop-up window to view a theme preview.
 *
 * @param path the path to the image file
 */
function viewThemePreview(path) {

	var loc = path;

	if ( themePreviewWindow ) {
		themePreviewWindow.close();
	}
	themePreviewWindow = window.open(loc,"ma_theme_preview",
				"width=1044,height=788,menubar=no,toolbar=no,resizable,personalbar=no,directories=no,hotkeys=no");
}


/**
 * Open a pop-up window to view a media item.
 * 
 * @param itemId the media item ID
 * @param size the size key
 * @param compress the compress key
 */
var singleItemWindow = null;
function viewSingleItem(itemId,size,compress,origWidth,origHeight) {
	var url = getMediaServerURL(itemId,size,compress);
	var sizeData = getMediaItemWidthHeight(size,origWidth,origHeight);
	var width = sizeData['width'] +20;
	var height = sizeData['height'] +20;
	//alert("width = " +width +", height = " +height);
	var left = (screen.width - width ) / 2;
	var top = (screen.height - height ) / 2;
	var windowOpts = "titlebar,toolbar=no,resizable,scrollbars,left=" +left 
		+",top=" +top +",width=" +width +",height=" +height;
	if ( singleItemWindow != null && !singleItemWindow.closed ) {
		singleItemWindow.close();
	}
	singleItemWindow = window.open(url, "sitem", windowOpts);
	singleItemWindow.focus();
}


/**
 * Update an <img> element's width and height attributes.
 *
 * @param origWidth the natural width of the media item (pixels)
 * @param origHeight the natural height of the media item (pixels)
 * @param size the Media Album size
 * @param imgElem the <img> object
 * @param setWidth if provided, use as the image width
 * @param setHeight if provided, use as the image height
 */
function setMediaItemSize(origWidth,origHeight,size,imgElem,setWidth,setHeight) {
	var sizeData = getMediaItemWidthHeight(size,origWidth,origHeight,setWidth,setHeight);
	imgElem.setAttribute("width",sizeData['width']);
	imgElem.setAttribute("height",sizeData['height']);
}

// *******************************************************************
// MAINTAIN LIST ITEMS FUNCTIONS
// *******************************************************************

var msg_noSelection_en = "You must first select one or more items.";
var msg_noItemsToChooseFrom_en = "There are no items available to select for this."

function selectAllListItems(checkboxes) {
	if ( !checkboxes ) {
		return;
	}
	setCheckboxesChecked(checkboxes,true);
	handleChangeListItem(checkboxes);
}

function selectNoListItems(checkboxes) {
	if ( !checkboxes ) {
		return;
	}
	setCheckboxesChecked(checkboxes,false);
	handleChangeListItem(checkboxes);
}

function handleChangeListItem(checkboxes) {
	var selectedClass = "row-selected";

	var fieldArray = getFormElementArray(checkboxes);

	for ( idx = 0; idx < fieldArray.length; idx++ ) {
		var checkbox = fieldArray[idx];
		var parentTr = checkbox.parentNode.parentNode;
		if ( checkbox.checked ) {
			var newClass = parentTr.className;
			if ( !newClass || newClass.indexOf(selectedClass) < 0 ) {
				newClass = selectedClass + (newClass?"-"+newClass:"");
			}
			parentTr.className = newClass;
		} else {
			var oldClass = parentTr.className;
			//alert("oldClass = " +oldClass +", " +parentTr.getAttribute("class"));
			if ( oldClass && oldClass.indexOf(selectedClass) == 0 ) {
				if ( oldClass.length == selectedClass.length ) {
					parentTr.className = "";
				} else {
					oldClass = oldClass.substr(selectedClass.length+1);
					parentTr.className = oldClass;
				}
			}
		}
	}
}

function areAnyCheckboxesChecked(lang,checkboxes) {

	if ( !checkboxes ) {
		alert(eval("msg_noItemsToChooseFrom_"+lang));
		return false;
	}

	var fieldArray = getFormElementArray(checkboxes);
	/*
	if ( checkboxes.length != undefined && checkboxes.type == undefined ) {
		fieldArray = checkboxes;
	} else {
		fieldArray = new Array(checkboxes);
	}*/

	var checked = false;

	for ( idx = 0; idx < fieldArray.length; idx++ ) {
		var checkbox = fieldArray[idx];
		if ( checkbox.checked ) {
			checked = true;
			break;
		}
	}

	if ( !checked ) {
		alert(getMessage("msg_noSelection",lang));
	}

	return checked;
}

function getFormElementArray(element) {
	if ( !element ) {
		return new Array();
	}
	if ( element.length != undefined && element.type == undefined ) {
		return element;
	}
	return new Array(element);
}

/**
 * Return a string, truncated with '...' if longer than a specified length.
 */
function maxString(text,maxLength) {
	if ( !text || !maxLength || maxLength < 3 ) {
		return text;
	}
	if ( text.length > (maxLength + 3) ) {
		return "..." +text.substr(text.length - maxLength + 2);
	}
	return text;
}
