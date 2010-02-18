<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ page contentType="text/xml;UTF-8" language="java" %>
<jcr:jqom var="sitemaps">
    <query:selector nodeTypeName="jmix:sitemap" selectorName="stmp"/>
    <query:or>
        <query:sameNode path="${currentNode.path}" selectorName="stmp"/>
    </query:or>
</jcr:jqom>
<c:if test="${empty level}">
    <c:set var="level" value="1"/>
</c:if>
<c:set var="currentLevel" value="${level}"/>
<c:forEach items="${sitemaps.nodes}" varStatus="status" var="sitemapEL">
    <c:if test="${status.first}">
        <ul class="level_${currentLevel}">
    </c:if>
    <li class="">
        <a href='<c:url value="${sitemapEL.path}.html" context="${url.base}"/>'>${sitemapEL.properties["jcr:title"].string}</a>
        <c:set var="level" scope="request" value="${currentLevel + 1}"/>
        <c:forEach items="${sitemapEL.nodes}" var="child">
            <c:if test="${jcr:isNodeType(child, 'jmix:sitemap')}">
                <template:module node="${child}" forcedTemplate="sitemap" editable="false"/>
            </c:if>
        </c:forEach>
    </li>
    <c:if test="${status.last}">
        </ul>
    </c:if>
</c:forEach>
