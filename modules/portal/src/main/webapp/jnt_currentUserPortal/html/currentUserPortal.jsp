<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<template:addResources type="css" resources="tasks.css"/>
<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>


<%--<c:if test="${not jcr:isNodeType(user, 'jnt:user')}">--%>
<%--<jcr:node var="user" path="/users/${user.properties['jcr:createdBy'].string}"/>--%>
<%--</c:if>--%>
<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="/users/${renderContext.user.username}"/>
</c:if>
<jcr:node var="portal" path="${user.path}/myportal${fn:replace(renderContext.mainResource.node.path,'/','_')}"/>

<c:set var="writeable" value="${currentResource.workspace eq 'live'}"/>
<c:if test="${writeable}">
    <c:if test="${empty portal}">
        <form action="<c:url value='${url.base}${user.path}.createPortal.do'/>"
              method="post">
            <input type="hidden" name="portalPath" value="myportal${fn:replace(renderContext.mainResource.node.path,'/','_')}"/>
            <input type="hidden" name="redirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
            <input type="hidden" name="defaultPortal" value="${currentNode.properties['defaultPortal'].string}"/>
            <c:set var="ps" value=""/>
            <c:forEach items="${param}" var="p">
                <c:if test="${not empty ps}">
                    <c:set var="ps" value="${ps}&${p.key}=${p.value}" />
                </c:if>
                <c:if test="${empty ps}">
                    <c:set var="ps" value="?${p.key}=${p.value}" />
                </c:if>
            </c:forEach>

            <input type="hidden" name="newNodeOutputFormat" value="html${ps}"/>
            Create my portal : <input type="submit" name="submit">
        </form>
    </c:if>
    <c:if test="${not empty portal}">
        <template:module path="${user.path}/myportal${fn:replace(renderContext.mainResource.node.path,'/','_')}" editable="false"/>
    </c:if>
</c:if>
<c:if test="${not writeable}">
    <fmt:message key="label.portal.only.live"/>
</c:if>
