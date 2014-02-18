<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set var="source" value="${currentNode.properties.source.node}"/>

<c:choose>
    <c:when test="${empty source}">
        <fmt:message key="jnt_video.source.empty" />
    </c:when>
    <c:otherwise>
<%-- Include the VideoJS Library --%>
<template:addResources type="css" resources="video-js.css"/>
<template:addResources type="javascript" resources="video.js"/>

<template:addResources>
<script type="text/javascript">
    videojs.options.flash.swf = <c:url value="${url.server}${url.context}/modules/assets/swf/video-js.swf" />
</script>
</template:addResources>

<jcr:node path="${source.path}/jcr:content" var="sourceContent" />
<jcr:nodeProperty node="${sourceContent}" name="jcr:mimeType" var="mimeType" />

<video id="video-${currentNode.identifier}" class="video-js vjs-default-skin" controls <c:if test="${currentNode.properties.autoplay.boolean}">autoplay</c:if> preload="auto"
       width="${currentNode.properties.width.string}" height="${currentNode.properties.height.string}"
       data-setup='{<c:if test="${currentNode.properties.forceFlashPlayer.boolean}">"techOrder":["flash", "html5"]</c:if>}'>
  <source src="${source.url}" type='${mimeType.string == "video/x-f4v" ? "video/mp4" : mimeType.string}' />
</video>
    </c:otherwise>
</c:choose>
