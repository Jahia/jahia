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

    <query:definition var="listQuery" scope="request">
        <query:selector nodeTypeName="jnt:content"/>
        <query:childNode path="${boundComponent.path}"/>

        <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
            <jcr:nodeProperty node="${facet}" name="facet" var="currentFacetGroup"/>
            <jcr:nodeProperty node="${facet}" name="field" var="currentField"/>
            <c:set var="facetNodeTypeName" value="${fn:substringBefore(currentField.string, ';')}"/>
            <c:set var="facetPropertyName" value="${fn:substringAfter(currentField.string, ';')}"/>
            <jcr:nodeType name="${facetNodeTypeName}" var="facetNodeType"/>
            <jcr:nodeProperty node="${facet}" name="mincount" var="minCount"/>
            <c:set var="minCountParam" value=""/>
            <c:if test="${not empty minCount.string}">
                <c:set var="minCountParam" value="&mincount=${minCount.string}"/>
            </c:if>

            <jcr:nodeProperty node="${facet}" name="label" var="currentFacetLabel"/>
            <c:if test="${not empty currentFacetLabel.string and not empty facetPropertyName}">
                <c:set target="${facetLabels}" property="${facetPropertyName}" value="${currentFacetLabel.string}"/>
            </c:if>

            <c:choose>
                <c:when test="${jcr:isNodeType(facet, 'jnt:fieldFacet') or jcr:isNodeType(facet, 'jnt:dateFacet')}">
                    <c:if test="${jcr:isNodeType(facet, 'jnt:dateFacet')}">
                        <jcr:nodeProperty node="${facet}" name="labelFormat" var="currentFacetValueFormat"/>
                        <c:if test="${not empty currentFacetValueFormat.string}">
                            <c:set target="${facetValueFormats}" property="${facetPropertyName}"
                                   value="${currentFacetValueFormat.string}"/>
                        </c:if>
                    </c:if>
                    <c:if test="${not empty currentField and not facet:isFacetApplied(facetPropertyName, activeFacetsVars[activeFacetMapVarName], facetNodeType.propertyDefinitionsAsMap[facetPropertyName])}">
                        <c:set var="facetQuery"
                               value="nodetype=${facetNodeTypeName}&key=${facetPropertyName}${minCountParam}"/>
                        <c:set var="facetPrefix" value="${jcr:isNodeType(facet, 'jnt:dateFacet') ? 'date.' : ''}"/>
                        <c:forEach items="${facet.primaryNodeType.declaredPropertyDefinitions}"
                                   var="propertyDefinition">
                            <jcr:nodeProperty node="${facet}" name="${propertyDefinition.name}" var="facetPropValue"/>
                            <c:choose>
                                <c:when test="${functions:isIterable(facetPropValue)}">
                                    <c:forEach items="${facetPropValue}" var="facetPropValueItem">
                                        <c:if test="${not empty facetPropValueItem.string}">
                                            <c:set var="facetQuery"
                                                   value="${facetQuery}&${facetPrefix}${propertyDefinition.name}=${facetPropValueItem.string}"/>
                                        </c:if>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${not empty facetPropValue.string}">
                                        <c:set var="facetQuery"
                                               value="${facetQuery}&${facetPrefix}${propertyDefinition.name}=${facetPropValue.string}"/>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                        <query:column columnName="rep:facet(${facetQuery})" propertyName="${facetPropertyName}"/>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${jcr:isNodeType(facet, 'jnt:rangeFacet')}">
                            <jcr:nodeProperty node="${facet}" name="start" var="start"/>
                            <jcr:nodeProperty node="${facet}" name="end" var="end"/>
                            <jcr:nodeProperty node="${facet}" name="include" var="include"/>
                            <c:set var="includeLower" value="false"/>
                            <c:set var="includeUpper" value="false"/>
                            <c:forEach items="${include}" var="includeItem">
                                <c:choose>
                                    <c:when test="${'lower' == includeItem.string}">
                                        <c:set var="includeLower" value="true"/>
                                    </c:when>
                                    <c:when test="${'upper' == includeItem.string}">
                                        <c:set var="includeUpper" value="true"/>
                                    </c:when>
                                    <c:when test="${'all' == includeItem.string}">
                                        <c:set var="includeLower" value="true"/>
                                        <c:set var="includeUpper" value="true"/>
                                    </c:when>
                                </c:choose>
                            </c:forEach>
                            <c:set var="closeBrace">}</c:set>
                            <c:set var="currentFacetQuery"
                                   value="${includeLower == 'true' ? '[' : '{'}${start.string} TO ${end.string}${includeUpper == 'true' ? ']' : closeBrace}"/>
                        </c:when>
                        <c:when test="${jcr:isNodeType(facet, 'jnt:queryFacet')}">
                            <jcr:nodeProperty node="${facet}" name="query" var="currentFacetQuery"/>
                            <c:set var="currentFacetQuery" value="${currentFacetQuery.string}"/>
                        </c:when>
                    </c:choose>
                    <jcr:nodeProperty node="${facet}" name="valueLabel" var="currentFacetValueLabel"/>
                    <c:if test="${not empty currentFacetValueLabel.string and not empty currentFacetQuery}">
                        <c:set target="${facetValueLabels}" property="${currentFacetQuery}"
                               value="${currentFacetValueLabel.string}"/>
                    </c:if>
                    <c:if test="${not empty currentFacetLabel.string and not empty currentFacetQuery}">
                        <c:set target="${facetLabels}" property="${currentFacetQuery}"
                               value="${currentFacetLabel.string}"/>
                    </c:if>
                    <c:if test="${not empty currentFacetQuery and not facet:isFacetApplied(currentFacetQuery, activeFacetsVars[activeFacetMapVarName], null)}">
                        <query:column
                                columnName="rep:facet(nodetype=${facetNodeTypeName}&key=${facet.name}${minCountParam}&facet.query=${currentFacetQuery})"
                                propertyName="${not empty facetPropertyName ? facetPropertyName : 'rep:facet()'}"/>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:forEach items="${activeFacetsVars[activeFacetMapVarName]}" var="facet">
            <c:forEach items="${facet.value}" var="facetValue">
                <query:fullTextSearch propertyName="rep:filter(${jcr:escapeIllegalJcrChars(facet.key)})"
                                      searchExpression="${facetValue.value}"/>
            </c:forEach>
        </c:forEach>
    </query:definition>
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
                                    <facet:facetValueLabel currentFacetField="${currentFacet}"
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