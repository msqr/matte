// This is all key stroke navigation code
var kHasDOM = document.getElementById ? true : false;
var kNS4 = (document.layers && !kHasDOM) ? true : false;
var kNS5 = (kHasDOM && parseInt(navigator.appVersion) >= 5) ? true : false;
if (kNS4) 
{ 
    document.captureEvents(Event.KEYDOWN); 
}
document.onkeydown = getKey;

function getKey(keyStroke)
{
  var myKeyCode;
  if (kNS4) {
    myKeyCode = keyStroke.which;
  } else {
    if (kNS5) {
      myKeyCode = keyStroke.keyCode;
    } else {
      myKeyCode = event.keyCode;
    }
  }
  var keyString = String.fromCharCode(myKeyCode).toLowerCase();
  for (var i in key) if (keyString == i) {
    var jstag = "javascript:";
    var begin = key[i].indexOf(jstag)
    if (begin != -1 ) {
        begin = jstag.length;
        end = key[i].length;
        eval(unescape(key[i].substring(begin,end)));
    } else {
        window.location = key[i];
    }
  }
}
