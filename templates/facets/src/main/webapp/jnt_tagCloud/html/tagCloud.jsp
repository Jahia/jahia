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
<c:set var="usageThreshold"
       value="${not empty currentNode.properties['j:usageThreshold'] ? currentNode.properties['j:usageThreshold'].string : 1}"/>
<jcr:node var="tagsRoot" path="${renderContext.site.path}/tags"/>
<div class="tags">
    <h3><c:if
            test="${not empty currentNode.properties['jcr:title'] && not empty currentNode.properties['jcr:title'].string}"
            var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if
            test="${not titleProvided}"><fmt:message key="tags"/></c:if></h3>
    <%--<jcr:sql var="tags" sql="select * from [jnt:tag] as sel where ischildnode(sel,['${tagsRoot.path}']) order by sel.[j:nodename]"/>
    <c:set var="totalUsages" value="0"/>
    <jsp:useBean id="filteredTags" class="java.util.LinkedHashMap"/>
    <c:forEach items="${tags.nodes}" var="tag">
        <c:set var="count" value="${functions:length(tag.weakReferences)}"/>
        <c:if test="${usageThreshold <= 0 || count >= usageThreshold}">
            <c:set target="${filteredTags}" property="${tag.name}" value="${tag}"/>
            <c:set var="totalUsages" value="${totalUsages + count}"/>
        </c:if>
    </c:forEach>--%>

    <query:definition var="listQuery" scope="request">
        <query:selector nodeTypeName="nt:base"/>
        <query:descendantNode path="${renderContext.site.path}"/>
        <query:column columnName="rep:facet(nodetype=jmix:tagged&key=j:tags&facet.mincount=1)" propertyName="j:tags"/>
    </query:definition>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>
    <c:forEach items="${result.facetFields}" var="tags">
        <c:forEach items="${tags.values}" var="tag">
            <c:set var="totalUsages" value="${totalUsages + tag.count}"/>
        </c:forEach>
    </c:forEach>


    <c:if test="${not empty result}">
        <ul>
            <c:forEach items="${result.facetFields}" var="tags">
                <c:forEach items="${tags.values}" var="tag">
                    <c:url var="facetUrl" value="${url.mainResource}" context="/">
                        <c:param name="N-tag"
                                 value="${facet:encodeFacetUrlParam(facet:getFacetDrillDownUrl(tag, activeFacetsVars['N-tag']))}"/>
                    </c:url>
                    <jcr:node var="tagName" uuid="${tag.name}"/>
                    <li><a href="${facetUrl}" class="tag${functions:round(10 * tag.count / totalUsages)}0"
                           title="${tag.name} (${tag.count})">${tagName.name}</a></li>
                </c:forEach>
            </c:forEach>
        </ul>
    </c:if>
</div>