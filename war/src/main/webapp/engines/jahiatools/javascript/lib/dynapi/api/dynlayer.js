/*
   DynAPI Distribution
   DynLayer Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
*/
function DynLayer() {
	this.DynObject = DynObject;
	this.DynObject();

	this.elm=null;
	this.doc=null;
	this.css=null;

	var a=arguments
	if(a[0]) this.setID(a[0])
	this.x=a[1]||0;
	this.y=a[2]||0;
	this.w=a[3]||null;
	this.h=a[4]||null;
	this.bgColor=a[5]||null;
	this.visible=(a[6]!=false && a[6]!='hidden');
	this.z=a[7]||null;
	this.bgImage=a[8]||null;
	this.html=null;
};
DynLayer.prototype = new DynObject();
DynLayer.prototype.isDynLayer = true;
DynLayer.prototype.specificCreate=function() {
	if (this.created||!this.parent||this.elm!=null) return;

	if (is.ns6) {
		var parentElement=(this.parent.isDynLayer)?this.parent.elm:this.parent.doc.body;
		this.doc=this.parent.doc;
		var r = parentElement.ownerDocument.createRange()
		r.setStartBefore(parentElement);
		ptxt = r.createContextualFragment(this.getOuterHTML());
		parentElement.appendChild(ptxt);
		this.elm=parentElement.lastChild;
		this.css=this.elm.style;
	} else if (is.ie) {
		var parentElement=(this.parent.isDynLayer)?this.parent.elm:this.parent.doc.body;
		var code=this.getOuterHTML();
		parentElement.insertAdjacentHTML("beforeEnd", code);
		this.elm=parentElement.children[parentElement.children.length-1];
		this.css=this.elm.style;
		this.doc=this.parent.doc;
	} else if (is.ns4) {
		var recycled=this.parent.doc.recycled;
		if (recycled && recycled.length>0) {
	        this.elm=recycled[0];
			Methods.removeFromArray(recycled,recycled[0]);
		} else {
			this.elm=new Layer(this.w,this.parent.elm);
			this.elm.captureEvents(Event.LOAD);
			this.elm.onload=function() {};
		}
		this.css=this.elm;
		this.doc=this.elm.document;
		this.doc.lyrobj=this;
		if (this.w) this.css.clip.width=this.w;
		if (this.h) this.css.clip.height=this.h;
		this.elm.moveTo(this.x,this.y);
		this.doc.write(this.getInnerHTML());
		this.doc.close();
		if (this.bgColor!=null) this.setBgColor(this.bgColor);
		if (this.bgImage!=null) this.setBgImage(this.bgImage);
		if (this.clip) this.setClip(this.clip)
		if (this.z) this.css.zIndex=this.z;
		this.css.visibility=this.visible? "inherit" : (is.ns4?"hide":"hidden");
		for (var i=0;i<this.doc.images.length;i++) this.doc.images[i].lyrobj=this;
		for (i=0;i<this.doc.links.length;i++) this.doc.links[i].lyrobj=this;
	}
	this.frame=this.parent.frame
	this.elm.lyrobj=this;
	this.assignChildren();
	if (this.html!=null) {
		if (this.w==null && this.getContentWidth()>0) this.setWidth(this.getContentWidth(), false);
		if (this.h==null && this.getContentHeight()>0) this.setHeight(this.getContentHeight(), false);
	}
};
DynLayer.prototype.assignChildren=function() {
	var l=this.children.length;
	if(is.def) {
		for (var i=0; i<l; i++) {
			var child=this.children[i];
			if (is.ie) var elm=this.elm.all[child.id]; 
			else var elm=this.doc.getElementById(child.id);
			child.elm=elm;
			child.css=child.elm.style;
			child.doc=this.doc;
			child.elm.lyrobj=child;
			child.frame=this.frame;
			child.assignChildren();
			if (child.z) child.css.zIndex=child.z;
		}
	} else if(is.ns4) {
		for (var i=0; i<l; i++) {
			var child=this.children[i];
			var elm=this.doc.layers[child.id];
			child.elm=elm;
			child.css=elm;
			child.doc=elm.document;
			child.doc.lyrobj=child;
			child.elm.lyrobj=child;
			for (var j=0;j<child.doc.images.length;j++) child.doc.images[j].lyrobj=child;
			for (j=0;j<child.doc.links.length;j++) child.doc.links[j].lyrobj=child;
			child.assignChildren();
			if (child.z) child.css.zIndex=child.z;
		}
	}
};
DynLayer.prototype.specificRemove=function() {
	if (is.def && this.elm) {
		this.elm.style.visibility = "hidden";
		this.elm.innerHTML = "";
		this.elm.outerHTML = "";
		this.frame = null;
	}
	else if (is.ns4 && this.elm) {
		if (!this.parent.doc.recycled) this.parent.doc.recycled=[];
		this.parent.doc.recycled[this.parent.doc.recycled.length]=this.elm;
	}
	this.elm = null;
	this.doc = null;
	this.css = null;
};

