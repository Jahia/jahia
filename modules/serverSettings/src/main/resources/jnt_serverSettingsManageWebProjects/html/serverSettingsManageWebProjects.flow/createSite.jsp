<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<form action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>

    <fieldset>
        <div>
            <label for="title"><fmt:message key="serverSettings.manageWebProjects.webProject.title"/></label>
            <input id="title" name="title" value="${siteBean.title}"/>
        </div>

        <div>
            <label for="serverName"><fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/></label>
            <input id="serverName" name="serverName" value="${siteBean.serverName}"/>
        </div>

        <div>
            <label for="siteKey"><fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/></label>
            <input id="siteKey" name="siteKey" value="${siteBean.siteKey}"/>
        </div>

        <div>
            <label for="description"><fmt:message key="serverSettings.manageWebProjects.webProject.description"/></label>
            <textarea id="description" name="description">${siteBean.description}</textarea>
        </div>

        <div>
            <label for="defaultSite"><fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/></label>
            <input type="checkbox" name="defaultSite" id="defaultSite" <c:if test="${siteBean.defaultSite}">checked="checked"</c:if> />
            <input type="hidden" name="_defaultSite"/>
        </div>

        <div>
            <label for="createAdmin"><fmt:message key="serverSettings.manageWebProjects.webProject.createAdmin"/></label>
            <input type="checkbox" name="createAdmin" id="createAdmin" <c:if test="${siteBean.createAdmin}">checked="checked"</c:if> />
            <input type="hidden" name="_createAdmin"/>
        </div>
    </fieldset>

    <input type="submit" name="_eventId_next" value="<fmt:message key='label.next'/>"/>
</form>
