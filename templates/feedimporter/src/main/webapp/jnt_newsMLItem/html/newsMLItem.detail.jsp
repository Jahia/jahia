<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>

<div class="newsMLItem"><!--start newsListItem -->
    <h4><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h4>

    <p class="newsInfo">
        <span class="newsLabelDate"><fmt:message key="label.date"/>:</span>
            <span class="newsDate">
                <fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
                    value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
            </span>
    </p>

    <div class="more"><span><a href="${url.base}${currentNode.path}.detail.html"><fmt:message key="label.read"/>: <jcr:nodeProperty
            node="${currentNode}" name="jcr:title"/></a></span></div>
    <div class="clear"></div>


    <c:set var="currentList" value="${currentNode.nodes}" scope="request"/>
    <c:forEach items="${currentList}" var="subchild" varStatus="status">
        <div class="newsMLItem newsMLItem-box-style${(status.index mod 2)+1}">
            <template:module node="${subchild}" template="default"/>
        </div>
    </c:forEach>

</div>