<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>

 <jcr:nodeProperty node="${currentNode}" name="date" var="newsDate"/>
 <jcr:nodeProperty node="${currentNode}" name="desc" var="newsDesc"/>
 <jcr:nodeProperty node="${currentNode}" name="image" var="newsImage"/>

<div class="newsSummary">
    <!--start newsListItem -->
    <c:url value="${url.files}${newsImage.node.path}" var="imageUrl"/>
    <div class="newsSummaryImg"><img src="${imageUrl}" alt='<jcr:nodeProperty node="${currentNode}" name="jcr:title"/>'/></div>
    <h4><a href="<c:url value='${url.base}${currentNode.path}.html'/>"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a></h4>
    <p class="newsSummaryresume"> ${fn:substring(functions:removeHtmlTags(newsDesc.string),0,120)}</p>
    <div class="clear"> </div>
</div>