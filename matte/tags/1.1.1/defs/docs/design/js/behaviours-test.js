var MatteState = Class.create();
MatteState.prototype = {
	initialize: function() {
		this.selected = $A(new Array());
	},
	
	select: function(obj) {
		this.selected.push(obj);
	},
	
	unselect: function(obj) {
		this.selected = $A(this.selected.reject(function(val) {
			return obj == val;
		}));
	}
}

var AppState = new MatteState();

var MatteObserver = Class.create();
MatteObserver.prototype = {
  initialize: function(element, observer) {
    this.element   = $(element);
    this.observer  = observer;
  },
  
  onStart: function(eventName,draggable) {
    //alert("yep");
    if ( this.element == draggable.element ) {
    	this.num = Builder.node('div',{id:'drag-number'},AppState.selected.length);
    	this.element.appendChild(this.num);
    }
  },
  
  onEnd: function(eventName,draggable) {
    if ( this.element == draggable.element ) {
    	this.num.parentNode.removeChild(this.num);
    	//this.num.element.parentNode.removeChild(this.num.element);
    	//Insertion.After(draggable.element,'<div>'+this.num +'</div>');
    }
  }
}


var listMenu = new FSMenu('listMenu', true, 'visibility', 'visible', 'hidden');
var myrules = {
	'body' : function(el) {
		//listMenu.showDelay = 0;
		//listMenu.switchDelay = 125;
		//listMenu.hideDelay = 500;
		listMenu.cssLitClass = 'highlighted';
		//listMenu.showOnClick = 1;
		
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
		  if (!done && ref.style.filter.indexOf("alpha") == -1)
		   ref.style.filter += ' alpha(opacity=' + counter + ')';
		  else if (f.length && f.alpha) with (f.alpha) {
		   if (done) enabled = false;
		   else { opacity = counter; enabled=true }
		  }
		 }
		 else ref.style.opacity = ref.style.MozOpacity = counter/100.1;
		};
		
		// I'm applying them both to this menu and setting the speed to 20%. Delete this to disable.
		listMenu.animations[listMenu.animations.length] = animFade;
		listMenu.animations[listMenu.animations.length] = animClipDown;
		listMenu.animSpeed = 20;
		
		listMenu.activateMenu("listMenuRoot");
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
				new Draggable(span,{revert:true});
				Draggables.addObserver(new MatteObserver(span));
				
				Event.observe(span, 'click', function(evt) {
					if ( Element.hasClassName(newImg, 'selected') ) {
						Element.removeClassName(newImg,'selected');
						AppState.unselect(myId);
					} else {
						Element.addClassName(newImg,'selected');
						AppState.select(myId);
					}
				});
			}
		}
	}
};

//Event.observe(window, 'load', init, false);
Behaviour.register(myrules);
