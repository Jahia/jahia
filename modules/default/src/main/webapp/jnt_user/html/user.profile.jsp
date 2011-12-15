<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="user-profile-view.css"/>

<c:forEach items="${currentNode.properties}" var="property">
    <c:if test="${property.name == 'j:firstName'}"><c:set var="firstname" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:lastName'}"><c:set var="lastname" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:function'}"><c:set var="function" value="${property.string}"/></c:if>
</c:forEach>

<div class="user-profile-view">
		<jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
        <c:if test="${not empty picture}">
          <img class='user-profile-img userProfileImage' src="${picture.node.thumbnailUrls['avatar_120']}" alt="${fn:escapeXml(title)} ${fn:escapeXml(firstname)} ${fn:escapeXml(lastname)}" width="60"
                 height="60"/>
        </c:if>
        <c:if test="${empty picture}">
            <img class='user-profile-img' src="<c:url value='${url.currentModule}/images/usersmall.png'/>" alt="" border="0" width="32"
                 height="32"/>
        </c:if>
          <h5>${fn:escapeXml(empty firstname and empty lastname ? currentNode.name : firstname)}&nbsp;${fn:escapeXml(lastname)}</h5>
          <p>${fn:escapeXml(function)}</p>
          <div class="clear"></div>
</div>
