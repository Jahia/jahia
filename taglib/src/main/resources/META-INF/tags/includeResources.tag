<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%@ tag body-content="empty" description="Includes external resources (CSS, JavaScript, inline CSS styles, inline JavaScript), which are required for current page and were registered using addResources tag." %>
<%@ attribute name="invertCss" required="false" type="java.lang.Boolean" description="Revert list of CSS" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
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
        <c:when test="${'javascript' == staticAsset.key}">
            <script id="staticAsset${staticAsset.key}${var.index}" type="text/javascript" src="${resource}"></script>
        </c:when>
        <c:when test="${'opensearch' == staticAsset.key}">
            <link rel="search" type="application/opensearchdescription+xml" href="${fn:escapeXml(resource)}" title="${fn:escapeXml(functions:default(renderContext.staticAssetOptions[resource].title, 'Jahia search'))}" />
        </c:when>
        <c:otherwise>
            ${resource}
        </c:otherwise>
    </c:choose>
    </c:forEach>
</c:forEach>
