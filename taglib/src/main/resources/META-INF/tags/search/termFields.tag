<%@ tag body-content="empty" description="Renders selection options for the search term fields." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided. [true]"
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Comma separated list of fields to search in. [siteContent]" %>
<%@ attribute name="selectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in that are available for user selection.
              This option has effect only in case the searchInAllowSelection attribute is set to true. Possibible options: siteContent, description, fileContent, filename, keywords, title, files (a shortcut for selecting file fields at once: description, fileContent, filename, keywords, title, tags). [siteContent,files]" %>
<%@ attribute name="appearance" required="false" type="java.lang.String"
              description="Specify the way field options will be displayed. Possible values are: checkbox and select. [checkbox]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="appearance" value="${functions:default(appearance, 'checkbox')}"/>
<c:set var="value" value="${functions:default(value, 'siteContent')}"/>
<c:set var="formId" value='<%= request.getAttribute("org.jahia.tags.search.form.formId") %>'/>
<c:set var="termIndex" value="${searchTermFieldIndexes[formId]}"/>
<c:set var="selectionOptions" value="${fn:replace(selectionOptions, ' ', '')}"/>
<c:set var="selectionOptions" value="${functions:default(selectionOptions, 'siteContent,files')}"/>
<c:if test="${display}">
    <c:set var="customKey" value="src_terms[${termIndex}].fields.custom"/>
    <c:set var="pValues" value="${fn:join(paramValues[customKey], ',')}"/>    
    <c:if test="${appearance == 'select'}">
        <c:set target="${attributes}" property="name" value="${customKey}"/>
        <select ${functions:attributes(attributes)}>
            <c:forTokens items="${selectionOptions}" delims="," var="field">
                <c:set var="key" value="src_terms[${termIndex}].fields.${field}"/>
                <c:set var="fieldSelected" value="${functions:default(fn:contains(pValues, field) or param[key], fn:contains(value, field))}"/>
                <option value="${field}" ${fieldSelected ? 'selected="selected"' : ''}><fmt:message key="searchForm.term.searchIn.${field}"/></option>
            </c:forTokens>
        </select>
    </c:if>
    <c:if test="${appearance != 'select'}">
        <span class="searchFields">
    <c:forTokens items="${selectionOptions}" delims="," var="field">
        <c:set var="key" value="src_terms[${termIndex}].fields.${field}"/>
        <c:set var="fieldSelected" value="${functions:default(param[key] or fn:contains(pValues, field), fn:contains(value, field))}"/>
        <input type="hidden" id="src_terms[${termIndex}].fields.${field}" name="src_terms[${termIndex}].fields.${field}" value="${fn:escapeXml(fieldSelected)}"/>
        <span class="searchField"><input type="checkbox" id="src_terms[${termIndex}].fields.${field}_view" name="src_terms[${termIndex}].fields.${field}_view" value="true" ${fieldSelected ? 'checked="checked"' : ''} onchange="document.getElementById('src_terms[${termIndex}].fields.${field}').value = this.checked;${attributes.onchange}"/>&nbsp;<label for="src_terms[${termIndex}].fields.${field}_view"><fmt:message key="searchForm.term.searchIn.${field}"/></label></span>
    </c:forTokens>
        </span>
    </c:if>
</c:if>
<c:if test="${not display}">
<c:forTokens items="${value}" delims="," var="field">
    <input type="hidden" name="src_terms[${termIndex}].fields.${field}" value="true"/>
</c:forTokens>
</c:if>
<c:set target="${searchTermFieldIndexes}" property="${formId}" value="${searchTermFieldIndexes[formId] + 1}"/>