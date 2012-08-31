<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="tags.css"/>
<c:set var="edit" value="${renderContext.editMode}" />

<c:set var="usageThreshold"
       value="${not empty currentNode.properties['j:usageThreshold'] ? currentNode.properties['j:usageThreshold'].string : 1}"/>
<c:set var="numberOfTagsLimit"
       value="${not empty currentNode.properties['limit'] ? currentNode.properties['limit'].string : 50}"/>
<jcr:node var="tagsRoot" path="${renderContext.site.path}/tags"/>
<template:addCacheDependency flushOnPathMatchingRegexp="${renderContext.site.path}/tags/.*"/>
<div class="tags">
    <h3><c:if
            test="${not empty currentNode.properties['jcr:title'] && not empty currentNode.properties['jcr:title'].string}"
            var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if
            test="${not titleProvided}"><fmt:message key="tags"/></c:if></h3>
        <c:if test="${edit && empty currentNode.properties.resultPage}"><p><fmt:message key="warn.no.searchResultPage"/></p></c:if>

    <query:definition var="listQuery" scope="request">
        <query:selector nodeTypeName="nt:base"/>
        <query:descendantNode path="${currentNode.properties['relative'].boolean ? renderContext.mainResource.node.path : renderContext.site.path}"/>
        <query:column columnName="rep:facet(nodetype=jmix:tagged&key=j:tags&facet.mincount=${usageThreshold}&facet.limit=${numberOfTagsLimit}&facet.sort=true)" propertyName="j:tags"/>
    </query:definition>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>

    <jsp:useBean id="tagCloud" class="java.util.HashMap"/>
    <c:forEach items="${result.facetFields}" var="tags">
        <c:forEach items="${tags.values}" var="tag">
            <c:set var="totalUsages" value="${totalUsages + tag.count}"/>
            <c:set target="${tagCloud}" property="${tag.name}" value="${tag.count}"/>
        </c:forEach>
    </c:forEach>

    <c:if test="${not empty tagCloud}">
        <ul>
            <c:forEach items="${tagCloud}" var="tag">
                <jcr:node var="tagName" uuid="${tag.key}"/>
                <c:if test="${empty currentNode.properties.resultPage}">
                    <c:set var="edit" value="${true}"/>
                </c:if>
                <c:url var="facetUrl" value="${url.base}${currentNode.properties.resultPage.node.path}.html">
                    <c:param name="src_terms[0].term" value="${tagName.name}"/>
                    <c:param name="src_terms[0].fields.tags" value="true"/>
                    <c:param name="src_sites.values" value="${renderContext.site.siteKey}"/>
                    <c:param name="autoSuggest" value="false"/>
                </c:url>
                <li><c:if test="${!edit}"><a href="${facetUrl}" class="tag${functions:round(10 * tag.value / totalUsages)}0"
                                             title="${tagName.name} (${tag.value})">${tagName.name}</a></c:if>
                    <c:if test="${edit}">${tagName.name}</c:if>
                </li>
            </c:forEach>
        </ul>
    </c:if>
</div>