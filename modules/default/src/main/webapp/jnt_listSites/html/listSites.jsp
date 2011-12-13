<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="css" resources="listsites.css"/>
<c:set var="ajaxRequired"
       value="${currentResource.workspace eq 'live' and jcr:hasPermission(currentResource.node, 'jcr:read_default')}"/>
<c:if test="${ajaxRequired}">
    <div id="listsites${currentNode.identifier}">
        <script type="text/javascript">
            $('#listsites${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>');
        </script>
    </div>
</c:if>
<c:if test="${not ajaxRequired}">
    <c:if test="${empty currentNode.properties['typeOfContent'] or currentNode.properties['typeOfContent'].string eq 'website'}">
        <jcr:sql var="result"
                 sql="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites') and localname(site) <> 'systemsite' order by site.[jcr:created] desc"
                 limit="${currentNode.properties['numberMaxOfSitesDisplayed'].string}"/>
    </c:if>
    <c:if test="${not empty currentNode.properties['typeOfContent'] and currentNode.properties['typeOfContent'].string ne 'website'}">
        <jcr:sql var="result"
                 sql="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites') order by site.[jcr:created] desc"
                 limit="${currentNode.properties['numberMaxOfSitesDisplayed'].string}"/>
    </c:if>
    <ul class="list-sites">
        <c:forEach items="${result.nodes}" var="node">
            <c:choose>
                <c:when test="${currentNode.properties.typeOfContent.string eq 'contents'}">
                    <c:set var="page" value="/contents"/>
                </c:when>
                <c:when test="${currentNode.properties.typeOfContent.string eq 'files'}">
                    <c:set var="page" value="/files"/>
                </c:when>
                <c:otherwise>
                    <c:set var="page" value="/${node.home.name}"/>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${not empty node and (jcr:hasPermission(node,'editModeAccess') || jcr:hasPermission(node,'contributeModeAccess'))}">
                    <li class="listsiteicon">${node.displayableName}
                        <c:set var="siteId" value="${node.properties['j:siteId'].long}"/>
                        <c:if test="${currentNode.properties.edit.boolean && jcr:hasPermission(node,'administrationAccess')}">
                            <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='/administration/?do=change&changesite=${siteId}#sites'/>"><fmt:message
                                key="label.administration"/></a>
                        </c:if>
                        <c:set var="baseLive" value="${url.baseLive}"/>
                        <c:set var="basePreview" value="${url.basePreview}"/>
                        <c:set var="baseContribute" value="${url.baseContribute}"/>
                        <c:set var="baseEdit" value="${url.baseEdit}"/>
                        <c:if test="${not fn:contains(node.languages, currentResource.locale)}">
                            <c:set var="localeLength" value="${fn:length(fn:toUpperCase(currentResource.locale))}"/>
                            <c:set var="baseLive"
                                   value="${fn:substring(url.baseLive,-1,fn:length(url.baseLive)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="basePreview"
                                   value="${fn:substring(url.basePreview,-1,fn:length(url.basePreview)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="baseContribute"
                                   value="${fn:substring(url.baseContribute,-1,fn:length(url.baseContribute)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="baseEdit"
                                   value="${fn:substring(url.baseEdit,-1,fn:length(url.baseEdit)-localeLength)}${node.defaultLanguage}"/>
                        </c:if>
                        <c:set var="remotelyPublished" value="${jcr:isNodeType(node,'jmix:remotelyPublished')}"/>
                        <c:if test="${currentNode.properties.edit.boolean && jcr:hasPermission(node,'editModeAccess') && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                            <img src="<c:url value='/icons/editMode.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${baseEdit}${node.path}${page}.html'/>"><fmt:message
                                key="label.editMode"/></a>
                        </c:if>
                        <c:if test="${currentNode.properties.contribute.boolean  && jcr:hasPermission(node,'contributeModeAccess') && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                            <c:url value='/icons/contribute.png' var="icon"/>
                            <c:if test="${currentNode.properties.typeOfContent.string eq 'contents'}">
                                <c:url value='/icons/content-manager-1616.png' var="icon"/>
                            </c:if>
                            <img src="${icon}" width="16" height="16" alt=" " role="presentation"
                                 style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${baseContribute}${node.path}${page}.html'/>"><fmt:message
                                key="label.contribute"/></a>
                        </c:if>
                        <c:if test="${currentNode.properties.preview.boolean && jcr:hasPermission(node,'jcr:read_default') && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                            <img src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${basePreview}${node.path}${page}.html'/>"><fmt:message
                                key="label.preview"/></a>
                        </c:if>
                        <c:if test="${currentNode.properties.live.boolean && (node.home.properties['j:published'].boolean or remotelyPublished)}">
                            <img src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${baseLive}${node.path}${page}.html'/>"><fmt:message
                                key="label.live"/></a>
                        </c:if>
                    </li>
                </c:when>
                <c:otherwise>
                    <c:set var="editModeAccess"
                           value="${jcr:findAllowedNodesForPermission('editModeAccess', node, 'jnt:page')}"/>
                    <c:set var="contributeModeAccess"
                           value="${jcr:findAllowedNodesForPermission('contributeModeAccess', node, 'jnt:page')}"/>
                    <c:set var="previewModeAccess"
                           value="${jcr:findAllowedNodesForPermission('jcr:read_default', node, 'jnt:page')}"/>
                    <c:if test="${node.home.properties['j:published'].boolean or not empty editModeAccess or not empty contributeModeAccess or not empty previewModeAccess}">
                        <li class="listsiteicon">${node.displayableName}
                            <c:set var="baseLive" value="${url.baseLive}"/>
                            <c:set var="basePreview" value="${url.basePreview}"/>
                            <c:set var="baseContribute" value="${url.baseContribute}"/>
                            <c:set var="baseEdit" value="${url.baseEdit}"/>
                            <c:if test="${not fn:contains(node.languages, currentResource.locale)}">
                                <c:set var="localeLength" value="${fn:length(fn:toUpperCase(currentResource.locale))}"/>
                                <c:set var="baseLive"
                                       value="${fn:substring(url.baseLive,-1,fn:length(url.baseLive)-localeLength)}${node.defaultLanguage}"/>
                                <c:set var="basePreview"
                                       value="${fn:substring(url.basePreview,-1,fn:length(url.basePreview)-localeLength)}${node.defaultLanguage}"/>
                                <c:set var="baseContribute"
                                       value="${fn:substring(url.baseContribute,-1,fn:length(url.baseContribute)-localeLength)}${node.defaultLanguage}"/>
                                <c:set var="baseEdit"
                                       value="${fn:substring(url.baseEdit,-1,fn:length(url.baseEdit)-localeLength)}${node.defaultLanguage}"/>
                            </c:if>

                            <c:if test="${not empty editModeAccess && currentNode.properties.contribute.boolean && !renderContext.settings.distantPublicationServerMode}">
                                <img src="<c:url value='/icons/editMode.png'/>" width="16" height="16" alt=" "
                                     role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                    href="<c:url value='${baseEdit}${editModeAccess[0].path}.html'/>"><fmt:message
                                    key="label.editMode"/></a>
                            </c:if>

                            <c:if test="${not empty contributeModeAccess && currentNode.properties.contribute.boolean && !renderContext.settings.distantPublicationServerMode}">
                                <img src="<c:url value='/icons/contribute.png'/>" width="16" height="16" alt=" "
                                     role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                    href="<c:url value='${baseContribute}${contributeModeAccess[0].path}.html'/>"><fmt:message
                                    key="label.contribute"/></a>
                            </c:if>

                            <c:if test="${not empty previewModeAccess && currentNode.properties.preview.boolean && !renderContext.settings.distantPublicationServerMode}">
                                <img src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" "
                                     role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                    href="<c:url value='${basePreview}${previewModeAccess[0].path}.html'/>"><fmt:message
                                    key="label.preview"/></a>
                            </c:if>
                            <c:if test="${currentNode.properties.live.boolean && node.home.properties['j:published'].boolean}">
                                <img src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" "
                                     role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                    href="<c:url value='${baseLive}${node.path}${page}.html'/>"><fmt:message
                                    key="label.live"/></a>
                            </c:if>
                        </li>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </ul>
</c:if>