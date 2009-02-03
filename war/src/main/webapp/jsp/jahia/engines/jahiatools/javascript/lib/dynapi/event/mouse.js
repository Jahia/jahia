/*
   DynAPI Distribution
   Event Classes

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser]
	dynapi.event [listeners]

*/
// The mouseEvent object
DynMouseEvent=function(old) {
	this.DynEvent=DynEvent;
	this.DynEvent();
	// Copy properties from given event, if any
	if (old) for(var i in old) this[i]=old[i]
};
DynMouseEvent.prototype=new DynEvent();
DynMouseEvent.prototype.preBubbleCode=function() {
	this.x+=this.src.x;
	this.y+=this.src.y;
};
DynMouseEvent.prototype.getX=function() {return this.x};
DynMouseEvent.prototype.getY=function() {return this.y};
DynMouseEvent.prototype.getPageX=function() {return this.pageX};
DynMouseEvent.prototype.getPageY=function() {return this.pageY};
DynMouseEvent.prototype.cancelBrowserEvent=function() {this.browserReturn=false};
// This is THE event. 
DynMouseEvent._e=new DynMouseEvent()
// If this is true, then mouseups always happen where the mousedown happened
DynMouseEvent.forceMouseUp = false

DynMouseEvent.EventMethod=function(e) {
	var dynobject=this.lyrobj;
	if(is.def) {
		if (is.ie) var e=dynobject.frame.event
        	e.cancelBubble=true;

		if (DynAPI.wasDragging && e.type=="click") {
			DynAPI.wasDragging=false;
			return true;
		}
	}

	// Get the real source for the event
	if(is.def) var realsrc = Methods.getContainerLayerOf(is.ie?e.srcElement:e.target) || dynobject
	else if(is.ns4) var realsrc=e.target.lyrobj||dynobject

	// Now 'realsrc' should point to the DynLayer object where the event initially was triggered
	if (!realsrc) { alert('Error in MouseEvents'); return false; }

	if(is.def) {
		if(e.type=="mouseout" && realsrc.isParentOf(Methods.getContainerLayerOf(is.ie?e.toElement:e.relatedTarget),true)) return true;
		if(e.type=="mouseover" && realsrc.isParentOf(Methods.getContainerLayerOf(is.ie?e.fromElement:e.relatedTarget),true)) return true;
	}
	var evt=DynMouseEvent._e

	// Step one: properties common to all DynEvents
	var type = e.type
	evt.type = type;
	evt.src=realsrc;
	evt.browserReturn=true;
	evt.bubble=true;

	// Step two: mouse coordinate properties
	evt.pageX=is.ie?e.x+document.body.scrollLeft:e.pageX-window.pageXOffset;
	evt.pageY=is.ie?e.y+document.body.scrollTop:e.pageY-window.pageYOffset;
	evt.x=is.ie?e.x-evt.src.getPageX():e.layerX
	evt.y=is.ie?e.y-evt.src.getPageY():e.layerY

	// Step three: mouse buttons
	var b=is.ie?e.button:e.which;
	if (is.ie){
		if (b==2) b=3;
		else if (b==4) b=2;
	};
	evt.button=b;
	if (evt.button==2 && (type=='mousedown' || type=='mouseup' || type=='mouseclick')) type=evt.type='md'+type;
	if (evt.button==3 && (type=='mousedown' || type=='mouseup' || type=='mouseclick')) type=evt.type='rt'+type;

	// Step four: modifiers
	if (is.def){
		evt.altKey=(e.altKey||e.altLeft);
		evt.ctrlKey=(e.ctrlKey||e.ctrlLeft);
		evt.shiftKey=(e.shiftKey||e.shiftLeft);
	}
	else if (is.ns4){
		var m=e.modifiers;
		evt.altKey=(m==1||m==3||m==5||m==7);
		evt.ctrlKey=(m==2||m==3||m==6||m==7);
		evt.shiftKey=(m==4||m==5||m==6||m==7);
	}

	// Step five: reference to the original event
	evt.orig=e

	if(is.def) {
		if (evt.type=='mouseover') {
			var fromL = Methods.getContainerLayerOf(is.ie?e.fromElement:e.relatedTarget)
			if(fromL && fromL.isChildOf(realsrc.parent,true)) evt.setBubble(false);
		}
		if (evt.type=='mouseout') {
			var toL = Methods.getContainerLayerOf(is.ie?e.toElement:e.relatedTarget)
			if(toL && toL.isChildOf(realsrc.parent,true)) evt.setBubble(false);
		}
	}
	else if(is.ns4 && (e.type=="mouseover" || e.type=="mouseout")) evt.setBubble(false);


	// This forces mouseUps to happen in the same place where mousedowns took place
	if (DynMouseEvent.forceMouseUp && is.def) { 
		if (e.type=='mousedown') DynMouseEvent.focus=realsrc; 
		else if (e.type=='mouseup' && DynMouseEvent.focus!=null) 
		evt.src=realsrc=DynMouseEvent.focus; 
	};

	// Invoke the event
	realsrc.invokeEvent(type,evt);

	// Other checks
	if (is.ns4 && is.platform=="other" && type=="mousedown") {
		if (this.dbltimer!=null) {
			evt.type=type="dblclick";
			evt.bubble = true;
			realsrc.invokeEvent(type,evt);
		}
		else this.dbltimer=setTimeout(this+'.dbltimer=null',300);
	}

	if (is.ns4) {
		if (e.cancelBubble) return false;
		if (e && e.target.handleEvent && e.target!=this) e.target.handleEvent(type,e);
		}
	if (is.ns4 && type=="mouseup") {
		evt.type=type="click";
		evt.bubble = true;
		realsrc.invokeEvent(type,evt);
	}

	// Return value
	return evt.browserReturn;
}


