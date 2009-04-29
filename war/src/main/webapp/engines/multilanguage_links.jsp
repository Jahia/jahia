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
<%@page language = "java"%>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.engines.*" %>
<%@ page import="org.jahia.services.sites.*" %>
<%@ page import="org.jahia.utils.*" %>
<%@ page import="org.jahia.services.acl.JahiaBaseACL"%>
<%@ page import="org.jahia.registries.ServicesRegistry"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
final String engineUrl = (String) engineMap.get("engineUrl");
final String theScreen = (String) engineMap.get("screen");

EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
if (elh == null) {
  elh = new EngineLanguageHelper(jParams.getLocale());
}
final List localeList = new ArrayList();
final List siteLanguageSettings = jParams.getSite().getLanguageSettings();
if (siteLanguageSettings != null) {
  for (int i = 0; i < siteLanguageSettings.size(); i++) {
    final SiteLanguageSettings curSetting = (SiteLanguageSettings) siteLanguageSettings.get(i);
    if (curSetting.isActivated()) {
      final Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(curSetting.getCode());
      final boolean canEdit = ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission("engines.languages." + tempLocale.toString(), jParams.getUser(), JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) > 0;
      if (canEdit) {
        localeList.add(tempLocale);
      }
    }
  }
}
request.setAttribute("localeList", localeList);
final LangLinksFactory langLinksFactory = new LangLinksFactory(engineUrl + "&screen=" + theScreen, localeList, elh.getCurrentLanguageCode(), elh.getCurrentLanguageCode());
final Iterator langsEnum = langLinksFactory.getLinks();

/*display information about langs. only if there multiple langs.*/
%>
<%if (localeList.size() > 1) {%>
  <!-- language switch (start) -->
  <div class="dex-langItem-wrapper">
  <ul class="dex-langItem-wrapper">
    <% while (langsEnum.hasNext()) { %>
      <li>
        <%
        final LangLink link = (LangLink) langsEnum.next();
        final String href = "javascript:handleLanguageChange('" + link.getLanguageCode() + "');";
        if (link.getLocale().toString().equals(elh.getCurrentLocale().toString())) { %>
          <internal:displayLanguageFlag code="<%=link.getLocale().toString()%>" alt="<%=link.getLocale().getDisplayName(link.getLocale())%>"/>&nbsp;<%=link.getLocale().getDisplayName(link.getLocale())%>
        <% } else { %>
          <internal:displayLanguageFlag href="<%=href%>" code="<%=link.getLocale().toString()%>" alt="<%=link.getLocale().getDisplayName(link.getLocale())%>"/>&nbsp;<a href="<%=href%>" title="<%=link.getLocale().getDisplayName(link.getLocale())%>"><%=link.getLocale().getDisplayName(link.getLocale())%></a>
        <% } %>
      </li>
    <% } %>
  </ul>
  </div>
<%}%>
<input type="hidden" name="engine_lang" value="<%=elh.getCurrentLanguageCode()%>"/>
<input type="hidden" name="prev_engine_lang" value="<%=elh.getCurrentLanguageCode()%>"/>
<!-- language switch (end) -->