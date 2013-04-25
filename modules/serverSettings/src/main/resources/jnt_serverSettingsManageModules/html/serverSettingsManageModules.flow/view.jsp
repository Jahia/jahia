<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
<template:addResources type="javascript" resources="jquery.min.js,bootstrap.js,jquery.blockUI.js"/>
<template:addResources type="css" resources="bootstrap.css"/>

<script type="text/javascript">
    $(document).ready(function () {
        $('.button-download').click(function () {
            $.blockUI({ css: {
                border: 'none',
                padding: '15px',
                backgroundColor: '#000',
                '-webkit-border-radius': '10px',
                '-moz-border-radius': '10px',
                opacity: .5,
                color: '#fff'
            }, message: '<fmt:message key="label.workInProgressTitle"/>' });
        });
    });
</script>

<c:set value="${renderContext.editModeConfigName eq 'studiomode' or renderContext.editModeConfigName eq 'studiolayoutmode'}"
       var="isStudio"/>

<c:if test="${not isStudio}">
    <form:form modelAttribute="moduleFile" class="form" enctype="multipart/form-data" method="post">
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
        <div class="alert alert-info">
            <label for="moduleFile"><fmt:message key="serverSettings.manageModules.upload.module"/></label>
            <input type="file" id="moduleFile" name="moduleFile" accept=""/><input class="btn btn-primary" type="submit"
                                                                                   name="_eventId_upload"
                                                                                   value="<fmt:message key='label.upload'/>"/>
        </div>
    </form:form>
