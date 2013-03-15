<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<form action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>

    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/> :
        ${siteBean.siteKey}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.title"/> :
        ${siteBean.title}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/> :
        ${siteBean.serverName}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.description"/> :
        ${siteBean.description}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/> :
        ${siteBean.defaultSite}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/> :
        ${siteBean.templateSet}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.modules"/> :
        <c:forEach items="${siteBean.modules}" var="module">
            ${module} &nbsp;
        </c:forEach>
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.language"/> :
        ${siteBean.language}
    </div>


    <input type="submit" name="_eventId_previous" value="<fmt:message key='label.previous'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='label.next'/>"/>
</form>
