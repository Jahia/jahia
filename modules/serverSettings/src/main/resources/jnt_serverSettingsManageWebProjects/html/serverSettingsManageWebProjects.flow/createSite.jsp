<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
    <div class="validationError">
        <ul>
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <li>${fn:escapeXml(error.text)}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<form id="createSiteForm" action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>

    <fieldset>
        <div>
            <label for="title"><fmt:message key="label.name"/> * </label>
            <input id="title" name="title" value="${fn:escapeXml(siteBean.title)}"/>
        </div>

        <div>
            <label for="serverName"><fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/> * </label>
            <input id="serverName" name="serverName" value="${fn:escapeXml(siteBean.serverName)}"/>
        </div>

        <div>
            <label for="siteKey"><fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/> * </label>
            <input id="siteKey" name="siteKey" value="${fn:escapeXml(siteBean.siteKey)}"/>
        </div>

        <div>
            <label for="description"><fmt:message key="label.description"/></label>
            <textarea id="description" name="description">${fn:escapeXml(siteBean.description)}</textarea>
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

    <input type="submit" name="_eventId_cancel" value="<fmt:message key='label.cancel' />"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='label.next'/>"/>
</form>
