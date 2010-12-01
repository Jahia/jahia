<%@ page import="org.jahia.services.content.nodetypes.ConstraintsHelper" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/jcr" %>
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
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.datepicker.js"/>
<template:addResources type="css" resources="jquery.treeview.css,jquery.fancybox.css"/>
<template:addResources type="javascript"
                       resources="jquery.treeview.min.js,jquery.treeview.async.jahia.js,jquery.fancybox.pack.js"/>
<template:addResources>
    <style type="text/css">
        div#fancy_div {
            background: #FFF;
            color: #000;
            overflow: auto;
        }
    </style>
</template:addResources>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<div id="${currentNode.UUID}">
    <template:include templateType="html" template="hidden.header"/>
    <c:set var="animatedTasks" value=""/>
    <c:set var="animatedWFs" value=""/>

    <c:set var="inSite" value="true"/>
    <c:forEach items="${moduleMap.currentList}" var="child" begin="${moduleMap.begin}" end="${moduleMap.end}"
               varStatus="status">
        <%-- only editorial contents are contribuable --%>
        <c:if test="${functions:isNodeType(child,'jmix:editorialContent')}">
            <%@include file="edit.jspf" %>
            <%--<%@include file="workflow.jspf" %>--%>
            <div id="edit-${child.identifier}">
                <template:module node="${child}"/>
            </div>
            <hr/>
        </c:if>
    </c:forEach>
    <div class="clear"></div>
    <c:if test="${moduleMap.editable and renderContext.editMode}">
        <template:module path="*"/>
    </c:if>
    <template:include templateType="html" template="hidden.footer"/>

</div>
<c:if test="${not renderContext.ajaxRequest}">
    <%@include file="addcontent.jspf" %>
</c:if>