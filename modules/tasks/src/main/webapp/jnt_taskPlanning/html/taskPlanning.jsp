<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
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
<template:addResources type="javascript" resources="jquery.sortElements.js"/>
<c:if test="${empty param['bindedComponent']}">
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent').path}"/>
</c:if>
<c:if test="${not empty param['bindedComponent']}">
<c:set var="bindedComponent" value="${param['bindedComponent']}"/>
</c:if>
<c:if test="${currentResource.workspace eq 'live'}">
    <div id="currentUserTasks${currentNode.identifier}">
        <script type="text/javascript">
            $('#currentUserTasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax?bindedComponent=${bindedComponent}"/>');
        </script>
    </div>
</c:if>
<c:if test="${currentResource.workspace ne 'live'}">
<c:set value="" var="todayDisplayed" scope="request"/>
<query:definition var="listQuery"
                  statement="select * from [jnt:task] as t where isdescendantnode(t,['${bindedComponent}']) and [dueDate] is not null order by [dueDate]"/>
<jcr:jqom var="result" qomBeanName="listQuery"/>

<jsp:useBean id="now" class="java.util.Date"/>

<ul class="planningtasks" >
<li class="planningtask now" date="${now.time}">
   Today
</li>
<c:forEach items="${result.nodes}" var="task">
    <c:choose>
        <c:when test="${task.properties['state'].string eq 'finished'}">
            <li class="planningtask finishedTask" date="${task.properties['dueDate'].date.time.time}">
                <span class="date value"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                                    pattern="dd/MM/yyyy"/></span>
                <span class="value">${task.properties['jcr:title'].string}</span>
            </li>
        </c:when>
        <c:otherwise>
            <li class="planningtask unfinishedTask" date="${task.properties['dueDate'].date.time.time}">
                <span class="date value"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                                    pattern="dd/MM/yyyy"/></span>
                <span class="value">${task.properties['jcr:title'].string}</span>
            </li>
        </c:otherwise>
    </c:choose>
</c:forEach>

<workflow:workflowForPath path="${renderContext.mainResource.node.path}/%" var="test"/>
<c:forEach items="${test}" var="process">
    <jcr:node var="node" uuid="${process.nodeId}"/>
    <c:url value="${url.base}${node.path}.html" var="link"/>
    <c:if test="${not process.completed}">
        <workflow:workflow id="${process.processId}" provider="${process.provider}" var="active"/>
        <c:forEach items="${active.availableActions}" var="task">
            <c:if test="${not empty task.dueDate}">
                <fmt:formatDate pattern="dd/MM/yyyy"
                                value="${task.dueDate}"
                                var="endDate"/>
                <li class="planningtask unfinishedTask" date="${task.dueDate.time}">
                    <span class="date value">${endDate}</span>
                    <span class="value">${task.displayName} - ${node.name}</span>
                </li>
            </c:if>
        </c:forEach>
    </c:if>
    <workflow:workflowHistory var="history" workflowId="${process.processId}"
                              workflowProvider="${process.provider}"/>
    <c:forEach items="${history}" var="task">
        <c:if test="${not empty task.endTime}">
            <jsp:useBean id="historyData" class="java.util.LinkedHashMap"/>
            <fmt:formatDate pattern="dd/MM/yyyy"
                            value="${task.endTime}"
                            var="endDate"/>
            <li class="planningtask finishedTask" date="${task.endTime.time}">
                <span class="date value">${endDate}</span>
                <span class="value">${task.displayName} - ${node.name}</span>
            </li>
        </c:if>
    </c:forEach>
</c:forEach>
</ul>
<script>
    $(document).ready(function() {
        $('.planningtask').sortElements(function(a, b){
            return $(a).attr('date') > $(b).attr('date') ? 1 : -1;
        });
    });
</script>
</c:if>
