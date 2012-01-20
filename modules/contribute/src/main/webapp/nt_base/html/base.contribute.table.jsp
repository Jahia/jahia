<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
<%--
<template:addResources type="css" resources="contentlist.css"/>
--%>
<template:addResources type="css" resources="contribute.min.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<utility:setBundle basename="JahiaContributeMode" useUILocale="true"/>
<%@include file="../../include/contributeCKEditorToolbar.jspf" %>
<div id="${currentNode.UUID}">
    <c:set var="animatedTasks" value=""/>
    <c:set var="animatedWFs" value=""/>

    <c:set var="suffix" value=".html"/>
    <c:set var="parent" value="${jcr:getParentOfType(currentNode,'jnt:content')}"/>
    <c:set var="icon" value="folder_up.png"/>
    <c:set var="alt"><fmt:message key="label.backToParent"/></c:set>
    <c:if test="${empty parent}">
        <c:set var="parent" value="${jcr:getParentOfType(currentNode,'jnt:folder')}"/>
    </c:if>
    <c:if test="${empty parent or jcr:isNodeType(parent, 'jnt:contentList')}">
        <c:set var="parent" value="${jcr:getParentOfType(currentNode,'jnt:page')}"/>
        <c:set var="icon" value="back.png"/>
        <c:set var="alt"><fmt:message key="label.backToPage"/></c:set>
    </c:if>



    <h3>
        <c:if test="${not empty parent}">
            <c:if test="${jcr:isNodeType(parent, 'jnt:content') and not jcr:isNodeType(parent, 'jnt:contentFolder')}">
                <c:set var="suffix" value=".editContent.html"/>
            </c:if>
            <a title="${alt}" href="<c:url value='${url.base}${parent.path}${suffix}'/>"><img height="32" width="32"
                                                                                     border="0"
                                                                                     style="cursor: pointer;"
                                                                                     title="${alt}" alt="${alt}"
                                                                                     src="<c:url value='${url.templatesPath}/default/images/icons/${icon}'/>"></a>
        </c:if>
        <input type="checkbox" class="jahiaCBoxContributeContent" name="${currentNode.identifier}" /> ${fn:escapeXml(currentNode.displayableName)}
    </h3>
</div>

