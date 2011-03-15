<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
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
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<template:addResources type="javascript" resources="i18n/jquery.ui.min.js"/>
<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="css" resources="timepicker.css"/>


<c:if test="${currentResource.workspace eq 'live'}">
<div id="tasks${currentNode.identifier}"/>
    <script type="text/javascript">
        $('#tasks${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.tasklist.html.ajax"/>');
    </script>
</c:if>
<c:if test="${currentResource.workspace ne 'live'}">

<form name="myform" method="post">
    <input type="hidden" name="nodeType" value="jnt:task">
    <input type="hidden" name="redirectTo" value="<c:url value='${url.base}${currentNode.path}.tasklist'/>">
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
    <col span="1" width="10%" class="col1"/>
    <col span="1" width="50%" class="col2"/>
    <col span="1" width="10%" class="col3"/>
    <col span="1" width="25%" class="col4"/>
    <col span="1" width="15%" class="col5"/>
</colgroup>
<thead>
<tr>
    <th class="center" id="Type" scope="col"><fmt:message key="jnt_task.type"/> <a href="user.tasklist.jsp#"
                                                                                   title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a
            title="sort down"
            href="user.tasklist.jsp#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
    <th id="Title" scope="col"><fmt:message key="mix_title.jcr_title"/> <a href="user.tasklist.jsp#"
                                                                           title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png"
            alt="up"/></a><a
            title="sort down" href="user.tasklist.jsp#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png"
            alt="down"/></a></th>
    <th class="center" id="State" scope="col"><fmt:message key="jnt_task.state"/> <a href="user.tasklist.jsp#"
                                                                                     title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a
            title="sort down"
            href="user.tasklist.jsp#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
    <th class="center" id="Priority" scope="col"><fmt:message key="jnt_task.priority"/> <a
            href="user.tasklist.jsp#" title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a
            title="sort down"
            href="user.tasklist.jsp#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
    <th id="Date" scope="col"><fmt:message key="jnt_task.dueDate"/> <a href="user.tasklist.jsp#" title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png"
            alt="up"/></a><a
            title="sort down" href="user.tasklist.jsp#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png"
            alt="down"/></a></th>
</tr>
</thead>