// Extend DynDocument to capture its events
DynDocument.prototype.captureMouseEvents = function() {
	if(is.def&&!is.ie) {
		this.doc.addEventListener("mousemove",DynMouseEvent.EventMethod,false)
		this.doc.addEventListener("mousedown",DynMouseEvent.EventMethod,false)
		this.doc.addEventListener("mouseup",DynMouseEvent.EventMethod,false)
		this.doc.addEventListener("mouseover",DynMouseEvent.EventMethod,false)
		this.doc.addEventListener("mouseout",DynMouseEvent.EventMethod,false)
		this.doc.addEventListener("click",DynMouseEvent.EventMethod,false)
		this.doc.addEventListener("dblclick",DynMouseEvent.EventMethod,false)
	}
	else {
		if (is.ns4) this.doc.captureEvents(Event.MOUSEMOVE | Event.MOUSEDOWN | Event.MOUSEUP | Event.CLICK | Event.DBLCLICK);
		this.doc.onmousemove=this.doc.onmousedown=this.doc.onmouseup=this.doc.onclick=this.doc.ondblclick=DynMouseEvent.EventMethod;
		if (is.ie5) this.doc.oncontextmenu=function(){ return false };
	}
}
DynDocument.prototype.releaseMouseEvents=function() {
	if(is.def&&!is.ie) {
		this.doc.removeEventListener("mousemove",DynMouseEvent.EventMethod,false)
		this.doc.removeEventListener("mousedown",DynMouseEvent.EventMethod,false)
		this.doc.removeEventListener("mouseup",DynMouseEvent.EventMethod,false)
		this.doc.removeEventListener("mouseover",DynMouseEvent.EventMethod,false)
		this.doc.removeEventListener("mouseout",DynMouseEvent.EventMethod,false)
		this.doc.removeEventListener("click",DynMouseEvent.EventMethod,false)
		this.doc.removeEventListener("dblclick",DynMouseEvent.EventMethod,false)
	}
	else {
		if (is.ns4) this.doc.releaseEvents(Event.MOUSEMOVE | Event.MOUSEDOWN | Event.MOUSEUP | Event.CLICK | Event.DBLCLICK);
		this.doc.onmousemove=this.doc.onmousedown=this.doc.onmouseup=this.doc.onclick=this.doc.ondblclick=null
		if (is.ie5) this.doc.oncontextmenu=null
	}
}
/* Extend DynLayer to capture mouse events upon layer creation (if needed) */
DynLayer.prototype.captureMouseEvents = function() {
	var elm=this.elm
	if(!elm) return
	if(is.def&&!is.ie) {
		elm.addEventListener("mousemove",DynMouseEvent.EventMethod,false)
		elm.addEventListener("mousedown",DynMouseEvent.EventMethod,false)
		elm.addEventListener("mouseup",DynMouseEvent.EventMethod,false)
		elm.addEventListener("mouseover",DynMouseEvent.EventMethod,false)
		elm.addEventListener("mouseout",DynMouseEvent.EventMethod,false)
		elm.addEventListener("click",DynMouseEvent.EventMethod,false)
		elm.addEventListener("dblclick",DynMouseEvent.EventMethod,false)
	}
	else {
		if (is.ns4) elm.captureEvents(Event.MOUSEDOWN | Event.MOUSEUP | Event.CLICK | Event.DBLCLICK | Event.MOUSEMOVE);
		elm.onmousemove=elm.onmousedown=elm.onmouseup=elm.onmouseover=elm.onmouseout=elm.onclick=elm.ondblclick=DynMouseEvent.EventMethod
		if (is.ie5) elm.oncontextmenu=function(){return false}
	}
}
DynLayer.prototype.releaseMouseEvents=function() {
	var elm=this.elm
	if(!elm) return
	if(is.def&&!is.ie) {
		elm.removeEventListener("mousemove",DynMouseEvent.EventMethod,false)
		elm.removeEventListener("mousedown",DynMouseEvent.EventMethod,false)
		elm.removeEventListener("mouseup",DynMouseEvent.EventMethod,false)
		elm.removeEventListener("mouseover",DynMouseEvent.EventMethod,false)
		elm.removeEventListener("mouseout",DynMouseEvent.EventMethod,false)
		elm.removeEventListener("click",DynMouseEvent.EventMethod,false)
		elm.removeEventListener("dblclick",DynMouseEvent.EventMethod,false)
	}
	else {
		if (is.ns4) elm.releaseEvents(Event.MOUSEDOWN | Event.MOUSEUP | Event.CLICK | Event.DBLCLICK | Event.MOUSEMOVE);
		elm.onmousemove=elm.onmousedown=elm.onmouseup=elm.onclick=elm.ondblclick=null
		if (is.ie5) elm.oncontextmenu=null
	}
}

