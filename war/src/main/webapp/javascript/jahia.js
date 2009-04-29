

 // global popup
var myEngineWin = null;
var pickerlist = null;
var workInProgress = null;
var GlobalCallbackCount = 0;
var GlobalCallback = new Array();
var GlobalListenerCount = 0;
var GlobalCommands = new Array();
var GlobalPrototypeCallCount = 0;
var GlobalPrototypeCalls = new Array();
// OpenJahiaWindow
function OpenJahiaWindow(url, name, width, height) {
    var params;
    // check for veryvery small screens
    if (screen.availHeight <= 720 || screen.availWidth <= 950) {
        width = screen.availWidth;
        height = screen.availHeight;
        params = "width=" + width + ",height=" + height + ",left=10,top=10,resizable=yes,scrollbars=yes,status=no";
    } else {
        params = "width=" + width + ",height=" + height + ",left=10,top=10,resizable=yes,scrollbars=no,status=no";
    }

    // Hollis : check if the popup is alread opened, if so, give it the focus
    if (myEngineWin != null) {
        try {
            if (myEngineWin.closed) { // need to test it first...
                myEngineWin = null;
                myEngineWin = window.open(url, name, params);
            } else {
                if (myEngineWin.name != name) {
                    myEngineWin.close();
                    myEngineWin = null;
                    myEngineWin = window.open(url, name, params);
                } else {
                    myEngineWin.focus();
                }
            }
        } catch (ex) {
            // suppress exception
        }
    } else {
        myEngineWin = window.open(url, name, params);
    }

}
// end OpenJahiaWindow


// OpenJahiaScrollableWindow
function OpenJahiaScrollableWindow(url, name, width, height) {

    // check for veryvery small screens
    if (screen.availHeight <= 720 || screen.availWidth <= 950) {
        width = screen.availWidth;
        height = screen.availHeight;
    }

    var params = "width=" + width + ",height=" + height + ",left=10,top=10,resizable=yes,scrollbars=yes,status=no";
    // Check if the popup is alread opened, if so, give it the focus
    if (myEngineWin != null) {
        try {
            if (myEngineWin.closed) { // need to test it first...
                myEngineWin = null;
                myEngineWin = window.open(url, name, params);
            } else {
                if (myEngineWin.name != name) {
                    myEngineWin.close();
                    myEngineWin = null;
                    myEngineWin = window.open(url, name, params);
                } else {
                    myEngineWin.focus();
                }
            }
        } catch (ex) {
            // suppress exception
        }
    } else {
        myEngineWin = window.open(url, name, params);
    }
}
// end OpenJahiaScrollableWindow

// ReloadEngine
function ReloadEngine(params) {
    var oldurl = location.href;
    var pos = oldurl.indexOf("&engine_params");
    if (pos != -1) {
        oldurl = oldurl.substring(0, pos);
    }
    location.href = oldurl + "&engine_params=" + params;
}

// end ReloadEngine
function closeEngineWin() {
    document.body.style.cursor = "default";
    if (myEngineWin != null) {
        try {
            if (myEngineWin.closed) { // need to test it first...
                purge(myEngineWin.document);
                myEngineWin = null;
            } else {
                myEngineWin.close();
                purge(myEngineWin.document);
                myEngineWin = null;
            }
        } catch (ex) {
            // suppress exception
        }
    } else {
        // it's not an engine window
        purge(document.body);

    }
}

function closeEngineWinAllPopups() {
    document.body.style.cursor = "default";
    try {
        if (myEngineWin != null) {
            if (myEngineWin.closed) { // need to test it first...
                if (myEngineWin.myWin && !myEngineWin.myWin.closed) {
                    myEngineWin.myWin.close();
                    purge(myEngineWin.myWin.document);
                }
                purge(myEngineWin.document);
                myEngineWin.myWin = null;
                myEngineWin = null;
            } else {
                if (myEngineWin.myWin && !myEngineWin.myWin.closed) {
                    myEngineWin.myWin.close();
                    purge(myEngineWin.myWin.document);
                }
                myEngineWin.close();
                purge(myEngineWin.document);
                myEngineWin.myWin = null;
                myEngineWin = null;
            }
        }
    } catch (e) {
    }
    try {
        removeAll();
    } catch (ex) {
    }
}

var oldLoc = "";

