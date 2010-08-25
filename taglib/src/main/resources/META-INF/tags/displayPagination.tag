<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search" %>
<%@ attribute name="nbItemsList" required="false" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item value with."  %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<c:if test="${not empty moduleMap.paginationActive and moduleMap.totalSize > 0 and moduleMap.nbPages > 1}">
    <c:set var="searchUrl" value="${url.current}.ajax"/>
    <c:if test="${not empty currentResource.moduleParams.displaySearchParams}">
        <c:set var="searchUrl"><search:searchUrl/></c:set>
    </c:if>
    <c:url value="${searchUrl}" context="/" var="basePaginationUrl"/>
    <c:set target="${moduleMap}" property="basePaginationUrl" value="${basePaginationUrl}"/>
    <div class="pagination"><!--start pagination-->

        <div class="paginationPosition"><span>Page ${currentPage} of ${moduleMap.nbPages} (${moduleMap.totalSize} results)</span>
        </div>
        <div class="paginationNavigation">
            <label for="pageSizeSelector">Nb of items:</label>
            <select id="pageSizeSelector"
                    onchange="jreplace('${currentNode.UUID}','${basePaginationUrl}',{begin:${moduleMap.begin},pagesize:$('#pageSizeSelector').val()},'${currentResource.moduleParams.callback}')">
                <c:if test="${empty moduleMap.nbItemsList}">
                    <c:set target="${moduleMap}" property="nbItemsList" value="5,10,25,50,100"/>
                </c:if>
                <c:forTokens items="${moduleMap.nbItemsList}" delims="," var="opt">
                    <option value="${opt}" <c:if test="${moduleMap.pageSize eq opt}">selected="true" </c:if>>${opt}</option>
                </c:forTokens>
            </select>
            &nbsp;
            <c:if test="${moduleMap.currentPage>1}">
                <a class="previousLink"
                   onclick="jreplace('${currentNode.UUID}','${moduleMap.basePaginationUrl}',{begin:${ (moduleMap.currentPage-2) * moduleMap.pageSize },end:${ (moduleMap.currentPage-1)*moduleMap.pageSize-1},pagesize:${moduleMap.pageSize}},'${currentResource.moduleParams.callback}')">Previous</a>
            </c:if>
            <c:forEach begin="1" end="${moduleMap.nbPages}" var="i">
                <c:if test="${i != moduleMap.currentPage}">
                    <span><a class="paginationPageUrl"
                             onclick="jreplace('${currentNode.UUID}','${moduleMap.basePaginationUrl}',{begin:${ (i-1) * moduleMap.pageSize },end:${ i*moduleMap.pageSize-1},pagesize:${moduleMap.pageSize}},'${currentResource.moduleParams.callback}')"> ${ i }</a></span>
                </c:if>
                <c:if test="${i == moduleMap.currentPage}">
                    <span class="currentPage">${ i }</span>
                </c:if>
            </c:forEach>

            <c:if test="${moduleMap.currentPage<moduleMap.nbPages}">
                <a class="nextLink"
                   onclick="jreplace('${currentNode.UUID}','${moduleMap.basePaginationUrl}',{begin:${ moduleMap.currentPage * moduleMap.pageSize },end:${ (moduleMap.currentPage+1)*moduleMap.pageSize-1},pagesize:${moduleMap.pageSize}},'${currentResource.moduleParams.callback}')">Next</a>
            </c:if>
        </div>

        <div class="clear"></div>
    </div>
    <c:remove var="listTemplate"/>
    <!--stop pagination-->
</c:if>