 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="publication.css"/>
<jcr:nodeProperty var="file" node="${currentNode}" name="file"/>
<div class="publicationListItemInline"><!--start publicationListItemInline -->
    <p>
        <span class="publicationDate">
            <c:if test="${!empty currentNode.properties.date.string}">
                ${currentNode.properties.date.string}
            </c:if>
        </span>
	</p>
    <h5>
        <a class="publicationDescriptionSimple" href="${file.node.url}">
            <jcr:nodeProperty node="${currentNode}" name="jcr:title"/>
         </a>
    </h5>
    <p class="publicationAuthor">
		<c:if test="${!empty currentNode.properties.author.string}">
            <fmt:message key="by"/>: ${currentNode.properties.author.string}
        </c:if>
    </p>
	<div>
        ${currentNode.properties.body.string}
    </div>
    <div class="clear"></div>
</div>
<!--stop publicationListItemInline -->
