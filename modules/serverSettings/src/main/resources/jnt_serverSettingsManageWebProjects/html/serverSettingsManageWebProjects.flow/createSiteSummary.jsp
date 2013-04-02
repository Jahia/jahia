<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.blockUI.js"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#${currentNode.identifier}-next').click(function() {
                $.blockUI({ css: {
                    border: 'none',
                    padding: '15px',
                    backgroundColor: '#000',
                    '-webkit-border-radius': '10px',
                    '-moz-border-radius': '10px',
                    opacity: .5,
                    color: '#fff'
                }, message: '<fmt:message key="org.jahia.admin.workInProgressTitle"/>' });
            });
        });
    </script>
</template:addResources>

<form action="${flowExecutionUrl}" method="POST">
    <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>

    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>:&nbsp;
        ${siteBean.siteKey}
    </div>
    <div>
        <fmt:message key="label.name"/>:&nbsp;
        ${fn:escapeXml(siteBean.title)}
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/>:&nbsp;
        ${fn:escapeXml(siteBean.serverName)}
    </div>
    <div>
        <fmt:message key="label.description"/>:&nbsp;
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
        <fmt:message key="label.modules"/>:&nbsp;
        <c:forEach items="${siteBean.modulePackages}" var="module" varStatus="loopStatus">
            ${module.name}&nbsp;(${module.rootFolder})${!loopStatus.last ? ',&nbsp;' : ''}
        </c:forEach>
    </div>
    <div>
        <fmt:message key="serverSettings.manageWebProjects.webProject.language"/>:&nbsp;
        ${siteBean.language}
    </div>


    <input type="submit" name="_eventId_previous" value="<fmt:message key='label.previous'/>"/>
    <input type="submit" name="_eventId_next" id="${currentNode.identifier}-next" value="<fmt:message key='label.next'/>"/>
</form>
