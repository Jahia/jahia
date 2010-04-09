<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="wiki.css"/>

<template:addWrapper name="wrapper.wiki"/>
<div id="one"><!--start tab One-->

    <jcr:sql var="pageList"
             sql="select * from [jnt:wikiPage] as page where isdescendantnode(page,['${currentNode.path}']) order by page.[j:lastModifiedDate] desc"
             limit="10"/>

    <c:if test="${pageList.nodes.size == 0}">
        <fmt:message key="jnt_wiki.noPage"/>
    </c:if>


    <c:if test="${pageList.nodes.size > 0}">
        <h3 class="boxtitleh3"><fmt:message key="jnt_wiki.lastModifiedPages"/> </h3>

        <ul class="listwiki">
            <c:forEach items="${pageList.nodes}" var="page">
                <li>
                    <a href="${currentNode.name}/${page.name}.html">${page.name}</a> -
                    <em><fmt:message key="jnt_wiki.lastModif"/>
                    <fmt:formatDate value="${page.properties['jcr:lastModified'].date.time}" type="both"/></em>
                </li>
            </c:forEach>
        </ul>

    </c:if>

    <div>
        <form name="wikiForm"/>
        <fmt:message key="jnt_wiki.createPage"/> : <input id="link" name="link" onchange="form.action='${currentNode.name}/'+form.elements.link.value+'.html'"/> <input class="button" type="submit"/>
        </form>
    </div>
</div>
<!--stop grid_10-->
