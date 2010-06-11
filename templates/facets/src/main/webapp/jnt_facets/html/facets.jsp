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
            <jcr:nodeProperty node="${facet}" name="facet" var="currentFacet"/>
            <c:set var="facetNodeTypeName" value="${fn:substringBefore(currentFacet.string, ';')}"/>
            <c:set var="facetPropertyName" value="${fn:substringAfter(currentFacet.string, ';')}"/>            
            <jcr:nodeType name="${facetNodeTypeName}" var="facetNodeType"/>
            <c:if test="${not empty currentFacet and not query:isFacetApplied(facetPropertyName, activeFacetsVars[activeFacetMapVarName], facetNodeType.propertyDefinitionsAsMap[facetPropertyName])}">
                <query:column columnName="rep:facet(nodetype=${facetNodeTypeName}&key=${facetPropertyName}&facet.mincount=1)" propertyName="${facetPropertyName}"/>
            </c:if>
        </c:forEach>
        <c:forEach items="${activeFacetsVars[activeFacetMapVarName]}" var="facet">
            <query:fullTextSearch propertyName="rep:filter(${query:escapeIllegalJCRChars(facet.key)})" searchExpression="${facet.value.value}"/>
        </c:forEach>
    </query:definition>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>

    <%@include file="activeFacets.jspf"%>
    <div class="archives">
        <h3>Facets</h3>
        <c:forEach items="${result.facetFields}" var="currentFacet">
            <h4>${currentFacet.name}</h4>
            <ul>
                <c:forEach items="${currentFacet.values}" var="facetValue">
                    <c:url var="facetUrl" value="${url.mainResource}" context="/">
                        <c:param name="${facetParamVarName}" value="${query:encodeFacetUrlParam(query:getFacetDrillDownUrl(facetValue, activeFacetsVars[facetParamVarName]))}"/>
                    </c:url>
                    <li><a href="${facetUrl}">
                        <c:choose>
                            <c:when test="${currentFacet.name == 'j:defaultCategory'}">
                                <jcr:node var="category" uuid="${facetValue.name}"/>${category.name}
                            </c:when>
                            <c:otherwise>
                                ${facetValue.name}
                            </c:otherwise>
                        </c:choose>
                    </a> (${facetValue.count})<br/></li>
            </c:forEach>
        </ul>
    </c:forEach>
</div>
</c:if>
<c:if test="${renderContext.editMode}">
    facets set :
    <c:forEach items="${jcr:getNodes(currentNode, 'jnt:facet')}" var="facet">
        <template:module node="${facet}"/>
    </c:forEach>
    <template:area path="${currentNode.path}/facets" nodeTypes="jnt:facet" editable="true"/>
    <template:linker path="*"/>
</c:if>