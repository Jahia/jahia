<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="maincontent"> 
    <jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/> 
    <jcr:nodeProperty node="${currentNode}" name="mainContentBody" var="mainContentBody"/>
    <jcr:nodeProperty node="${currentNode}" name="mainContentImage" var="mainContentImage"/>
    <h3>${mainContentTitle.string}</h3>
    <c:if test="${!empty mainContentImage}">
    	<img src="${mainContentImage.node.url}" alt="${mainContentImage.node.url}"/>
    </c:if>
    ${mainContentBody.string}
    <hr/>
		<template:module node="${currentNode}" template="tags"/><br/>
    	<template:module node="${currentNode}" template="addTag"/>
    <hr/>
</div>
<br class="clear"/>
