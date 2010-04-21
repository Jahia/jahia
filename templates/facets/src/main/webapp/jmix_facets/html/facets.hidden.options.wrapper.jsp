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
<c:set var="renderOptions" value="before" scope="request"/>
<div class="archives">
    <h3>Facets</h3>
<c:forEach items="${result.facetFields}" var="currentFacet">
            <h4>${currentFacet.name}</h4>
            <ul>
            <c:forEach items="${currentFacet.values}" var="facetValue">
                <li><a href="${url.mainResource}?${currentFacet.name}=${facetValue.name}">${facetValue.name}</a> (${facetValue.count})<br/></li>
            </c:forEach>
            </ul>
</c:forEach>
</div>