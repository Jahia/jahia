<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<c:if test="${renderContext.editMode}">
<fieldset>
    <legend><c:out value="${not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType)}"/></legend>
</c:if>
	<c:if test="${not empty title.string}">
		<h2><c:out value="${title.string}"/></h2>
	</c:if>
    <c:forEach items="${currentNode.nodes}" var="menuItem">
        <template:module node="${menuItem}" editable="true">
        	<template:param name="subNodesTemplate" value="navMenuItem"/>
        </template:module>
    </c:forEach>
    <c:if test="${renderContext.editMode}">
    	<div class="addelements">
        	<span>???Add your menu items here???</span>
        	<template:module path="*"/>
        </div>
    </c:if>
<c:if test="${renderContext.editMode}">
</fieldset>
</c:if>
