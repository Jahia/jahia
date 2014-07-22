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
<%--@elvariable id="searchSettings" type="org.jahia.services.search.SearchSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,bootstrapSwitch.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css,bootstrapSwitch.css"/>


<h2>
    <fmt:message key="serverSettings.searchServerSettings"/>
</h2>
<p>
<c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
    <c:if test="${message.severity eq 'ERROR'}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
    </c:if>
</c:forEach>
</p>
<c:if test="${settingsUpdated}">
    <div class="alert alert-success">
        <button type="button" class="close" data-dismiss="alert">&times;</button>
        <fmt:message key="label.changeSaved"/>
    </div>
</c:if>
<div class="box-1">
    <form class="form-horizontal" name="jahiaAdmin" action='${flowExecutionUrl}' method="post">
        <div class="control-group">
            <label class="control-label" for="currentProvider"><fmt:message key="serverSettings.searchServerSettings.provider"/>&nbsp;:</label>
            <div class="controls">
                <select name="currentProvider" id="currentProvider">
                    <c:forEach items="${availableProviders}" var="availableProvider">
                        <option value="${fn:escapeXml(availableProvider)}" <c:if test="${availableProvider eq searchSettings.currentProvider}">selected="selected"</c:if> >${fn:escapeXml(availableProvider)}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="control-group">
            <div class="controls">
                <button class="btn btn-primary" type="submit" name="_eventId_submit"><i class="icon-ok icon-white"></i>&nbsp;<fmt:message key="label.save"/></button>
            </div>
        </div>
    </form>
</div>
