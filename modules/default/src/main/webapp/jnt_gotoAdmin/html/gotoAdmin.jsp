<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="css" resources="goto-links.css"/>
<c:if test="${!empty currentNode.properties['toAdminComponent']}">
    <script type="text/javascript">
        $.post("${url.context}/administration", null, null, "json");
    </script>
    <c:set var="optionURL" value="?do=${currentNode.properties['toAdminComponent'].string}&sub=display"/>
</c:if>
<c:choose>
    <c:when test="${not empty currentNode.properties['buttonClass']}">
        <a href="${url.context}/administration${optionURL}" class="${currentNode.properties['buttonClass'].string}">
            <c:if test="${!empty currentNode.properties['jcr:title']}">
                ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
            </c:if>
            <c:if test="${empty currentNode.properties['jcr:title']}">

                <fmt:message key="label.administration"/>
            </c:if>
        </a>
    </c:when>
    <c:otherwise>
        <img src="${url.context}/icons/admin.png" width="16" height="16" alt=" " role="presentation"
             style="position:relative; top: 4px; margin-right:2px; "/><a
            href="${url.context}/administration${optionURL}">
        <c:if test="${!empty currentNode.properties['jcr:title']}">
            ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
        </c:if>
        <c:if test="${empty currentNode.properties['jcr:title']}">

            <fmt:message key="label.administration"/>
        </c:if>
    </a>
    </c:otherwise>
</c:choose>

