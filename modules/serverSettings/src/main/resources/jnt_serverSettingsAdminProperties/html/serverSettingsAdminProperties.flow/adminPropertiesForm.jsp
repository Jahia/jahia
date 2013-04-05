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

<jcr:node path="/users/root" var="adminUser"/>
<h2><fmt:message key="serverSettings.adminProperties"/></h2>
<c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
    <div class="${msg.severity == 'ERROR' ? 'validationError' : ''}" style="color: ${msg.severity == 'ERROR' ? 'red' : 'blue'};">${fn:escapeXml(msg.text)}</div>
</c:forEach>
<form:form modelAttribute="adminProperties" class="form">
    <h3><fmt:message key="label.username"/>:&nbsp;${adminUser.name}</h3>
    <label for="firstName"><fmt:message key="label.firstName"/></label>
    <form:input type="text" id="firstName" path="firstName"/>  <br/>

    <label for="lastName"><fmt:message key="label.lastName"/></label>
    <form:input type="text" id="lastName" path="lastName"/> <br/>
    <label for="email"><fmt:message key="label.email"/></label>
    <form:input type="text" id="email" path="email"/> <br/>
    <label for="organization"><fmt:message key="label.organization"/></label>
    <form:input type="text" id="organization" path="organization" autocomplete="off"/>   <br/>
    <label for="emailNotifications"><fmt:message key="serverSettings.user.emailNotifications"/></label>
    <form:checkbox id="emailNotifications" path="emailNotificationsDisabled" />
    <br/>
    <label for="preferredLanguage"><fmt:message key="serverSettings.user.preferredLanguage"/></label>
    <select id="preferredLanguage" name="preferredLanguage" size="1">
        <c:forEach items="${functions:availableAdminBundleLocale(renderContext.UILocale)}" var="uiLanguage">
            <option value="${uiLanguage}" <c:if test="${uiLanguage eq adminProperties.preferredLanguage}">selected="selected" </c:if>>${functions:displayLocaleNameWith(uiLanguage, renderContext.UILocale)}</option>
        </c:forEach>
    </select>
    <br/>
    <label for="password"><fmt:message key="label.password"/></label>
    <form:input type="password" id="password" path="password" autocomplete="off"/>
    (<fmt:message key="serverSettings.user.edit.password.no.change"/>)
    <br/>
    <label for="passwordConfirm"><fmt:message key="label.confirmPassword"/></label>
    <form:input type="password" id="passwordConfirm" path="passwordConfirm" autocomplete="off"/>
    (<fmt:message key="serverSettings.user.edit.password.no.change"/>)
    <br/>

    <input id="submit" type="submit" value="<fmt:message key='label.save'/>" name="_eventId_submit">
</form:form>
<h3><fmt:message key="serverSettings.user.groupList"/>:</h3>
<ul>
    <c:forEach items="${adminProperties.groups}" var="group">
        <li>${fn:escapeXml(group.groupname)}</li>
    </c:forEach>
</ul>
