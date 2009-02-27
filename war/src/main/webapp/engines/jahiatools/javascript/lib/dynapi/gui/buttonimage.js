/*
   DynAPI Distribution
   ButtonImage Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
	dynapi.gui [dynimage]
*/

function ButtonImage() {
	this.DynImage = DynImage;
	this.DynImage();
	if (arguments.length>0) this.setImages(arguments);
	this.addEventListener(ButtonImage.events);
};

ButtonImage.events = new EventListener();
ButtonImage.events.onmousedown = function (e) {
	var o = e.getSource();
	if (o.checkbox) o.setSelected(!o.selected);
	else o.setImage(o.selectedImage);
	e.setBubble(false);
};
ButtonImage.events.onmouseup = function (e) {
	var o = e.getSource();
	if (!o.checkbox) o.setImage(o.defaultImage);
	e.setBubble(false);
};
ButtonImage.events.onmouseover = function (e) {
	var o = e.getSource();
	if (o.selected) o.setImage(o.selectedRoll);
	else o.setImage(o.defaultRoll);
	e.setBubble(false);
};
ButtonImage.events.onmouseout = function (e) {
	var o = e.getSource();
	if (o.selected) o.setImage(o.selectedImage);
	else o.setImage(o.defaultImage);
	e.setBubble(false);
}

ButtonImage.prototype = new DynImage;
ButtonImage.prototype.checkbox = false;
ButtonImage.prototype.setImages = function(defaultImage,defaultRoll,selectedImage,selectedRoll) {
	if (arguments.length==4) this.checkbox = true;
	this.defaultImage = defaultImage;
	this.defaultRoll = defaultRoll;
	this.selectedImage = selectedImage;
	this.selectedRoll = selectedRoll;
	this.setImage(this.defaultImage);
	this.setSize(this.defaultImage.width,this.defaultImage.height);
};
ButtonImage.prototype.setSelected = function(b) {
  this.selected=b
	if (b) {
		this.setImage(this.selectedImage);
		this.invokeEvent("select");
	}
	else {
		this.setImage(this.defaultImage);
		this.invokeEvent("deselect");
	}
};