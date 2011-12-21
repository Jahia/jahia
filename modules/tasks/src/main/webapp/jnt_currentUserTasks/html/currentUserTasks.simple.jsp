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

<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>

<template:addResources type="javascript" resources="jquery.form.js"/>

<template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${currentResource.locale}.js"/>
<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="css" resources="timepicker.css"/>
<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="${renderContext.user.localPath}"/>
</c:if>


<form name="myform" method="post">
    <input type="hidden" name="jcrNodeType" value="jnt:task">
    <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${currentNode.path}.html${ps}'/>">
    <input type="hidden" name="state">
</form>


<script type="text/javascript">
    function send(task, state) {
        form = document.forms['myform'];
        form.action = '<c:url value="${url.base}"/>' + task;
        form.elements.state.value = state;
        form.submit();
    }
</script>
<div id="tasklist">
    <div id="${user.UUID}">

        <ul>

            <jcr:sql var="tasks"
                     sql="select * from [jnt:task] as task where task.assignee='${user.identifier}'"/>
            <c:set var="nodes" value="${tasks.nodes}"/>
            <%--<c:set value="${jcr:getNodes(currentNode,'jnt:task')}" var="tasks"/>--%>

            <c:if test="${currentNode.properties['viewUserTasks'].boolean}">
                <c:forEach items="${nodes}" var="task"
                           begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
                    <li>
                        <a href="<c:url value='${url.base}${task.path}.html'/>>">${fn:escapeXml(task.propertiesAsString['jcr:title'])}</a>

                        <c:choose>
                            <c:when test="${task.propertiesAsString.state == 'active'}">
                                <span><img alt="" src="<c:url value='${url.currentModule}/images/right_16.png'/>" height="16" width="16"/></span>
                <span>
                    <a href="javascript:send('${task.path}','suspended')"><fmt:message
                            key="jnt_task.suspended"/></a>&nbsp;
                    <a href="javascript:send('${task.path}','cancelled')"><fmt:message
                            key="jnt_task.cancel"/></a>&nbsp;
                    <a href="javascript:send('${task.path}','finished')"><fmt:message
                            key="jnt_task.complete"/></a>
                </span>
                            </c:when>
                            <c:when test="${task.propertiesAsString.state == 'finished'}">
                                <img alt="" src="<c:url value='${url.currentModule}/images/tick_16.png'/>" height="16" width="16"/>
                            </c:when>
                            <c:when test="${task.propertiesAsString.state == 'suspended'}">
                <span><img alt="" src="<c:url value='${url.currentModule}/images/bubble_16.png'/>" height="16"
                           width="16"/></span>
                <span>
                    <a href="javascript:send('${task.path}','cancelled')"><fmt:message
                            key="jnt_task.cancel"/></a>&nbsp;
                    <a href="javascript:send('${task.path}','active')"><fmt:message
                            key="jnt_task.continue"/></a>
                </span>
                            </c:when>
                            <c:when test="${task.propertiesAsString.state == 'canceled'}">
                                <img alt="" src="<c:url value='${url.currentModule}/images/warning_16.png'/>" height="16" width="16"/>
                            </c:when>
                        </c:choose>
                    </li>
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
                        <c:if test="${node!=null}">
                        <li>
                            <c:set var="taskTitle"
                                   value="${not empty task.displayName ? task.displayName : task.name} - ${task.variables['jcr:title'][0].value}"/>
                            <c:set var="path" value="${jcr:findDisplayableNode(node, renderContext).path}"/>
                            <c:if test="${not empty path}">
                                <c:url var="preview" value="${renderContext.servletPath}/${task.variables.workspace}/${task.variables.locale}${path}.html"/>
                                <a target="_blank" href="${preview}">${fn:escapeXml(taskTitle)}</a>
                            </c:if>
                            <c:if test="${empty path}">
                                ${fn:escapeXml(taskTitle)}
                            </c:if>
                            <div class="listEditToolbar">
                                <c:choose>
                                    <c:when test="${not empty task.formResourceName}">
                                        <input class="workflowaction" type="button" value="${task.name}"
                                               onclick="$.toggle('task${node.identifier}-${task.id}');$('#taskrow${node.identifier}-${task.id}').toggleClass('hidden');"/>
                                        <c:if test="${!empty preview}">
                                            <input class="workflowaction"  type="button" onclick="window.open('${preview}','<fmt:message key="label.preview"/>')" value="<fmt:message key='label.preview'/>">
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${task.outcomes}" var="outcome">
                                            <input class="workflowaction" type="button" value="${outcome}"
                                                   onclick="executeTask('${node.path}', '${task.provider}:${task.id}', '${outcome}', '<c:url value="${url.base}"/>', '${currentNode.UUID}', '<c:url value="${url.current}.ajax"/>','window.location=window.location;')"/>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                                    <%--
                                    <template:addResources>
                                        <script type="text/javascript">
                                            animatedcollapse.addDiv('comments${node.identifier}-${task.id}', 'fade=1,speed=100');
                                        </script>
                                    </template:addResources>
                                    <input class="workflowaction" type="button" value="<fmt:message key="jnt_task.comments"/>"
                                           onclick="animatedcollapse.toggle('comments${node.identifier}-${task.id}');$('#commentsrow${node.identifier}-${task.id}').toggleClass('hidden');"/>
                                    --%>
                            </div>
                        </li>
                        <c:if test="${not empty task.formResourceName}">
                            <div class="hidden" id="taskrow${node.identifier}-${task.id}">
                                <div style="display:none;" id="task${node.identifier}-${task.id}" class="taskformdiv">
                                    <c:set var="workflowTaskFormTask" value="${task}" scope="request"/>
                                    <c:url var="myUrl" value="${url.current}.ajax"/>
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
                            </div>
                        </c:if>
                            </c:if>
                    </c:if>
                </c:forEach>
            </c:if>

        </ul>
    </div>
    <div class="clear"></div>
</div>

