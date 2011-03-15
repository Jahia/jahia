<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set target="${renderContext}" property="contentType" value="text/plain;charset=UTF-8"/>
<jcr:jqom var="sitemaps">
    <query:selector nodeTypeName="jmix:sitemap" selectorName="stmp"/>
    <query:or>
        <query:descendantNode path="${currentNode.path}" selectorName="stmp"/>
        <query:sameNode path="${currentNode.path}" selectorName="stmp"/>
    </query:or>
</jcr:jqom>

<c:forEach items="${sitemaps.nodes}" varStatus="status" var="sitemapEL">
<c:url value="${url.base}${sitemapEL.path}.html"/>

</c:forEach>    
