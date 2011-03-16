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
<template:addResources type="javascript" resources="jquery.js,jquery.fancybox.js,jquery.fancybox.load.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>

<template:include view="hidden.header"/>
<p>
    <c:forEach items="${moduleMap.currentList}" var="child" varStatus="status">
        <jcr:node var="child" uuid="${child.properties['j:node'].string}"/>
        <c:if test="${jcr:isNodeType(child, 'jmix:thumbnail')}">
            <%--<a class="zoom" rel="group" title="${child.properties['j:node'].node.name}" href="#item${status.count}">--%>

            <a class="zoom" rel="group" title="${child.name}" href="${child.url}">
                <img src="${url.context}/repository/default${child.path}/thumbnail" alt="">
            </a>

            <%--<div style="display:none" id="item${status.count}">--%>
            <%--<template:module node="${child}" />--%>
            <%--</div>--%>
        </c:if>
    </c:forEach>
</p>

<div class="clear"></div>
<c:if test="${moduleMap.editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include view="hidden.footer"/>
