/*
   DynAPI Distribution
   Thread Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
*/
function Thread(dlyr) {
	// Inherit from object. Provides unique ID
	this.DynObject = DynObject
	this.DynObject()
	
	this.setDynLayer(dlyr);
}
Thread.prototype = new DynObject
Thread.prototype.active = false;
Thread.prototype.interval = 50;
Thread.prototype.cancelThread = false;
Thread.prototype.sleep = function (ms) {
	this.interval = Math.abs(parseInt(ms));
	if (this.active) {
		this.stop();
		setTimeout(this+'.start()',this.interval+1);
	}
};
Thread.prototype.setFPS = function (fps) {
	this.sleep(Math.floor(1000/fps));
};
Thread.prototype.cancel = function () {
	this.cancelThread = true;
	this.stop();
};
Thread.prototype.start = function () {
	if (!this.active) {
		this.active = true;
		if (!this.cancelThread) this.timer = setInterval(this+'.run()',this.interval);
	}
};
Thread.prototype.run = function () {}; // overwrite run
Thread.prototype.stop = function () {
	this.active = false;
	if (!this.cancelThread && this.timer) {
		clearInterval(this.timer);
		delete this.timer;
	}
};
Thread.prototype.setDynLayer = function (dlyr) {
	this.dlyr = dlyr;
};
Thread.prototype.getDynLayer = function () {
	return this.dlyr;
};
