<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../declarations.jspf" %>
<div class="languageSwitching_horizontal_display">
    <ul>
        <c:forEach var="langCode" items="${requestScope.languageCodes}">
            <li class="${requestScope.linkDisplay}">
                <c:choose>
                    <c:when test="${requestScope.linkDisplay == 'flag'}">
                        <ui:displayLanguageFlag languageCode="${langCode}"
                                                       title="Switch to ${langCode}"
                                                       titleKey="switchTo"
                                                       onLanguageSwitch="${requestScope.onLanguageSwitch}"
                                                       redirectCssClassName="${requestScope.redirectCssClassName}"/>
                    </c:when>
                    <c:otherwise>
                        <ui:displayLanguageSwitchLink languageCode="${langCode}"
                                                             title="Switch to ${langCode}"
                                                             titleKey="switchTo"
                                                             linkKind="${requestScope.linkDisplay}"
                                                             onLanguageSwitch="${requestScope.onLanguageSwitch}"
                                                             redirectCssClassName="${requestScope.redirectCssClassName}"/>
                    </c:otherwise>
                </c:choose>
               
            </li>
        </c:forEach>
    </ul>
</div>