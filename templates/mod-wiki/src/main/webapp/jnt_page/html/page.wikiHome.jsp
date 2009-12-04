<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addWrapper name="wikiWrapper"/>
<div id="one"><!--start tab One-->

    <div class="intro wiki">
        Welcome to Jahia Wiki !


    </div>

    <jcr:sql var="pageList"
             sql="select * from [jnt:wikiPage] as page where isdescendantnode(page,['${currentNode.path}']) order by page.[j:lastModifiedDate]"
             limit="10"/>

    <c:if test="${pageList.nodes.size == 0}">
        No page found, create your first wiki page !
    </c:if>

    <c:if test="${pageList.nodes.size > 0}">
        <ul>
            <c:forEach items="${pageList.nodes}" var="page">
                <li>
                    <a href="${currentNode.name}/${page.name}.html">${page.name}</a>
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
