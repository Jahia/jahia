<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
    <div class="validationError">
        <ul>
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <li>${error.text}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<form action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>
    <fieldset>
        <div>
            <label for="userName"><fmt:message key="label.username"/></label>
            <input type="text" id="userName" value="${siteBean.adminProperties.userName}" name="userName"/>
        </div>

        <div>
            <label for="firstName"><fmt:message key="label.firstName"/></label>
            <input type="text" id="firstName" value="${siteBean.adminProperties.firstName}" name="firstName"/>
        </div>
        <div>
            <label for="lastName"><fmt:message key="label.lastName"/></label>
            <input type="text" id="lastName" value="${siteBean.adminProperties.lastName}" name="lastName"/>
        </div>
        <div>
            <label for="email"><fmt:message key="label.email"/></label>
            <input type="text" id="email" value="${siteBean.adminProperties.email}" name="email"/>
        </div>
        <div>
            <label for="organization"><fmt:message key="label.organization"/></label>
            <input type="text" id="organization" value="${siteBean.adminProperties.organization}" name="organization" />
        </div>

        <div>
            <label for="password"><fmt:message key="label.password"/></label>
            <input type="password" id="password" name="password" autocomplete="off"/>
        </div>

        <div>
            <label for="confirmPassword"><fmt:message key="label.confirmPassword"/></label>
            <input type="password" id="confirmPassword" name="confirmPassword" autocomplete="off"/>
        </div>

    </fieldset>

    <input type="submit" name="_eventId_previous" value="<fmt:message key='label.previous'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='label.next'/>"/>
</form>
