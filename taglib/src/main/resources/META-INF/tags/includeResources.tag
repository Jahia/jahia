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
<%@ tag body-content="empty" description="Includes external resources (CSS, JavaScript, inline CSS styles, inline JavaScript), which are required for current page and were registered using addResources tag." %>
<%@ attribute name="types" required="false" type="java.lang.String" description="Comma-separated list of resource types to include. [css,inlinecss,javascript,inlinejavascript]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:forTokens items="${fn:toLowerCase(functions:default(types, 'css,inlinecss,javascript,inlinejavascript'))}" delims="," var="resourceType">
	<c:set var="resources" value="${renderContext.staticAssets[resourceType]}"/>
	<c:if test="${not empty resources && 'inlinecss' == resourceType}">
	<style type="text/css">
	/* <![CDATA[ */
	</c:if>
	<c:if test="${not empty resources && 'inlinejavascript' == resourceType}">
	<script type="text/javascript">
	/* <![CDATA[ */
	</c:if>
	<c:forEach var="resource" items="${resources}">
		<c:choose>
			<c:when test="${'css' == resourceType}">
				<link rel="stylesheet" href="${resource}" media="screen" type="text/css"/>
			</c:when>
			<c:when test="${'javascript' == resourceType}">
				<script type="text/javascript" src="${resource}"></script>
			</c:when>
			<c:when test="${'inlinecss' == resourceType || 'inlinejavascript' == resourceType}">
				${resource}
			</c:when>
		</c:choose>
	</c:forEach>
	<c:if test="${not empty resources && 'inlinecss' == resourceType}">
	/* ]]> */
	</style>
	</c:if>
	<c:if test="${not empty resources && 'inlinejavascript' == resourceType}">
	/* ]]> */
	</script>
	</c:if>
</c:forTokens>