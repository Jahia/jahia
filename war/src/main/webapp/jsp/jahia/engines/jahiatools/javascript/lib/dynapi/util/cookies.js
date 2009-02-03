/*
   DynAPI Distribution
   Cookies Class

   The DynAPI Distribution is distributed under the terms of the GNU LGPL license.

   Requirements:
	dynapi.api [dynlayer, dyndocument, browser]
*/ 
DynAPI.cookies = {
	saveCookie : function(name,value,days) {
		if (days) {
			var date=new Date();
			date.setTime(date.getTime()+(days*24*60*60*1000));
			var expires="; expires="+date.toGMTString();
		} 
		else expires = "";
		document.cookie = name+"="+value+expires+"; path=/";
	},
	readCookie : function(name) {
		var nameEQ=name+"=";
		var ca=document.cookie.split(';');
		for(var i=0;i<ca.length;i++) {
			var c=ca[i];
			while (c.charAt(0)==' ') c=c.substring(1,c.length);
			if (c.indexOf(nameEQ)==0) return c.substring(nameEQ.length,c.length);
		}
		return null;
	},
	deleteCookie : function(name) {
		DynAPI.cookies.saveCookie(name,"",-1);
	}
}
