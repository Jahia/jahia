/*
   DynAPI Distribution
   Debug Extensions

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser]
*/ 
DynLayer.prototype.debug = function() {
	var str ="";
	str += "DynLayer: "+this.id+"\n";
	str += "-----------------------\n";
	str += "     Size: "+this.getWidth()+"x"+this.getHeight()+"\n";
	str += " Position: "+this.getX()+","+this.getY()+"\n";
	str += "  BgImage: "+this.getBgImage()+"\n";
	str += "  BgColor: "+this.getBgColor()+"\n";
	str += "  Content: "+this.getHTML()+"\n";
	str += "   zIndex: "+this.getZIndex()+"\n";
	str += "  Visible: "+this.getVisible()+"\n";
	str += "----- Number of Children: "+this.children.length+"\n";
	return str;
}
DynObject.prototype.tree = function(space) {
	var ret ='';
	space = space||".";
	ret += space +" "+this.getDef()+"\n"
	for(i in this.children)
		ret += this.children[i].tree(space+" . ."); // JM: With simple spaces NS6 does not indent (pfff !!!)
	return ret;
}
DynAPIObject.prototype.getDef=function(){return "DynAPI"}
DynDocument.prototype.getDef=function(){return "DynDocument ( "+this.id+" )"}
DynLayer.prototype.getDef=function(){return this.id}
