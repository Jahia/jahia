<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="module" type="org.jahia.modules.serversettings.forge.Module"--%>
<template:addResources type="javascript" resources="jquery.min.js"/>
<fmt:message key="serverSettings.manageModules.details" var="i18nModuleDetails" />

<form id="viewInstalledModulesForm" style="display: none" action="${flowExecutionUrl}" method="POST">
    <input type="hidden" name="_eventId" value="viewInstalledModules"/>
</form>
<ul class="nav nav-tabs">
    <li><a href="#" onclick="$('#viewInstalledModulesForm').submit()"><fmt:message key="serverSettings.manageModules.installedModules"/></a></li>
    <li class="active">
        <a href="#"><fmt:message key="serverSettings.manageModules.availableModules"/></a>
    </li>
</ul>

<table class="table table-bordered table-striped table-hover">
    <thead>
    <tr>
        <th><fmt:message key='serverSettings.manageModules.moduleName'/></th>
        <th></th>
        <th>
            <fmt:message key="serverSettings.manageModules.version"/>
        </th>
        <th>
            <fmt:message key="serverSettings.manageModules.details"/>
        </th>
        <th>
            <fmt:message key="serverSettings.manageModules.download"/>
        </th>
    </tr>
    </thead>

    <tbody>
    <c:forEach items="${requestScope.modules}" var="module">
        <tr>
            <td><c:if test="${not empty module.icon}"><img width="100" height="100" src="${module.icon}"/></c:if>${module.title}</td>
            <td> ${module.name}</td>
            <td> ${module.version}</td>
            <c:url value="${module.remoteUrl}" context="/" var="remoteUrl"/>
            <td>
                <button class="btn btn-info" type="button" onclick='window.location.assign("${remoteUrl}")'>
                    <i class="icon-zoom-in icon-white"></i>
                    &nbsp;${i18nModuleDetails}
                </button>

            <td>

            <c:choose>

            <c:when test="${not empty allModuleVersions[module.name] and functions:contains(allModuleVersions[module.name],module.version)}">
                Already installed
            </c:when>
            <%--<c:when test="${not empty allModuleVersions[module.name]}">--%>
                <%--Other versions installed--%>
            <%--</c:when>--%>
            <c:otherwise>
                <form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
                    <input type="hidden" name="forgeId" value="${module.forgeId}"/>
                    <input type="hidden" name="moduleUrl" value="${module.downloadUrl}"/>
                    <button class="btn btn-block button-download" type="submit" name="_eventId_installForgeModule">
                        <i class="icon-download"></i>
                        &nbsp;<fmt:message key="serverSettings.manageModules.download"/>
                    </button>
                </form>
            </c:otherwise>
            </c:choose>

            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>