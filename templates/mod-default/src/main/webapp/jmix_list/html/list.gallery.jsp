<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:remove var="currentList" scope="request"/>
<template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false" >
    <template:param name="forcedSkin" value="none" />
</template:module>

<template:addResources type="javascript" resources="jquery.min.js,jquery.fancybox.pack.js,jquery.fancybox.load.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>

<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>

<p>
    <c:forEach items="${currentList}" var="child" varStatus="status">
        <c:if test="${not empty child.properties['j:node'].node}">
            <%--<a class="zoom" rel="group" title="${child.properties['j:node'].node.name}" href="#item${status.count}">--%>

            <a class="zoom" rel="group" title="${child.properties['j:node'].node.name}" href="${url.files}${child.properties['j:node'].node.path}">
                <img src="${url.context}/repository/default${child.properties['j:node'].node.path}/thumbnail" alt="">
            </a>

            <%--<div style="display:none" id="item${status.count}">--%>
                <%--<template:module node="${child}" />--%>
            <%--</div>--%>
        </c:if>
    </c:forEach>
    <template:module path="*"/>
</p>
