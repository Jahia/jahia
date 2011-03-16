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
<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>
<c:if test="${empty param.letter}">
    <c:set value="A" var="selectedLetter"/>
</c:if>
<c:if test="${!empty param.letter}">
    <c:set var="selectedLetter" value="${param.letter}"/>
</c:if>

<div id="${currentNode.UUID}">
    <div class="alphabeticalMenu"><!--start alphabeticalMenu-->
        <template:addResources type="javascript" resources="ajaxreplace.js" />
        <div class="alphabeticalMenu"><!--start alphabeticalMenu-->
            <div class="alphabeticalNavigation">
                <c:forTokens items="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z" var="letter" delims=",">
                    <c:url var="myUrl" value="${url.current}.ajax">
                        <c:param name="letter" value="${letter}"/>
                    </c:url>
                    <span><a class="alphabeticalLetter <c:if test='${letter eq selectedLetter}'>current</c:if>" href="javascript:replace('${currentNode.UUID}','${myUrl}')" >${letter}</a></span>
                </c:forTokens>
            </div>
            <div class='clear'></div></div>

        <ul>
            <c:choose>
                <c:when test="${(renderContext.editModeConfigName eq 'studiomode') || !(renderContext.editModeConfigName eq 'editmode')}">
                    <c:forEach items="${currentNode.nodes}" var="subchild">
                        <p>
                            <template:module node="${subchild}" view="${moduleMap.subNodesTemplate}"  editable="${moduleMap.editable}" />
                        </p>
                    </c:forEach>
                </c:when>
                <c:otherwise>

                    <jcr:sql var="list"
                             sql="select * from [jnt:content] as content  where
              (content.['${currentNode.properties.field.string}'] like '${fn:toLowerCase(selectedLetter)}%' or
              content.['${currentNode.properties.field.string}'] like '${fn:toUpperCase(selectedLetter)}%') and
               isdescendantnode(content, ['${currentNode.path}'])
               order by content.['${currentNode.properties.field}']"/>

                    <c:forEach items="${list.nodes}" var="subchild">
                        <p>
                            <template:module node="${subchild}" view="${moduleMap.subNodesTemplate}"  editable="${moduleMap.editable}" />
                        </p>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
    <div class='clear'></div>
</div>