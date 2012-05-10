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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="listsites.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="javascript" resources="managesites.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<c:set var="ajaxRequired"
       value="${currentResource.workspace eq 'live' and jcr:hasPermission(currentResource.node, 'jcr:read_default')}"/>
<c:if test="${ajaxRequired}">
    <div id="listsites${currentNode.identifier}">
        <script type="text/javascript">
            $('#listsites${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax?includeJavascripts=true"/>');
        </script>
    </div>
</c:if>

<c:if test="${not ajaxRequired}">
    <template:addResources type="css" resources="listsites.css"/>
    <template:include view="hidden.header"/>

    <ul class="list-sites">
        <c:forEach items="${moduleMap.currentList}" var="node" begin="${moduleMap.begin}" end="${moduleMap.end}">
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
                    <li class="listsiteicon">
                        <c:if test="${(currentNode.properties.export.boolean or currentNode.properties.delete.boolean) && jcr:hasPermission(node,'adminVirtualSites')}">
                            <input class="sitecheckbox" type="checkbox" name="${node.name}" />
                        </c:if>
                        ${node.displayableName}
                        <c:set var="siteId" value="${node.properties['j:siteId'].long}"/>
                        <c:if test="${currentNode.properties.administrationlink.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                            <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='/administration/?do=change&changesite=${siteId}#sites'/>"><fmt:message
                                key="label.administration"/></a>
                        </c:if>
                        <c:set var="baseLive" value="${url.baseLive}"/>
                        <c:set var="basePreview" value="${url.basePreview}"/>
                        <c:set var="baseContribute" value="${url.baseContribute}"/>
                        <c:set var="baseLightEdit" value="/cms/lightedit/default/${currentResource.locale}"/>
                        <c:set var="baseEdit" value="${url.baseEdit}"/>
                        <c:if test="${not fn:contains(node.languages, currentResource.locale)}">
                            <c:set var="localeLength" value="${fn:length(fn:toUpperCase(currentResource.locale))}"/>
                            <c:set var="baseLive"
                                   value="${fn:substring(url.baseLive,-1,fn:length(url.baseLive)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="basePreview"
                                   value="${fn:substring(url.basePreview,-1,fn:length(url.basePreview)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="baseContribute"
                                   value="${fn:substring(url.baseContribute,-1,fn:length(url.baseContribute)-localeLength)}${node.defaultLanguage}"/>
                            <c:set var="baseLightEdit"
                                   value="${fn:substring(baseLightEdit,-1,fn:length(baseLightEdit)-localeLength)}${node.defaultLanguage}"/>
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
                        <c:if test="${currentNode.properties.lightedit.boolean  && jcr:hasPermission(node,'contributeModeAccess') && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                            <img src="<c:url value='/icons/lightedit.png'/>" width="16" height="16" alt=" " role="presentation"
                                 style="position:relative; top: 4px; margin-right:2px; "><a
                                href="<c:url value='${baseLightEdit}${node.path}${page}.html'/>"><fmt:message
                                key="label.lightedit"/></a>
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
                        <c:if test="${currentNode.properties.editproperties.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                            <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                                 role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                href="#" class="changePropertiesButton" id="changePropertiesButton${node.identifier}" onclick="$('#editSiteDiv${node.identifier}').slideToggle()"><fmt:message key="label.manageSite.changeProperties"/></a>
                        </c:if>

                        <jsp:useBean id="nowDate" class="java.util.Date" />
                        <fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>

                        <c:if test="${currentNode.properties.editproperties.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                                <div id="editSiteDiv${node.identifier}" style="display:none">
                                    <form class="editSiteForm ajaxForm" id="editSiteForm${node.identifier}" action="<c:url value='${url.base}${node.path}.adminEditSite.do'/>" >

                                        <fieldset>
                                            <legend><fmt:message key="label.manageSite.siteProperties"/></legend>
                                            <h3><fmt:message key="label.manageSite.siteProperties"/></h3>

                                            <p id="siteTitleForm${node.identifier}">
                                                <label for="siteTitle${node.identifier}"><fmt:message key="jnt_virtualsite.j_title"/></label>
                                                <input type="text" name="siteTitle" id="siteTitle${node.identifier}" value="${node.properties['j:title'].string}"/>
                                            </p>

                                            <p id="siteServerNameForm${node.identifier}">
                                                <label for="siteServerName${node.identifier}"><fmt:message key="jnt_virtualsite.j_serverName"/></label>
                                                <input type="text" name="siteServerName" id="siteServerName${node.identifier}" value="${node.properties['j:serverName'].string}"/>
                                            </p>

                                            <p id="siteDescrForm${node.identifier}">
                                                <label for="siteDescr${node.identifier}"><fmt:message key="jnt_virtualsite.j_description"/></label>
                                                <textarea type="text" name="siteDescr" id="siteDescr${node.identifier}">${node.properties['j:description'].string}</textarea>
                                            </p>
                                        </fieldset>
                                    </form>
                                    <button site="${node.identifier}" onclick="editProperties('${node.identifier}')"><fmt:message key="label.manageSite.submitChanges"/></button>
                                </div>
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
                            <c:set var="baseLightEdit" value="/cms/lightedit/default/${currentResource.locale}"/>
                            <c:set var="baseEdit" value="${url.baseEdit}"/>
                            <c:if test="${not fn:contains(node.languages, currentResource.locale)}">
                                <c:set var="localeLength" value="${fn:length(fn:toUpperCase(currentResource.locale))}"/>
                                <c:set var="baseLive"
                                       value="${fn:substring(url.baseLive,-1,fn:length(url.baseLive)-localeLength)}${node.defaultLanguage}"/>
                                <c:set var="basePreview"
                                       value="${fn:substring(url.basePreview,-1,fn:length(url.basePreview)-localeLength)}${node.defaultLanguage}"/>
                                <c:set var="baseContribute"
                                       value="${fn:substring(url.baseContribute,-1,fn:length(url.baseContribute)-localeLength)}${node.defaultLanguage}"/>
                                <c:set var="baseLightEdit"
                                       value="${fn:substring(baseLightEdit,-1,fn:length(baseLightEdit)-localeLength)}${node.defaultLanguage}"/>
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

                            <c:if test="${not empty contributeModeAccess && currentNode.properties.lightedit.boolean && !renderContext.settings.distantPublicationServerMode}">
                                <img src="<c:url value='/icons/lightedit.png'/>" width="16" height="16" alt=" "
                                     role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a
                                    href="<c:url value='${baseLightEdit}${contributeModeAccess[0].path}.html'/>"><fmt:message
                                    key="label.lightedit"/></a>
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

    <div style="display:none">
        <div id="dialog-delete-confirm" title=" ">
            <p><span class="ui-icon ui-icon-alert"
                     style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.delete.confirm" /></p>
        </div>
        <div id="nothing-selected" >
            <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.manageSites.noSiteSelected"/></p>
        </div>
    </div>
    <div style="display:none; position:fixed; left:0; top:0; width:100%; height:100%; z-index:9999" id="loading">
        <h1><fmt:message key="org.jahia.admin.workInProgressTitle"/></h1>
    </div>

    <jcr:node var="root" path="/"/>
    <c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <form class="deleteSiteForm ajaxForm" id="deleteSiteForm" action="<c:url value='${url.base}/sites.adminDeleteSite.do'/>" >
        </form>
    </c:if>
    <c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <form class="exportForm ajaxForm"  name="export" id="exportForm" method="POST">
            <input type="hidden" name="exportformat" value="site"/>
            <input type="hidden" name="live" value="true"/>
        </form>
    </c:if>

    <c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <button class="deleteSiteButton" id="deleteSiteButton" onclick="deleteSite()"><fmt:message key="label.manageSite.deleteSite"/></button>
    </c:if>
    <c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <c:url var="stagingExportUrl" value="${renderContext.request.contextPath}/cms/export/default/sites_staging_export_${now}.zip"/>
        <button class="exportStagingButton" id="exportStagingButton" onclick="exportSite('${stagingExportUrl}',false)"><fmt:message key="label.manageSite.exportStaging"/></button>
        <c:url var="exportUrl" value="${renderContext.request.contextPath}/cms/export/default/sites_export_${now}.zip"/>
        <button class="exportLiveButton" id="exportLiveButton" onclick="exportSite('${exportUrl}',true)"><fmt:message key="label.manageSite.exportLive"/></button>
    </c:if>


    <template:include view="hidden.footer"/>
</c:if>