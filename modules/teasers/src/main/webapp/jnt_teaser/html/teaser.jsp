<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<template:addResources type="css" resources="teaser.css"/>
<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

 <%--<jcr:nodeProperty node="${currentNode}" name="link" var="link"/>--%>
<div class="boxteaser">
    <div class="teaser teaser-fixed-height">
        <img src="${image.node.url}" class="floatleft" />
        <div class="teaser-content">
            <h3 class="teaser-title title"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
            <p> ${currentNode.properties.abstract.string}</p>
        </div>
        <template:addCacheDependency uuid="${currentNode.properties.link.string}"/>
        <c:if test="${not empty currentNode.properties.link.node}">
            <div class="more">
                <span><a href="<c:url value='${url.base}${currentNode.properties.link.node.path}.html'/>"><fmt:message key="jnt_teaser.readMore"/></a></span>
            </div>
        </c:if>
    </div>
</div>