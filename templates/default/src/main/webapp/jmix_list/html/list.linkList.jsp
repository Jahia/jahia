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

<template:include template="hidden.header"/>
<c:if test="${not empty currentList}">
    <ul>
        <c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">
            <li>
                <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
                <c:choose>
                    <c:when test='${jcr:isNodeType(subchild, "nt:file")}'>
                        <a href="${url.files}${subchild.path}"><c:out
                                value="${not empty title && not empty title.string ? title.string : subchild.name}"/></a>
                    </c:when>
                    <c:otherwise>
                        <a href="${url.base}${subchild.path}.html"><c:out
                                value="${not empty title && not empty title.string ? title.string : subchild.name}"/></a>
                    </c:otherwise>
                </c:choose>
            </li>
        </c:forEach>
    </ul>
</c:if>
<div class="clear"></div>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include template="hidden.footer"/>
