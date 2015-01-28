<%@ page import="java.util.Locale, java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<c:set target="${renderContext}" property="contentType" value="application/json;charset=UTF-8"/>
<c:set var="uiLocale" value="${renderContext.UILocale}"/>
<% Locale uiLocale = (Locale) pageContext.getAttribute("uiLocale"); %>
<jsp:useBean id="languages" class="java.util.LinkedHashMap"/>
<c:forEach var="locale" items="${renderContext.liveMode ? renderContext.site.activeLiveLanguagesAsLocales : renderContext.site.languagesAsLocales}">
<%
Locale l = (Locale) pageContext.getAttribute("locale");
((Map) pageContext.getAttribute("languages")).put(l.toString(), l.getDisplayName(uiLocale));
%>
</c:forEach>
<json:array>
<c:forEach var="lang" items="${languages}">
	<json:object>
		<json:property name="key" value="${lang.key}"/>
		<json:property name="value" value="${lang.value}"/>
	</json:object>
</c:forEach>
</json:array>
