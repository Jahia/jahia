<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js"/>

<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent and jcr:isNodeType(boundComponent, 'jmix:list')}">
    <template:addCacheDependency node="${boundComponent}"/>

    <c:set var="pagesizeid" value="pagesize${boundComponent.identifier}"/>
    <c:choose>
        <c:when test="${not empty param[pagesizeid]}">
            <c:set var="pageSize" value="${param[pagesizeid]}"/>
        </c:when>
        <c:when test="${not empty param.src_itemsPerPage}">
            <c:set var="pageSize" value="${param.src_itemsPerPage}"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageSize" value="${currentNode.properties['pageSize'].long}"/>
        </c:otherwise>
    </c:choose>
    <jsp:useBean id="pagerLimits" class="java.util.HashMap" scope="request"/>
    <c:set var="varName">${boundComponent.identifier}_loaded</c:set>
    <c:set var="totalSize" value="${2147483647}"/>

    <c:if test="${not empty pagerLimits[varName]}">
        <c:set var="totalSize" value="${pagerLimits[varName]+1}"/>
    </c:if>
    <template:initPager totalSize="${totalSize}" pageSize="${pageSize}" id="${boundComponent.identifier}"/>

    <c:set target="${pagerLimits}" property="${boundComponent.identifier}" value="${moduleMap.end}"/>

    <c:if test="${currentNode.properties.displayPager.boolean}">

        <c:set var="beginid" value="begin${boundComponent.identifier}"/>
        <c:set var="endid" value="end${boundComponent.identifier}"/>
        <c:set var="pagesizeid" value="pagesize${boundComponent.identifier}"/>

        <c:if test="${not empty moduleMap.paginationActive and moduleMap.totalSize > 0 and moduleMap.nbPages > 0 and moduleMap.end<moduleMap.totalSize}">
            <c:set target="${moduleMap}" property="usePagination" value="true"/>
            <c:choose>
                <c:when test="${not empty moduleMap.displaySearchParams}">
                    <c:set var="searchUrl"><search:searchUrl/>&</c:set>
                    <c:url value="${searchUrl}" context="/" var="basePaginationUrl">
                        <c:if test="${not empty param}">
                            <c:forEach items="${param}" var="extraParam">
                                <c:if test="${extraParam.key ne beginid and extraParam.key ne endid and extraParam.key ne pagesizeid and !fn:startsWith(extraParam.key, 'src_')}">
                                    <c:param name="${extraParam.key}" value="${extraParam.value}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                    </c:url>
                </c:when>
                <c:otherwise>
                    <c:set var="searchUrl" value="${not empty moduleMap.pagerUrl ? moduleMap.pagerUrl : url.mainResource}${not empty moduleMap.pagerUrl ? '':'?'}"/>
                    <c:url value="${searchUrl}" var="basePaginationUrl">
                        <c:if test="${not empty param}">
                            <c:forEach items="${param}" var="extraParam">
                                <c:if test="${extraParam.key ne beginid and extraParam.key ne endid and extraParam.key ne pagesizeid}">
                                    <c:param name="${extraParam.key}" value="${extraParam.value}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                    </c:url>
                </c:otherwise>
            </c:choose>


            <c:url value="${basePaginationUrl}" context="/" var="nextUrl">
                <c:param name="${beginid}" value="0"/>
                <c:param name="${endid}" value="${moduleMap.end + moduleMap.pageSize}"/>
                <c:param name="${pagesizeid}" value="${moduleMap.pageSize}"/>
            </c:url>
            <c:if test="${not empty modeDispatcherId}">
                <c:set var="ajaxUrl" value="${functions:escapeJavaScript(fn:replace(nextUrl,'.html' ,'.html.ajax'))}"/>
                <c:set var="nextUrl" value="$('\#${modeDispatcherId}').load('${ajaxUrl}')"/>
            </c:if>
            <c:if test="${empty modeDispatcherId}">
                Infinite scroll - use ajax dispatcher
            </c:if>

            <div class="pagerLoading" style="display: none">Loading</div>

            <script type="text/javascript">
                $(document).ready(function() {
                    var docLoading = false;
                    $(window).scroll(function() {
                        if(!docLoading && $(window).scrollTop() + $(window).height() == $(document).height()) {
                            docLoading = true;
                            $('.pagerLoading').css('display','block');
                            $('\#${modeDispatcherId}').load('${ajaxUrl}');
                        }
                    });
                });
            </script>
        </c:if>
    </c:if>
</c:if>

