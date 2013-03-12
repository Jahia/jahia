<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<jcr:node path="/users/root" var="adminUser"/>
<h2><fmt:message key="label.adminProperties"/></h2>
<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
    <div class="validationError">
        <ul>
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <li>${error.text}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>
<form action="${flowExecutionUrl}" method="post" class="form">
    <h3><fmt:message key="label.login"/>:&nbsp;${adminUser.name}</h3>
    <label for="firstName"><fmt:message key="label.firstName"/></label>
    <input type="text" id="firstName" value="${adminProperties.firstName}" name="firstName"/>  <br/>

    <label for="lastName"><fmt:message key="label.lastName"/></label>
    <input type="text" id="lastName" value="${adminProperties.lastName}" name="lastName"/> <br/>
    <label for="email"><fmt:message key="label.email"/></label>
    <input type="text" id="email" value="${adminProperties.email}" name="email"/> <br/>
    <label for="organization"><fmt:message key="label.organization"/></label>
    <input type="text" id="organization" value="${adminProperties.organization}" name="organization" autocomplete="off"/>   <br/>
    <label for="emailNotifications"><fmt:message key="label.emailNotifications"/></label>
    <input type="checkbox" id="emailNotifications" name="emailNotificationsDisabled" ${adminProperties.emailNotificationsDisabled?" checked":""}/>
    <input type="hidden" name="_emailNotificationsDisabled">
    <br/>
    <label for="password"><fmt:message key="label.password"/></label>
    <input type="password" id="password" name="password" autocomplete="off"/>    <br/>
    <label for="confirmPassword"><fmt:message key="label.confirmPassword"/></label>
    <input type="password" id="confirmPassword" name="confirmPassword" autocomplete="off"/>
    <br/>

    <input id="submit" type="submit" value="ok" name="_eventId_submit">
</form>
<h3><fmt:message key="label.groups"/> : <br/></h3>
<ul>
    <c:forEach items="${jcr:getUserMembership(adminUser)}" var="group">
        <li>${group.value.groupname}</li>
    </c:forEach>
</ul>

