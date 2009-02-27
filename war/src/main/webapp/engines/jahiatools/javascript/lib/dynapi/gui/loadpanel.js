/*
   DynAPI Distribution
   LoadPanel Widget

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
*/
function LoadPanel(url) {
	this.DynLayer = DynLayer;
	this.DynLayer();
	this.autoH=true;
	this.autoW=false;
	this.isILayer=false;
	this.isIFrame=!(is.ie5 && is.platform=='win32');
	var l=new EventListener(this);
	l.onresize=function(e) {
		var o=e.getTarget();
		if (!o.created || o.isReloading) return;
		if (o.autoH && o.url) o.reload();
	};
	l.oncreate=function(e) {
		var o=e.getTarget();
		o.setURL(o.url);
		if (!o.isReloading && o.tempURL) {
			o.setURL(o.tempURL);
			delete o.tempURL;
		}
	};
	this.addEventListener(l);
	this.tempURL=url;
	return this;
};
LoadPanel.prototype=new DynLayer();
LoadPanel.prototype.setAutoResizeWidth=function(b) {
	this.autoW=b;
};
LoadPanel.prototype.setAutoResizeHeight=function(b) {
	this.autoH=b;
};
LoadPanel.prototype.useILayer=function(b) {
	if (is.ns4) {
		this.isILayer=b;
		if (this.created) this.reload();
	}
};
LoadPanel.prototype.useIFrame=function(b) {
	if (is.def) {
		this.isIFrame=b;
		if (this.created) this.reload();
	}
};
LoadPanel.prototype.insertInlineElements=function() {
	if (is.ie) {
		if (this.isIFrame) this.setHTML('<IFRAME ID="'+this.id+'loadElement" STYLE="visibility: hidden; display: none;"></IFRAME>',false);
		else this.setHTML('<DIV ID="'+this.id+'loadElement" STYLE="behavior:url(#default#download)" style="display: none;"></DIV>',false);
	}
	else if (is.ns4 && this.isILayer) this.setHTML('<ilayer></ilayer>',false);
	else if (is.ns6) this.setHTML('<IFRAME ID="'+this.id+'loadElement" STYLE="visibility: hidden;"></IFRAME>',false);
};
LoadPanel.prototype.findInlineElements=function() {
	if (is.ie) {
		if (this.isIFrame) this.loadElement=document.frames(this.id+'loadElement');
		else this.loadElement=document.all(this.id+'loadElement');
	}
	else if (is.ns4) {
		if (this.isILayer) this.loadElement=this.doc.layers[0];
		else this.loadElement=this.elm;
	}
	else if (is.ns6) this.loadElement=document.getElementById(this.id+'loadElement');
};
LoadPanel.prototype.getFileScope=function() {
	if (!this.loadElement) return null;
	return this.loadElement;
};
LoadPanel.prototype.clearFile=function() {
	this.url=null;
	if (this.isILayer) {
		this.loadElement.document.write('');
		this.loadElement.document.close();
	}
	else this.reload();
};
LoadPanel.prototype.getURL=function() {
	return this.url;
};
LoadPanel.prototype.setURL=function(url) {
	if (!url) return;
	if (!this.created) this.url=url;
	else LoadPanel.queue.add(url,this);
};
LoadPanel.prototype.reload=function() {
	this.isReloading=true;
	var url=this.url;
	var p=this.parent;
	this.removeFromParent();
	this.html = '';
	p.addChild(this);
	this.isReloading=false;
};
LoadPanel.prototype.loadHandler=function(url) {
	this.url=url;
	if (is.ie5 && !this.isIFrame && this.elm && this.elm.all) {
		var imgs = this.elm.all.tags("img");
		for (var i=0;i<imgs.length;i++) {
			if (imgs[i].readyState == 'uninitialized') imgs[i].src = imgs[i].src;
		}
	}
	if (is.ns4 && this.isILayer) {
		var w=this.loadElement.document.width;
		var h=this.loadElement.document.height;
	}
	else {
		var w=this.getContentWidth();
		var h=this.getContentHeight();
	}
	if (this.autoW) this.setWidth(w,false);
	if (this.autoH) this.setHeight(h,false);
	this.isReloading=false;
	this.invokeEvent('load');
};
function LoadQueue() {
	this.queue=new Array();
	this.index=0;
};
LoadQueue.prototype.toString=function() {
	return "LoadPanel.queue";
};
LoadQueue.prototype.add=function(url,loadpanel) {
	var q=this.queue.length;
	this.queue[q]=[url,loadpanel];
	this.loadNext();
};
LoadQueue.prototype.loadNext=function() {
	if (!this.busy && this.queue[this.index]) {
		this.busy=true;
		var lpanel=this.currentLoadPanel=this.queue[this.index][1];
		var url=this.currentURL=this.queue[this.index][0];
		if (is.ns4) {
			if (is.ILayer) lpanel.insertInlineElements();
			lpanel.findInlineElements();
			DynAPI.document.releaseMouseEvents();
			var lyr=lpanel.elm;
			while(lyr.parentLayer) lyr=lyr.parentLayer;
			lyr.onload=LoadQueue.loadHandler;
			lpanel.loadElement.onload=LoadQueue.loadHandler;
			lpanel.loadElement.src=url;
		} else {
			if (!lpanel.loadElement) {
				lpanel.insertInlineElements();
				lpanel.findInlineElements();
			}
			if (is.ie) {
				if (lpanel.isIFrame) {
					lpanel.loadElement.document.isLoading=true;
					lpanel.loadElement.location=url;
					lpanel.timerID=setInterval(this.toString() + '.loadTimer()',250);
				}
				else lpanel.loadElement.startDownload(url,LoadQueue.loadHandler);
			}
			else if (is.ns6) {
				lpanel.timerID=setInterval(this.toString() + '.loadTimer()',250);
				lpanel.loadElement.src=url;
			}
		}
		Methods.removeFromArray(this.queue,this.index);
	}
};
if (is.ns6) {
	LoadQueue.prototype.loadTimer=function() {
		var lpanel=this.currentLoadPanel;
		if (!document.getElementById(lpanel.id+'loadElement')) {
			clearInterval(lpanel.timerID);
			LoadQueue.continueLoad();
		}
		else if (lpanel.loadElement.contentDocument && lpanel.loadElement.contentDocument.body.innerHTML != document.body.innerHTML) {
			clearInterval(lpanel.timerID);
			LoadQueue.loadHandler(lpanel.loadElement.contentDocument);
		}
	}
} else if (is.ie) {
	LoadQueue.prototype.loadTimer=function() {
		var lpanel=this.currentLoadPanel;
		if (!document.frames(lpanel.id+'loadElement')) {
			clearInterval(lpanel.timerID);
			LoadQueue.continueLoad();
		}
		else if (!lpanel.loadElement.document.isLoading && (lpanel.loadElement.document.readyState=='interactive' || lpanel.loadElement.document.readyState=='complete')) {
			clearInterval(lpanel.timerID);
			LoadQueue.loadHandler(lpanel.loadElement.document.body.innerHTML);
		}
	}
}
LoadQueue.loadHandler=function(e) {
	var q=LoadPanel.queue;
	var lp=q.currentLoadPanel;
	if (q.currentLoadPanel) {
		if (is.ie) {
			lp.elm.innerHTML=e;
			if (lp.isIFrame) lp.loadElement=null;
		}
		else if (is.ns4) {
			var lyr = lp.elm;
			while(lyr.parentLayer != window) lyr = lyr.parentLayer;
			lyr.onload = function(){};
			lp.loadElement.onload = function(){};
		}
		else if (is.ns6) {
			var html=e.body.innerHTML;
			lp.elm.innerHTML=html;
			lp.loadElement=null;
		}
		setTimeout('LoadQueue.continueLoad()',200);
	}
};
LoadQueue.continueLoad=function() {
	var q=LoadPanel.queue;
	q.currentLoadPanel.loadHandler(q.currentURL);
	q.busy=false;
	if (q.queue[q.index]) q.loadNext();
	else DynAPI.document.captureMouseEvents();
};
LoadPanel.queue=new LoadQueue();