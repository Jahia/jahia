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
<%@ tag body-content="empty" description="Renders search term input control." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided. [true]"
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value." %>
<%@ attribute name="match" required="false" type="java.lang.String" description="The match type for search terms. [as_is]" %>
<%@ attribute name="searchIn" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in. [content]" %>
<%@ attribute name="searchInAllowSelection" required="false" type="java.lang.Boolean"
              description="Do we need to display search fields options to allow user selection? [false]" %>
<%@ attribute name="searchInSelectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in that are available for user selection.
              This option has effect only in case the searchInAllowSelection attribute is set to true. [content,metadata]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search"%>

<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="formId" value='<%= findAncestorWithClass(this, (Class) request.getAttribute("org.jahia.tags.search.form.class")).toString() %>'/>
<c:set var="termIndex" value="${searchTermIndexes[formId]}"/>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set var="key" value="src_terms[${termIndex}].term"/>
<c:set target="${attributes}" property="name" value="${key}"/>
<c:set var="value" value="${h:default(param[key], value)}"/>
<c:set var="key" value="src_terms[${termIndex}].fields"/>
<c:set var="searchIn" value="${h:default(param[key], searchIn)}"/>
<c:set var="searchInAllowSelection" value="${h:default(searchInAllowSelection, false)}"/>
<c:set var="searchInSelectionOptions" value="${h:default(searchInSelectionOptions, 'content,metadata')}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${not empty match && match != 'as_is'}">
    <c:set var="key" value="src_terms[${termIndex}].match"/>
    <input type="hidden" name="${key}" value="${h:default(param[key], match)}"/>
</c:if>
<c:if test="${searchInAllowSelection || not empty searchIn}">
    <s:termFields value="${searchIn}" selectionOptions="${searchInSelectionOptions}" display="${searchInAllowSelection}"/>
</c:if>
<c:set target="${searchTermIndexes}" property="${formId}" value="${searchTermIndexes[formId] + 1}"/>