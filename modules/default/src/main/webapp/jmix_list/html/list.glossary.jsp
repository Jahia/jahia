<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="javascript" resources="ajaxreplace.js" />
<template:addResources type="javascript" resources="jquery.min.js"/>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="resourceReadOnly" value="${currentResource.moduleParams.readOnly}"/>
<template:include view="hidden.header"/>
<c:set var="isEmpty" value="true"/>

<c:if test="${empty param.letter}">
    <c:set value="A" var="selectedLetter"/>
</c:if>
<c:if test="${!empty param.letter}">
    <c:set var="selectedLetter" value="${param.letter}"/>
</c:if>
<c:set var="field" value="${currentNode.properties.field.string}" />
<jsp:useBean id="nodesToDisplay" class="java.util.LinkedHashMap"/>
<jsp:useBean id="lettersSet" class="java.util.LinkedHashMap"/>

<div id=${currentNode.identifier}>
    <c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}">
        <c:set var="letter" value="${fn:substring(subchild.properties[field].string,0 ,1 )}"/>
        <c:set target="${lettersSet}" property="${fn:toUpperCase(letter)}" value="${lettersSet[letter] + 1}"/>
        <c:if test="${fn:startsWith(subchild.properties[field].string,fn:toLowerCase(selectedLetter)) or fn:startsWith(subchild.properties[field].string,fn:toUpperCase(selectedLetter))}">
            <c:set target="${nodesToDisplay}" property="${subchild.identifier}" value="${subchild}"/>
            <c:set var="isEmpty" value="false"/>
        </c:if>
    </c:forEach>


    <div class="alphabeticalMenu"><!--start alphabeticalMenu-->
        <div class="alphabeticalNavigation">
            <c:forTokens items="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z" var="letter" delims=",">
                <c:url var="myUrl" value="${url.mainResource}">
                    <c:param name="letter" value="${letter}"/>
                </c:url>
                <c:choose>
                    <c:when test="${!empty lettersSet[letter]}">
                        <span><a title="${lettersSet[letter]}" class="alphabeticalLetter <c:if test='${letter eq selectedLetter}'>current</c:if>" href="${myUrl}" >${letter}</a></span>
                    </c:when>
                    <c:otherwise>
                        <span><span class="alphabeticalLetter">${letter}</span></span>
                    </c:otherwise>
                </c:choose>
            </c:forTokens>
        </div>
        <div class='clear'></div>
    </div>
    <ul>

        <c:forEach items="${nodesToDisplay}" var="subchild">
            <li><template:module node="${subchild.value}" view="${moduleMap.subNodesView}" editable="${moduleMap.editable && !resourceReadOnly}"/></li>
        </c:forEach>
        <c:if test="${not omitFormatting}"><div class="clear"></div></c:if>
        <c:if test="${moduleMap.editable and renderContext.editMode && !resourceReadOnly}">
            <li><template:module path="*"/></li>
        </c:if>
        <c:if test="${not empty moduleMap.emptyListMessage and renderContext.editMode and isEmpty}">
            <li>${moduleMap.emptyListMessage}</li>
        </c:if>
    </ul>
</div>


<template:include view="hidden.footer"/>
