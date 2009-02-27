/*
   DynAPI Distribution
   Sprite Class
   Modified: 2000.11.28

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
*/

// this is currently not really needed
// what was initally going to be Sprite has become Thread, PathAnim, and DynImage

function Sprite(img) {
	this.DynImage = DynImage;
	this.DynImage(img);
	this.thread = new Thread(this);
	this.thread._key = Thread.setUniqueKey(this);
	this.thread.run = this.run;
};
Sprite.prototype = new DynImage;

// Thread methods

Sprite.prototype.sleep = function (ms) {
	this.thread.sleep(ms);
};
Sprite.prototype.setFPS = function (fps) {
	this.thread.setFPS(fps);
};
Sprite.prototype.cancel = function () {
	this.thread.cancelThread();
};
Sprite.prototype.start = function () {
	this.thread.start();
};
Sprite.prototype.stop = function () {
	this.thread.stop();
};
Sprite.prototype.run = function () {};	// overwrite run
