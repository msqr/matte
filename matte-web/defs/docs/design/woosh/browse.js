var wooshBehaviours = {
	'.frame-t' : function(el) {
		Element.setStyle(el, {
			background: '#fff url(img/tb_05.gif) repeat-x left top'
		});
	},
	
	'.frame-ml' : function(el) {
		Element.setStyle(el, {
			background: '#fff url(img/tb_18.gif)'
		});
	},
	
	'.frame-mr'  : function(el) {
		Element.setStyle(el, {
			background: '#fff url(img/tb_23.gif) repeat-y'
		});
	},
	
	'.frame-b' : function(el ) {
		Element.setStyle(el, {
			background: '#fff url(img/tb_27.gif) repeat-x left bottom'
		});
	}
	
}

function setShadow(el) {
	var dim = Element.getDimensions(el);
	var width = dim.width;
	var height = dim.height;
	if ( width > 0 && height > 0 ) {
		var bgUrl = 'shadow.do?w=' +width 
			+'&h=' +height +'&b=6&r=3&c=3289650';
		//alert("setting shadow: " +bgUrl);
		Element.setStyle(el.parentNode, {
			'background-image' : 'url(shadow.png)',
			'background-repeat' : 'no-repeat',
			'background-position' : '-3px -3px'
		});
	}
}

Behaviour.register(wooshBehaviours);
//Event.observe(window, 'load', init);
