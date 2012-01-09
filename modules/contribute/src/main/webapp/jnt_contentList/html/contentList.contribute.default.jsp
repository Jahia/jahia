<%@ page import="org.jahia.services.content.nodetypes.ConstraintsHelper" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utils" uri="http://www.jahia.org/tags/utilityLib" %>
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
<%--
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="css" resources="timepicker.css,datepicker.css,jquery.treeview.css,jquery.fancybox.css,jquery.colorbox.css,contentlist.css,formcontribute.css"/>
--%>
<template:addResources type="css" resources="contribute.min.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript"
                       resources="jquery.treeview.min.js,jquery.treeview.async.jahia.js,jquery.fancybox.js,jquery.colorbox.js"/>
<template:addResources type="javascript" resources="jquery.defer.js"/>
<template:addResources type="javascript" resources="treeselector.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<utils:setBundle basename="JahiaContributeMode" useUILocale="true"/>
<%@include file="../../include/contributeCKEditorToolbar.jspf" %>
<div id="${currentNode.UUID}">
    <template:include templateType="html" view="hidden.header"/>
    <c:set var="animatedTasks" value=""/>
    <c:set var="animatedWFs" value=""/>

    <c:set var="inSite" value="true"/>
    <c:forEach items="${moduleMap.currentList}" var="child" begin="${moduleMap.begin}" end="${moduleMap.end}"
               varStatus="status">
        <%-- only editorial contents are contribuable --%>
        <c:if test="${jcr:isNodeType(child,'jmix:editorialContent')}">
            <c:if test="${jcr:hasPermission(currentNode, 'jcr:modifyProperties_default')}">
                <%@include file="edit.jspf"%>
            </c:if>
            <%--<%@include file="workflow.jspf" %>--%>
            <c:set var="markedForDeletion" value="${jcr:isNodeType(child, 'jmix:markedForDeletion')}"/>
            <div id="edit-${child.identifier}"${markedForDeletion ? 'class="markedForDeletion"' : ''}>
                <c:if test="${markedForDeletion}">
                    <span class="markedForDeletionLabel"><fmt:message key="label.publication.markedfordeletion"/></span>
                </c:if>
                <template:module node="${child}"/>
            </div>
            <hr/>
        </c:if>
    </c:forEach>
    <div class="clear"></div>
    <c:if test="${moduleMap.editable and renderContext.editMode}">
        <template:module path="*"/>
    </c:if>
    <template:include templateType="html" view="hidden.footer"/>

<div class="clear"></div>

<c:if test="${jcr:hasPermission(currentNode, 'jcr:addChildNodes_default')}">
    <%@include file="addcontent.jspf" %>
</c:if>
</div>
