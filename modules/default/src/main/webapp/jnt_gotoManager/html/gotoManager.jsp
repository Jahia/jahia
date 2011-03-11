<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="javascript" resources="jquery.js"/>
<c:if test="${currentResource.workspace eq 'live'}">
    <div id="gotoManager${currentNode.identifier}"/>
    <script type="text/javascript">
        $('#gotoManager${currentNode.identifier}').load('${url.basePreview}${currentNode.path}.html.ajax');
    </script>
    </div>
</c:if>
<c:if test="${currentResource.workspace ne 'live'}">
    <c:if test="${currentNode.properties.type.string eq 'document'}">
        <c:set var="conf" value="filemanager"/>
        <c:set var="label" value="label.filemanager"/>
        <c:set var="icon" value="fileManager"/>
        <c:set var="multisite" value="true"/>
    </c:if>
    <c:if test="${currentNode.properties.type.string eq 'content'}">
        <c:set var="conf" value="editorialcontentmanager"/>
        <c:set var="label" value="label.contentmanager"/>
        <c:set var="icon" value="contentManager"/>
        <c:set var="multisite" value="true"/>
    </c:if>
    <c:if test="${currentNode.properties.type.string eq 'united content'}">
        <c:set var="conf" value="contentmanager"/>
        <c:set var="label" value="label.unitedcontentmanager"/>
        <c:set var="icon" value="contentManager"/>
    </c:if>
    <c:if test="${currentNode.properties.type.string eq 'roles'}">
        <c:set var="conf" value="rolesmanager"/>
        <c:set var="label" value="label.serverroles"/>
        <c:set var="icon" value="roleManager"/>
    </c:if>
    <c:if test="${multisite eq 'true'}">
        <jcr:sql var="result" sql="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites')"/>
        <ul>
            <c:forEach items="${result.nodes}" var="node">
                <jcr:node var="home" path="${node.path}/home"/>
                <c:if test="${jcr:hasPermission(home,'jcr:addChildNodes')}">
                    <li> ${node.properties['jcr:title'].string} <a
                            href="${url.context}/engines/manager.jsp?conf=${conf}&site=${node.identifier}"
                            target="_blank">
                        <c:if test="${!empty currentNode.properties['jcr:title']}">
                            ${currentNode.properties["jcr:title"].string}
                        </c:if>
                        <c:if test="${empty currentNode.properties['jcr:title']}">
                            <img src="${url.context}/icons/${icon}.png" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; ">
                            <fmt:message key="${label}"/>
                        </c:if>
                    </a>
                    </li>
                </c:if>
            </c:forEach>
        </ul>
    </c:if>
    <c:if test="${multisite ne 'true'}">
        <a href="${url.context}/engines/manager.jsp?conf=${conf}" target="_blank">
            <c:if test="${!empty currentNode.properties['jcr:title']}">
                ${currentNode.properties["jcr:title"].string}
            </c:if>
            <c:if test="${empty currentNode.properties['jcr:title']}">
                <img src="${url.context}/icons/${icon}.png" width="16" height="16" alt=" " role="presentation"
                     style="position:relative; top: 4px; margin-right:2px; ">
                <fmt:message key="${label}"/>
            </c:if>
        </a>
    </c:if>
</c:if>
