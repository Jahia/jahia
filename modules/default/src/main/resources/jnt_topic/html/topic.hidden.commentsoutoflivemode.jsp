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

<template:addResources type="javascript" resources="jquery.cuteTime.js" var="resourcePath"/>
<c:if test="${not empty resourcePath}">
    <c:forTokens items="${resourcePath}" delims="," var="resource" varStatus="i">
            <script>
                if ($.fn.cuteTime == undefined) {
                    $.getScript("${resource}",function(){
                        $('.timestamp').cuteTime({ refresh: 60000 });
                    });
                } else {
					$('.timestamp').cuteTime({ refresh: 60000 });
				}
            </script>
    </c:forTokens>
</c:if>
<ul class="genericListComment" id="${currentNode.UUID}">
    <c:set target="${moduleMap}" property="commentsList" value="${currentNode.nodes}"/>
    <c:set target="${moduleMap}" property="listTotalSize" value="${fn:length(currentNode.nodes)}"/>
    <c:forEach items="${moduleMap.commentsList}" var="subchild" varStatus="status">
        <template:module node="${subchild}" view="default"/>
    </c:forEach>
</ul>
<template:removePager id="${currentNode.identifier}"/>
