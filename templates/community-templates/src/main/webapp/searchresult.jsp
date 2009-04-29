<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="common/declarations.jspf" %>
<template:template>
    <template:templateHead>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="common/head_externals.jspf" %>
        <utility:applicationResources/>
        <link rel="stylesheet" href="<utility:resolvePath value='theme/${currentTheme}/css/openclose.css'/>" type="text/css" />
        <script type="text/javascript">
           function toggleSearch(searchType) {
               if (document.getElementById(searchType === 'pages' ? 'searchTypePages' : 'searchTypeFiles').style.display == 'none') {
             	    document.getElementById(searchType === 'pages' ? 'searchTypePages' : 'searchTypeFiles').style.display = 'block';
                    document.getElementById(searchType === 'pages' ? 'searchTypeFiles' : 'searchTypePages').style.display = 'none';
               } else {
              	   document.getElementById(searchType === 'pages' ? 'searchTypePages' : 'searchTypeFiles').style.display = 'none';
               }
               return false;
           }
        </script>
    </template:templateHead>
    <template:templateBody>
        <div id="bodywrapper">
            <div id="container"><!--start container-->
                <!-- Head page -->
                <template:include page="common/header.jsp"/>
            </div>
            <!--stop container-->
            <div id="container2"><!--start container2-->
                <div id="container3"><!--start container3-->
                    <div id="wrapper"><!--start wrapper-->
                        <div id="content2"><!--start content-->
                            <div class="spaceContent"><!--start spaceContent -->
                                <template:include page="common/breadcrumb.jsp"/>
                               <div class="box">
                                    <h2><fmt:message key="search.results.title"/></h2>
                                    <s:results>
                                       <div class="resultsList">
                                            <h3>
                                                <fmt:message key="search.results.found">
                                                    <fmt:param value="${count}"/>
                                                </fmt:message>
                                            </h3>
                                            <template:include page="common/advancedSearchForm.jsp"/>
                                            <c:set var="itemsPerPage" value="${functions:default(param['src_itemsPerPage'], '10')}"/>
                                            <pg:pager maxPageItems="${itemsPerPage}" url="${jahia.page.url}" export="currentPageNumber=pageNumber">
                                            <c:forEach var="aParam" items="${paramValues}">
                                               <c:if test="${not fn:startsWith(aParam.key, 'pager.')}"><pg:param name="${aParam.key}"/></c:if>
                                            </c:forEach>
                                            <c:if test="${count > 0}">
                                                <ol start="${itemsPerPage * (currentPageNumber - 1) + 1}" >
                                                    <s:resultIterator>
                                                         <pg:item>
                                                        <li>
                                                           <h4><a href="${hit.link}">${fn:escapeXml(hit.title)}</a></h4>
                                                            <div class="resultslistDesc">${hit.summary}</div>
                                                            <div class="resultsListFileType">${hit.contentType}</div>
                                                            <div class="resultsListDateModified"><fmt:formatDate value="${hit.lastModified}" pattern="dd.MM.yyyy HH:mm"/></div>
                                                            <c:if test="${hit.typeFile}"><div class="resultsListSize">${hit.sizeKb}k</div></c:if>
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
												<div class="box4-topleft"></div><div class="box4-topright"></div>
                                                    <div class="box4-text">
                                                        <div class="box4-wrapper">
                            								
                                                            <div class="pagination"><!--start pagination-->
                                                                <div class="paginationNavigation">
                                                                    <pg:prev ifnull="true">
                                                                        <c:if test="${not empty pageUrl}"><a href="${pageUrl}"><strong><fmt:message key="search.results.pagination.previous"/></strong></a></c:if>
                                                                        <c:if test="${empty pageUrl}"><span><strong><fmt:message key="search.results.pagination.previous"/></strong></span></c:if>
                                                                    </pg:prev>
                                                                    <pg:pages>
                                                                        <c:if test="${pageNumber != currentPageNumber}"><a href="${pageUrl}">${pageNumber}</a></c:if>
                                                                        <c:if test="${pageNumber == currentPageNumber}"><span>${pageNumber}</span></c:if>
                                                                    </pg:pages>
                                                                    <pg:next ifnull="true">
                                                                        <c:if test="${not empty pageUrl}"><a href="${pageUrl}"><strong><fmt:message key="search.results.pagination.next"/></strong></a></c:if>
                                                                        <c:if test="${empty pageUrl}"><span><strong><fmt:message key="search.results.pagination.next"/></strong></span></c:if>
                                                                    </pg:next>
                                                                </div>
                                                                <div class="paginationPosition"><fmt:message key="search.results.pagination.results">
                                                                    <fmt:param value="${itemsPerPage * (currentPageNumber - 1) + 1}"/>
                                                                    <fmt:param value="${itemsPerPage * currentPageNumber < itemCount ? itemsPerPage * currentPageNumber : itemCount}"/>
                                                                    <fmt:param value="${itemCount}"/>
                                                                    </fmt:message>
                                                                </div>
                                                            </div><!--stop pagination-->
                                                            
                                                          </div>
														</div>
														<div class="box4-bottomleft"></div><div class="box4-bottomright"></div>
    													<div class="box4-shadow-bottomleft"></div><div class="box4-shadow-bottomright"></div>
													</div><!--stop box 4 style2-->
                                         </pg:index>
									</pg:pager>
							</s:results>
                                    
                                </div>
                            </div><!--stop space content-->
                        </div><!--stopContent-->
                    </div><!--stop wrapper-->
                    <div id="leftInset"><!--start leftInset-->
                        <div class="space"><!--start space leftInset -->
                            <!-- left menu -->
                            <template:include page="common/leftmenu.jsp"/>
                            <!-- Search area -->
                            <template:include page="common/searchArea.jsp"/>
                        </div><!--stop space leftInset-->
                    </div><!--stop leftInset-->
                    <div class="clear"></div>
                </div><!--stop container2-->
                <!-- footer -->
                <template:include page="common/footer.jsp"/>
                <div class="clear"></div>
            </div><!--stop container3-->
        </div>
    </template:templateBody>
</template:template>