<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="css" resources="simplesearchform.css" />

<c:set var="pathType" value="${currentNode.properties.pathType.string}"/>
<c:set var="searchPath" value="${currentNode.properties.path.node.path}"/>
<c:set var="searchType" value="${currentNode.properties.nodeType.string}"/>
<%--possible values are ; all, sites,  currentsite, pages, currentpage, custom--%>
<c:choose>
    <c:when test="${pathType eq 'all'}">
        <c:set var="searchPath" value="/"/>
    </c:when>
    <c:when test="${pathType eq 'sites'}">
        <c:set var="searchPath" value="/sites"/>
    </c:when>
    <c:when test="${pathType eq 'currentsite'}">
        <c:set var="searchPath" value="${renderContext.site.path}"/>
    </c:when>
    <c:when test="${pathType eq 'pages'}">
        <c:set var="searchPath" value="${renderContext.site.home.path}"/>
    </c:when>
    <c:when test="${pathType eq 'currentpage'}">
        <c:if test="${jcr:isNodeType(renderContext.mainResource.node,'jnt:page')}">
            <c:set var="searchPath" value="${renderContext.mainResource.node.path}"/>
        </c:if>
        <c:if test="${!jcr:isNodeType(renderContext.mainResource.node,'jnt:page')}">
            <c:set var="searchPath" value="${jcr:getParentOfType(renderContext.mainResource.node,'jnt:page').path}"/>
        </c:if>
    </c:when>
    <c:when test="${pathType eq 'custom' and !(empty searchPath)}">
        <c:set var="searchPath" value="${searchPath}"/>
    </c:when>
    <c:otherwise>
        <c:set var="searchPath" value="${renderContext.site.path}"/>
    </c:otherwise>
</c:choose>
<c:if test="${empty searchPath}">
    <c:set var="searchPath"   value="${renderContext.site.path}"/>
</c:if>
<c:if test="${empty searchType}">
    <c:set var="searchType"   value="'nt:base"/>
</c:if>
<c:set var="xPathQuery" value="/jcr:root${searchPath}//element(*, ${searchType})[jcr:contains(.,'{$q}*')]" />

<c:url var="findUrl" value="${url.find}"/>

<template:addCacheDependency uuid="${currentNode.properties.result.string}"/>
<c:if test="${not empty currentNode.properties.result.node}">
    <c:url value='${url.base}${currentNode.properties.result.node.path}.html' var="searchUrl"/>
    <s:form method="post" class="simplesearchform" action="${searchUrl}">
        <jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
        <c:if test="${not empty title.string}">
            <label for="searchCustomTerm">${fn:escapeXml(title.string)}:&nbsp;</label>
        </c:if>
        <fmt:message key='search.startSearching' var="startSearching"/>
        <s:term match="all_words" id="searchCustomTerm" value="${startSearching}" searchIn="siteContent,tags" onfocus="if(this.value==this.defaultValue)this.value='';" onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
        <s:site value="${renderContext.site.name}" display="false"/>
        <s:pagePath value="${searchPath}" display="false" includeChildren="true"/>
        <s:nodeType value="${searchType}" display="false" />
        <s:language value="${renderContext.mainResource.locale}" display="false" />
        <input class="searchsubmit" type="submit"  title="<fmt:message key='search.submit'/>" value=""/>

    </s:form><br class="clear"/>
</c:if>