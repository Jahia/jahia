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
<%@ tag body-content="empty" description="Renders selection options for the search term fields." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided. [true]"
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Comma separated list of fields to search in. [siteContent]" %>
<%@ attribute name="selectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in that are available for user selection.
              This option has effect only in case the searchInAllowSelection attribute is set to true. Possibible options: siteContent, description, fileContent, filename, keywords, title, files (a shortcut for selecting file fields at once: description, fileContent, filename, keywords, title). [siteContent,files]" %>
<%@ attribute name="appearance" required="false" type="java.lang.String"
              description="Specify the way field options will be displayed. Possible values are: checkbox and select. [checkbox]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="appearance" value="${functions:default(appearance, 'checkbox')}"/>
<c:set var="value" value="${functions:default(value, 'siteContent')}"/>
<c:set var="formId" value='<%= findAncestorWithClass(this, (Class) request.getAttribute("org.jahia.tags.search.form.class")).toString() %>'/>
<c:set var="termIndex" value="${searchTermFieldIndexes[formId]}"/>
<c:set var="selectionOptions" value="${functions:default(fn:replace(selectionOptions, ' ', ''), 'siteContent,files')}"/>
<c:if test="${display}">
    <c:if test="${appearance == 'select'}">
        <c:set var="key" value="src_terms[${termIndex}].fields.custom"/>
        <c:set target="${attributes}" property="name" value="${key}"/>
        <select ${functions:attributes(attributes)}>
            <c:forTokens items="${selectionOptions}" delims="," var="field">
                <c:set var="key" value="src_terms[${termIndex}].fields.custom"/>
                <c:set var="fieldSelected" value="}"/>
                <option value="${field}" ${param[key] == field ? 'selected="selected"' : ''}><fmt:message key="searchForm.term.searchIn.${field}"/></option>
            </c:forTokens>
        </select>
    </c:if>
    <c:if test="${appearance != 'select'}">
        <div class="searchFields"><fmt:message key="searchForm.term.searchIn"/>
    <c:forTokens items="${selectionOptions}" delims="," var="field">
        <c:set var="key" value="src_terms[${termIndex}].fields.${field}"/>
        <c:set var="fieldSelected" value="${functions:default(param[key], fn:contains(value, field))}"/>
        <input type="hidden" id="src_terms[${termIndex}].fields.${field}" name="src_terms[${termIndex}].fields.${field}" value="${fn:escapeXml(fieldSelected)}"/>
        <span class="searchField"><input type="checkbox" id="src_terms[${termIndex}].fields.${field}_view" name="src_terms[${termIndex}].fields.${field}_view" value="true" ${fieldSelected ? 'checked="checked"' : ''} onchange="document.getElementById('src_terms[${termIndex}].fields.${field}').value = this.checked;"/>&nbsp;<label for="src_terms[${termIndex}].fields.${field}_view"><fmt:message key="searchForm.term.searchIn.${field}"/></label></span>
    </c:forTokens>
        </div>
    </c:if>
</c:if>
<c:if test="${not display}">
<c:forTokens items="${value}" delims="," var="field">
    <input type="hidden" name="src_terms[${termIndex}].fields.${field}" value="true"/>
</c:forTokens>
</c:if>
<c:set target="${searchTermFieldIndexes}" property="${formId}" value="${searchTermFieldIndexes[formId] + 1}"/>