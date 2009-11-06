<%@ page import="java.util.Collection" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:remove var="currentList" scope="request"/>
<template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false"/>
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

Page :
<%       // ugly scriptlet to remove
            int pageSize = 2;
            Collection c = (Collection)pageContext.findAttribute("currentList");
            int nbPages = c.size() / pageSize;
            for (int i = 0; i < nbPages; i++) {
                %><a href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=<%=(i*pageSize)%>&end=<%=((i+1)*pageSize-1)%>')"> <%=(i+1)%></a>&nbsp;<%
            }
%>

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