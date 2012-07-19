<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<script type="text/javascript">
    $(document).ready(function () {
        $("a#openPopup-${currentNode.name}").fancybox();
    });
</script>
<c:if test="${not renderContext.editMode}">
<div style="display:none">
    <div id="popup-${currentNode.name}" class="${currentNode.properties['popupClass'].string}">
</c:if>
        <c:forEach items="${jcr:getChildrenOfType(currentNode,'jmix:droppableContent')}" var="child">
            <template:module node="${child}"/>
        </c:forEach>
        <c:if test="${renderContext.editMode}">
            <template:module path="*"/>
        </c:if>
<c:if test="${not renderContext.editMode}">
    </div>
</div>
</c:if>
<a class="${currentNode.properties['buttonClass'].string}" id="openPopup-${currentNode.name}"
href="#popup-${currentNode.name}">${currentNode.properties['buttonLabel'].string}</a>
