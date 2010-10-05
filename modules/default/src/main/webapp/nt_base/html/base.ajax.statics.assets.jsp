<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:forEach items="${renderContext.staticAssets}" var="staticAsset">

    <c:choose>
        <c:when test="${staticAsset.key eq 'css' and invertCss}">
            <c:set var="resources" value="${functions:reverse(renderContext.staticAssets[staticAsset.key])}"/>
        </c:when>
        <c:otherwise><c:set var="resources" value="${renderContext.staticAssets[staticAsset.key]}"/></c:otherwise>
    </c:choose>
    <c:forEach var="resource" items="${resources}" varStatus="var">
    <c:choose>
        <c:when test="${'css' == staticAsset.key}">
            <link id="staticAsset${staticAsset.key}${var.index}" rel="stylesheet" href="${resource}" media="screen" type="text/css"/>
        </c:when>
        <%--<c:when test="${'javascript' == staticAsset.key}">
            <script id="staticAsset${staticAsset.key}${var.index}" type="text/javascript" src="${resource}"></script>
        </c:when>--%>
        <c:when test="${'opensearch' == staticAsset.key}">
            <link rel="search" type="application/opensearchdescription+xml" href="${fn:escapeXml(resource)}" title="${fn:escapeXml(functions:default(renderContext.staticAssetOptions[resource].title, 'Jahia search'))}" />
        </c:when>
        <c:otherwise>
            <c:set var="inline">
            ${inline}
            ${resource}
            </c:set>
        </c:otherwise>
    </c:choose>
    </c:forEach>
</c:forEach>
