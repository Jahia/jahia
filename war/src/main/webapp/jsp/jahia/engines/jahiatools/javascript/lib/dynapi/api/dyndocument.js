/*
   DynAPI Distribution
   DynDocument Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
*/
DynDocument = function(frame) {
	this.DynObject = DynObject
	this.DynObject()
	this.elm = frame;
	this.doc = frame.document;
	this.frame = frame;
	this.fgColor = this.doc.fgColor||'';
	this.bgColor = this.doc.bgColor||'';
	this.elm.lyrobj=this;this.doc.lyrobj=this;
	this.findDimensions();				
}
DynDocument.prototype = new DynObject();
DynDocument.prototype.isDynDocument = true;
DynDocument.prototype.getBgColor = function() {
	return this.bgColor;
};
DynDocument.prototype.specificCreate=function() {
};
DynDocument.prototype.specificRemove=function() {
	this.elm=this.doc=this.frame=null;
};
DynDocument.prototype.getX=function() { return 0; };
DynDocument.prototype.getY=function() { return 0; };
DynDocument.prototype.getPageX=function() { return 0; };
DynDocument.prototype.getPageY=function() { return 0; };
DynDocument.prototype.getWidth = function() {
	if (!this.w) this.findDimensions();
	return this.w;
};
DynDocument.prototype.getHeight = function() {
	if (!this.h) this.findDimensions();
	return this.h
};
DynDocument.prototype.findDimensions = function() {
	this.w=(is.ns || is.opera)? this.elm.innerWidth : this.doc.body.clientWidth;
	this.h=(is.ns || is.opera)? this.elm.innerHeight : this.doc.body.clientHeight;
};
DynDocument.prototype.setBgColor = function(color) {
	if (color == null) color='';
	if (color == '' && is.ns4) color = '#ffffff';
	this.bgColor = color;
	this.doc.bgColor = color;
};
DynDocument.prototype.setFgColor = function(color) {
	if (color == null) color='';
	if (color == '' && is.ns4) color='#ffffff';
	this.fgColor = color;
	this.doc.fgColor = color;
};
DynDocument.prototype.load = function(path) {
	this.doc.location = path;
};
DynAPI.addLoadFunction("DynAPI.document = DynAPI.addChild(new DynDocument(self));DynAPI.document.all = DynObject.all;")
DynAPI.addResizeFunction("if (DynAPI.document) DynAPI.document.findDimensions();");
