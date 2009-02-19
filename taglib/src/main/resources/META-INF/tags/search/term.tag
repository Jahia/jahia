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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ tag body-content="empty" description="Renders search term input control." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<c:set var="display" value="${h:default(display, true)}"/>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value." %>
<%@ attribute name="match" required="false" type="java.lang.String" description="The match type for search terms. [as_is]" %>
<%@ attribute name="searchIn" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in. [content]" %>
<%@ attribute name="searchInAllowSelection" required="false" type="java.lang.Boolean"
              description="Do we need to display search fields options to allow user selection? [false]" %>
<%@ attribute name="searchInSelectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in that are available for user selection.
              This option has effrect only in case the searchInAllowSelection attribute is set to true. [content,metadata]" %>
<c:set var="formId" value="<%=this.getParent().toString() %>"/>
<c:set var="termIndex" value="${searchTermIndexes[formId]}"/>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set var="key" value="src_terms[${termIndex}].term"/>
<c:set target="${attributes}" property="name" value="${key}"/>
<c:set var="value" value="${h:default(param[key], value)}"/>
<c:set var="key" value="src_terms[${termIndex}].fields"/>
<c:set var="searchIn" value="${h:default(param[key], h:default(searchIn, 'content'))}"/>
<c:set var="searchInAllowSelection" value="${h:default(searchInAllowSelection, false)}"/>
<c:set var="searchInSelectionOptions" value="${h:default(searchInSelectionOptions, 'content,metadata')}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${not empty match && match != 'as_is'}">
    <c:set var="key" value="src_terms[${termIndex}].match"/>
    <input type="hidden" name="${key}" value="${h:default(param[key], match)}"/>
</c:if>
<c:if test="${searchInAllowSelection}">
    <div class="searchFields"><utility:resourceBundle resourceName="searchForm.term.searchIn" defaultValue="search in:"/>
<c:forTokens items="${searchInSelectionOptions}" delims="," var="field">
    <c:set var="key" value="src_terms[${termIndex}].fields.${field}"/>
    <c:set var="fieldSelected" value="${h:default(param[key], fn:contains(searchIn, field))}"/>
    <input type="hidden" id="src_terms[${termIndex}].fields.${field}" name="src_terms[${termIndex}].fields.${field}" value="${fieldSelected}"/>
    <span class="searchField"><input type="checkbox" id="src_terms[${termIndex}].fields.${field}_view" name="src_terms[${termIndex}].fields.${field}_view" value="true" ${fieldSelected ? 'checked="checked"' : ''} onchange="document.getElementById('src_terms[${termIndex}].fields.${field}').value = this.checked;"/>&nbsp;<label for="src_terms[${termIndex}].fields.${field}_view"><utility:resourceBundle resourceName="searchForm.term.searchIn.${field}" defaultValue="${field}"/></label></span>
</c:forTokens>
    </div>
</c:if>
<c:if test="${not searchInAllowSelection}">
<c:forTokens items="${searchIn}" delims="," var="field">
    <input type="hidden" name="src_terms[${termIndex}].fields.${field}" value="true"/>
</c:forTokens>
</c:if>
<c:set target="${searchTermIndexes}" property="${formId}" value="${searchTermIndexes[formId] + 1}"/>