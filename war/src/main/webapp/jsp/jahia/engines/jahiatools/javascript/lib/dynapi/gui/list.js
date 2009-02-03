/*
   DynAPI Distribution
   List Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser, events]
	dynapi.gui [label]
*/

function ListItem(text,value){
	this.Label = Label;
	this.Label(text);
	this.value = value;
	this.setSelectable(false);
	this.deselectable = true;
	this.isMouseOver = false;
	this.selected = false;
	this.itemStyle = {};
	this.itemStyle.bos=false;
	this.itemStyle.ios=false;
	this.addEventListener(ListItem.listevents);
}
ListItem.prototype = new Label();

ListItem.listevents = new EventListener();
ListItem.listevents.oncreate = function(e) {
	var o = e.getSource();
	if (o.list.created) o.list.arrangeItems();
};
ListItem.listevents.onmousedown = function (e) {
	var o = e.getSource();
	o.setSelected(!o.selected);
};
ListItem.listevents.onmouseover = function (e) {
	var o = e.getSource();
	if (!o.selected && !o.isMouseOver) {
		o.setBgColor(o.itemStyle.bgColorRoll);
		o.setFontColor(o.itemStyle.textRoll);
		for (var i=0;i<o.list.items.length;i++) {
			if (o.list.items[i]!=o && !o.list.items[i].selected && o.list.items[i].isMouseOver) {
				o.list.items[i].setBgColor(o.list.items[i].itemStyle.bgcolor);
				o.list.items[i].setFontColor(o.list.items[i].itemStyle.textNormal);
				o.list.items[i].isMouseOver = false;
			}
		}
	}
	o.isMouseOver = true;
};
ListItem.listevents.onmouseout = function (e) {
	var o = e.getSource();
	if (!o.selected && o.isMouseOver) {
		o.setBgColor(o.itemStyle.bgcolor);
		o.setFontColor(o.itemStyle.textNormal);
	}
	o.isMouseOver = false;
};
ListItem.prototype.setColors = function(bg,bgr,bgs,tn,tr,ts) {
    var s = this.itemStyle;
	s.bgcolor = bg||'#eeeeee';
	s.bgColorRoll = bgr||'#cccccc';
	s.bgColorSelect = bgs||'lightblue';
	s.textNormal = tn||'#000000';
	s.textRoll = tr||'#000000';
	s.textSelect = ts||'#000000';
	this.setBgColor(s.bgcolor);
};
ListItem.prototype.boldOnSelect = function(b) {
	this.itemStyle.bos=b;
};
ListItem.prototype.italicOnSelect = function(b) {
	this.itemStyle.ios=b;
};
ListItem.prototype.setSelected = function(b) {
	if (this.selected==b || !this.deselectable) return;
	this.selected=b;
	if (this.itemStyle.bos) this.setFontBold(b,false);
	if (this.itemStyle.ios) this.setFontItalic(b,false);
	if (b) {
		this.setBgColor(this.itemStyle.bgColorSelect);
		this.setFontColor(this.itemStyle.textSelect);
		this.invokeEvent("select");
	}
	else {
		this.setBgColor(this.isMouseOver?this.itemStyle.bgColorRoll:this.itemStyle.bgcolor)
		this.setFontColor(this.itemStyle.textNormal);
		this.invokeEvent("deselect");
	}
}
ListItem.prototype.setValue = function(value) {
	this.value=value;
};
ListItem.prototype.getValue = function() {
	return this.value;
};

