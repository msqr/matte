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
// $Id: matte-locale.js,v 1.8 2007/06/25 10:35:32 matt Exp $
// ===================================================================
// 
// Derived from code in Scriptaculous, Copyright (c) 2005 Thomas Fuchs 
// (http://script.aculo.us, http://mir.aculo.us)


var DEFAULT_LANG = 'en';

var MatteLocale = {

	bundle: {},

	init: function(messages) {
		if ( messages ) this.bundle = messages;
	},
	
	/**
	 * Initialize from DOM nodes of type <msg key='x'>value</msg>.
	  */
	initXmsg: function(xMsgNodes) {
		if ( !(xMsgNodes && xMsgNodes.length) ) return;
		for ( var i = 0; i < xMsgNodes.length; i++ ) {
			if ( !xMsgNodes[i].hasChildNodes() ) continue;
			var key = xMsgNodes[i].getAttribute('key');
			xMsgNodes[i].normalize();
			var value = xMsgNodes[i].firstChild.nodeValue;
			this.bundle[key] = value;
		}
	},
	
	initJson: function(msgData) {
		if ( !msgData ) return;
		for ( var key in msgData ) {
			this.bundle[key] = msgData[key];
		}
	},
  
	i18n : function(key,params) {
		var msg = this.bundle[key];
		if ( !msg ) {
			msg = '??'+key+'??';
		} else if ( params ) {
			var i = 0;
			for ( i = 0; i < params.length; i++ ) {
				msg = msg.replace(new RegExp('\\{'+(i)+'\\}','g'),params[i]);
			}
		}
		return msg;
	 }
}

MatteLocale.init();
