if (navigator.plugins) {
	for (i=0; i < navigator.plugins.length; i++ ) {
		if (navigator.plugins[i].name.indexOf
			("QuickTime") >= 0)
		{ haveQuickTime = true; }
	}
}

if ((navigator.appVersion.indexOf("Mac") > 0)
	&& (navigator.appName.substring(0,9) == "Microsoft")
	&& (parseInt(navigator.appVersion) < 5) )
{ haveQuickTime = true; }