DynLayer.prototype.getInnerHTML=function() {
	var s="";
	if (this.html!=null) s+=this.html;
	var l=this.children.length;
	for (var i=0;i<l;i++) s+=this.children[i].getOuterHTML();
	return s;
};
if (is.def) {
	DynLayer.prototype.getOuterHTML=function() {
		var s='<div id="'+this.id+'" style="';
		if (this.visible==false) s+=' visibility:hidden;';
		if (this.x!=null) s+=' left:'+this.x+'px;';
		if (this.y!=null) s+=' top:'+this.y+'px;';
		if (this.w!=null) s+=' width:'+this.w+'px;';
		if (this.h!=null) s+=' height:'+this.h+'px;';
		if (this.clip) s+=' clip:rect('+this.clip[0]+'px '+this.clip[1]+'px '+this.clip[2]+'px '+this.clip[3]+'px);';
		else if (this.w!=null && this.h!=null) s+=' clip:rect(0px '+this.w+'px '+this.h+'px 0px);';
		if (this.z) s+=' z-index='+this.z+';';
		if (this.bgImage!=null)	s+=' background-image:url('+this.bgImage+');'
		if (this.bgColor!=null)	s+=' background-color:'+this.bgColor+';'
		if (is.ie55 && this.bgImage==null && this.html==null) s+=' background-image:url(javascript:null);'
		s+=' position:absolute;">';
		if (this.html!=null) s+=this.html;
		for (var i=0; i<this.children.length; i++) s+=this.children[i].getOuterHTML();
		s+='</div>';
		return s;

	};
} else if (is.ns4) {
	DynLayer.prototype.getOuterHTML=function() {
		var s='\n<layer id="'+this.id+'"';
		if (this.visible==false) s+=' visibility="hide"';
		if (this.x!=null) s+=' left='+this.x;
		if (this.y!=null) s+=' top='+this.y;
		if (this.w!=null) s+=' width='+this.w;
		if (this.h!=null) s+=' height='+this.h;
		if (this.clip) s+=' clip="'+this.clip[3]+','+this.clip[0]+','+this.clip[1]+','+this.clip[2]+'"';
		else if (this.w!=null && this.h!=null) s+=' clip="0,0,'+this.w+','+this.h+'"';
		if (this.z) s+=' zIndex='+this.z;
		if (this.bgImage!=null)	s+=' background="'+this.bgImage+'"';
		if (this.bgColor!=null)	s+=' bgcolor="'+this.bgColor+'"';
		s+='>';
		if (this.html!=null) s+=this.html;
		for (var i=0; i<this.children.length; i++) s+=this.children[i].getOuterHTML();
		s+='</layer>';
		return s;
	};
};
DynLayer.prototype._oldDL_create = DynLayer.prototype.create
DynLayer.prototype.create = function() {
	this._oldDL_create()
	this.invokeEvent("resize")
}

if (is.ns) {
	DynLayer.prototype._setX=function(){ this.css.left=this.x; }
	DynLayer.prototype._setY=function(){ this.css.top=this.y; }
} else {
	DynLayer.prototype._setX=function(){ this.css.pixelLeft=this.x; }
	DynLayer.prototype._setY=function(){ this.css.pixelTop=this.y; }
};

