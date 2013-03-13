<%--@elvariable id="userProperties" type="org.jahia.modules.serversettings.users.management.UserProperties"--%>
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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<div>
    <span style="font-size: larger;">${userProperties.displayName}</span>

    <p>
        <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
            <c:if test="${message.severity eq 'ERROR'}">
                <span style="color: red;">${message.text}</span><br/>
            </c:if>
        </c:forEach>
    </p>

    <form action="${flowExecutionUrl}" method="post">
        <fieldset>
            <label for="firstName"><fmt:message key="label.firstName"/></label>
            <input name="firstName" id="firstName" value="${userProperties.firstName}"><br/>
            <label for="lastName"><fmt:message key="label.lastName"/></label>
            <input name="lastName" id="lastName" value="${userProperties.lastName}"><br/>
            <label for="email"><fmt:message key="label.email"/></label>
            <input name="email" id="email" value="${userProperties.email}"><br/>
            <label for="organization"><fmt:message key="label.organization"/></label>
            <input name="organization" id="organization" value="${userProperties.organization}"><br/>
        </fieldset>
        <fieldset>
            <label for="password"><fmt:message key="label.password"/></label>
            <input type="password" name="password" id="password" value=""><br/>
            <label for="passwordConfirm"><fmt:message key="label.confirmPassword"/></label>
            <input type="password" name="passwordConfirm" id="passwordConfirm" value=""><br/>
        </fieldset>
        <fieldset>
            <input type="submit" name="_eventId_update" value="<fmt:message key='label.update'/>"/>
        </fieldset>
    </form>
</div>