// CloseJahiaWindow
function CloseJahiaWindow(refreshOpener, containerAnchor) {
    var params = "";
    if (CloseJahiaWindow.arguments.length > 2) {
        params = CloseJahiaWindow.arguments[2];
    }

    if (! window.opener) {
        window.close();
        delete window;
        if (myEngineWin != null) purge(myEngineWin.document);
        myEngineWin = null;
        return;
    }

    var oldUrl = window.opener.location.href;
    var pos = oldUrl.indexOf("&engine_params");
    if (pos != -1) {
        oldUrl = oldUrl.substring(0, pos);
    }

    var newUrl = oldUrl;
    if (params != "") {
        newUrl += params;
    }

    //alert( "Refreshing window with url :\n" + newUrl +"\n params: "+params+ "refreshOpener = "+refreshOpener);
    if (newUrl.charAt(newUrl.length - 1) == '#') {
        newUrl = newUrl.substring(0, newUrl.length - 1);
    }
    try {
        if (params.indexOf("submit") != -1) {
            if (refreshOpener.indexOf("yes") != -1) {
                if (window.opener != null) {
                    window.opener.document.forms[0].submit();
                }
            } else {
                window.opener.refreshMonitor();
            }
        } else {
            if (refreshOpener.indexOf("yes") != -1) {
                var hashIndex = newUrl.indexOf("#");
                if (hashIndex > -1) {
                    newUrl = newUrl.substring(0, hashIndex);
                }
                if (containerAnchor) {
                    var reloadIndex = newUrl.indexOf("?goto=");
                    if (reloadIndex > -1) {
                        newUrl = newUrl.substring(0, reloadIndex);
                    }

                    if (navigator.appName == "Netscape") { // For Firefox which has a Netscape appname
                        window.opener.location.reload();
                    } else {
                        var url = newUrl + "?goto=" + containerAnchor + "#" + containerAnchor;
                        if (url == window.opener.location.href) {
                        		url = newUrl + "#" + containerAnchor;
                        }
                        window.opener.location.replace(url);
                    }
                } else {
                    window.opener.location.href = newUrl;
                }
            } else {
                window.opener.refreshMonitor();
            }
            oldLoc = window.opener.location;
            WaitForRefresh();
        }
        window.close();
        if (myEngineWin != null) purge(myEngineWin.document);
        purge(window.document);
        delete window;
        myEngineWin = null;
    } catch (ex) {
        window.close();
        if (myEngineWin != null) purge(myEngineWin.document);
        purge(window.document);
        myEngineWin = null;
        delete window;
    }
}
// end CloseJahiaWindow

// WaitForRefresh (called by CloseJahiaWindow)
function WaitForRefresh() {
    // alert( "Trying to close" );
    var newLoc = window.opener.location;
    if (newLoc != oldLoc) {
        setTimeout("WaitForRefresh()", 100);
    } else {
        window.close();
    }
}
// end WaitForRefresh

// CloseJahiaWindow - added for deleting current site to display parentsite after delete
function CloseJahiaWindowWithUrl(newUrl) {
    //alert( "Rereshing window with url :\n" + newUrl );
    window.opener.location.href = newUrl;
    oldLoc = window.opener.location;
    WaitForRefresh();
} // end CloseJahiaWindow

// saveAndAddNew
function saveAndAddNew(url) {
    var engineWin = window.opener.myEngineWin;
    if (window.opener.myEngineWin != null) purge(window.opener.myEngineWin.document);
    window.opener.myEngineWin = null;
    window.location.href = url;
    //if ( refreshOpener == "yes" ){
    //window.opener.location.href = defineMatrixParam(window.opener.location.href);
    //}
    oldLoc = window.opener.location;
    while (window.opener.location != null
            && (oldLoc != window.opener.location)) {
        setTimeout("", 1000);
    }
    window.opener.myEngineWin = engineWin;
}

// applyJahiaWindow
function applyJahiaWindow(url) {
    window.location.href = url;
}

// closePopupWindow
function closePopupWindow() {
    var params = "";
    if (closePopupWindow.arguments.length > 0) {
        params = closePopupWindow.arguments[0];
    }
    if (closePopupWindow.arguments.length > 1) {
        var refreshOpener = closePopupWindow.arguments[1];
        if (refreshOpener == "yes") {
            var theUrl = window.opener.location.href;
            if (theUrl.indexOf("?") != -1) {
                if (params.charAt(0) == "&") {
                    theUrl += params;
                } else {
                    theUrl += "&" + params;
                }
            } else {
                if (params.charAt(0) == "&") {
                    theUrl += "?" + params.substring(1, params.length);
                } else {
                    theUrl += "?" + params;
                }
            }
            window.opener.location.href = theUrl;
        }
    }
    window.close();
    if (myEngineWin != null) purge(myEngineWin.document);
    purge(window.document);
    myEngineWin = null;
    delete window;
}

