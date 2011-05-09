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
<template:addResources type="javascript" resources="jquery.js"/>
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
<c:if test="${currentNode.properties.useMainResource.boolean}">
    <c:set var="glossaryPath" value="${renderContext.mainResource.node.path}"/>
</c:if>
<c:if test="${!currentNode.properties.useMainResource.boolean}">
    <c:set var="glossaryPath" value="${currentNode.path}"/>
</c:if>
<c:if test="${!empty param.glossaryPath}">
    <c:set var="glossaryPath" value="${param.glossaryPath}"/>
</c:if>

<div id="${currentNode.UUID}">
    <template:addResources type="javascript" resources="ajaxreplace.js" />
    <div class="alphabeticalMenu"><!--start alphabeticalMenu-->
        <div class="alphabeticalNavigation">
            <c:forTokens items="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z" var="letter" delims=",">
                <c:url var="myUrl" value="${url.current}.ajax">
                    <c:param name="letter" value="${letter}"/>
                    <c:param name="glossaryPath" value="${glossaryPath}"/>
                </c:url>
                <span><a class="alphabeticalLetter <c:if test='${letter eq selectedLetter}'>current</c:if>" href="javascript:replace('${currentNode.UUID}','${myUrl}')" >${letter}</a></span>
            </c:forTokens>
        </div>
        <div class='clear'></div>
    </div>
    <h3>${selectedLetter}</h3>
    <ul>
        <c:if test="${!empty currentNode.properties.field.string}">
        <jcr:sql var="list"
                 sql="select * from [jnt:content] as content  where
              (content.['${currentNode.properties.field.string}'] like '${fn:toLowerCase(selectedLetter)}%' or
              content.['${currentNode.properties.field.string}'] like '${fn:toUpperCase(selectedLetter)}%') and
               isdescendantnode(content, ['${glossaryPath}'])
               order by content.['${currentNode.properties.field.string}']"/>

        <c:forEach items="${list.nodes}" var="subchild">
            <li>
                <template:module node="${subchild}" view="${currentNode.properties['j:subNodesView'].string}"  editable="${moduleMap.editable}" />
            </li>
        </c:forEach>
        </c:if>
    </ul>
</div>
<div class='clear'></div>
