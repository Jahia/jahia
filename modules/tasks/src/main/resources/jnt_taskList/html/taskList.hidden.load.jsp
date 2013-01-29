<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="tasks.css"/>
<template:addResources type="javascript" resources="tasks.js"/>
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

    <c:if test="${not empty currentNode.properties['filterOnTypes']}">
        <query:definition var="listQuery"
                          statement="select * from [jnt:task] as t where isdescendantnode(t,['${functions:sqlencode(bindedComponent)}']) and t.type='${currentNode.properties['filterOnTypes'].string}' order by [jcr:created] desc"/>
    </c:if>
    <c:if test="${empty currentNode.properties['filterOnTypes']}">
        <query:definition var="listQuery"
                          statement="select * from [jnt:task] as t where isdescendantnode(t,['${functions:sqlencode(bindedComponent)}']) order by [jcr:created] desc"/>
    </c:if>
    <c:set target="${moduleMap}" property="listQuery" value="${listQuery}"/>
    <c:set target="${moduleMap}" property="subNodesView" value="taskList"/>
</c:if>
<script type="text/javascript">
    function sendNewStatus(uuid, task, state, finalOutcome, reloadurl) {
        $(".taskaction-complete").addClass("taskaction-disabled");
        $(".taskaction").addClass("taskaction-disabled");
        $.post('<c:url value="${url.base}"/>' + task,
                {"jcrMethodToCall":"put", "state":state, "finalOutcome":finalOutcome, "form-token":document.forms['tokenForm_' +
                                                                                                                  uuid].elements['form-token'].value},
                function () {
                    $('#task_' + uuid).load(encodeURI(reloadurl), null, function () {
                        $(".taskaction-complete").removeClass("taskaction-disabled");
                        $(".taskaction").removeClass("taskaction-disabled");
                    });
                }, "json");
    }
    ;
    function sendNewAssignee(uuid, task, key, reloadurl) {
        $(".taskaction-complete").addClass("taskaction-disabled");
        $(".taskaction").addClass("taskaction-disabled");
        $.post('<c:url value="${url.base}"/>' + task,
                {"jcrMethodToCall":"put", "state":"active", "assigneeUserKey":key, "form-token":document.forms['tokenForm_' +
                                                                                                               uuid].elements['form-token'].value},
                function () {
                    $('#task_' + uuid).load(encodeURI(reloadurl), null, function () {
                        $(".taskaction-complete").removeClass("taskaction-disabled");
                        $(".taskaction").removeClass("taskaction-disabled");
                    });
                }, "json");
    }
    ;

</script>