// applyPopupWindow
function applyPopupWindow(popupNewUrl, openerUrlParams, refreshOpener) {
    if (refreshOpener == "yes") {
        var theUrl = window.opener.location.href;
        if (theUrl.indexOf("?") != -1) {
            if (openerUrlParams.charAt(0) == "&") {
                theUrl += openerUrlParams;
            } else {
                theUrl += "&" + openerUrlParams;
            }
        } else {
            if (openerUrlParams.charAt(0) == "&") {
                theUrl += "?" + openerUrlParams.substring(1, openerUrlParams.length);
            } else {
                theUrl += "?" + openerUrlParams;
            }
        }
        window.opener.location.href = theUrl;
        WaitForRefresh();
    }
    window.location.href = popupNewUrl;
}

function MM_preloadImages() { //v3.0
    var d = document;
    if (d.images) {
        if (!d.MM_p) d.MM_p = new Array();
        var i,j = d.MM_p.length,a = MM_preloadImages.arguments;
        for (i = 0; i < a.length; i++)
            if (a[i].indexOf("#") != 0) {
                d.MM_p[j] = new Image;
                d.MM_p[j++].src = a[i];
            }
    }
}

function MM_swapImgRestore() { //v3.0
    var i,x,a = document.MM_sr;
    for (i = 0; a && i < a.length && (x = a[i]) && x.oSrc; i++) x.src = x.oSrc;
}

function MM_findObj(n, d) { //v3.0
    var p,i,x;
    if (!d) d = document;
    if ((p = n.indexOf("?")) > 0 && parent.frames.length) {
        d = parent.frames[n.substring(p + 1)].document;
        n = n.substring(0, p);
    }
    if (!(x = d[n]) && d.all) x = d.all[n];
    for (i = 0; !x && i < d.forms.length; i++) x = d.forms[i][n];
    for (i = 0; !x && d.layers && i < d.layers.length; i++) x = MM_findObj(n, d.layers[i].document);
    return x;
}

function MM_swapImage() { //v3.0
    var i,j = 0,x,a = MM_swapImage.arguments;
    document.MM_sr = new Array;
    for (i = 0; i < (a.length - 2); i += 3)
        if ((x = MM_findObj(a[i])) != null) {
            document.MM_sr[j++] = x;
            if (!x.oSrc) x.oSrc = x.src;
            x.src = a[i + 2];
        }
}


function setfocus() {
}
// This function has to be on the body tag (onLoad) but the declaration isn't on all includes.

// Used with container list pagination
// Set
function changePage(whatForm, scrollingInput, val) {
    scrollingInput.value = val;
    var ctnListname = scrollingInput.name.substring(10, scrollingInput.name.length);
    whatForm.elements['ctnlistpagination_' + ctnListname].value = 'true';
    whatForm.submit();
}

/**
 * This method removes a query parameter from an URL, very practical
 * when we want to update a value or just remove the param altogether.
 *
 * @param paramURL the URL to remove the query parameter from
 * @param key name of the parameter in the query string
 * @return the modified URL.
 */
function removeQueryParam(paramURL, key) {
    var queryPos = paramURL.indexOf('?');
    if (queryPos < 0) {
        return paramURL;
    }
    var pairs = paramURL.substring(queryPos + 1).split("&");
    var newURL = paramURL.substring(0, queryPos + 1);
    var nbPairs = 0;

    for (var i = 0; i < pairs.length; i++) {
        var pos = pairs[i].indexOf('=');
        if (pos >= 0) {
            var argname = pairs[i].substring(0, pos);
            if (argname != key) {
                nbPairs++;
                if (nbPairs > 1) {
                    newURL += "&";
                }
                newURL += pairs[i];
            }
        }
    }
    return newURL;
}

// just display pickers straight ahead
function displayPickers(ctx, id, width, height) {
    params = "width=" + width + ",height=" + height + ",top=0,left=0,resizable=yes,scrollbars=yes,status=yes";
    url = ctx + "/engines/importexport/dispPickers.jsp?id=" + id;
    if (pickerlist) pickerlist.close();
    pickerlist = window.open(url, "jwin", params);
}

