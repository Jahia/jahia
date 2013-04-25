<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<%--@elvariable id="webprojectHandler" type="org.jahia.modules.serversettings.flow.WebprojectHandler"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.blockUI.js,bootstrap.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources>
<script type="text/javascript">
    $(document).ready(function() {
        $('#${currentNode.identifier}-processImport').click(function() {
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
</template:addResources>

    <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
        <c:if test="${message.severity eq 'ERROR'}">
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                    ${message.text}
            </div>
        </c:if>
    </c:forEach>

<form action="${flowExecutionUrl}" method="post">
        <div class="box-1">
            <c:forEach items="${webprojectHandler.importsInfos}" var="importInfoMap">
                    <label for="${importInfoMap.key}">
                        <input type="checkbox" id="${importInfoMap.key}" name="importsInfos['${importInfoMap.key}'].selected" value="true"
                               <c:if test="${importInfoMap.value.selected}">checked="checked"</c:if>/> ${importInfoMap.key}
                        <input type="hidden" id="${importInfoMap.key}" name="_importsInfos['${importInfoMap.key}'].selected"/>
                    </label>
                    <%@include file="importValidation.jspf"%>
                    <c:if test="${importInfoMap.value.site}">
                        <div class="container-fluid">
                            <div class="row-fluid">
                                <div class="span6">
                                    <label for="${importInfoMap.value.siteKey}siteTitle">
                                        <fmt:message key="serverSettings.manageWebProjects.webProject.title"/>
                                    </label>
                                    <input type="text" id="${importInfoMap.value.siteKey}siteTitle"
                                           name="importsInfos['${importInfoMap.key}'].siteTitle"
                                           value="${fn:escapeXml(importInfoMap.value.siteTitle)}"/>
                                </div>
                                <div class="span6">
                                    <label for="${importInfoMap.value.siteKey}siteServerName">
                                        <fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/>
                                    </label>
                                    <input type="text" id="${importInfoMap.value.siteKey}siteServerName"
                                           name="importsInfos['${importInfoMap.key}'].siteServername"
                                           value="${fn:escapeXml(importInfoMap.value.siteServername)}"/>
                                </div>
                            </div>
                            <div class="row-fluid">
                                <div class="span6">
                                    <label for="${importInfoMap.value.siteKey}siteKey">
                                        <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>
                                    </label>
                                    <input type="text" id="${importInfoMap.value.siteKey}siteKey"
                                           name="importsInfos['${importInfoMap.key}'].siteKey"
                                           value="${fn:escapeXml(importInfoMap.value.siteKey)}"/>
                                </div>
                                <div class="span6">
                                    <label for="${importInfoMap.value.siteKey}templates">
                                        <fmt:message key="serverSettings.webProjectSettings.pleaseChooseTemplateSet"/>
                                    </label>
                                    <select id="${importInfoMap.value.siteKey}templates" name="importsInfos['${importInfoMap.key}'].templates">
                                        <c:forEach items="${requestScope.templateSets}" var="module">
                                            <option value="${module}" <c:if test="${importInfoMap.value.templates eq module}"> selected="selected"</c:if>>${module}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <c:if test="${importInfoMap.value.legacyImport}">
                                <div class="row-fluid">
                                    <div class="span6">
                                        <label for="${importInfoMap.value.siteKey}legacyMapping">
                                            <fmt:message key="serverSettings.manageWebProjects.selectDefinitionMapping"/>
                                        </label>
                                        <select id="${importInfoMap.value.siteKey}legacyMapping"
                                                name="importsInfos['${importInfoMap.key}'].selectedLegacyMapping">
                                            <c:forEach items="${importInfoMap.value.legacyMappings}" var="module">
                                                <option value="${module.absolutePath}" <c:if
                                                        test="${importInfoMap.value.selectedLegacyMapping eq module.name}"> selected="selected"</c:if>>${module.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="span6">
                                        <label for="${importInfoMap.value.siteKey}legacyDefinitions">
                                            <fmt:message key="serverSettings.manageWebProjects.selectLegacyDefinitions"/>
                                        </label>
                                        <select id="${importInfoMap.value.siteKey}legacyDefinitions"
                                                name="importsInfos['${importInfoMap.key}'].selectedLegacyDefinitions">
                                            <c:forEach items="${importInfoMap.value.legacyDefinitions}" var="module">
                                                <option value="${module.absolutePath}" <c:if
                                                        test="${importInfoMap.value.selectedLegacyDefinitions eq module.name}"> selected="selected"</c:if>>${module.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </c:if>
            </c:forEach>
        <input class="btn btn-primary" type="submit" name="_eventId_processImport" id="${currentNode.identifier}-processImport" value="<fmt:message key='label.next'/>"/>
    </div>
</form>

