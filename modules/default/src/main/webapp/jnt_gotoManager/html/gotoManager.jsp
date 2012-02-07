<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="css" resources="gotomanager.css"/>
<c:set var="gotoType" value="${currentNode.propertiesAsString.type}"/>
<c:if test="${!renderContext.settings.distantPublicationServerMode}">
<c:if test="${gotoType != 'roles' || renderContext.enterpriseEdition}">
    <c:if test="${currentResource.workspace eq 'live'}">
        <template:addResources type="javascript" resources="jquery.min.js"/>
        <template:addResources type="css" resources="goto-links.css"/>
        <div id="gotoManager${currentNode.identifier}"/>
        <script type="text/javascript">
            $('#gotoManager${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>');
        </script>
        </div>
    </c:if>
    <c:if test="${currentResource.workspace ne 'live'}">
        <c:if test="${gotoType eq 'document'}">
            <c:set var="conf" value="filemanager"/>
            <c:set var="requiredPermission" value="fileManager"/>
            <c:set var="label" value="label.filemanager"/>
            <c:set var="icon" value="treepanel-files-manager-1616"/>
            <c:set var="multisite" value="true"/>
        </c:if>
        <c:if test="${gotoType eq 'content'}">
            <c:set var="conf" value="editorialcontentmanager"/>
            <c:set var="requiredPermission" value="editorialContentManager"/>
            <c:set var="label" value="label.contentmanager"/>
            <c:set var="icon" value="treepanel-content-manager-1616"/>
            <c:set var="multisite" value="true"/>
        </c:if>
        <c:if test="${gotoType eq 'unitedContent'}">
            <c:set var="conf" value="repositoryexplorer"/>
            <c:set var="requiredPermission" value="repositoryExplorer"/>
            <c:set var="label" value="label.repositoryexplorer"/>
            <c:set var="icon" value="repositoryExplorer"/>
        </c:if>
        <c:if test="${gotoType eq 'roles'}">
            <c:set var="conf" value="rolesmanager"/>
            <c:set var="requiredPermission" value="rolesManager"/>
            <c:set var="label" value="label.serverroles"/>
            <c:set var="icon" value="roleManager"/>
        </c:if>
        <c:if test="${multisite}">
            <jcr:sql var="result" sql="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites')"/>
            <ul class="gotomanager">
                <c:forEach items="${result.nodes}" var="node">
                    <jcr:node var="home" path="${node.home.path}"/>
                    <c:if test="${jcr:hasPermission(home,requiredPermission)}">
                        <li><img src="${url.context}/icons/${icon}.png" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; " />${fn:escapeXml(node.displayableName)}&nbsp;<a
                                href="${url.context}/engines/manager.jsp?conf=${conf}&site=${node.identifier}"
                                target="_blank">
                            <c:if test="${!empty currentNode.properties['jcr:title']}">
                                ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
                            </c:if>
                            <c:if test="${empty currentNode.properties['jcr:title']}">

                                <fmt:message key="${label}"/>
                            </c:if>
                        </a>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </c:if>
        <c:if test="${!multisite && jcr:hasPermission(currentNode,requiredPermission)}">
            <img src="${url.context}/icons/${icon}.png" width="16" height="16" alt=" " role="presentation"
                 style="position:relative; top: 4px; margin-right:2px; " /><a href="${url.context}/engines/manager.jsp?conf=${conf}&site=${renderContext.site.identifier}" target="_blank">
            <c:if test="${!empty currentNode.properties['jcr:title']}">
                ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
            </c:if>
            <c:if test="${empty currentNode.properties['jcr:title']}">
                <fmt:message key="${label}"/>
            </c:if>
        </a>
        </c:if>
    </c:if>
</c:if>
</c:if>