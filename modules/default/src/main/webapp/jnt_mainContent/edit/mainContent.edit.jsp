<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
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

<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addResources type="css" resources="formmaincontent.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>
<c:if test="${empty requestScope.ajaxCall}">
    <script>
        $(document).ready(function(){
            initEditFields("${currentNode.identifier}");
        });
    </script>
</c:if>
<div class="FormMainContent">
    <h3 class="title edit${currentNode.identifier}" jcr:id="jcr:title" jcr:url="${url.base}${currentNode.path}">
        <jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
    <c:if test="${!empty image}">
        <div class="imagefloat${currentNode.properties.align.string} file${currentNode.identifier}" jcr:id="image"
             jcr:url="${url.base}${currentNode.path}">
            <img src="${image.node.url}" alt="${image.node.url}"/>
        </div>
    </c:if>
    <div class="clear"></div>
    <c:if test="${empty image}">
        <div class="imagefloat${currentNode.properties.align.string} file${currentNode.identifier}" jcr:id="image"
             jcr:url="${url.base}${currentNode.path}">
            <span>click here to attach a file</span>
        </div>
    </c:if>
   <div class="clear"></div>
    <jcr:propertyInitializers var="options" nodeType="jnt:mainContent" name="align"/>
    <span jcr:id="align" class="choicelistEdit${currentNode.identifier}"
                              jcr:url="${url.base}${currentNode.path}"
                              jcr:options="{<c:forEach items="${options}" varStatus="status" var="option"><c:if test="${status.index > 0}">,</c:if>'${option.value.string}':'${option.displayName}'</c:forEach>}">Image Alignment${currentNode.properties.align.string}</span>
        <span jcr:id="body" class="ckeditorEdit${currentNode.identifier}"
              id="ckeditorEdit${currentNode.identifier}${scriptPropName}"
              jcr:url="${url.base}${currentNode.path}">${currentNode.properties.body.string}</span>
</div>
<br class="clear"/>