function removeAll() {
    if (jahia.config) {
        jahia.config.contextPath = null;
        jahia.config.i18n['org.jahia.button.ok'] = null;
        jahia.config.i18n['org.jahia.button.saveAddNew'] = null;
        jahia.config.i18n['org.jahia.button.apply'] = null;
        jahia.config.i18n = null;
        jahia.config.theScreen = null;
        jahia.config.lockKey = null;
        jahia.config.lockType = null;
        jahia.config.pid = null;
        jahia.config.needToRefreshParentPage = null;
        jahia.config.jspSource = null;
        jahia.config.sendKeepAliveTimeOut = null;
        jahia.config = null;
        jahia = null;
    }
    for (i = 0; i < GlobalListenerCount; i++) {
        GlobalCommands[i].removeAllInvokeListeners();
        GlobalCommands[i].commandXmlDoc = null;
        GlobalCommands[i]._responseXmlDoc = null;
        GlobalCommands[i]._st = null;
        GlobalCommands[i]._en = null;
        GlobalCommands[i] = null;
    }
    GlobalListenerCount = 0;
    for (i = 0; i < GlobalCallbackCount; i++) {
        GlobalCallback[i].obj = null;
        GlobalCallback[i].func = null;
        GlobalCallback[i]._args = null;
        GlobalCallback[i] = null;
    }
    GlobalCallbackCount = 0;
    for (i = 0; i < GlobalPrototypeCallCount; i++) {
    	  GlobalPrototypeCalls[i].transport = null;
        purge(GlobalPrototypeCalls[i]);
        GlobalPrototypeCalls[i] = null;
    }
    GlobalPrototypeCallCount = 0;

    window.currentValue = null;
    var treeKeys = window["treeItems"];
    if (treeKeys) {
        var keys = treeKeys.split(",");
        for (var j = 0; j < keys.length; j++) {
            window["treeItem" + keys[j]] = null;
        }
        delete keys;
        window["treeItems"] = null;
    }
    delete treeKeys;

    window.onbeforeunload = null;
    var el = document.getElementById("tree1");
    if (el) {
        el.innerHTML = "";
    }
    el = null;
    document.close();
    document.clear();
    purge(document);
    delete window;
}
function addIframeElement(replaceObjectID, srcVal, widthVal, heightVal, idVal, frameborderVal, scrollingVal, alignVal, className) {
    if (document.getElementById(idVal)) {
        return;
        // already added
    }
    var newIframeElement = document.createElement('iframe');
    newIframeElement.setAttribute('width', widthVal);
    newIframeElement.setAttribute('height', heightVal);
    newIframeElement.setAttribute('frameborder', frameborderVal);
    newIframeElement.setAttribute('scrolling', scrollingVal);
    newIframeElement.setAttribute('align', alignVal);
    newIframeElement.setAttribute('id', idVal);
    newIframeElement.setAttribute('name', idVal);
    newIframeElement.className = className;
    var replaceObject = document.getElementById(replaceObjectID);
    if (replaceObject) {
        replaceObject.parentNode.replaceChild(newIframeElement, replaceObject);
        top[idVal].location.href = srcVal;
    }
    top[idVal].name = idVal;
}

function adjustIFrameSize(iframeWindow) {
    if (iframeWindow.document.height) {
        var element = parent.document.getElementById(iframeWindow.name);
        element.style.height = iframeWindow.document.height + 'px';
        element.style.width = iframeWindow.document.width + 'px';

    } else if (document.all) {
        var iframeElement = parent.document.all[iframeWindow.name];
        if (iframeWindow.document.compatMode &&
            iframeWindow.document.compatMode != 'BackCompat') {
            iframeElement.style.height =
            iframeWindow.document.documentElement.scrollHeight + 5 + 'px';
            iframeElement.style.width =
            iframeWindow.document.documentElement.scrollWidth + 5 + 'px';
        } else {
            iframeElement.style.height = iframeWindow.document.body.scrollHeight + 5 + 'px';
            iframeElement.style.width = iframeWindow.document.body.scrollWidth + 5 + 'px';
        }
    }
}

function handleTimeBasedPublishing(event, serverURL, objectKey, params, dialogTitle) {
    //alert("timeBasedPub : objecKey=" + objectKey + ", params=" + params);
    var tbpStatus = new TimeBasedPublishingStatus();
    tbpStatus.run(event, serverURL, objectKey, params, dialogTitle);
}

