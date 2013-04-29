<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="otherVersions" type="java.util.Map<org.jahia.services.templates.ModuleVersion,org.jahia.data.templates.JahiaTemplatesPackage>"--%>
<%--@elvariable id="bundleInfo" type="java.util.Map<java.lang.String, java.lang.String>"--%>
<%--@elvariable id="activeVersion" type="org.jahia.data.templates.JahiaTemplatesPackage"--%>
<template:addResources type="javascript" resources="jquery.js,jquery-ui.min.js,bootstrap.js,jquery.blockUI.js"/>
<template:addResources type="css" resources="bootstrap.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<script type="text/javascript">
    $(document).ready(function() {
        $('.button-download').click(workInProgress)
    });
</script>

<c:set value="${renderContext.editModeConfigName eq 'studiomode' or renderContext.editModeConfigName eq 'studiolayoutmode'}" var="isStudio"/>

<script type="text/javascript">
    $(function() {
        var selectedForm;
        $("#disable-confirm").dialog({
            autoOpen: false,
            resizable: false,
            height:200,
            modal: true,
            buttons: {
                "<fmt:message key='label.yes'/>": function() {
                    $('#'+selectedForm + ' input[name=purge]').val(true);
                    $('#'+selectedForm).submit();
                    $( this ).dialog( "close" );
                },
                "<fmt:message key='label.no'/>": function() {
                    $('#'+selectedForm).submit();
                    $( this ).dialog( "close" );
                },
                "<fmt:message key='label.cancel'/>": function() {
                    $( this ).dialog( "close" );
                }
            }
        });
        $(".disable-button").click(function() {
            selectedForm = $(".disable-button").parent().attr('id');
            $( "#disable-confirm" ).dialog( "open" );
        });
    });
</script>

<div id="disable-confirm" title="<fmt:message key='serverSettings.manageModules.module.disable.purgeContent.title'/>">
    <p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 20px 0;"></span><fmt:message key="serverSettings.manageModules.module.disable.purgeContent.message"/></p>
</div>

<div id="detailActiveVersion">
    <h2>${activeVersion.name}&nbsp;${activeVersion.version}</h2>
    <p>
        ${fn:escapeXml(bundleInfo['Bundle-Description'])}
    </p>
    <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
        <c:if test="${message.severity eq 'INFO'}">
            <div class="alert alert-success">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                    ${message.text}
            </div>
        </c:if>
        <c:if test="${message.severity eq 'ERROR'}">
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                    ${message.text}
            </div>
        </c:if>
    </c:forEach>
    <c:if test="${not empty error}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            <fmt:message key="${error}"/>
        </div>
    </c:if>
    <table class="table table-striped table-bordered table-hover">
        <thead>
            <tr>
                <th>
                    <fmt:message key="serverSettings.manageModules.module.state"/>
                </th>
                <th>
                    <fmt:message key="serverSettings.manageModules.module.type"/>
                </th>
                <th>
                    <fmt:message key="serverSettings.manageModules.module.author"/>
                </th>

<c:if test="${not isStudio}">
                <th>
                    <fmt:message key="serverSettings.manageModules.module.source.uri"/>
                </th>
</c:if>

            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <c:choose>
                        <c:when test="${activeVersion.bundle.state eq 32}">
                            <fmt:message key="serverSettings.manageModules.module.state.active"/>
                        </c:when>
                        <c:when test="${activeVersion.bundle.state eq 16}">
                            <fmt:message key="serverSettings.manageModules.module.state.stopping"/>
                        </c:when>
                        <c:when test="${activeVersion.bundle.state eq 8}">
                            <fmt:message key="serverSettings.manageModules.module.state.starting"/>
                        </c:when>
                        <c:when test="${activeVersion.bundle.state eq 4}">
                            <fmt:message key="serverSettings.manageModules.module.state.resolved"/>
                        </c:when>
                        <c:when test="${activeVersion.bundle.state eq 2}">
                            <fmt:message key="serverSettings.manageModules.module.state.installed"/>
                        </c:when>
                        <c:when test="${activeVersion.bundle.state eq 1}">
                            <fmt:message key="serverSettings.manageModules.module.state.uninstalled"/>
                        </c:when>
                    </c:choose>
                </td>
                <td>
                    ${activeVersion.moduleType}
                </td>
                <td>
                    ${bundleInfo['Implementation-Vendor']}
                </td>
