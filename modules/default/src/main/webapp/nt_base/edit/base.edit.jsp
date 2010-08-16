<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

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
<template:addResources type="css" resources="datepicker.css"/>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="css" resources="formcontribute.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>

<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<c:if test="${empty requestScope.ajaxCall}">
    <script>
        $(document).ready(function(){
            initEditFields("${currentNode.identifier}");
        });
    </script>
</c:if>
<div class="FormContribute">
    <c:set var="type" value="${currentNode.primaryNodeType}"/>
    <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
        <c:if test="${!propertyDefinition.multiple and propertyDefinition.contentItem}">
            <c:set var="prop" value="${currentNode.properties[propertyDefinition.name]}"/>
            <c:set var="scriptPropName" value="${fn:replace(propertyDefinition.name,':','_')}"/>
            <p>
            <label>${jcr:label(propertyDefinition,renderContext.mainResourceLocale)}&nbsp;:</label>
            <c:choose>
                <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                    <c:if test="${propertyDefinition.selector eq selectorType.FILEUPLOAD or propertyDefinition.selector eq selectorType.FILEPICKER}">
                        <div class="file${currentNode.identifier}" jcr:id="${propertyDefinition.name}"
                             jcr:url="${url.base}${currentNode.path}">
                            <span><fmt:message key="add.file"/></span>
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
