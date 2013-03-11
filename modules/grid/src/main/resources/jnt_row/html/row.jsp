<%@ include file="../../common/declarations.jspf" %>
<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="960.css" />
<template:addResources type="javascript" resources="jquery.min.js"/>

<jcr:nodeProperty node="${currentNode}" name="column" var="column"/>
<c:choose>
    <c:when test="${column.string == '1col16'}">
        <c:set target="${colMap}" property="col1" value="16"/>
    </c:when>
    <c:when test="${column.string == '2col412'}">
        <c:set target="${colMap}" property="col2" value="4"/>
        <c:set target="${colMap}" property="col1" value="12"/>
    </c:when>
    <c:when test="${column.string == '2col124'}">
        <c:set target="${colMap}" property="col1" value="12"/>
        <c:set target="${colMap}" property="col2" value="4"/>
    </c:when>
    <c:when test="${column.string == '2col511'}">
        <c:set target="${colMap}" property="col1" value="5"/>
        <c:set target="${colMap}" property="col2" value="11"/>
    </c:when>
    <c:when test="${column.string == '2col115'}">
        <c:set target="${colMap}" property="col1" value="11"/>
        <c:set target="${colMap}" property="col2" value="5"/>
    </c:when>
    <c:when test="${column.string == '2col610'}">
        <c:set target="${colMap}" property="col2" value="6"/>
        <c:set target="${colMap}" property="col1" value="10"/>
    </c:when>
    <c:when test="${column.string == '2col106'}">
        <c:set target="${colMap}" property="col1" value="10"/>
        <c:set target="${colMap}" property="col2" value="6"/>
    </c:when>
    <c:when test="${column.string == '2col88'}">
        <c:set target="${colMap}" property="col1" value="8"/>
        <c:set target="${colMap}" property="col2" value="8"/>
    </c:when>
    <c:when test="${column.string == '3col448'}">
        <c:set target="${colMap}" property="col3" value="4"/>
        <c:set target="${colMap}" property="col2" value="4"/>
        <c:set target="${colMap}" property="col1" value="8"/>
    </c:when>
    <c:when test="${column.string == '3col466'}">
        <c:set target="${colMap}" property="col3" value="4"/>
        <c:set target="${colMap}" property="col2" value="6"/>
        <c:set target="${colMap}" property="col1" value="6"/>
    </c:when>
    <c:when test="${column.string == '3col484'}">
        <c:set target="${colMap}" property="col3" value="4"/>
        <c:set target="${colMap}" property="col1" value="8"/>
        <c:set target="${colMap}" property="col2" value="4"/>
    </c:when>
    <c:when test="${column.string == '3col664'}">
        <c:set target="${colMap}" property="col1" value="6"/>
        <c:set target="${colMap}" property="col2" value="6"/>
        <c:set target="${colMap}" property="col3" value="4"/>
    </c:when>
    <c:when test="${column.string == '3col844'}">
        <c:set target="${colMap}" property="col1" value="8"/>
        <c:set target="${colMap}" property="col2" value="4"/>
        <c:set target="${colMap}" property="col3" value="4"/>
    </c:when>
    <c:when test="${column.string == '4col4444'}">
        <c:set target="${colMap}" property="col1" value="4"/>
        <c:set target="${colMap}" property="col2" value="4"/>
        <c:set target="${colMap}" property="col3" value="4"/>
        <c:set target="${colMap}" property="col4" value="4"/>
    </c:when>
    <c:otherwise>
        <c:set target="${colMap}" property="col1" value="10"/>
        <c:set target="${colMap}" property="col2" value="6"/>
    </c:otherwise>
</c:choose>
<div <c:if test="${editableModule and renderContext.editModeConfigName eq 'studiomode'}">
    <c:url var="background" value="${url.currentModule}/img/960_16_10_10.png"/>
    style="background-color: #FFFFFF;background-image: url('${background}');background-repeat: repeat-y;"
</c:if> class="container_16">
    <%--<c:if test="${renderContext.editModeConfigName eq 'studiovisualmode'}">--%>
        <%--<div class="grid_16">${jcr:label(currentNode.primaryNodeType,currentResource.locale)} ${currentNode.name} : ${column.string}</div>--%>
    <%--</c:if>--%>
    <c:forEach items="${colMap}" var="col" varStatus="count">
        <!--start grid_${col.value}-->
        <div class='grid_${col.value}'>
            <%--<c:if test="${renderContext.editModeConfigName eq 'studiovisualmode'}">--%>
            <%--<div style="border: 1px dashed #999; padding: 5px; position:relative;" class='grid_${fn:replace(currentNode.identifier,'-','_')}' id='grid_${fn:replace(currentNode.identifier,'-','_')}_${count.count}'>--%>
                <%--<span>Size : ${col.value}</span>--%>
                <%--</c:if>--%>
                <template:area path="${currentNode.name}-${col.key}" areaAsSubNode="true"/>
                <c:if test="${pageScope['org.jahia.emptyArea']}">
                    &nbsp;&nbsp;
                </c:if>
                <%--<c:if test="${renderContext.editModeConfigName eq 'studiovisualmode'}">--%>
            <%--</div>--%>
            <%--</c:if>--%>
            <div class='clear'></div>
        </div>
        <!--stop grid_${col.value}-->
    </c:forEach>
    <div class='clear'></div>
</div>

<%--<c:if test="${renderContext.editModeConfigName eq 'studiovisualmode'}">--%>
    <%--<script type="text/javascript">--%>
        <%--onGWTFrameLoad(function () {--%>
            <%--maxHeight = 0;--%>
            <%--$('.grid_${fn:replace(currentNode.identifier,'-','_')}').each(function () {--%>
                <%--maxHeight = $(this).height() > maxHeight ? $(this).height() : maxHeight--%>
            <%--})--%>
            <%--$('.grid_${fn:replace(currentNode.identifier,'-','_')}').each(function () {--%>
                <%--$(this).height(maxHeight)--%>
            <%--})--%>
        <%--});--%>
    <%--</script>--%>
<%--</c:if>--%>