<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utils" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="css" resources="contribute.min.css"/>

<div class="listEditToolbar">
<ul class="subnodesList">
<c:forEach items="${jcr:getChildrenOfType(currentNode, 'jnt:content')}" var="child" varStatus="status">
    <c:set var="markedForDeletion" value="${jcr:isNodeType(child, 'jmix:markedForDeletion')}"/>
    <c:set var="markedForDeletionRoot" value="${jcr:isNodeType(child, 'jmix:markedForDeletionRoot')}"/>
    <c:set var="nodeName" value="${!empty child.propertiesAsString['jcr:title'] ? child.propertiesAsString['jcr:title'] : child.name}"/>
    <li>
        <input type="checkbox" class="jahiaCBoxContributeContent" name="${child.identifier}" ${child.locked ? 'disabled=true':''}/>
        <c:if test="${markedForDeletion}">
            <span class="markedForDeletion">
        </c:if>
        <c:url value="${url.base}${child.path}.editContent.html" var="childUrl"/>
        <a href="${childUrl}">${fn:escapeXml(nodeName)}</a>
        <c:if test="${markedForDeletion}">
            </span>
        </c:if>
        <c:if test="${not status.first}">
            <button id="moveUp-${currentNode.identifier}-${status.index}"
                    onclick="invert('${child.path}','${previousChild.path}', '<c:url value="${url.base}"/>', '${currentNode.UUID}', null,null)">
                <span class="icon-contribute icon-moveup"></span><fmt:message key="label.move.up"/></button>
        </c:if>
        <c:if test="${not status.last}">
            <button id="moveDown-${currentNode.identifier}-${status.index}"
                    onclick="document.getElementById('moveUp-${currentNode.identifier}-${status.index+1}').onclick()">
                <span class="icon-contribute icon-movedown"></span><fmt:message key="label.move.down"/></button>
        </c:if>
        <c:set var="previousChild" value="${child}"/>
        <c:if test="${!child.locked && !markedForDeletion}">
            <fmt:message key="message.remove.single.confirm" var="i18nDeleteConfirm">
                <fmt:param value="${child.displayableName}"/>
            </fmt:message>
            <fmt:message key="label.comment" var="i18nDeleteComment"/>
            <button id="delbtn-${child.identifier}" onclick="deleteSingleNode('${child.identifier}',true);">
                <span class="icon-contribute icon-delete"></span><fmt:message key="label.delete"/></button>
        </c:if>
        <c:if test="${markedForDeletion && fn:length(child.properties['j:lockTypes']) <= 1}">
            <fmt:message key="message.undelete.confirm" var="i18nUndeleteConfirm">
                <fmt:param value="${child.displayableName}"/>
            </fmt:message>
            <button id="undelbtn-${child.identifier}"
                    onclick="deleteSingleNode('${child.identifier}',false);">
                <span class="icon-contribute icon-undelete"></span><fmt:message key="label.undelete"/></button>
            <jcr:nodeProperty node="${child}" name="j:published" var="childPublished"/>
            <c:if test="${empty childPublished}">
                <fmt:message key="message.remove.single.confirm" var="i18nDeleteConfirm">
                    <fmt:param value="${child.displayableName}"/>
                </fmt:message>
                <button id="delbtn-${child.identifier}"
                        onclick="deleteSingleNode('${child.identifier}');">
                    <span class="icon-contribute icon-delete"></span><fmt:message key="label.deletePermanently"/></button>
            </c:if>
            <c:if test="${not empty childPublished}">
                <fmt:message key="message.requestDeletionApproval.confirm" var="i18nPublishConfirm">
                    <fmt:param value="${child.displayableName}"/>
                </fmt:message>
                <button id="pubbtn-${child.identifier}"
                        onclick="publishNodes(new Array('${child.identifier}'), '${functions:escapeJavaScript(i18nPublishConfirm)}');">
                    <span class="icon-contribute icon-publish"></span><fmt:message key="label.requestDeletionApproval"/></button>
            </c:if>
        </c:if>
    </li>
</c:forEach>
</ul>
</div>
