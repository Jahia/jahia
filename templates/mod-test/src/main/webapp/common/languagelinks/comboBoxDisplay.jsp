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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../declarations.jspf" %>
<div class="languageSwitching_comboBox_display">
    <select name="languageSwitchParam" onchange="document.location.href=(this.options[this.selectedIndex].value)">
        <c:forEach var="langCode" items="${requestScope.languageCodes}">
            <ui:displayLanguageSwitchLink languageCode="${langCode}"
                                                 linkKind="${requestScope.linkDisplay}"
                                                 var="linkValue"
                                                 urlVar="urlValue"
                                                 titleKey="switchTo"
                                                 title="Switch to ${langCode}"
                                                 onLanguageSwitch="${requestScope.onLanguageSwitch}"
                                                 display="false"/>
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