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
<c:set var="displayTab" value="${not empty renderContext.mainResource.moduleParams.displayTab ? renderContext.mainResource.moduleParams.displayTab : param.displayTab}"/>
<c:set var="ps" value=""/>
<c:forEach items="${param}" var="p">
    <c:if test="${p.key ne 'displayTab'}">
        <c:set var="ps" value="${ps}&${p.key}=${p.value}" />
    </c:if>
</c:forEach>
<div id="tabs${currentNode.name}">
    <div class="idTabsContainer"><!--start idTabsContainer-->
        <ul class="idTabs">
            <c:forEach items="${currentNode.nodes}" var="subList" varStatus="status">
                <c:if test="${status.first || displayTab eq subList.name}">
                    <c:set var="displayList" value="${subList}"/>
                </c:if>
                <template:module node="${subList}" view="tabularList" editable="false" >
                    <template:param name="stat" value="${status.first}"/>
                    <template:param name="displayTab" value="${displayTab}"/>
                    <template:param name="ps" value="${ps}"/>
                </template:module>
            </c:forEach>
        </ul>
    </div>
    <c:if test="${not empty displayList}">
        <div class="tabContainer"><!--start tabContainer-->
            <template:module path="${displayList.path}" view="default"/>
            <div class="clear"></div>
        </div>
    </c:if>
    <!--stop tabContainer-->
</div>
<c:if test="${renderContext.editMode}">
    <template:module path="*"/>
</c:if>