function List(){
	this.DynLayer=DynLayer;
	this.DynLayer();
	this.multiMode = false;
	this.items = [];
	this.addEventListener(List.listener);
	this.ievents = new EventListener(this);
	this.ievents.onselect = function(e){
		e.getTarget().select(e.getSource());
	};
	this.ievents.ondeselect = function(e){
  		e.getTarget().deselect(e.getSource());
	};
	/*default style*/
	this.listStyle = {};
	this.listStyle.borders = 1;
	this.listStyle.spacing = 1;
	this.listStyle.padding = 4;
	this.listStyle.bos = false;
	this.listStyle.ios = false;
	this.listStyle.bg = "#eeeeee";
	this.listStyle.bgRoll = "#cccccc";
	this.listStyle.bgSelect = "lightblue";
	this.listStyle.textNormal = this.listStyle.textRoll = this.listStyle.textSelect = "#000000";
	this.totalHeight = this.listStyle.borders;

}
List.prototype = new DynLayer();
List.prototype.add = function(text,value){
	var i = new ListItem(text,value);
	i.list = this;
	var ls = this.listStyle;
	i.setColors(ls.bg,ls.bgRoll,ls.bgSelect,ls.textNormal,ls.textRoll,ls.textSelect);
	if (ls.bos) i.boldOnSelect(true,false);
	if (ls.ios) i.italicOnSelect(true,false);
	i.setPadding(ls.padding);
	i.addEventListener(this.ievents);
	this.items[this.items.length] = i;
	this.addChild(i);
};
List.prototype.arrangeItems = function(){
	this.totalHeight = this.listStyle.borders;
	for (var i=0;i<this.items.length;i++){
		this.items[i].moveTo(this.listStyle.borders,this.totalHeight);
		this.items[i].setWidth(this.w-this.listStyle.borders*2);
		this.totalHeight = this.totalHeight+this.items[i].h+this.listStyle.spacing;	
	}
	this.setHeight(this.totalHeight-this.listStyle.spacing+this.listStyle.borders);
};
List.prototype.remove = function(item){
	var i = this.getIndexOf(item);
	if (i==-1) return;
	this.items[i].deleteFromParent();
	Methods.removeFromArray(this.items,item);
	if (this.selectedIndex==i){
		this.selectedIndex=-1;
		this.selectedItem=null;
	}
	this.arrangeItems();
};
List.prototype.origSetWidth = DynLayer.prototype.setWidth;
List.prototype.setWidth = function(w){
	this.origSetWidth(w);
	for (var i=0;i<this.items.length;i++){
		this.items[i].setWidth(w-this.listStyle.borders*2);
	}
};
List.listener = new EventListener()
List.listener.oncreate = function(e){
    var o = e.getSource();
	o.arrangeItems();
};
List.prototype.getIndexOf = function(item){
	for (var i=0;i<this.items.length;i++){
		if (this.items[i]==item) return i;
	}
	return -1;
};
List.prototype.select = function(item){
	this.selectedIndex = this.getIndexOf(item);
	this.selectedItem = item;
	if (this.multiMode) return;
	for (var i=0;i<this.items.length;i++){
		if (this.items[i] != item) this.items[i].setSelected(false);
	}
	this.invokeEvent("select");
};
List.prototype.deselect = function(item){
	if (this.selectedItem == item){
		this.selectedItem = null;
		this.selectedIndex = -1;
	}
};
List.prototype.deselectAll = function(){
	for (var i=0;i<this.items.length;i++) {
		if (this.items[i].selected) this.items[i].setSelected(false);
	}
};
	
List.prototype.setSelectionMode = function(mode) {
	this.deselectAll();
	this.multiMode = mode;
};
List.prototype.setColors = function(bg,bgRoll,bgSelect,textNormal,textRoll,textSelect){
    var ls = this.listStyle;
	ls.bg = bg||ls.bg;
	ls.bgRoll = bgRoll||ls.bgRoll;
	ls.bgSelect = bgSelect||ls.bgSelect;
	ls.textNormal = textNormal||ls.textNormal;
	ls.textRoll = textRoll||ls.textRoll;
	ls.textSelect = textSelect||ls.textSelect;
	if (this.items.length == 0) return;
	for (var i=0;i<this.items.length;i++) {
		this.items[i].setColors(bg,bgRoll,bgSelect,textNormal,textRoll,textSelect);
	}
};
List.prototype.boldOnSelect = function(b) {
	this.listStyle.bos = b;
};
List.prototype.italicOnSelect = function(b) {
	this.listStyle.ios = b;
};
List.prototype.getSelectedIndex = function() {
    return this.selectedIndex;
};
List.prototype.getSelectedItem = function() {
	return this.selectedItem;
};
List.prototype.getSelectedIndexes = function() {
	var a = [];
	for (var i=0;i<this.items.length;i++) if (this.items[i].selected) a[a.length] = i;
	return a;
};
List.prototype.setBorders = function(b){
	this.listStyle.borders = b;
	if (this.created) this.arrangeItems();
};
List.prototype.setSpacing = function(b){
	this.listStyle.spacing = b;
	if (this.created) this.arrangeItems();
};