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

<template:include view="hidden.header"/>
<c:set var="firstInLevel" value="${statusNavMenu.first}"/>
<c:set var="lastInLevel" value="${statusNavMenu.last}"/>
<c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="menuStatus">
    <c:set var="listItemCssClass"
           value="${jcr:hasChildrenOfType(subchild,'jnt:navMenu,jmix:navMenuItem') ? 'hasChildren' : 'noChildren'}${(menuStatus.first and firstInLevel) ? ' firstInLevel' : ''}${(menuStatus.last and lastInLevel) ? ' lastInLevel' : ''}"
           scope="request"/>
    <c:set var="statusNavMenu" value="${menuStatus}" scope="request"/>
    <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                     editable="${moduleMap.editable}"/>
</c:forEach>
<c:if test="${not omitFormatting}">
    <div class="clear"></div>
</c:if>
<c:if test="${moduleMap.editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include view="hidden.footer"/>
