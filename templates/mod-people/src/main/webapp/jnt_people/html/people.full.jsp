<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="people.css"/>


<div class="peopleListItem">
    <jcr:nodeProperty var="picture" node="${currentNode}" name="picture"/>
    <c:if test="${not empty picture}">
         <div class="peoplePhoto">
            <img src="${picture.node.thumbnailUrls['thumbnail']}" alt="${currentNode.properties.lastname.string} picture" >
        </div>
    </c:if>
		<div class="peopleBody">
            <h4>${currentNode.properties.firstname.string} ${currentNode.properties.lastname.string}</h4>
            <p>${currentNode.properties.function.string}</p>
            <p>${currentNode.properties.businessUnit.string}</p>
            <p>M.: ${currentNode.properties.cellular.string}</p>
            <p>T.: ${currentNode.properties.telephone.string}</p>
            <p>F.: ${currentNode.properties.fax.string}</p>
            <p><a href='mailto:${currentNode.properties.email.string}'>${currentNode.properties.email.string}</a></p>
            <p>${currentNode.properties.biography.string}</p>
		</div>
</div>

<div class="clear"></div>