
if(typeof(AC)=="undefined"){AC={};}

AC.Quicktime={


packageMovie:function(name,fileUrl,options){


function addParameter(parent,name,value){

if(!parent){
return;
}

var param=document.createElement('param');
param.setAttribute('value',value);
param.setAttribute('name',name);
parent.appendChild(param);

}


function createOuterObject(name,fileUrl,options){

var outerObject=document.createElement('object');
addParameter(outerObject,'src',fileUrl);

outerObject.setAttribute('id',name);


var activexVersion='6,0,2,0';

if(null!=options&&''!=options.codebase&&typeof(options.codebase)!='undefined'){
activexVersion=options.codeBase;
}

outerObject.setAttribute('codebase',
'http://www.apple.com/qtactivex/qtplugin.cab#version='+activexVersion);

return outerObject;
}


function createInnerObject(name,fileUrl,options){

var innerObject=document.createElement('object');

innerObject.setAttribute('type','video/quicktime');
innerObject.setAttribute('data',fileUrl);
innerObject.setAttribute('id',name+"Inner");

return innerObject;
}


function configureMovieOptions(){

if(null==options||typeof(options)=='undefined'){
return false;
}

for(var property in options){

var attributeName=property.toLowerCase();

switch(attributeName){
case('type'):
case('src'):
case('data'):
case('classid'):
case('name'):
case('id'):

break;
case('class'):
Element.addClassName(outerObject,options[property]);
break;
case('innerId'):
if(innerObject){
innerObject.setAttribute('id',options[property]);
}
break;
case('width'):
case('height'):
outerObject.setAttribute(attributeName,options[property]);
if(innerObject){
innerObject.setAttribute(attributeName,options[property]);
}
break;
default:
addParameter(outerObject,attributeName,options[property]);
addParameter(innerObject,attributeName,options[property]);
break;
}

}

}



if(name==null||fileUrl==null){
throw new TypeError('Valid Name and File URL are required arguments.');
}


var outerObject=createOuterObject(name,fileUrl,options);

if(!AC.Detector.isIEStrict()){





var innerObject=createInnerObject(name,fileUrl,options);

try{
outerObject.appendChild(innerObject);
outerObject.inner=innerObject;
}catch(e){}

}else{









Event.observe(window,'unload',function(){
outerObject.style.display='none';
outerObject=null;
});

}

configureMovieOptions();




addParameter(outerObject,'saveembedtags',true);
addParameter(innerObject,'saveembedtags',true);



outerObject.setAttribute('classid',
'clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B');




outerObject.toMarkup=function(){

if(!outerObject.outerHTML){
return;
}

var markup="";

markup=outerObject.outerHTML.replace('</OBJECT>','');

for(var i=0;i<outerObject.childNodes.length;i++){
markup+=outerObject.childNodes[i].outerHTML;
}

markup+='</OBJECT>';

return markup;
}

return outerObject;
},


swapMovie:function(container,oldMovie,newMovie){


var isIE=AC.Detector.isIEStrict();


if(isIE&&oldMovie){
oldMovie.style.display='none';
}




Element.removeAllChildNodes(container);

var movieReference=null;

if(!isIE){
container.appendChild(newMovie);
movieReference=newMovie;
}else{
container.innerHTML=newMovie.toMarkup();
movieReference=container.firstChild;
movieReference.style.display='block';
}


return movieReference;
},


render:function(movie,element,options){

var target=$(element);
var placeholderContent=target.innerHTML;

if(!AC.Detector.isQTInstalled()){
Element.addClassName(target,'static');
return
}

target.innerHTML='';
Element.addClassName(target,'loading');
target.appendChild(movie);

var showContent=function(element,content){
return(function(){
Element.removeClassName(element,"loading");
element.innerHTML='';
element.innerHTML=content;
})
}

var options=options?options:{};
var delay=3000;

if("delay"in options){
delay=options.delay;
}

if(!AC.Detector.isOpera()){
var qtSuccessCheck=setTimeout(showContent(target,placeholderContent),delay);
}

var movieLoaded=function(){
checkController.monitorMovie();
Element.removeClassName(element,"loading");
Element.addClassName(element,"loaded");
clearTimeout(qtSuccessCheck);
}

var movieFinished=function(){return;}

if("finishedState"in options){

var showFinishedState=function(target,content){
return(function(){

checkController.detachFromMovie();


movie.style.display='none';
movie=null;

target.innerHTML='';

if(typeof(content)=='string'){
target.innerHTML=content;
}else{
target.appendChild(content);
}
})
}

movieFinished=showFinishedState(target,options.finishedState)
}

var checkController=new AC.QuicktimeController(movie,{
onMoviePlayable:movieLoaded,
onMovieFinished:movieFinished})
}

};


