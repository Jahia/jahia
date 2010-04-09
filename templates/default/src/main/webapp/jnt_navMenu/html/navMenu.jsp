<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addWrapper name="${empty param.navMenuWrapper ? 'wrapper.default' : param.navMenuWrapper}"/>
<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<c:if test="${not empty title.string}">
    <span><c:out value="${fn:escapeXml(title.string)}"/><c:if test="${renderContext.editMode}"> <fmt:message key="navMenu.label.submenu"/></c:if></span>
</c:if>
<c:set var="items" value="${currentNode.nodes}"/>
<c:set var="navMenuLevel" value="${fn:length(jcr:getParentsOfType(currentNode, 'jnt:navMenu')) + 1}"/>
<c:if test="${navMenuLevel eq 1 and (renderContext.editMode or renderContext.contributionMode)}">
    <p><a href="${url.base}${currentNode.path}.menuDesignTmp.html"><span><fmt:message key="navMenu.label.edit"/></span></a></p>
</c:if>
<c:if test="${not empty items}">
    <ul class="navmenu level_${navMenuLevel}">
        <c:forEach items="${items}" var="menuItem" varStatus="menuStatus">
            <c:set var="listItemCssClass"
                   value="${jcr:hasChildrenOfType(menuItem,'jnt:navMenu,jmix:navMenuItem') ? 'hasChildren' : 'noChildren'}${menuStatus.first ? ' firstInLevel' : ''}${menuStatus.last ? ' lastInLevel' : ''}"
                   scope="request"/>
            <c:if test="${jcr:isNodeType(menuItem, 'jnt:navMenu')}"><li class="${listItemCssClass}"></c:if>
            <c:set var="statusNavMenu" value="${menuStatus}" scope="request"/>
            <template:module node="${menuItem}" editable="true"
                             template="${jcr:isNodeType(menuItem, 'jnt:navMenu') ? template:'hidden.menuElement'}">
                <template:param name="subNodesTemplate" value="hidden.menuElement"/>
                <template:param name="omitFormatting" value="true"/>
            </template:module>
            <c:if test="${jcr:isNodeType(menuItem, 'jnt:navMenu')}"></li></c:if>
        </c:forEach>
    </ul>
</c:if>