<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="uiComponents" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="display" required="true" type="java.lang.String"%>
<%@ attribute name="linkDisplay" required="false" rtexprvalue="true" %>
<%@ attribute name="flagType" required="false" rtexprvalue="true" type="java.lang.String" %><%-- flagType is render choice off the flag(plain, shadow, or nothing) --%>
<%@ attribute name="onLanguageSwitch" required="false" rtexprvalue="true" %>
<%@ attribute name="redirectToHomePageStyle" required="false" rtexprvalue="true" %>
<%@ attribute name="order" required="false" rtexprvalue="true" %>
<%@ attribute name="activeLanguagesOnly" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="cssClassName" required="false" rtexprvalue="true" %>
<%@ attribute name="redirectCssClassName" required="false" rtexprvalue="true" %>
<%@ attribute name="rootPage" type="org.jahia.services.content.JCRNodeWrapper" required="false" rtexprvalue="true" %>


<uiComponents:initLangBarAttributes order="${order}" activeLanguagesOnly="${not empty activeLanguagesOnly ? activeLanguagesOnly : false}"/>

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
                                                        flagType="${flagType}"
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
                                                        flagType="${flagType}"
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