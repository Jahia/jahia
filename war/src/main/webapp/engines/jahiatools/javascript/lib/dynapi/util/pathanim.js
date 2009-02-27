/*
   DynAPI Distribution
   PathAnimation Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
	dynapi.util [thread]
*/

function PathAnimation(dlyr) {
	this.Thread = Thread;
	this.Thread(dlyr);
	this.paths = new Array();
	this.pathPlaying = null;
}
PathAnimation.prototype = new Thread;

PathAnimation.prototype.addAnimation = function (path, loops, resets) {
	var n = this.paths.length;
	this.paths[n] = path;
	this.setLoops(n,loops);
	this.setResets(n,resets);
	this.setFrame(n,0);
	return n;
};
PathAnimation.prototype.setLoops = function (n, loops) {
	this.paths[n].loops = (loops);
};
PathAnimation.prototype.setResets = function (n, resets) {
	this.paths[n].resets = (resets);
};
PathAnimation.prototype.setFrame = function (n, frame) {
	this.paths[n].frame = frame;
};
PathAnimation.prototype.playAnimation = function (noevt) {
	if (this.playing) return;
	this.pathPlaying = null;
	if (typeof(arguments[0]) == "number") {
		this.pathPlaying = this.paths[arguments[0]];
	}
	else if (typeof(arguments[0]) == "object") {  
		this.pathPlaying = arguments[0];
		this.pathPlaying.loops = arguments[1]||false;
		this.pathPlaying.resets = arguments[2]||false;
		this.pathPlaying.frame = 0;
	}
	this.playing = true;
	if (this.dlyr!=null && noevt!=false) this.dlyr.invokeEvent("pathstart");
	this.start();
};
PathAnimation.prototype.stopAnimation = function (noevt) {
	if (this.pathPlaying && this.pathPlaying.resets && !this.cancelThread && this.dlyr!=null) this.dlyr.moveTo(this.pathPlaying[0],this.pathPlaying[1]);
	this.stop();
	delete this.pathPlaying; 
	this.playing = false;
	if (this.dlyr!=null && noevt!=false) this.dlyr.invokeEvent("pathstop");
};
PathAnimation.prototype.run = function () {
	if (!this.playing || this.pathPlaying==null) return;
	var anim = this.pathPlaying;
	if (anim.frame>=anim.length/2) {
		if (anim.loops) {
			anim.frame = 0;
		}
		else if (anim.resets) {
			anim.frame = 0;
			if (this.dlyr!=null) this.dlyr.moveTo(anim[0],anim[1]);
			this.stopAnimation();
			return;
		}
		else {
			anim.frame = 0;
			this.stopAnimation();
			return;
		}
	}
	if (anim.frame==0 && (this.dlyr!=null && this.dlyr.x==anim[0] && this.dlyr.y==anim[1])) {
		anim.frame += 1;
	}
	this.newX = anim[anim.frame*2];
	this.newY = anim[anim.frame*2+1];
	
	if (this.dlyr!=null) {
		this.dlyr.invokeEvent("pathrun");
		this.dlyr.moveTo(this.newX,this.newY);
	}
	anim.frame++;
};

// DynLayer Extensions
// These will invoke "pathstart", "pathrun", and "pathstop" events on the dynlayer when used
DynLayer.prototype.slideTo = function(x,y,inc,ms) {
	if (!this.pathanim) this.pathanim = new PathAnimation(this);
	if (!ms) ms = 20;
	if (!inc) inc = 10;
	if (x==null) x = this.x;
	if (y==null) y = this.y;
	this.pathanim.sleep(ms);
	this.pathanim.playAnimation(PathAnimation.line(this.x,this.y, x,y, inc));
};
DynLayer.prototype.stopSlide = function () {
	if (this.pathanim) this.pathanim.stopAnimation();
};

// Path Functions
// Generates a path between 2 points, stepping inc pixels at a time
PathAnimation.line = function(x1,y1,x2,y2,inc) {
	var distx = x2 - x1;
	var disty = y2 - y1;
	var N = Math.floor(Math.sqrt(distx*distx + disty*disty)/inc);
	var a = PathAnimation.getNormalizedAngle(x1,y1,x2,y2);
	var dx = inc * Math.cos(a);
	var dy = -inc * Math.sin(a);
	var path = [];
	for (var i=0;i<=N;i++) {
		path[i*2] = Math.round(x1 +  i*dx);
		path[i*2+1] = Math.round(y1 + i*dy);
	}
	if (path[i*2-2] != x2 || path[i*2-1] != y2) {
		path[i*2] = x2;
		path[i*2+1] = y2;
	}
	return path;
};
// Generates a path between 2 points in N steps
PathAnimation.lineN = function(x1,y1,x2,y2,N) {
	if (N==0) return [];
	var dx = (x2 == x1)? 0 : (x2 - x1)/N;
	var dy = (y2 == y1)? 0 : (y2 - y1)/N;
	var path = new Array();
	for (var i=0;i<=N;i++) {
		path[i*2] = Math.round(x1 + i*dx);
		path[i*2+1] = Math.round(y1 + i*dy);
	}
	return path;
};
// Combines separate [x1,x2],[y1,y2] arrays into a path array [x1,y1,x2,y2]
PathAnimation.interlace = function(x,y) {
	var l = Math.max(x.length,y.length);
	var a = new Array(l*2);
	for (var i=0; i<l; i++) {
		a[i*2] = x[i];
		a[i*2+1] = y[i];
	}
	return a;
};
// Returns correct angle in radians between 2 points
PathAnimation.getNormalizedAngle = function(x1,y1,x2,y2) {
	var distx = Math.abs(x1-x2);
	var disty = Math.abs(y1-y2);
	if (distx==0 && disty==0) angle = 0;
	else if (distx==0) angle = Math.PI/2;
	else angle = Math.atan(disty/distx);
	if (x1<x2) {
		if (y1<y2) angle = Math.PI*2-angle;
	}
	else {
		if (y1<y2) angle = Math.PI+angle;
		else angle = Math.PI-angle;
	}
	return angle;
};
// Radian conversion
PathAnimation.radianToDegree = function(radian) {
	return radian*180/Math.PI
};
PathAnimation.degreeToRadian = function(degree) {
	return degree*Math.PI/180
};