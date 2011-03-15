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
<template:addResources type="javascript" resources="jquery.js"/>
<c:if test="${currentResource.workspace eq 'live'}">
<div id="listsites${currentNode.identifier}"/>
    <script type="text/javascript">
        $('#listsites${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>');
    </script>
</div>
</c:if>
<c:if test="${currentResource.workspace ne 'live'}">

<jcr:sql var="result" sql="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites')"/>
<ul>
<c:forEach items="${result.nodes}" var="node">
    <jcr:node var="home" path="${node.path}/home"/>
    <c:if test="${jcr:hasPermission(home,'jcr:addChildNodes')}">
    <li><c:if test="${currentNode.properties.type.string eq 'edit'}">
        ${node.properties['jcr:title'].string} <a href="${url.baseEdit}${node.path}/home.html"> <img src="${url.context}/icons/editMode.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.editMode"/></a>
    </c:if>
    <c:if test="${currentNode.properties.type.string eq 'contribute'}">
        ${node.properties['jcr:title'].string} <a href="${url.baseContribute}${node.path}/home.html"><img src="${url.context}/icons/contribute.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.contribute"/></a>
    </c:if>
    <c:if test="${currentNode.properties.type.string eq 'live' && home.properties['j:published'].boolean}">
            ${node.properties['jcr:title'].string} <a href="${url.baseLive}${node.path}/home.html"><img src="${url.context}/icons/live.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.live"/></a>
    </c:if>
    </li>
    </c:if>
</c:forEach>
</ul>
</c:if>