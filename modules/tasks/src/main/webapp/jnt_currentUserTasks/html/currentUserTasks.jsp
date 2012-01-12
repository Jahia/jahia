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
        function send(task, state) {
            form = document.forms['myform'];
            form.action = '<c:url value="${url.base}"/>' + task;
            form.elements.state.value = state;
            form.submit();
        }
    </script>
    <div id="tasklist">
        <div id="${user.UUID}">


            <table width="100%" class="table tableTasks" summary="Tasks">
                <colgroup>
                    <col span="1" width="15%" class="col1"/>
                    <col span="1" width="50%" class="col2"/>
                    <col span="1" width="15%" class="col3"/>
                    <col span="1" width="20%" class="col4"/>
                </colgroup>
                <thead>
                <tr>
                    <th class="center" id="Type" scope="col"><fmt:message key="jnt_task.type"/></th>
                    <th id="Title" scope="col"><fmt:message key="mix_title.jcr_title"/></th>
                    <th class="center" id="State" scope="col"><fmt:message key="label.actions"/></th>
                    <th id="Date" scope="col"><fmt:message key="label.startDate"/></th>
                </tr>
                </thead>

                <tbody>

                <jcr:sql var="tasks"
                         sql="select * from [jnt:task] as task where task.assignee='${user.identifier}' order by task.[jcr:created]"/>
                <c:set var="nodes" value="${tasks.nodes}"/>
                    <%--<c:set value="${jcr:getNodes(currentNode,'jnt:task')}" var="tasks"/>--%>
                <c:if test="${currentNode.properties['viewUserTasks'].boolean}">
                    <c:forEach items="${nodes}" var="task" varStatus="status">
                        <tr class="${status.count % 2 == 0 ? 'odd' : 'even'}">
                            <td class="center" headers="Type"><img alt=""
                                                                   src="<c:url value='${url.currentModule}/images/flag_16.png'/>" height="16"
                                                                   width="16"/>
                            </td>
                            <td headers="Title">${fn:escapeXml(task.propertiesAsString['jcr:title'])}</td>
                            <td class="center" headers="State">
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
                            </td>
                            <td headers="Date"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                                               dateStyle="short" type="date"/></td>
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
                                <td class="center" headers="Type">
                                    <img alt="" src="<c:url value='${url.currentModule}/images/workflow.png'/>"/>
                                </td>
                                <td headers="Title">
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
                                </td>
                                <td >
                                    <div class="listEditToolbar">
                                        <c:choose>
                                            <c:when test="${not empty task.formResourceName}">
                                                <input class="workflowaction" type="button" value="${task.name}"
                                                       onclick="$('#taskrow${node.identifier}-${task.id}').toggle('fast');"/>
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
                                    </div>
                                </td>
                                <td>
                                    <fmt:formatDate value="${task.createTime}" type="both" timeStyle="short"/>
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