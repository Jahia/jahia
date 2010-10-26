<%@ include file="../../common/declarations.jspf" %>
<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="960.css" />

<jcr:nodeProperty node="${currentNode}" name="customColumn" var="customColumn"/>
<jcr:nodeProperty node="${currentNode}" name="principalColumn" var="principalColumn"/>
<c:forTokens items="${customColumn.string}" delims="," varStatus="vs" var="col">
    <c:if test="${vs.count eq principalColumn.string}">
        <c:set target="${colMap}" property="colContent" value="${col}"/>
    </c:if>
    <c:if test="${!(vs.count eq principalColumn.string)}">
        <c:set target="${colMap}" property="col${vs.count}" value="${col}"/>
    </c:if>
</c:forTokens>

<c:if test="${editableModule}">
    <div class="grid_16 alpha omega">${jcr:label(currentNode.primaryNodeType,currentResource.locale)} ${currentNode.name} : ${column.string}</div>
</c:if>
<c:forEach items="${colMap}" var="col" varStatus="count">
    <!--start grid_${col.value}-->
    <div class='grid_${col.value} <c:if test="${count.first}"> alpha</c:if> <c:if test="${count.last}"> omega</c:if>'>
        <template:area path="${currentNode.name}-${col.key}"/>
        <div class='clear'></div>
    </div>
    <!--stop grid_${col.value}-->
</c:forEach>
<div class='clear'></div>

