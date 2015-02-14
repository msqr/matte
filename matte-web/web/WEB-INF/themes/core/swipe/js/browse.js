/*jslint browser: true */
/**
 * @require jQuery 2.1
 */

(function() {
'use strict';

function init() {
	$('a.download-album').on('click', function(event) {
		var form = $('#album-download-modal'),
			link = $(this),
			albumKey = link.data('album-key');
		event.preventDefault();
		form.find('input[name=albumKey]').val(albumKey);
		form.modal('show');
	});
	
	$('#album-download-modal input[name=original]').on('change', function() {
		var selects = $(this.form).find('select');
		selects.prop('disabled', this.checked);
	});
}

$(init);

}());
