/* JavaScript for user ratings.
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
 * $Id: ratings.js,v 1.1 2007/01/08 04:39:01 matt Exp $
 * ===================================================================
 */

var MAX_RATING = 5;
var XML_REQ = null;

/**
 * StarSet object
 * 
 * Object to handle the rating star image mouse events. It is assumed
 * the star images are grouped together in some sort of container tag, 
 * which is by default hidden via CSS. The image objects are assumed
 * to have nothing except whitespace between each of them. This object
 * will remove the space from the DOM when it initializes, and then 
 * set the visibility of the parent container to visible. The 
 * wsUrl parameter is assumed to end with the rating parameter, i.e. 
 * '/path/to/service?rating=' so that the rating can simply be appended
 * to that URL.
 */
function StarSet(imagePath, wsUrl, setNumber, currentRating, recipeId) {
    this.imgArray = new Array();
    this.imgPath = imagePath;
    this.setNum = setNumber;
    this.currRating = currentRating;
    this.recipeId = recipeId;
    this.wsUrl = wsUrl;
    
    for ( var i = 1; i <= MAX_RATING; i++ ) {
	    	var starImg = document.getElementById('star.'+this.setNum+'.'+i);
	    	if ( !starImg ) {
	    		alert("Can't find star " +this.setNum +'.' +i);
	    		return;
	    	}
	    	this.imgArray[(i-1)] = starImg;
	    	starImg.onmouseover = handleStarMouseOver
	    	starImg.onmouseout = handleStarMouseOut
	    	starImg.onclick = handleStarClick
	    	starImg.rating = i;
	    	starImg.starSet = this;

	    	// little hack to remove extra text from between img tags, as 
	    	// browsers render whitespace if it's there and XSLT might format 
	    	// XHTML output with spaces
	    	if ( starImg.previousSibling && starImg.previousSibling.nodeType == 3 ) {
	    		// nodeType 3 == Node.TEXT_NODE (IE does not support DOM constants)
	    		starImg.parentNode.removeChild(starImg.previousSibling);
	    	}
    }
    
    this.resetRating();
    
    	// now make stars visible by setting parent node to visible
    	this.imgArray[0].parentNode.style.visibility = 'visible';
}

function setRating(rating) {
    // TODO call XML web service to store the rating...
    this.currRating = rating;
    loadXMLDoc(this.wsUrl+rating);
}

function highlightRating(rating) {
	for ( var i = 0; i < rating; i++ ) {
		this.imgArray[i].src = this.imgPath + 'star-active.png';
	}
	for ( var i = rating; i < MAX_RATING; i++ ) {
		this.imgArray[i].src = this.imgPath + 'star-off.png';
	}
}

function resetRating() {
	for ( var i = 0; i < this.currRating; i++ ) {
		this.imgArray[i].src = this.imgPath + 'star-on.png';
	}
	for ( var i = this.currRating; i < MAX_RATING; i++ ) {
		this.imgArray[i].src = this.imgPath + 'star-off.png';
	}
}

/* StarSet class methods */

StarSet.prototype.setRating = setRating;
StarSet.prototype.highlightRating = highlightRating;
StarSet.prototype.resetRating = resetRating;

/* mouseOver function traps... 'this' refers to <img> object */

function handleStarMouseOver() {
	this.starSet.highlightRating(this.rating);
}

function handleStarMouseOut() {
	this.starSet.resetRating();
}

function handleStarClick() {
	this.starSet.setRating(this.rating);
}

/* XML HTTP request support */		

function loadXMLDoc(url) {
    // branch for native XMLHttpRequest object
    if (window.XMLHttpRequest) {
        XML_REQ = new XMLHttpRequest();
        XML_REQ.onreadystatechange = processReqChange;
        XML_REQ.open("GET", url, true);
        XML_REQ.send(null);
    // branch for IE/Windows ActiveX version
    } else if (window.ActiveXObject) {
        XML_REQ = new ActiveXObject("Microsoft.XMLHTTP");
        if (XML_REQ) {
            XML_REQ.onreadystatechange = processReqChange;
            XML_REQ.open("GET", url, true);
            XML_REQ.send();
        }
    }
}

function processReqChange() {
    // only if req shows "loaded"
    if (XML_REQ.readyState == 4) {
        // only if "OK"
        if (XML_REQ.status == 200) {
            // ...all's well
            // alert("who-hoo! " +XML_REQ.responseText);
        } else {
            alert("There was a problem retrieving the XML data:\n" +
                XML_REQ.statusText);
        }
    }
}
