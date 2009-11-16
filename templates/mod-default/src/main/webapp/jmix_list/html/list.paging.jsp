<%@ page import="java.util.Collection" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:remove var="currentList" scope="request"/>
<template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false">
    <template:param name="forcedSkin" value="none" />
</template:module>

<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>

<div id="${currentNode.UUID}">

    <c:set var="step" value="2"/>

    <template:addResources type="javascript" resources="ajaxreplace.js" />

    <c:set var="begin" value="${param.begin}"/>
<c:set var="end" value="${param.end}"/>

    <c:if test="${empty begin}">
        <c:set var="begin" value="0"/>
    </c:if>
    <c:if test="${empty end}">
        <c:set var="end" value="${step - 1}"/>
    </c:if>

    <c:set var="nbPages" value="${fn:length(currentList) / 2}" />
    <c:forEach begin="0" end="${nbPages}" var="i">
        <a href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=${ i * 2 }&end=${ (i + 1)*2-1}')"> ${ i + 1}</a>&nbsp;
    </c:forEach>

<c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">
    <p>
        <template:module node="${subchild}" template="${subNodesTemplate}" editable="${editable}" >
            <c:if test="${not empty forcedSkin}">
                <template:param name="forcedSkin" value="${forcedSkin}"/>
            </c:if>
            <c:if test="${not empty renderOptions}">
                <template:param name="renderOptions" value="${renderOptions}"/>
            </c:if>
        </template:module>
    </p>
</c:forEach>
</div>