<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="tasks.js"/>

<c:set var="user" value="${currentNode.properties['assigneeUserKey'].string}"/>
<c:if test="${empty user}">
    <c:set var="user" value="${currentNode.properties['jcr:createdBy'].string}"/>
</c:if>

<c:set value="${currentNode.properties['state'].string eq 'finished'}" var="finished"/>
<div class="taskComment${finished ? ' taskCommentResolved' : ''}">
        <jcr:jqom var="result"
                  statement="select * from [jnt:user] as u where localname(u)='${functions:sqlencode(user)}'"/>
        <c:forEach items="${result.nodes}" var="usernode">
            <div>
                <template:module node="${usernode}" view="profile"/>
            </div>
        </c:forEach>
<p class="taskTitle">
    <span class="value">${currentNode.properties['jcr:title'].string}</span>
</p>
<p class="taskdate value"><fmt:formatDate value="${currentNode.properties['dueDate'].date.time}" pattern="dd/MM/yyyy"/></p>
<p class="taskDescription">
    ${currentNode.properties['description'].string}
</p>

<c:set value="${jcr:hasPermission(currentNode,'jcr:modifyProperties')}" var="canchange"/>
<p class="taskaction" >Resolved : <input class="completeTaskAction" taskPath="<c:url value="${url.base}${currentNode.path}"/>" type="checkbox" ${finished ? ' checked="true"':''} ${finished or not canchange?' disabled="true"':''} onchange="completeTask($(this))"></p>

</div>
