<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.fancybox.pack.js,jquery.fancybox.load.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>

<div id="${currentNode.UUID}">

    <c:remove var="currentList" scope="request"/>
    <c:choose>
        <c:when test="${jcr:isNodeType(currentNode, 'jmix:pager')}">
            <c:set scope="request" var="paginationActive" value="true"/>
        </c:when>
        <c:otherwise>
            <c:set var="begin" value="0" scope="request"/>
        </c:otherwise>
    </c:choose>
    <template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false">
        <template:param name="forcedSkin" value="none"/>
    </template:module>

    <c:if test="${empty editable}">
        <c:set var="editable" value="false"/>
    </c:if>
    <c:if test="${not empty paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.init"/>
    </c:if>


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
        <template:module path="*"/>
    </p>

    <div class="clear"></div>
    <c:if test="${not empty paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden"/>
    </c:if>
</div>
<template:removePager/>
