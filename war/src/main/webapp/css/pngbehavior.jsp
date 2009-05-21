<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@page language="java"%><%
    response.setContentType("text/x-component");
	response.setHeader("Cache-Control", "public, max-age=604800, post-check=7200, pre-check=604800");
	response.setDateHeader("Expires", System.currentTimeMillis() + 604800 * 1000); // 7 days
    final String path = request.getContextPath() + "/css/blank.gif";
%>
<public:component lightWeight="true">
<public:attach event="onpropertychange" onevent="propertyChanged()" />
<public:attach event="onbeforeprint" onevent="beforePrint()" for="window"/>
<public:attach event="onafterprint" onevent="afterPrint()" for="window"/>
<script>

/*
 * PNG Behavior
 *
 * This script was created by Erik Arvidsson (http://webfx.eae.net/contact.html#erik)
 * for WebFX (http://webfx.eae.net)
 * Copyright 2002-2004
 *
 * For usage see license at http://webfx.eae.net/license.html
 *
 * Version: 1.02
 * Created: 2001-??-??	First working version
 * Updated: 2002-03-28	Fixed issue when starting with a non png image and
 *                      switching between non png images
 *          2003-01-06	Fixed RegExp to correctly work with IE 5.0x
 *          2004-05-09  When printing revert to original
 *
 */

var supported = /MSIE ((5\.5)|6)/.test(navigator.userAgent) &&
				(navigator.platform == "Win32" || navigator.platform == "Win64");

var realSrc;
var blankSrc = "<%=path%>";
var isPrinting = false;

if (supported) fixImage();

function propertyChanged() {
	if (!supported || isPrinting) return;

	var pName = event.propertyName;
	if (pName != "src") return;
	// if not set to blank
	if (!new RegExp(blankSrc).test(src))
		fixImage();
};

function fixImage() {
	// get src
	var src = element.src;

	// check for real change
	if (src == realSrc && /\.png$/i.test(src)) {
		element.src = blankSrc;
		return;
	}

	if ( ! new RegExp(blankSrc).test(src)) {
		// backup old src
		realSrc = src;
	}

	// test for png
	if (/\.png$/i.test(realSrc)) {
		// set blank image
		element.src = blankSrc;
		// set filter
		element.runtimeStyle.filter = "progid:DXImageTransform.Microsoft." +
					"AlphaImageLoader(src='" + src + "',sizingMethod='scale')";
	}
	else {
		// remove filter
		element.runtimeStyle.filter = "";
	}
}

function beforePrint() {
	isPrinting = true;
	element.src = realSrc;
	element.runtimeStyle.filter = "";
	realSrc = null;
}

function afterPrint() {
	isPrinting = false;
	fixImage();
}

</script>
</public:component>
