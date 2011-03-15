<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="wiki.css"/>
<template:addResources type="css" resources="markitupStyle.css"/>
<template:addResources type="css" resources="markitupWikiStyle.css"/>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.markitup.js"/>
<template:addResources type="javascript" resources="markitupWikiSet.js"/>

<c:set var="pageName" value="*"/>
<c:if test="${not empty param.newPageName}">
    <c:set var="pageName" value="${param['newPageName']}"/>
</c:if>
<c:set var="pageNode" value="${jcr:getMeAndParentsOfType(renderContext.mainResource.node, 'jnt:page')[0]}"/>
<c:if test="${jcr:isNodeType(renderContext.mainResource.node,'jnt:wikiPage')}">
    <c:set var="pageNode" value="${renderContext.mainResource.node}"/>
</c:if>
<template:tokenizedForm>
    <form name="formWiki" class="formWiki" method="post" action="<c:url value='${url.base}${pageNode.path}/${pageName}'/>">
        <input type="hidden" name="autoCheckin" value="true">
        <input type="hidden" name="nodeType" value="jnt:wikiPage">
        <c:choose>
            <c:when test="${not empty param.newPageName}">
                <input type="hidden" name="jcr:title" value="${param['wikiTitle']}">
            </c:when>
            <c:otherwise>
                <label for="title-${currentNode.identifier}"><fmt:message key="label.title"/>: </label>
                <input type="text" name="jcr:title" id="title-${currentNode.identifier}"/>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript">
            $(document).ready(function() {
                // Add markItUp! to your textarea in one line
                // $('textarea').markItUp( { Settings }, { OptionalExtraSettings } );
                $('#text-${currentNode.identifier}').markItUp(mySettings);

            });
        </script>
        <label for="text-${currentNode.identifier}"><fmt:message key="jnt_wiki.Content"/>: </label>
        <textarea class="textareawiki" name="wikiContent" rows="30" cols="85" id="text-${currentNode.identifier}"><fmt:message
                key="jnt_wiki.typeContentHere"/></textarea>

        <p>
            <label for="comment-${currentNode.identifier}"><fmt:message key="jnt_wiki.addComment"/>: </label><input
                name="lastComment" id="comment-${currentNode.identifier}"/>

        </p>
        <input class="button" type="submit"/>
    </form>
</template:tokenizedForm>