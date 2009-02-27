
//*****************************************************************************
// Do not remove this notice.
//
// Copyright 2000 by Mike Hall.
// See http://www.brainjar.com for terms of use.

// Modified by Khue Nguyen, 2005 Copyright Jahia Solutions
//*****************************************************************************

//----------------------------------------------------------------------------
// Code to determine the browser and version.
//----------------------------------------------------------------------------

function Browser() {

  var ua, s, i;

  this.isIE    = false;  // Internet Explorer
  this.isNS    = false;  // Netscape
  this.version = null;

  ua = navigator.userAgent;

  s = "MSIE";
  if ((i = ua.indexOf(s)) >= 0) {
    this.isIE = true;
    this.version = parseFloat(ua.substr(i + s.length));
    return;
  }

  s = "Netscape6/";
  if ((i = ua.indexOf(s)) >= 0) {
    this.isNS = true;
    this.version = parseFloat(ua.substr(i + s.length));
    return;
  }

  // Treat any other "Gecko" browser as NS 6.1.

  s = "Gecko";
  if ((i = ua.indexOf(s)) >= 0) {
    this.isNS = true;
    this.version = 6.1;
    return;
  }
}

var browser = new Browser();

//----------------------------------------------------------------------------
// Code for handling the menu bar and active button.
//----------------------------------------------------------------------------

// Check the browser for DOM manipulation
function checkBrowser(){
	this.ver=navigator.appVersion;
	this.dom=document.getElementById?1:0;
	this.ie6=(this.ver.indexOf("MSIE 6")>-1 && this.dom)?1:0;
	this.ie55=((this.ver.indexOf("MSIE 5.5")>-1 || this.ie6) && this.dom)?1:0;
	this.ie5=((this.ver.indexOf("MSIE 5")>-1 || this.ie5 || this.ie6) && this.dom)?1:0;
	this.ie4=(document.all && !this.dom)?1:0;
	this.ns5=(this.dom && parseInt(this.ver) >= 5) ?1:0;
	this.ns4=(document.layers && !this.dom)?1:0;
	this.ie4plus=(this.ie6 || this.ie5 || this.ie4);
	this.ie5plus=(this.ie6 || this.ie5)
	this.bw=(this.ie6 || this.ie5 || this.ie4 || this.ns4 || this.ns5);
	return this;
}

bw = new checkBrowser();

// Get an Object contained in a DOM document by its ID attribute
function getObjectById (ID) {
	var obj;
	if (bw.dom)
		return document.getElementById (ID);
	else if (bw.ie4)
		return document.all (ID);
	else 
		alert ("Error: Your browser version is not supported. Please upgrade...");
}

// Returns an array of values. The delimiter is the ',' character
function getNodeValues (content, nodeName) {
	var tag = "<" + nodeName + ">";
	var start = content.indexOf (tag);
	var end   = content.indexOf ("</" + nodeName + ">");
	
	if (start < end) {
	    var values = content.substring (start + tag.length, end);
		return values.split (";;");
	} else {
		return new Array (0);
	}
}

// Returns the value of an XML tag
function getNodeValue (content, nodeName) {
	var tag = "<" + nodeName + ">";
	var start = content.indexOf (tag);
	var end   = content.indexOf ("</" + nodeName + ">");
	
	if (start < end) {
		return content.substring (start + tag.length, end);
	} else {
		return null;
	}
}

//-------------------------------------------------------------------------------------------------
//
// Date Time server using AJAX
//
//-------------------------------------------------------------------------------------------------
// AJAX based function to get the server time
function getServerTime (context,targetId,dateFormat,timeZoneOffSet,displayTime) {
    var req;
	try {
		// correct values are "POST" or "GET" (HTTP methods).
		var method = "GET" ;
	    var url = context + "/ajaxaction/GetServerTime";
		var data = "dateformat=" + dateFormat + "&timeZoneOffSet=" + timeZoneOffSet;
		
		if (method == "GET") {
			url += "?" + data;
			data = null;
		}

		// Create new XMLHttpRequest request
    	if (window.XMLHttpRequest) {
        	req = new XMLHttpRequest ();
			
    	} else if (window.ActiveXObject) {
        	req = new ActiveXObject ("Microsoft.XMLHTTP");
			
    	} else {
			alert ("Error: Your Browser does not support XMLHTTPRequests, please upgrade...");  
			return;
		}
		req.open (method, url, true);
				
		req.onreadystatechange = function () {
			displayServerTime(req,targetId,dateFormat);
		}

		if (method == "POST") {
			req.setRequestHeader ("Content-type", "application/x-www-form-urlencoded"); 
		}
		if ( data != null ){
			req.send (data);
		} else {
			req.send("");
		}
		
	} catch (e) {
		alert ("Exception sending the Request: " + e);
	}
}

function displayServerTime (req,targetId,dateFormat,displayTime) {
	var readyState = req.readyState;
	if (req.readyState == 4) {
		// alert ("resp: " + req.responseText);
    	if (req.status == 200) {
			try {
				var response = req.responseText;
				var serverTime = getNodeValue (response, "servertime");
				var targetObject = getObjectById (targetId);
				targetObject.innerHTML = serverTime;  
			} catch (e) {
				alert ("Exception retrieving server time " + e);
			}
		} else {
			alert ("There was a problem processing the request. Status: " + 
						   req.status + ", msg: " + req.statusText);
		}
	}	
}


/**
 * Returns the number of minutes + seconds in millis between the Server and client time
 *
 */
function getServerClientTimeDiff (context,targetId,clientTime,clientTimeZoneOffSet) {
    var req;
	try {
		// correct values are "POST" or "GET" (HTTP methods).
		var method = "GET" ;
	    var url = context + "/ajaxaction/GetClientServerTimeDiff";
		var data = "clientTime="+clientTime+"&timeZoneOffSet=" + clientTimeZoneOffSet;
		
		if (method == "GET") {
			url += "?" + data;
			data = null;
		}

		// Create new XMLHttpRequest request
    	if (window.XMLHttpRequest) {
        	req = new XMLHttpRequest ();
			
    	} else if (window.ActiveXObject) {
        	req = new ActiveXObject ("Microsoft.XMLHTTP");
			
    	} else {
			alert ("Error: Your Browser does not support XMLHTTPRequests, please upgrade...");  
			return;
		}
		req.open (method, url, true);
				
		req.onreadystatechange = function () {
			setServerClientTimeDiff(req,targetId);
		}

		if (method == "POST") {
			req.setRequestHeader ("Content-type", "application/x-www-form-urlencoded"); 
		}
		if ( data != null ){
			req.send (data);
		} else {
			req.send("");
		}
		
	} catch (e) {
		alert ("Exception sending the Request: " + e);
	}
}

function setServerClientTimeDiff(req,targetId) {
	var readyState = req.readyState;
	if (req.readyState == 4) {
    	if (req.status == 200) {
			try {
				var response = req.responseText;
				var value = getNodeValue (response, "serverClientTimeDiff");
				var targetObject = getObjectById (targetId);
				targetObject.value = value;
			} catch (e) {
				alert ("Exception retrieving server client diff time " + e);
			}
		} else {
			alert ("There was a problem processing the request. Status: " + 
						   req.status + ", msg: " + req.statusText);
		}
	}	
}		

