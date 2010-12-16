<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<c:if test="${currentResource.workspace eq 'live'}">
<div id="gotoAdmin${currentNode.identifier}"/>
    <script type="text/javascript">
        $('#gotoAdmin${currentNode.identifier}').load('${url.basePreview}${currentNode.path}.html.ajax');
    </script>
</div>
</c:if>
<c:if test="${currentResource.workspace ne 'live'}">
    <a href="${url.context}/administration">
    <c:if test="${!empty currentNode.properties['jcr:title']}">
        ${currentNode.properties["jcr:title"].string}
    </c:if>
    <c:if test="${empty currentNode.properties['jcr:title']}">
        <img src="${url.context}/icons/admin.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; ">
        <fmt:message key="label.administration"/>
    </c:if>
</a>
    </c:if>