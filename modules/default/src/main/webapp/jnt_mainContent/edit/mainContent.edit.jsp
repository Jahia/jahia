<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
            <span><fmt:message key="label.edit.attach.file"/></span>
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