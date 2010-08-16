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

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:statement" var="query"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:language" var="lang"/>
<jcr:nodeProperty node="${currentNode}" name="maxItems" var="maxItems"/>
<c:choose>
	<c:when test="${lang.string == 'JCR-SQL2'}">
		<query:definition var="listQuery" statement="${query.string}" limit="${maxItems.long}" scope="request"/>
        <c:set target="${moduleMap}" property="listQuery" value="${listQuery}" />
	</c:when>
	<c:when test="${lang.string == 'xpath'}">
		<jcr:xpath var="result" xpath="${query.string}" limit="${maxItems.long}"/>
        <c:set target="${moduleMap}" property="currentList" value="${result.nodes}" />
        <c:set target="${moduleMap}" property="end" value="${functions:length(result.nodes)}" />
        <c:set target="${moduleMap}" property="listTotalSize" value="${moduleMap.end}" />
	</c:when>
	<c:otherwise>
		<utility:logger level="error" value="Unsupported query language encountered: ${lang}"/>
		<% request.setAttribute("currentList", java.util.Collections.EMPTY_LIST); %>
	</c:otherwise>
</c:choose>
<c:set target="${moduleMap}" property="editable" value="false" />

<c:set var="editable" value="false" scope="request"/>
