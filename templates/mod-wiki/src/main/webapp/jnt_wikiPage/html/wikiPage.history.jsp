<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addWrapper name="wikiWrapper"/>
    <div class="box">
        <div class="boxgrey boxpadding16 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border"><!--start box -->
                    <form name="diff" method="get" action="${currentNode.name}.compare.html">
                        <h4 class="title titlegrey">Comparaison des versions (table)</h4>
                        <table width="100%" class="table tableTasks " summary="Mes taches en cour en table">
                            <caption class=" hidden">
                                Comparaison des versions (table)
                            </caption>
                            <colgroup>
                                <col span="1" width="10%" class="col1"/>
                                <col span="1" width="45%" class="col2"/>
                                <col span="1" width="15%" class="col3"/>
                                <col span="1" width="30%" class="col4"/>
                            </colgroup>
                            <thead>
                            <tr>
                                <th class="center" id="Selection" scope="col">Selection</th>
                                <th id="Title" scope="col">Title</th>
                                <th id="Author" scope="col">Author</th>
                                <th id="Date" scope="col">Date</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach items="${currentNode.versionHistory.allLinearFrozenNodes}" var="version"
                                       begin="1" varStatus="status">
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
                            <a class="aButton" href="javascript:document.forms['diff'].submit()"><span>Comparer les versions sélectionnées</span></a>

                            <div class="clear"></div>
                        </div>
                        <div class="pagination"><!--start pagination-->
                            <div class="paginationPosition"><span>Page 2 of 2 - 450 results</span></div>
                            <div class="paginationNavigation"><a href="#" class="previousLink">Previous</a> <span><a
                                    href="#" class="paginationPageUrl">1</a></span> <span><a href="#"
                                                                                             class="paginationPageUrl">2</a></span>
                                <span><a href="#" class="paginationPageUrl">3</a></span> <span><a href="#"
                                                                                                  class="paginationPageUrl">4</a></span>
                                <span><a href="#" class="paginationPageUrl">5</a></span> <span
                                        class="currentPage">6</span> <a href="#" class="nextLink">Next</a></div>
                            <div class="clear"></div>
                        </div>
                        <!--stop pagination-->
                    </form>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->


</div>
<!--stop tabtwo-->
