<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>


<jcr:nodeProperty node="${currentNode}" name="image" var="newsImage"/>


<div class="newsListItem"><!--start newsListItem -->
    <h4><a href="<c:url value='${url.base}${currentNode.path}.html'/>"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a></h4>

    <p class="newsInfo">
        <span class="newsLabelDate"><fmt:message key="label.date"/>:</span>
            <span class="newsDate">
                <fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
                    value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
            </span>
    </p>

    <c:if test="${not empty newsImage}">
        <div class="newsImg"><a href="<c:url value='${url.base}${currentNode.path}.html'/>"><img src="${newsImage.node.url}"/></a></div>
    </c:if>
    <p class="newsResume">
        ${functions:abbreviate(functions:removeHtmlTags(currentNode.properties.desc.string),400,450,'...')}
    </p>

    <div class="more"><span><a href="<c:url value='${url.base}${currentNode.path}.html'/>"><fmt:message key="label.read"/>: <jcr:nodeProperty
            node="${currentNode}" name="jcr:title"/></a></span></div>
    <div class="clear"></div>
</div>