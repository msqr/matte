// Copyright (c) 2006 Matt Magoffin
// 
// ===================================================================
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of
// the License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
// 02111-1307 USA
// ===================================================================
// $Id: matte-locale.js,v 1.1 2007/01/08 04:39:01 matt Exp $
// ===================================================================
// 
// Derived from code in Scriptaculous, Copyright (c) 2005 Thomas Fuchs 
// (http://script.aculo.us, http://mir.aculo.us)


var DEFAULT_LANG = 'en';

var MatteLocale = {
  SupportedLangs: {'en':true, 'es':true},

  bundle: {},
  
  load_msg: function(messages) {
    // inserting via DOM fails in Safari 2.0, so brute force approach
    document.write('<script type="text/javascript" src="'+messages+'"></script>');
  },
  
  load: function() {
    if((typeof Prototype=='undefined') ||
      parseFloat(Prototype.Version.split(".")[0] + "." +
                 Prototype.Version.split(".")[1]) < 1.4)
      throw("MatteLocale requires the Prototype JavaScript framework >= 1.4.0");

    $A(document.getElementsByTagName("script")).findAll( function(s) {
      return (s.src && s.src.match(/matte-locale\.js(\?.*)?$/))
    }).each( function(s) {
      var path = s.src.replace(/matte-locale\.js(\?.*)?$/,'');
      var myLang = s.src.match(/\?.*lang=([a-z,]*)/);
      if ( myLang ) myLang = myLang[1];
      if ( !MatteLocale.SupportedLangs[myLang] ) myLang = DEFAULT_LANG;
      MatteLocale.load_msg(path+'matte-messages_'+myLang+'.js')
    });
  },
  
  init: function(messages) {
  	if ( messages ) this.bundle = messages;
  },
  
  i18n : function(key,params) {
	var msg = this.bundle[key];
	if ( !msg ) {
		msg = '';
	} else if ( params ) {
		var i = 0;
		for ( i = 0; i < params.length; i++ ) {
			msg = msg.replace(new RegExp('\\{'+(i+1)+'\\}','g'),params[i]);
		}
	}
	return msg;
  }
}

MatteLocale.load();
