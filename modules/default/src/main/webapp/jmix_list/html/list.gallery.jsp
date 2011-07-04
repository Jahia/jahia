<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:if test="${not renderContext.editMode}">
    <template:addResources type="javascript" resources="jquery.min.js,jquery.fancybox.js,jquery.fancybox.load.js"/>
    <template:addResources type="css" resources="jquery.fancybox.css"/>
</c:if>
<template:include view="hidden.header"/>
<p>
    <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status">
        <c:if test="${jcr:isNodeType(subchild, 'jmix:nodeReference')}">
            <jcr:node var="referedNode" uuid="${subchild.properties['j:node'].string}"/>
            <c:if test="${jcr:isNodeType(referedNode, 'jmix:thumbnail')}">
                <c:choose>
                    <c:when test="${not renderContext.editMode}">
                        <a class="zoom" rel="group" title="${referedNode.name}" href="${referedNode.url}?r=1">
                            <c:url value="${referedNode.thumbnailUrls['thumbnail']}" var="imgUrl" />
                            <img src="${imgUrl}" alt=""/>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                                         editable="${moduleMap.editable}"/>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </c:if>
    </c:forEach>
</p>

<div class="clear"></div>
<c:if test="${moduleMap.editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include view="hidden.footer"/>
