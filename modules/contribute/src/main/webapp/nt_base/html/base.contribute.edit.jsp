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
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>

<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<utility:setBundle basename="JahiaContributeMode" useUILocale="true" templateName="Jahia Contribute Mode"/>
<c:set var="nodeLocked" value="${jcr:isLockedAndCannotBeEdited(currentNode)}"/>
<c:if test="${empty currentResource.moduleParams.contentType}">
    <c:set var="contentType" value="content"/>
</c:if>
<c:if test="${not empty currentResource.moduleParams.contentType}">
    <c:set var="contentType" value="${currentResource.moduleParams.contentType}"/>
</c:if>
<c:forEach items="${currentNode.nodeTypes}" var="typeName">
<jcr:nodeType name="${typeName}" var="type"/>
<c:if test="${!nodeLocked && !renderContext.ajaxRequest}">
    <c:set var="initEditFields" value="initEditFields('${currentNode.identifier}');"/>
    <c:forEach items="${currentNode.primaryNodeType.propertyDefinitions}" var="propertyDefinition">
        <c:if test="${!propertyDefinition.multiple and propertyDefinition.itemType eq contentType and not propertyDefinition.hidden and !(propertyDefinition.name eq 'jcr:title') and !(propertyDefinition.name eq '*')}">
            <c:choose>
                <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                    <c:set var="initEditFields" value="${initEditFields}setFileSelector('${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}');setFileEdit('${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}');"/>
                </c:when>
                <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                    <c:set var="initEditFields" value="${initEditFields}setChoiceListEdit('${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}');"/>
                </c:when>
            </c:choose>
        </c:if>
    </c:forEach>
    <template:addResources>
        <script>
            $(document).ready(function() {
                ${initEditFields}
            });
        </script>
    </template:addResources>
</c:if>
<style>

    img {
        border: none;
    }

        /*  */

    #treepreview {
        position: absolute;
        border: 1px solid #ccc;
        background: #333;
        padding: 5px;
        display: none;
        color: #fff;
        z-index:9999;
    }

        /*  */
