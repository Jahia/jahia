<%@ include file="../../common/declarations.jspf" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="960.css" />

<jcr:nodeProperty node="${currentNode}" name="customColumn" var="customColumn"/>
<c:set var="nbCols" value="0"/>
<c:set var="nbArea" value="0"/>

<c:forTokens items="${customColumn.string}" delims="," varStatus="vs" var="col">
    <c:set target="${colMap}" property="col${vs.count}" value="${col}"/>
    <c:if test="${fn:contains(col,' ')}">
        <c:forTokens items="${col}" delims=" " varStatus="vs" var="c">
            <c:if test="${vs.count eq 1}">
                <c:set var="col" value="${c}"/>
            </c:if>
        </c:forTokens>
    </c:if>
    <c:set var="nbCols" value="${nbCols + col}"/>
    <c:set var="nbAreas" value="${nbAreas + 1}"/>
</c:forTokens>
<c:set var="nbNames" value="0"/>
<c:forTokens items="${currentNode.properties.colNames.string}" delims="," varStatus="vs">

    <c:set var="nbNames" value="${nbNames + 1}"/>
</c:forTokens>

<c:if test="${!empty currentNode.properties.divID}"> <div id="${currentNode.properties.divID.string}"></c:if>
<div class="container_16">
    <c:if test="${!empty currentNode.properties.divClass}"><div class="${currentNode.properties.divClass.string}"></c:if>

    <c:if test="${editableModule}">
        <div class="grid_${nbCols}">
            <p>${jcr:label(currentNode.primaryNodeType,currentResource.locale)} ${currentNode.name} : ${column.string}</p>
            <c:if test="${nbNames != nbAreas}">
                <p><fmt:message key="label.generatedNames"/></p>
            </c:if>
        </div>
        <div class='clear'></div>
    </c:if>
    <c:set var="colNames" value="${fn:split(currentNode.properties.colNames.string, ',')}"/>

    <c:forEach items="${colMap}" var="col" varStatus="count">
        <c:set var="column" value="${col.value}"/>
        <c:set var="colCss" value=""/>
        <c:if test="${fn:contains(column,' ')}">
            <c:forTokens items="${column}" delims=" " varStatus="vs" var="c">
                <c:if test="${vs.count eq 1}">
                    <c:set var="column" value="${c}"/>
                </c:if>
                <c:if test="${!(vs.count eq 1)}">
                    <c:set var="colCss" value="${colCss} ${c}"/>
                </c:if>
            </c:forTokens>
        </c:if>
        <!--start grid_${column}-->
        <div class='grid_${column} ${colCss}'>
            <c:if test="${nbNames == nbAreas}">
                <c:forTokens items="${currentNode.properties.colNames.string}" var="colName" delims="," varStatus="vs1">
                    <c:if test="${count.count == vs1.count}">
                        <template:area path="${colName}"/>
                    </c:if>
                </c:forTokens>
            </c:if>
            <c:if test="${nbNames != nbAreas}">
                <template:area path="${currentNode.name}-${col.key}"/>
            </c:if>
            <div class='clear'></div>
        </div>
        <!--stop grid_${column}-->
    </c:forEach>
    <div class='clear'></div>
    <c:if test="${!empty currentNode.properties.divClass}"></div></c:if>
</div>
<c:if test="${!empty currentNode.properties.divID}"></div></c:if>

