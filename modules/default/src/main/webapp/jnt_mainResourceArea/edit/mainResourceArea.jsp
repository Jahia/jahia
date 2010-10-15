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
<template:addResources type="css" resources="mainresource.css"/>

<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">

        <c:set value="${currentNode.nodes}" var="currentList" scope="request"/>

        <div class="mainResourceArea<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <div class="mainResourceAreaTemplate">
                <span>List : ${currentNode.name}</span>
            </div>
            <c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}"
                       end="${moduleMap.end}">
                <template:module node="${subchild}"/>
            </c:forEach>
            <c:if test="${currentList.size == 0}">
                <div class="loremipsum">
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed dignissim tellus in metus viverra
                    pharetra. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos
                    himenaeos. Mauris eu risus elit. Donec nibh diam, commodo in adipiscing et, euismod sed orci. Donec
                    eu metus eget mauris fringilla pretium. Mauris vehicula, arcu malesuada malesuada varius, est leo
                    porttitor lacus, id fermentum lacus eros ac sem. Proin non nunc magna, nec euismod diam. Ut faucibus
                    dignissim erat sit amet sagittis. Aenean vestibulum, odio a imperdiet semper, diam lacus egestas
                    velit, non lobortis libero massa et risus. Nunc quis sagittis est. Duis non orci vel quam posuere
                    rutrum. Fusce et fringilla lorem. Nam tempus, dolor pretium consequat bibendum, odio leo feugiat
                    odio, vitae pulvinar velit ipsum sit amet augue. Fusce ultrices ultricies tortor. Nunc vel pulvinar
                    ipsum. Cras et nibh turpis, ac ornare leo. Cras elementum magna et risus porta accumsan. Duis dui
                    leo, tincidunt at blandit non, euismod eu odio.
                </div>
            </c:if>
            <c:if test="${renderContext.editMode}">
                <fmt:message key="label.studio.add.archetype" /> <template:module path="*"/>
            </c:if>
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
        <template:wrappedContent template="${currentNode.properties['j:areaTemplate'].string}"
                                 path="${currentNode.name}" nodeTypes="${nodeTypes}">
            <c:if test="${not empty currentNode.properties['j:subNodesView'].string}">
                <template:param name="subNodesTemplate" value="${currentNode.properties['j:subNodesView'].string}"/>
            </c:if>
            <c:if test="${not empty currentNode.properties['j:mockupStyle'].string}">
                <template:param name="mockupStyle" value="${currentNode.properties['j:mockupStyle'].string}"/>
            </c:if>
        </template:wrappedContent>
    </c:otherwise>
</c:choose>

