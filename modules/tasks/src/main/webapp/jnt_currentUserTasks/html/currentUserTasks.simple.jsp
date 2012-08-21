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
<template:tokenizedForm>
    <form name="myform" method="post" action="">
        <input type="hidden" name="jcrNodeType" value="jnt:task">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${currentNode.path}.html${ps}'/>">
        <input type="hidden" name="state">
    </form>
</template:tokenizedForm>

<script type="text/javascript">
    var ready = true;
    function sendNewStatus(uuid, task, state, finalOutcome) {
        if (ready) {
            ready = false;
            $(".taskaction-complete").addClass("taskaction-disabled");
            $(".taskaction").addClass("taskaction-disabled");
            $.post('<c:url value="${url.base}"/>' + task,
                    {"jcrMethodToCall":"put", "state":state, "finalOutcome":finalOutcome, "form-token":document.forms['tokenForm_' +
                                                                                                                      uuid].elements['form-token'].value},
                    null, "json");
        }
    }
    ;
</script>
<template:include view="hidden.header"/>
<div id="tasklist">
    <div id="${user.UUID}">

        <ul>
            <c:forEach items="${moduleMap.currentList}" var="task" varStatus="status" begin="${moduleMap.begin}"
                       end="${moduleMap.end}">
                <template:tokenizedForm>
                    <form id="tokenForm_${task.identifier}" name="tokenform_${task.identifier}" method="post"
                          action="<c:url value='${url.base}'/>${task.path}">
                    </form>
                </template:tokenizedForm>
                <li>
                    <a href="<c:url value='${url.base}${task.path}.html'/>">${fn:escapeXml(task.displayableName)}</a>

                    <c:choose>
                        <c:when test="${task.propertiesAsString.state == 'active'}">
                                <span><img alt="" src="<c:url value='${url.currentModule}/images/right_16.png'/>"
                                           height="16" width="16"/></span>
                <span>
                    <a href="javascript:sendNewStatus('${task.identifier}','${task.path}','suspended')"><fmt:message
                            key="jnt_task.state.suspended"/></a>&nbsp;
                    <fmt:setBundle basename="${task.properties['taskBundle'].string}" var="taskBundle"/>
                                        <c:if test="${not empty task.properties['possibleOutcomes']}">
                                            <c:forEach items="${task.properties['possibleOutcomes']}" var="outcome"
                                                       varStatus="status">
                                                <fmt:message bundle="${taskBundle}" var="outcomeLabel"
                                                             key="${fn:replace(task.properties['taskName'].string,' ','.')}.${fn:replace(outcome.string,' ','.')}"/>
                                                <c:if test="${fn:startsWith(outcomeLabel, '???')}"><fmt:message
                                                        bundle="${taskBundle}" var="outcomeLabel"
                                                        key="${fn:replace(task.properties['taskName'].string,' ','.')}.${fn:replace(fn:toLowerCase(outcome.string),' ','.')}"/></c:if>
                                                <a class="taskaction taskaction-start"
                                                   href="javascript:sendNewStatus('${task.identifier}','${task.path}','finished','${outcome.string}')"
                                                   title="${outcome.string}">${outcomeLabel}</a>
                                            </c:forEach>
                                        </c:if>
                                        <c:if test="${empty task.properties['possibleOutcomes']}">

                                            <a href="javascript:send('${task.path}','finished')"><fmt:message key="jnt_task.complete"/></a>
                                        </c:if>
                </span>
                        </c:when>
                        <c:when test="${task.propertiesAsString.state == 'finished'}">
                            <img alt="" src="<c:url value='${url.currentModule}/images/tick_16.png'/>" height="16"
                                 width="16"/>
                        </c:when>
                        <c:when test="${task.propertiesAsString.state == 'suspended'}">
                <span><img alt="" src="<c:url value='${url.currentModule}/images/bubble_16.png'/>" height="16"
                           width="16"/></span>
                <span>
                    <a href="javascript:sendNewStatus('${task.identifier}','${task.path}','active')"><fmt:message
                            key="jnt_task.continue"/></a>
                </span>
                        </c:when>
                    </c:choose>
                </li>
            </c:forEach>
        </ul>
    </div>
    <div class="clear"></div>
</div>

