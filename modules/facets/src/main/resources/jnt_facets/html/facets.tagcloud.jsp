<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="facets.css"/>
<template:addResources type="css" resources="tags.css"/>
<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent}">
    <c:set var="facetParamVarName" value="N-${boundComponent.name}"/>
    <c:set var="activeFacetMapVarName" value="afm-${boundComponent.name}"/>
    <c:if test="${not empty param[facetParamVarName] and empty activeFacetsVars[facetParamVarName]}">
        <c:if test="${activeFacetsVars == null}">
            <jsp:useBean id="activeFacetsVars" class="java.util.HashMap" scope="request"/>
        </c:if>
        <c:set target="${activeFacetsVars}" property="${facetParamVarName}"
               value="${functions:decodeUrlParam(param[facetParamVarName])}"/>
        <c:set target="${activeFacetsVars}" property="${activeFacetMapVarName}"
               value="${facet:getAppliedFacetFilters(activeFacetsVars[facetParamVarName])}"/>
    </c:if>

    <jsp:useBean id="facetLabels" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueLabels" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueFormats" class="java.util.HashMap" scope="request"/>

    <template:option node="${boundComponent}" nodetype="${boundComponent.primaryNodeTypeName},jmix:list" view="hidden.load">
        <template:param name="queryLoadAllUnsorted" value="true"/>
    </template:option>

    <facet:setupQueryAndMetadata var="listQuery" boundComponent="${boundComponent}" existingQuery="${moduleMap.listQuery}"
                                 activeFacets="${activeFacetsVars[activeFacetMapVarName]}"/>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>
    <c:if test="${(result.facetFields[0].valueCount gt 0)}">
        <c:if test="${!empty activeFacetsVars[activeFacetMapVarName]}">
            <div class="facets">
                <%@include file="activeFacets.jspf" %>
            </div>
        </c:if>
        <div class="tags">
            <h3><c:if
                    test="${not empty currentNode.properties['jcr:title'] && not empty currentNode.properties['jcr:title'].string}"
                    var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if
                    test="${not titleProvided}"><fmt:message key="tags"/></c:if></h3>
            <jsp:useBean id="tagCloud" class="java.util.HashMap"/>
            <c:forEach items="${result.facetFields}" var="tags">
                <c:forEach items="${tags.values}" var="tag">
                    <c:set var="totalUsages" value="${totalUsages + tag.count}"/>
                    <c:set target="${tagCloud}" property="${tag.name}" value="${tag.count}"/>
                </c:forEach>
            </c:forEach>

            <c:if test="${not empty tagCloud}">
                <c:forEach items="${result.facetFields}" var="currentFacet">
                    <ul>
                        <c:forEach items="${currentFacet.values}" var="facetValue">
                            <c:if test="${not facet:isFacetValueApplied(facetValue, activeFacetsVars[activeFacetMapVarName])}">
                                <c:url var="facetUrl" value="${url.mainResource}">
                                    <c:param name="${facetParamVarName}"
                                             value="${functions:encodeUrlParam(facet:getFacetDrillDownUrl(facetValue, activeFacetsVars[facetParamVarName]))}"/>
                                </c:url>
                                <li><a href="${facetUrl}"
                                       class="tag${functions:round(10 * tagCloud[facetValue.name] / totalUsages)}0">
                                    <facet:facetValueLabel currentFacetFieldName="${currentFacet.name}"
                                                           facetValueCount="${facetValue}"
                                                           facetValueLabels="${facetValueLabels}"
                                                           facetValueFormats="${facetValueFormats}"/>
                                </a></li>
                            </c:if>
                        </c:forEach>
                    </ul>
                </c:forEach>
            </c:if>

        </div>
    </c:if>
</c:if>
<c:if test="${renderContext.editMode}">
    <fmt:message key="facets.facetsSet"/> :
    <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
        <template:module node="${facet}"/>
    </c:forEach>
    <template:module path="*"/>
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName,':','_')}"/>
</c:if>