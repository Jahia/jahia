<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.js"/>
<c:set var="targetNodePath" value="${renderContext.mainResource.node.path}"/>
<c:if test="${!empty param.targetNodePath}">
    <c:set var="targetNodePath" value="${param.targetNodePath}"/>
</c:if>
<c:if test="${!empty currentNode.properties.folder}">
    <c:set var="targetNodePath" value="${currentNode.properties.folder.node.path}"/>
</c:if>
<div id="fileList${currentNode.identifier}">
    <template:addResources type="css" resources="fileList.css"/>
    <jcr:node var="targetNode" path="${targetNodePath}"/>
    <ul class="filesList">
        <c:forEach items="${targetNode.nodes}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
                <li>
                    <c:choose>
                        <c:when test="${fn:startsWith(subchild.fileContent.contentType,'image/')}">
                            <img width="100" src="${subchild.url}"  ondblclick="CKEDITOR.instances.editContent.insertHtml('<img src=\'${subchild.url}\'/>')" alt="${fn:escapeXml(subchild.name)}"/>
                        </c:when>
                        <c:otherwise>
                            <span class="icon <%=FileUtils.getFileIcon( ((JCRNodeWrapper) pageContext.findAttribute("subchild")).getName()) %>"></span>
                            <a href="${subchild.url}"
                               title="${fn:escapeXml(not empty title.string ? title.string : subchild.name)}">
                                    ${fn:escapeXml(not empty refTitle ? refTitle : not empty title.string ? title.string : subchild.name)}
                            </a>
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>
<template:addCacheDependency node="${currentNode.parent}"/>