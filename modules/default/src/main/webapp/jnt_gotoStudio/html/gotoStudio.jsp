<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="css" resources="goto-links.css"/>
<c:if test="${!renderContext.settings.productionMode && !renderContext.settings.distantPublicationServerMode}">
 <img src="${url.context}/icons/studio.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; " />
 <a href="<c:url value='${url.studio}'/>">
    <c:if test="${!empty currentNode.properties['jcr:title']}">
        ${currentNode.properties["jcr:title"].string}
    </c:if>
    <c:if test="${empty currentNode.properties['jcr:title']}">
        <fmt:message key="label.studio"/>
    </c:if>
</a>
</c:if>
