<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>
<script>
    function initCuteTime() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    }
    $(document).ready(function () {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>
<c:set var="pagesizeParamName" value="pagesize${currentNode.identifier}"/>
<c:choose>
    <c:when test="${not empty param[pagesizeParamName]}">
        <c:set var="pageSize" value="${param[pagesizeParamName]}"/>
    </c:when>
    <c:when test="${not empty param.src_itemsPerPage}">
        <c:set var="pageSize" value="${param.src_itemsPerPage}"/>
    </c:when>
    <c:otherwise>
        <c:set var="pageSize" value="5"/>
    </c:otherwise>
</c:choose>
<ul class="genericListComment" id="${currentNode.UUID}">
    <jcr:sql var="numberOfPostsQuery"
             sql="select * from [jnt:post] as post  where isdescendantnode(post, ['${currentNode.path}']) order by post.[jcr:lastModified] desc"/>
    <c:set target="${moduleMap}" property="commentsList" value="${numberOfPostsQuery.nodes}"/>
    <c:set target="${moduleMap}" property="listTotalSize" value="${numberOfPostsQuery.nodes.size}"/>
    <template:initPager totalSize="${moduleMap.listTotalSize}" id="${currentNode.identifier}" pageSize="${pageSize}"/>
    <c:forEach items="${moduleMap.commentsList}" var="subchild" varStatus="status" begin="${moduleMap.begin}"
               end="${moduleMap.end}">
        <template:module node="${subchild}" />
    </c:forEach>
</ul>
<template:displayPagination/>
<template:removePager id="${currentNode.identifier}"/>
