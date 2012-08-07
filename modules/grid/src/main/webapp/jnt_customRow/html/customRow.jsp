<%@ include file="../../common/declarations.jspf" %>
<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="960.css" />

<jcr:nodeProperty node="${currentNode}" name="customColumn" var="customColumn"/>
<c:set var="nbCols" value="0"/>
<c:set var="nbArea" value="0"/>

<c:forTokens items="${customColumn.string}" delims="," varStatus="vs" var="col">
    <c:set target="${colMap}" property="col${vs.count}" value="${col}"/>
    <c:if test="${fn:contains(col,' ')}">
        <c:forTokens items="${col}" delims=" " varStatus="vs" var="c">
            <c:if test="${vs.count eq 1}">
                <c:set var="col" value="${c}"/>
            </c:if>
        </c:forTokens>
    </c:if>
    <c:set var="nbCols" value="${nbCols + col}"/>
    <c:set var="nbAreas" value="${nbAreas + 1}"/>
</c:forTokens>
<c:set var="nbNames" value="0"/>
<c:forTokens items="${currentNode.properties.colNames.string}" delims="," varStatus="vs">

    <c:set var="nbNames" value="${nbNames + 1}"/>
</c:forTokens>

<c:if test="${!empty currentNode.properties.divID}"> <div id="${currentNode.properties.divID.string}"></c:if>
<div class="container_16">
    <c:if test="${!empty currentNode.properties.divClass}"><div class="${currentNode.properties.divClass.string}"></c:if>

    <c:if test="${editableModule}">
        <div class="grid_${nbCols}">
            <p>${jcr:label(currentNode.primaryNodeType,currentResource.locale)} ${currentNode.name} : ${column.string}</p>
            <c:if test="${nbNames != nbAreas}">
                <p><fmt:message key="label.generatedNames"/></p>
            </c:if>
        </div>
        <div class='clear'></div>
    </c:if>
    <c:set var="colNames" value="${fn:split(currentNode.properties.colNames.string, ',')}"/>

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
        <div class='grid_${column} ${colCss}'>
            <c:if test="${nbNames == nbAreas}">
                <c:forTokens items="${currentNode.properties.colNames.string}" var="colName" delims="," varStatus="vs1">
                    <c:if test="${count.count == vs1.count}">
                        <template:area path="${colName}" areaAsSubNode="true"/>
                    </c:if>
                </c:forTokens>
            </c:if>
            <c:if test="${nbNames != nbAreas}">
                <template:area path="${currentNode.name}-${col.key}" areaAsSubNode="true"/>
            </c:if>
            <c:if test="${pageScope['org.jahia.emptyArea']}">
                &nbsp;&nbsp;
            </c:if>
            <div class='clear'></div>
        </div>
        <!--stop grid_${column}-->
    </c:forEach>
    <div class='clear'></div>
    <c:if test="${!empty currentNode.properties.divClass}"></div></c:if>
</div>
<c:if test="${!empty currentNode.properties.divID}"></div></c:if>

