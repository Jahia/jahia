<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.js"/>
<template:include view="hidden.header"/>
<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>
<c:if test="${empty param.letter}">
    <c:set value="A" var="selectedLetter"/>
</c:if>
<c:if test="${!empty param.letter}">
    <c:set var="selectedLetter" value="${param.letter}"/>
</c:if>
<c:if test="${currentNode.properties.useMainResource.boolean}">
    <c:set var="glossaryPath" value="${renderContext.mainResource.node.path}"/>
</c:if>
<c:if test="${!currentNode.properties.useMainResource.boolean}">
    <c:set var="glossaryPath" value="${currentNode.path}"/>
</c:if>
<c:if test="${!empty param.glossaryPath}">
    <c:set var="glossaryPath" value="${param.glossaryPath}"/>
</c:if>

<div id="${currentNode.UUID}">
    <template:addResources type="javascript" resources="ajaxreplace.js" />
    <div class="alphabeticalMenu"><!--start alphabeticalMenu-->
        <div class="alphabeticalNavigation">
            <c:forTokens items="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z" var="letter" delims=",">
                <c:url var="myUrl" value="${url.current}.ajax">
                    <c:param name="letter" value="${letter}"/>
                    <c:param name="glossaryPath" value="${glossaryPath}"/>
                </c:url>
                <span><a class="alphabeticalLetter <c:if test='${letter eq selectedLetter}'>current</c:if>" href="javascript:replace('${currentNode.UUID}','${myUrl}')" >${letter}</a></span>
            </c:forTokens>
        </div>
        <div class='clear'></div>
    </div>
    <h3>${selectedLetter}</h3>
    <ul>
        <c:if test="${!empty currentNode.properties.field.string}">
        <jcr:sql var="list"
                 sql="select * from [jnt:content] as content  where
              (content.['${currentNode.properties.field.string}'] like '${fn:toLowerCase(selectedLetter)}%' or
              content.['${currentNode.properties.field.string}'] like '${fn:toUpperCase(selectedLetter)}%') and
               isdescendantnode(content, ['${glossaryPath}'])
               order by content.['${currentNode.properties.field.string}']"/>

        <c:forEach items="${list.nodes}" var="subchild">
            <li>
                <template:module node="${subchild}" view="${currentNode.properties['j:subNodesView'].string}"  editable="${moduleMap.editable}" />
            </li>
        </c:forEach>
        </c:if>
    </ul>
</div>
<div class='clear'></div>
