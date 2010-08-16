<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jcr:nodeProperty node="${currentNode}" name="flashSource" var="flashSource"/>
<jcr:nodeProperty node="${currentNode}" name="width" var="widthFlash"/>
<jcr:nodeProperty node="${currentNode}" name="height" var="heightFlash"/>
<jcr:nodeProperty node="${currentNode}" name="flashPlayer" var="flashPlayer"/>
<jcr:nodeProperty node="${currentNode}" name="id" var="idFlash"/>
<jcr:nodeProperty node="${currentNode}" name="name" var="nameFlash"/>
<jcr:nodeProperty node="${currentNode}" name="swliveconnect" var="swliveconnectFlash"/>
<jcr:nodeProperty node="${currentNode}" name="play" var="playFlash"/>
<jcr:nodeProperty node="${currentNode}" name="loop" var="loopFlash"/>
<jcr:nodeProperty node="${currentNode}" name="menu" var="menuFlash"/>
<jcr:nodeProperty node="${currentNode}" name="quality" var="qualityFlash"/>
<jcr:nodeProperty node="${currentNode}" name="scale" var="scaleFlash"/>
<jcr:nodeProperty node="${currentNode}" name="align" var="alignFlash"/>
<jcr:nodeProperty node="${currentNode}" name="salign" var="salignFlash"/>
<jcr:nodeProperty node="${currentNode}" name="wmode" var="wmodeFlash"/>
<jcr:nodeProperty node="${currentNode}" name="bgcolor" var="bgcolorFlash"/>
<jcr:nodeProperty node="${currentNode}" name="base" var="baseFlash"/>
<jcr:nodeProperty node="${currentNode}" name="flashvars" var="flashvarsFlash"/>

<template:addResources type="javascript" resources="swfobject.js"/>
<template:addResources type="css" resources="flash.css"/>
	    <div id="flashcontent${currentNode.UUID}">
            <div class="flashcontent"><!--START FLASH -->
                <strong>You need to upgrade your Flash Player</strong><br />
                <a href="http://www.adobe.com/go/getflashplayer" target="_blank"><img src="${url.currentModule}/images/160x41_Get_Flash_Player.jpg" alt="get flash player" /></a>
			</div>
	    </div>
        <script type="text/javascript">
            var so = new SWFObject("${flashSource.node.url}", "${nameFlash.string}", "${widthFlash.long}", "${heightFlash.long}", "${flashPlayer.string}", "${bgcolorFlash.string}");
                    <c:if test="${not empty wmodeFlash.string}">
                          so.addParam("wmode", "${wmodeFlash.string}");
                    </c:if>
                    <c:if test="${not empty idFlash.string}">
                          so.addParam("id", "${idFlash.string}");
                    </c:if>
                    <c:if test="${not empty swliveconnectFlash.string}">
                          so.addParam("swliveconnect", "${swliveconnectFlash.string}");
                    </c:if>
                    <c:if test="${not empty playFlash.string}">
                          so.addParam("wmode", "${playFlash.string}");
                    </c:if>
                    <c:if test="${not empty menuFlash.string}">
                          so.addParam("menu", "${menuFlash.string}");
                    </c:if>
                    <c:if test="${not empty scaleFlash.string}">
                          so.addParam("scale", "${scaleFlash.string}");
                    </c:if>
                    <c:if test="${not empty qualityFlash.string}">
                          so.addParam("quality", "${qualityFlash.string}");
                    </c:if>
                    <c:if test="${not empty alignFlash.string}">
                          so.addParam("align", "${alignFlash.string}");
                    </c:if>
                    <c:if test="${not empty salignFlash.string}">
                          so.addParam("salign", "${salignFlash.string}");
                    </c:if>
                    <c:if test="${not empty baseFlash.string}">
                          so.addParam("base", "${baseFlash.string}");
                    </c:if>
                    <c:if test="${not empty flashvarsFlash.string}">
                          so.addParam("flashvars", "${flashvarsFlash.string}");
                    </c:if>
                  so.write("flashcontent${currentNode.UUID}");</script>