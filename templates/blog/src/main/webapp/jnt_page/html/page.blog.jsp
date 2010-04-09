<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="blog.css"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<%--add category Filter--%>

<c:if test="${! empty param.addTag}">
    <c:choose>
    <c:when test="${empty tagFilter}">
        <c:set scope="session" var="tagFilter" value="${param.addTag}"/>
    </c:when>
    <c:otherwise>
        <c:set var="addTagR" value="$$$${param.addTag}"/>
        <c:set var="addTagL" value="${param.addTag}$$$"/>
        <c:if test="${!(fn:contains(tagFilter,addTagR) || fn:contains(tagFilter,addTagL))}">
            <c:set scope="session" var="tagFilter" value="${tagFilter}$$$${param.addTag}"/>
        </c:if>
    </c:otherwise>
    </c:choose>
</c:if>
<c:set var="param.addTag" value=""/>

<%--remove category Filter--%>
<c:if test="${!empty param.removeTag}">
    <c:set var="tagsMap" value="${fn:split(tagFilter, '$$$')}"/>
    <c:set var="tagstmp" value=""/>
    <c:forEach var="tag" items="${tagsMap}">

        <c:if test="${!(tag == param.removeTag)}">
            <c:choose>
            <c:when test="${tagstmp == ''}">
                <c:set var="tagstmp" value="${tag}"/>
            </c:when>
            <c:otherwise>
                <c:set var="tagstmp" value="${tagstmp}$$$${tag}"/>
            </c:otherwise>
            </c:choose>
        </c:if>
    </c:forEach>
    <c:set var="tagFilter" value="${tagstmp}" scope="session"/>
</c:if>

<c:if test="${param.removeAllTags == 'true'}">
    <c:set var="tagFilter" value="" scope="session"/>    
</c:if>

<template:addWrapper name="hidden.blogWrapper"/>
<div class="post" id="${currentNode.UUID}-blogContents">
    <div class="addArticle"><!--start preferences-->
    <h3><a class="addArticle"
    <c:if test="${jcr:isNodeType(currentNode, 'jnt:blogContent')}">
        href="${url.base}${currentResource.node.parent.path}.blogEdit.html"
    </c:if>
    <c:if test="${!jcr:isNodeType(currentNode, 'jnt:blogContent')}">
        href="${url.base}${currentResource.node.path}.blogEdit.html"
    </c:if>
    > <fmt:message key="jnt_blog.addNew"/></a></h3>
</div>
    
    <c:if test="${currentNode.nodes.size > 0}">

    <template:initPager pageSize="10" totalSize="${currentNode.nodes.size}" id="${currentNode.identifier}"/>
    <c:set var="tagsMap" value="${fn:split(tagFilter, '$$$')}"/>
    <c:set var="queryContraint" value="isdescendantnode(blogContent,['${currentNode.path}'])"/>
    <c:if test="${!empty(tagFilter)}">
        <c:forEach var="tag" items="${tagsMap}">
            <jcr:node var="tagNode" path="${renderContext.site.path}/tags/${tag}"/>
            <c:set var="queryContraint" value="${queryContraint} and blogContent.[j:tags] = '${tagNode.UUID}'"/>
        </c:forEach>
    </c:if>    
    <c:if test="${!empty param.textSearch}">
        <c:set var="queryContraint" value="${queryContraint} and contains(blogContent.*, '${param.textSearch}') "/>        
    </c:if>
    <%--// Apply filters--%>
        <jcr:sql var="blogList"
                 sql="
                    select * from [jnt:blogContent] as blogContent
                    where
                    ${queryContraint}
                    order by blogContent.[j:lastModifiedDate] desc
         "/>
    <c:forEach items="${blogList.nodes}" var="child" begin="${begin}" end="${end}">
        <template:module node="${child}" template="short"/>
    </c:forEach>
    <div class="pagination"><!--start pagination-->
        <c:set var="nodesList" value="${jcr:getNodes(currentNode,'jnt:blogContent')}"/>
        <div class="paginationPosition"><span>Page ${currentPage} of ${nbPages} - ${blogList.nodes.size} results</span>
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
        <template:removePager id="${currentNode.identifier}"/>
</c:if>
</div>
