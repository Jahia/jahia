/*
   DynAPI Distribution
   Button Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
	dynapi.gui [label, dynimage]
*/

// to do:
// do support for new Button(new DynImage()) and new Button(new Label())

function Button() {   // Image() or "string"
	
	this.DynLayer = DynLayer;
	this.DynLayer();
	
	this.setBgColor("#CECECE");
	
	if (typeof(arguments[0])=="string") {
		this.setText(arguments[0]);
	}
	else if (typeof(arguments[0])=="object") {
		this.setImage(arguments[0]);
	}
	
	this.t = this.addChild(new DynLayer(null,0,0,0,1,"#636363"));
	this.r = this.addChild(new DynLayer(null,0,0,1,0,"#636363"));
	this.b = this.addChild(new DynLayer(null,0,0,0,1,"#636363"));
	this.l = this.addChild(new DynLayer(null,0,0,1,0,"#636363"));
	
	this.bvt = this.addChild(new DynLayer(null,1,1,0,1,"#FFFFFF"));
	this.bvl = this.addChild(new DynLayer(null,1,1,1,0,"#FFFFFF"));

	var listener = new EventListener(this);
	listener.oncreate = function(e) {
		var o = e.getTarget();
		//alert(o.w+' '+o.h);
		if (o.label) o.setText(o.text)
		if (o.w==null && o.h==null) {
			if (o.label) {
				o.setSize(o.label.w+4,o.label.h+4);
				//alert(o.label.w)
			}
			else if (o.imglyr) o.setSize(o.imglyr.w+4,o.imglyr.h+4);
		}
		else o.recenter();
		if (o.label) o.label.setVisible(true);
		else if (o.imglyr) o.imglyr.setVisible(true);
	};
	listener.onresize = function(e) {
		var o = e.getTarget();
		
		o.t.setWidth(o.w);
		o.b.setWidth(o.w);
		o.r.setHeight(o.h);
		o.l.setHeight(o.h);
		
		o.r.setX(o.w-1);
		o.b.setY(o.h-1);
		
		o.bvt.setWidth(o.w-2);
		o.bvl.setHeight(o.h-2);
		
		o.recenter();
	};
	listener.onmousedown = function(e) {
		var o = e.getTarget();
		if (o.enabled!=false) o.setDown();
	};
	listener.onmouseup = function(e) {
		var o = e.getTarget();
		if (o.enabled!=false) o.setUp();
	};
	this.addEventListener(listener);
};
Button.prototype = new DynLayer;
Button.prototype.setImage = function(imgObj) {
	this.img = imgObj;
	if (!this.imglyr) {
		this.imglyr = this.addChild(new DynImage(imgObj));
		this.imglyr.setVisible(false);
	}
	else this.imglyr.setImage(imgObj);
  	this.imglyr.addEventListener(Button.imglyrlistener); //added
};
Button.prototype.setText = function(text) {
	this.text = text
	if (!this.label) {
		this.label = this.addChild(new Label(arguments[0]));
		this.label.setVisible(false);
		this.label.setSelectable(false);
		if (this.padding) this.label.setPadding(this.padding);
		this.label.pack();
	}
	else {
		this.label.setText(text);
		this.label.pack();
	}
	this.recenter();
};
Button.prototype.setPadding = function(p) { // only for label for right now
	this.padding = p;
	if (this.label) this.label.setPadding(p);
};
Button.prototype.setEnabled = function(b) { // only for label for right now
	this.enabled = b;
};
Button.prototype.recenter = function() {
	if (!this.created) return;
	if (this.label) {
		var x = Math.round((this.w-this.label.w)/2);
		var y = Math.round((this.h-this.label.h)/2);
		this.label.moveTo(x,y);
	}
	else if (this.imglyr) {
		var x = Math.round((this.w-this.imglyr.w)/2);
		var y = Math.round((this.h-this.imglyr.h)/2);
		this.imglyr.moveTo(x,y);
	}
};
Button.prototype.setDown = function() {
	this.bvt.setVisible(false);
	this.bvl.setVisible(false);
	this.setBgColor("#9C9C9C");
};
Button.prototype.setUp = function() {
	this.bvt.setVisible(true);
	this.bvl.setVisible(true);
	this.setBgColor("#CECECE");
};
Button.imglyrlistener=new EventListener();
Button.imglyrlistener.onresize=function(e){
	var o = e.getSource();
	o.w=o.img.width; o.h=o.img.height; o.parent.recenter();
}