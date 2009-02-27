/*
   DynAPI Distribution
   ImageAnimation Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
	dynapi.util [thread, pathanim]
	dynapi.gui [dynimage]
*/

function ImageAnimation(dynimage) {
	this.Thread = Thread;
	this.Thread(dynimage);

	this.imgAnim = new Array();
	this.imgAnim.playing = null;
}
ImageAnimation.prototype = new Thread;
ImageAnimation.prototype.addAnimation = function (imgArray) {
	var animNum = this.imgAnim.length;
	this.imgAnim[animNum] = imgArray;
	this.imgAnim[animNum].loops = false;
	this.imgAnim[animNum].resets = false;
	this.imgAnim[animNum].frame = 0;
	this.imgAnim[animNum].playing = true;
	this.imgAnim[animNum].direction = 0;
	this.imgAnim[animNum].alternates = false;
	return animNum;
};
ImageAnimation.prototype.getFrame = function (animNum,frameNum) {
	return this.imgAnim[animNum][frameNum];
};

ImageAnimation.prototype.setLoops = function (animNum,loop) {
	this.imgAnim[animNum].loops = loop;
};
ImageAnimation.prototype.setResets = function (animNum) {
	this.imgAnim[animNum].resets = true;
};
ImageAnimation.prototype.setAlternates = function (animNum,alt) {
	this.imgAnim[animNum].loops = true;
	this.imgAnim[animNum].alternates = alt;
};

ImageAnimation.prototype.playAnimation = function (animNum) {
	if (animNum!=null && this.imgAnim.playing!=animNum) {
		this.playing = true;
		this.imgAnim.playing = animNum;
		if (this.dlyr!=null) this.dlyr.invokeEvent("imgstart");
		this.start();
	}
};
ImageAnimation.prototype.stopAnimation = function () {
	this.imgAnim.playing = null;
	this.playing = false;
	this.stop();
	if (this.dlyr!=null) this.dlyr.invokeEvent("imgrun");
};
ImageAnimation.prototype.run = function () {
	if (!this.playing || this.imgAnim.playing==null || this.dlyr==null) return;
	
	var anim = this.imgAnim[this.imgAnim.playing];
	
	if (anim.frame==0 && this.img==anim[anim.frame]) {
		anim.frame++;	// skip 1st frame if same
	}
	if (this.dlyr!=null) this.dlyr.invokeEvent("imgrun");
	this.dlyr.setImage(anim[anim.frame]);
	
	if (anim.frame>=anim.length-1) {
		if (anim.loops) {
			if (anim.alternates && anim.direction==0 && anim.frame==anim.length-1) {
				anim.direction = 1; 
				anim.frame = anim.length-2;
			}
			else anim.frame = 0;
		}
		else if (anim.resets) {
			anim.frame = 0;
			this.stop()
		}
		else {
			this.stop()
		}
	}
	else {
		if (anim.alternates) {
			if (anim.frame==0 && anim.direction==1) {
				anim.direction = 0;
				anim.frame = 1;
			}
			else if (anim.direction==0) {
				anim.frame++;
			}
			else if (anim.direction==1) {
				anim.frame--;
			}
		}
		else {
			anim.frame++;
		}
	}
};
