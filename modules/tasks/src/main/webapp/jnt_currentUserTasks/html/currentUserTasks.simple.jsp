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
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>

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

            <c:if test="${currentNode.properties['viewUserTasks'].boolean}">
                <template:include view="hidden.load"/>
                <c:set var="listQuery" value="${moduleMap.listQuery}"/>
                <jcr:jqom var="tasks" qomBeanName="listQuery"/>

                <c:set var="nodes" value="${tasks.nodes}"/>
                <%--<c:set value="${jcr:getNodes(currentNode,'jnt:task')}" var="tasks"/>--%>

                <c:forEach items="${nodes}" var="task"
                           begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">

                    <c:set var="found" value="${fn:contains(currentNode.properties['filterOnTypes'].string, task.properties['type'])}"/>
                    <c:if test="${empty currentNode.properties['taskTypes'].string or found}">

                    <li>
                        <a href="<c:url value='${url.base}${task.path}.html'/>>">${fn:escapeXml(task.displayName)}</a>

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
                    </c:if>
                </c:forEach>
            </c:if>
        </ul>
    </div>
    <div class="clear"></div>
</div>

