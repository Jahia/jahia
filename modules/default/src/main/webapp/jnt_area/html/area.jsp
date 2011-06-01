<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">

        <c:set value="${currentNode.nodes}" var="currentList" scope="request"/>

        <div class="area<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <div class="areaTemplate"> <img src="<c:url value='${url.currentModule}/images/icons/edit.png'/>" alt="edit" style="display: inline;"/>
                <span>Area : ${currentNode.name}</span>
            </div>
            <%--<c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}"--%>
                       <%--end="${moduleMap.end}">--%>
                <%--<template:module node="${subchild}"/>--%>
            <%--</c:forEach>--%>
            <%--<c:if test="${renderContext.editMode}">--%>
                <%--<fmt:message key="label.studio.add.archetype" /> <template:module path="*"/>--%>
            <%--</c:if>--%>
        </div>


    </c:when>
    <c:otherwise>
        <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="restrictions" scope="request"/>
        <c:if test="${not empty restrictions}">
            <c:forEach items="${restrictions}" var="value">
                <c:if test="${not empty nodeTypes}">
                    <c:set var="nodeTypes" value="${nodeTypes} ${value.string}"/>
                </c:if>
                <c:if test="${empty nodeTypes}">
                    <c:set var="nodeTypes" value="${value.string}"/>
                </c:if>
            </c:forEach>
        </c:if>
        <c:set var="listLimit" value="${currentNode.properties['j:numberOfItems'].long}"/>
        <c:if test="${empty listLimit}">
            <c:set var="listLimit" value="${-1}"/>
        </c:if>
        <template:area view="${currentNode.properties['j:areaView'].string}" listLimit="${listLimit}"
                                 path="${currentNode.name}" nodeTypes="${nodeTypes}" mockupStyle="${currentNode.properties['j:mockupStyle'].string}">
            <c:if test="${not empty currentNode.properties['j:subNodesView'].string}">
                <template:param name="subNodesView" value="${currentNode.properties['j:subNodesView'].string}"/>
            </c:if>
            <c:if test="${not empty currentNode.properties['j:mockupStyle'].string}">
                <template:param name="mockupStyle" value="${currentNode.properties['j:mockupStyle'].string}"/>
            </c:if>
        </template:area>
    </c:otherwise>
</c:choose>

