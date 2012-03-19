<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="task" type="org.jahia.services.workflow.WorkflowTask"--%>
<template:addResources type="css" resources="tasks.css"/>
<template:addResources type="css" resources="contentlist.css"/>

<template:addResources type="javascript" resources="tasks.js"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="css" resources="timepicker.css"/>
<c:if test="${currentResource.workspace eq 'live'}">
    <form name="myform" method="post">
        <input type="hidden" name="jcrNodeType" value="jnt:task">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>">
        <input type="hidden" name="jcrNewNodeOutputFormat" value="<c:url value='${renderContext.mainResource.template}.html'/>">
        <input type="hidden" name="state">
    </form>

    <div id="currentUserTasks${currentNode.identifier}">
        <script type="text/javascript">
            $('#currentUserTasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>');
        </script>
    </div>
</c:if>

<c:if test="${currentResource.workspace ne 'live'}">
    <c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

    <c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
        <jcr:node var="user" path="${renderContext.user.localPath}"/>
    </c:if>

    <c:if test="${not renderContext.ajaxRequest}">
        <form name="myform" method="post">
            <input type="hidden" name="jcrNodeType" value="jnt:task">
            <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>">
            <input type="hidden" name="jcrNewNodeOutputFormat" value="<c:url value='${renderContext.mainResource.template}.html'/>">
            <input type="hidden" name="state">
        </form>
    </c:if>

    <script type="text/javascript">
        function sendNewStatus(uuid, task, state) {
            $.post('<c:url value="${url.base}"/>' + task, {"jcrMethodToCall":"put","state":state}, function() {
                $('#currentUserTasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>',null,function(){
                    $("#taskdetail_"+uuid).css("display","block");
                });
            }, "json");
        };
        function sendNewAssignee(uuid, task, key) {
            $.post('<c:url value="${url.base}"/>' + task, {"jcrMethodToCall":"put","state":"active","assigneeUserKey":key}, function() {
                $('#currentUserTasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>',null,function(){
                    $("#taskdetail_"+uuid).css("display","block");
                });
            }, "json");
        };

        function switchDisplay(identifier) {
            $(".taskdetail").each(function () {
                if (!$(this).is("#taskdetail_" + identifier)) {
                    $(this).slideUp("medium");
                }
            });
            $("#taskdetail_" + identifier).slideToggle("medium");
        }

    </script>
    <div id="tasklist">
        <div id="${user.UUID}">


            <table width="100%" class="table tableTasks" summary="Tasks">
                <colgroup>
                    <col span="1" width="60%" class="col1"/>
                    <col span="1" width="10%" class="col2"/>
                    <col span="1" width="10%" class="col3"/>
                    <col span="1" width="10%" class="col4"/>
                    <col span="1" width="10%" class="col5"/>
                </colgroup>
                <thead>
                <tr>
                    <th id="Title" scope="col"><fmt:message key="mix_title.jcr_title"/></th>
                    <th id="State" class="center" scope="col"><fmt:message key="jnt_task.state"/></th>
                    <th id="DueDate" scope="col"><fmt:message key="jnt_task.dueDate"/></th>
                    <th id="Assigned" scope="col"><fmt:message key="jnt_task.assignee"/></th>
                    <th id="CreatedBy" scope="col"><fmt:message key="mix_createdBy.jcr_createdBy"/></th>
                </tr>
                </thead>

                <tbody>


                <c:if test="${currentNode.properties['viewUserTasks'].boolean}">
                    <template:include view="hidden.load"/>
                    <c:set var="listQuery" value="${moduleMap.listQuery}"/>
                    <jcr:jqom var="tasks" qomBeanName="listQuery"/>

                    <c:set var="nodes" value="${tasks.nodes}"/>
                    <%--<c:set value="${jcr:getNodes(currentNode,'jnt:task')}" var="tasks"/>--%>

                    <c:forEach items="${nodes}" var="task" varStatus="status">
                        <tr class="${status.count % 2 == 0 ? 'odd' : 'even'}">
                            <td headers="Title">
                                <span class="icon-task-high"></span>&nbsp;<a href="javascript:void(0)">${fn:escapeXml(task.properties['jcr:title'].string)}</a><span class="opentask" onclick="switchDisplay('${task.identifier}')">Show task</span>

                                <div style="display:none;" class="taskdetail" id="taskdetail_${task.identifier}">
                                    <p class="task-info-p">Created by: ${task.properties['jcr:createdBy'].string}, on <fmt:formatDate value="${task.properties['jcr:created'].date.time}" dateStyle="long" type="date"/></p>
                                    <p class="task-priority-p">Importance: <span class="task-priority task-${task.properties['priority'].string}">${task.properties['priority'].string}</span></p>
                                    <p class="task-text">${task.properties['description'].string}</p>
                                    <ul class="taskactionslist">
                                        <c:choose>
                                            <c:when test="${task.properties.state.string == 'active' and task.properties['assigneeUserKey'].string ne user.name}">
                                                <li><a class="taskactionslist taskactionslist-assign" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','${user.name}')" title="assign to me">Assign to me</a></li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'active' and task.properties['assigneeUserKey'].string eq user.name}">
                                                <li><a class="taskactionslist taskactionslist-refuse" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','')" title="Refuse">Refuse</a></li>
                                                <li><a class="taskactionslist taskactionslist-start" href="javascript:sendNewStatus('${task.identifier}','${task.path}','started')" title="start">Start</a></li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'started' and task.properties['assigneeUserKey'].string eq user.name}">
                                                <li><a class="taskactionslist taskactionslist-refuse" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','')" title="Refuse">Refuse</a></li>
                                                <li><a class="taskactionslist taskactionslist-suspend" href="javascript:sendNewStatus('${task.identifier}','${task.path}','suspended')" title="suspend">Suspend</a></li>
                                                <li class="taskactions-right"><input class="completeTaskAction" taskPath="<c:url value="${url.base}${currentNode.path}"/>" type="checkbox" onchange="sendNewStatus('${task.identifier}','${task.path}','finished')"/>&nbsp;Completed</li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'finished'}">
                                                <li class="taskactions-right"><input name="Completed" type="checkbox" disabled="disabled" checked="checked" value="Completed" />&nbsp;Completed</li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'suspended' and task.properties['assigneeUserKey'].string eq user.name}">
                                                <li><a class="taskactionslist taskactionslist-refuse" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','')" title="Refuse">Refuse</a></li>
                                                <li><a class="taskactionslist taskactionslist-continue" href="javascript:sendNewStatus('${task.identifier}','${task.path}','started')" title="start">Resume</a></li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'canceled'}">
                                            </c:when>
                                        </c:choose>
                                    </ul>
                                </div>
                            </td>
                            <td class="center" headers="State">
                                <span class="task-status task-status-${task.properties.state.string}">${task.properties.state.string}</span>

                            </td>
                            <td headers="DueDate"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                                               dateStyle="short" type="date"/></td>
                            <td headers="Assigned">${task.properties['assigneeUserKey'].string}</td>
                            <td headers="CreatedBy">${task.properties['jcr:createdBy'].string}</td>
                        </tr>
                    </c:forEach>
                </c:if>
                <c:if test="${currentNode.properties['viewWorkflowTasks'].boolean}">
                    <jcr:nodeProperty node="${currentNode}" name="workflowTypes" var="workflowTypes"/>
                    <workflow:tasksForNode var="wfTasks" user="${renderContext.user}"/>
                    <c:forEach items="${wfTasks}" var="task" varStatus="status">
                        <workflow:workflow id="${task.processId}" provider="${task.provider}" var="wf"/>
                        <c:set var="found" value="false"/>

                        <c:forEach items="${workflowTypes}" var="type">
                            <c:set var="currentType">${wf.workflowDefinition.provider}:${wf.workflowDefinition.key}</c:set>
                            <c:if test="${type.string eq currentType}">
                                <c:set var="found" value="true"/>
                            </c:if>

                        </c:forEach>

                        <c:if test="${empty workflowTypes or found}">
                            <jcr:node var="node" uuid="${task.variables.nodeId}"/>
                            <c:if test="${node != null}">
                            <tr class="${((status.count + 1)) % 2 == 0 ? 'odd' : 'even'}">
                                <td headers="Title">
                                    <a href="javascript:void(0)">${fn:escapeXml(not empty task.displayName ? task.displayName : task.name)}</a>
                                    <span class="opentask" onclick="switchDisplay('${task.id}')">Show task</span>

                                    <div style="display:none;" class="taskdetail" id="taskdetail_${task.id}">
                                        <c:set var="path" value="${jcr:findDisplayableNode(node, renderContext).path}"/>
                                        <c:if test="${not empty path}">
                                            <c:url var="preview" value="${renderContext.servletPath}/${task.variables.workspace}/${task.variables.locale}${path}.html"/>
                                            <a target="_blank" href="${preview}">${fn:escapeXml(task.variables['jcr:title'][0].value)}</a>
                                        </c:if>
                                        <c:if test="${empty path}">
                                            ${fn:escapeXml(task.variables['jcr:title'][0].value)}
                                        </c:if>
                                        <%--<p class="task-info-p">Created by: ${task.properties['jcr:createdBy'].string}, on <fmt:formatDate value="${task.properties['jcr:created'].date.time}" dateStyle="long" type="date"/></p>--%>
                                        <%--<p class="task-priority-p">Importance: <span class="task-priority task-${task.properties['priority'].string}">${task.properties['priority'].string}</span></p>--%>
                                        <%--<p class="task-text">${task.properties['description'].string}</p>--%>
                                        <ul class="taskactionslist">
                                            <c:choose>
                                                <c:when test="${not empty task.formResourceName}">
                                                    <li><a class="taskactionslist taskactionslist-${task.name}" href="javascript:$('#taskrow${node.identifier}-${task.id}').toggle('fast');">${task.name}</a></li>
                                                    <li><a class="taskactionslist taskactionslist-preview" href="javascript:window.open('${preview}','<fmt:message key="label.preview"/>')"><fmt:message key='label.preview'/></a></li>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${task.outcomes}" var="outcome">
                                                        <li><a class="taskactionslist taskactionslist-${outcome}" href="javascript:executeTask('${node.path}', '${task.provider}:${task.id}', '${outcome}', '<c:url value="${url.base}"/>', '${currentNode.UUID}', '<c:url value="${url.current}.ajax"/>','window.location=window.location;')" title="${outcome}">${outcome}</a></li>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </ul>
                                    </div>
                                </td>
                                <td class="center" headers="State">
                                    <span class="task-status task-status-started">started</span>
                                </td>
                                <td headers="DueDate">
                                    <%--<fmt:formatDate value="${task.properties['dueDate'].date.time}"--%>
                                                                   <%--dateStyle="short" type="date"/>--%>
                                </td>
                                <td headers="Assigned">
                                <%--${task.properties['assigneeUserKey'].string}--%>
                                </td>
                                <td headers="CreatedBy">
                                <%--${task.properties['jcr:createdBy'].string}--%>
                                </td>
                            </tr>
                            <c:if test="${not empty task.formResourceName}">
                                <tr class="${((status.count + 1)) % 2 == 0 ? 'odd' : 'even'} " id="taskrow${node.identifier}-${task.id}"  style="display:none;">
                                    <td colspan="4">
                                        <div id="task${node.identifier}-${task.id}" class="taskformdiv">
                                            <c:set var="workflowTaskFormTask" value="${task}" scope="request"/>
                                            <c:url value="${url.current}.ajax" var="myUrl"/>
                                            <template:include view="contribute.workflow">
                                                <template:param name="resourceNodeType" value="${task.formResourceName}"/>
                                                <template:param name="workflowTaskForm" value="${task.provider}:${task.id}"/>
                                                <template:param name="workflowTaskFormTaskName" value="${task.name}"/>
                                                <template:param name="workflowTaskFormCallbackId" value="${currentNode.UUID}"/>
                                                <template:param name="workflowTaskFormCallbackURL" value="${myUrl}"/>
                                                <template:param name="workflowTaskFormCallbackJS"
                                                                value=""/>
                                                <template:param name="workflowTaskNodeUuid" value="${node.identifier}"/>
                                            </template:include>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                                </c:if>
                        </c:if>
                    </c:forEach>
                </c:if>

                </tbody>
            </table>
        </div>
        <div class="clear"></div>
    </div>
</c:if>