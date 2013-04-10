<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.js,bootstrap.js"/>
<jcr:node path="/users/root" var="adminUser"/>
<h2><fmt:message key="serverSettings.adminProperties"/></h2>

<c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
    <div class="${msg.severity == 'ERROR' ? 'validationError' : ''} alert ${msg.severity == 'ERROR' ? 'alert-error' : 'alert-success'}"><button type="button" class="close" data-dismiss="alert">&times;</button>${fn:escapeXml(msg.text)}</div>
</c:forEach>
<form:form modelAttribute="adminProperties" class="form">
    <h3><fmt:message key="label.username"/>:&nbsp;${adminUser.name}</h3>
    <div class="container-fluid">
        <div class="row-fluid">
            <div class="span6">
                <label for="firstName"><fmt:message key="label.firstName"/></label>
                <form:input type="text" id="firstName" path="firstName"/>
                <label for="email"><fmt:message key="label.email"/></label>
                <form:input type="text" id="email" path="email"/>
            </div>
            <div class="span6">
                <label for="lastName"><fmt:message key="label.lastName"/></label>
                <form:input type="text" id="lastName" path="lastName"/>
                <label for="organization"><fmt:message key="label.organization"/></label>
                <form:input type="text" id="organization" path="organization" autocomplete="off"/>
            </div>
        </div>

        <div class="row-fluid">
            <div class="span6">
                <label for="emailNotifications"><fmt:message key="serverSettings.user.emailNotifications"/></label>
                <form:checkbox id="emailNotifications" path="emailNotificationsDisabled" />
            </div>
            <div class="span6">
                <label for="preferredLanguage"><fmt:message key="serverSettings.user.preferredLanguage"/></label>
                <select id="preferredLanguage" name="preferredLanguage" size="1">
                    <c:forEach items="${functions:availableAdminBundleLocale(renderContext.UILocale)}" var="uiLanguage">
                        <option value="${uiLanguage}" <c:if test="${uiLanguage eq adminProperties.preferredLanguage}">selected="selected" </c:if>>${functions:displayLocaleNameWith(uiLanguage, renderContext.UILocale)}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="row-fluid">
            <div class="span6">
                <label for="password"><fmt:message key="label.password"/></label>
                <form:input type="password" id="password" path="password" autocomplete="off"/>
                (<fmt:message key="serverSettings.user.edit.password.no.change"/>)
            </div>
            <div class="span6">
                <label for="passwordConfirm"><fmt:message key="label.confirmPassword"/></label>
                <form:input type="password" id="passwordConfirm" path="passwordConfirm" autocomplete="off"/>
                (<fmt:message key="serverSettings.user.edit.password.no.change"/>)
            </div>
        </div>
        <div class="row-fluid">
            <div class="span12 ">
                <input class="btn btn-primary" id="submit" type="submit" value="<fmt:message key='label.save'/>" name="_eventId_submit">
            </div>
        </div>
    </div>

</form:form>
    <hr/>
    <h3><fmt:message key="serverSettings.user.groupList"/>:</h3>
    <p>
        <c:forEach items="${adminProperties.groups}" var="group">
            <span class="badge badge-info">${fn:escapeXml(group.groupname)}</span>
        </c:forEach>
    </p>
