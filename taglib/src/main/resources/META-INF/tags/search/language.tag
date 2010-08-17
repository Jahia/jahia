<%@ tag body-content="empty" description="Renders language selection control. By default all active languages of the site are used. This can be overridden by providing a list of languages to be displayed." %>
<%@ tag import="java.util.Locale, org.jahia.services.sites.JahiaSite, org.jahia.utils.LanguageCodeConverters" %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false"
              description="Represents a single language code to be used for search or a comma separated string of codes." %>
<%@ attribute name="valueOptions" required="false"
              description="Represents a comma separated string of language codes to be displayed in the selection list." %>
<%@ attribute name="allowAll" required="false" type="java.lang.Boolean"
              description="If set to true, we diaplys an option to search in all languages. [false]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="display" value="${functions:default(display, true)}"/>
<c:if test="${display}">
    <c:set var="value" value="${not empty value ? fn:replace(value , ' ', '') : ''}"/>
    <c:set var="value" value="${functions:default(paramValues['src_languages.values'], not empty value ? fn:split(value, ',') : null)}"/>
    <c:set var="selectedValues" value=",${not empty value ? fn:join(value, ',') : renderContext.mainResource.locale},"/>
    <c:if test="${empty valueOptions}">
        <c:set var="valueOptions" value="${fn:join(renderContext.site.activeLanguageCodes, ',')}"/>
    </c:if>
    <c:set var="allowAll" value="${not empty allowAll ? allowAll : false}"/>
    <c:set target="${attributes}" property="name" value="src_languages.values"/>
    <select ${functions:attributes(attributes)}>
   		<c:if test="${allowAll}">
   			<option value=""><fmt:message key="searchForm.any"/></option>
   		</c:if>
        <c:set var="currentLocale" value="${renderContext.mainResource.locale}"/>
        <%
        for (String lang : ((String)jspContext.getAttribute("valueOptions")).split(",")) {
            jspContext.setAttribute("selectedLang", "," + lang + ",");
            jspContext.setAttribute("langDisplayName", LanguageCodeConverters.languageCodeToLocale(lang).getDisplayName(((Locale) jspContext.getAttribute("currentLocale")))); 
            %>
            <option value="<%=lang%>" ${fn:contains(selectedValues, selectedLang) ? 'selected="selected"' : ''}><c:out
                    value="${langDisplayName}"/></option>
        <%}%>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_languages.values" value="${fn:escapeXml(value)}"/></c:if>