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
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" 
%><%@taglib prefix="s" uri="http://www.jahia.org/tags/search" 
%><%@taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<div class="searchform">
<s:form searchFor="files" resultsPage="this">
    <fieldset>
        <legend>Text search</legend>
        Search term:&nbsp;<s:term/><br/>
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