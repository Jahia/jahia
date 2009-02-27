/*
   DynAPI Distribution
   PushPanel Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api.*
	dynapi.util [thread, pathanim]
	dynapi.gui [button, label, viewport, dynimage]
*/

function PushPanel(content) {
	this.ViewPort = ViewPort;
	this.ViewPort(content);
	
	this.inc = 7;
	
	this.button0 = this.addChild(new Button());
	this.button1 = this.addChild(new Button());
	this.button0.setVisible(false);
	this.button1.setVisible(false);

	var button0Listener = new EventListener(this);
	button0Listener.onmousedown = function(e) {
		var o = e.getTarget();
		if (o.vertical) o.scrollUp();
		else o.scrollLeft();
	};
	var button1Listener = new EventListener(this);
	button1Listener.onmousedown = function(e) {
		var o = e.getTarget();
		if (o.vertical) o.scrollDown();
		else o.scrollRight();
	};
	var cancelListener = new EventListener(this);
	cancelListener.onmouseup = function(e) {
		var o = e.getTarget();
		o.cancelScroll();
		o.checkButtons();
	};
	
	this.button0.addEventListener(button0Listener);
	this.button0.addEventListener(cancelListener);
	this.button1.addEventListener(button1Listener);
	this.button1.addEventListener(cancelListener);
	
	this.setVertical();

	this.addEventListener(PushPanel.viewportListener);
	
	this.setTheme(MetalPushPanel());
}
PushPanel.prototype = new ViewPort;

PushPanel.viewportListener = new EventListener();
PushPanel.viewportListener.onprecreate = function(e) {
	var o = e.getSource();
	
	if (o.vertical) {
		o.button0.setImage(o.theme.up);
		o.button1.setImage(o.theme.dn);
	}
	else if (o.horizontal) {
		o.button0.setImage(o.theme.lt);
		o.button1.setImage(o.theme.rt);
	}
	o.resizeButtons();
}
PushPanel.viewportListener.oncreate = function(e) {
	var o = e.getSource();
	o.checkButtons();
};
PushPanel.viewportListener.onresize = function(e) {
	var o = e.getSource();

	if (o.created) o.resizeButtons();
	o.checkButtons();
};
PushPanel.viewportListener.oncontentchange = function(e) {
	var o = e.getSource();
	if (!o.created) return;

	o.resizeButtons();
	o.checkButtons();
};
PushPanel.viewportListener.onscroll = function(e) {
	var o = e.getSource();
	if (!o.created) return;
	
	o.checkButtons();
};

PushPanel.prototype.resizeButtons = function() {
	if (this.vertical) {
		this.button0.setSize(this.w,16);
		this.button1.setY(this.h-16);
		this.button1.setSize(this.w,16);
	}
	else {
		this.button0.setSize(16,this.h);
		this.button1.setX(this.w-16);
		this.button1.setSize(16,this.h);
	}
};

PushPanel.prototype.checkButtons = function() {
	if (!this.created) return;
	
	var b0vis = false;
	var b1vis = false;
	if (this.vertical) {
		if (this.availableScrollY>0) {
			if (this.contentPane.y==0) b1vis = true;
			else if (this.contentPane.y==-this.availableScrollY) b0vis = true;
			else b0vis = b1vis = true;
		}
	}
	else if (this.horizontal) {
		if (this.availableScrollX>0) {
			if (this.contentPane.x==0) b1vis = true;
			else if (this.contentPane.x==-this.availableScrollX) b0vis = true;
			else b0vis = b1vis = true;
		}
	}
	if (this.button0.visible!=b0vis) this.button0.setVisible(b0vis);
	if (this.button1.visible!=b1vis) this.button1.setVisible(b1vis);
	
	if (!b0vis) this.button0.setUp();
	if (!b1vis) this.button1.setUp();
	//alert('check '+b0vis+' '+b1vis+' '+this.horizontal+' '+this.button1.visible+' '+this.button1.created);
};

PushPanel.prototype.setVertical = function() {
	this.vertical = true;
	this.horizontal = false;
}
PushPanel.prototype.setHorizontal = function() {
	this.vertical = false;
	this.horizontal = true;
}

PushPanel.prototype.setTheme = function(theme) {
	this.theme = theme;
}

function MetalPushPanel(url) {
	if (!DynAPI.librarypath) return null;
	return {
		up : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowup.gif',9,5),
		dn : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowdown.gif',9,5),
		lt : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowleft.gif',5,9),
		rt : DynImage.getImage(DynAPI.librarypath+'dynapi/images/common/arrowright.gif',5,9)
	};
};
