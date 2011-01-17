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
<template:addResources type="css" resources="forum.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>
<script>
    function initCuteTime() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    }
    $(document).ready(function () {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>
<div id="forum-body">
    <div class="posts" id="${currentNode.UUID}">
        <h2><a href="${url.base}${currentNode.parent.path}.html"><jcr:nodeProperty node="${currentNode}"
                                                                                   name="threadSubject"/></a></h2>
        <a name="wrap"></a>

        <div class="forum-actions">

            <div class="forum-buttons">
                <div class="forum-post-icon"><a title="Post a new post" href="#threadPost"><span/><fmt:message
                        key="new.post"/></a></div>
            </div>
            <div class="forum-pagination">
                ${functions:length(currentNode.nodes)}&nbsp;<fmt:message key="posts"/>
            </div>

        </div>
        <c:set target="${moduleMap}" property="currentList" value="${currentNode.nodes}" />
        <c:set target="${moduleMap}" property="listTotalSize" value="${fn:length(currentNode.nodes)}" />
        <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status">
            <div class="forum-box forum-box-style${(status.index mod 2)+1}">
                <template:module node="${subchild}" template="forum"/>
            </div>
        </c:forEach>
        <div class="forum-actions">
            <div class="forum-pagination">
                ${functions:length(currentNode.nodes)}&nbsp;<fmt:message key="posts"/>
            </div>

        </div>
    </div>
</div>