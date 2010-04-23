<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<div class="filterList">
    <h3>Active Facets</h3>    
    <ul>
        <c:forEach items="${activeFacetsMap}" var="facet">
                <li><a href="${url.mainResource}?delPropName=${facet.key}">
                    <c:choose>
                        <c:when test="${facet.key == 'j:defaultCategory'}">
                            <jcr:node var="category" uuid="${facet.value}"/>${category.name}
                        </c:when>
                        <c:otherwise>
                            ${facet.value}
                        </c:otherwise>
                    </c:choose>
                </a></li>
        </c:forEach>
    </ul>
</div>
<div class="archives">
    <h3>Facets</h3>
    <c:forEach items="${result.facetFields}" var="currentFacet">
        <h4>${currentFacet.name}</h4>
        <ul>
            <c:forEach items="${currentFacet.values}" var="facetValue">
                <c:if test="${facetValue.count > 0 }">
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
                </c:if>
            </c:forEach>
        </ul>
    </c:forEach>
</div>