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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="pagecategorizing.css"/>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <div class="categorythispage">
        <jcr:nodeProperty node="${bindedComponent}" name="j:defaultCategory" var="assignedCategories"/>
        <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ' ,')}"/>
        <c:url var="postUrl" value="${url.base}${bindedComponent.path}"/>
        <script type="text/javascript">
            var uuidCategories = "${bindedComponent.identifier}";
            var uuids = new Array();
            <c:forEach items="${assignedCategories}" var="category" varStatus="status">
            <c:if test="${not empty category.node}">
            uuids.push("${category.node.identifier}");
            </c:if>
            </c:forEach>

            function deleteCategory(uuid) {
                $.ajaxSetup({traditional: true, cache:false});
                var newUuids = new Array();
                for (i = 0; i < uuids.length; i++) {
                    if (uuids[i] != uuid) {
                        newUuids.push(uuids[i])
                    }
                }
                uuids = newUuids;
                if (uuids.length == 0) {
                    $.post("${postUrl}", {"jcrMethodToCall":"put","jcrRemoveMixin":"jmix:categorized"}, function(result) {
                        $("#category" + uuid).hide();
                        if (uuids.length == 0) {
                            var spanNoYetCat = $('<span><fmt:message key="label.categories.noCategory"/></span>').attr('class', 'nocategorizeditem' + uuidCategories);
                            $("#jahia-categories-" + uuidCategories).append(spanNoYetCat)
                        }
                    }, "json");
                } else {
                    $.post("${postUrl}", {"j:defaultCategory":uuids,"jcrMethodToCall":"put","jcr:mixinTypes":"jmix:categorized"}, function(result) {
                        $("#category" + uuid).hide();
                    }, "json");
                }
                return false;
            }

        </script>
        <jsp:useBean id="filteredCategories" class="java.util.LinkedHashMap"/>
        <c:forEach items="${assignedCategories}" var="category" varStatus="status">
            <c:if test="${not empty category.node}">
                <c:set target="${filteredCategories}" property="${category.node.identifier}"
                       value="${category.node.properties['jcr:title'].string}"/>
            </c:if>
        </c:forEach>
        <div class="categorized">
            <span><fmt:message key="label.categories"/>:</span>
            <span id="jahia-categories-${bindedComponent.identifier}">
                <c:choose>
                    <c:when test="${not empty filteredCategories}">
                        <c:forEach items="${filteredCategories}" var="category" varStatus="status">
                            <div id="category${category.key}" style="display:inline">
                                    ${!status.first ? separator : ''}<span
                                    class="categorizeditem">${fn:escapeXml(category.value)}</span>
                                <a class="delete" onclick="deleteCategory('${category.key}')" href="#"></a>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <span class="nocategorizeditem${bindedComponent.identifier}"><fmt:message
                                key="label.categories.noCategory"/></span>
                    </c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>
</c:if>