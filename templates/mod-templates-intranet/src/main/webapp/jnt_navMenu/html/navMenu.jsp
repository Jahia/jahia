<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="navigation.css"/>
<div id="navigation">
<div id="navbar">
<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<c:if test="${not empty title.string}">
    <span><c:out value="${title.string}"/></span>
</c:if>
<c:set var="items" value="${currentNode.nodes}"/>
<c:if test="${renderContext.editMode || not empty items}">
<ul class="main-nav">
<c:forEach items="${items}" var="menuItem">
    <c:choose>
        <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenu')}">
            <li class="submenu">
                <jcr:nodeProperty name="jcr:title" node="${menuItem}" var="title"/>
                <c:if test="${not empty title.string}">
                    <span><c:out value="${title.string}"/></span>
                </c:if>
                <div class="box-inner">
                    <ul class="submenu">
                        <template:module node="${menuItem}" editable="true" template="hidden.submenu"/>
                    </ul>
                </div>
            </li>
        </c:when>
        <c:when test="${jcr:isNodeType(menuItem, 'jmix:list')}">
            <template:module node="${menuItem}" editable="true">
                <template:param name="ommitFormatting" value="true"/>
                <template:param name="subNodesTemplate" value="hidden.navMenuItem"/>
                <template:param name="subNodesWrapper" value="wrapper.navMenuItem"/>
            </template:module>
        </c:when>
        <c:otherwise>
            <template:module node="${menuItem}" editable="true" templateWrapper="wrapper.navMenuItem">
                <template:param name="subNodesTemplate" value="hidden.navMenuItem"/>
            </template:module>
        </c:otherwise>
    </c:choose>
</c:forEach>
<c:if test="${renderContext.editMode}">
    <li><fmt:message key="label.add.new.content"/><template:module path="*"/></li>
</c:if>
</ul>
</c:if>
</div>
</div>