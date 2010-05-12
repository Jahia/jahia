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
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="css" resources="960.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addWrapper name="wrapper.dashboard"/>
<div id="${currentNode.identifier}">
    <jcr:sql var="result"
             sql="select * from [jnt:content] as p where p.[jcr:createdBy]='${currentNode.name}' or p.[jcr:lastModifiedBy]='${currentNode.name}' order by p.[jcr:lastModified] desc"/>
    <c:set var="currentList" value="${result.nodes}" scope="request"/>
    <c:set var="listTotalSize" value="${fn:length(result.nodes)}" scope="request"/>
    <c:choose>
        <c:when test="${empty param.pagesize}">
            <c:set var="pageSize" value="10"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageSize" value="${param.pagesize}"/>
        </c:otherwise>
    </c:choose>
    <template:initPager totalSize="${listTotalSize}" pageSize="${pageSize}" id="${currentNode.identifier}"/>
    <ul>
        <c:forEach items="${currentList}" var="subchild" varStatus="status" begin="${begin}" end="${end}">
            <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
            <li>
                <a href="${url.base}${subchild.path}.html" target="_blank">
                    <c:choose>
                        <c:when test="${not empty title}">${title.string}</c:when>
                        <c:otherwise>${subchild.name}</c:otherwise>
                    </c:choose>
                </a>
                &nbsp;<span><fmt:message key="label.lastModif"/>:&nbsp;<fmt:formatDate value="${subchild.properties['jcr:lastModified'].date.time}" dateStyle="short" type="both"/></span>
            </li>
        </c:forEach>
    </ul>
    <template:displayPagination nbItemsList="5,10,20,40,60,80,100,200"/>
    <template:removePager id="${currentNode.identifier}"/>
</div>
