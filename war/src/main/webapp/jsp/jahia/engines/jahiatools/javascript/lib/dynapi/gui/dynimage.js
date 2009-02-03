/*
   DynAPI Distribution
   DynImage Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
*/

function DynImage() {
	this.DynLayer = DynLayer;
	this.DynLayer();

	if (typeof(arguments[0])=="string") this.setImageSrc(arguments[0]);
	else if (typeof(arguments[0])=="object") this.setImage(arguments[0]);
	else this.img = null;
	if (this.img && this.img.dynimages) {
		this.img.dynimages[this.img.dynimages.length]=this;
  		this.imgresize=true;
	}
	this.addEventListener(DynImage.listener);
};
DynImage.prototype = new DynLayer;

DynImage.listener = new EventListener();

DynImage.listener.onprecreate = function(e) {
	var o = e.getSource();
	if (o.w!=null && o.h!=null) {
		o.setImage(o.img,true);
	}
};

DynImage.listener.onresize = function (e) {
	var o = e.getSource();
	if (o.created) {
		if (o.img) {
			o.setImage(o.img,true);
		}
	}
};

// this is an optional method that only effects the behaviour on subsequent image changes.
// During the first creation of DynImage if no w/h is set it will automatically resize
// during creation regardless of whether this value is set or not (that is a function of the DynLayer).
// If you setAutoResize(true) when you change images after creation it will resize itself again.
DynImage.prototype.setAutoResize = function (b) {
	this.autoResize = b;
	if (this.created) this.setImage(this.img);
};

DynImage.prototype.setImage = function (imgObject,bRedraw) {
	if (!imgObject) {
		return;
	}
	this.img = imgObject;

	if (this.created && this.autoResize && !bRedraw) {
		if (this.img.width!=this.w && this.img.height!=this.h) {
			this.setSize(this.img.width, this.img.height, false);
			bRedraw = true;
		}
	}

	if (!this.created || bRedraw) {
		var wh = ((this.w!=null && this.h!=null)) ? ' width='+this.w+' height='+this.h : '';
		this.setHTML('<img name="'+this.id+'Image" src="'+imgObject.src+'"'+wh+' border=0>');
	}
	else if (this.created) {
		this.doc.images[this.id+'Image'].src = this.img.src;
	}
};
DynImage.prototype.getImage = function (imgObject) {
	return this.img;
};
DynImage.prototype.setImageSrc = function (imgsrc) {
	if (imgsrc) {
		this.setImage(DynImage.getImage(imgsrc));
	}
};
DynImage.prototype.getImageSrc = function () {
	return this.img? this.img.src : null;
};

// Functions

DynImage.loadimages=[];
DynImage.getImage=function(src,w,h) {
	for (var i=0;i<DynImage.loadimages.length;i++) {
		if (DynImage.loadimages[i].img.origsrc==src || DynImage.loadimages[i].img.src==src)
			return DynImage.loadimages[i].img;
	}
	DynImage.loadimages[i] = {};
	if (w&&h) DynImage.loadimages[i].img = new Image(w,h);
	else DynImage.loadimages[i].img = new Image();
	DynImage.loadimages[i].img.src=DynImage.loadimages[i].img.origsrc=src;
	DynImage.loadimages[i].img.dynimages=[];
	if (DynAPI.loaded && !DynImage.timerId) DynImage.loaderStart();
	return DynImage.loadimages[i].img;
};
DynImage.loaderStart=function() {
	DynImage.timerId=setTimeout('DynImage.loadercheck()',50);
	if (DynImage.onLoaderStart) DynImage.onLoaderStart();
};
DynImage.loadercheck=function() {
	DynImage.ItemsDone=0;
	var max=DynImage.loadimages.length;
	for (var i=0; i<max; i++) if (DynImage.loadimages[i].img.complete) DynImage.ItemsDone+=1;
	if (DynImage.ItemsDone<max) {
		if (DynImage.onLoading) DynImage.onLoading();
		DynImage.timerId=setTimeout('DynImage.loadercheck()',25);
	}
	else {
	for (var i=0; i<DynImage.loadimages.length; i++) {
    		if (DynImage.loadimages[i].img.dynimages) {
				if (is.def&&(!is.ie||(is.ie&&is.platform=="mac"))) {
					DynImage.loadimages[i].img.width = DynImage.loadimages[i].img.w;
					DynImage.loadimages[i].img.height = DynImage.loadimages[i].img.h;
				}
				var dlen=DynImage.loadimages[i].img.dynimages.length;
    	  		for (var j=dlen-1;j>=0;j--){
    	    			if (DynImage.loadimages[i].img.dynimages[j].imgresize) {
    	    				DynImage.loadimages[i].img.dynimages[j].setSize(DynImage.loadimages[i].img.width,DynImage.loadimages[i].img.height,false);
							DynImage.loadimages[i].img.dynimages[j].imgresize=false;
							DynImage.loadimages[i].img.dynimages[j]=null;
						}
          			}
          		DynImage.loadimages[i].img.dynimages=null;
			}
		}
		if (DynImage.onLoaderDone) DynImage.onLoaderDone();
		DynImage.timerId=null;
	}
};
DynAPI.addLoadFunction("DynImage.loaderStart()")