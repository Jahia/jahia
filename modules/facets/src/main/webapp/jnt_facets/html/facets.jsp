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
<c:set var="boundComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent}">
    <c:set var="facetParamVarName" value="N-${boundComponent.name}"/>
    <c:set var="activeFacetMapVarName" value="afm-${boundComponent.name}"/>
    <c:if test="${not empty param[facetParamVarName] and empty activeFacetsVars[facetParamVarName]}">
        <c:if test="${activeFacetsVars == null}">
           <jsp:useBean id="activeFacetsVars" class="java.util.HashMap" scope="request"/>
        </c:if>
        <c:set target="${activeFacetsVars}" property="${facetParamVarName}" value="${functions:decodeUrlParam(param[facetParamVarName])}"/>
        <c:set target="${activeFacetsVars}" property="${activeFacetMapVarName}" value="${facet:getAppliedFacetFilters(activeFacetsVars[facetParamVarName])}"/>
    </c:if>

    <jsp:useBean id="facetLabels" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueLabels" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueFormats" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueRenderers" class="java.util.HashMap" scope="request"/>
    <jsp:useBean id="facetValueNodeTypes" class="java.util.HashMap" scope="request"/>

    <c:choose>
        <c:when test="${jcr:isNodeType(boundComponent, 'jnt:contentRetrieval')}">
            <jcr:nodeProperty node="${boundComponent}" name='j:startNode' var="startNode"/>
            <jcr:nodeProperty node="${boundComponent}" name='j:criteria' var="criteria"/>
            <jcr:nodeProperty node="${currentNode}" name='j:sortDirection' var="sortDirection"/>
            <jcr:nodeProperty node="${boundComponent}" name='j:type' var="type"/>
            <jcr:nodeProperty node="${boundComponent}" name="j:filter" var="filters"/>
            <query:definition var="mainQuery" scope="page">
                <query:selector nodeTypeName="${type.string}"/>
                <query:descendantNode path="${not empty startNode and not empty startNode.node ? startNode.node.path : renderContext.site.path}"/>
                <query:or>
                    <c:forEach var="filter" items="${filters}">
                        <c:if test="${not empty filter.string}">
                            <query:equalTo propertyName="j:defaultCategory" value="${filter.string}"/>
                        </c:if>
                    </c:forEach>
                </query:or>
            </query:definition>
        </c:when>
        <c:when test="${jcr:isNodeType(boundComponent, 'jnt:query')}">
            <jcr:nodeProperty node="${boundComponent}" name="jcr:statement" var="query"/>
            <jcr:nodeProperty node="${boundComponent}" name="jcr:language" var="lang"/>
            <c:if test="${lang.string == 'JCR-SQL2'}">
                <query:definition var="mainQuery" statement="${query.string}" scope="page"/>
            </c:if>
        </c:when>
        <c:otherwise>
            <query:definition var="mainQuery" scope="page">
                <query:selector nodeTypeName="nt:base"/>
                <c:set var="descendantNode" value="${fn:substringAfter(boundComponent.path,'/sites/')}"/>
                <c:set var="descendantNode" value="${fn:substringAfter(descendantNode,'/')}"/>
                <query:descendantNode path="/sites/${renderContext.site.name}/${descendantNode}"/>
            </query:definition>
        </c:otherwise>
    </c:choose>
    <query:definition var="listQuery" qom="${mainQuery}" scope="request">
        <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
            <jcr:nodeProperty node="${facet}" name="facet" var="currentFacetGroup"/>
            <jcr:nodeProperty node="${facet}" name="field" var="currentField"/>
            <c:set var="facetNodeTypeName" value="${fn:substringBefore(currentField.string, ';')}"/>
            <c:set var="facetPropertyName" value="${fn:substringAfter(currentField.string, ';')}"/>
            <c:if test="${!empty facetNodeTypeName}">
            <jcr:nodeType name="${facetNodeTypeName}" var="facetNodeType"/>
            <c:if test="${not empty facetNodeType}">
                <c:set target="${facetValueNodeTypes}" property="${facetPropertyName}" value="${facetNodeType}" />
            </c:if>
            </c:if>
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
                <c:when test="${jcr:isNodeType(facet, 'jnt:fieldFacet') or jcr:isNodeType(facet, 'jnt:fieldHierarchicalFacet') or jcr:isNodeType(facet, 'jnt:dateFacet')}">
                    <c:choose>
                        <c:when test="${jcr:isNodeType(facet, 'jnt:dateFacet')}">
                            <jcr:nodeProperty node="${facet}" name="labelFormat" var="currentFacetValueFormat" />
                            <c:if test="${not empty currentFacetValueFormat.string}">
                                <c:set target="${facetValueFormats}" property="${facetPropertyName}" value="${currentFacetValueFormat.string}" />
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <jcr:nodeProperty node="${facet}" name="labelRenderer" var="currentFacetValueRenderer" />
                            <c:if test="${not empty currentFacetValueRenderer.string}">
                                <c:set target="${facetValueRenderers}" property="${facetPropertyName}" value="${currentFacetValueRenderer.string}" />
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${not empty currentField and not facet:isFacetApplied(facetPropertyName, activeFacetsVars[activeFacetMapVarName], facetNodeType.propertyDefinitionsAsMap[facetPropertyName])}">
                        <c:set var="facetQuery" value="nodetype=${facetNodeTypeName}&key=${facetPropertyName}${minCountParam}"/>
                        <c:set var="paramPrefix" value="${jcr:isNodeType(facet, 'jnt:dateFacet') ? 'date.' : ''}"/>
                        <c:if test="${not empty currentFacetValueRenderer.string}">
                            <c:set var="facetQuery" value="${facetQuery}&${paramPrefix}labelRenderer=${currentFacetValueRenderer.string}"/>
                        </c:if>
                        <c:forEach items="${facet:getPropertyDefinitions(facet)}" var="propertyDefinition">
                            <jcr:nodeProperty node="${facet}" name="${propertyDefinition.name}" var="facetPropValue"/>
                            <c:choose>
                                <c:when test="${functions:isIterable(facetPropValue)}">
                                    <c:forEach items="${facetPropValue}" var="facetPropValueItem">
                                        <c:if test="${not empty facetPropValueItem.string}">
                                            <c:set var="facetQuery" value="${facetQuery}&${paramPrefix}${propertyDefinition.name}=${facetPropValueItem.string}"/>
                                        </c:if>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${not empty facetPropValue.string}">
                                        <c:set var="paramValue" value="${facetPropValue.string}" />
                                        <c:if test="${jcr:isNodeType(facet, 'jnt:fieldHierarchicalFacet') and propertyDefinition.name == 'prefix'}">
                                            <c:set var="currentActiveFacets" value="${activeFacetsVars[activeFacetMapVarName][facetPropertyName]}" />
                                            <c:choose>
                                                <c:when test="${not empty currentActiveFacets}">
                                                    <c:set var="paramValue" value="${facet:getDrillDownPrefix(currentActiveFacets[fn:length(currentActiveFacets) - 1].key)}" />
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="paramValue" value="${facet:getIndexPrefixedPath(facetPropValue.string)}" />
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                        <c:set var="facetQuery" value="${facetQuery}&${paramPrefix}${propertyDefinition.name}=${paramValue}"/>
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
                            <c:set var="currentFacetQuery" value="${includeLower == 'true' ? '[' : '{'}${start.string} TO ${end.string}${includeUpper == 'true' ? ']' : closeBrace}"/>
                        </c:when>
                        <c:when test="${jcr:isNodeType(facet, 'jnt:queryFacet')}">
                            <jcr:nodeProperty node="${facet}" name="query" var="currentFacetQuery"/>
                            <c:set var="currentFacetQuery" value="${currentFacetQuery.string}"/>
                        </c:when>
                    </c:choose>
                    <jcr:nodeProperty node="${facet}" name="valueLabel" var="currentFacetValueLabel"/>
                    <c:if test="${not empty currentFacetValueLabel.string and not empty currentFacetQuery}">
                        <c:set target="${facetValueLabels}" property="${currentFacetQuery}" value="${currentFacetValueLabel.string}"/>
                    </c:if>
                    <c:if test="${not empty currentFacetLabel.string and not empty currentFacetQuery}">
                        <c:set target="${facetLabels}" property="${currentFacetQuery}" value="${currentFacetLabel.string}"/>
                    </c:if>
                    <c:if test="${not empty currentFacetQuery and not facet:isFacetApplied(currentFacetQuery, activeFacetsVars[activeFacetMapVarName], null)}">
                        <query:column columnName="rep:facet(nodetype=${facetNodeTypeName}&key=${facet.name}${minCountParam}&facet.query=${currentFacetQuery})" propertyName="${not empty facetPropertyName ? facetPropertyName : 'rep:facet()'}"/>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:forEach items="${activeFacetsVars[activeFacetMapVarName]}" var="facet">
            <c:forEach items="${facet.value}" var="facetValue">
                <query:fullTextSearch propertyName="rep:filter(${jcr:escapeIllegalJcrChars(facet.key)})" searchExpression="${facetValue.value}"/>
            </c:forEach>
        </c:forEach>
    </query:definition>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>
    <div class="facets">
        <%@include file="activeFacets.jspf"%>
        <c:if test="${facet:isUnappliedFacetExisting(result, activeFacetsVars[activeFacetMapVarName])}">
            <h4><fmt:message key="facets.SelectFilter"/></h4> <br/>
        </c:if>
        <c:forEach items="${result.facetFields}" var="currentFacet">
            <%@include file="facetDisplay.jspf"%>
        </c:forEach>
        <c:forEach items="${result.facetDates}" var="currentFacet">
            <%@include file="facetDisplay.jspf"%>
        </c:forEach>
        <c:set var="currentFacetLabel" value=""/>
        <c:set var="mappedFacetLabel" value=""/>
        <c:forEach items="${result.facetQuery}" var="facetValue" varStatus="iterationStatus">
            <facet:facetLabel currentActiveFacet="${facetValue}" facetLabels="${facetLabels}" display="false"/>
            <c:if test="${iterationStatus.first or (mappedFacetLabel != currentFacetLabel and not empty mappedFacetLabel)}">
                <c:set var="currentFacetLabel" value="${mappedFacetLabel}"/>
                <c:if test="${not empty currentFacetLabel}">
                    </ul>
                </c:if>

                <div class="facetsList">
                <h5>${mappedFacetLabel}</h5>
                <ul>
            </c:if>
            <c:if test="${not facet:isFacetValueApplied(facetValue, activeFacetsVars[activeFacetMapVarName])}">
                <c:set var="facetDrillDownUrl" value="${facet:getFacetDrillDownUrl(facetValue, activeFacetsVars[facetParamVarName])}"/>
                <c:url var="facetUrl" value="${url.mainResource}">
                    <c:param name="${facetParamVarName}" value="${functions:encodeUrlParam(facetDrillDownUrl)}"/>
                </c:url>
                <li><a href="${facetUrl}"><facet:facetValueLabel currentActiveFacetValue="${facetValue}" facetValueLabels="${facetValueLabels}"/></a> (${facetValue.value})<br/></li>
            </c:if>
        </c:forEach>
        <c:if test="${not empty currentFacetLabel}">
            </ul>
            </div>
        </c:if>
    </div>
</c:if>
<c:if test="${renderContext.editMode}">
    <fmt:message key="facets.facetsSet"/> :
    <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
        <template:module node="${facet}"/>
    </c:forEach>
    <template:module path="*"/>
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName,':','_')}"/>
</c:if>