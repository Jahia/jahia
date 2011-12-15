<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:include view="hidden.header"/>
<c:if test="${not empty moduleMap.currentList}">
    <ul>
        <c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}">
            <li>
                <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
                <c:choose>
                    <c:when test='${jcr:isNodeType(subchild, "nt:file")}'>
                        <a href="<c:url value='${url.files}${subchild.path}'/>"><c:out
                                value="${not empty title && not empty title.string ? title.string : subchild.name}"/></a>
                    </c:when>
                    <c:when test='${jcr:isNodeType(subchild, "jmix:nodeReference")}'>
                        <jcr:nodeProperty node="${subchild.properties['j:node'].node}" name="jcr:title" var="title"/>
                        <a href="<c:url value='${url.base}${subchild.properties["j:node"].node.path}.html'/>"><c:out
                                value="${not empty title && not empty title.string ? title.string : subchild.properties['j:node'].node.name}"/></a>
                    </c:when>
                    <c:otherwise>
                        <a href="<c:url value='${url.base}${subchild.path}.html'/>"><c:out
                                value="${not empty title && not empty title.string ? title.string : subchild.name}"/></a>
                    </c:otherwise>
                </c:choose>
            </li>
        </c:forEach>
        <c:if test="${functions:length(moduleMap.currentList) == 0 and not empty moduleMap.emptyListMessage}">
            ${moduleMap.emptyListMessage}
        </c:if>
    </ul>
</c:if>
<c:if test="${moduleMap.editable and renderContext.editMode && !resourceReadOnly}">
    <template:module path="*"/>
 </c:if>
<div class="clear"></div>
<template:include view="hidden.footer"/>
