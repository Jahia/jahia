<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>


<c:choose>
    <c:when test="${not empty subNodesView}">
        <c:set target="${moduleMap}" property="subNodesView" value="${subNodesView}"/>
        <c:remove var="subNodesView" scope="request"/>
    </c:when>
    <c:when test="${jcr:isNodeType(currentNode, 'jmix:renderableList')}">
        <c:set target="${moduleMap}" property="subNodesView"
               value="${currentNode.properties['j:subNodesView'].string}"/>
    </c:when>
</c:choose>
<fmt:message key="label.noItemFound" var="notFoundLabel"/>
<c:set target="${moduleMap}" property="emptyListMessage" value="${notFoundLabel}" />
<c:set target="${moduleMap}" property="begin" value="0"/>
<template:include view="hidden.load"/>
<c:if test="${empty moduleMap.currentList and not empty moduleMap.listQuerySql}">
    <jcr:sql var="result" sql="${moduleMap.listQuerySql}"/>
    <c:set target="${moduleMap}" property="currentList" value="${result.nodes}"/>
    <c:set target="${moduleMap}" property="end" value="${functions:length(result.nodes)}"/>
    <c:set target="${moduleMap}" property="listTotalSize" value="${moduleMap.end}"/>
</c:if>
<c:set var="facetParamVarName" value="N-${currentNode.name}"/>
<c:set var="activeFacetMapVarName" value="afm-${currentNode.name}"/>
<c:if test="${not empty param[facetParamVarName] and empty activeFacetsVars[facetParamVarName]}">
    <c:if test="${activeFacetsVars == null}">
        <jsp:useBean id="activeFacetsVars" class="java.util.HashMap" scope="request"/>
    </c:if>
    <c:set target="${activeFacetsVars}" property="${facetParamVarName}"
           value="${functions:decodeUrlParam(param[facetParamVarName])}"/>
    <c:set target="${activeFacetsVars}" property="${activeFacetMapVarName}"
           value="${facet:getAppliedFacetFilters(activeFacetsVars[facetParamVarName])}"/>
</c:if>
<c:if test="${empty moduleMap.currentList and not empty moduleMap.listQuery}">
    <c:choose>
      <c:when test="${not empty moduleMap.listQueryResultVar}">
          <c:set var="resultName" value="${moduleMap.listQueryResultVar}"/>
      </c:when>
      <c:otherwise>
          <c:set var="resultName" value="result_${currentNode.identifier}"/>
      </c:otherwise>
    </c:choose>

    <query:definition var="listQuery" qom="${moduleMap.listQuery}">
        <c:forEach items="${activeFacetsVars[activeFacetMapVarName]}" var="facet">
            <c:forEach items="${facet.value}" var="facetValue">
                <c:if test="${not fn:endsWith(resultName, '-withFacetFilter')}">
                    <c:set var="resultName" value="${resultName}-withFacetFilter"/>
                </c:if>
                <query:fullTextSearch propertyName="rep:filter(${jcr:escapeIllegalJcrChars(facet.key)})"
                                      searchExpression="${facetValue.value}"/>
            </c:forEach>
        </c:forEach>
    </query:definition>
    
    <c:choose>
        <c:when test='${queryMap[resultName] eq null}'>
            <jcr:jqom var="result" qomBeanName="listQuery"/>
            <c:if test='${queryMap == null}'>
                <jsp:useBean id="queryMap" class="java.util.HashMap" scope="request"/>
            </c:if>
            <c:set target="${queryMap}" property="${resultName}" value="${result}"/>
        </c:when>
        <c:otherwise>
            <c:set var="result" value="${queryMap[resultName]}"/>
        </c:otherwise>
    </c:choose>    

    <%-- pager specific --%>
    <c:set target="${moduleMap}" property="listTotalSize" value="${functions:length(result.nodes)}"/>
    <c:set target="${moduleMap}" property="listApproxSize" value="${result.approxCount}"/>
    <c:set target="${moduleMap}" property="end" value="${moduleMap.listApproxSize > 0 ? moduleMap.listApproxSize : moduleMap.listTotalSize}"/>        

    <%-- set result --%>
    <c:set target="${moduleMap}" property="currentList" value="${result.nodes}"/>
</c:if>
<c:if test="${!empty areaResource}">
    <c:if test="${jcr:isNodeType(areaResource, 'jmix:orderedList')}">
        <jcr:sort list="${moduleMap.currentList}"
                  properties="${areaResource.properties.ignoreCase.string},${areaResource.properties.firstField.string},${areaResource.properties.firstDirection.string},${areaResource.properties.secondField.string},${areaResource.properties.secondDirection.string},${areaResource.properties.thirdField.string},${areaResource.properties.thirdDirection.string}"
                  var="currentList"/>
        <c:set value="${currentList}" target="${moduleMap}" property="currentList"/>
    </c:if>
    <c:if test="${!empty areaResource.properties['j:numberOfItems']}">
        <c:set value="${areaResource.properties['j:numberOfItems'].string -1}" target="${moduleMap}" property="end"/>
    </c:if>
</c:if>

<c:if test="${jcr:isNodeType(currentNode, 'jmix:orderedList')}">
    <jcr:sort list="${moduleMap.currentList}"
              properties="${currentNode.properties.ignoreCase.string},${currentNode.properties.firstField.string},${currentNode.properties.firstDirection.string},${currentNode.properties.secondField.string},${currentNode.properties.secondDirection.string},${currentNode.properties.thirdField.string},${currentNode.properties.thirdDirection.string}"
              var="currentList"/>
    <c:set value="${currentList}" target="${moduleMap}" property="currentList"/>
    <c:set value="true" target="${moduleMap}" property="orderedList"/>
</c:if>


<c:if test="${not empty param.filter}">
    <jcr:filter var="currentList" list="${moduleMap.currentList}" properties="${param.filter}" node="${currentNode}"/>
    <c:set value="${currentList}" target="${moduleMap}" property="currentList"/>
</c:if>

<c:if test="${empty moduleMap.editable}">
    <c:set target="${moduleMap}" property="editable" value="false"/>
</c:if>

<c:set var="beginName" value="begin_${currentNode.identifier}"/>
<c:set var="endName" value="end_${currentNode.identifier}"/>
<c:if test="${not empty requestScope[beginName]}">
    <c:set target="${moduleMap}" property="begin" value="${requestScope[beginName]}"/>
</c:if>
<c:if test="${not empty requestScope[endName]}">
    <c:set target="${moduleMap}" property="end" value="${requestScope[endName]}"/>
</c:if>
