<%@ include file="../../common/declarations.jspf" %>
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

<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="960.css" />
<div class="container container_16"><!--start container_16-->
<c:if test="${jcr:isNodeType(currentNode, 'jmix:gridPage')}">
    <jcr:nodeProperty node="${currentNode}" name="column" var="column"/>
    <c:choose>
        <c:when test="${column.string == '1col16'}">
            <c:set target="${colMap}" property="col1" value="16"/>
        </c:when>
        <c:when test="${column.string == '2col412'}">
            <c:set target="${colMap}" property="col2" value="4"/>
            <c:set target="${colMap}" property="col1" value="12"/>
        </c:when>
        <c:when test="${column.string == '2col124'}">
            <c:set target="${colMap}" property="col1" value="12"/>
            <c:set target="${colMap}" property="col2" value="4"/>
        </c:when>
        <c:when test="${column.string == '2col106'}">
            <c:set target="${colMap}" property="col1" value="10"/>
            <c:set target="${colMap}" property="col2" value="6"/>
        </c:when>
        <c:when test="${column.string == '2col610'}">
            <c:set target="${colMap}" property="col2" value="6"/>
            <c:set target="${colMap}" property="col1" value="10"/>
        </c:when>
        <c:when test="${column.string == '2col88'}">
            <c:set target="${colMap}" property="col1" value="8"/>
            <c:set target="${colMap}" property="col2" value="8"/>
        </c:when>
        <c:when test="${column.string == '3col448'}">
            <c:set target="${colMap}" property="col3" value="4"/>
            <c:set target="${colMap}" property="col2" value="4"/>
            <c:set target="${colMap}" property="col1" value="8"/>
        </c:when>
        <c:when test="${column.string == '3col466'}">
            <c:set target="${colMap}" property="col3" value="4"/>
            <c:set target="${colMap}" property="col2" value="6"/>
            <c:set target="${colMap}" property="col1" value="6"/>
        </c:when>
        <c:when test="${column.string == '3col484'}">
            <c:set target="${colMap}" property="col3" value="4"/>
            <c:set target="${colMap}" property="col1" value="8"/>
            <c:set target="${colMap}" property="col2" value="4"/>
        </c:when>
        <c:when test="${column.string == '3col664'}">
            <c:set target="${colMap}" property="col1" value="6"/>
            <c:set target="${colMap}" property="col2" value="6"/>
            <c:set target="${colMap}" property="col3" value="4"/>
        </c:when>
        <c:when test="${column.string == '3col844'}">
            <c:set target="${colMap}" property="col1" value="8"/>
            <c:set target="${colMap}" property="col2" value="4"/>
            <c:set target="${colMap}" property="col3" value="4"/>
        </c:when>
        <c:otherwise>
            <c:set target="${colMap}" property="col1" value="10"/>
            <c:set target="${colMap}" property="col2" value="6"/>
        </c:otherwise>
    </c:choose>
</c:if>

<c:forEach items="${colMap}" var="col" varStatus="count">
    <c:choose>
        <c:when test="${col.value > 8}">
            <div class='grid_${col.value}'><!--start grid_${col.value}-->
				<template:area path="${col.key}"/>
				<div class='clear'></div>
            </div>
            <!--stop grid_${col.value}-->
        </c:when>

        <c:otherwise>
            <div class='grid_${col.value}'><!--start grid_${col.value}-->
                <template:area path="${col.key}"/>
                <div class='clear'></div>
            </div>
            <!--stop grid_${col.value}-->
        </c:otherwise>
    </c:choose>
</c:forEach>
    <div class='clear'></div>
</div>
<!--stop container_16-->
