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

<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<template:addResources type="css" resources="metrics.css"/>
<jcr:nodeProperty node="${currentNode}" name="j:nodeTypeFilter" var="nodeTypeFilter"/>
<jcr:nodeProperty node="${currentNode}" name="j:recommendationLimit" var="recommendationLimit"/>
<c:set var="bindedComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">

<div class="metrics">
<c:catch var="ex">
    <sql:setDataSource driver="com.mysql.jdbc.Driver" url="jdbc:mysql://127.0.0.1:3306/jahia-6.5" user="jahia"
                       password="jahia" var="ds"/>

    <c:if test="${not empty ex and renderContext.editMode}">
        SELECT * FROM objxref WHERE leftpath='${bindedComponent.path}' and rightnodetype='${nodeTypeFilter.string}' order by counter desc limit ${recommendationLimit.long};
    </c:if>

    <sql:query var="refs" dataSource="${ds}">
        SELECT * FROM objxref WHERE leftpath='${bindedComponent.path}' and rightnodetype='${nodeTypeFilter.string}' order by counter desc limit ${recommendationLimit.long};
    </sql:query>
    <p><!--fmt:message key="recommendationsIntro"/-->Users that have viewed this content have also viewed :</p>
    <ol>
    <c:forEach items="${refs.rows}" var="ref">
        <jcr:node path="${ref.rightpath}" var="curNode"/>
        <li><a href="${url.base}${ref.rightpath}"><jcr:nodeProperty node="${curNode}" name="jcr:title"/> (${ref.counter})</a></li>
    </c:forEach>
    </ol>


</c:catch>
<c:if test="${not empty ex and renderContext.editMode}">
    <c:if test = "${ex!=null}">
    The exception is : ${ex}<br><br>
    There is an exception: ${ex.message}<br>
    </c:if>
    <p>For this module to work you need to parse metrics logs of jahia</p>
</c:if>
</div>
</c:if>
<template:linker property="j:bindedComponent"/>