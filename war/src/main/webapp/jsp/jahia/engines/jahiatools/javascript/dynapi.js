/*
   DynAPI Distribution
   DynAPI base Object. Empty shell defining common properties and methods

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
*/
DynObject = function() {
	this.setID("DynObject"+(DynObject.Count++));
	this.isChild = false;
	this.created = false;
	this.parent = null;
	this.children = [];
};
DynObject.prototype.getClass = function() { return this.constructor };
DynObject.prototype.setID = function(id) {
	this.id = id;
	DynObject.all[this.id] = this;
};
DynObject.prototype.addChild = function(c) {
	if(c.isChild) c.parent.removeChild(c);
	c.isChild = true;
	c.parent = this;
	if(this.created) c.create()
	this.children[this.children.length] = c;
	return c;
};
DynObject.prototype.removeChild = function(c) {
	var l = this.children.length;
	for(var i=0;i<l && this.children[i]!=c;i++);
	if(i!=l) {
		c.invokeEvent("beforeremove");
		c.specificRemove();
		c.created=false;
		c.invokeEvent("remove");
		c.isChild = false;
		c.parent = null;
		this.children[i] = this.children[l-1];
		this.children[l-1] = null;
		this.children.length--;
	}
};
DynObject.prototype.deleteFromParent = function () {
	if(this.parent) this.parent.deleteChild(this);
};
DynObject.prototype.removeFromParent = function () {
	if(this.parent) this.parent.removeChild(this);
};
DynObject.prototype.create = function() {
	this.flagPrecreate();
	this.specificCreate();
	this.created = true;
	var l = this.children.length;
	for(var i=0;i<l;i++) this.children[i].create()
	this.invokeEvent("create");
};
DynObject.prototype.flagPrecreate = function() {
	var l=this.children.length;
	for (var i=0; i<l;  i++) this.children[i].flagPrecreate();
	this.invokeEvent('precreate');
};
DynObject.prototype.del = function() {
	this.deleteAllChildren();
	this.invokeEvent("beforeremove");
	this.specificRemove();
	this.created = false;
	this.invokeEvent("remove");
	this.invokeEvent("delete");
};
DynObject.prototype.deleteChild = function(c) {
	var l = this.children.length;
	for(var i=0;i<l && this.children[i]!=c;i++);
	if(i!=l) {
		this.children[i] = this.children[l-1];
		this.children[l-1] = null;
		this.children.length--;
		c.del()
		delete c;
	}
};
DynObject.prototype.deleteAllChildren = function() {
	var l = this.children.length;
	for(var i=0;i<l;i++) {
		this.children[i].del();
		delete this.children[i];
	}
	this.children = [];
};
DynObject.prototype.toString = function() {
	return "DynObject.all."+this.id
};
DynObject.prototype.getAll = function() {
	var ret = [];
	var temp;
	var l = this.children.length;
	for(var i=0;i<l;i++) {
		ret[this.children[i].id] = this.children[i];
		temp = this.children[i].getAll();
		for(var j=0;j<temp.length;j++) ret[j] = temp[j];
	}
	return ret
};
DynObject.prototype.isParentOf = function(obj,equality) {
	if(!obj) return false
	return (equality && this==obj) || this.getAll()[obj.id]==obj
}
DynObject.prototype.isChildOf = function(obj,equality) {
	if(!obj) return false
	return (equality && this==obj) || obj.getAll()[this.id]==this
}
DynObject.prototype.specificCreate	= function() {};
DynObject.prototype.specificRemove	= function() {};
DynObject.prototype.invokeEvent		= function() {};
DynObject.Count = 0;
DynObject.all = [];


Methods = {
	removeFromArray : function(array, index, id) {
		var which=(typeof(index)=="object")?index:array[index];
		if (id) delete array[which.id];
        	else for (var i=0; i<array.length; i++)
			if (array[i] == which) {
				if(array.splice) array.splice(i,1);
				else {	for(var x=i; x<array.length-1; x++) array[x]=array[x+1];
         				array.length -= 1; }
			break;
			}
		return array;
	},
	getContainerLayerOf : function(element) {
		if(!element) return null
		if(is.def&&!is.ie) while (!element.lyrobj && element.parentNode && element.parentNode!=element) element=element.parentNode;
		else if(is.ie) while (!element.lyrobj && element.parentElement && element.parentElement!=element) element=element.parentElement;
		return element.lyrobj
	}
};


