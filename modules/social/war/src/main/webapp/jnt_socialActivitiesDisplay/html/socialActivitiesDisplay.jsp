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
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>
<jcr:nodeProperty node="${currentNode}" name="j:connectionSource" var="connectionSource"/>
<jcr:nodeProperty node="${currentNode}" name="j:activitiesLimit" var="activitiesLimit"/>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
        <c:if test="${jcr:isNodeType(bindedComponent, 'jnt:user')}">
            <social:get-connections var="connections" path="${bindedComponent.path}"/>
        </c:if>
        <c:if test="${not jcr:isNodeType(bindedComponent, 'jnt:user')}">
            <social:get-acl-connections var="connections" path="${bindedComponent.path}"/>
        </c:if>
        <social:get-activities var="activities" sourcePaths="${connections}" pathFilter="${bindedComponent.path}"/>
        <c:if test="${empty activities}">
            No activities found.
        </c:if>
        <c:if test="${not empty activities}">
            <div class="boxsocial">
                <div class="boxsocialpadding10 boxsocialmarginbottom16">
                    <div class="boxsocial-inner">
                        <div class="boxsocial-inner-border"><!--start boxsocial -->
                            <ul class="activitiesList">
                                <c:forEach items="${activities}" var="activity">
                                    <template:module path="${activity.path}"/>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
</c:if>
<template:linker property="j:bindedComponent"/>