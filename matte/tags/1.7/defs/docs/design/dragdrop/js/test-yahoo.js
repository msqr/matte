var test = new Object();

test.DD = function(id, sGroup, config) {
	this.initTest(id, sGroup, config);
};

YAHOO.extend(test.DD, YAHOO.util.DDProxy);
test.DD.TYPE = "DDPlayer";

test.DD.prototype.initTest = function(id, sGroup, config) {
    if (!id) { return; }

    this.init(id, sGroup, config);
    this.initFrame();

    var el = this.getDragEl()
    YAHOO.util.Dom.setStyle(el, "opacity", 0.76);

    // specify that this is not currently a drop target
    this.isTarget = false;

    this.originalStyles = [];

    this.type = test.DD.TYPE;

    this.startPos = YAHOO.util.Dom.getXY( this.getEl() );
};

test.DD.prototype.startDrag = function(x, y) {
    var Dom = YAHOO.util.Dom;

    var dragEl = this.getDragEl();
    var clickEl = this.getEl();

    while ( dragEl.hasChildNodes() ) {
    	dragEl.removeChild(dragEl.firstChild);
    }
    dragEl.appendChild(clickEl.cloneNode(true));
    //dragEl.className = clickEl.className;

    YAHOO.util.Dom.setStyle(dragEl, "z-index", 999);
	YAHOO.util.Dom.setStyle(dragEl, "opacity", 0.5);

    YAHOO.util.Dom.setStyle(clickEl, "opacity", 0.8);
};

test.DD.prototype.endDrag = function(e) {
    // reset the linked element styles
    YAHOO.util.Dom.setStyle(this.getEl(), "opacity", 1);
};

test.DD.prototype.onDragDrop = function(e, id) {
	alert("Arg, you got me: " +YAHOO.util.Dom.get(id).innerText);
};

test.DD.prototype.onDragOver = function(e, id) {};

test.DD.prototype.onDrag = function(e, id) {};

var myrules = {
	'img.thumb' : function(el) {
		new test.DD(el);
	},
	
	'#album-list li' : function(el) {
		 new YAHOO.util.DDTarget(el);
	}
};

Behaviour.register(myrules);