<c:if test="${not isStudio}">
                <td>
                    <c:choose>
                        <c:when test="${not empty activeVersion.sourcesFolder}">
                            <input class="btn" type="button"
                                   onclick='window.parent.location.assign("/cms/studio/${currentResource.locale}/modules/${activeVersion.rootFolder}.html")'
                                   value="<fmt:message key='serverSettings.manageModules.goToStudio' />"/>
                        </c:when>
                        <c:when test="${not empty activeVersion.scmURI}">
                            <c:if test="${functions:contains(sourceControls, fn:substringBefore(fn:substringAfter(activeVersion.scmURI, ':'),':'))}">
                                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="module" value="${activeVersion.rootFolder}"/>
                                    <input type="hidden" name="scmUri" value="${activeVersion.scmURI}"/>
                                    <fmt:message var="label" key='serverSettings.manageModules.downloadSources'/>
                                    <input class="btn button-download" type="submit" name="_eventId_downloadSources" value="${label}" onclick=""/>
                                </form>
                            </c:if>
                        </c:when>

                        <c:otherwise>
                            <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                <input type="hidden" name="module" value="${activeVersion.rootFolder}"/>
                                <input type="hidden" name="scmUri" value="scm:git:"/>
                                <fmt:message var="label" key='serverSettings.manageModules.downloadSources'/>
                                <input class="btn" type="submit" name="_eventId_viewDownloadForm" value="${label}" onclick=""/>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </td>
</c:if>
            </tr>
        </tbody>
    </table>

