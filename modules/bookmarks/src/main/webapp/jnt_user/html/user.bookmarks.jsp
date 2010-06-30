<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="bookmarks.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<div id="${currentNode.identifier}">
    <jcr:sql var="result"
             sql="select * from [jnt:bookmark] as b where isdescendantnode(b,['${currentNode.path}'])"/>
    <c:set var="currentList" value="${result.nodes}" scope="request"/>
    <c:set var="listTotalSize" value="${fn:length(result.nodes)}" scope="request"/>
    <c:choose>
        <c:when test="${empty param.pagesize}">
            <c:set var="pageSize" value="40"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageSize" value="${param.pagesize}"/>
        </c:otherwise>
    </c:choose>
    <template:initPager totalSize="${moduleMap.listTotalSize}" pageSize="${pageSize}" id="${currentNode.identifier}"/>
    <ul class="userMyBookmarksList" id="${currentNode.UUID}">
        <c:forEach items="${moduleMap.currentList}" var="bookmark" varStatus="status" begin="${moduleMap.begin}" end="${moduleMap.end}">

            <li>
                <jcr:nodeProperty node="${bookmark}" name="jcr:title" var="title"/>
                <jcr:node var="myNode" path="${bookmark.path}"/>
                <a class="userMyBookmarksListIcon" href="${bookmark.properties['url'].string}">${bookmark.properties['jcr:title'].string}</a>
                &nbsp;<span>&nbsp;<fmt:formatDate
                    value="${bookmark.properties['date'].date.time}" dateStyle="short" type="both"/></span>
               <div class="floatright">
               &nbsp;<button onclick="deleteNode('${bookmark.path}', '${url.base}', '${currentNode.UUID}', '${url.current}')">
                    <span class="icon-contribute icon-delete"></span><fmt:message key="label.delete"/></button>
               </div>
               <div class="clear"></div>
            </li>
        </c:forEach>
    </ul>
    <template:displayPagination nbItemsList="5,10,20,40,60,80,100,200"/>
    <template:removePager id="${currentNode.identifier}"/>
</div>
