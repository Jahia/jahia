<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../common/declarations.jspf" %>

<script type="text/javascript"><!--
function toggleSearch(searchType) {
if (document.getElementById(searchType === 'pages' ? 'searchTypePages' : 'searchTypeFiles').style.display == 'none') {
document.getElementById(searchType === 'pages' ? 'searchTypePages' : 'searchTypeFiles').style.display = 'block';
document.getElementById(searchType === 'pages' ? 'searchTypeFiles' : 'searchTypePages').style.display = 'none';
} else {
document.getElementById(searchType === 'pages' ? 'searchTypePages' : 'searchTypeFiles').style.display = 'none';
}
return false;
}
--></script>

<!-- start content -->
<!-- Change id to modify positioning :  #position1=areaB/mainArea/areaA  #position2=areaB/mainArea #position3=mainArea/areaA   position4=mainArea only #position5=50%areaB/50% mainArea -->
<div id="position2">
    <div class="spacer">
        <template:include page="common/breadcrumb.jsp"/>
            <h2><fmt:message key="search.results.title"/></h2>
            <s:results>
                <div class="resultsList">
                <h3><fmt:message key="search.results.found">
                        <fmt:param value="${count}"/>
                    </fmt:message></h3>
                <template:include page="/modules/search/advancedSearchForm.jsp"/>
                <c:set var="itemsPerPage" value="${functions:default(param['src_itemsPerPage'], '10')}"/>
                <pg:pager maxPageItems="${itemsPerPage}" url="${jahia.page.url}" export="currentPageNumber=pageNumber">
                    <c:forEach var="aParam" items="${paramValues}">
                        <c:if test="${not fn:startsWith(aParam.key, 'pager.')}"><pg:param name="${aParam.key}"/></c:if>
                    </c:forEach>
                    <c:if test="${count > 0}">
                        <ol start="${itemsPerPage * (currentPageNumber - 1) + 1}">
                            <s:resultIterator>
                                <pg:item>
                                    <li>
                                        <h4><a <c:if test="${hit.typeFile}">class="${hit.iconType}"</c:if> href="${hit.link}">${fn:escapeXml(hit.title)}</a></h4>

                                        <div class="resultslistDesc">${hit.summary}</div>
                                        <div class="resultsListFileType">${hit.contentType}</div>
                                        <div class="resultsListDateModified"><fmt:formatDate value="${hit.lastModified}"
                                                                                             pattern="dd.MM.yyyy HH:mm"/></div>
                                        <c:if test="${hit.typeFile}">
                                            <div class="resultsListSize">${hit.sizeKb}k</div>
                                        </c:if>
                                    </li>
                                </pg:item>
                            </s:resultIterator>
                        </ol>
                    </c:if>
                    <c:if test="${count == 0}">
                        <h4><fmt:message key="search.results.no.results"/></h4>
                    </c:if>
                    </div>
                    <pg:index export="itemCount">
                        <div class="box4-container box4-style2 box4-newsActions"><!--start box 4 style2-->
                            <div class="box4-topleft"></div>
                            <div class="box4-topright"></div>


                                    <div class="pagination"><!--start pagination-->
                                        <div class="paginationNavigation">
                                            <pg:prev ifnull="true">
                                                <c:if test="${not empty pageUrl}"><a
                                                        href="${pageUrl}"><strong><fmt:message
                                                        key="search.results.pagination.previous"/></strong></a></c:if>
                                                <c:if test="${empty pageUrl}"><span><strong><fmt:message
                                                        key="search.results.pagination.previous"/></strong></span></c:if>
                                            </pg:prev>
                                            <pg:pages>
                                                <c:if test="${pageNumber != currentPageNumber}"><a
                                                        href="${pageUrl}">${pageNumber}</a></c:if>
                                                <c:if test="${pageNumber == currentPageNumber}"><span>${pageNumber}</span></c:if>
                                            </pg:pages>
                                            <pg:next ifnull="true">
                                                <c:if test="${not empty pageUrl}"><a
                                                        href="${pageUrl}"><strong><fmt:message
                                                        key="search.results.pagination.next"/></strong></a></c:if>
                                                <c:if test="${empty pageUrl}"><span><strong><fmt:message
                                                        key="search.results.pagination.next""/></strong></span></c:if>
                                            </pg:next>
                                        </div>
                                        <div class="paginationPosition"><fmt:message
                                                key="search.results.pagination.results">
                                            <fmt:param value="${itemsPerPage * (currentPageNumber - 1) + 1}"/>
                                            <fmt:param value="${itemsPerPage * currentPageNumber < itemCount ? itemsPerPage * currentPageNumber : itemCount}"/>
                                            <fmt:param value="${itemCount}"/>
                                            </fmt:message></div>
                                    </div>
                                    <!--stop pagination-->
                            <div class="box4-bottomleft"></div>
                            <div class="box4-bottomright"></div>
                            <div class="box4-shadow-bottomleft"></div>
                            <div class="box4-shadow-bottomright"></div>
                        </div>
                        <!--stop box 4 style2-->
                    </pg:index>
                </pg:pager>
            </s:results>
    </div>
</div>
<!--stopContent-->