function fixPNG(myImage) {
    var arVersion = navigator.appVersion.split("MSIE")
    var version = parseFloat(arVersion[1])

    if ((version >= 5.5) && (version < 7) && (document.body.filters)) {
        var imgID = (myImage.id) ? "id='" + myImage.id + "' " : ""
        var imgClass = (myImage.className) ? "class='" + myImage.className + "' " : ""
        var imgTitle = (myImage.title) ?
                       "title='" + myImage.title + "' " : "title='" + myImage.alt + "' "
        var imgStyle = "display:inline-block;" + myImage.style.cssText
        myImage.outerHTML = "<span " + imgID + imgClass + imgTitle
                + " style=\"" + "width:" + myImage.width
                + "px; height:" + myImage.height
                + "px;" + imgStyle + ";"
                + "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader"
                + "(src=\'" + myImage.src + "\', sizingMethod='scale');\"></span>"
    }
}

/*
 The purge function takes a reference to a DOM element as an argument. It loops through the element's attributes.
 If it finds any functions, it nulls them out. This breaks the cycle, allowing memory to be reclaimed. It will also look
 at all of the element's descendent elements, and clear out all of their cycles as well. The purge function is harmless
 on Mozilla and Opera. It is essential on IE. The purge function should be called before removing any element, either by
 the removeChild method, or by setting the innerHTML property.
 */
function purge(d) {
    if (!d) return;
    //alert ("purge: " + d.attributes + ", " + d);
    try {
        var a = d.attributes, i, l, n;
        if (a) {
            l = a.length;
            for (i = 0; i < l; i += 1) {
                n = a[i].name;
                if (typeof d[n] === 'function') {
                    d[n] = null;
                }
            }
        }
        a = d.childNodes;
        if (a) {
            l = a.length;
            for (i = 0; i < l; i += 1) {
                purge(d.childNodes[i]);
            }
        }
    } catch (e) {
        // ignore
    }
    a = null;
    n = null;
    d = null;
}

function getPageOffsetLeft(el) {

    var x;

    // Return the x coordinate of an element relative to the page.

    x = el.offsetLeft;
    if (el.offsetParent != null)
        x += getPageOffsetLeft(el.offsetParent);

    return x;
}

function getPageOffsetTop(el) {

    var y;

    // Return the x coordinate of an element relative to the page.

    y = el.offsetTop;
    if (el.offsetParent != null)
        y += getPageOffsetTop(el.offsetParent);

    return y;
}

var frameBodySizes = [];
var lastFrameBodySize = [];
var numberOfCalls = []; 
var numberOfFrames = 0;

function iFrameDocumentWrite(iFrameElement, content, delayedIFrameResizeTime) {
  numberOfFrames++;
  frameBodySizes[numberOfFrames] = iFrameElement;
  iFrameElement.onload = function () { resizeIFrame(iFrameElement) } ;
  var oDoc = iFrameElement.contentWindow || iFrameElement.contentDocument;
  if (oDoc.document) {
      oDoc = oDoc.document;
  }
  oDoc.open();
  oDoc.write(content);
  // oDoc.close();
  if ((oDoc.body) && (oDoc.body.scrollHeight)) {
      iFrameElement.height= oDoc.body.scrollHeight;
      lastFrameBodySize[numberOfFrames] = oDoc.body.scrollHeight;
      numberOfCalls[numberOfFrames] = 0;
  }
  resizeTime = Number(delayedIFrameResizeTime);
  if (resizeTime != 0) {
    setTimeout('adjustFrameHeight('+numberOfFrames+')', resizeTime);
  }
}

function adjustFrameHeight(frameNumber) {
    iFrameElement = frameBodySizes[frameNumber];
    numberOfCalls[numberOfFrames] = numberOfCalls[numberOfFrames] + 1;
    if (numberOfCalls[numberOfFrames] > 10) {
        alert('Maximum number of calls to adjust iFrame height reached, aborting... ');
        return;
    }

    var oDoc = iFrameElement.contentWindow || iFrameElement.contentDocument;
    if (oDoc.document) {
        oDoc = oDoc.document;
    }
    if ((oDoc.body) && (oDoc.body.scrollHeight)) {
        if (lastFrameBodySize[frameNumber] != oDoc.body.scrollHeight) {
            iFrameElement.height= oDoc.body.scrollHeight;
            lastFrameBodySize[frameNumber] = oDoc.body.scrollHeight;
            if (resizeTime != 0) {
              setTimeout('adjustFrameHeight('+frameNumber+')', resizeTime);
            }
        }
    }
}

function resizeIFrame(element) {
    var iFrameElement = element;
    var oDoc = iFrameElement.contentWindow || iFrameElement.contentDocument;
    if (oDoc.document) {
        oDoc = oDoc.document;
    }
    if ((oDoc.body) && (oDoc.body.scrollHeight)) {
        iFrameElement.height= oDoc.body.scrollHeight;
    }
}

window.onunload = closeEngineWinAllPopups;
