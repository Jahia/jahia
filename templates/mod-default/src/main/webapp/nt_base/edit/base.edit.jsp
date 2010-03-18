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
<template:addResources type="css" resources="960.css"/>
<template:addResources type="css" resources="datepicker.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>

<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<div>
    <c:set var="type" value="${currentNode.primaryNodeType}"/>
    <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
        <c:if test="${!propertyDefinition.multiple and propertyDefinition.contentItem}">
            <c:set var="prop" value="${currentNode.properties[propertyDefinition.name]}"/>
            <c:set var="scriptPropName" value="${fn:replace(propertyDefinition.name,':','_')}"/>
            <p>
            <span class="label">${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}&nbsp;:</span>
            <c:choose>
                <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                    <c:if test="${propertyDefinition.selector eq selectorType.FILEUPLOAD or propertyDefinition.selector eq selectorType.FILEPICKER}">
                        <div class="file${currentNode.identifier}" jcr:id="${propertyDefinition.name}"
                             jcr:url="${url.base}${currentNode.path}">
                            <span>add a file (file will be uploaded in your files directory before submitting the form)</span>
                        </div>
                        <template:module node="${prop.node}" template="default" templateType="html"/>
                    </c:if>
                </c:when>
                <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                    <c:set var="dateTimePicker"
                           value="${propertyDefinition.selector eq selectorType.DATETIMEPICKER}"/>
                        <span jcr:id="${propertyDefinition.name}" class="dateEdit${currentNode.identifier}"
                              id="dateEdit${currentNode.identifier}${scriptPropName}"
                              jcr:url="${url.base}${currentNode.path}">
                            <c:if test="${not empty prop}">
                                <fmt:formatDate value="${prop.date.time}" pattern="dd, MMMM yyyy HH:mm"/>
                            </c:if>
                        </span>
                </c:when>
                <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                    <jcr:propertyInitializers var="options" nodeType="${type.name}"
                                              name="${propertyDefinition.name}"/>
                        <span jcr:id="${propertyDefinition.name}" class="choicelistEdit${currentNode.identifier}"
                              jcr:url="${url.base}${currentNode.path}"
                              jcr:options="{<c:forEach items="${options}" varStatus="status" var="option"><c:if test="${status.index > 0}">,</c:if>'${option.value.string}':'${option.displayName}'</c:forEach>}">${prop.string}</span>
                </c:when>
                <c:when test="${propertyDefinition.selector eq selectorType.RICHTEXT}">
                        <span jcr:id="${propertyDefinition.name}" class="ckeditorEdit${currentNode.identifier}"
                              id="ckeditorEdit${currentNode.identifier}${scriptPropName}"
                              jcr:url="${url.base}${currentNode.path}">${prop.string}</span>
                </c:when>
                <c:otherwise>
                        <span jcr:id="${propertyDefinition.name}" class="edit${currentNode.identifier}"
                              id="edit${currentNode.identifier}${scriptPropName}"
                              jcr:url="${url.base}${currentNode.path}">${prop.string}</span>
                </c:otherwise>
            </c:choose>
            </p>
        </c:if>
    </c:forEach>
</div>
