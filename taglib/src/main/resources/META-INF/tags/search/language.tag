<%-- 
/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
--%>
<%@ tag body-content="empty"
        description="Renders language selection control. By default all active languages of the site are used. This can be overridden by providing a list of languages to be disaplyed." %>
<%@ tag import="java.util.Locale, org.jahia.services.sites.JahiaSite, org.jahia.utils.LanguageCodeConverters" %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false"
              description="Represents a single language code to be used for search or a comma separated string of codes." %>
<%@ attribute name="valueOptions" required="false"
              description="Represents a comma separated string of language codes to be displayed in the selection list." %>
<c:if test="${display}">
    <c:set var="value" value="${not empty value ? value : ''}"/>
    <c:set var="value" value="${h:default(paramValues['src_languages.values'], fn:split(value, ','))}"/>
    <c:set var="selectedValues" value=",${fn:join(value, ',')},"/>
    <c:if test="${empty valueOptions}">
        <c:set var="valueOptions" value="${fn:join(jahia.site.activeLanguageCodes, ',')}"/>
    </c:if>
    <c:set target="${attributes}" property="name" value="src_languages.values"/>
    <select ${h:attributes(attributes)}>
        <option value="" ${selectedValues == ',,' ? 'selected="selected"' : ''}><utility:resourceBundle resourceBundle="JahiaEnginesResources"
                resourceName="org.jahia.engines.search.currentLanguage" defaultValue="---"/></option>
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