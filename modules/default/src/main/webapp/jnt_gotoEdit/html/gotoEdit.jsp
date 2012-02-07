<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="css" resources="goto-links.css"/>
<c:if test="${!renderContext.settings.distantPublicationServerMode
and renderContext.mainResource.node.properties['j:originWS'].string ne 'live'
and not jcr:isNodeType(renderContext.mainResource.node.resolveSite, 'jmix:remotelyPublished')
}">
    <c:if test="${not renderContext.editMode}">
    <img src="${url.context}/icons/editMode.png" width="16" height="16" alt=" " role="presentation"
         style="position:relative; top: 4px; margin-right:2px; " />
    <a href="<c:url value='${url.edit}'/>">
    <c:if test="${!empty currentNode.properties['jcr:title']}">
        ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
    </c:if>
    <c:if test="${empty currentNode.properties['jcr:title']}">
        <fmt:message key="label.editMode"/>
    </c:if>
    </a>
    </c:if>
    <c:if test="${renderContext.editMode}">
        <img src="${url.context}/icons/editMode.png" width="16" height="16" alt=" " role="presentation"
             style="position:relative; top: 4px; margin-right:2px; " />
        <c:if test="${!empty currentNode.properties['jcr:title']}">
            ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
        </c:if>
        <c:if test="${empty currentNode.properties['jcr:title']}">
            <fmt:message key="label.editMode"/>
        </c:if>
    </c:if>
</c:if>

