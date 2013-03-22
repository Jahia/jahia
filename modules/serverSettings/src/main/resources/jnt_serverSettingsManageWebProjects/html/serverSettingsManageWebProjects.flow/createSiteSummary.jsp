<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>

    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>:&nbsp;
        ${siteBean.siteKey}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.title"/>:&nbsp;
        ${fn:escapeXml(siteBean.title)}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/>:&nbsp;
        ${fn:escapeXml(siteBean.serverName)}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.description"/>:&nbsp;
        ${fn:escapeXml(siteBean.description)}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/>:&nbsp;
        ${siteBean.defaultSite}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/>:&nbsp;
        ${siteBean.templateSetPackage.name}&nbsp;(${siteBean.templateSet})
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.modules"/>:&nbsp;
        <c:forEach items="${siteBean.modulePackages}" var="module" varStatus="loopStatus">
            ${module.name}&nbsp;(${module.rootFolder})${!loopStatus.last ? ',&nbsp;' : ''}
        </c:forEach>
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.language"/>:&nbsp;
        ${siteBean.language}
    </div>


    <input type="submit" name="_eventId_previous" value="<fmt:message key='label.previous'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='label.next'/>"/>
</form>