</style>
<div class="FormContribute">
    <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
        <c:if test="${propertyDefinition.name eq 'jcr:title' and propertyDefinition.itemType eq contentType}">
            <c:set var="prop" value="${currentNode.properties[propertyDefinition.name]}"/>
            <c:set var="scriptPropName" value="${fn:replace(propertyDefinition.name,':','_')}"/>
            <p>
                <label>${jcr:labelInNodeType(propertyDefinition,renderContext.UILocale,type)}&nbsp;:</label>
            <span jcr:id="${propertyDefinition.name}" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}${scriptPropName}"
                  jcr:url="<c:url value='${url.base}${currentNode.path}'/>">${prop.string}</span>
            </p>
        </c:if>
    </c:forEach>
    <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
        <c:set var="readonly" value="${nodeLocked || propertyDefinition.protected}"/>
        <c:if test="${!propertyDefinition.multiple and propertyDefinition.itemType eq contentType and not propertyDefinition.hidden and !(propertyDefinition.name eq 'jcr:title') and !(propertyDefinition.name eq '*')}">
            <c:set var="prop" value="${currentNode.properties[propertyDefinition.name]}"/>
            <c:set var="scriptPropName" value="${fn:replace(propertyDefinition.name,':','_')}"/>
            <p>
            <label>${jcr:labelInNodeType(propertyDefinition,renderContext.UILocale,type)}&nbsp;:</label>
            <c:if test="${readonly}">
                <c:choose>
                    <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                        File
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
            </c:if>
            <c:if test="${not readonly}">
                <c:choose>
                    <c:when test="${renderContext.liveMode}">
                        <c:url value='${url.baseLive}' var="baseURL"/>
                    </c:when>
                    <c:otherwise>
                        <c:url value='${url.basePreview}' var="baseURL"/>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                        <c:choose>
                            <c:when test="${propertyDefinition.selector eq selectorType.FILEUPLOAD or propertyDefinition.selector eq selectorType.CONTENTPICKER}">
                                <c:choose>
                                    <c:when test="${propertyDefinition.selectorOptions.type eq 'image' || propertyDefinition.selectorOptions.type eq 'file'}">
                                        <c:set var="pickerTypeImage" value="${propertyDefinition.selectorOptions.type == 'image'}"/>
                                        <div class="fileSelector${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${propertyDefinition.name}"
                                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                                             jeditabletreeselector:baseURL="${baseURL}"
                                             jeditabletreeselector:root="${renderContext.site.path}"
                                             jeditabletreeselector:nodetypes="${pickerTypeImage ? 'nt:folder,jmix:image,jnt:virtualsite' : 'nt:folder,nt:file,jnt:virtualsite'}"
                                             jeditabletreeselector:selectablenodetypes="${pickerTypeImage ? 'jmix:image' : 'nt:file'}"
                                             jeditabletreeselector:selectorLabel="<fmt:message key='label.show.file.picker'/>"
                                             jeditabletreeselector:preview="true" jeditabletreeselector:previewPath="<c:url value='${url.files}'/>">
                                            <span><fmt:message key="label.select.file"/></span>
                                        </div>

                                        <span><fmt:message key="label.or"/></span>
                                        <div class="file${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${propertyDefinition.name}"
                                             jcr:url="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>">
                                            <span><fmt:message key="add.file"/></span>
                                        </div>

                                    </c:when>
                                    <c:when test="${propertyDefinition.selectorOptions.type eq 'folder'}">
                                        <div class="fileSelector${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${propertyDefinition.name}"
                                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                                             jeditabletreeselector:baseURL="${baseURL}"
                                             jeditabletreeselector:root="${renderContext.site.path}"
                                             jeditabletreeselector:nodetypes="jnt:folder,jnt:virtualsite"
                                             jeditabletreeselector:selectablenodetypes="jnt:folder"
                                             jeditabletreeselector:selectorLabel="<fmt:message key='label.select.folder'/>"
                                             jeditabletreeselector:preview="false">
                                            <span><fmt:message key="label.select.folder"/></span>
                                        </div>

                                    </c:when>
                                    <c:when test="${propertyDefinition.selectorOptions.type eq 'contentfolder'}">
                                        <div class="fileSelector${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${propertyDefinition.name}"
                                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                                             jeditabletreeselector:baseURL="${baseURL}"
                                             jeditabletreeselector:root="${renderContext.site.path}"
                                             jeditabletreeselector:nodetypes="jnt:contentFolder"
                                             jeditabletreeselector:selectablenodetypes="jnt:contentFolder"
                                             jeditabletreeselector:selectorLabel="<fmt:message key='label.select.folder'/>"
                                             jeditabletreeselector:preview="false">
                                            <span><fmt:message key="label.select.folder"/></span>
                                        </div>

                                    </c:when>
                                    <c:when test="${propertyDefinition.selectorOptions.type eq 'page'}">
                                        <div class="fileSelector${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${propertyDefinition.name}"
                                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                                             jeditabletreeselector:baseURL="${baseURL}"
                                             jeditabletreeselector:root="${renderContext.site.path}"
                                             jeditabletreeselector:nodetypes="jnt:page"
                                             jeditabletreeselector:selectablenodetypes="jnt:page"
                                             jeditabletreeselector:selectorLabel="<fmt:message key='label.show.page.picker'/>"
                                             jeditabletreeselector:preview="false">
                                            <span><fmt:message key="label.select.page"/></span>
                                        </div>

                                    </c:when>

                                    <c:otherwise>
                                        <div class="fileSelector${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${propertyDefinition.name}"
                                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                                             jeditabletreeselector:baseURL="${baseURL}"
                                             jeditabletreeselector:root="${renderContext.site.path}"
                                             jeditabletreeselector:nodetypes="jnt:content,jnt:page,jnt:virtualsite"
                                             jeditabletreeselector:selectablenodetypes="jmix:droppableContent"
                                             jeditabletreeselector:selectorLabel="<fmt:message key='label.show.content.picker'/>" jeditabletreeselector:preview="false">
                                            <span><fmt:message key="label.select.content"/></span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                                <jcr:propertyInitializers var="options" nodeType="${type.name}"
                                                          name="${propertyDefinition.name}"/>
                                <div jcr:id="${propertyDefinition.name}" class="choicelistEdit${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}"
                                     jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                                     jcr:options="{<c:forEach items="${options}" varStatus="status" var="option"><c:if test="${status.index > 0}">,</c:if>'${option.value.string}':'${option.displayName}'</c:forEach>}">${prop.string}</div>
                            </c:when>
                            <c:otherwise>
                                <div class="fileSelector${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${propertyDefinition.name}"
                                     jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                                     jeditabletreeselector:baseURL="${baseURL}"
                                     jeditabletreeselector:root="${renderContext.site.path}"
                                     jeditabletreeselector:nodetypes="jnt:content,jnt:page,jnt:virtualsite"
                                     jeditabletreeselector:selectablenodetypes="jmix:droppableContent"
                                     jeditabletreeselector:selectorLabel="<fmt:message key='label.show.content.picker'/>" jeditabletreeselector:preview="false">
                                    <span><fmt:message key="label.select.content"/></span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div id="renderingOfFile${currentNode.identifier}${fn:replace(propertyDefinition.name,':','_')}">
                            <template:module node="${prop.node}" view="default" />
                        </div>
                    </c:when>
                    <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                        <c:set var="dateTimePicker"
                               value="${propertyDefinition.selector eq selectorType.DATETIMEPICKER ? 'dateTimeEdit' : 'dateEdit'}"/>
                        <div jcr:id="${propertyDefinition.name}" class="${dateTimePicker}${currentNode.identifier}"
                             id="${dateTimePicker}${currentNode.identifier}${scriptPropName}"
                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>" jcr:value="${prop.string}" jcr:valuems="${not empty prop.date ? prop.date.timeInMillis : ''}">
                            <c:if test="${not empty prop}">
                                <fmt:formatDate value="${prop.date.time}" pattern="dd, MMMM yyyy HH:mm"/>
                            </c:if>
                        </div>
                    </c:when>
                    <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                        <jcr:propertyInitializers var="options" nodeType="${type.name}"
                                                  name="${propertyDefinition.name}"/>
                        <div jcr:id="${propertyDefinition.name}" class="choicelistEdit${currentNode.identifier}"
                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>"
                             jcr:options="{<c:forEach items="${options}" varStatus="status" var="option"><c:set var="value" value="${fn:replace(option.displayName,'\\'',' ')}"/><c:if test="${status.index > 0}">,</c:if>'${option.value.string}':'${value}'<c:if test="${prop.string eq option.value.string}"><c:set var="val" value="${value}"/></c:if></c:forEach>}">${val}</div>
                    </c:when>
                    <c:when test="${propertyDefinition.selector eq selectorType.RICHTEXT}">
                        <div jcr:id="${propertyDefinition.name}" class="ckeditorEdit${currentNode.identifier}"
                             id="ckeditorEdit${currentNode.identifier}${scriptPropName}${renderContext.mainResource.locale}"
                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>" jcr:ckeditorToolbar="contributeCKEditorToolbar">${prop.string}</div>
                    </c:when>
                    <c:otherwise>
                        <div jcr:id="${propertyDefinition.name}" class="edit${currentNode.identifier}"
                             id="edit${currentNode.identifier}${scriptPropName}"
                             jcr:url="<c:url value='${url.base}${currentNode.path}'/>">${prop.string}</div>
                    </c:otherwise>
                </c:choose>
            </c:if>
            </p>
        </c:if>
    </c:forEach>
</div>
</c:forEach>