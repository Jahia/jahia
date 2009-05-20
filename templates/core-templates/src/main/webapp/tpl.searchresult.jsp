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
        <%@ include file="common/template-head.jspf" %>
        <utility:applicationResources/>
    </template:templateHead>
    <template:templateBody>
        <div id="header">
            <template:include page="common/header.jsp"/>
        </div>
        <div id="pagecontent">
            <div class="content2cols">
                <div id="columnA">
                     <template:include page="common/columnA.jsp"/>
                </div>
                <div id="columnB">
                    <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}"/></h2>

                    <s:results>
                        <h3>Search Results: ${count} found</h3>
                        <div id="resultslist">
                          <ol>
                            <s:resultIterator>
                            <li>
                              <dl>
                                <dt><a href="${hit.link}">${fn:escapeXml(hit.title)}</a></dt>
                                <dd>${hit.summary}</dd>
                                <dd>File format: ${hit.contentType}</dd>
                                <dd>created: <fmt:formatDate value="${hit.created}" pattern="dd.MM.yyyy HH:mm"/>&nbsp;by&nbsp;${fn:escapeXml(hit.createdBy)}</dd>
                                <dd>last modified: <fmt:formatDate value="${hit.lastModified}" pattern="dd.MM.yyyy HH:mm"/>&nbsp;by&nbsp;${fn:escapeXml(hit.lastModifiedBy)}</dd>
                                <c:if test="${hit.typeFile}">
                                    <dd>size: ${hit.sizeKb}k</dd>
                                </c:if>
                              </dl>
                            </li>
                            </s:resultIterator>
                          </ol>
                        </div>
                    </s:results>

                    <div>
                        <a class="bottomanchor" href="#pagetop"><fmt:message key="pageTop"/></a>
                    </div>
                </div>

                <br class="clear"/>
            </div>
            <!-- end of content2cols section -->
        </div>
        <!-- end of pagecontent section-->

        <div id="footer">
            <template:include page="common/footer.jsp"/>
        </div>
    </template:templateBody>
</template:template>