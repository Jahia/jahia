<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
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
<template:addResources type="css" resources="tabularList.css"/>
<template:addResources type="javascript" resources="jquery.min.js,ajaxreplace.js"/>
<c:set var="displayTab" value="${not empty renderContext.mainResource.moduleParams.displayTab ? renderContext.mainResource.moduleParams.displayTab : param.displayTab}"/>
<div id="tabs${currentNode.name}">
    <div class="idTabsContainer"><!--start idTabsContainer-->

        <ul class="idTabs">
            <c:forEach items="${jcr:getChildrenOfType(currentNode, 'jnt:contentList')}" var="subList" varStatus="status">
                <c:if test="${status.first}">
                    <c:set var="displayList" value="${subList}"/>
                </c:if>
                <c:if test="${not empty displayTab}">
                    <c:if test="${displayTab eq subList.name}">
                        <c:set var="displayList" value="${subList}"/>
                    </c:if>
                </c:if>
                <c:choose>
                    <c:when test="${(empty displayTab and status.first) or (displayTab eq subList.name)}">
                        <li>
                            <a class="selected"><span>${fn:escapeXml(subList.displayableName)}</span></a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${renderContext.editMode}">
                                <li>
                                    <a href="<c:url value='${url.mainResource}?displayTab=${subList.name}'/>"><span>${fn:escapeXml(subList.displayableName)}</span></a>
                                </li>
                            </c:when>
                            <c:otherwise>
                                <c:set var="res" value="${renderContext.ajaxRequest and not empty renderContext.ajaxResource ? renderContext.ajaxResource.node.path : renderContext.mainResource.node.path}"/>
                                <li>
                                    <a onclick="jreplace('tabs${currentNode.name}', '<c:url value="${url.base}${currentNode.path}.html.ajax"/>',{displayTab:'${subList.name}',mainResource:'${res}.html'},null,true);"><span>${subList.displayableName}</span></a>
                                </li>
                            </c:otherwise>
                        </c:choose>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </ul>
    </div>
    <c:if test="${not empty displayList}">
        <div class="tabContainer"><!--start tabContainer-->
            <template:list path="${displayList.path}"/>
            <div class="clear"></div>
        </div>
    </c:if>
    <!--stop tabContainer-->
</div>
<c:if test="${renderContext.editMode}">
    <template:module path="*"/>
</c:if>