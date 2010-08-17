<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<template:addResources type="css" resources="news.css"/>

<div class="tc-article"><!--start newsListItem -->
    <div class="image">
        <jcr:sql var="imageQuery"
                 sql="select * from [jnt:newsMLContentItem] as c  where isdescendantnode(c,['${currentNode.path}'])"/>
        <c:forEach items="${imageQuery.nodes}" var="contentItemNode">
            <jcr:nodeProperty node="${contentItemNode}" name="image" var="newsImage"/>
            <c:if test="${not empty newsImage}">
                <img src="${newsImage.node.url}" alt="" width="73" height="73"/>
            </c:if>
        </c:forEach>
    </div>
    <span class="newsDate">
        <fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
            value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
        <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
    </span>
    <h3><a href="${url.base}${currentNode.path}.detail.html"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a></h3>
    <p>
    </p>
    <div class="tc-separator"></div>
</div>