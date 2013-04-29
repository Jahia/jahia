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
            $('#${currentNode.identifier}-next').click(workInProgress)
        });
    </script>
</template:addResources>

    <form action="${flowExecutionUrl}" method="POST">
        <h2><fmt:message key="serverSettings.manageWebProjects.createWebProject"/></h2>
        <table class="table table-bordered table-striped table-hover">
            <tbody>
                <tr>
                    <td style="width: 30%">
                        <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>:&nbsp;
                    </td>
                    <td style="width: 70%">
                        ${siteBean.siteKey}
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="label.name"/>:&nbsp;
                    </td>
                    <td>
                        ${fn:escapeXml(siteBean.title)}
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/>:&nbsp;
                    </td>
                    <td>
                        ${fn:escapeXml(siteBean.serverName)}
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="label.description"/>:&nbsp;
                    </td>
                    <td>
                        ${fn:escapeXml(siteBean.description)}
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/>:&nbsp;
                    </td>
                    <td>
                        ${siteBean.defaultSite}
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/>:&nbsp;
                    </td>
                    <td>
                        ${siteBean.templateSetPackage.name}&nbsp;(${siteBean.templateSet})
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="label.modules"/>:&nbsp;
                    </td>
                    <td>
                        <p style="line-height: 2em">
                            <c:forEach items="${siteBean.modulePackages}" var="module" varStatus="loopStatus">
                                <span class="badge badge-info">${module.name}</span>
                            </c:forEach>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="serverSettings.manageWebProjects.webProject.language"/>:&nbsp;
                    </td>
                    <td>
                        ${siteBean.language}
                    </td>
                </tr>

            </tbody>
        </table>
        <input class="btn btn-primary" type="submit" name="_eventId_next" id="${currentNode.identifier}-next" value="<fmt:message key='label.next'/>"/>
        <input class="btn" type="submit" name="_eventId_previous" value="<fmt:message key='label.previous'/>"/>
    </form>
