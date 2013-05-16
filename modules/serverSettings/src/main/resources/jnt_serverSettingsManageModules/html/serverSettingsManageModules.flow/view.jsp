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
<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js,jquery.blockUI.js,bootstrap-filestyle.min.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>

<template:addResources>
<script type="text/javascript">
    $(document).ready(function() {
        $('.button-download').click(workInProgress)
    });
    $(document).ready(function () {
        $(":file").filestyle({classButton: "btn",classIcon: "icon-folder-open"/*,buttonText:"Translation"*/});
    });
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
            <label for="moduleFileUpload"><fmt:message key="serverSettings.manageModules.upload.module"/></label>
            <input type="file" id="moduleFileUpload" name="moduleFile" accept=""/>
            <button class="btn btn-primary" type="submit" name="_eventId_upload">
                <i class="icon-download icon-white"></i>
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
    <c:choose>
        <c:when test="${isStudio}">
            <c:forEach items="${allModuleVersions}" var="entry">
                <%@include file="currentModuleVars.jspf" %>
                <%@include file="modulesTableRow.jspf" %>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <c:forEach items="${allModuleVersions}" var="entry">
                <%@include file="currentModuleVars.jspf" %>
                <c:if test="${!isMandatoryDependency && sourcesDownloadable}">
                    <%@include file="modulesTableRow.jspf" %>
                </c:if>
            </c:forEach>
            <c:forEach items="${allModuleVersions}" var="entry">
                <%@include file="currentModuleVars.jspf" %>
                <c:if test="${!isMandatoryDependency && !sourcesDownloadable}">
                    <%@include file="modulesTableRow.jspf" %>
                </c:if>
            </c:forEach>
            <c:forEach items="${allModuleVersions}" var="entry">
                <%@include file="currentModuleVars.jspf" %>
                <c:if test="${isMandatoryDependency}">
                    <%@include file="modulesTableRow.jspf" %>
                </c:if>
            </c:forEach>
        </c:otherwise>
    </c:choose>
    </tbody>
</table>
<c:if test="${not isStudio}">
<p><a id="mandatory-dependency">&nbsp;</a><span class="text-error"><strong>*</strong></span>&nbsp;-&nbsp;${i18nMandatoryDependency}</p>
</c:if>