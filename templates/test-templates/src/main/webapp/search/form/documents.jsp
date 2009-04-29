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
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" 
%><%@taglib prefix="s" uri="http://www.jahia.org/tags/search" 
%><%@taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<div class="searchform">
<s:form searchFor="files" resultsPage="this">
    <fieldset>
        <legend>Text search</legend>
        Search term:&nbsp;<s:term searchIn="content,filename,description,documentTitle,keywords" searchInAllowSelection="true" searchInSelectionOptions="content,filename,description,documentTitle,keywords"/><br/>
    </fieldset>
    <fieldset>
        <legend>Author and date</legend>
	    Author:&nbsp;<s:createdBy/><br/>
	    Created:&nbsp;<s:created/><br/>
	    Last editor:&nbsp;<s:lastModifiedBy/><br/>
        Modified:&nbsp;<s:lastModified/><br/>
    </fieldset>
    <fieldset>
        <legend>Documents</legend>
        Document type:&nbsp;<s:documentType/><br/>
        Location:&nbsp;<s:fileLocation /><br/><br/>
        File type:&nbsp;<s:fileType/><br/>
        Categories:&nbsp;<s:documentProperty documentType="jmix:categorized" name="j:defaultCategory" />
    </fieldset>
    <input type="submit" name="search" value="Search"/>
</s:form>
</div>
<s:results>
    <h3>Search Results: ${count} found</h3>
    <c:set var="itemsPerPage" value="10"/>
    <pg:pager maxPageItems="${itemsPerPage}" url="${jahia.page.url}" export="currentPageNumber=pageNumber">
    <c:forEach var="aParam" items="${paramValues}">
       <c:if test="${not fn:startsWith(aParam.key, 'pager.')}"><pg:param name="${aParam.key}"/></c:if>
    </c:forEach>
    <div id="resultslist">
      <ol start="${itemsPerPage * (currentPageNumber - 1) + 1}">
        <s:resultIterator>
        <pg:item>
        <li>
          <dl>
            <dt><a class="${hit.iconType}" href="${hit.link}">${fn:escapeXml(hit.title)}</a></dt>
            <dd>${hit.summary}</dd>
            <dd>File format: ${hit.contentType}</dd>
            <dd>created: <fmt:formatDate value="${hit.created}" pattern="dd.MM.yyyy HH:mm"/>&nbsp;by&nbsp;${fn:escapeXml(hit.createdBy)}</dd>
            <dd>last modified: <fmt:formatDate value="${hit.lastModified}" pattern="dd.MM.yyyy HH:mm"/>&nbsp;by&nbsp;${fn:escapeXml(hit.lastModifiedBy)}</dd>
            <c:if test="${hit.typeFile}">
                <dd>size: ${hit.sizeKb}k</dd>
            </c:if>
          </dl>
        </li>
        </pg:item>
        </s:resultIterator>
      </ol>
    </div>
    <pg:index export="itemCount">
        <div class="pagination">
            <p>
                <pg:prev ifnull="true">
                    <c:if test="${not empty pageUrl}"><a href="${pageUrl}"><strong>Previous</strong></a></c:if>
                    <c:if test="${empty pageUrl}"><span><strong>Previous</strong></span></c:if>
                </pg:prev>
                <pg:pages>
                    <c:if test="${pageNumber != currentPageNumber}"><a href="${pageUrl}">${pageNumber}</a></c:if>
                    <c:if test="${pageNumber == currentPageNumber}"><span>${pageNumber}</span></c:if>
                </pg:pages>
                <pg:next ifnull="true">
                    <c:if test="${not empty pageUrl}"><a href="${pageUrl}"><strong>Next</strong></a></c:if>
                    <c:if test="${empty pageUrl}"><span><strong>Next</strong></span></c:if>
                </pg:next>
            </p>
            <h4>Results ${itemsPerPage * (currentPageNumber - 1) + 1}-${itemsPerPage * currentPageNumber < itemCount ? itemsPerPage * currentPageNumber : itemCount} of ${itemCount}
            </h4>
        </div>
    </pg:index>
    </pg:pager>
<%--     
    <div>
            <s:resultTableSettings contextId="my" icon="images/columns.gif">
                <s:resultTable/>
            </s:resultTableSettings>
    </div>
--%>
</s:results>