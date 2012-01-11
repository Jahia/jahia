<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
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
<c:set var="children" value="${moduleMap.currentList}"/>
<c:set var="count" value="${moduleMap.listTotalSize}"/>

<c:if test="${count > 0}">
    <c:set value="${functions:randomInt(count)}" var="itemToDisplay"/>
    <c:forEach items="${children}" var="subchild" begin="${itemToDisplay}" end="${itemToDisplay}">
        <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                         editable="true"/>
    </c:forEach>
</c:if>
<c:if test="${renderContext.editMode}">
    <template:module path="*"/>
</c:if>