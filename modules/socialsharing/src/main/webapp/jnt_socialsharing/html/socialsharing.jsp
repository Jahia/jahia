<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="css" resources="socialsharing.css" />

<ul class="socialsharing">
    <c:forEach items="${currentNode.properties.socialNetworks}" var="socialNetwork">
        <li>
            <%--Retrieve the parent page--%>
            <c:set var="parentPage" value="${jcr:getParentOfType(currentNode, 'jnt:page')}"/>
            <c:set var="parentPageUrl" value="${url.server}${url.live}/${parentPage.path}.html"/>
            <c:set var="parentPageTitle" value="${parentPage.properties['jcr:title'].string}"/>

            <%--Generate the share url with the parent page link--%>
            <fmt:message key="${socialNetwork.string}.url.share" var="shareUrl"/>
            <c:set var="shareUrl" value="${fn:replace(shareUrl,'[url]',parentPageUrl)}"/>
            <c:set var="shareUrl" value="${fn:replace(shareUrl,'[title]',parentPageTitle)}"/>

            ${currentNode.properties['jcr:title'].string}<a href="${shareUrl}"><img src="<c:url value='${url.currentModule}/images/${socialNetwork.string}_32.png'/>" alt="${socialNetwork.string}" /></a>
        </li>
    </c:forEach>
</ul>