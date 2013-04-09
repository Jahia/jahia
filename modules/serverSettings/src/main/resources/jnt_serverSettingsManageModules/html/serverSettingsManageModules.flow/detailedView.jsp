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
<%--@elvariable id="otherVersions" type="java.util.Map<org.jahia.services.templates.ModuleVersion,org.jahia.data.templates.JahiaTemplatesPackage>"--%>
<%--@elvariable id="bundleInfo" type="java.util.Map<java.lang.String, java.lang.String>"--%>
<%--@elvariable id="activeVersion" type="org.jahia.data.templates.JahiaTemplatesPackage"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<div id="detailActiveVersion" style="border: thin groove;">
    <h3>${activeVersion.name}&nbsp;${activeVersion.version}</h3>

    <p>
        ${bundleInfo['Bundle-Description']}
    </p>
    <dl>
        <dt><fmt:message key="serverSettings.manageModules.module.type"/></dt>
        <dd>${activeVersion.moduleType}</dd>

        <dt><fmt:message key="serverSettings.manageModules.module.author"/></dt>
        <dd>${bundleInfo['Implementation-Vendor']}</dd>

        <dt><fmt:message key="serverSettings.manageModules.module.source.uri"/></dt>
        <dd><c:choose>
            <c:when test="${not empty activeVersion.sourcesFolder}">
                <input class="btn" type="button" onclick='window.parent.location.assign("/cms/studio/${currentResource.locale}/modules/${activeVersion.rootFolder}.siteTemplate.html")' value="<fmt:message key='serverSettings.manageModules.goToStudio' />"/>
                <%--<c:if test="${renderContext.editModeConfigName ne 'studiomode' and renderContext.editModeConfigName ne 'studiolayoutmode'}">--%>
                <%--<a href="/cms/studio/${currentResource.locale}/modules/${currentModule.rootFolder}.siteTemplate.html"></a>--%>
                <%--</c:if>--%>

            </c:when>
            <c:when test="${not empty activeVersion.scmURI}">
                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                    <input type="hidden" name="module" value="${activeVersion.rootFolder}"/>
                    <input type="hidden" name="scmUri" value="${activeVersion.scmURI}"/>
                    <fmt:message var="label" key='serverSettings.manageModules.downloadSources' />
                    <input class="btn" type="submit" name="_eventId_downloadSources" value="${label}" onclick=""/>
                </form>
            </c:when>

            <c:otherwise>
                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                    <input type="hidden" name="module" value="${activeVersion.rootFolder}"/>
                    <input type="hidden" name="scmUri" value="scm:git:"/>
                    <fmt:message var="label" key='serverSettings.manageModules.downloadSources' />
                    <input class="btn" type="submit" name="_eventId_downloadSources" value="${label}" onclick=""/>
                </form>
            </c:otherwise>
        </c:choose></dd>
    </dl>
</div>
<div id="versionsList">
    <h3><fmt:message key="serverSettings.manageModules.versions"/></h3>
    <table style="border: thin groove">
        <thead>
        <tr>
            <th><fmt:message key="serverSettings.manageModules.module.version"/></th>
            <th><fmt:message key="serverSettings.manageModules.module.active"/></th>
            <th><fmt:message key="serverSettings.manageModules.module.manage"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${otherVersions}" var="package">
            <tr>
                <td>${package.key}</td>
                <td><c:choose><c:when test="${package.value.activeVersion}"><fmt:formatDate value="${activeVersionDate}"
                                                                                            type="both"/></c:when><c:otherwise><fmt:message
                        key="serverSettings.manageModule.module.inactive"/></c:otherwise></c:choose></td>
                <td>
                    <c:choose>
                        <c:when test="${package.key eq activeVersion.version and package.value.activeVersion}">
                            <div class="active-version">
                                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="module" value="${package.value.rootFolder}"/>
                                    <fmt:message var="label" key='serverSettings.manageModules.stopModule'/>
                                    <input class="btn btn-danger" type="submit" name="_eventId_stopModule"
                                           value="${label}" onclick=""/>
                                </form>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="inactive-version">
                                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="module" value="${package.value.rootFolder}"/>
                                    <input type="hidden" name="version" value="${package.key}"/>
                                    <fmt:message var="label" key='serverSettings.manageModules.startModule'/>
                                    <input class="btn btn-success" type="submit" name="_eventId_startModule"
                                           value="${label}" onclick=""/>
                                </form>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<div id="sitesList">
    <h3><fmt:message key="serverSettings.manageModules.sites.management"/></h3>
    <table style="border: thin groove">
        <thead>
        <tr>
            <th><fmt:message key="serverSettings.manageModules.module.site"/></th>
            <th><fmt:message key="serverSettings.manageModules.module.dependency.type"/></th>
            <th><fmt:message key="serverSettings.manageModules.module.manage"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${sites}" var="site">
            <tr>
                <td>${site}</td>
                <td>
                    <c:choose>
                        <c:when test="${functions:contains(sitesDirect,site)}">
                            <fmt:message key="serverSettings.manageModules.module.dependency.type.direct"/>
                        </c:when>
                        <c:when test="${functions:contains(sitesTemplates,site)}">
                            <fmt:message key="serverSettings.manageModules.module.dependency.type.templates"/>
                        </c:when>
                        <c:when test="${functions:contains(sitesTransitive,site)}">
                            <fmt:message key="serverSettings.manageModules.module.dependency.type.transitive"/>
                        </c:when>
                        <c:otherwise>
                            <fmt:message key="serverSettings.manageModules.module.no.dependency"/>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>