AC.QuicktimeController=Class.create();
AC.QuicktimeController.prototype={

movie:null,
options:null,

movieAttacher:null,
attachDelay:10,

movieWatcher:null,
monitorDelay:400,
currentTime:0,
percentLoaded:0,
movieSize:0,

allowAttach:true,

controllerPanel:null,
currentControl:null,
playControl:null,
pauseControl:null,
slider:null,
track:null,
playHead:null,
loadedProgress:null,

isJogging:false,
hardPaused:false,
duration:0,
finished:false,
playing:false,


initialize:function(movie,options){

if(typeof(movie)!='undefined'){
this.attachToMovie(movie,options);
}

},


attachToMovie:function(movie,options){

this.options=options||{};


Event.observe(window,'unload',function(){
this.detachFromMovie();
}.bind(this))


var attach=function(onMoviePlayable,startTime){

if(!this.allowAttach){
return;
}

var status="";

try{
this.movie=movie;
status=this.movie.GetPluginStatus();
}catch(e){
try{
this.movie=movie.inner;
status=this.movie.GetPluginStatus();
}catch(e){
this.movie=null;
}
}

var duration=new Date().getTime()-startTime;

if(status=='Playable'||status=='Complete'){
clearInterval(this.movieAttacher);
if(typeof(onMoviePlayable)=='function'){
onMoviePlayable();
}
}else if(status&&status.match(/error/i)||duration>10000){
clearInterval(this.movieAttacher);
}
};


clearInterval(this.movieAttacher);

if(null==document.getElementById(movie.id)){
throw new ReferenceError('Movie has to be appended to document prior to initializing controller.');
}

this.movieAttacher=setInterval(
attach.bind(this,this.options.onMoviePlayable,new Date().getTime()),this.attachDelay);

},


detachFromMovie:function(){

this.allowAttach=false;

clearInterval(this.movieAttacher);
clearTimeout(this.movieWatcher);
this.movie=null;
this.reset();
this.allowAttach=true;
},


reset:function(){
this.duration=0;
this.movieSize=0;
this.percentLoaded=0;

if(this.slider){
this.slider.setValue(0);
this.slider.trackLength=this.slider.maximumOffset()-this.slider.minimumOffset();
}
},


render:function(containerId){

this.controllerPanel=document.createElement('div');
Element.addClassName(this.controllerPanel,'ACQuicktimeController');


this.playControl=document.createElement('div');
Element.addClassName(this.playControl,'control');
Element.addClassName(this.playControl,'play');
this.playControl.innerHTML='Play';
this.playControl.onclick=this.Play.bind(this);

this.pauseControl=document.createElement('div');
Element.addClassName(this.pauseControl,'control');
Element.addClassName(this.pauseControl,'pause');
this.pauseControl.innerHTML='Pause';
this.pauseControl.onclick=this.Stop.bind(this);

var playing=false;

if(null!=this.movie){
playing=this.GetAutoPlay();
}

this.currentControl=(playing)?this.pauseControl:this.playControl;
this.controllerPanel.appendChild(this.currentControl);

this.sliderPanel=document.createElement('div');
Element.addClassName(this.sliderPanel,'sliderPanel');

this.track=document.createElement('div');
Element.addClassName(this.track,'track');
this.sliderPanel.appendChild(this.track);

this.loadedProgress=document.createElement('div');
Element.addClassName(this.loadedProgress,'loadedProgress');
this.track.appendChild(this.loadedProgress);

this.trackProgress=document.createElement('div');
Element.addClassName(this.trackProgress,'trackProgress');
this.track.appendChild(this.trackProgress);

this.playHead=document.createElement('div');
Element.addClassName(this.playHead,'playHead');
this.track.appendChild(this.playHead);

this.controllerPanel.appendChild(this.sliderPanel);


if(containerId){

$(containerId).appendChild(this.controllerPanel);

if(this.movie){
this.monitorMovie();
}
}

return this.controllerPanel;
},


monitorMovie:function(){

function monitor(controller){

return(function(){

if(controller.controllerPanel!==null){

var shouldBePauseable=controller.isPlaying()&&(controller.currentControl==controller.playControl);
var shouldBePlayable=!controller.isPlaying()&&(controller.currentControl==controller.pauseControl);

if(!controller.isJogging&&shouldBePauseable){
controller.controllerPanel.replaceChild(controller.pauseControl,controller.currentControl);
controller.currentControl=controller.pauseControl;
}else if(!controller.isJogging&&shouldBePlayable){
controller.controllerPanel.replaceChild(controller.playControl,controller.currentControl);
controller.currentControl=controller.playControl;
}


if(controller.percentLoaded<=1){

var trackWidth=Element.getDimensions(controller.track).width;

var loaded=controller.GetMaxBytesLoaded()/controller.GetMovieSize();

if(!isNaN(loaded)&&0!==loaded){
controller.percentLoaded=loaded;
}

var progressWidth=trackWidth*controller.percentLoaded;
Element.setStyle(controller.loadedProgress,{width:progressWidth+'px'});
}


if(controller.isPlaying()){
controller.slider.setValue(controller.GetTime()/controller.GetDuration());
}

}


if(controller.isPlaying()){

if(!controller.playing){

controller.playing=true;

if(typeof(controller.options.onMovieStart)=='function'){
controller.options.onMovieStart();
}
}

}else{

if(controller.playing){

controller.playing=false;

if(typeof(controller.options.onMovieStop)=='function'){
controller.options.onMovieStop();
}
}
}


if(controller.isFinished()){
if(!controller.finished){


if(typeof(controller.options.onMovieFinished)=='function'){
controller.options.onMovieFinished();
}

controller.finished=true;
}
}else{
controller.finished=false;
}
if(controller.movie!==null){
controller.movieWatcher=setTimeout(monitorCallback,controller.monitorDelay);
}
});
}

var monitorCallback=monitor(this);

if(this.controllerPanel!==null){

this.slider=new Control.Slider(this.playHead,this.track,{
onSlide:function(value){

if(isNaN(value)){
return;
}

this.trackProgress.style.width=this.slider.translateToPx(value);


try{
this.movie.Stop();

this.isJogging=true;
this.SetTime(value*this.GetDuration());

}catch(e){

}

}.bind(this),

onChange:function(value){

if(isNaN(value)){
return;
}

this.trackProgress.style.width=this.slider.translateToPx(value);

if(!this.isPlaying()&&!this.hardPaused&&!this.isFinished()){
try{
this.movie.Play();
}catch(e){

}
}

this.isJogging=false;

}.bind(this)
});

}

this.movieWatcher=setTimeout(monitorCallback,this.monitorDelay);

},

Play:function(){
if(null!=this.movie){
this.hardPaused=false;
this.movie.Play();
}
},

Stop:function(){
this.hardPaused=true;
this.movie.Stop();
},

Rewind:function(){
if(null!=this.movie){
this.movie.Stop();
this.movie.Rewind();
}
},

Step:function(count){
this.movie.Step(count);
},

ShowDefaultView:function(){
this.movie.ShowDefaultView();
},

GoPreviousNode:function(){
this.movie.GoPreviousNode();
},

GetQuicktimeVersion:function(){
return this.movie.GetQuickTimeVersion();
},

GetQuicktimeLanguage:function(){
return this.movie.GetQuicktimeLanguage();
},

GetQuicktimeConnectionSpeed:function(){
return this.movie.GetQuicktimeConnectionSpeed();
},

GetIsQuickTimeRegistered:function(){
return this.movie.GetIsQuickTimeRegistered();
},

GetComponentVersion:function(){
return this.movie.GetComponentVersion();
},

GetPluginVersion:function(){
return this.movie.GetPluginVersion();
},

ResetPropertiesOnReload:function(){
this.movie.ResetPropertiesOnReload();
},

GetPluginStatus:function(){
return this.movie.GetPluginStatus();
},

GetAutoPlay:function(){
return this.movie.GetAutoPlay();
},

SetAutoPlay:function(autoPlay){
this.movie.SetAutoPlay(autoPlay);
},

GetControllerVisible:function(){
return this.movie.GetControllerVisible();
},

SetControllerVisible:function(visible){
this.movie.SetControllerVisible(visible);
},

GetRate:function(){
return this.movie.GetRate();
},

SetRate:function(rate){
this.movie.SetRate();
},

GetTime:function(){

var actualTime=0;
try{

actualTime=this.movie.GetTime();
}
catch(e){

}

if(0===actualTime){

actualTime=this.currentTime+this.monitorDelay;
}else{
this.currentTime=actualTime;
}

return actualTime;
},

SetTime:function(time){
this.movie.SetTime(time);
},

GetVolume:function(){
return this.movie.GetVolume();
},

SetVolume:function(volume){
this.movie.SetVolume(volume);
},

GetMute:function(){
return this.movie.GetMute();
},

SetMute:function(mute){
this.movie.SetMute();
},

GetMovieName:function(){
return this.movie.GetMovieName();
},

SetMovieName:function(movieName){
this.movie.SetMovieName(movieName);
},

GetMovieID:function(){
return this.movie.GetMovieID();
},

SetMovieID:function(movieID){
this.movie.SetMovieID(movieID);
},

GetStartTime:function(){
return this.movie.GetStartTime();
},

SetStartTime:function(time){
this.movie.SetStartTime(time);
},

GetEndTime:function(){
return this.movie.GetEndTime();
},

SetEndTime:function(time){
this.movie.SetEndTime(time);
},

GetBgColor:function(){
return this.movie.GetBgColor();
},

SetBgColor:function(color){
this.movie.SetBgColor(color);
},

GetIsLooping:function(){
return this.movie.GetIsLooping();
},

SetIsLooping:function(loop){
this.movie.SetIsLooping(loop);
},

GetLoopIsPalindrome:function(){
return this.movie.GetLoopIsPalindrome();
},

SetLoopIsPalindrome:function(loop){
this.movie.SetLoopIsPalindrome(loop);
},

GetPlayEveryFrame:function(){
return this.movie.GetPlayEveryFrame();
},

SetPlayEveryFrame:function(playAll){
this.movie.SetPlayEveryFrame(playAll);
},

GetHREF:function(){
return this.movie.GetHREF();
},

SetHREF:function(url){
this.movie.SetHREF(url);
},

GetTarget:function(){
return this.movie.GetTarget();
},

SetTarget:function(target){
this.movie.SetTarget(target);
},

GetQTNEXTUrl:function(){
return this.movie.GetQTNEXTUrl();
},

SetQTNEXTUrl:function(index,url){
this.movie.SetQTNEXTUrl(index,url);
},

GetURL:function(){
return this.movie.GetURL();
},

SetURL:function(url){
this.movie.SetURL(url);

this.reset();
},

GetKioskMode:function(){
return this.movie.GetKioskMode();
},

SetKioskMode:function(kioskMode){
this.movie.SetKioskMode(kioskMode);
},

GetDuration:function(){
if(null==this.duration||0===this.duration){
try{
this.duration=this.movie.GetDuration();
}catch(e){
this.duration=null;
}
}
return this.duration;
},

GetMaxTimeLoaded:function(){
return this.movie.GetMaxTimeLoaded();
},

GetTimeScale:function(){
return this.movie.GetTimeScale();
},

GetMovieSize:function(){
if(0===this.movieSize){
this.movieSize=this.movie.GetMovieSize();
}
return this.movieSize;
},

GetMaxBytesLoaded:function(){
return this.movie.GetMaxBytesLoaded();
},

GetTrackCount:function(){
return this.movie.GetTrackCount();
},

GetMatrix:function(){
return this.movie.GetMatrix();
},

SetMatrix:function(matrix){
this.movie.SetMatrix(matrix);
},

GetRectangle:function(){
return this.movie.GetRectangle();
},

SetRectangle:function(rect){
this.movie.SetRectangle(rect);
},

GetLanguage:function(){
return this.movie.GetLanguage();
},

SetLanguage:function(language){
this.movie.SetLanguage(language);
},

GetMIMEType:function(){
return this.movie.GetMIMEType();
},

GetUserData:function(type){
return this.movie.GetUserData(type);
},

GetIsVRMovie:function(){
return this.movie.GetIsVRMovie();
},

GetHotspotUrl:function(hotspotID){
return this.movie.GetHotspotUrl(hotspotID);
},

SetHotspotUrl:function(hotspotID,url){
this.movie.SetHotspotUrl(hotspotID,url);
},

GetHotspotTarget:function(hotspotID){
return this.movie.GetHotspotTarget(hotspotID);
},

SetHotspotTarget:function(hotspotID,target){
this.movie.SetHotspotTarget(hotspotID,target);
},

GetPanAngle:function(){
return this.movie.GetPanAngle();
},

SetPanAngle:function(angle){
this.movie.SetPanAngle(angle);
},

GetTiltAngle:function(){
return this.movie.GetTiltAngle();
},

SetTiltAngle:function(angle){
this.movie.SetTiltAngle(angle);
},

GetFieldOfView:function(){
return this.movie.GetFieldOfView();
},

SetFieldOfView:function(fov){
this.movie.SetFieldOfView(fov);
},

GetNodeCount:function(){
return this.movie.GetNodeCount();
},

SetNodeID:function(id){
this.movie.SetNodeID(id);
},

GetTrackName:function(index){
return this.movie.GetTrackName(index);
},

GetTrackType:function(index){
return this.movie.GetTrackType(index);
},

GetTrackEnabled:function(index){
return this.movie.GetTrackEnabled(index);
},

SetTrackEnabled:function(index,enabled){
this.movie.SetTrackEnabled(index,enabled);
},

GetSpriteTrackVariable:function(trackIndex,variableIndex){
return this.movie.GetSpriteTrackVariable(trackIndex,variableIndex);
},

SetSpriteTrackVariable:function(variableIndex,value){
this.movie.SetSpriteTrackVariable(variableIndex,value);
},



isPlaying:function(){
var playing=false;

try{
playing=this.movie.GetRate()!==0
}catch(e){}

return playing;
},

isFinished:function(){

var isStopped=true;
var isAtEnd=false;

try{
isStopped=this.movie.GetRate()===0;
isAtEnd=this.movie.GetTime()==this.GetDuration();
}catch(e){



return false;
}

return isStopped&&isAtEnd;
},

toggle:function(){

if(this.isPlaying()){
this.Stop();
}else{
this.Play();
}

}


};

