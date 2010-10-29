/**
 * MatteState: class to keep track of current state of application,
 * e.g. selected objects. The 'selected' property is an array of 
 * keys, manipulated via the select() and unselect() functions.
 */
var MatteState = Class.create();
MatteState.prototype = {
	initialize: function() {
		this.selected = new Array();
	},
	
	/**
	 * Make an object "selected" by adding it's key
	 * to array of selected keys.
	 * 
	 * @param key the key of the object to select
	 */
	select: function(key) {
		if ( this.selected.indexOf(key) < 0 ) {
			this.selected.push(key);
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
	initialize: function(element, selectableElement, selectableElementId) {
		this.element   = $(element);
		this.selectableElement = selectableElement;
		this.selectableElementId = selectableElementId;
	},

	/**
	 * Display the number decoration if more than one item selected.
	 */
	onStart: function(eventName,draggable) {
		if ( this.element == draggable.element ) {
			if ( !Element.hasClassName(this.selectableElement, 'selected') ) {
				Element.addClassName(this.selectableElement,'selected');
				AppState.select(this.selectableElementId);
			}
			if ( AppState.selected.length > 1 ) {
				this.num = Builder.node('div',{id:'drag-number'},AppState.selected.length);
				
				// Safari does not seem to make width available until after element inserted into DOM,
				// so assume CSS starts with visibility:hidden, insert into DOM, then reposition to lower-right
	
				this.element.appendChild(this.num);
				
				if ( Element.getStyle(this.num,'width') ) {
					var dimensions = Element.getDimensions(this.selectableElement);
					var numWidth = Element.getStyle(this.num,'width').match(/^\d+/); // remove 'px'
					var numHeight = Element.getStyle(this.num,'height').match(/^\d+/);
					Element.setStyle(this.num,{top:(dimensions.height-(numHeight/2)-4)+'px', 
						left:(dimensions.width-(numWidth/2)-4)+'px'});
				}
				
				Element.setStyle(this.num,{visibility:'visible'});
			}
		}
	},

	/**
	 * Remove the number decoration if previously shown.
	 */
	onEnd: function(eventName,draggable) {
		if ( this.element == draggable.element ) {
			if ( this.num != null ) {
				this.num.parentNode.removeChild(this.num);
				this.num = null;
			}
			if ( Element.hasClassName(this.selectableElement, 'selected') ) {
				Element.removeClassName(this.selectableElement,'selected');
			}
		}
	}
}

var myrules = {
	/*'body' : function(el) {
		// reduce body top margin if no sub nav available
		if ( !($('sub-nav') || $('sub-nav-data')) ) {
			var h1 = $A(el.getElementsByTagName('h1'));
			if ( h1.length > 0 ) {
			
			} else {
				Element.setStyle(el,'padding-top',
			}
		}
	},*/
	
	'span.rating-stars' : function(el) {
		// FIXME: this is hack for demo only
		var starNum = 0;
		for ( var i = 0; i < 5; i++ ) {
			var starNode = Builder.node('img',{alt:i,src:'img/star-off.png',id:'star.'+starNum+'.'+(i+1),
				'class':'star-rating'});
			el.appendChild(starNode);
		}
		STAR_SETS[starNum] = new StarSet('img/','ma2/setRating.do?recipeId=118&rating=',starNum,4,118);
	},
	
	'.collapsing' : function(el) {
		
		// first add the open arrow
		Element.cleanWhitespace(el.parentNode);
		var myHeader = el.previousSibling;
		if ( el.parentNode.nodeName.toLowerCase() == 'li' ) {
			// nested collapsing list item, assume has link within <li>
			myHeader = el.parentNode;
		} else {
			myHeader.style.cursor = 'pointer';
		}
		var arrowNode = Builder.node('img',{'src':'img/arrow-open.png','class':'showhide'});
		myHeader.insertBefore(arrowNode,myHeader.firstChild);

		//new Insertion.Top(myHeader,'<img src="img/arrow-open.png" class="showhide"/>');
		myHeader.style.cursor = 'pointer';
		
		var isOpen = true;
		
		// create function to switch arrow from open/closed
		var replaceArrow = function(obj) {
			arrowNode.src = 'img/arrow-'+(isOpen?'closed':'open') +'.png';
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
		
		
		if ( myHeader.nodeName.toLowerCase() != 'li' ) {
			myHeader.onclick = handleClick;
		} else {
			arrowNode.onclick = handleClick;
		}
	},
	
	'ol.collapsing li' : function(el) {
		var myA = $A(el.getElementsByTagName('a')).first();
		if ( myA && myA.href.match(/(album|collection)Id=(\d+)/) ) {
			var data = myA.href.match(/(album|collection)Id=(\d+)/);
			
		
			// wrap A with a <div> and make the <div> droppable
			var spanNode = Builder.node('div',{'class':'list-drop'}); // class required or NPE in Prototype
			if ( myA.previousSibling && Element.hasClassName(myA.previousSibling,'showhide') ) {
				spanNode.appendChild(myA.previousSibling); // move node into spanNode
			}
			spanNode.appendChild(myA.cloneNode(true));
			myA.parentNode.replaceChild(spanNode, myA);
			
			Droppables.add(spanNode,{
				/*accept : 'selected',*/
				hoverclass : 'can-drop',
				greedy : 'true',
				onDrop : function(draggable,droppable) {
						//alert("Agh, you got me! " +data[0]);
					}
				});
		}
	},

	'#left-pane-tab' : function(el) {
		// set cursor to pointer when over tab
		el.style.cursor = 'pointer';
		
		var pane = $('left-pane'); // Prototype way for document.getElementById('left-pane');
		var main = $('main-pane');
		
		el.title = MatteLocale.i18n( pane.style.left == '-300px' 
			? 'left.pane.tab.title.close' : 'left.pane.tab.title.open');
		
		// onclick: close or open the pane
		el.onclick = function() {
			var isOpen = pane.style.left == '-300px' ? false : true;
			var moveToX = isOpen ? -300 : 300;
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
	},

	'img.thumb' : function(el) {

		// surround <img> with <span class="thumb" style="...">
		// unless already surrounded by one
		if ( is_js >= 1.5 ) {
			var imgClass = is_safari ? 'thumb' : 'thumb-float';
			var parent = el.parentNode;
			if ( !(parent.nodeName == 'SPAN' && parent.className == imgClass) ) {
				var width = el.getAttribute('width');
				var height = el.getAttribute('height');
				var bgStyle = 'transparent ' +'url(http://msqr.us/ma/ShadowServer?w=' +width 
					+'&amp;h=' +height +'&amp;b=6&amp;r=3&amp;c=3289650)'+' no-repeat -3px -3px';
				
				var span = document.createElement('span');
				span.className = imgClass;
	
				span.style.backgroundImage = 'url(http://msqr.us/ma/ShadowServer?w=' +width 
					+'&h=' +height +'&b=6&r=3&c=3289650)';
				span.style.backgroundRepeat = 'no-repeat';
				span.style.backgroundPosition = '-3px -3px';
				
				var newImg = el.cloneNode(true);
				var myId = newImg.src.match(/MediaServer-(\d+).jpg/);
				if ( myId ) {
					myId = myId[1];
				} else {
					myId = -1;
				}
				
				span.appendChild(newImg);
				el.parentNode.replaceChild(span,el);
				new Draggable(span,{
					revert:true, 
					ghosting:false, 
					starteffect: function(element) { 
						new Effect.Opacity(element, {duration:0.2, from:1.0, to:0.4}); 
					  },
					  /*reverteffect: function(element, top_offset, left_offset) {
						var dur = Math.sqrt(Math.abs(top_offset^2)+Math.abs(left_offset^2))*0.02;
						element._revert = new Effect.Move(element, { x: -left_offset, y: -top_offset, duration: dur});
					  },*/
					  endeffect: function(element) { 
						new Effect.Opacity(element, {duration:0.2, from:0.4, to:1.0}); 
					  }
				});
				var myObserver = new NumberDecorationObserver(span,newImg,myId);
				Draggables.addObserver(myObserver);
				
				Event.observe(span, 'click', function(evt) {
					if ( Element.hasClassName(newImg, 'selected') ) {
						Element.removeClassName(newImg,'selected');
						AppState.unselect(myId);
					} else {
						Element.addClassName(newImg,'selected');
						AppState.select(myId);
					}
					
					// adjust info pane as needed
					var infoPane = $('info-pane');
					
					if ( AppState.selected.length > 1 ) {
						// show multi items info pane
						document.getElementsByClassName('multi',infoPane).each(function(node) {
							Element.setStyle(node,{display:'block'});
						});
						document.getElementsByClassName('single',infoPane).each(function(node) {
							if ( !Element.hasClassName(node,'multi') ) {
								Element.setStyle(node,{display:'none'});
							}
						});
					} else if ( AppState.selected.length == 1 ) {
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
					} else {
						// hide info pane
						Element.setStyle(infoPane,{display:'none'});
					}
				});
			}
		}
	}
};


// AppState: global instance of MatteState
var AppState = new MatteState();
var STAR_SETS = new Array();
Behaviour.register(myrules);
