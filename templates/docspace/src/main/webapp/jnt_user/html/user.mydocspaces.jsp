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
<template:addResources type="css" resources="docspace.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addWrapper name="wrapper.dashboard"/>
<div id="${currentNode.identifier}">
    <jcr:sql var="result"
             sql="select * from [jnt:folder] as file order by file.[jcr:lastModified] desc"/>
    <c:set var="currentList" value="${result.nodes}" scope="request"/>
    <c:set var="listTotalSize" value="${functions:length(result.nodes)}" scope="request"/>
    <c:choose>
        <c:when test="${empty param.pagesize}">
            <c:set var="pageSize" value="40"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageSize" value="${param.pagesize}"/>
        </c:otherwise>
    </c:choose>
    <template:initPager totalSize="${listTotalSize}" pageSize="${pageSize}" id="${currentNode.identifier}"/>
    <ul class="docspacelist">
        <c:forEach items="${currentList}" var="subchild" varStatus="status" begin="${begin}" end="${end}">
            <c:if test="${jcr:hasPermission(subchild, 'write') and (not empty jcr:getParentOfType(subchild, 'jnt:page'))}">
                <li>
                    <a class="adocspace" href="${url.basePreview}${subchild.path}.html"
                       title="${subchild.name}">${functions:abbreviate(subchild.name,20,30,'...')}</a>

                    &nbsp;<span><fmt:message key="label.lastModif"/>:&nbsp;<fmt:formatDate
                        value="${subchild.properties['jcr:lastModified'].date.time}" dateStyle="short"
                        type="both"/></span>
                </li>
            </c:if>
        </c:forEach>
    </ul>
    <template:displayPagination nbItemsList="5,10,20,40,60,80,100,200"/>
    <template:removePager id="${currentNode.identifier}"/>
</div>
