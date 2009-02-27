/*
   DynAPI Distribution
   Console Debugging Utility

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	none
*/

DynAPI.console={
	enabled : true,
	
	open : function() {
		if (!DynAPI.console.enabled) return;
		DynAPI.console.consolewin=window.open('','DynAPIConsoleWindow','resizable=1,scrollbars=1');
		DynAPI.console.consolewin.document.open('text/plain');
	},	
	write : function(msg) {
		if (!DynAPI.console.enabled) return;
		if (!DynAPI.console.consolewin || DynAPI.console.consolewin.closed) DynAPI.console.open();
		DynAPI.console.consolewin.document.writeln(msg);
		if (is.ie) DynAPI.console.consolewin.scrollTo(0, document.body.scrollHeight);
	},	
	close : function() {
		if (DynAPI.console.consolewin) DynAPI.console.consolewin.close();
	},
	enable : function() {
		DynAPI.console.enabled=true;
	},
	disable : function() {
		DynAPI.console.enabled=false;
	},
	clear : function() {
		DynAPI.console.consolewin.document.open('text/plain');
	},
	dumpProperties : function(obj,hidemethods) {
		DynAPI.console.write('\nObject Properties\n-----------------')
		var s=[];
		for (var i in obj) {
			var l=s.length;
			if (typeof obj[i]=='function') {
				if (!hidemethods) s[l]=i+' = [method]';
				else continue;
			}
			else if (typeof obj[i]=='object') s[l]=i+' = '+obj[i];
			else s[l]=i+' ('+(typeof obj[i])+')'+' = '+obj[i];
		};
		s.sort();
		DynAPI.console.write(s.join('\n'));
	}
};