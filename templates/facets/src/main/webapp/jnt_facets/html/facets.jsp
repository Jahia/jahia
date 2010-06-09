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
    <query:definition var="listQuery" scope="request">
        <query:selector nodeTypeName="nt:base"/>
        <query:childNode path="${bindedComponent.path}"/>

        <c:forEach items="${currentNode.nodes}" var="facet">
            <jcr:nodeProperty node="${facet}" name="facet" var="currentFacet"/>
            <c:if test="${! empty currentFacet}">
                <query:column columnName="rep:facet(nodetype=${fn:substringBefore(currentFacet.string, ';')}&column=${fn:substringAfter(currentFacet.string, ';')}&facet.mincount=1)" propertyName="${fn:substringAfter(currentFacet.string, ';')}"/>
            </c:if>
        </c:forEach>
        <c:forEach items="${activeFacetsMap}" var="facet">
            <query:equalTo value="${facet.value}" propertyName="${facet.key}"/>
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
                    <li><a href="${url.mainResource}?propName=${currentFacet.name}&propValue=${facetValue.name}">
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
    <c:forEach items="${currentNode.nodes}" var="facet">
        <template:module node="${facet}"/>
    </c:forEach>
    <template:area path="${currentNode.path}/facets" nodeTypes="jnt:facet" editable="true"/>
    <template:linker path="*"/>
</c:if>