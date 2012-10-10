<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.ISODateTimeFormat" %>
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
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
    <jsp:useBean id="now" class="java.util.Date"/>
    <%
        DateTime dt = new DateTime();
        DateTime startDate = dt.minusDays(14);
        DateTime endDate = dt.plusDays(7);
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        pageContext.setAttribute("startDate", fmt.print(startDate));
        pageContext.setAttribute("endDate", fmt.print(endDate));
        pageContext.setAttribute("currentDate", fmt.print(dt));
        pageContext.setAttribute("startDateDate", startDate.toDate());
        pageContext.setAttribute("endDateDate", endDate.toDate());
        pageContext.setAttribute("currentDateDate", dt.toDate());
    %>
    <c:set value="" var="todayDisplayed" scope="request"/>
    <jcr:node var="user" path="${renderContext.user.localPath}"/>
    <c:set value=" and (((task.assigneeUserKey is null or task.assigneeUserKey='') and (task.candidates is null or task.candidates='u:${functions:sqlencode(user.name)}' "
           var="sql"/>
    <c:forEach items="${user:getUserMembership(user)}" var="membership">
        <c:set value="${sql} or task.candidates='g:${functions:sqlencode(membership.key)}'" var="sql"/>
    </c:forEach>
    <c:set value="${sql} )) or task.assigneeUserKey='${functions:sqlencode(user.name)}')" var="sql"/>
    <query:definition var="listQuery"
                      statement="select * from [jnt:task] as task where isdescendantnode(task,['${functions:sqlencode(bindedComponent)}']) and
                      (([dueDate] is not null and [dueDate] > '${startDate}' and [dueDate] <= '${currentDate}' and state <> 'finished')
                      or ([dueDate] is not null and [dueDate] > '${currentDate}' and [dueDate] <= '${endDate}'))
                      ${sql} order by [dueDate]"/>
    <jcr:jqom var="result" qomBeanName="listQuery"/>

    <ul class="scheduletasks">
        <li class="scheduletask now" date="${now.time}">
            <fmt:message key="label.upcoming"/>
        </li>
        <c:set var="emptyTasks" value="true"/>
        <c:forEach items="${result.nodes}" var="task">
            <c:set var="emptyTasks" value="false"/>
            <li class="${task.properties['type'].string} scheduletask ${task.properties['state'].string eq 'finished' ? 'finishedTask' : 'unfinishedTask'}"
                date="${task.properties['dueDate'].date.time.time}">
        <span class="date value"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                                 pattern="dd/MM/yyyy"/></span>
                <c:set value="${jcr:findDisplayableNode(task, renderContext)}" var="displayableNode"/>
                <c:choose>
                    <c:when test="${displayableNode.path ne renderContext.mainResource.node.path}">
                        <span class="value"><a
                                href="${url.base}${displayableNode.path}.html">${task.properties['jcr:title'].string}</a></span>
                    </c:when>
                    <c:otherwise>
                        <span class="value">${task.properties['jcr:title'].string}</span>
                    </c:otherwise>
                </c:choose>
            </li>
        </c:forEach>

        <workflow:workflowForPath path="${renderContext.mainResource.node.path}/%" var="test"/>
        <c:forEach items="${test}" var="process">
            <jcr:node var="node" uuid="${process.nodeId}"/>
            <c:url value="${url.base}${node.path}.html" var="link"/>
            <c:if test="${not process.completed}">
                <workflow:workflow id="${process.processId}" provider="${process.provider}" var="active"/>
                <c:forEach items="${active.availableActions}" var="task">
                    <c:if test="${not empty task.dueDate and task.dueDate.time ge startDateDate.time and task.dueDate.time le endDateDate.time}">
                        <c:set var="emptyTasks" value="false"/>
                        <fmt:formatDate pattern="dd/MM/yyyy"
                                        value="${task.dueDate}"
                                        var="endDate"/>
                        <li class="scheduletask workflowtask unfinishedTask" date="${task.dueDate.time}">
                            <span class="date value">${endDate}</span>
                            <span class="value"><a href="${link}"><span
                                    class="task-${fn:replace(task.name,' ','_')}">${task.displayName}</span>
                                - ${node.name}</a></span>
                        </li>
                    </c:if>
                </c:forEach>
            </c:if>
            <workflow:workflowHistory var="history" workflowId="${process.processId}"
                                      workflowProvider="${process.provider}"/>
            <c:forEach items="${history}" var="task">
                <c:if test="${not empty task.endTime and task.endTime.time ge currentDateDate.time  and task.endTime.time le endDateDate.time}">
                    <jsp:useBean id="historyData" class="java.util.LinkedHashMap"/>
                    <fmt:formatDate pattern="dd/MM/yyyy"
                                    value="${task.endTime}"
                                    var="endDate"/>
                    <li class="scheduletask workflowtask finishedTask" date="${task.endTime.time}">
                        <span class="date value">${endDate}</span>
                        <span class="value"><a href="${link}"><span
                                class="task-${fn:replace(task.outcome,' ','_')}">${task.displayOutcome}</span>
                            - ${node.name}</a></span>
                    </li>
                </c:if>
            </c:forEach>
        </c:forEach>
        <c:if test="${emptyTasks eq true}">
            <li class="scheduleTasks"><span class=value><fmt:message key="label.upcoming.no.tasks"/></span></li>
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
