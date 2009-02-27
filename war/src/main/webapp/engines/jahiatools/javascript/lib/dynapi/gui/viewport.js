/*
   DynAPI Distribution
   ViewPort Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api.*
	dynapi.util [thread, pathanim]
	dynapi.gui [label]
*/

// note: we need an onremove event

function ViewPort(content) {
	this.DynLayer = DynLayer;
	this.DynLayer();

	this.contentPane = new DynLayer();
	this.addChild(this.contentPane);
	
	this.bufferW = 0;
	this.bufferH = 0;

	var scrollEvent = new EventListener(this);
	scrollEvent.onpathrun = function(e) {
		e.getTarget().invokeEvent('scroll');
	};
	this.contentPane.addEventListener(scrollEvent);

	var viewportListener = new EventListener(this);
	viewportListener.onresize = function(e) {
		var o = e.getTarget();
		if (!o.created || !o.content) return;

		o.findDimensions();
		
		if (!o.enableHScroll) o.contentPane.setX(0);
		else if (o.contentPane.x<-o.availableScrollX) {o.contentPane.setX(-o.availableScrollX);}
		
		if (!o.enableVScroll) o.contentPane.setY(0);
		else if (o.contentPane.y<-o.availableScrollY) {o.contentPane.setY(-o.availableScrollY);}
		
		o.invokeEvent("scroll");
	};
	viewportListener.oncreate = function(e) {
		var o = e.getTarget();
		if(is.def && o.css) o.css.overflow='hidden'
		o.reset(false);
	};
	this.addEventListener(viewportListener);

	this.contentResizeListener = new EventListener(this);
	this.contentResizeListener.onresize = function(e) {
		var o = e.getTarget();
		o.findDimensions();
		o.invokeEvent("contentchange");
	};
	this.contentResizeListener.onload = function(e) {  // for loadpanel
		var o = e.getTarget();
		if (o.created && o.content) {
			o.reset();
		}
	};
	
	this.setContent(content);
}

ViewPort.prototype = new DynLayer;

ViewPort.prototype.reset = function(b) {
	this.contentPane.moveTo(0,0);
	this.findDimensions();
	if (b!=false) this.invokeEvent("contentchange");
};
ViewPort.prototype.setContent = function(content) {
	if (this.content && this.contentPane.children.length>0) {
		if (this.content==content) return;
		this.content.removeFromParent();
		this.content.removeEventListener(this.contentResizeListener);
	}
	if (!content) this.content = new DynLayer();
	else this.content = content;
	
	this.content.moveTo(0,0);
	this.contentPane.moveTo(0,0);

	this.contentPane.addChild(this.content);
	this.content.addEventListener(this.contentResizeListener);
	
	this.findDimensions();

	this.invokeEvent("contentchange");
};
ViewPort.prototype.findDimensions = function() {
	if (!this.content) return;
	this.contentPane.setSize(this.content.getWidth(),this.content.getHeight());
	
	this.availableScrollX = this.content.getWidth()-this.getWidth()+this.bufferW;
	this.availableScrollY = this.content.getHeight()-this.getHeight()+this.bufferH;
	this.enableHScroll = this.availableScrollX>0;
	this.enableVScroll = this.availableScrollY>0;
};
ViewPort.prototype.jumpTo = function(x,y) {
	this.content.moveTo(x,y);
	this.invokeEvent("scroll");
};
ViewPort.prototype.setRatio = function(rx,ry) {
	this.setRatioX(rx);
	this.setRatioY(ry);
};
ViewPort.prototype.setRatioX = function(rx) {
	if (rx!=null) this.contentPane.setX(-this.availableScrollX*rx);
};
ViewPort.prototype.setRatioY = function(ry) {
	if (ry!=null) this.contentPane.setY(-this.availableScrollY*ry);
};
ViewPort.prototype.getRatioX = function() {
	if (!this.content || !this.enableHScroll) return 0;
	else if (this.contentPane.x==0) return 0;
	else if (this.contentPane.x==-this.availableScrollX) return 1;
	else return 1-(this.availableScrollX+this.contentPane.x)/this.availableScrollX;
};
ViewPort.prototype.getRatioY = function() {
	if (!this.content || !this.enableVScroll) return 0;
	else if (this.contentPane.y==0) return 0;
	else if (this.contentPane.y==-this.availableScrollY) return 1;
	else return 1-(this.availableScrollY+this.contentPane.y)/this.availableScrollY;
};
	
ViewPort.prototype.scrollUp = function() {this.scrollSlide(null,0);};
ViewPort.prototype.scrollDown = function() {this.scrollSlide(null,-this.availableScrollY);};
ViewPort.prototype.scrollLeft = function() {this.scrollSlide(0,null);};
ViewPort.prototype.scrollRight = function() {this.scrollSlide(-this.availableScrollX,null);};
ViewPort.prototype.scrollSlide = function(x,y) {
	if (x!=null && this.enableHScroll) {
		this.invokeEvent("scrollstart");
		this.contentPane.slideTo(x,this.contentPane.y);
	}
	else if (y!=null && this.enableVScroll) {
		this.invokeEvent("scrollstart");
		this.contentPane.slideTo(this.contentPane.x,y);
	}
};
ViewPort.prototype.cancelScroll = function() {
	this.contentPane.stopSlide();
	this.invokeEvent("scrollend");
};
