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
<%@ page import = "org.jahia.views.engines.*"%>
<%@ page import="org.jahia.views.engines.versioning.revisionsdetail.actions.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@include file="/views/engines/common/taglibs.jsp" %>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<%

    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final String theScreen = (String) engineMap.get("screen");
    final String actionURL = (String) request.getAttribute("ContentVersioning.ActionURL");
    String engineUrl = actionURL;

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
                final boolean canEdit = ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission("engines.languages." +
                        tempLocale.toString(),
                        jParams.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) > 0;
                if (canEdit) localeList.add(tempLocale);
            }
        }
    }

    request.setAttribute("localeList", localeList);

    final LangLinksFactory langLinksFactory =
            new LangLinksFactory(engineUrl + "&screen=" + theScreen,
                    localeList,
                    elh.getCurrentLanguageCode(), // previous Lang
                    elh.getCurrentLanguageCode());

    final Iterator langsEnum = langLinksFactory.getLinks(); %>

<%  /*display information about langs. only if there multiple langs.*/
if (localeList.size() > 1) {%>
  <ul class="dex-langItem-wrapper">
    <% while (langsEnum.hasNext()) { %>
      <li>
        <%
        final LangLink link = (LangLink) langsEnum.next();
        final String href = "javascript:handleLanguageChange('" + link.getLanguageCode() + "');";
        if (link.getLocale().toString().equals(elh.getCurrentLanguageCode())) { %>
          <internal:displayLanguageFlag code="<%=link.getLocale().getLanguage()%>" alt="<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>"/>&nbsp;<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>
        <% } else { %>
          <internal:displayLanguageFlag href="<%=href%>" code="<%=link.getLocale().getLanguage()%>" alt="<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>"/>&nbsp;<a href="<%=href%>" title="<%=link.getLocale().getDisplayName(elh.getCurrentLocale())%>"><%=link.getLocale().getDisplayName(elh.getCurrentLocale())%></a>
        <% } %>
      </li>
    <% } %>
  </ul>
<%}%>

<input type="hidden" name="engine_lang" value="<%=elh.getCurrentLanguageCode()%>"/>
<input type="hidden" name="prev_engine_lang" value="<%=elh.getCurrentLanguageCode()%>"/>