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
<%@ tag body-content="empty" description="Renders selection options for the search term fields." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided. [true]"
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Comma separated list of fields to search in. [content]" %>
<%@ attribute name="selectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in that are available for user selection.
              This option has effect only in case the searchInAllowSelection attribute is set to true. [content,metadata]" %>
<%@ attribute name="appearance" required="false" type="java.lang.String"
              description="Specify the way field options will be displayed. Possible values are: checkbox and select. [checkbox]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="appearance" value="${h:default(appearance, 'checkbox')}"/>
<c:set var="value" value="${h:default(value, 'content')}"/>
<c:set var="formId" value='<%= findAncestorWithClass(this, (Class) request.getAttribute("org.jahia.tags.search.form.class")).toString() %>'/>
<c:set var="termIndex" value="${searchTermFieldIndexes[formId]}"/>
<c:set var="selectionOptions" value="${h:default(selectionOptions, 'content,metadata')}"/>
<c:if test="${display}">
    <c:if test="${appearance == 'select'}">
        <c:set var="key" value="src_terms[${termIndex}].fields.custom"/>
        <c:set target="${attributes}" property="name" value="${key}"/>
        <select ${h:attributes(attributes)}>
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
        <c:set var="fieldSelected" value="${h:default(param[key], fn:contains(value, field))}"/>
        <input type="hidden" id="src_terms[${termIndex}].fields.${field}" name="src_terms[${termIndex}].fields.${field}" value="${fieldSelected}"/>
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