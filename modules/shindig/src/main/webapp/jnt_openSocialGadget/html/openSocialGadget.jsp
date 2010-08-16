<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="gadgetUrl" value="${currentNode.propertiesAsString['j:url']}"/>
<c:if test="${not renderContext.editMode}">
<c:if test="${empty requestScope['org.jahia.modules.shindig.containerResourcesIncluded']}">
    <c:set var="org.jahia.modules.shindig.containerResourcesIncluded" value="true" scope="request"/>
    <c:set var="org.jahia.modules.shindig.gadgetIndex" value="0" scope="request"/>
    <c:set var="base" value="${pageContext.request.contextPath}/modules/shindig/gadgets/files/container"/>
    <template:addResources type="css" resources="${base}/gadgets.css"/>
    <template:addResources type="javascript" resources="${pageContext.request.contextPath}/modules/shindig/gadgets/js/rpc.js?c=1&debug=1"/>
    <template:addResources type="javascript" resources="${base}/cookies.js"/>
    <template:addResources type="javascript" resources="${base}/util.js"/>
    <template:addResources type="javascript" resources="${base}/gadgets.js"/>
    <template:addResources type="javascript" resources="${base}/cookiebaseduserprefstore.js"/>
    <template:addResources type="javascript" resources="jquery.min.js"/>

    <template:addResources type="inlinejavascript">
        var jahiaGadgetUrls = new Array();
        
        $(document).ready(function () {
            var ids = new Array();
            var myGadgets = new Array();
            for (var i = 0; i < jahiaGadgetUrls.length; i++) {
                var gd = gadgets.container.createGadget({specUrl: jahiaGadgetUrls[i]});
                gd.setServerBase('${pageContext.request.contextPath}/modules/shindig/gadgets/');
                myGadgets.push(gd);
                gadgets.container.addGadget(gd);
                ids.push('gadget-chrome-' + i);
            }
            gadgets.container.layoutManager.setGadgetChromeIds(ids);
        
            for (var i = 0; i < myGadgets.length; i++) {
                gadgets.container.renderGadget(myGadgets[i]);
            }
        });
    </template:addResources>
</c:if>
<template:addResources type="inlinejavascript">
    jahiaGadgetUrls.push("${gadgetUrl}");
</template:addResources>
</c:if>

<div id="gadget-chrome-${requestScope['org.jahia.modules.shindig.gadgetIndex']}" class="gadgets-gadget-chrome${renderContext.editMode ? ' x-panel-linker' : ''}">
<c:if test="${renderContext.editMode}">
    <h4><fmt:message key="jnt_openSocialGadget"/>&nbsp;${fn:escapeXml(functions:default(currentNode.propertiesAsString['jcr:title'], currentNode.name))}</h4>
    <p><strong><fmt:message key="jnt_openSocialGadget.j_url"/></strong>:&nbsp;${fn:escapeXml(gadgetUrl)}</p>
</c:if>
</div>

<c:set var="org.jahia.modules.shindig.gadgetIndex" value="${requestScope['org.jahia.modules.shindig.gadgetIndex'] + 1}" scope="request"/>