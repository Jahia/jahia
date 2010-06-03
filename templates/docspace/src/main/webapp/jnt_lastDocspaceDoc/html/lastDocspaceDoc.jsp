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
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<c:set var="startNode" value="${currentNode.properties.startNode.node}"/>
<h4 class="boxdocspace-title"><fmt:message key="docspace.label.docspace.last.document"/></h4>
<ul class="docspacelist">
    <jcr:sql var="result"
             sql="select * from [jnt:file] as file where isdescendantnode(file, ['${startNode.path}']) order by file.[jcr:lastModified] desc"/>
    <c:forEach items="${result.nodes}" var="document" end="9">
        <li>
            <c:if test="${jcr:hasPermission(document, 'write')}">
                <span class="icon ${functions:fileIcon(document.name)}"></span><a
                    href="${url.basePreview}${document.path}.html"
                    title="${document.name}">${functions:abbreviate(document.name,20,30,'...')}</a>
            </c:if>
            <c:if test="${not jcr:hasPermission(document, 'write')}">
                <span class="icon ${functions:fileIcon(document.name)}"></span><a
                    href="${url.baseLive}${document.path}.docspace.html"
                    title="${document.name}">${functions:abbreviate(document.name,20,30,'...')}</a>
            </c:if>
            <span class="docspacelistinfo"><fmt:message
                            key="docspace.label.document.lastModification"/>&nbsp;<fmt:formatDate
                            value="${document.properties['jcr:lastModified'].time}" dateStyle="medium"/></span>
            <p class="docspacelistinfo2">${functions:abbreviate(functions:removeHtmlTags(document.properties['jcr:description'].string),100,150,'...')}</p>
        </li>
    </c:forEach>
</ul>