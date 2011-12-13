<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:if test="${renderContext.loggedIn || renderContext.editMode}">
<template:addResources type="css" resources="goto-links.css"/>
<img src="<c:url value='${url.currentModule}/images/logout-small.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><a href="<c:url value='${url.logout}'/>">${fn:escapeXml(currentNode.displayableName)}</a>
</c:if>