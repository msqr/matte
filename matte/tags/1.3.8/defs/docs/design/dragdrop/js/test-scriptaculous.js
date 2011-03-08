var myrules = {
	'img.thumb' : function(el) {	
		new Draggable(el,{revert:true});
	},
	
	'#album-list li' : function(el) {
		Droppables.add(el,{
				/*accept : 'selected',*/
				hoverclass : 'can-drop',
				greedy : 'true',
				onDrop : function(draggable,droppable) {
						alert("Agh, you got me!");
					}
				});
	}
};

Behaviour.register(myrules);
