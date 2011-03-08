<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="pagetagging.css"/>
<div  class="tagthispage">
	<c:if test="${not empty currentNode.properties['jcr:title'] && not empty not empty currentNode.properties['jcr:title'].string}" var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if test="${not titleProvided}"><fmt:message key="tags"/></c:if>:&nbsp;<template:module node="${jcr:getParentOfType(currentNode, 'jnt:page')}" template="hidden.tags"/>
	<template:module node="${jcr:getParentOfType(currentNode, 'jnt:page')}" template="hidden.addTag" editable="false" />
</div>