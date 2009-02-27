/*
   DynAPI Distribution
   HoverAnimation Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
	dynapi.util [thread, pathanim, hoveranim]
*/

function HoverAnimation(dlyr) {
	this.Thread = Thread;
	this.Thread(dlyr);

	this.offsetX = 0;
	this.offsetY = 0;
	this.playing = false;
	this.amplitude = 100;
	this.angle = 0;
	this.setAngleIncrement(10);
}
HoverAnimation.prototype = new Thread;
HoverAnimation.prototype.setAmplitude = function (amp) {
	this.amplitude = amp;
};
HoverAnimation.prototype.setAngle = function (a) {
	this.angle = PathAnimation.degreeToRadian(a);
};
HoverAnimation.prototype.setAngleIncrement = function (inc) {
	this.angleinc = PathAnimation.degreeToRadian(inc);
};
HoverAnimation.prototype.playAnimation = function () {
	this.playing = true;
	if (this.dlyr!=null) {
		this.offsetX = 0;
		this.offsetY = this.amplitude*Math.sin(this.angle);
		this.baseX = this.dlyr.x;
		this.baseY = this.dlyr.y+this.offsetY;
		this.dlyr.invokeEvent("hoverstart");
	}
	this.start();
};
HoverAnimation.prototype.stopAnimation = function () {
	this.playing = false;
	this.stop();
	if (this.dlyr!=null) this.dlyr.invokeEvent("hoverstop");
};
HoverAnimation.prototype.run = function () {
	if (!this.playing || this.dlyr==null) return;
	this.angle += this.angleinc;
	this.offsetX = 0;
	this.offsetY = this.amplitude*Math.sin(this.angle);
	if (this.dlyr!=null) {
		this.dlyr.invokeEvent("hoverrun");
		this.dlyr.moveTo(this.baseX+this.offsetX,this.baseY+this.offsetY);
	}
};
HoverAnimation.prototype.reset = function () {
	this.angle = this.offsetX = this.offsetY = 0;
};

HoverAnimation.prototype.generatePath = function(centerX,centerY) {
	// to do
};
