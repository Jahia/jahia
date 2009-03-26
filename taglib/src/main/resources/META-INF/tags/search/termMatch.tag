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

<%@ tag body-content="empty" description="Renders a select control for chosing the term match type: as is, all words, exact phrase or all words." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided. [true]"
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value." %>
<%@ attribute name="selectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of match type options to be shown in the cobo box. [all_words,exact_phrase,any_word,without_words,as_is]" %>
<%@ attribute name="appearance" required="false" type="java.lang.String"
              description="Specify the way match type options will be displayed. Possible values are: select and radio. [select]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="appearance" value="${h:default(appearance, 'select')}"/>
<c:set var="formId" value='<%= findAncestorWithClass(this, (Class) request.getAttribute("org.jahia.tags.search.form.class")).toString() %>'/>
<c:set var="termIndex" value="${searchTermMatchIndexes[formId]}"/>
<c:set var="key" value="src_terms[${termIndex}].match"/>
<c:set target="${attributes}" property="name" value="${key}"/>
<c:set var="value" value="${h:default(param[key], value)}"/>
<c:if test="${display}">
    <c:set var="selectionOptions" value="${h:default(selectionOptions, 'all_words,exact_phrase,any_word,without_words,as_is')}"/>
    <c:if test="${appearance == 'radio'}">
        <div class="matchTypes">
        <c:forTokens items="${selectionOptions}" delims="," var="option" varStatus="status">    
            <span class="matchType"><input type="radio" name="${key}" id="${key}.${option}" ${value == option || status.first && empty value ? 'checked="checked"' : ''} value="${option}"/><label for="${key}.${option}"><fmt:message key="searchForm.term.match.${option}"/></label></span>
        </c:forTokens>
        </div>
    </c:if>
    <c:if test="${appearance != 'radio'}">
    <select ${h:attributes(attributes)}>
        <c:forTokens items="${selectionOptions}" delims="," var="option">    
            <option value="${option}" ${value == option ? 'selected="selected"' : ''}><fmt:message key="searchForm.term.match.${option}"/></option>
        </c:forTokens>
    </select>
    </c:if>
</c:if>
<c:if test="${!display && not empty value}"><input type="hidden" name="src_terms[${termIndex}].match" value="${value}"/></c:if>
<c:set target="${searchTermMatchIndexes}" property="${formId}" value="${searchTermMatchIndexes[formId] + 1}"/>