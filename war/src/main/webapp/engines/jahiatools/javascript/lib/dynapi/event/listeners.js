/*
   DynAPI Distribution
   Advanced Event object. Generic Event Listeners

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
*/

DynEvent=function(type,src,target) {
	this.type=type;
	this.src=src;
	this.target=target;
	this.bubble=false;
};
DynEvent.prototype.setBubble=function(b) {this.bubble=b};
DynEvent.prototype.getType=function() {return this.type};
DynEvent.prototype.getSource=function() {return this.src};
DynEvent.prototype.getTarget=function() {return this.target};
DynEvent.prototype.preBubbleCode = function() {};

EventListener = function(target) { this.target=target };
EventListener.prototype.handleEvent=function(type,e,args) {
	if(this["on"+type]) this["on"+type](e,args);
};
// Extend DynObject to support EventListeners
DynObject.prototype.addEventListener=function(listener) {
	if(!this.eventListeners) { this.eventListeners = []; }
	this.hasEventListeners = true;
	for (var i=0;i<this.eventListeners.length;i++) if (this.eventListeners[i]==listener) return;
	this.eventListeners[this.eventListeners.length]=listener;
}
DynObject.prototype.removeEventListener=function(listener) {
	Methods.removeFromArray(this.eventListeners, listener, false);
	if(this.eventListeners.length==0) {
		this.hasEventListeners=false;
	}
}
DynObject.prototype.removeAllEventListeners=function() {
	if (!this.hasEventListeners) return;
	this.eventListeners=[];
	this.hasEventListeners=false;
}
DynObject.prototype.invokeEvent=function(type,e,args) {
	if (!e) e=new DynEvent(type,this);
	if (this.hasEventListeners) for(var i=0;i<this.eventListeners.length;i++) {
		e.target=this.eventListeners[i].target;
		this.eventListeners[i].handleEvent(type,e,args);
	}
	if(e.bubble && this.parent) {
		e.preBubbleCode();
		e.src = this.parent;
		this.parent.invokeEvent(type,e,args);
	}
}
DynObject.prototype.eventListeners = null;
DynObject.prototype._listeners_del = DynObject.prototype.del
DynObject.prototype.del = function() {
    this.removeAllEventListeners();
    this._listeners_del();
};
