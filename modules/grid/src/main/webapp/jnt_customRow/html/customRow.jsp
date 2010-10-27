<%@ include file="../../common/declarations.jspf" %>
<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="960.css" />

<jcr:nodeProperty node="${currentNode}" name="customColumn" var="customColumn"/>
<jcr:nodeProperty node="${currentNode}" name="principalColumn" var="principalColumn"/>
<c:set var="nbCols" value="0"/>
<c:forTokens items="${customColumn.string}" delims="," varStatus="vs" var="col">
    <c:if test="${vs.count eq principalColumn.string}">
        <c:set target="${colMap}" property="colContent" value="${col}"/>
    </c:if>
    <c:if test="${!(vs.count eq principalColumn.string)}">
        <c:set target="${colMap}" property="col${vs.count}" value="${col}"/>
    </c:if>
    <c:if test="${fn:contains(col,' ')}">
        <c:forTokens items="${col}" delims=" " varStatus="vs" var="c">
            <c:if test="${vs.count eq 1}">
                <c:set var="col" value="${c}"/>
            </c:if>
        </c:forTokens>
    </c:if>
    <c:set var="nbCols" value="${nbCols + col}"/>
</c:forTokens>

<c:if test="${!empty currentNode.properties.divClass || !empty currentNode.properties.divID}">
    <c:if test='${!empty currentNode.properties.divID}'><div id="${currentNode.properties.divID.string}"></c:if>
    <div class='container container_${nbCols} <c:if test="${!empty currentNode.properties.divClass}">${currentNode.properties.divClass.string}"</c:if>'>
</c:if>
<c:if test="${editableModule}">
    <div class="grid_${nbCols}">${jcr:label(currentNode.primaryNodeType,currentResource.locale)} ${currentNode.name} : ${column.string}</div>
    <div class='clear'></div>
</c:if>

<c:forEach items="${colMap}" var="col" varStatus="count">
    <c:set var="column" value="${col.value}"/>
    <c:set var="colCss" value=""/>
    <c:if test="${fn:contains(column,' ')}">
        <c:forTokens items="${column}" delims=" " varStatus="vs" var="c">
            <c:if test="${vs.count eq 1}">
                <c:set var="column" value="${c}"/>
            </c:if>
            <c:if test="${!(vs.count eq 1)}">
                <c:set var="colCss" value="${colCss} ${c}"/>
            </c:if>
        </c:forTokens>
    </c:if>
    <!--start grid_${column}-->
    <div
            class='${colCss} grid_${column}'>
        <template:area path="${currentNode.name}-${col.key}"/>
        <div class='clear'></div>
    </div>
    <!--stop grid_${column}-->
</c:forEach>
<div class='clear'></div>
<c:if test="${!empty currentNode.properties.divClass || !empty currentNode.properties.divID}">
    </div>
    <c:if test='${!empty currentNode.properties.divID}'></div></c:if>    
</c:if>