DynLayer.prototype.moveTo=function(x,y) {
	if (x!=null) this.x=x;
	if (y!=null) this.y=y;
	if (this.css==null) return;
	this._setX();
	this._setY();
	this.invokeEvent('move');
};
DynLayer.prototype.moveBy=function(x,y) {
	this.moveTo(this.x+x,this.y+y);
};
DynLayer.prototype.setX=function(x) { this.moveTo(x,null); };
DynLayer.prototype.setY=function(y) { this.moveTo(null,y); };
DynLayer.prototype.getX=function() { return this.x; };
DynLayer.prototype.getY=function() { return this.y; };
DynLayer.prototype.getPageX=function() {
	if (this.css==null) return null;
	if (is.ns4) return this.css.pageX;
	else return (this.isChild)? this.parent.getPageX()+this.x : this.x;
};
DynLayer.prototype.getPageY=function() {
	if (this.css==null) return null;
	if (is.ns4) return this.css.pageY;
	else return (this.isChild)? this.parent.getPageY()+this.y : this.y;
};
DynLayer.prototype.setPageX=function(x) {
	if (this.css==null) return;
	if (is.ns4) this.css.pageX=x;
	if (is.ie) {
		if (this.isChild) this.setX(this.parent.getPageX()-x);
		else this.setX(x);
	}
	this.getX();
	this.invokeEvent('move');
};
DynLayer.prototype.setPageY=function(y) {
	if (this.css==null) return;
	if (is.ns4) this.css.pageY=y;
	if (is.ie) {
		if (this.isChild) this.setY(this.parent.getPageY()-y);
		else this.setY(y);
	}
	this.getY();
	this.invokeEvent('move');
};
DynLayer.prototype.setVisible=function(b) {
	this.visible=b;
	if (this.css==null) return;
	this.css.visibility = b? "inherit" : (is.ns4?"hide":"hidden");
};
DynLayer.prototype.getVisible=function() {
	return this.visible;
};
DynLayer.prototype.setZIndex=function(z) {
	this.z=z;
	if (this.css==null) return;
	this.css.zIndex=z;
};
DynLayer.prototype.getZIndex=function() {
	return this.z;
};
DynLayer.prototype.setBgImage=function(path) {
	this.bgImage=path;
	if (this.css==null) return;
	if (is.ns4) {
		this.elm.background.src=path;
		if (!path) this.setBgColor(this.getBgColor());
	}
	else this.css.backgroundImage='url('+path+')';
};
DynLayer.prototype.getBgImage=function() {
	return this.bgImage;
};
DynLayer.prototype.setBgColor=function(color) {
	if (color==null) {
		if (is.ns4) color=null
		else color='transparent'
	}
	this.bgColor=color;
	if (this.css==null) return;
	if (is.ns4) this.doc.bgColor=color;
	else this.css.backgroundColor=color;
};
DynLayer.prototype.getBgColor=function() {
	return this.bgColor;
};
if (is.ns4) {
	DynLayer.prototype._setHTML=function(html) {
		var sTmp=(this.w==null)?'<NOBR>'+this.html+'</NOBR>':this.html
		this.doc.open()
		this.doc.write(sTmp)
		this.doc.close()
        	for (var i=0;i<this.doc.images.length;i++) this.doc.images[i].lyrobj=this;
        	for (i=0;i<this.doc.links.length;i++) this.doc.links[i].lyrobj=this;
	}
} else if (is.ie) {
	DynLayer.prototype._setHTML=function(html) { 
		var images = this.elm.all.tags("img")
		for (var i=0;i<images.length;i++) images[i].lyrobj=this
		}
} else {
	DynLayer.prototype._setHTML=function(html) {;
		sTmp=(this.w==null)?'<NOBR>'+this.html+'</NOBR>':this.html;
		while (this.elm.hasChildNodes()) this.elm.removeChild(this.elm.firstChild);
		var r=this.elm.ownerDocument.createRange();
		r.selectNodeContents(this.elm);
		r.collapse(true);
		var df=r.createContextualFragment(sTmp);
		this.elm.appendChild(df);
	}
};
DynLayer.prototype.setHTML=function(html,noevt) {
	this.html=html?html:'';
	if (is.platform=="mac") this.html+='\n';
	if (this.css==null) return;
	if (noevt!=false) this.invokeEvent("beforeload");
	this.elm.innerHTML=html;
	this._setHTML(html);
	if (noevt!=false) this.invokeEvent("load");
}
DynLayer.prototype.getHTML=function() {
	return this.html;
};
DynLayer.prototype.setSize = function(w,h,noevt) {
	this.w=(w==null)?this.w:w<0?0:w;
	if (this.w==null) return;
	this.h=(h==null)?this.h:h<0?0:h;
	if (this.h==null) return;
	if (this.css!=null) {
		if (is.ns4) { 
			this.css.clip.width = this.w;
			this.css.clip.height = this.h;
			}
		else {
			this.css.width = this.w;
			this.css.height = this.h;
			this.css.clip = 'rect(0px '+(this.w||0)+'px '+(this.h||0)+'px 0px)';
		}
	}
    if (noevt!=false) this.invokeEvent('resize');
};
DynLayer.prototype.setWidth=function(w,noevt) {
	this.w=(w==null)?this.w:w<0?0:w;
	if (this.w==null) return;
	if (this.css!=null) {
		if (is.ns4) this.css.clip.width = this.w;
		else {
			this.css.width = this.w;
			this.css.clip = 'rect(0px '+(this.w||0)+'px '+(this.h||0)+'px 0px)';
		}
	}
    if (noevt!=false) this.invokeEvent('resize');
};
DynLayer.prototype.setHeight=function(h,noevt) {
	this.h=(h==null)?this.h:h<0?0:h;
	if (this.h==null) return;
	if (this.css!=null) {
		if (is.ns4) this.css.clip.height = this.h;
		else {
			this.css.height = this.h;
			this.css.clip = 'rect(0px '+(this.w||0)+'px '+(this.h||0)+'px 0px)';
		}
	}
	if (noevt!=false) this.invokeEvent('resize');
};
DynLayer.prototype.getWidth=function() {
	return this.w;
};
DynLayer.prototype.getHeight=function() {
	return this.h;
};
DynLayer.prototype.getContentWidth=function() {
	if (this.elm==null) return 0;
	else {
		if (is.ns4) return this.doc.width;
		else if (is.ie) {
       			if (is.platform=="mac") this.elm.offsetWidth //Mac hack, forces scrollWidth to fill due to IE 5 mac bug
        		return parseInt(this.elm.scrollWidth);
			}
		else {
			var tw = this.elm.style.width
		        this.elm.style.width = "auto";
        		var w = this.elm.offsetWidth;
        		this.elm.style.width = tw;
        		return w;
		}
	};
};
DynLayer.prototype.getContentHeight=function() {
	if (this.elm==null) return 0;
	else {
		if (is.ns4) return this.doc.height;
		else if (is.ie) {
       			if (is.platform=="mac") this.elm.offsetHeight;
        		return parseInt(this.elm.scrollHeight);
			}
		else {
			var th = this.elm.style.height
			this.elm.style.height = "auto";
        		var h = this.elm.offsetHeight;
        		this.elm.style.height = th;
        		return h;
		}
	}
};
DynLayer.prototype.setClip=function(clip) {
	var cc=this.getClip();
	for (var i=0;i<clip.length;i++) if (clip[i]==null) clip[i]=cc[i];
	this.clip=clip;
	if (this.css==null) return;
	var c=this.css.clip;
	if (is.ns4) c.top=clip[0], c.right=clip[1], c.bottom=clip[2], c.left=clip[3];
	else this.css.clip="rect("+clip[0]+"px "+clip[1]+"px "+clip[2]+"px "+clip[3]+"px)";
};
DynLayer.prototype.getClip=function() {
	if (this.css==null || !this.css.clip) return [0,0,0,0];
	var c = this.css.clip;
	if (c) {
		if (is.ns4) return [c.top,c.right,c.bottom,c.left];
		if (c.indexOf("rect(")>-1) {
			c=c.split("rect(")[1].split(")")[0].split("px");
			for (var i=0;i<c.length;i++) c[i]=parseInt(c[i]);
			return [c[0],c[1],c[2],c[3]];
		}
		else return [0,this.w,this.h,0];
	}
};