<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<div>
    <p>
        <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
            <c:if test="${message.severity eq 'ERROR'}">
                <span style="color: red;">${message.text}</span><br/>
            </c:if>
            <c:if test="${message.severity eq 'INFO'}">
                <span style="color: green;">${message.text}</span><br/>
            </c:if>
        </c:forEach>
    </p>
    <form action="${flowExecutionUrl}" method="post" enctype="multipart/form-data">
        <fieldset>
            <label for="csvFile"><fmt:message key="label.csvFile"/></label>
            <input type="file" name="csvFile" id="csvFile"/><br/>
            <label for="csvSeparator"><fmt:message key="label.csvSeparator"/></label>
            <input type="text" name="csvSeparator" value="${csvFile.csvSeparator}" id="csvSeparator"/>
        </fieldset>
        <fieldset>
            <input type="submit" name="_eventId_confirm" value="<fmt:message key='label.ok'/>"/>
            <input type="submit" name="_eventId_cancel" value="<fmt:message key='label.cancel'/>"/>
        </fieldset>
    </form>
    <p>
        <fmt:message key="serverSettings.users.batch.file.format"/>
    </p>
</div>