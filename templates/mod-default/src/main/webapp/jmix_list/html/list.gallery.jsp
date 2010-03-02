<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.fancybox.pack.js,jquery.fancybox.load.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>

<%@include file="../include/header.jspf" %>
<p>
    <c:forEach items="${currentList}" var="child" varStatus="status" begin="${begin}" end="${end}">
        <c:if test="${jcr:isNodeType(child, 'jmix:thumbnail')}">
            <%--<a class="zoom" rel="group" title="${child.properties['j:node'].node.name}" href="#item${status.count}">--%>

            <a class="zoom" rel="group" title="${child.name}" href="${url.files}${child.path}">
                <img src="${url.context}/repository/default${child.path}/thumbnail" alt="">
            </a>

            <%--<div style="display:none" id="item${status.count}">--%>
            <%--<template:module node="${child}" />--%>
            <%--</div>--%>
        </c:if>
    </c:forEach>
</p>

<div class="clear"></div>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<%@include file="../include/footer.jspf" %>
