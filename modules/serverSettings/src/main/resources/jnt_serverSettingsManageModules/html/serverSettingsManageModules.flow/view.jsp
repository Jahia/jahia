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
<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js,jquery.blockUI.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>

<template:addResources>
<script type="text/javascript">
    $(document).ready(function() { $('.button-download').click(workInProgress) });
</script>
</template:addResources>

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
            <input type="file" id="moduleFile" name="moduleFile" accept=""/>
            <button class="btn btn-primary" type="submit" name="_eventId_upload">
                <i class="icon-download-alt icon-white"></i>
                &nbsp;<fmt:message key='label.upload'/>
            </button>
        </div>
    </form:form>
</c:if>
<%@include file="moduleLabels.jspf" %>
<table class="table table-bordered table-striped table-hover">
    <thead>
    <tr>
        <th><fmt:message key='serverSettings.manageModules.moduleName'/></th>
        <th></th>
        <th>${i18nModuleDetails}</th>
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
                <td>
                    <strong>${currentModule != null ? currentModule.name : defaultVersion.name}</strong>
                    <c:if test="${not isStudio && functions:contains(systemSiteRequiredModules, currentModule != null ? currentModule.rootFolder : defaultVersion.rootFolder)}">
                        <span class="text-error" title="${i18nMandatoryDependency}"><a href="#mandatory-dependency" class="text-error"><strong>*</strong></a></span>
                    </c:if>
                </td>
                <td>${entry.key}</td>
                <td>
                    <c:if test="${isStudio}">
                        <c:url var="urlDetails" value="${url.base}/modules/${currentModule.rootFolder}.html"/>
                        <button class="btn btn-info" type="button" onclick='window.location.assign("${urlDetails}")'>
                            <i class="icon-zoom-in icon-white"></i>
                            &nbsp;${i18nModuleDetails}
                        </button>
                    </c:if>
                    <c:if test="${not isStudio}">
                        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                            <input type="hidden" name="selectedModule" value="${entry.key}"/>
                            <button class="btn btn-info" type="submit" name="_eventId_viewDetails" onclick="">
                                <i class="icon-zoom-in icon-white"></i>
                                &nbsp;${i18nModuleDetails}
                            </button>
                        </form>
                    </c:if>
                </td>
                <td>
                    <c:forEach items="${entry.value}" var="version">
                        <c:set var="isActiveVersion" value="${version.key == currentModule.version}"/>
                        <c:set var="displayVersionAndState" value="true"/>
                        <c:if test="${not isStudio}">
                            <%@include file="moduleVersionActions.jspf" %>
                        </c:if>
                        <c:if test="${isStudio and isActiveVersion}">
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
                                <span class="label label-success"><strong>${i18nModuleActive}</strong></span>
                            </c:when>
                            <c:otherwise>
                                <span class="label"><strong>${i18nModuleInactive}</strong></span>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${not empty currentModule.sourcesFolder}">
                                <c:url var="urlToStudio" value="/cms/studio/${currentResource.locale}/modules/${currentModule.rootFolder}.html"/>
                                <button class="btn btn-block" type="button" name="_eventId_startModule" onclick='window.parent.location.assign("${urlToStudio}")'>
                                    <i class="icon-circle-arrow-right"></i>
                                    &nbsp;<fmt:message key='serverSettings.manageModules.goToStudio' />
                                </button>
                            </c:when>
                            <c:when test="${not empty currentModule.scmURI}">
                                <c:if test="${functions:contains(sourceControls, fn:substringBefore(fn:substringAfter(currentModule.scmURI, ':'),':'))}">
                                    <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                        <input type="hidden" name="module" value="${entry.key}"/>
                                        <input type="hidden" name="scmUri" value="${currentModule.scmURI}"/>
                                        <button class="btn btn-block button-download" type="submit" name="_eventId_downloadSources" onclick="">
                                            <i class="icon-download"></i>
                                            &nbsp;${i18nDownloadSources}
                                        </button>
                                    </form>
                                </c:if>
                            </c:when>

                            <c:otherwise>
                                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="module" value="${entry.key}"/>
                                    <input type="hidden" name="scmUri" value="scm:git:"/>
                                    <fmt:message var="label" key='serverSettings.manageModules.downloadSources'/>
                                    <button class="btn btn-block" type="submit" name="_eventId_downloadSources" onclick="">
                                        <i class="icon-download"></i>
                                        &nbsp;${i18nDownloadSources}
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>

                    </td>
                </c:if>
                <td>
                    <c:choose>
                        <c:when test="${not empty sitesTemplates[entry.key]}"><fmt:message key='serverSettings.manageModules.usedInSites.templates'/></c:when>
                        <c:when test="${not empty sitesDirect[entry.key]}"><fmt:message key='serverSettings.manageModules.usedInSites.direct'/></c:when>
                        <c:when test="${empty sitesDirect[entry.key] and not empty sitesTransitive[entry.key]}"><fmt:message key='serverSettings.manageModules.usedInSites.transitive'/></c:when>
                        <c:otherwise>&nbsp;</c:otherwise>
                    </c:choose>
                </td>

            </tr>
        </c:if>
    </c:forEach>
    </tbody>
</table>
<c:if test="${not isStudio}">
<p><a id="mandatory-dependency">&nbsp;</a><span class="text-error"><strong>*</strong></span>&nbsp;-&nbsp;${i18nMandatoryDependency}</p>
</c:if>