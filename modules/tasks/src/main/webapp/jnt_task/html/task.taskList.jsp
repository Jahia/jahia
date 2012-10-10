<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
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
<c:if test="${not renderContext.ajaxRequest}">
    <template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
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
    <template:addResources type="javascript"
                           resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
    <template:addResources type="javascript" resources="jquery.form.js"/>

    <template:addResources type="javascript" resources="timepicker.js"/>
    <template:addResources type="css" resources="timepicker.css"/>
</c:if>

<c:set var="user" value="${currentNode.properties['assigneeUserKey'].string}"/>
<c:if test="${empty user}">
    <c:set var="user" value="${currentNode.properties['jcr:createdBy'].string}"/>
</c:if>

<jcr:node var="currentUser" path="${renderContext.user.localPath}"/>
<c:set value="${currentNode.properties['state'].string eq 'finished'}" var="finished"/>
<c:url var="reloadurl" value="${url.base}${currentNode.path}.taskList.html.ajax">
    <c:forEach items="${param}" var="p">
        <c:param name="${p.key}" value="${p.value}"/>
    </c:forEach>
</c:url>
<c:set var="identifierName" value="#task_${currentNode.identifier}"/>
<c:if test="${not renderContext.ajaxRequest}">
    <div class="taskComment${finished ? ' taskCommentResolved' : ''}" id="task_${currentNode.identifier}">
</c:if>
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

<p class="taskdate value"><fmt:formatDate value="${currentNode.properties['dueDate'].date.time}"
                                          pattern="dd/MM/yyyy"/></p>

<p class="taskDescription">
    ${currentNode.properties['description'].string}
</p>
<template:tokenizedForm>
    <form id="tokenForm_${currentNode.identifier}" name="tokenform_${currentNode.identifier}" method="post"
          action="<c:url value='${url.base}'/>${currentNode.path}">
    </form>
