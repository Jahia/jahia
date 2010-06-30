<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>

<div class="newsMLItem"><!--start newsListItem -->
    <div class="article-meta">
        <fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
            value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
        <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
        <span class="clear"/>
    </div>
    <h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>
    <div class="article-left"></div>

    <div class="article-text">
    <c:set var="currentList" value="${currentNode.nodes}" scope="request"/>
    <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status">
        <div class="newsMLItem newsMLItem-box-style${(status.index mod 2)+1}">
            <template:module node="${subchild}" template="default"/>
        </div>
    </c:forEach>
    </div>
    
    <jcr:nodeProperty node="${currentNode}" name="associatedWith" var="associatedWiths"/>

    On the same subject...
    
    <c:forEach items="${associatedWiths}" var="prop">
        <jcr:sql var="relatedNodes"
                 sql="select * from [jnt:newsMLItem] as news where news.[publicIdentifier] like '${prop.string}%'" limit="5"/>
        <c:forEach items="${relatedNodes.nodes}" var="relatedNode">
            <template:module path="${relatedNode.path}" />
        </c:forEach>
    </c:forEach>

</div>