<tbody>
<workflow:tasksForNode var="wfTasks" user="${renderContext.user}"/>
<c:forEach items="${wfTasks}" var="task" varStatus="status">
    <jcr:node var="node" uuid="${task.variables.nodeId}"/>
    <tr class="${((status.count + 1)) % 2 == 0 ? 'odd' : 'even'}">
    <td class="center" headers="Type">
        <img alt="" src="${url.currentModule}/images/workflow.png"/>
    </td>
    <td headers="Title">
		<c:if test="${'user-connection' == task.variables.workflow.key}" var="isUserConnectionRequest">
			<c:set var="taskTitle" value="${not empty task.displayName ? task.displayName : task.name} (${task.variables.fromUser})"/>
		</c:if>
		<c:if test="${not isUserConnectionRequest}">
		<c:set var="taskTitle" value="${not empty task.formResourceName and not empty task.variables['jcr:title'] ? task.variables['jcr:title'][0].value : (not empty task.displayName ? task.displayName : task.name)}"/>
		</c:if>
        <c:if test="${jcr:isNodeType(node,'jnt:page')}">
            <c:set var="path" value="${node.path}"/>
        </c:if>
        <c:if test="${!jcr:isNodeType(node,'jnt:page')}">
            <c:set var="path" value="${jcr:getParentOfType(node,'jnt:page').path}"/>
        </c:if>
        <a target="_blank"
           href="${url.context}/cms/render/${task.variables.workspace}/${task.variables.locale}${path}.html">${fn:escapeXml(taskTitle)}</a>
    </td>
    <td colspan="3">
        <div class="listEditToolbar">
            <c:choose>
                <c:when test="${not empty task.formResourceName}">
                        <script type="text/javascript">
                            animatedcollapse.addDiv('task${node.identifier}-${task.id}', 'fade=1,speed=100');
                        </script>
                    <input class="workflowaction" type="button" value="${task.name}"
                           onclick="animatedcollapse.toggle('task${node.identifier}-${task.id}');$('#taskrow${node.identifier}-${task.id}').toggleClass('hidden');"/>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${task.outcomes}" var="outcome">
                        <input class="workflowaction" type="button" value="${outcome}"
                               onclick="executeTask('${node.path}', '${task.provider}:${task.id}', '${outcome}', '<c:url value="${url.base}"/>', '${currentNode.UUID}', '<c:url value="${url.current}"/>','window.location=window.location;')"/>
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
    </td>
    </tr>
    <c:if test="${not empty task.formResourceName}">
    	<tr class="${((status.count + 1)) % 2 == 0 ? 'odd' : 'even'} hidden" id="taskrow${node.identifier}-${task.id}">
        <td colspan="5">
            <c:url var="myUrl" value="${url.current}.ajax"/>
            <div style="display:none;" id="task${node.identifier}-${task.id}" class="taskformdiv">
                <c:set var="workflowTaskFormTask" value="${task}" scope="request"/>
                <template:module node="${node}" template="contribute.add">
                    <template:param name="resourceNodeType" value="${task.formResourceName}"/>
                    <template:param name="workflowTaskForm" value="${task.provider}:${task.id}"/>
                    <template:param name="workflowTaskFormTaskName" value="${task.name}"/>
                    <template:param name="workflowTaskFormCallbackId" value="${currentNode.UUID}"/>
                    <template:param name="workflowTaskFormCallbackURL" value="${myUrl}"/>
                    <template:param name="workflowTaskFormCallbackJS"
                                    value="$('.taskformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=100');});animatedcollapse.reinit();"/>
                </template:module>
            </div>
        </td>
        </tr>
    </c:if>
</c:forEach>
</tbody>
</table>

    <script type="text/javascript">
        animatedcollapse.init();
    </script>
<div class="pagination"><!--start pagination-->

    <div class="paginationPosition"><span>Page ${currentPage} of ${nbPages} - ${nodes.size} results</span>
    </div>
    <div class="paginationNavigation">
        <c:if test="${currentPage>1}">
            <c:url var="myUrl" value="${url.current}">
                <c:param name="begin" value="${ (currentPage-2) * pageSize}"/>
                <c:param name="end" value="${ (currentPage-1)*pageSize-1}"/>
            </c:url>
            <a class="previousLink"
               href="javascript:replace('${currentNode.UUID}-tasks','${myUrl}')"><fmt:message key="jnt_userTask.previous"/></a>
        </c:if>
        <c:forEach begin="1" end="${nbPages}" var="i">
            <c:if test="${i != currentPage}">
                <c:url var="myUrl" value="${url.current}">
                    <c:param name="begin" value="${ (i-1) * pageSize }"/>
                    <c:param name="end" value="${ i*pageSize-1}"/>
                </c:url>
                    <span><a class="paginationPageUrl"
                             href="javascript:replace('${currentNode.UUID}-tasks','${myUrl}')"> ${ i }</a></span>
            </c:if>
            <c:if test="${i == currentPage}">
                <span class="currentPage">${ i }</span>
            </c:if>
        </c:forEach>

        <c:if test="${currentPage<nbPages}">
            <c:url var="myUrl" value="${url.current}">
                <c:param name="begin" value="${ currentPage * pageSize }"/>
                <c:param name="end" value="${ (currentPage+1)*pageSize-1}"/>
            </c:url>
            <a class="nextLink"
               href="javascript:replace('${currentNode.UUID}-tasks','${myUrl}')"><fmt:message key="jnt_userTask.next"/> </a>
        </c:if>
    </div>

    <div class="clear"></div>
</div>
<!--stop pagination-->
<template:removePager id="${currentNode.identifier}"/>
</div>
<div class="clear"></div>
</div>
</div>
</div>
</div>
</c:if>