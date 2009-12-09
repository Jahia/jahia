<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<template:addResources type="css" resources="blog.css"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>

<template:addWrapper name="blogWrapper"/>
<div class="post" id="${currentNode.UUID}-blogContents">
<c:if test="${currentNode.nodes.size > 0}">
    List of entries :
    <template:initPager pageSize="10" totalSize="${currentNode.nodes.size}"/>

    <c:forEach items="${currentNode.nodes}" var="child" begin="${begin}" end="${end}">
        <template:module node="${child}" template="short"/>
    </c:forEach>
    <div class="pagination"><!--start pagination-->
        <c:set var="nodesList" value="${jcr:getNodes(currentNode,'jnt:blogContent')}"/>
        <div class="paginationPosition"><span>Page ${currentPage} of ${nbPages} - ${nodesList.size} results</span>
        </div>
        <div class="paginationNavigation">
            <c:if test="${currentPage>1}">
                <a class="previousLink"
                   href="javascript:replace('${currentNode.UUID}-blogContents','${url.current}?ajaxcall=true&begin=${ (currentPage-2) * pageSize }&end=${ (currentPage-1)*pageSize-1}')">Previous</a>
            </c:if>
            <c:forEach begin="1" end="${nbPages}" var="i">
                <c:if test="${i != currentPage}">
                    <span><a class="paginationPageUrl"
                             href="javascript:replace('${currentNode.UUID}-blogContents','${url.current}?ajaxcall=true&begin=${ (i-1) * pageSize }&end=${ i*pageSize-1}')"> ${ i }</a></span>
                </c:if>
                <c:if test="${i == currentPage}">
                    <span class="currentPage">${ i }</span>
                </c:if>
            </c:forEach>

            <c:if test="${currentPage<nbPages}">
                <a class="nextLink"
                   href="javascript:replace('${currentNode.UUID}-blogContents','${url.current}?ajaxcall=true&begin=${ currentPage * pageSize }&end=${ (currentPage+1)*pageSize-1}')">Next</a>
            </c:if>
        </div>

        <div class="clear"></div>
    </div>
    <!--stop pagination-->
</c:if>
</div>