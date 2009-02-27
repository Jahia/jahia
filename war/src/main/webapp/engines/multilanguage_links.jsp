<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
          <internal:displayLanguageFlag code="<%=link.getLocale().getLanguage()%>" alt="<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>"/>&nbsp;<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>
        <% } else { %>
          <internal:displayLanguageFlag href="<%=href%>" code="<%=link.getLocale().getLanguage()%>" alt="<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>"/>&nbsp;<a href="<%=href%>" title="<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>"><%=link.getLocale().getDisplayName(elh.getCurrentLocale())%></a>
        <% } %>
      </li>
    <% } %>
  </ul>
  </div>
<%}%>
<input type="hidden" name="engine_lang" value="<%=elh.getCurrentLanguageCode()%>"/>
<input type="hidden" name="prev_engine_lang" value="<%=elh.getCurrentLanguageCode()%>"/>
<!-- language switch (end) -->