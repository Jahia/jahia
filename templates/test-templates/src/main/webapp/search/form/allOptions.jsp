<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" 
%><%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" 
%><%@taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<div class="searchform">
<s:form resultsPage="this">
    <fieldset>
        <legend>Text search</legend>
        Search term:&nbsp;<s:term searchIn="content,filename,description,documentTitle,keywords" searchInAllowSelection="true" searchInSelectionOptions="content,filename,description,documentTitle,keywords"/><br/>
        Raw query:&nbsp;<s:rawQuery/><br/>
    </fieldset>
    <fieldset>
        <legend>Author and date</legend>
	    Author:&nbsp;<s:createdBy/><br/>
	    Created:&nbsp;<s:created/><br/>
	    Last editor:&nbsp;<s:lastModifiedBy/><br/>
        Modified:&nbsp;<s:lastModified/><br/>
    </fieldset>
    <fieldset>
        <legend>More...</legend>
        Page path: <s:pagePath/><br/>
        Results per page:&nbsp;<s:itemsPerPage/><br/>
    </fieldset>
    <fieldset>
        <legend>Documents</legend>
        Document type:&nbsp;<s:documentType/><br/>
        Location:&nbsp;<s:fileLocation /><br/><br/>
        File type:&nbsp;<s:fileType/><br/>
        Categories:&nbsp;<s:documentProperty documentType="jmix:categorized" name="j:defaultCategory" />
    </fieldset>
    <fieldset>
        <legend>Document properties for 'jmix:test'</legend>
        <jcr:nodeType ntName="jmix:test" var="type"/>
            <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
                ${jcr:label(propertyDefinition)}:&nbsp;<s:documentProperty documentType="${type.name}" name="${propertyDefinition.name}" /><br/>
            </c:forEach>
    </fieldset>
    <input type="submit" name="search" value="Search"/>
</s:form>
</div>
<s:results>
    <h3>Search Results: ${count} found</h3>
    <div id="resultslist">
      <ol>
        <s:resultIterator>
        <li>
          <dl>
            <dt><a <c:if test="${hit.typeFile}">class="${hit.iconType}"</c:if> href="${hit.link}">${fn:escapeXml(hit.title)}</a></dt>
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

