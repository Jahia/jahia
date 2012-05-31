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

<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="javascript" resources="managesites.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="css" resources="loading.css"/>
<template:addResources type="css" resources="edit-site-form.css"/>
<template:include view="hidden.header"/>

<script type="text/javascript">
    $(document).ready(function() {
        $("a.changePropertiesButton").fancybox();
        $("a.detailsButton").fancybox({
            margin : 50,
            scrolling : 'auto',
            width : 600,
            height : 400,
            autoDimensions : false,
            type : 'ajax'
        });
        $(".checkAll").click(function () {
            $(".sitecheckbox").each(function (index) {
                if ($(".checkAll").attr("checked") == "checked") {
                    $(this).attr("checked","checked");
                } else {
                    $(this).removeAttr("checked");
                }
            });
        });
        $("a.groupMngmtButton").click(function(){
            $.ajax({
                url:"${url.context}/administration",
                data:{do:'change',changesite:$(this).attr('siteid')},
                async:false,
                type:"POST"
            });
        });
    });
</script>

<jcr:node var="root" path="/"/>
<c:if test="${moduleMap.end > 0 and moduleMap.end > moduleMap.begin}">
    <c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <button class="deleteSiteButton" id="deleteSiteButton" onclick="deleteSite()"><fmt:message key="label.manageSite.deleteSite"/></button>
    </c:if>
    <c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
        <c:url var="stagingExportUrl" value="${renderContext.request.contextPath}/cms/export/default/sites_staging_export_${now}.zip"/>
        <button class="exportStagingButton" id="exportStagingButton" onclick="exportSite('${stagingExportUrl}',false)"><fmt:message key="label.manageSite.exportStaging"/></button>
        <c:url var="exportUrl" value="${renderContext.request.contextPath}/cms/export/default/sites_export_${now}.zip"/>
        <button class="exportLiveButton" id="exportLiveButton" onclick="exportSite('${exportUrl}',true)"><fmt:message key="label.manageSite.exportLive"/></button>
    </c:if>
</c:if>

<table width="100%" class="table list-sites" summary="Site List Table" id="siteListTable">
<caption class=" hidden">
</caption>
<colgroup>
    <c:if test="${(currentNode.properties.export.boolean or currentNode.properties.delete.boolean)}">
        <col span="1" width="5%" class="col1"/>
    </c:if>
    <col span="1" width="35%" class="col2"/>
    <col span="1" width="15%" class="col3"/>
    <col span="1" width="25%" class="col4"/>
    <col span="1" width="20%" class="col5"/>
</colgroup>
<thead>
<tr>
    <c:if test="${(currentNode.properties.export.boolean or currentNode.properties.delete.boolean)}">
        <th class="center" id="Select" scope="col"><input type="checkbox" class="allFileCheckbox checkAll"/></th>
    </c:if>
    <th id="Title" scope="col"><fmt:message key="label.title"/></th>
    <th id="Key" scope="col"><fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/></th>
    <th id="ServerName" scope="col"><fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/></th>
    <th id="Actions" scope="col"><fmt:message key="label.actions"/></th>
</tr>
</thead>

