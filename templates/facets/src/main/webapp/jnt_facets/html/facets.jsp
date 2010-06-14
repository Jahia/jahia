<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<c:set var="bindedComponent" value="${currentNode.properties['j:bindedComponent'].node}"/>
<c:if test="${not empty bindedComponent}">
    <c:choose>
        <c:when test="${jcr:isNodeType(bindedComponent, 'jnt:mainResourceDisplay')}">
            <c:set var="bindedComponent" value="${renderContext.mainResource.node}"/>
        </c:when>
        <c:otherwise>
            <c:set var="bindedComponent" value="${bindedComponent}"/>
        </c:otherwise>
    </c:choose>
    
    <c:set var="facetParamVarName" value="N-${bindedComponent.name}"/>
    <c:set var="activeFacetMapVarName" value="afm-${bindedComponent.name}"/>    
    <c:if test="${not empty param[facetParamVarName] and empty activeFacetVars[facetParamVarName]}">
        <c:if test="${activeFacetVars == null}">
           <jsp:useBean id="activeFacetsVars" class="java.util.HashMap" scope="request"/>
        </c:if>
        <c:set target="${activeFacetsVars}" property="${facetParamVarName}" value="${query:decodeFacetUrlParam(param[facetParamVarName])}"/>
        <c:set target="${activeFacetsVars}" property="${activeFacetMapVarName}" value="${query:getAppliedFacetFilters(activeFacetsVars[facetParamVarName])}"/>
    </c:if>
    
    <query:definition var="listQuery" scope="request">
        <query:selector nodeTypeName="nt:base"/>
        <query:childNode path="${bindedComponent.path}"/>

        <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
            <jcr:nodeProperty node="${facet}" name="facet" var="currentFacetGroup"/>        
            <jcr:nodeProperty node="${facet}" name="field" var="currentField"/>
            <c:set var="facetNodeTypeName" value="${fn:substringBefore(currentField.string, ';')}"/>
            <c:set var="facetPropertyName" value="${fn:substringAfter(currentField.string, ';')}"/>        
            <c:choose>
                <c:when test="${jcr:isNodeType(facet, 'jnt:fieldFacet') or jcr:isNodeType(facet, 'jnt:dateFacet')}">
                    <jcr:nodeType name="${facetNodeTypeName}" var="facetNodeType"/>
                    <c:if test="${not empty currentField and not query:isFacetApplied(facetPropertyName, activeFacetsVars[activeFacetMapVarName], facetNodeType.propertyDefinitionsAsMap[facetPropertyName])}">
                        <c:set var="facetQuery" value="nodetype=${facetNodeTypeName}&key=${facetPropertyName}"/>
                        <c:choose>
                            <c:when test="${jcr:isNodeType(facet, 'jnt:dateFacet')}">                        
                                <c:set var="facetPrefix" value="date."/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="facetPrefix" value=""/>
                            </c:otherwise>                  
                        </c:choose>      
                        <c:forEach items="${facet.primaryNodeType.declaredPropertyDefinitions}" var="propertyDefinition">
                            <jcr:nodeProperty node="${facet}" name="${propertyDefinition.name}" var="facetPropValue"/>
                            <c:if test="${not empty facetPropValue.string}">
                                <c:set var="facetQuery" value="${facetQuery}&${facetPrefix}${propertyDefinition.name}=${facetPropValue.string}"/>
                            </c:if>
                        </c:forEach>              
                        <query:column columnName="rep:facet(${facetQuery})" propertyName="${facetPropertyName}"/>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <jcr:nodeProperty node="${facet}" name="query" var="currentFacetQuery"/>            
                    <c:if test="${not empty currentFacetQuery and not query:isFacetApplied(currentFacetGroup.string, activeFacetsVars[activeFacetMapVarName], null)}">
                        <query:column columnName="rep:facet(key=${facet.name}&facet.query=${currentFacetQuery.string})" propertyName="${facetPropertyName}"/>
                    </c:if>            
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:forEach items="${activeFacetsVars[activeFacetMapVarName]}" var="facet">
            <c:forEach items="${facet.value}" var="facetValue">
                <query:fullTextSearch propertyName="rep:filter(${query:escapeIllegalJCRChars(facet.key)})" searchExpression="${facetValue.value}"/>
            </c:forEach>
        </c:forEach>
    </query:definition>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>

    <%@include file="activeFacets.jspf"%>
    <div class="archives">
        <h3>Facets</h3>
        <c:forEach items="${result.facetFields}" var="currentFacet">
            <%@include file="facetDisplay.jspf"%>
        </c:forEach>
        <c:forEach items="${result.facetDates}" var="currentFacet">
            <%@include file="facetDisplay.jspf"%>
        </c:forEach>
        <c:forEach items="${result.facetQuery}" var="facetValue">
            <h4></h4>
            <ul>        
                <c:if test="${not query:isFacetValueApplied(facetValue, activeFacetsVars[activeFacetMapVarName])}">
                    <c:url var="facetUrl" value="${url.mainResource}" context="/">
                        <c:param name="${facetParamVarName}" value="${query:encodeFacetUrlParam(query:getFacetDrillDownUrl(facetValue, activeFacetsVars[facetParamVarName]))}"/>
                    </c:url>
                    <li><a href="${facetUrl}">${facetValue.key}</a> (${facetValue.value})<br/></li>
                </c:if>
            </ul>    
        </c:forEach>                        
    </div>
</c:if>
<c:if test="${renderContext.editMode}">
    facets set :
    <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
        <template:module node="${facet}"/>
    </c:forEach>
    <template:module path="*"/>
    <template:linker path="*"/>
</c:if>