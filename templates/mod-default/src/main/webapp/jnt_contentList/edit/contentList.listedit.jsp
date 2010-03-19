<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<template:include templateType="html" template="hidden.header"/>
<c:forEach items="${currentList}" var="child" begin="${begin}" end="${end}" varStatus="status">

    <%-- buttons --%>
    <div class="listEditToolbar">
        <c:if test="${child.locked ne 'true'}">
            <input type="button" value="Edit"
                   onclick="replace('edit-${child.identifier}', '${url.base}${child.path}.edit.edit?ajaxcall=true', 'initEditFields(\'${child.identifier}\')')"/>
        </c:if>
        <c:if test="${child.locked eq 'true'}">
            <input type="button" value="Locked" disabled="true"/>
        </c:if>
        <input type="button" value="Preview"
               onclick="replace('edit-${child.identifier}', '${url.base}${child.path}.html?ajaxcall=true', '')"/>

        <c:if test="${currentNode.properties['j:canOrderInContribution'].boolean}">
            <c:if test="${not status.first}">
                <input id="moveUp-${currentNode.identifier}-${status.index}" type="button" value="Move up"
                       onclick="invert('${child.path}','${previousChild.path}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true')"/>
            </c:if>
            <c:if test="${not status.last}">
                <input type="button" value="Move down"
                       onclick="document.getElementById('moveUp-${currentNode.identifier}-${status.index+1}').onclick()"/>
            </c:if>
        </c:if>
        <c:if test="${child.locked ne 'true'}">
            <c:if test="${currentNode.properties['j:canDeleteInContribution'].boolean}">
                <input type="button" value="Delete" onclick="deleteNode('${child.path}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true')"/>
            </c:if>

            <workflow:workflowsForNode workflowAction="publish" var="workflows" node="${child}"/>

            [ <c:forEach items="${workflows}" var="wf" >
                <input type="button" value="${wf.name}" onclick="startWorkflow('${child.path}', '${wf.provider}:${wf.key}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true')" />
            </c:forEach> ]
        </c:if>

        <workflow:tasksForNode var="tasks" node="${child}"/>
        <c:forEach items="${tasks}" var="task" >
            [ <c:forEach items="${task.outcomes}" var="outcome" >
                <input type="button" value="${outcome}" onclick="executeTask('${child.path}', '${task.provider}:${task.id}', '${outcome}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true')" />
            </c:forEach> ]
        </c:forEach>

        <c:set var="previousChild" value="${child}"/>
    </div>

    <div id="edit-${child.identifier}">
        <template:module templateType="html" node="${child}"/>
    </div>
   <hr/>
</c:forEach>
<div class="clear"></div>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include templateType="html" template="hidden.footer"/>

<c:if test="${empty param.ajaxcall}">
    <%-- include add nodes forms --%>
    <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="types"/>
    <p>
        <fmt:message key="label.add.new.content" />
    </p>
    <script type="text/javascript">
        <c:forEach items="${types}" var="type" varStatus="status">
            animatedcollapse.addDiv('add${currentNode.identifier}-${status.index}', 'fade=1,speed=700,group=newContent');
        </c:forEach>
        animatedcollapse.init();
    </script>
    <c:if test="${types != null}">
        <c:forEach items="${types}" var="type" varStatus="status">
            <jcr:nodeType name="${type.string}" var="nodeType"/>
            <button onclick="animatedcollapse.toggle('add${currentNode.identifier}-${status.index}');">${jcr:labelForLocale(nodeType, renderContext.mainResourceLocale)}</button>
        </c:forEach>

        <c:forEach items="${types}" var="type" varStatus="status">
            <div style="display:none;" id="add${currentNode.identifier}-${status.index}">
            <template:module node="${currentNode}" templateType="edit" template="add">
                <template:param name="resourceNodeType" value="${type.string}"/>
                <template:param name="currentListURL" value="${url.current}?ajaxcall=true"/>
            </template:module>
            </div>
        </c:forEach>
    </c:if>
</c:if>
