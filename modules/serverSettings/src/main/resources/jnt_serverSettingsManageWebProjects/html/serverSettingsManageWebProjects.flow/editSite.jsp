<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>EDIT SITE</h1>

<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
    <div class="validationError">
        <ul>
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <li>${fn:escapeXml(error.text)}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<form action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>

    <fieldset>
        <div>
            <label><fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>:</label>
            ${fn:escapeXml(selectedSite.siteKey)}
        </div>

        <div>
            <label for="title"><fmt:message key="serverSettings.manageWebProjects.webProject.title"/>*:</label>
            <input id="title" name="title" value="${fn:escapeXml(siteBean.title)}"/>
        </div>

        <div>
            <label for="serverName"><fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/>*:</label>
            <input id="serverName" name="serverName" value="${fn:escapeXml(siteBean.serverName)}"/>
        </div>

        <div>
            <label><fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/>:</label>
            ${fn:escapeXml(selectedSite.templatePackageName)}&nbsp;(${fn:escapeXml(selectedSite.templateFolder)})
        </div>

        <div>
            <label><fmt:message key="serverSettings.manageWebProjects.webProject.modules"/>:</label>
            <c:forEach items="${siteBean.modulePackages}" var="module" varStatus="loopStatus">
                ${module.name}&nbsp;(${module.rootFolder})${!loopStatus.last ? ',&nbsp;' : ''}
            </c:forEach>
        </div>

        <div>
            <label for="defaultSite"><fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/>:</label>
            <c:choose>
                <c:when test="${siteBean.defaultSite}">
                    <fmt:message key="serverSettings.manageWebProjects.webProject.isDefault"/>
                    <input type="hidden" name="defaultSite" value="true"/>
                </c:when>
                <c:otherwise>
                    <input type="checkbox" name="defaultSite" id="defaultSite" ${siteBean.defaultSite ? 'checked="checked"' : ''}/>
                </c:otherwise>
            </c:choose>
        </div>

        <div>
            <label for="description"><fmt:message key="serverSettings.manageWebProjects.webProject.description"/>:</label>
            <textarea id="description" name="description">${fn:escapeXml(siteBean.description)}</textarea>
        </div>
    </fieldset>

    <input type="submit" name="_eventId_cancel" value="<fmt:message key='label.cancel' />"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='label.save'/>"/>
</form>
