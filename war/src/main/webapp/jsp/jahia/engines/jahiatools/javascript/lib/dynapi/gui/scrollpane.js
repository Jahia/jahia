/*
   DynAPI Distribution
   ScrollPane Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
   
   Requirements:
	dynapi.api.*
	dynapi.util [thread, pathanim]
	dynaip.gui [buttonimage, scrollbar, viewport, dynimage, label]
*/

function ScrollPane(content) {
	this.ViewPort = ViewPort;
	this.ViewPort(content);
		
	this.corner = this.addChild(new Button());
	this.corner.setEnabled(false);
	this.corner.setVisible(false);
	
	this.vbar = this.addChild(new ScrollBar(ScrollBar.VERTICAL));
	this.vbar.direction = "v";
	this.hbar = this.addChild(new ScrollBar(ScrollBar.HORIZONTAL));
	this.hbar.direction = "h";
	
	this.rt = this.addChild(new Button());
	this.rt.direction = "Right";
	this.lt = this.addChild(new Button());
	this.lt.direction = "Left";
	this.up = this.addChild(new Button());
	this.up.direction = "Up";
	this.dn = this.addChild(new Button());
	this.dn.direction = "Down";
	
	this.setVertVisible(false);
	this.setHorzVisible(false);
	
	var buttonListener = new EventListener(this);
	buttonListener.onmousedown = function(e) {
		var scroll = e.getTarget();
		scroll["scroll"+e.getSource().direction]();
		scroll.findDimensions();
	};
	buttonListener.onmouseup = function(e) {
		e.getTarget().cancelScroll();
	};
	
	this.rt.addEventListener(buttonListener);	
	this.lt.addEventListener(buttonListener);
	this.up.addEventListener(buttonListener);
	this.dn.addEventListener(buttonListener);
	
	var barListener = new EventListener(this);
	barListener.onscroll = function(e) {
		var bar = e.getSource();
		var o = e.getTarget();
		if (bar.direction=="v") o.setRatioY(bar.getRatioY());
		else o.setRatioX(bar.getRatioX());
	};
	this.vbar.addEventListener(barListener);
	this.hbar.addEventListener(barListener);

	this.addEventListener(ScrollPane.listener);
	
	this.setTheme(MetalScrollPane());
}

ScrollPane.listener = new EventListener();
ScrollPane.listener.onprecreate = function(e) {
	var o = e.getSource();
	if (!o.theme) {
		alert("error: no theme"); 
		return;
	}

	var w = o.theme.vknob.width;
	var h = o.theme.hknob.height;

	o.vbar.setImages(o.theme.vbg, o.theme.vknob);
	o.hbar.setImages(o.theme.hbg, o.theme.hknob);

	o.corner.setSize(w,h);

	o.rt.setSize(w,h);
	o.rt.setImage(o.theme.rt);
	o.lt.setSize(w,h);
	o.lt.setImage(o.theme.lt);
	o.up.setSize(w,h);
	o.up.setImage(o.theme.up);
	o.dn.setSize(w,h);
	o.dn.setImage(o.theme.dn);
};
ScrollPane.listener.onscroll = function(e) {
	var o = e.getSource();
	if (!o.created) return;

	o.hbar.setRatioX(o.getRatioX());
	o.vbar.setRatioY(o.getRatioY());
};
ScrollPane.listener.onresize = function(e) {
	var o = e.getSource();
	if (!o.created || !o.content) return;

	var bw = o.hbar.h;
	var bh = o.vbar.w;
	var olap = o.theme.overlap;

	o.bufferW = bw;
	o.bufferH = bh;
	o.findDimensions();

	o.setVertVisible(o.enableVScroll);
	o.setHorzVisible(o.enableHScroll);

	if (o.enableVScroll && o.enableHScroll) {
		o.bufferW = bw;
		o.bufferH = bh;
		o.corner.setVisible(true);
		o.corner.moveTo(o.w-bw,o.h-bh);

		o.vbar.moveTo(o.w-bw,bh-olap);
		o.vbar.setHeight(o.h-3*bh+2*olap);
		o.up.setX(o.w-bw);
		o.dn.moveTo(o.w-bw,o.h-2*bh+olap);

		o.hbar.moveTo(bw-olap,o.h-bh);
		o.hbar.setWidth(o.w-3*bw+2*olap);
		o.lt.setY(o.h-bh);
		o.rt.moveTo(o.w-2*bw+olap,o.h-bh);

	}
	else if (o.enableVScroll) {
		o.bufferH = 0;
		o.corner.setVisible(false);

		o.vbar.moveTo(o.w-bw,bh-olap);
		o.vbar.setHeight(o.h-2*bh+olap);
		o.up.setX(o.w-bw);
		o.dn.moveTo(o.w-bw,o.h-bh)
	}
	else if (o.enableHScroll) {
		o.bufferW = 0;
		o.corner.setVisible(false);

		o.hbar.moveTo(bw-olap,o.h-bh);		
		o.hbar.setWidth(o.w-2*bw+olap);
		o.lt.setY(o.h-bh);
		o.rt.moveTo(o.w-bw,o.h-bh);
	}
	else {
		o.corner.setVisible(false);
	}		
	o.findDimensions();

	o.hbar.findRatio();
	o.vbar.findRatio();

	o.invokeEvent("scroll");
};
ScrollPane.listener.oncontentchange = function(e) {
	var o = e.getSource();
	o.setSize(o.w,o.h);
};

ScrollPane.prototype = new ViewPort;

ScrollPane.prototype.setVertVisible = function(b) {
	this.vbar.setVisible(b);
	this.up.setVisible(b);
	this.dn.setVisible(b);
};
ScrollPane.prototype.setHorzVisible = function(b) {
	this.hbar.setVisible(b);
	this.lt.setVisible(b);
	this.rt.setVisible(b);
};

ScrollPane.prototype.setTheme = function(theme) {
	this.theme = theme;
};

function MetalScrollPane() {
	if (!DynAPI.librarypath) return null;
	return {
		vbg : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-vbg.gif'),
		vknob : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-vknob.gif'),
	
		hbg : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-hbg.gif'),
		hknob : DynImage.getImage(DynAPI.librarypath+'dynapi/images/scrollpane/scrollbar-hknob.gif'),

		up : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowup.gif'),
		dn : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowdown.gif'),
		lt : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowleft.gif'),
		rt : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowright.gif'),
		
		overlap : 1
	};
};
