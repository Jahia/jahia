<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="prop" type="org.jahia.services.content.JCRPropertyWrapper"--%>
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
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="css" resources="datepicker.css"/>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="css" resources="formcontribute.css"/>
<template:addResources type="css" resources="jquery.treeview.css,jquery.fancybox.css"/>
<template:addResources type="css" resources="timepicker.css"/>
--%>
<template:addResources type="css" resources="contribute.min.css"/>
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
<template:addResources type="javascript" resources="i18n/contributedefault-${currentResource.locale}.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>

<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<c:set var="nodeLocked" value="${jcr:isLockedAndCannotBeEdited(currentNode)}"/>
<c:if test="${empty currentResource.moduleParams.contentType}">
    <c:set var="contentType" value="content"/>
</c:if>
<c:if test="${not empty currentResource.moduleParams.contentType}">
    <c:set var="contentType" value="${currentResource.moduleParams.contentType}"/>
</c:if>

<jsp:useBean id="displayedFields" class="java.util.HashMap"/>

<c:forEach items="${currentNode.nodeTypes}" var="typeName">
    <jcr:nodeType name="${typeName}" var="type"/>
        <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
            <c:set var="readonly" value="${nodeLocked or propertyDefinition.protected or not jcr:hasPermission(currentNode, 'jcr:modifyProperties_default')}"/>
            <c:set var="scriptPropName" value="${fn:replace(propertyDefinition.name,':','_')}"/>
            <c:if test="${!propertyDefinition.multiple and propertyDefinition.itemType eq contentType and not propertyDefinition.hidden and !(propertyDefinition.name eq 'jcr:title') and !(propertyDefinition.name eq '*') and not fn:contains(displayedFields,scriptPropName)}">
                <c:set var="prop" value="${currentNode.properties[propertyDefinition.name]}"/>
                <p>
                    <h4>${jcr:labelInNodeType(propertyDefinition,currentResource.locale,type)}:</h4>
                    <c:set target="${displayedFields}" value="${scriptPropName}" property="${scriptPropName}" />
                    <c:choose>
                        <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                            <template:module path="${prop.node.path}"/>
                        </c:when>
                        <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                        <span>
                            <c:if test="${not empty prop}">
                                <fmt:formatDate value="${prop.date.time}" pattern="dd, MMMM yyyy HH:mm"/>
                            </c:if>
                        </span>
                        </c:when>
                        <c:otherwise>
                            <span>${prop.string}</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </c:if>
        </c:forEach>
</c:forEach>