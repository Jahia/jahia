<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.services.content.decorator.JCRFrozenNode" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addWrapper name="wikiWrapper"/>

<div id="three"><!--start tab two-->
    <form name="diff" method="get" action="${currentNode.name}.compare.html">
        <h4 class="title titlegrey">Comparaison des versions (ul li)</h4>
        <ul class="list3 wikicompare">
            <li class="wikiTitle"><span class="wikiselection">Selection</span>

                <span class="wikitext">Title</span>
                <span class="wikidate">Date</span> <span
                        class="wikiauthor">Author</span></li>
            <c:forEach items="${currentNode.versionHistory.allLinearFrozenNodes}" var="version" begin="1">
                <li>
                <span class="wikiselection">
                    <input type="radio" value="${version.parent.name}" name="oldid"
                           id="w-oldid-${version.parent.name}"/>
                    &nbsp;
                    <input type="radio" value="${version.parent.name}" name="diff" id="w-diff-${version.parent.name}"/>
                </span>

                    <div class="wikitext"><a href="#">${version.properties['lastComment'].string} </a></div>
                    <span class="wikidate"><fmt:formatDate value="${version.properties['jcr:lastModified'].date.time}"
                                                           dateStyle="short" type="both"/></span> <span
                        class="wikiauthor">${version.properties['jcr:lastModifiedBy'].string}</span> 
                </li>
            </c:forEach>

        </ul>
        <div class="divButton">
            <a class="aButton" href="javascript:document.forms['diff'].submit()"><span>Comparer les versions sélectionnées</span></a>

            <div class="clear"></div>
        </div>

    </form>
    <div class="clear"></div>
    <div class="pagination"><!--start pagination-->
        <div class="paginationPosition"><span>Page 2 of 2 - 450 results</span></div>
        <div class="paginationNavigation"><a href="#" class="previousLink">Previous</a> <span><a
                href="wikiPage.history.jsp#"
                class="paginationPageUrl">1</a></span>
            <span><a href="#" class="paginationPageUrl">2</a></span> <span><a
                    href=""
                    class="paginationPageUrl">3</a></span>
            <span><a href="#" class="paginationPageUrl">4</a></span> <span><a
                    href="wikiPage.history.jsp#"
                    class="paginationPageUrl">5</a></span>
            <span class="currentPage">6</span> <a href="#" class="nextLink">Next</a></div>

        <div class="clear"></div>
    </div>
    <!--stop pagination-->
</div>
<!--stop tabtwo-->