DynAPIObject = function() {
	this.DynObject = DynObject;
	this.DynObject();

	this.loaded = false;
	this.hookLoad = null;
	this.hookUnload = null;
	this.librarypath = '';
	this.packages = [];
	this.errorHandling = true;
	this.returnErrors = true;
	this.onLoadCodes = [];
	this.onUnLoadCodes = [];
	this.onResizeCodes = [];
}
DynAPIObject.prototype = new DynObject();
DynAPIObject.prototype.setLibraryPath = function(path) {
	if (path.substring(path.length-1)!='/') path+='/';
	this.librarypath=path;
}
DynAPIObject.prototype.addPackage = function(pckg) {
	if (this.packages[pckg]) return;
	this.packages[pckg] = { libs: [] };
}
DynAPIObject.prototype.addLibrary = function(path,files) {
	var pckg = path.substring(0,path.indexOf('.'));
	if (!pckg) {
		alert("DynAPI Error: Incorrect DynAPI.addLibrary usage");
		return;
	}
	var name = path.substring(path.indexOf('.')+1);
	if (!this.packages[pckg]) this.addPackage(pckg);
	if (this.packages[pckg].libs[name]) {
		alert("DynAPI Error: Library "+name+" already exists");
		return;
	}
	this.packages[pckg].libs[name] = files;
}
DynAPIObject.prototype.include = function(src,pth) {
	src=src.split('.');
	if (src[src.length-1] == 'js') src.length -= 1;
	var path=pth||this.librarypath||'';
	if (path.substr(path.length-1) != "/") path += "/";
	var pckg=src[0];
	var grp=src[1];
	var file=src[2];
	if (file=='*') {
		if (this.packages[pckg]) group=this.packages[pckg].libs[grp];
		if (group) for (var i=0;i<group.length;i++) document.write('<script language="Javascript1.2" src="'+path+pckg+'/'+grp+'/'+group[i]+'.js"><\/script>');
		else alert('include()\n\nThe following package could not be loaded:\n'+src+'\n\nmake sure you specified the correct path.');
	} else document.write('<script language="Javascript1.2" src="'+path+src.join('/')+'.js"><\/script>');
}
DynAPIObject.prototype.errorHandler = function (msg, url, lno) {
	if (!this.loaded || !this.errorHandling) return false;
	if (is.ie) {
		lno-=1;
		alert("DynAPI reported an error\n\nError in project: '" + url + "'.\nLine number: " + lno + ".\n\nMessage: " + msg);
	} else if (is.ns4) {
		alert("DynAPI reported an error\n\nError in file: '" + url + "'.\nLine number: " + lno + ".\n\nMessage: " + msg);
	} else return false;
	return this.returnErrors;
}
DynAPIObject.prototype.addLoadFunction = function(f) {
	this.onLoadCodes[this.onLoadCodes.length] = f;
}
DynAPIObject.prototype.addUnLoadFunction = function(f) {
	this.onUnLoadCodes[this.onUnLoadCodes.length] = f;
}
DynAPIObject.prototype.addResizeFunction = function(f) {
	this.onResizeCodes[this.onResizeCodes.length] = f;
}
DynAPIObject.prototype.loadHandler = function() {
	this.created = true;
	eval(this.onLoadCodes.join(";"));
	if (this.onLoad) this.onLoad();
	this.loaded=true;
	eval(this.hookLoad);
}
DynAPIObject.prototype.unloadHandler = function() {
	if (!is.ns4) this.deleteAllChildren();
	eval(this.onUnLoadCodes.join(";"));
	if (this.onUnload) this.onUnload();
	eval(this.hookUnload);
}
DynAPIObject.prototype.resizeHandler = function() {
	eval(this.onResizeCodes.join(";"));
	if (this.onResize) this.onResize();
}
DynAPI = new DynAPIObject();
DynAPI.hookLoad=window.onload;
DynAPI.hookUnload=window.onunload;
onload = function() { DynAPI.loadHandler(); }
onunload = function() { DynAPI.unloadHandler(); }
//onresize = function() { DynAPI.resizeHandler(); }
onerror = function(msg, url, lno) { DynAPI.errorHandler(msg, url, lno); }

DynAPI.addPackage('dynapi');
DynAPI.addLibrary('dynapi.api'  ,["browser","dynlayer","dyndocument"]);
DynAPI.addLibrary('dynapi.event',["listeners","mouse","dragevent","keyboard"]);
DynAPI.addLibrary('dynapi.ext'  ,["inline","layer","dragdrop","functions"]);
DynAPI.addLibrary('dynapi.gui'  ,["viewport","dynimage","button","buttonimage","label","list","loadpanel","pushpanel","scrollbar","scrollpane","sprite"]);
DynAPI.addLibrary('dynapi.util' ,["circleanim","cookies","debug","thread","hoveranim","imganim","pathanim","console"]);
