<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="css" resources="timepicker.css"/>
<utility:setBundle basename="modules.tasks.resources.JahiaTasks" useUILocale="true"/>

<c:if test="${currentResource.workspace eq 'live'}">
    <div id="tasks${currentNode.identifier}"/>
    <script type="text/javascript">
        $('#tasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.tasklist.html.ajax"/>');
    </script>
</c:if>
<c:if test="${currentResource.workspace ne 'live'}">

    <form name="myform" method="post">
        <input type="hidden" name="jcrNodeType" value="jnt:task">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${currentNode.path}.tasklist'/>">
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
    <div id="tasklist" class="boxtasks">
        <div class=" boxtasksgrey boxtaskspadding16 boxtasksmarginbottom16">
            <div class="boxtasks-inner">
                <div class="boxtasks-inner-border"><!--start boxtasks -->
                    <div id="${currentNode.UUID}">


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
                            <workflow:tasksForNode var="wfTasks" user="${renderContext.user}"/>
                            <c:forEach items="${wfTasks}" var="task" varStatus="status">
                                <jcr:node var="node" uuid="${task.variables.nodeId}"/>
                                <c:if test="${node!=null}">
                                <tr class="${((status.count + 1)) % 2 == 0 ? 'odd' : 'even'}">
                                    <td class="center" headers="Type">
                                        <img alt="" src="<c:url value='${url.currentModule}/images/workflow.png'/>"/>
                                    </td>
                                    <td headers="Title">
                                        <c:if test="${'user-connection' == task.variables.workflow.key}" var="isUserConnectionRequest">
                                            <c:set var="taskTitle" value="${not empty task.displayName ? task.displayName : task.name} (${task.variables.fromUser})"/>
                                        </c:if>
                                        <c:if test="${not isUserConnectionRequest}">
                                            <c:set var="taskTitle" value="${not empty task.formResourceName and not empty task.variables['jcr:title'] ? task.variables['jcr:title'][0].value : (not empty task.displayName ? task.displayName : task.name)}"/>
                                        </c:if>
                                        <c:set var="path" value="${jcr:findDisplayableNode(node, renderContext).path}"/>
                                        <c:if test="${not empty path}">
                                            <c:url var="preview" value="${renderContext.servletPath}/${task.variables.workspace}/${task.variables.locale}${path}.html"/>
                                            <a target="_blank" href="${preview}">${fn:escapeXml(taskTitle)}</a>
                                        </c:if>
                                        <c:if test="${empty path}">
                                            ${fn:escapeXml(taskTitle)}
                                        </c:if>
                                    </td>
                                    <td>
                                        <div class="listEditToolbar">
                                            <c:choose>
                                                <c:when test="${not empty task.formResourceName}">
                                                    <script type="text/javascript">
                                                        animatedcollapse.addDiv('task${node.identifier}-${task.id}', 'fade=1,speed=100');
                                                    </script>
                                                    <input class="workflowaction" type="button" value="${task.displayName}"
                                                           onclick="animatedcollapse.toggle('task${node.identifier}-${task.id}');$('#taskrow${node.identifier}-${task.id}').toggleClass('hidden');"/>
                                                    <input class="workflowaction"  type="button" onclick="window.open('${preview}','<fmt:message key="label.preview"/>')" value="<fmt:message key='label.preview'/>">
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${task.outcomes}" var="outcome">
                                                        <input class="workflowaction" type="button" value="${outcome}"
                                                               onclick="executeTask('${node.path}', '${task.provider}:${task.id}', '${outcome}', '<c:url value="${url.base}"/>', '${currentNode.UUID}', '<c:url value="${url.current}"/>','window.location=window.location;')"/>
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
                                    <tr class="${((status.count + 1)) % 2 == 0 ? 'odd' : 'even'} hidden" id="taskrow${node.identifier}-${task.id}">
                                        <td colspan="5">
                                            <c:url var="myUrl" value="${url.current}.ajax"/>
                                            <div style="display:none;" id="task${node.identifier}-${task.id}" class="taskformdiv">
                                                <c:set var="workflowTaskFormTask" value="${task}" scope="request"/>
                                                <c:set var="workflowTaskFormCallbackJS">$('.taskformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=100');});animatedcollapse.reinit();</c:set>
                                                <template:include view="contribute.workflow">
                                                    <template:param name="resourceNodeType" value="${task.formResourceName}"/>
                                                    <template:param name="workflowTaskForm" value="${task.provider}:${task.id}"/>
                                                    <template:param name="workflowTaskFormTaskName" value="${task.name}"/>
                                                    <template:param name="workflowTaskFormCallbackId" value="${currentNode.UUID}"/>
                                                    <template:param name="workflowTaskFormCallbackURL" value="${myUrl}"/>
                                                    <template:param name="workflowTaskFormCallbackJS"
                                                                    value="${workflowTaskFormCallbackJS}"/>
                                                    <template:param name="workflowTaskNodeUuid" value="${node.identifier}"/>
                                                </template:include>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                                </c:if>
                            </c:forEach>
                            </tbody>
                        </table>

                        <script type="text/javascript">
                            animatedcollapse.init();
                        </script>

                        <div class="clear"></div>
                    </div>
                    <!--stop pagination-->
                </div>
                <div class="clear"></div>
            </div>
        </div>
    </div>
</c:if>
