<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="tasks.css"/>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.sortElements.js"/>
<%--
This resources are needed by the ajax loaded content
--%>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<c:choose>
<c:when test="${jcr:isNodeType(renderContext.mainResource.node, 'jnt:user') or param.user != null}">
    <c:if test="${currentResource.workspace eq 'live'}">
        <div id="currentUserTasks${currentNode.identifier}">
            <script type="text/javascript">
                $('#currentUserTasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"><c:param name="user" value="${renderContext.mainResource.node.name}"/></c:url>');
            </script>
        </div>
    </c:if>

    <c:if test="${currentResource.workspace ne 'live'}">
        <c:set var="user" value="${renderContext.mainResource.node.name}"/>
        <c:if test="${param.user !=null}">
            <c:set var="user" value="${param.user}"/>
        </c:if>
        <c:set var="taskType" value="task"/>

        <c:set value="" var="todayDisplayed" scope="request"/>
        <query:definition var="listQuery"
                          statement="select * from [jnt:task] as t where [type] = '${taskType}' and [jcr:createdBy] = '${functions:sqlencode(user)}' and [dueDate] is not null order by [dueDate]"/>
        <jcr:jqom var="result" qomBeanName="listQuery"/>

        <jsp:useBean id="now" class="java.util.Date"/>
        <jsp:useBean id="workflowTaskList" class="java.util.LinkedHashMap"/>
        <c:set var="emptyTasks" value="true"/>
        <ul class="scheduletasks">
            <li class="scheduletask now" date="${now.time}">
                <fmt:message key="label.upcoming" />
            </li>
            <c:forEach items="${result.nodes}" var="task">
                <c:set var="emptyTasks" value="false"/>
                <li class="${task.properties['type'].string} scheduletask ${task.properties['state'].string eq 'finished' ? 'finishedTask' : 'unfinishedTask'}" date="${task.properties['dueDate'].date.time.time}">
                    <span class="date value"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                                             pattern="dd/MM/yyyy"/></span>
                    <c:set value="${jcr:findDisplayableNode(task, renderContext)}" var="displayableNode"/>
                    <c:choose>
                        <c:when test="${displayableNode.path ne renderContext.mainResource.node.path}">
                            <span class="value"><a href="${url.base}${displayableNode.path}.html">${task.properties['jcr:title'].string}</a></span>
                        </c:when>
                        <c:otherwise>
                            <span class="value">${task.properties['jcr:title'].string}</span>
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:forEach>
            <query:definition var="listQuery"
                              statement="select * from [docmix:docspace]"/>
            <jcr:jqom var="result" qomBeanName="listQuery"/>


            <c:forEach items="${result.nodes}" var="ds" varStatus="vs1">
                <workflow:workflowForPath path="${ds.path}/%" var="test"/>
                <c:forEach items="${test}" var="process" varStatus="vs2">
                    <c:set target="${workflowTaskList}" value="${process}" property="a${vs1.count}b${vs2.count}"/>
                </c:forEach>
            </c:forEach>

            <c:forEach items="${workflowTaskList}" var="process">
                <jcr:node var="node" uuid="${process.value.nodeId}"/>
                <c:url value="${url.base}${node.path}.html" var="link"/>
                <c:if test="${not process.value.completed}">
                    <workflow:workflow id="${process.value.processId}" provider="${process.value.provider}"
                                       var="active"/>
                    <c:forEach items="${active.availableActions}" var="task">
                        <fmt:formatDate pattern="dd/MM/yyyy"
                                        value="${task.dueDate}"
                                        var="endDate"/>
                        <c:set var="emptyTasks" value="false"/>
                        <li class="scheduletask workflowtask unfinishedTask" date="${task.dueDate.time}">
                            <span class="value">${endDate}</span>
                            <span class="value">${task.displayName} - ${node.name}</span>
                            <template:module node="${node}" view="workflowMonitor" editable="false">
                                <template:param name="workflowType" value="docspace"/>
                                <template:param name="showHistory" value="false"/>
                                <template:param name="task" value="${task.name}"/>
                            </template:module>
                        </li>
                    </c:forEach>
                </c:if>
            </c:forEach>
            <c:if test="${emptyTasks}">
                <li class="scheduleTasks"><span class=value><fmt:message key="label.upcoming.no.tasks" /></span></li>
            </c:if>

        </ul>
        <script>
            $(document).ready(function () {
                $('.scheduletask').sortElements(function (a, b) {
                    return $(a).attr('date') > $(b).attr('date') ? 1 : -1;
                });
            });
        </script>
    </c:if>
</c:when>
<c:otherwise>
    This view can only work is main resource is jnt:user (actually ${renderContext.mainResource.node.primaryNodeType.name}) or if a "user" param is set
</c:otherwise>
</c:choose>
