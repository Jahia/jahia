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
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="javascript" resources="jquery.js,admin-bootstrap.js"/>
<jcr:node path="/users/root" var="adminUser"/>
<h2><fmt:message key="serverSettings.adminProperties"/></h2>

<c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
    <div class="${msg.severity == 'ERROR' ? 'validationError' : ''} alert ${msg.severity == 'ERROR' ? 'alert-error' : 'alert-success'}"><button type="button" class="close" data-dismiss="alert">&times;</button>${fn:escapeXml(msg.text)}</div>
</c:forEach>
<div class="box-1">
    <form:form modelAttribute="adminProperties" class="form" autocomplete="off">
        <h3><fmt:message key="label.username"/>:&nbsp;${adminUser.name}</h3>
        <div class="container-fluid">
            <div class="row-fluid">
                <div class="span4">
                    <label for="firstName"><fmt:message key="label.firstName"/></label>
                    <form:input class="span12" type="text" id="firstName" path="firstName"/>
                    <label for="email"><fmt:message key="label.email"/></label>
                    <form:input class="span12" type="text" id="email" path="email"/>
                </div>
                <div class="span4">
                    <label for="lastName"><fmt:message key="label.lastName"/></label>
                    <form:input class="span12" type="text" id="lastName" path="lastName"/>
                    <label for="organization"><fmt:message key="label.organization"/></label>
                    <form:input type="text" class="span12" id="organization" path="organization" autocomplete="off"/>
                </div>
            </div>

            <div class="row-fluid">
                <div class="span4">
                    <label for="emailNotifications"><fmt:message key="serverSettings.user.emailNotifications"/></label>
                    <form:checkbox id="emailNotifications" path="emailNotificationsDisabled" />
                </div>
                <div class="span4">
                    <label for="preferredLanguage"><fmt:message key="serverSettings.user.preferredLanguage"/></label>
                    <select class="span12" id="preferredLanguage" name="preferredLanguage" size="1">
                        <c:forEach items="${functions:availableAdminBundleLocale(renderContext.UILocale)}" var="uiLanguage">
                            <option value="${uiLanguage}" <c:if test="${uiLanguage eq adminProperties.preferredLanguage}">selected="selected" </c:if>>${functions:displayLocaleNameWith(uiLanguage, renderContext.UILocale)}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="row-fluid">
                <div class="span4">
                    <label for="password"><fmt:message key="label.password"/></label>
                    <form:input class="span12" type="password" id="password" path="password" autocomplete="off"/>
                    <span class="text-info">(<fmt:message key="serverSettings.user.edit.password.no.change"/>)</span>
                </div>
                <div class="span4">
                    <label for="passwordConfirm"><fmt:message key="label.confirmPassword"/></label>
                    <form:input type="password" class="span12" id="passwordConfirm" path="passwordConfirm" autocomplete="off"/>
                    <span class="text-info">(<fmt:message key="serverSettings.user.edit.password.no.change"/>)</span>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span12" style="margin-top:15px;">
                    <button class="btn btn-primary" id="submit" type="submit" name="_eventId_submit"><i class="icon-ok icon-white"></i>&nbsp;<fmt:message key='label.save'/></button>
                </div>
            </div>
        </div>

    </form:form>
</div>
    <hr/>
<fieldset id="groupsFields" title="<fmt:message key="serverSettings.user.groups.list"/>">
    <div class="container-fluid">
        <div class="row-fluid">
            <div class="span12">
                <label for="groupsFields"><fmt:message key="serverSettings.user.groups.list"/></label>
                <select class="span12 fontfix" name="selectMember" size="6" multiple>
                    <c:forEach items="${userGroups}" var="group">
                        <option value="${user:formatUserValueOption(group)}">${user:formatUserTextOption(group, 'Name, 20;SiteTitle, 15;Properties, 20')}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
    </div>
</fieldset>
