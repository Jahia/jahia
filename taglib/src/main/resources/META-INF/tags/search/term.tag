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
              This option has effect only in case the searchInAllowSelection attribute is set to true. [siteContent,files]" %>
<%@ attribute name="applyFilterOnWildcardTerm" required="false" type="java.lang.Boolean"
              description="As Lucene omits running analysers on terms with wildcards, control whether Jahia should run the ISOLatin1AccentFilter on wildcard terms instead. [true]" %>              
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search"%>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="formId" value='<%= request.getAttribute("org.jahia.tags.search.form.formId") %>'/>
<c:set var="termIndex" value="${searchTermIndexes[formId]}"/>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set var="key" value="src_terms[${termIndex}].term"/>
<c:set target="${attributes}" property="name" value="${key}"/>
<c:set var="value" value="${functions:default(param[key], value)}"/>
<c:set var="key" value="src_terms[${termIndex}].fields"/>
<c:set var="searchIn" value="${functions:default(param[key], searchIn)}"/>
<c:set var="searchInAllowSelection" value="${functions:default(searchInAllowSelection, false)}"/>
<c:set var="searchInSelectionOptions" value="${functions:default(searchInSelectionOptions, 'siteContent,files')}"/>
<input ${functions:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<input type="hidden" name="src_terms[${termIndex}].applyFilter" value="${functions:default(applyFilterOnWildcardTerm, true)}"/>
<c:if test="${not empty match && match != 'as_is'}">
    <c:set var="key" value="src_terms[${termIndex}].match"/>
    <input type="hidden" name="${key}" value="${fn:escapeXml(functions:default(param[key], match))}"/>
</c:if>
<c:if test="${searchInAllowSelection || not empty searchIn}">
    <search:termFields value="${searchIn}" selectionOptions="${searchInSelectionOptions}" display="${searchInAllowSelection}"/>
</c:if>
<c:set target="${searchTermIndexes}" property="${formId}" value="${searchTermIndexes[formId] + 1}"/>