/*
   DynAPI Distribution
   Simple event classes. 
   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.
*/ 
DynObject.prototype.invokeEvent = function(type,e,args) {
	var ret = true;
	if(this["on"+type]) ret = this["on"+type](e,args)
	if(ret && this.parent) this.parent.invokeEvent(type,e,args);
}
