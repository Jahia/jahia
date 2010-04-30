<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<template:include templateType="html" template="hidden.header"/>
<c:set var="animatedTasks" value=""/>
<c:set var="animatedWFs" value=""/>
<c:forEach items="${currentList}" var="child" begin="${begin}" end="${end}" varStatus="status">

    <%-- buttons --%>
    <div class="listEditToolbar">
        <c:if test="${child.locked ne 'true'}">
            <button
                    onclick="replace('edit-${child.identifier}', '${url.base}${child.path}.edit.edit?ajaxcall=true', 'initEditFields(\'${child.identifier}\')')">
                <span class="icon-contribute icon-edit"></span><fmt:message key="label.edit"/></button>
        </c:if>
        <c:if test="${child.locked eq 'true'}">
            <button>
                <span class="icon-contribute icon-locked"></span>Locked
            </button>
        </c:if>
        <button
                onclick="replace('edit-${child.identifier}', '${url.base}${child.path}.html?ajaxcall=true', '')">
            <span class="icon-contribute icon-preview"></span><fmt:message key="label.preview"/></button>

        <c:if test="${currentNode.properties['j:canOrderInContribution'].boolean}">
            <c:if test="${not status.first}">
                <button id="moveUp-${currentNode.identifier}-${status.index}"
                        onclick="invert('${child.path}','${previousChild.path}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true')">
                    <span class="icon-contribute icon-moveup"></span><fmt:message key="label.move.up"/></button>
            </c:if>
            <c:if test="${not status.last}">
                <button
                        onclick="document.getElementById('moveUp-${currentNode.identifier}-${status.index+1}').onclick()">
                    <span class="icon-contribute icon-movedown"></span><fmt:message key="label.move.down"/></button>
            </c:if>
        </c:if>
        <c:if test="${child.locked ne 'true'}">
            <c:if test="${currentNode.properties['j:canDeleteInContribution'].boolean}">
                <button onclick="deleteNode('${child.path}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true')">
                    <span class="icon-contribute icon-delete"></span><fmt:message key="label.delete"/></button>
            </c:if>

            <workflow:workflowsForNode workflowAction="publish" var="workflows" node="${child}"/>
            <br/>
            <c:forEach items="${workflows}" var="wf">
                <c:choose>
                    <c:when test="${not empty wf.formResourceName}">
                        <script language="JavaScript">
                            animatedcollapse.addDiv('workflow${child.identifier}-${wf.key}', 'fade=1,speed=700,group=workflow');
                            <c:set var="animatedWFs" value="${animatedWFs}animatedcollapse.addDiv('workflow${child.identifier}-${wf.key}', 'fade=1,speed=700,group=workflow');"/>
                        </script>
                        <input class="workflow" type="button" value="${wf.name}"
                               onclick="animatedcollapse.toggle('workflow${child.identifier}-${wf.key}');"/>
                    </c:when>
                    <c:otherwise>
                        <input class="workflow" type="button" value="${wf.name}"
                               onclick="startWorkflow('${child.path}', '${wf.provider}:${wf.key}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true','${animatedWFs}${animatedTasks}animatedcollapse.reinit();')"/></c:otherwise>
                </c:choose>
            </c:forEach>
        </c:if>

        <workflow:tasksForNode var="tasks" node="${child}"/>
        <br/>
        <c:forEach items="${tasks}" var="task">
            <c:choose>
                <c:when test="${not empty task.formResourceName}">
                    <script language="JavaScript">
                        animatedcollapse.addDiv('task${child.identifier}-${task.id}', 'fade=1,speed=700,group=tasks');
                        <c:set var="animatedTasks" value="${animatedTasks}animatedcollapse.addDiv('task${child.identifier}-${task.id}', 'fade=1,speed=700,group=tasks');"/>
                    </script>
                    <input class="workflowaction" type="button" value="${task.name}"
                               onclick="animatedcollapse.toggle('task${child.identifier}-${task.id}');"/>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${task.outcomes}" var="outcome">
                        <input class="workflowaction" type="button" value="${outcome}"
                               onclick="executeTask('${child.path}', '${task.provider}:${task.id}', '${outcome}', '${url.base}', '${currentNode.UUID}', '${url.current}?ajaxcall=true','${animatedWFs}${animatedTasks}animatedcollapse.reinit();')"/>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </c:forEach>

        <c:set var="previousChild" value="${child}"/>
    </div>

    <c:forEach items="${workflows}" var="wf">
        <c:choose>
            <c:when test="${not empty wf.formResourceName}">
                <div style="display:none;" id="workflow${child.identifier}-${wf.key}" class="workflowformdiv">
                    <template:module node="${child}" templateType="edit" template="add">
                        <template:param name="resourceNodeType" value="${wf.formResourceName}"/>
                        <template:param name="workflowStartForm" value="${wf.provider}:${wf.key}"/>
                        <template:param name="workflowStartFormWFName" value="${wf.name}"/>
                        <template:param name="workflowStartFormWFCallbackId" value="${currentNode.UUID}"/>
                        <template:param name="workflowStartFormWFCallbackURL" value="${url.current}?ajaxcall=true"/>
                        <template:param name="workflowStartFormWFCallbackJS" value="$('.taskformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=700,group=tasks');});$('.workflowformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=700,group=workflow');});animatedcollapse.reinit();"/>
                    </template:module>
                </div>
            </c:when>
        </c:choose>
    </c:forEach>
    <c:forEach items="${tasks}" var="task">
        <c:choose>
            <c:when test="${not empty task.formResourceName}">
                <div style="display:none;" id="task${child.identifier}-${task.id}" class="taskformdiv">
                    <c:set var="workflowTaskFormTask" value="${task}" scope="request"/>
                    <template:module node="${child}" templateType="edit" template="add">
                        <template:param name="resourceNodeType" value="${task.formResourceName}"/>
                        <template:param name="workflowTaskForm" value="${task.provider}:${task.id}"/>
                        <template:param name="workflowTaskFormTaskName" value="${task.name}"/>
                        <template:param name="workflowTaskFormCallbackId" value="${currentNode.UUID}"/>
                        <template:param name="workflowTaskFormCallbackURL" value="${url.current}?ajaxcall=true"/>
                        <template:param name="workflowTaskFormCallbackJS" value="$('.taskformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=700,group=tasks');});$('.workflowformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=700,group=workflow');});animatedcollapse.reinit();"/>
                    </template:module>
                </div>
            </c:when>
        </c:choose>
    </c:forEach>
    <br/>

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
    <h3 class="titleaddnewcontent">
        <img title="" alt="" src="${url.currentModule}/images/add.png"/><fmt:message key="label.add.new.content"/>
    </h3>
    <script language="JavaScript">
        <c:forEach items="${types}" var="type" varStatus="status">
            animatedcollapse.addDiv('add${currentNode.identifier}-${status.index}', 'fade=1,speed=700,group=newContent');
        </c:forEach>
        animatedcollapse.init();
    </script>
    <c:if test="${types != null}">
        <div class="listEditToolbar">
            <c:forEach items="${types}" var="type" varStatus="status">
                <jcr:nodeType name="${type.string}" var="nodeType"/>
                <button onclick="animatedcollapse.toggle('add${currentNode.identifier}-${status.index}');"><span
                        class="icon-contribute icon-add"></span>${jcr:label(nodeType, renderContext.mainResourceLocale)}
                </button>
            </c:forEach>
        </div>

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
