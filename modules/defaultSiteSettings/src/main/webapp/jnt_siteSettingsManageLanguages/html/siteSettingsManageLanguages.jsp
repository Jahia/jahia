<%@ page import="org.jahia.services.render.Resource,
                 org.jahia.utils.LanguageCodeConverters" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="site" value="${currentNode.resolveSite}"/>
<c:set var="siteKey" value="${site.name}"/>
<c:set var="installedModules" value="${site.installedModules}"/>
<c:set var="templatePackageName" value="${site.templatePackageName}"/>

<h2>${fn:escapeXml(currentNode.displayableName)} - ${fn:escapeXml(site.displayableName)}</h2>

<form action="">
    <table style="width: 100%;" cellpadding="0" cellspacing="0" border="1">
        <thead>
        <tr>
            <th>Language</th>
            <th>Default language</th>
            <th>Mandatory</th>
            <th>Active (Edit)</th>
            <th>Active (Live)</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="lang" items="${site.languagesAsLocales}" varStatus="status">
            <tr>
                <td>${lang.displayName} (${lang})</td>
                <td><input type="radio" name="defaultLanguage" value="${lang}"
                           <c:if test="${site.defaultLanguage eq lang}">checked="checked"</c:if> /></td>
                <td><input type="checkbox" name="mandatoryLanguages" value="${lang}"
                           <c:if test="${fn:contains(site.mandatoryLanguages, lang)}">checked="checked"</c:if>/></td>
                <td><input type="checkbox" name="activeEditLanguages" value="${lang}"
                           <c:if test="${fn:contains(site.activeEditLanguages, lang)}">checked="checked"</c:if>/></td>
                <td><input type="checkbox" name="activeLiveLanguages" value="${lang}"
                           <c:if test="${fn:contains(site.activeLiveLanguages, lang)}">checked="checked"</c:if>/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <input type="checkbox" name="mixLanguages" id="mixLanguages" value="true"${site.mixLanguagesActive ? ' checked="checked"' : ''} />
    <label for="mixLanguages">&nbsp;<fmt:message
            key="org.jahia.admin.languages.ManageSiteLanguages.mixLanguages.label"/></label>
    <br>
    <input type="checkbox" name="allowsUnlistedLanguages" id="allowsUnlistedLanguages" value="true"${site.allowsUnlistedLanguages ? ' checked="checked"' : ''} />
    <label for="allowsUnlistedLanguages">&nbsp;<fmt:message
            key="org.jahia.admin.languages.ManageSiteLanguages.allowsUnlistedLanguages.label"/></label>
    <br>

    <%
        Resource r = (Resource) request.getAttribute("currentResource");
        request.setAttribute("locales", LanguageCodeConverters.getSortedLocaleList(r.getLocale()));
    %>

    <div>
        <b><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.availableLanguages.label"/></b><br/>
        <select name="language_list" id="language_list" multiple="multiple" size="10">
            <c:forEach var="locale" items="${locales}">
                <option value="${locale}">${locale.displayName}(${locale})</option>
            </c:forEach>
        </select>
    </div>
    <div>
        <a class="add-lang" href="javascript:sendForm();"
           title="<fmt:message key='org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label'/>"><fmt:message
                key="org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label"/></a>
    </div>

</form>