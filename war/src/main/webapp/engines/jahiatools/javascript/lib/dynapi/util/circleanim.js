/*
   DynAPI Distribution
   CircleAnimation Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
	dynapi.util [thread, pathanim, circleanim]
*/

function CircleAnimation(dlyr) {
	this.Thread = Thread;
	this.Thread(dlyr);

	this.offsetX = 0;
	this.offsetY = 0;
	this.playing = false;
	this.radius = 100;
	this.angle = 0;
	this.setAngleIncrement(10);
}
CircleAnimation.prototype = new Thread;
CircleAnimation.prototype.setRadius = function (r) {
	this.hradius = this.vradius = r;
};
CircleAnimation.prototype.setHRadius = function (r) {
	this.hradius = r;
};
CircleAnimation.prototype.setVRadius = function (r) {
	this.vradius = r;
};
CircleAnimation.prototype.setAngle = function (a) {
	this.angle = PathAnimation.degreeToRadian(a);
};
CircleAnimation.prototype.setAngleIncrement = function (inc) {
	this.angleinc = PathAnimation.degreeToRadian(inc);
};
CircleAnimation.prototype.playAnimation = function () {
	this.playing = true;
	if (this.dlyr!=null) {
		this.offsetX = this.hradius*Math.cos(this.angle);
		this.offsetY = -this.vradius*Math.sin(this.angle);
		this.baseX = this.dlyr.x-this.offsetX;
		this.baseY = this.dlyr.y+this.offsetY;
		this.dlyr.invokeEvent("circlestart");
	}
	this.start();
};
CircleAnimation.prototype.stopAnimation = function () {
	this.playing = false;
	this.stop();
	if (this.dlyr!=null) this.dlyr.invokeEvent("circlestop");
};
CircleAnimation.prototype.run = function () {
	if (!this.playing || this.dlyr==null) return;	
	this.angle += this.angleinc;
	this.offsetX = this.hradius*Math.cos(this.angle);
	this.offsetY = -this.vradius*Math.sin(this.angle);

	if (this.dlyr!=null) {
		this.dlyr.invokeEvent("circlerun");
		this.dlyr.moveTo(this.baseX+this.offsetX,this.baseY+this.offsetY);
	}
};
CircleAnimation.prototype.reset = function () {
	this.angle = this.offsetX = this.offsetY = 0;
};
CircleAnimation.prototype.generatePath = function(centerX,centerY) {
	if (centerX==null) centerX = this.dlyr!=null? this.dlyr.x : 0;
	if (centerY==null) centerY = this.dlyr!=null? this.dlyr.y : 0;
	var path = [];
	var i = 0;
	for (var a=this.angle;a<=this.angle+Math.PI*2;a+=this.angleinc) {
		path[i] = Math.round(centerX + this.hradius*Math.cos(a));
		path[i+1] = Math.round(centerY - this.vradius*Math.sin(a));
		i+=2;
	}
	return path;
};
