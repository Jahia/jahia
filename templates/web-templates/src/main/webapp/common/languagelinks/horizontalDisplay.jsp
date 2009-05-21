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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../declarations.jspf" %>
<ul>
    <c:forEach var="langCode" items="${requestScope.languageCodes}">
        <c:set var="selected" value=""/>
        <c:if test="${requestScope.currentRequest.locale == langCode}"><c:set var="selected" value="selected"/></c:if>
        <li class="${selected}"><c:choose><c:when test="${requestScope.linkDisplay == 'flag'}"><ui:displayLanguageFlag
                languageCode="${langCode}"
                title="Switch to ${langCode}"
                titleKey="switchTo"
                onLanguageSwitch="${requestScope.onLanguageSwitch}"
                redirectCssClassName="${requestScope.redirectCssClassName}"/></c:when><c:otherwise>
            <ui:displayLanguageSwitchLink languageCode="${langCode}"
                                          title="Switch to ${langCode}"
                                          titleKey="switchTo"
                                          linkKind="${requestScope.linkDisplay}"
                                          onLanguageSwitch="${requestScope.onLanguageSwitch}"
                                          redirectCssClassName="${requestScope.redirectCssClassName}"/>
        </c:otherwise></c:choose><c:if test="${requestScope.displayLanguageState == true}"><ui:displayLanguageState
                languageCode="${langCode}"/></c:if></li>
    </c:forEach>
</ul>