</div>
<div class="accordion" id="accordion2">
<div class="accordion-group">
    <div class="accordion-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseOne">
            <strong><fmt:message key="serverSettings.manageModules.versions"/></strong></a>
    </div>
    <div id="collapseOne" class="accordion-body collapse" style="height: 0px; ">
        <div class="accordion-inner">
            <table class="table table-striped table-bordered table-hover">
                <thead>
                <tr>
                    <th><fmt:message key="serverSettings.manageModules.module.version"/></th>
                    <th><fmt:message key="serverSettings.manageModules.module.state"/></th>
                    <c:if test="${not isStudio}">
                    <th><fmt:message key="serverSettings.manageModules.module.manage"/></th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${otherVersions}" var="package">
                    <tr>
                        <td>${package.key}</td>
                        <td>
                            <c:choose>
                                <c:when test="${package.value.bundle.state eq 32}">
                                    <fmt:message key="serverSettings.manageModules.module.state.active"/>
                                </c:when>
                                <c:when test="${package.value.bundle.state eq 16}">
                                    <fmt:message key="serverSettings.manageModules.module.state.stopping"/>
                                </c:when>
                                <c:when test="${package.value.bundle.state eq 8}">
                                    <fmt:message key="serverSettings.manageModules.module.state.starting"/>
                                </c:when>
                                <c:when test="${package.value.bundle.state eq 4}">
                                    <fmt:message key="serverSettings.manageModules.module.state.resolved"/>
                                </c:when>
                                <c:when test="${package.value.bundle.state eq 2}">
                                    <fmt:message key="serverSettings.manageModules.module.state.installed"/>
                                </c:when>
                                <c:when test="${package.value.bundle.state eq 1}">
                                    <fmt:message key="serverSettings.manageModules.module.state.uninstalled"/>
                                </c:when>
                            </c:choose>
                        </td>
                        <c:if test="${not isStudio}">
                        <td>
                            <c:choose>
                                <c:when test="${package.key eq activeVersion.version and package.value.activeVersion}">
                                    <div class="active-version">
                                        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                            <input type="hidden" name="module" value="${package.value.rootFolder}"/>
                                            <fmt:message var="label" key='serverSettings.manageModules.stopModule'/>
                                            <input class="btn btn-danger" type="submit" name="_eventId_stopModule"
                                                   value="${label}" onclick=""/>&nbsp; ${package.key} ( ${package.value.state.state} )
                                        </form>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="inactive-version">
                                        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                            <input type="hidden" name="module" value="${package.value.rootFolder}"/>
                                            <input type="hidden" name="version" value="${package.key}"/>
                                            <fmt:message var="label"
                                                         key='serverSettings.manageModules.startModule'/>
                                            <input class="btn btn-success" type="submit" name="_eventId_startModule"
                                                   value="${label}" onclick=""/>&nbsp; ${package.key} ( ${package.value.state.state} )
                                        </form>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="accordion-group">
    <div class="accordion-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseTwo">
            <strong><fmt:message key="serverSettings.manageModules.sites.management"/></strong></a>
    </div>
    <div id="collapseTwo" class="accordion-body collapse" style="height: 0px; ">
        <div class="accordion-inner">
            <table class="table table-striped table-bordered table-hover">
                <thead>
                <tr>
                    <th><fmt:message key="serverSettings.manageModules.module.site"/></th>
                    <th><fmt:message key="serverSettings.manageModules.module.dependency.type"/></th>
                    <th><fmt:message key="serverSettings.manageModules.module.manage"/></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${sites}" var="site" varStatus="status">
                    <tr>
                        <td>${site}</td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty sitesDirect[activeVersion.rootFolder] and functions:contains(sitesDirect[activeVersion.rootFolder],site)}">
                                    <fmt:message key="serverSettings.manageModules.usedInSites.direct"/>
                                </c:when>
                                <c:when test="${not empty sitesTemplates[activeVersion.rootFolder] and functions:contains(sitesTemplates[activeVersion.rootFolder],site)}">
                                    <fmt:message
                                            key="serverSettings.manageModules.usedInSites.templates"/>
                                </c:when>
                                <c:when test="${not empty sitesTransitive[activeVersion.rootFolder] and functions:contains(sitesTransitive[activeVersion.rootFolder],site)}">
                                    <fmt:message
                                            key="serverSettings.manageModules.usedInSites.transitive"/>
                                </c:when>
                                <c:otherwise>
                                    <fmt:message key="serverSettings.manageModules.module.no.dependency"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:set var="cellEmpty" value="true"/>
                            <c:if test="${activeVersion.moduleType ne 'templatesSet'}">
                            <c:choose>
                                <c:when test="${not empty sitesDirect[activeVersion.rootFolder] and functions:contains(sitesDirect[activeVersion.rootFolder],site)}">
                                    <form id="disable${status.index}" style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                        <input type="hidden" name="module" value="${activeVersion.rootFolder}"/>
                                        <input type="hidden" name="disableFrom" value="/sites/${site}"/>
                                        <input type="hidden" name="purge" value="false"/>
                                        <input type="hidden" name="_eventId_disable" value="true"/>
                                        <fmt:message var="label"
                                                     key='serverSettings.manageModules.module.disable'/>
                                        <input class="btn btn-danger disable-button" type="button" value="${label}" onclick=""/>
                                        <c:set var="usedOnce" value="true"/>
                                    </form>
                                    <c:set var="cellEmpty" value="false"/>
                                </c:when>
                                <c:when test="${not empty sitesTransitive[activeVersion.rootFolder] and functions:contains(sitesTransitive[activeVersion.rootFolder],site)}">
                                </c:when>
                                <c:otherwise>
                                    <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                        <input type="hidden" name="module" value="${activeVersion.rootFolder}"/>
                                        <input type="hidden" name="enableOn" value="/sites/${site}"/>
                                        <fmt:message var="label"
                                                     key='serverSettings.manageModules.module.enable'/>
                                        <input class="btn btn-success" type="submit" name="_eventId_enable"
                                               value="${label}" onclick=""/>
                                    </form>
                                    <c:set var="cellEmpty" value="false"/>
                                </c:otherwise>
                            </c:choose>
                            </c:if>
                            <c:if test="${cellEmpty}">&nbsp;</c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${not empty usedOnce}">
                <tr>
                    <td align="right" colspan="3">
                        <form id="disableAll" style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                            <input type="hidden" name="module" value="${activeVersion.rootFolder}"/>
                            <input type="hidden" name="purge" value="false"/>
                            <input type="hidden" name="_eventId_disableAll" value="true"/>
                            <fmt:message var="label"
                                         key='serverSettings.manageModules.module.disable.all'/>
                            <input class="btn btn-danger disable-button" type="button" value="${label}" onclick=""/>
                        </form>
                    </td>
                </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>



