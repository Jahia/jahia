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
        <jcr:nodeType ntname="jmix:test">
            <jcr:properties>
                <jcr:propertyLabel/>:&nbsp;<s:documentProperty documentType="${type.name}" name="${propertyDefinition.name}" /><br/>
            </jcr:properties>
        </jcr:nodeType>
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

