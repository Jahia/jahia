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
<%@ tag body-content="empty"
        description="Renders language selection control. By default all active languages of the site are used. This can be overridden by providing a list of languages to be disaplyed." %>
<%@ tag import="java.util.Locale, org.jahia.services.sites.JahiaSite, org.jahia.utils.LanguageCodeConverters" %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false"
              description="Represents a single language code to be used for search or a comma separated string of codes." %>
<%@ attribute name="valueOptions" required="false"
              description="Represents a comma separated string of language codes to be displayed in the selection list." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="display" value="${h:default(display, true)}"/>
<c:if test="${display}">
    <c:set var="value" value="${not empty value ? value : ''}"/>
    <c:set var="value" value="${h:default(paramValues['src_languages.values'], fn:split(value, ','))}"/>
    <c:set var="selectedValues" value=",${fn:join(value, ',')},"/>
    <c:if test="${empty valueOptions}">
        <c:set var="valueOptions" value="${fn:join(jahia.site.activeLanguageCodes, ',')}"/>
    </c:if>
    <c:set target="${attributes}" property="name" value="src_languages.values"/>
    <select ${h:attributes(attributes)}>
        <option value="" ${selectedValues == ',,' ? 'selected="selected"' : ''}><fmt:message key="searchForm.currentLanguage"/></option>
        <c:set var="currentLocale" value="${jahia.processingContext.locale}"/>
        <c:forTokens items="${valueOptions}" delims="," var="lang">
            <c:set var="langCode" value="${fn:trim(lang)}"/>
            <c:set var="selectedLang" value=",${langCode},"/>
            <% jspContext.setAttribute("langDisplayName", LanguageCodeConverters.languageCodeToLocale((String) jspContext.getAttribute("langCode")).getDisplayName(((Locale) jspContext.getAttribute("currentLocale")))); %>
            <option value="${langCode}" ${fn:contains(selectedValues, selectedLang) ? 'selected="selected"' : ''}><c:out
                    value="${langDisplayName}"/></option>
        </c:forTokens>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_languages.values" value="${value}"/></c:if>