</c:if>
<table class="table table-bordered table-striped table-hover">
    <thead>
    <tr>
        <th><fmt:message key='serverSettings.manageModules.moduleName'/></th>
        <th></th>
        <th><fmt:message key='serverSettings.manageModules.details'/></th>
        <th><fmt:message key='serverSettings.manageModules.versions'/></th>
        <c:if test="${not isStudio}">
            <th><fmt:message key='serverSettings.manageModules.status'/></th>
        </c:if>
        <c:if test="${not isStudio}">
            <th><fmt:message key='serverSettings.manageModules.sources'/></th>
        </c:if>
        <th><fmt:message key='serverSettings.manageModules.usedInSites'/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${allModuleVersions}" var="entry">
        <c:set value="${registeredModules[entry.key]}" var="currentModule"/>
        <c:if test="${empty currentModule}">
            <c:forEach var="v" items="${entry.value}" end="0"><c:set var="defaultVersion"
                                                                     value="${v.value}"/></c:forEach>
        </c:if>
        <c:if test="${not isStudio or not empty currentModule.sourcesFolder}">
            <tr>
                <td><strong>${currentModule != null ? currentModule.name : defaultVersion.name}</strong></td>
                <td>${entry.key}</td>
                <td>
                    <c:if test="${isStudio}">
                        <input class="btn btn-info" type="button"
                               onclick='window.location.assign("${url.base}/modules/${currentModule.rootFolder}.html")'
                               value="<fmt:message key='serverSettings.manageModules.details' />"/>
                    </c:if>
                    <c:if test="${not isStudio}">
                        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                            <input type="hidden" name="selectedModule" value="${entry.key}"/>
                            <input class="btn btn-info" type="submit" name="_eventId_viewDetails"
                                   value="<fmt:message key='serverSettings.manageModules.details' />" onclick=""/>
                        </form>
                    </c:if>
                </td>
                <td>
                    <c:forEach items="${entry.value}" var="version">
                        <c:if test="${not isStudio}">
                            <c:set var="unresolvedDependencies" value=""/>
                            <c:forEach items="${version.value.depends}" var="dep">
                                <c:if test="${empty registeredModules[dep]}">
                                    <c:set var="unresolvedDependencies" value="${unresolvedDependencies} ${dep}"/>
                                </c:if>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${not empty unresolvedDependencies}">
                                    <div class="active-version">
                                        <fmt:message key='serverSettings.manageModules.module.depends'/>
                                        : ${unresolvedDependencies}
                                        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                            <input type="hidden" name="module" value="${entry.key}"/>
                                            <fmt:message var="label" key='serverSettings.manageModules.startModule'/>
                                            <input class="btn btn-success" type="submit" value="${label}"
                                                   disabled="true"/>&nbsp; ${version.key} ( ${version.value.state.state} )
                                        </form>
                                    </div>
                                </c:when>
                                <c:when test="${version.key eq currentModule.version}">
                                    <div class="active-version">
                                        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                            <input type="hidden" name="module" value="${entry.key}"/>
                                            <fmt:message var="label" key='serverSettings.manageModules.stopModule'/>
                                            <input class="btn btn-danger" type="submit" name="_eventId_stopModule"
                                                   value="${label}" onclick=""/>&nbsp; ${version.key} ( ${version.value.state.state} )
                                        </form>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="inactive-version">
                                        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                            <input type="hidden" name="module" value="${entry.key}"/>
                                            <input type="hidden" name="version" value="${version.key}"/>
                                            <fmt:message var="labelStart"
                                                         key='serverSettings.manageModules.startModule'/>
                                            <fmt:message var="labelUndeployModule"
                                                         key='serverSettings.manageModules.undeployModule'/>

                                            <input class="btn btn-success" type="submit" name="_eventId_startModule"
                                                   value="${labelStart}" onclick="" />&nbsp;

                                            <c:set value="${not empty sitesDirect[entry.key] or not empty sitesTemplates[entry.key] or not empty sitesTransitive[entry.key]}" var="used"/>
                                            <input class="btn" type="submit" name="_eventId_undeployModule"
                                                   ${(fn:length(entry.value) == 1 and used) ? "disabled='true'" :""}
                                                   value="${labelUndeployModule}"
                                                   onclick=""/>&nbsp; ${version.key} ( ${version.value.state.state} )
                                        </form>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${isStudio and version.key eq currentModule.version}">
                            <div class="active-version">
                                    ${version.key}
                            </div>
                        </c:if>
                    </c:forEach>
                </td>


                <c:if test="${not isStudio}">
                    <td>
                        <c:choose>
                            <c:when test="${not empty currentModule}">
                                <span class="label label-success"><strong>Active</strong></span>
                            </c:when>
                            <c:otherwise>
                                <span class="label"><strong>Inactive</strong></span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </c:if>

                <c:if test="${not isStudio}">
                    <td>
                            <%--${currentModule.sourcesFolder}--%>
                        <c:choose>
                            <c:when test="${not empty currentModule.sourcesFolder}">
                                <input class="btn btn-block" type="button"
                                       onclick='window.parent.location.assign("/cms/studio/${currentResource.locale}/modules/${currentModule.rootFolder}.html")'
                                       value="<fmt:message key='serverSettings.manageModules.goToStudio' />"/>
                            </c:when>
                            <c:when test="${not empty currentModule.scmURI}">
                                <c:if test="${functions:contains(sourceControls, fn:substringBefore(fn:substringAfter(currentModule.scmURI, ':'),':'))}">
                                    <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                        <input type="hidden" name="module" value="${entry.key}"/>
                                        <input type="hidden" name="scmUri" value="${currentModule.scmURI}"/>
                                        <fmt:message var="label" key='serverSettings.manageModules.downloadSources'/>
                                        <input class="btn btn-block button-download" type="submit"
                                               name="_eventId_downloadSources" value="${label}" onclick=""/>
                                    </form>
                                </c:if>
                            </c:when>

                            <c:otherwise>
                                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="module" value="${entry.key}"/>
                                    <input type="hidden" name="scmUri" value="scm:git:"/>
                                    <fmt:message var="label" key='serverSettings.manageModules.downloadSources'/>
                                    <input class="btn btn-block" type="submit" name="_eventId_downloadSources"
                                           value="${label}" onclick=""/>
                                </form>
                            </c:otherwise>
                        </c:choose>

                    </td>
                </c:if>
                <td>
                    <c:if test="${not empty sitesTemplates[entry.key]}"><fmt:message key='serverSettings.manageModules.usedInSites.templates'/></c:if>
                    <c:if test="${not empty sitesDirect[entry.key]}"><fmt:message key='serverSettings.manageModules.usedInSites.direct'/></c:if>
                    <c:if test="${empty sitesDirect[entry.key] and not empty sitesTransitive[entry.key]}"><fmt:message key='serverSettings.manageModules.usedInSites.transitive'/></c:if>
                </td>

            </tr>
        </c:if>
    </c:forEach>
    </tbody>
</table>