<%--@elvariable id="nodeTypes" type="java.util.Map<java.lang.String,java.lang.Boolean>"--%>
<c:if test="${not empty nodeTypes}">
    <div class="accordion-group">
        <div class="accordion-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseThree">
                <strong><fmt:message key="serverSettings.manageModules.module.nodetypes"/></strong></a>
        </div>
        <div id="collapseThree" class="accordion-body collapse" style="height: 0px; ">
            <div class="accordion-inner">
                <table class="table table-striped table-bordered table-hover">
                    <thead>
                    <tr>
                        <th><fmt:message key="serverSettings.manageModules.module.nodetype.name"/></th>
                        <th><fmt:message key='serverSettings.manageModules.module.nodetype.component'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${nodeTypes}" var="nodeType">
                        <tr>
                            <td><span style="font: bold">${nodeType.key}</span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${nodeType.value}">
                                        <fmt:message key="label.yes"/>
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:message key="label.no"/>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</c:if>
<c:if test="${not empty activeVersion.dependencies}">
    <div class="accordion-group">
        <div class="accordion-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseFour">
                <strong><fmt:message key="serverSettings.manageModules.module.dependencies"/></strong></a>
        </div>
        <div id="collapseFour" class="accordion-body collapse" style="height: 0px; ">
            <div class="accordion-inner">
                <table class="table table-striped table-bordered table-hover">
                    <thead>
                    <tr>
                        <th><fmt:message key="serverSettings.manageModules.module.dependency.name"/></th>
                        <th><fmt:message key='serverSettings.manageModules.details'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${activeVersion.dependencies}" var="dependency">
                        <tr>
                            <td><span style="font: bold">${dependency.name}</span></td>
                            <td>
                                <c:if test="${isStudio and not empty dependency.sourcesFolder}">
                                    <input class="btn btn-info" type="button" onclick='window.location.assign("${url.base}/modules/${dependency.rootFolder}.html")' value="<fmt:message key='serverSettings.manageModules.details' />"/>
                                </c:if>
                                <c:if test="${not isStudio}">
                                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="selectedModule" value="${dependency.rootFolder}"/>
                                    <input class="btn btn-info" type="submit" name="_eventId_viewDetails"
                                           value="<fmt:message key='serverSettings.manageModules.details' />"
                                           onclick=""/>
                                </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</c:if>
<c:if test="${not empty dependantModules}">

    <div class="accordion-group">
        <div class="accordion-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseFive">
                <strong><fmt:message key="serverSettings.manageModules.module.dependantModules"/></strong></a>
        </div>
        <div id="collapseFive" class="accordion-body collapse" style="height: 0px; ">
            <div class="accordion-inner">
                <table class="table table-striped table-bordered table-hover">
                    <thead>
                    <tr>
                        <th><fmt:message key="serverSettings.manageModules.module.dependency.name"/></th>
                        <th><fmt:message key='serverSettings.manageModules.details'/></th>
                    </tr>
                    </thead>
                    <tbody>

                    <c:forEach items="${dependantModules}" var="dependency">
                        <tr>
                            <td><span style="font: bold">${dependency.name}</span></td>
                            <td>
                                <c:if test="${isStudio}">
                                    <c:choose>
                                        <c:when test="${not empty dependency.sourcesFolder}">
                                            <input class="btn btn-info" type="button"
                                                onclick='window.location.assign("${url.base}/modules/${dependency.rootFolder}.html")'
                                                value="<fmt:message key='serverSettings.manageModules.details' />"/>
                                        </c:when>
                                        <c:otherwise>&nbsp;</c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${not isStudio}">
                                    <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                        <input type="hidden" name="selectedModule" value="${dependency.rootFolder}"/>
                                        <input class="btn btn-info" type="submit" name="_eventId_viewDetails"
                                               value="<fmt:message key='serverSettings.manageModules.details' />"
                                               onclick=""/>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</c:if>

</div>