<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>


<jcr:nodeProperty node="${currentNode}" name="image" var="newsImage"/>


<div class="newsMLContentItem"><!--start newsListItem -->

    <c:if test="${not empty newsImage}">
        <div class="newsImg"><a href="${url.base}${currentNode.path}.detail.html"><img src="${newsImage.node.url}"/></a></div>
    </c:if>
    <p class="newsResume">
        ${functions:removeHtmlTags(currentNode.properties.datacontent.string)}
    </p>

</div>