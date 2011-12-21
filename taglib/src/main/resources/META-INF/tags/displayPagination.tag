<%@ tag description="Display pagination after call to initPager tag." %>
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
<%@ attribute name="displayNumberOfItemsPerPage" required="false" type="java.lang.Boolean" description="Display the Number Of Items Per Page select list"  %>              
<%@ attribute name="id" required="false" type="java.lang.String"
              description="The ID of the paginated list."  %>
<%@ attribute name="nbOfPages" required="false" type="java.lang.String"
              description="The number of pagination pages to display" %>
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

<c:if test="${empty id}">
    <c:set var="id" value="${currentNode.identifier}"/>
</c:if>
<c:set var="beginid" value="begin${id}"/>
<c:set var="endid" value="end${id}"/>
<c:set var="pagesizeid" value="pagesize${id}"/>

<c:if test="${not empty moduleMap.paginationActive and moduleMap.totalSize > 0 and moduleMap.nbPages > 0}">
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
    

    <c:set target="${moduleMap}" property="basePaginationUrl" value="${basePaginationUrl}"/>
    ${extraParams}
    <div class="pagination"><!--start pagination-->

        <div class="paginationPosition"><span><fmt:message key="pagination.pageOf.withTotal"><fmt:param value="${moduleMap.currentPage}"/><fmt:param value="${moduleMap.nbPages}"/><fmt:param value="${moduleMap.totalSize}"/></fmt:message></span>
        </div>
        <div class="paginationNavigation">
					<c:if test="${empty displayNumberOfItemsPerPage || displayNumberOfItemsPerPage eq 'true'}">        
                <label for="pageSizeSelector${currentNode.identifier}"><fmt:message key="pagination.itemsPerPage"/>:</label>
                <c:url value="${basePaginationUrl}" context="/" var="selectSizeUrl">
                    <c:param name="${beginid}" value="${moduleMap.begin}"/>
                </c:url>
                <select id="pageSizeSelector${currentNode.identifier}" onchange="window.location='${fn:escapeXml(selectSizeUrl)}&${pagesizeid}='+$('#pageSizeSelector${currentNode.identifier}').val();">
                    <c:if test="${empty nbItemsList}">
                        <c:set var="nbItemsList" value="5,10,25,50,100"/>
                    </c:if>
                    <c:forTokens items="${nbItemsList}" delims="," var="opt">
                        <option value="${opt}" <c:if test="${moduleMap.pageSize eq opt}">selected="true" </c:if>>${opt}</option>
                    </c:forTokens>
                </select>
                &nbsp;
            </c:if>
            <c:if test="${moduleMap .currentPage>1}">
                <c:url value="${basePaginationUrl}" context="/" var="beginUrl">
                    <c:param name="${beginid}" value="0"/>
                    <c:param name="${endid}" value="${moduleMap.pageSize-1}"/>
                    <c:param name="${pagesizeid}" value="${moduleMap.pageSize}"/>
                </c:url>
                <a class="previousLink" href="${fn:escapeXml(beginUrl) }"><fmt:message key="pagination.begin"/></a>
                <c:url value="${basePaginationUrl}" context="/" var="previousUrl">
                    <c:param name="${beginid}" value="${(moduleMap.currentPage-2) * moduleMap.pageSize }"/>
                    <c:param name="${endid}" value="${ (moduleMap.currentPage-1)*moduleMap.pageSize-1}"/>
                    <c:param name="${pagesizeid}" value="${moduleMap.pageSize}"/>
                </c:url>
                <a class="previousLink" href="${fn:escapeXml(previousUrl) }"><fmt:message key="pagination.previous"/></a>
            </c:if>
            <c:if test="${empty nbOfPages}">
                <c:set var="nbOfPages" value="5"/>
            </c:if>
            <c:choose>
                <c:when test="${nbOfPages > 1}">
                     <c:set var="paginationBegin" value="${moduleMap.currentPage < nbOfPages ? 1 : moduleMap.currentPage - (nbOfPages - 2)}" />
                </c:when>
                <c:otherwise>
                    <c:set var="paginationBegin" value="${moduleMap.currentPage}"/>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${nbOfPages > 1}">
                     <c:set var="paginationEnd" value="${(paginationBegin + (nbOfPages-1)) > moduleMap.nbPages ? moduleMap.nbPages : (paginationBegin + (nbOfPages-1))}" />
                </c:when>
                <c:otherwise>
                    <c:set var="paginationEnd" value="${moduleMap.currentPage}"/>
                </c:otherwise>
            </c:choose>
            <c:forEach begin="${paginationBegin}" end="${paginationEnd}" var="i">
                <c:if test="${i != moduleMap.currentPage}">
                    <c:url value="${basePaginationUrl}" context="/" var="paginationPageUrl">
                        <c:param name="${beginid}" value="${ (i-1) * moduleMap.pageSize }"/>
                        <c:param name="${endid}" value="${ i*moduleMap.pageSize-1}"/>
                        <c:param name="${pagesizeid}" value="${moduleMap.pageSize}"/>
                    </c:url>
                    <span><a class="paginationPageUrl" href="${fn:escapeXml(paginationPageUrl)}"> ${ i }</a></span>
                </c:if>
                <c:if test="${i == moduleMap.currentPage}">
                    <span class="currentPage">${ i }</span>
                </c:if>
            </c:forEach>

            <c:if test="${moduleMap.currentPage<moduleMap.nbPages}">
                <c:url value="${basePaginationUrl}" context="/" var="nextUrl">
                    <c:param name="${beginid}" value="${ moduleMap.currentPage * moduleMap.pageSize }"/>
                    <c:param name="${endid}" value="${ (moduleMap.currentPage+1)*moduleMap.pageSize-1}"/>
                    <c:param name="${pagesizeid}" value="${moduleMap.pageSize}"/>
                </c:url>
                <a class="nextLink" href="${fn:escapeXml(nextUrl)}"><fmt:message key="pagination.next"/></a>
                  <c:url value="${basePaginationUrl}" context="/" var="endUrl">
                    <c:param name="${beginid}" value="${(moduleMap.nbPages-1) * moduleMap.pageSize }"/>
                    <c:param name="${endid}" value="${(moduleMap.nbPages-1) * moduleMap.pageSize +  moduleMap.pageSize}"/>
                    <c:param name="${pagesizeid}" value="${moduleMap.pageSize}"/>
                </c:url>
                <a class="nextLink" href="${fn:escapeXml(endUrl)}"><fmt:message key="pagination.end"/></a>
            </c:if>
        </div>

        <div class="clear"></div>
    </div>
    <c:set target="${moduleMap}" property="usePagination" value="false"/>
    <c:remove var="listTemplate"/>
    <!--stop pagination-->
</c:if>