<tbody>
<c:forEach items="${moduleMap.currentList}" var="node" begin="${moduleMap.begin}" end="${moduleMap.end}">
    <tr class="odd">
        <c:if test="${(currentNode.properties.export.boolean or currentNode.properties.delete.boolean)}">
            <td class="center" headers="Select">
                <c:if test="${jcr:hasPermission(node,'adminVirtualSites')}">
                    <input class="sitecheckbox" type="checkbox" name="${node.name}" />
                </c:if>
            </td>
        </c:if>
        <td headers="Title">${node.displayableName}</td>
        <td headers="Key">${node.name}</td>
        <td headers="ServerName">${node.properties['j:serverName'].string}</td>
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
                <td headers="Actions">
                    <c:set var="siteId" value="${node.properties['j:siteId'].long}"/>
                    <c:if test="${currentNode.properties.administrationlink.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                        <a href="<c:url value='/administration/?do=change&changesite=${siteId}#sites'/>"
                           title="<fmt:message key='label.administration'/>"><img
                                src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                                role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
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
                        <a href="<c:url value='${baseEdit}${node.path}${page}.html'/>"
                           title="<fmt:message key='label.editMode'/>"><img
                                src="<c:url value='/icons/editMode.png'/>" width="16" height="16" alt=" "
                                role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.contribute.boolean  && jcr:hasPermission(node,'contributeModeAccess') && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                        <c:url value='/icons/contribute.png' var="icon"/>
                        <c:if test="${currentNode.properties.typeOfContent.string eq 'contents'}">
                            <c:url value='/icons/content-manager-1616.png' var="icon"/>
                        </c:if>
                        <a href="<c:url value='${baseContribute}${node.path}${page}.html'/>"
                           title="<fmt:message key='label.contribute'/>"><img
                                src="${icon}" width="16" height="16" alt=" " role="presentation"
                                style="position:relative; top: 4px; margin-right:2px; "/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.preview.boolean && jcr:hasPermission(node,'jcr:read_default') && !renderContext.settings.distantPublicationServerMode && not remotelyPublished}">
                        <a href="<c:url value='${basePreview}${node.path}${page}.html'/>"
                           title="<fmt:message key='label.preview'/>"><img
                                src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" "
                                role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.live.boolean && (node.home.properties['j:published'].boolean or remotelyPublished)}">
                        <a href="<c:url value='${baseLive}${node.path}${page}.html'/>"
                           title="<fmt:message key='label.live'/>"><img
                                src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" "
                                role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.editproperties.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                        <a href="#editSiteDiv${node.identifier}" class="changePropertiesButton" id="changePropertiesButton${node.identifier}"
                           title="<fmt:message key='label.manageSite.changeProperties'/>"><img
                                src="<c:url value='/icons/changeProperties.png'/>" width="16" height="16" alt=" "
                                role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                    </c:if>
                    <c:if test="${currentNode.properties.details.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                        <a href="<c:url value='${basePreview}${node.path}${page}.${currentNode.properties.detailsTemplate.string}.html'/>"
                           class="detailsButton" id="detailsButton${node.identifier}"
                           title="${currentNode.properties.detailsLabel.string}"><img
                                src="<c:url value='/icons/administrator.png'/>" width="16" height="16" alt=" "
                                role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                    </c:if>
                    <c:if test="${jcr:hasPermission(node,'adminVirtualSites')}">
                        <a href="${url.context}/administration?do=groups&sub=display"
                           class="groupMngmtButton" id="groupButton${node.identifier}" siteid="${siteId}"
                           title="go to group administration"><img
                                src="<c:url value='/css/images/andromeda/icons/group_edit.png'/>" width="16" height="16" alt=" "
                                role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                    </c:if>

                    <jsp:useBean id="nowDate" class="java.util.Date" />
                    <fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>

                    <c:if test="${currentNode.properties.editproperties.boolean && jcr:hasPermission(node,'adminVirtualSites')}">
                        <div style="display:none">
                            <div id="editSiteDiv${node.identifier}" class="popupSize">
                                <form class="editSiteForm ajaxForm" id="editSiteForm${node.identifier}" action="<c:url value='${url.base}${node.path}.adminEditSite.do'/>" >

                                    <fieldset>
                                        <legend><fmt:message key="label.manageSite.siteProperties"/></legend>
                                        <h3><fmt:message key="label.manageSite.siteProperties"/></h3>

                                        <p id="siteTitleForm${node.identifier}">
                                            <label for="siteTitle${node.identifier}"><fmt:message key="jnt_virtualsite.j_title"/> (*)</label>
                                            <input class="inputsize2" type="text" name="siteTitle" id="siteTitle${node.identifier}" value="${node.properties['j:title'].string}"/>
                                        </p>

                                        <p id="siteServerNameForm${node.identifier}">
                                            <label for="siteServerName${node.identifier}"><fmt:message key="jnt_virtualsite.j_serverName"/> (*)</label>
                                            <input class="inputsize2" type="text" name="siteServerName" id="siteServerName${node.identifier}" value="${node.properties['j:serverName'].string}"/>
                                        </p>

                                        <p id="siteDescrForm${node.identifier}">
                                            <label for="siteDescr${node.identifier}"><fmt:message key="jnt_virtualsite.j_description"/></label>
                                            <textarea class="inputsize2" type="text" name="siteDescr" id="siteDescr${node.identifier}">${node.properties['j:description'].string}</textarea>
                                        </p>
                                    </fieldset>
                                    <button site="${node.identifier}" onclick="editProperties('${node.identifier}')"><fmt:message key="label.manageSite.submitChanges"/></button>
                                </form>
                            </div>
                        </div>
                    </c:if>
                </td>
            </c:when>
            <c:otherwise>
                <c:set var="editModeAccess"
                       value="${jcr:findAllowedNodesForPermission('editModeAccess', node, 'jnt:page')}"/>
                <c:set var="contributeModeAccess"
                       value="${jcr:findAllowedNodesForPermission('contributeModeAccess', node, 'jnt:page')}"/>
                <c:set var="previewModeAccess"
                       value="${jcr:findAllowedNodesForPermission('jcr:read_default', node, 'jnt:page')}"/>
                <c:if test="${node.home.properties['j:published'].boolean or not empty editModeAccess or not empty contributeModeAccess or not empty previewModeAccess}">
                    <td headers="Actions">
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
                            <a href="<c:url value='${baseEdit}${editModeAccess[0].path}.html'/>"
                               title="<fmt:message key='label.editMode'/>"><img
                                    src="<c:url value='/icons/editMode.png'/>" width="16" height="16" alt=" "
                                    role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                        </c:if>

                        <c:if test="${not empty contributeModeAccess && currentNode.properties.contribute.boolean && !renderContext.settings.distantPublicationServerMode}">
                            <a href="<c:url value='${baseContribute}${contributeModeAccess[0].path}.html'/>"
                               title="<fmt:message key='label.contribute'/>"><img
                                    src="<c:url value='/icons/contribute.png'/>" width="16" height="16" alt=" "
                                    role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                        </c:if>

                        <c:if test="${not empty previewModeAccess && currentNode.properties.preview.boolean && !renderContext.settings.distantPublicationServerMode}">
                            <a href="<c:url value='${basePreview}${previewModeAccess[0].path}.html'/>"
                               title="<fmt:message key='label.preview'/>"><img
                                    src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" "
                                    role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                        </c:if>
                        <c:if test="${currentNode.properties.live.boolean && node.home.properties['j:published'].boolean}">
                            <a href="<c:url value='${baseLive}${node.path}${page}.html'/>"
                               title="<fmt:message key='label.live'/>"><img
                                    src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" "
                                    role="presentation" style="position:relative; top: 4px; margin-right:2px; "/></a>
                        </c:if>
                    </td>
                </c:if>
            </c:otherwise>
        </c:choose>
        </tr>
    </c:forEach>
    </tbody>
</table>

<div style="display:none">
    <div id="dialog-delete-confirm" title=" ">
        <p><span class="ui-icon ui-icon-alert"
                 style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.delete.confirm" /></p>
    </div>
    <div id="nothing-selected" >
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.manageSites.noSiteSelected"/></p>
    </div>
</div>
<div style="display:none;" class="loading">
    <h1><fmt:message key="org.jahia.admin.workInProgressTitle"/></h1>
</div>

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

<template:include view="hidden.footer"/>