DynObject.prototype.assignMouseEvents = function() {
	if (this.hasEventListeners) this.captureMouseEvents()
	var l=this.children.length;
	for (var i=0; i<l; i++) this.children[i].assignMouseEvents()
}
DynObject.prototype._OldM_addEventListener = DynObject.prototype.addEventListener
DynObject.prototype.addEventListener = function(l) {
	var r = this._OldM_addEventListener(l)
	if(this.hasEventListeners && this.created) this.captureMouseEvents()
	return r
}
DynObject.prototype._OldM_removeEventListener = DynObject.prototype.removeEventListener
DynObject.prototype.removeEventListener = function(l) {
	var r = this._OldM_removeEventListener(l)
	if(!this.hasEventListeners) this.releaseMouseEvents()
	return r
}
DynObject.prototype._OldM_removeAllEventListeners = DynObject.prototype.removeAllEventListeners
DynObject.prototype.removeAllEventListeners = function() {
	var r = this._OldM_removeAllEventListeners()
	this.releaseMouseEvents()
	return r
}
// DynLayer Specific
DynLayer.prototype._OldM_specificCreate = DynLayer.prototype.specificCreate
DynLayer.prototype.specificCreate = function() {
	this._OldM_specificCreate()
	this.assignMouseEvents()
}
// DynDocument Specific
DynDocument.prototype._OldM_specificCreate = DynDocument.prototype.specificCreate
DynDocument.prototype.specificCreate = function() {
	this._OldM_specificCreate()
	this.assignMouseEvents()
}
