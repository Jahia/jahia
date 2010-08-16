<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="css" resources="metrics.css"/>
<div class="metrics">
<c:catch var="ex">
    <sql:setDataSource driver="com.mysql.jdbc.Driver" url="jdbc:mysql://127.0.0.1:3306/jahia-6.5" user="jahia"
                       password="jahia" var="ds"/>

    <sql:query var="hits" dataSource="${ds}">
        select count(id) as hits,url from pageviewed where day(ts) = day(now())-1 group by url order by hits desc limit 10
    </sql:query>
    <p>Top Ten Pages for the last 24 hours</p>
    <ol>
    <c:forEach items="${hits.rows}" var="hit">
        <jcr:node path="${hit.url}" var="pageNode"/>
        <li><a href="${url.base}${hit.url}"><jcr:nodeProperty node="${pageNode}" name="jcr:title"/></a></li>
    </c:forEach>
    </ol>

</c:catch>
<c:if test = "${ex!=null}">
The exception is : ${ex}<br><br>
There is an exception: ${ex.message}<br>
</c:if>
<c:if test="${not empty ex and renderContext.editMode}">
    <p>For making this module work you need to parse metrics logs of jahia</p>
</c:if>
</div>