</template:tokenizedForm>
<ul class="taskactionslist">
    <c:set var="assignable" value="true"/>
    <c:if test="${not empty currentNode.properties['candidates'] and currentNode.properties['assigneeUserKey'].string ne currentUser.name}">
        <c:set var="assignable" value="false"/>
        <c:set var="candidates" value=""/>
        <c:forEach items="${currentNode.properties['candidates']}" var="candidate">
            <c:set var="candidates" value=" ${candidate.string} ${candidates} "/>
        </c:forEach>
        <c:set var="userKey" value="u:${currentUser.name}"/>
        <c:if test="${fn:contains(candidates, userKey)}">
            <c:set var="assignable" value="true"/>
        </c:if>
        <c:if test="${not assignable}">
            <c:set var="groups" value="${user:getUserMembership(currentUser)}"/>
            <c:forEach items="${groups}" var="x">
                <c:if test="${fn:contains(candidates, x.key)}">
                    <c:set var="assignable" value="true"/>
                </c:if>
            </c:forEach>
        </c:if>
    </c:if>
    <c:choose>
        <c:when test="${currentNode.properties.state.string == 'active' and currentNode.properties['assigneeUserKey'].string ne currentUser.name and assignable eq 'true'}">
            <li><a class="taskaction taskaction-assign"
                   href="javascript:sendNewAssignee('${currentNode.identifier}','${currentNode.path}','${currentUser.name}','${reloadurl}')"
                   title="assign to me"><fmt:message key="label.actions.assigneToMe"/></a></li>
        </c:when>
        <c:when test="${currentNode.properties.state.string == 'active' and currentNode.properties['assigneeUserKey'].string eq currentUser.name}">
            <li><a class="taskaction taskaction-refuse"
                   href="javascript:sendNewAssignee('${currentNode.identifier}','${currentNode.path}','','${reloadurl}')"
                   title="Refuse"><fmt:message key="label.actions.refuse"/></a></li>
            <li><a class="taskaction taskaction-start"
                   href="javascript:sendNewStatus('${currentNode.identifier}','${currentNode.path}','started',null,'${reloadurl}')"
                   title="start"><fmt:message key="label.actions.start"/></a></li>
        </c:when>
        <c:when test="${currentNode.properties.state.string == 'started' and currentNode.properties['assigneeUserKey'].string eq currentUser.name}">
            <li><a class="taskaction taskaction-refuse"
                   href="javascript:sendNewAssignee('${currentNode.identifier}','${currentNode.path}','','${reloadurl}')"
                   title="Refuse"><fmt:message key="label.actions.refuse"/></a></li>
            <li><a class="taskaction taskaction-suspend"
                   href="javascript:sendNewStatus('${currentNode.identifier}','${currentNode.path}','suspended',null,'${reloadurl}')"
                   title="suspend"><fmt:message
                    key="label.actions.suspend"/></a></li>
            <fmt:setBundle basename="${currentNode.properties['taskBundle'].string}" var="taskBundle"/>
            <c:if test="${not empty currentNode.properties['possibleOutcomes']}">
                <c:forEach items="${currentNode.properties['possibleOutcomes']}" var="outcome" varStatus="status">
                    <fmt:message bundle="${taskBundle}" var="outcomeLabel"
                                 key="${fn:replace(currentNode.properties['taskName'].string,' ','.')}.${fn:replace(outcome.string,' ','.')}"/>
                    <c:if test="${fn:startsWith(outcomeLabel, '???')}"><fmt:message bundle="${taskBundle}"
                                                                                    var="outcomeLabel"
                                                                                    key="${fn:replace(currentNode.properties['taskName'].string,' ','.')}.${fn:replace(fn:toLowerCase(outcome.string),' ','.')}"/></c:if>
                    <li><a class="taskaction taskaction-start"
                           href="javascript:sendNewStatus('${currentNode.identifier}','${currentNode.path}','finished','${outcome.string}','${reloadurl}')"
                           title="${outcome.string}">${outcomeLabel}</a></li>
                </c:forEach>
            </c:if>
            <c:if test="${empty currentNode.properties['possibleOutcomes']}">
                <c:set var="taskId" value="${currentNode.identifier}"/>
                <li>
                    <div class="taskaction-complete"><input class="completeTaskAction"
                                                            taskPath="<c:url value='${url.base}${currentNode.path}'/>"
                                                            type="checkbox" id="btnComplete-${taskId}"
                                                            onchange="sendNewStatus('${taskId}','${currentNode.path}','finished',null,'${reloadurl}')"/>&nbsp;<label
                            for="btnComplete-${taskId}"><fmt:message key="label.actions.completed"/></label></div>
                </li>
            </c:if>
            <jcr:node var="taskData" path="${currentNode.path}/taskData"/>
            <c:if test="${not empty taskData}">
                <script>
                    initEditFields('${taskData.identifier}');
                </script>
                <template:module path="${currentNode.path}/taskData" view="contribute.edit"/>
            </c:if>
        </c:when>
        <c:when test="${currentNode.properties.state.string == 'finished'}">
            <li>
                <div class="taskaction-complete"><input name="Completed" type="checkbox" disabled="disabled"
                                                        checked="checked" value="Completed"/>&nbsp;<fmt:message
                        key="label.actions.completed"/></div>
            </li>
        </c:when>
        <c:when test="${currentNode.properties.state.string == 'suspended' and currentNode.properties['assigneeUserKey'].string eq currentUser.name}">
            <li><a class="taskaction taskaction-refuse"
                   href="javascript:sendNewAssignee('${currentNode.identifier}','${currentNode.path}','','${reloadurl}')"
                   title="Refuse"><fmt:message key="label.actions.refuse"/></a></li>
            <li><a class="taskaction taskaction-continue"
                   href="javascript:sendNewStatus('${currentNode.identifier}','${currentNode.path}','started',null,'${reloadurl}')"
                   title="start"><fmt:message key="label.actions.resume"/></a></li>
        </c:when>
        <c:when test="${currentNode.properties.state.string == 'canceled'}">
        </c:when>
    </c:choose>
</ul>
<c:if test="${not renderContext.ajaxRequest}">
    </div>
</c:if>
