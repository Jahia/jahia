<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="uiComponents" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="display" required="true" type="java.lang.String"%>
<%@ attribute name="linkDisplay" required="false" rtexprvalue="true" %>
<%@ attribute name="onLanguageSwitch" required="false" rtexprvalue="true" %>
<%@ attribute name="redirectToHomePageStyle" required="false" rtexprvalue="true" %>
<%@ attribute name="order" required="false" rtexprvalue="true" %>
<%@ attribute name="activeLanguagesOnly" required="false" rtexprvalue="true" %>
<%@ attribute name="cssClassName" required="false" rtexprvalue="true" %>
<%@ attribute name="redirectCssClassName" required="false" rtexprvalue="true" %>
<%@ attribute name="rootPage" type="org.jahia.services.content.JCRNodeWrapper" required="false" rtexprvalue="true" %>


<uiComponents:initLangBarAttributes order="${order}"/>

<c:if test='${display != null}'>
    <c:if test='${fn:toLowerCase(display) == "comboBox"}'>
        <!-- combo box display -->
        <div class="languageSwitching_comboBox_display">
            <select name="languageSwitchParam"
                    onchange="document.location.href=(this.options[this.selectedIndex].value)">
                <c:forEach var="langCode" items="${requestScope.languageCodes}">
                    <uiComponents:displayLanguageSwitchLink languageCode="${langCode}"
                                                  linkKind="${linkDisplay}"
                                                  var="linkValue"
                                                  urlVar="urlValue"
                                                  titleKey="switchTo"
                                                  title="Switch to ${langCode}"
                                                  onLanguageSwitch="${onLanguageSwitch}"
                                                  display="false"
                                                  rootPage="${rootPage}"/>
                    <option class="${requestScope.linkDisplay}"
                            <c:if test='${empty urlValue}'>
                                selected="selected"
                                <c:set var="currentLang" scope="request" value="${langCode}"/>
                            </c:if>
                            value="${urlValue}">
                        <c:out value="${linkValue}"/>
                    </option>
                </c:forEach>
            </select>

        </div>
    </c:if>

    <c:if test='${fn:toLowerCase(display) == "vertical"}'>
        <!-- vertical display -->
        <div class="languageSwitching_vertical_display">
            <ul>
                <c:forEach var="langCode" items="${requestScope.languageCodes}">
                    <li class="${linkDisplay}">
                        <c:choose>
                            <c:when test="${fn:toLowerCase(linkDisplay) == 'flag'}">
                                <uiComponents:displayLanguageFlag languageCode="${langCode}"
                                                        title="Switch to ${langCode}"
                                                        titleKey="switchTo"
                                                        onLanguageSwitch="${onLanguageSwitch}"
                                                        redirectCssClassName="${redirectCssClassName}"/>
                            </c:when>
                            <c:otherwise>
                                <uiComponents:displayLanguageSwitchLink languageCode="${langCode}"
                                                              linkKind="${linkDisplay}"
                                                              title="Switch to ${langCode}"
                                                              titleKey="switchTo"
                                                              onLanguageSwitch="${onLanguageSwitch}"
                                                              redirectCssClassName="${redirectCssClassName}"/>
                            </c:otherwise>
                        </c:choose>

                    </li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <c:if test='${fn:toLowerCase(display) == "horizontal"}'>
        <!--  horizontal display -->
        <div class="languageSwitching_horizontal_display">
            <ul>
                <c:forEach var="langCode" items="${requestScope.languageCodes}">
                    <li class="${requestScope.linkDisplay}">
                        <c:choose>
                            <c:when test="${fn:toLowerCase(linkDisplay) == 'flag'}">
                                <uiComponents:displayLanguageFlag languageCode="${langCode}"
                                                        title="Switch to ${langCode}"
                                                        titleKey="switchTo"
                                                        onLanguageSwitch="${onLanguageSwitch}"
                                                        redirectCssClassName="${redirectCssClassName}"/>
                            </c:when>
                            <c:otherwise>
                                <uiComponents:displayLanguageSwitchLink languageCode="${langCode}"
                                                              title="Switch to ${langCode}"
                                                              titleKey="switchTo"
                                                              linkKind="${linkDisplay}"
                                                              onLanguageSwitch="${onLanguageSwitch}"
                                                              redirectCssClassName="${redirectCssClassName}"/>
                            </c:otherwise>
                        </c:choose>

                    </li>
                </c:forEach>
            </ul>
        </div>
    </c:if>
</c:if>