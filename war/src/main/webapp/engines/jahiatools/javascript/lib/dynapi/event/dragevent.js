/*
   DynAPI Distribution
   DragEvent Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
*/

// DragEvent object
function DragEvent(type,src) {
	this.DynEvent = DynEvent
	this.DynEvent()
	this.dragEnabled=true;
}
DragEvent.prototype = new DynEvent()
DragEvent.prototype.getX=function() {return this.x;};
DragEvent.prototype.getY=function() {return this.y;};
DragEvent.prototype.getPageX=function() {return this.pageX;};
DragEvent.prototype.getPageY=function() {return this.pageY;};
DragEvent.prototype.cancelDrag=function() {this.dragEnabled=false;};
DragEvent.dragPlay=0;
DragEvent.dragevent=new DragEvent();

DragEvent.lyrListener=new EventListener();
DragEvent.lyrListener.onmousedown=function(e) {

	if (is.def) e.cancelBrowserEvent();
	if (DragEvent.dragevent.src) return;

	var lyr=e.getSource();
	if (is.ie) lyr.doc.body.onselectstart = function() { return false; }

	// Initialize dragEvent object
	var de=DragEvent.dragevent;
	de.type="dragstart"
	de.src=lyr

	// Set properties
	de.isDragging=false;
	de.x=e.getPageX()-e.getSource().getPageX();
	de.y=e.getPageY()-e.getSource().getPageY();
	de.pageX=e.getPageX();
	de.pageY=e.getPageY();
	de.parentPageX=lyr.parent.getPageX();
	de.parentPageY=lyr.parent.getPageY();
};

DragEvent.docListener=new EventListener();
DragEvent.docListener.onmousemove=function(e) {
	// Get, if any, the currently drag in process and the layer. If none, return
	var de = DragEvent.dragevent;
	if (!de) return;
	var lyr = de.src;
	if (!lyr) return;

	if(!de.isDragging)
	// Detect if we should start the drag
	if(DragEvent.dragPlay==0 || (Math.abs(de.pageX-e.getPageX())-DragEvent.dragPlay>0) || (Math.abs(de.pageY-e.getPageY())-DragEvent.dragPlay>0)) {
		de.isDragging=true;
		de.src.invokeEvent("dragstart",de);
		e.setBubble(de.bubble);
	}
	if (!de.isDragging) return;
	else if (!de.dragEnabled) {
		// This allows 'cancelDrag' method to fire the mouseUp as if had been released by the user
		lyr.invokeEvent("mouseup");
		return;
	}

	// Properties
	de.type="dragmove";
	de.pageX=e.getPageX();
	de.pageY=e.getPageY();
	var x=de.pageX-de.parentPageX-de.x;
	var y=de.pageY-de.parentPageY-de.y;

	// Respect boundary, if any
	if (lyr.dragBoundary) {
		var dB=lyr.dragBoundary;
		if (dB=="parent") {
			var b=lyr.parent.getHeight();
			var r=lyr.parent.getWidth();
			var l=0;
			var t=0;
		} else {
			var b=dB[2];
			var r=dB[1];
			var l=dB[3];
			var t=dB[0];
		}
		var w=lyr.w;
		var h=lyr.h;
		if (x<l) x=l;
		else if (x+w>r) x=r-w;
		if (y<t) y=t;
		else if (y+h>b) y=b-h;
	}

	// Move dragged layer
	lyr.moveTo(x,y);
	lyr.invokeEvent("dragmove",de);
	e.cancelBrowserEvent();
	e.setBubble(de.bubble);
};
DragEvent.docListener.onmouseup=function(e) {
	// Get, if any, the currently drag in process and the layer. If none, return
	var de=DragEvent.dragevent;
	if (!de) return;
	var lyr=de.src;
	if (!lyr) return;

	if (!de.isDragging) {
	    	de.type="dragend";
    		de.src=null;
    		e.setBubble(true);
		return;
	}
	if (is.ie) lyr.doc.body.onselectstart = null;

	// Avoid click for the dragged layer ( with MouseEvent addition )
	if (is.def) DynAPI.wasDragging=true;
	if (lyr.parent.DragDrop) lyr.parent.DragDrop(lyr); 
	// Properties for the event
	de.type="dragend";
	de.isDragging=false;
	lyr.invokeEvent("dragend",de);

	// Clean drag stuff
	de.src=null;
	e.cancelBrowserEvent();
	e.setBubble(de.bubble);
};
DragEvent.setDragBoundary=function(dlyr,t,r,b,l) {
	var a=arguments;
	if (a.length==0) return;
	if (a.length==1) dlyr.dragBoundary="parent";
	else if (a.length==5) dlyr.dragBoundary=new Array(t,r,b,l);
};
DragEvent.enableDragEvents=function(f) {
	for (var i=0;i<arguments.length;i++) {
		var lyr=arguments[i];
		if (lyr.isDynLayer) lyr.addEventListener(DragEvent.lyrListener);
	}
	if(f.isDynDocument) f.addEventListener(DragEvent.docListener);
	else DynAPI.document.addEventListener(DragEvent.docListener);
};
DragEvent.disableDragEvents=function() {
	for (var i=0;i<arguments.length;i++) {
		var lyr=arguments[i];
		lyr.removeEventListener(DragEvent.lyrListener);
	}
};
