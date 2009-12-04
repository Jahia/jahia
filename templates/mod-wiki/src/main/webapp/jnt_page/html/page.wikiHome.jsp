<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addWrapper name="wikiWrapper"/>
<div id="one"><!--start tab One-->

    <jcr:sql var="pageList"
             sql="select * from [jnt:wikiPage] as page where isdescendantnode(page,['${currentNode.path}']) order by page.[j:lastModifiedDate] desc"
             limit="10"/>

    <c:if test="${pageList.nodes.size == 0}">
        No page found, create your first wiki page !
    </c:if>


    <c:if test="${pageList.nodes.size > 0}">
        <h3 class="boxtitleh3">Last modified pages</h3>

        <ul class="list4">
            <c:forEach items="${pageList.nodes}" var="page">
                <li>
                    <a href="${currentNode.name}/${page.name}.html">${page.name}</a> -
                    <em>last modified on
                    <fmt:formatDate value="${page.properties['jcr:lastModified'].date.time}" type="both"/></em>
                </li>
            </c:forEach>
        </ul>

    </c:if>

    <div>
        <form name="wikiForm"/>
        Create a new page : <input id="link" name="link" onchange="form.action='${currentNode.name}/'+form.elements.link.value+'.html'"> <input type="submit"/>
        </form>
    </div>
</div>
<!--stop grid_10-->
