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