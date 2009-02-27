/*
   DynAPI Distribution
   ScrollBar Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api.*
	dynapi.util [thread, pathanim]
	dynapi.gui [dynimage]
*/

// to do: add support for no images

function ScrollBar(orientation,bUseImages) {
	this.DynLayer = DynLayer;
	this.DynLayer();
	
	this.knob = this.addChild(new DynLayer());

	this.dragEvents = new EventListener(this);
	this.dragEvents.ondragmove = function (e) {
		var o = e.getTarget();
		o.findRatio();
		o.invokeEvent("scroll");
		e.setBubble(false);
	};
	this.dragEvents.ondragstart = function (e) {
		var o = e.getTarget();
		e.setBubble(false);
	};
	this.dragEvents.ondragend = function (e) {
		var o = e.getTarget();
		e.setBubble(false);
	};
	DragEvent.setDragBoundary(this.knob);
	DragEvent.enableDragEvents(this.knob);
	
	this.knob.addEventListener(this.dragEvents);
		
	this.mouseEvents = new EventListener(this);
	this.mouseEvents.onmousemove = function(e) {
		var knob = e.getTarget().knob;
		if (knob.pathanim && knob.pathanim.playing) e.setBubble(false);
	};
	this.mouseEvents.onmousedown = function(e) {
		var lyr = e.getSource()
		var o = e.getTarget()
		if (!lyr.pathanim || !lyr.pathanim.playing) {
			var newx = e.getX()-Math.ceil(o.knob.w/2);
			var newy = e.getY()-Math.ceil(o.knob.h/2);
			var offW = o.getWidth()-o.knob.w;
			var offH = o.getHeight()-o.knob.h;
			if (newx<0) newx=0;
			if (newx>=offW) newx=offW;
			if (newy<0) newy=0;
			if (newy>=offH) newy=offH;
						
			o.knob.slideTo(newx,newy);
		}
		e.setBubble(false);
	};
	this.mouseEvents.onmouseup = function(e) {
		e.getTarget().knob.stopSlide();
	};
	this.addEventListener(this.mouseEvents);
	
	var slideEvents = new EventListener(this);
	slideEvents.onpathrun = function(e) {
		var o = e.getTarget();
		o.findRatio();
		o.invokeEvent("scroll");
	};
	slideEvents.onpathstop = function(e) {
		var lyr = e.getSource();
		var o = e.getTarget();
		var evt = new DynMouseEvent();
		evt.bubble = false;
		evt.type = "mousedown";
		evt.src = lyr;
		evt.x = o.knob.getWidth()/2;
		evt.y = o.knob.getHeight()/2;
		evt.pageX = o.knob.getPageX()+evt.x;
		evt.pageY = o.knob.getPageY()+evt.y;
		lyr.invokeEvent("mousedown",evt);
	};
	this.knob.addEventListener(slideEvents);
	
	var resizeEvents = new EventListener(this);
	resizeEvents.onresize = function(e) {
		var o = e.getTarget();
		if (o.getWidth()>0 && o.knob.x+o.knob.w>o.getWidth()) o.knob.setX(o.getWidth()-o.knob.w);
		if (o.getHeight()>0 && o.knob.y+o.knob.h>o.getHeight()) o.knob.setY(o.getHeight()-o.knob.h);
		o.findRatio();
	};
	this.addEventListener(resizeEvents);
	
	this.setOrientation(orientation);
	if (bUseImages!=false) this.setTheme(MetalScrollBar());
};

ScrollBar.prototype = new DynLayer;

ScrollBar.prototype.ratiox = 0;
ScrollBar.prototype.ratioy = 0;

ScrollBar.prototype.setOrientation = function(type) { // 0=horz,1=vert
	this.horizontal = (type==ScrollBar.HORIZONTAL);
	this.vertical = (type==ScrollBar.VERTICAL);
};
ScrollBar.prototype.setTheme = function(theme) {
	if (!theme) return;
	if (this.horizontal) this.setImages(theme.htrough, theme.hknob);
	else if (this.vertical) this.setImages(theme.vtrough, theme.vknob);
}
ScrollBar.prototype.setImages = function(troughImage,knobImage) {
	if (troughImage) {
		this.troughImage = troughImage;
		this.setBgImage(this.troughImage.src);
		if (this.vertical) this.setWidth(this.troughImage.width,false);
		if (this.horizontal) this.setHeight(this.troughImage.height,false);
	}
	if (knobImage) {
		this.knobImage = knobImage;
		this.knob.setBgImage(this.knobImage.src);
		this.knob.setSize(this.knobImage.width,this.knobImage.height);
	}
	this.hasImages = true;
};
ScrollBar.prototype.setRatio = function(rx,ry) {
	this.setRatioX(rx);
	this.setRatioY(ry);
};
ScrollBar.prototype.setRatioX = function(rx) {
	this.knob.setX(Math.floor(rx*(this.getWidth()-this.knob.getWidth())));
};
ScrollBar.prototype.setRatioY = function(ry) {
	this.knob.setY(Math.floor(ry*(this.getHeight()-this.knob.getHeight())));
};
ScrollBar.prototype.getRatioX = function() {
	return this.ratiox;
};
ScrollBar.prototype.getRatioY = function() {
	return this.ratioy;
};

ScrollBar.prototype.findRatio = function() {
	var tx = (this.getWidth()-this.knob.w);
	var ty = (this.getHeight()-this.knob.h);
	this.ratiox = tx==0 ? 0 : (this.knob.x)/tx;
	this.ratioy = ty==0 ? 0 : (this.knob.y)/ty;
};
ScrollBar.prototype.reset = function() { 
	this.knob.moveTo(0,0); 
	this.ratiox=this.ratioy=0;
};
ScrollBar.VERTICAL = 1;
ScrollBar.HORIZONTAL = 2;

function MetalScrollBar () {
	if (!DynAPI.librarypath) return null;
	return {
		vtrough : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-vbg.gif',16,16),
		vknob : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-vknob.gif',16,37),
		htrough : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-hbg.gif',16,16),
		hknob : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-hknob.gif',37,16)
	}
}