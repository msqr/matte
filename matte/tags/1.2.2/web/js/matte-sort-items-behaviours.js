function handleSortItemsSubmit() {
	var submitForm = $('sort-items-submit-form');
	var albumItems = $A($('sort-items-container').getElementsByTagName('img'));
	albumItems.each(function(el, idx) {
		var itemId = el.id.substring(el.id.lastIndexOf('-')+1);
		submitForm.appendChild(Builder.node('input', {
			'type' 	: 'hidden',
			'name' 	: 'items[' +idx +'].itemId',
			'value'	:  itemId
		}));
		submitForm.appendChild(Builder.node('input', {
			'type' 	: 'hidden',
			'name' 	: 'items[' +idx +'].order',
			'value'	:  idx
		}));
	});
	submitForm.submit();
	return false;
}

var myRules = {

	'#sort-items-form' : function(form) {
		form.onsubmit = function() {
			return handleSortItemsSubmit();
		}
	},
	
	'#sort-items-container' : function(el) {
		Sortable.create(el, {
			tag: 'div',
			overlap: 'horizontal',
			constraint: false/*,
			onUpdate:function(){
		    if(Sortable.serialize("puzzle")==
		    }
		  }*/
		});	
	}
}

Behaviour.register(myRules);
