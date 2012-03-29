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
<template:addResources type="css" resources="contribute.min.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="timepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="jquery-ui.min.js"/>
<template:addResources type="javascript"
                       resources="jquery.treeview.min.js,jquery.treeview.async.jahia.js,jquery.fancybox.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>



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
<div id="currentUserTasks${currentNode.identifier}">
<c:if test="${currentResource.workspace eq 'live'}">
    <script type="text/javascript">
        $('#currentUserTasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>');
    </script>
</c:if>

<c:if test="${currentResource.workspace ne 'live'}">
    <c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

    <c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
        <jcr:node var="user" path="${renderContext.user.localPath}"/>
    </c:if>

    <form name="myform" method="post">
        <input type="hidden" name="jcrNodeType" value="jnt:task">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>">
        <input type="hidden" name="jcrNewNodeOutputFormat" value="<c:url value='${renderContext.mainResource.template}.html'/>">
        <input type="hidden" name="state">
    </form>

    <script type="text/javascript">
        var ready = true;
        <c:choose>
            <c:when test="${not empty modeDispatcherId}">
                <c:url  var="reloadurl" value="${url.basePreview}${currentNode.parent.path}.html.ajax">
                    <c:forEach items="${param}" var="p">
                        <c:param name="${p.key}" value="${p.value}"/>
                    </c:forEach>
                </c:url>
                <c:set var="identifierName" value="#${modeDispatcherId}"/>
            </c:when>
            <c:otherwise>
                <c:url  var="reloadurl" value="${url.basePreview}${currentNode.path}.html.ajax">
                    <c:forEach items="${param}" var="p">
                        <c:param name="${p.key}" value="${p.value}"/>
                    </c:forEach>
                </c:url>
                <c:set var="identifierName" value="#currentUserTasks${currentNode.identifier}"/>
            </c:otherwise>
        </c:choose>
        function sendNewStatus(uuid, task, state, finalOutcome) {
            if (ready) {
                ready = false;
                $(".taskaction-complete").addClass("taskaction-disabled");
                $(".taskaction").addClass("taskaction-disabled");
                $.post('<c:url value="${url.base}"/>' + task, {"jcrMethodToCall":"put","state":state,"finalOutcome":finalOutcome,"form-token":document.forms['tokenForm_' + uuid].elements['form-token'].value}, function() {
                    $('${identifierName}').load('${reloadurl}',null,function() {
                        $("#taskdetail_"+uuid).css("display","block");
                    });
                }, "json");
            }
        };
        function sendNewAssignee(uuid, task, key) {
            if (ready) {
                ready = false;
                $(".taskaction-complete").addClass("taskaction-disabled");
                $(".taskaction").addClass("taskaction-disabled");
                $.post('<c:url value="${url.base}"/>' + task, {"jcrMethodToCall":"put","state":"active","assigneeUserKey":key,"form-token":document.forms['tokenForm_' + uuid].elements['form-token'].value}, function() {
                    $('${identifierName}').load('${reloadurl}',null,function(){
                        $("#taskdetail_"+uuid).css("display","block");
                    });
                }, "json");
            }
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
    <template:include view="hidden.header"/>
    <div id="tasklist">
        <div id="${user.UUID}">

            <c:set value="${currentNode.properties['displayState'].boolean}" var="dispState"/>
            <c:set value="${currentNode.properties['displayDueDate'].boolean}" var="dispDueDate"/>
            <c:set value="${currentNode.properties['displayAssignee'].boolean}" var="dispAssignee"/>
            <c:set value="${currentNode.properties['displayCreator'].boolean}" var="dispCreator"/>

            <table width="100%" class="table tableTasks" summary="Tasks">
                <colgroup>
                    <col span="1" width="${100 - (dispAssignee?15:0)- (dispCreator?15:0)- (dispState?10:0)- (dispDueDate?15:0) }%" class="col1"/>
                    <c:if test="${dispAssignee}"><col span="1" width="15%" class="col2"/></c:if>
                    <c:if test="${dispCreator}"><col span="1" width="15%" class="col3"/></c:if>
                    <c:if test="${dispState}"><col span="1" width="10%" class="col4"/></c:if>
                    <c:if test="${dispDueDate}"><col span="1" width="15%" class="col5"/></c:if>
                </colgroup>
                <thead>
                <tr>
                    <th id="Title" scope="col"><fmt:message key="mix_title.jcr_title"/></th>
                    <c:if test="${dispAssignee}"><th id="Assigned" scope="col"><fmt:message key="jnt_task.assignee"/></th></c:if>
                    <c:if test="${dispCreator}"><th id="CreatedBy" scope="col"><fmt:message key="mix_createdBy.jcr_createdBy"/></th></c:if>
                    <c:if test="${dispState}"><th id="State" class="center" scope="col"><fmt:message key="jnt_task.state"/></th></c:if>
                    <c:if test="${dispDueDate}"><th id="DueDate" scope="col"><fmt:message key="jnt_task.dueDate"/></th></c:if>
                </tr>
                </thead>

                <tbody>
                    <c:forEach items="${moduleMap.currentList}"  var="task" varStatus="status" begin="${moduleMap.begin}" end="${moduleMap.end}">
                        <tr class="${status.count % 2 == 0 ? 'odd' : 'even'}">
                            <td headers="Title">
                                <span class="icon-task icon-task-${task.properties['priority'].string}"></span>&nbsp;<a href="javascript:void(0)">${fn:escapeXml(task.properties['jcr:title'].string)}</a><span class="opentask" onclick="switchDisplay('${task.identifier}')"><fmt:message key="label.showTask"/></span>

                                <div style="display:none;" class="taskdetail" id="taskdetail_${task.identifier}">
                                    <p class="task-info-p"><fmt:message key="label.createdBy"/>: ${task.properties['jcr:createdBy'].string}, <fmt:message key="label.createdOn"/> <fmt:formatDate value="${task.properties['jcr:created'].date.time}" dateStyle="long" type="both"/></p>
                                    <c:if test="${not empty task.properties['priority']}"><p class="task-priority-p"><fmt:message key="jnt_task.priority"/>: <span class="task-priority task-${task.properties['priority'].string}">${task.properties['priority'].string}</span></p></c:if>
                                    <c:if test="${not empty task.properties['description']}"><p class="task-text">${task.properties['description'].string}</p></c:if>
                                    <template:tokenizedForm>
                                        <form id="tokenForm_${task.identifier}" name="tokenform_${task.identifier}" method="post" action="<c:url value='${url.base}'/>${task.path}">
                                        </form>
                                    </template:tokenizedForm>
                                    <ul class="taskactionslist">
                                        <c:set var="assignable" value="true" />
                                        <c:if test="${not empty task.properties['candidates'] and task.properties['assigneeUserKey'].string ne user.name}">
                                            <c:set var="assignable" value="false" />
                                            <c:set var="candidates" value=""/>
                                            <c:forEach items="${task.properties['candidates']}" var="candidate">
                                                <c:set var="candidates" value=" ${candidate.string} ${candidates} "/>
                                            </c:forEach>
                                            <c:set var="userKey" value="u:${user.name}" />
                                            <c:if test="${fn:contains(candidates, userKey)}">
                                                <c:set var="assignable" value="true" />
                                            </c:if>
                                            <c:if test="${not assignable}">
                                                <c:set var="groups" value="${jcr:getUserMembership(user)}" />
                                                <c:forEach items="${groups}" var="x">
                                                    <c:if test="${fn:contains(candidates, x.key)}">
                                                        <c:set var="assignable" value="true" />
                                                    </c:if>
                                                </c:forEach>
                                            </c:if>
                                        </c:if>
                                        <c:choose>
                                            <c:when test="${task.properties.state.string == 'active' and task.properties['assigneeUserKey'].string ne user.name and assignable eq 'true'}">
                                                <li><a class="taskaction taskaction-assign" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','${user.name}')" title="assign to me"><fmt:message key="label.actions.assigneToMe"/></a></li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'active' and task.properties['assigneeUserKey'].string eq user.name}">
                                                <li><a class="taskaction taskaction-refuse" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','')" title="Refuse"><fmt:message key="label.actions.refuse"/></a></li>
                                                <li><a class="taskaction taskaction-start" href="javascript:sendNewStatus('${task.identifier}','${task.path}','started')" title="start"><fmt:message key="label.actions.start"/></a></li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'started' and task.properties['assigneeUserKey'].string eq user.name}">
                                                <li><a class="taskaction taskaction-refuse" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','')" title="Refuse"><fmt:message key="label.actions.refuse"/></a></li>
                                                <li><a class="taskaction taskaction-suspend" href="javascript:sendNewStatus('${task.identifier}','${task.path}','suspended')" title="suspend"><fmt:message key="label.actions.suspend"/></a></li>
                                                <fmt:setBundle basename="${task.properties['taskBundle'].string}" var="taskBundle"/>
                                                <c:if test="${not empty task.properties['targetNode'].node}">
                                                    <li><a class="taskaction taskaction-preview" target="_blank" href="<c:url value="${url.basePreview}${task.properties['targetNode'].node.path}.html"/>"><fmt:message key="label.preview"/></a></li>
                                                </c:if>
                                                <c:if test="${not empty task.properties['possibleOutcomes']}">
                                                <c:forEach items="${task.properties['possibleOutcomes']}" var="outcome" varStatus="status">
                                                    <li><a class="taskaction taskaction-start" href="javascript:sendNewStatus('${task.identifier}','${task.path}','finished','${outcome.string}')" title="${outcome.string}"><fmt:message bundle="${taskBundle}" key="${fn:replace(task.properties['taskName'].string,' ','.')}.${fn:replace(outcome.string,' ','.')}"/></a></li>
                                                </c:forEach>
                                                </c:if>
                                                <c:if test="${empty task.properties['possibleOutcomes']}">
                                                    <c:set var="taskId" value="${task.identifier}"/>
                                                    <li class="taskactions-right"><div class="taskaction-complete"><input class="completeTaskAction" taskPath="<c:url value='${url.base}${currentNode.path}'/>" type="checkbox" id="btnComplete-${taskId}" onchange="sendNewStatus('${taskId}','${task.path}','finished')"/>&nbsp;<label for="btnComplete-${taskId}"><fmt:message key="label.actions.completed"/></label></div></li>
                                                </c:if>
                                                <jcr:node var="taskData" path="${task.path}/taskData"/>
                                                <c:if test="${not empty taskData}">
                                                    <script>
                                                        initEditFields('${taskData.identifier}');
                                                    </script>
                                                    <template:module path="${task.path}/taskData" view="contribute.edit" />
                                                 </c:if>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'finished'}">
                                                <li class="taskactions-right"><div class="taskaction-complete"><input name="Completed" type="checkbox" disabled="disabled" checked="checked" value="Completed" />&nbsp;<fmt:message key="label.actions.completed"/></div></li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'suspended' and task.properties['assigneeUserKey'].string eq user.name}">
                                                <li><a class="taskaction taskaction-refuse" href="javascript:sendNewAssignee('${task.identifier}','${task.path}','')" title="Refuse"><fmt:message key="label.actions.refuse"/></a></li>
                                                <li><a class="taskaction taskaction-continue" href="javascript:sendNewStatus('${task.identifier}','${task.path}','started')" title="start"><fmt:message key="label.actions.resume"/></a></li>
                                            </c:when>
                                            <c:when test="${task.properties.state.string == 'canceled'}">
                                            </c:when>
                                        </c:choose>
                                        <c:if test="${not empty task.properties['dueDate']}"><li class="taskactions-right"><a class="taskaction taskaction-iCalendar" href="<c:url value='${url.base}${task.path}.ics'/>" title="iCalendar"><fmt:message key="label.actions.icalendar"/></a></li></c:if>
                                    </ul>
                                </div>
                            </td>
                            <c:if test="${dispAssignee}"><td headers="Assigned">${task.properties['assigneeUserKey'].string}</td></c:if>
                            <c:if test="${dispCreator}"><td headers="CreatedBy">${task.properties['jcr:createdBy'].string}</td></c:if>
                            <c:if test="${dispState}"><td class="center" headers="State">
                                <span class="task-status task-status-${task.properties.state.string}"><fmt:message key="jnt_task.state.${task.properties.state.string}"/></span>

                            </td></c:if>
                            <c:if test="${dispDueDate}"><td headers="DueDate"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                                               dateStyle="medium" timeStyle="short" type="both"/></td></c:if>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <div class="clear"></div>
    </div>
    <template:include view="hidden.footer"/>

</c:if>
</div>
