<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="intro" var="intro"/>

<h2>${title.string}</h2>

<div class="intro">
    ${intro.string}
</div>
<div>
	<fmt:message key="tags"/>:&nbsp;<template:module node="${currentNode}" template="tags"/>
	<template:module node="${currentNode}" template="addTag"/>
</div>
<c:forEach items="${currentNode.editableChildren}" var="paragraph">
    <template:module node="${paragraph}" template="default"/>
</c:forEach>