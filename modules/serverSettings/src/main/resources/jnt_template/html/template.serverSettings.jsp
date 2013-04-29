<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="${fn:substring(renderContext.request.locale,0,2)}">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <jcr:nodeProperty node="${renderContext.mainResource.node}" name="jcr:description" inherited="true" var="description"/>
    <jcr:nodeProperty node="${renderContext.mainResource.node}" name="jcr:createdBy" inherited="true" var="author"/>
    <c:set var="keywords" value="${jcr:getKeywords(renderContext.mainResource.node, true)}"/>
    <c:if test="${!empty description}"><meta name="description" content="${description.string}" /></c:if>
    <c:if test="${!empty author}"><meta name="author" content="${author.string}" /></c:if>
    <c:if test="${!empty keywords}"><meta name="keywords" content="${keywords}" /></c:if>
    <title>${fn:escapeXml(renderContext.mainResource.node.displayableName)}</title>

    <script type="text/javascript">
        function workInProgress() {
            if (window.parent.waitingMask) {
                window.parent.waitingMask('<fmt:message key="label.workInProgressTitle"/>');
            } else {
                $.blockUI({ css: {
                    border: 'none',
                    padding: '15px',
                    backgroundColor: '#000',
                    '-webkit-border-radius': '10px',
                    '-moz-border-radius': '10px',
                    opacity: .5,
                    color: '#fff'
                }, message: '<fmt:message key="label.workInProgressTitle"/>' });
            }
        }
    </script>
</head>

<body>

<div class="page-header">
    <h1><fmt:message key="label.administration"/></h1>
</div>

<template:area path="pagecontent"/>

<c:if test="${renderContext.editMode}">
    <template:addResources type="css" resources="edit.css" />
</c:if>
<template:addResources type="javascript" resources="jquery.min.js,jquery.blockUI.js,bootstrap.js"/>
<template:addResources type="css" resources="bootstrap.css,admin.css"/>
<template:theme/>

</body>
</html>
