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
          <img class='user-profile-img' src="${picture.node.thumbnailUrls['avatar_60']}" alt="${title} ${firstname} ${lastname}" width="60"
                 height="60"/>
        </c:if>
          <h5>${empty firstname and empty lastname?currentNode.name:firstname} ${lastname}</h5>
          <p>${function}</p>
          <div class="clear"></div>
</div>
