<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="wiki.css"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addWrapper name="wrapper.wiki"/>

    <div id="${currentNode.UUID}-history" class="boxwiki">
        <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">
            <div class="boxwiki-inner">
                <div class="boxwiki-inner-border"><!--start boxwiki -->
                    <form name="diff" method="get" action="${currentNode.name}.compare.html">
                        <h4 class="boxwiki-title"><fmt:message key="jnt_wiki.pageHistoryTitle"/></h4>
                        <table width="100%" class="table tablecompare " summary="Table Compare">
                            <caption class=" hidden">
                                <fmt:message key="jnt_wiki.pageHistoryTitle"/>
                            </caption>
                            <colgroup>
                                <col span="1" width="10%" class="col1"/>
                                <col span="1" width="45%" class="col2"/>
                                <col span="1" width="15%" class="col3"/>
                                <col span="1" width="30%" class="col4"/>
                            </colgroup>
                            <thead>
                            <tr>
                                <th class="center" id="Selection" scope="col"><fmt:message key="jnt_wiki.label.select"/></th>
                                <th id="Title" scope="col"><fmt:message key="jnt_wiki.label.title"/></th>
                                <th id="Author" scope="col"><fmt:message key="jnt_wiki.label.author"/></th>
                                <th id="Date" scope="col"><fmt:message key="jnt_wiki.label.date"/></th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:set var="nodes" value="${currentNode.versionHistory.allLinearFrozenNodes}"/>
                            <template:initPager pageSize="10" totalSize="${nodes.size -1 }" id="${currentNode.identifier}"/>

                            <c:forEach items="${nodes}" var="version"
                                       begin="${begin + 1}" end="${end + 1}" varStatus="status">
                                <c:choose>
                                    <c:when test="${status.count % 2 == 0}">
                                        <tr class="odd">
                                    </c:when>
                                    <c:otherwise>
                                        <tr class="even">
                                    </c:otherwise>
                                </c:choose>

                                <td class="center" headers="Selection">
                                    <input type="radio" value="${version.parent.name}" name="oldid"
                                           id="w-oldid-${version.parent.name}"/>
                                    &nbsp;
                                    <input type="radio" value="${version.parent.name}" name="diff"
                                           id="w-diff-${version.parent.name}"/>
                                </td>
                                <td headers="Title"><a href="#">${version.properties['lastComment'].string} </a></td>
                                <td headers="Author">${version.properties['jcr:lastModifiedBy'].string}</td>
                                <td headers="Date"><fmt:formatDate
                                        value="${version.properties['jcr:lastModified'].date.time}"
                                        dateStyle="short" type="both"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>

                        <div class="divButton">
                            <a class="aButton" href="javascript:document.forms['diff'].submit()"><span><fmt:message key="jnt_wiki.buttonLabel.compare"/></span></a>

                            <div class="clear"></div>
                        </div>


                        <div class="pagination"><!--start pagination-->

                            <div class="paginationPosition"><span><fmt:message key="page"/> ${currentPage} <fmt:message key="of"/> ${nbPages} - ${nodes.size -1 } <fmt:message key="results"/></span>
                            </div>
                            <div class="paginationNavigation">
                                <c:if test="${currentPage>1}">
                                    <a class="previousLink"
                                       href="javascript:replace('${currentNode.UUID}-history','${url.current}?ajaxcall=true&begin=${ (currentPage-2) * pageSize }&end=${ (currentPage-1)*pageSize-1}')"><fmt:message key="previous"/></a>
                                </c:if>
                                <c:forEach begin="1" end="${nbPages}" var="i">
                                    <c:if test="${i != currentPage}">
                                        <span><a class="paginationPageUrl"
                                                 href="javascript:replace('${currentNode.UUID}-history','${url.current}?ajaxcall=true&begin=${ (i-1) * pageSize }&end=${ i*pageSize-1}')"> ${ i }</a></span>
                                    </c:if>
                                    <c:if test="${i == currentPage}">
                                        <span class="currentPage">${ i }</span>
                                    </c:if>
                                </c:forEach>

                                <c:if test="${currentPage<nbPages}">
                                    <a class="nextLink"
                                       href="javascript:replace('${currentNode.UUID}-history','${url.current}?ajaxcall=true&begin=${ currentPage * pageSize }&end=${ (currentPage+1)*pageSize-1}')"><fmt:message key="next"/></a>
                                </c:if>
                            </div>

                            <div class="clear"></div>
                        </div>
                        <!--stop pagination-->
                    </form>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxwiki -->
