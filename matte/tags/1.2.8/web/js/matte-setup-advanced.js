function initAdvancedSetup() {
	var propHeaders = $A($('advanced-setup-table').getElementsByTagName('th'));
	propHeaders.each(function(propHeader) {
		propHeader = $(propHeader);
		propHeader.observe('click', function() {
			var propName = propHeader.firstChild.nodeValue;
			// sibling TD contains current value
			var propCell = propHeader.next('td');
			if ( propCell.hasClassName('modified') ) {
				return;
			}
			propCell.addClassName('modified');
			propCell.normalize();
			var propValue = propCell.firstChild 
				? trim(propCell.firstChild.nodeValue) : '';
			if ( propValue.length > 100 ) {
				// use textarea
				propCell.update('<textarea name="settings[\''
					+propName +'\']"></textarea>');
				$(propCell.firstChild).update(propValue);
			} else {
				// use text input
				propCell.update('<input type="text" name="settings[\''
					+propName +'\']"></input>');
				$(propCell.firstChild).value = propValue;
			}
		});
		propHeader.addClassName('a');
	});
}
function trim(s) {
	return s.replace(/^\s+/,'').replace(/\s+$/,'');
}
Event.observe(window, 'load', initAdvancedSetup);