if ( window.opener && typeof window.opener.PopupHandler == 'object' ) {
	window.opener.PopupHandler.complete(window);
}