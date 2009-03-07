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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.services.pages.JahiaPage" %>
<%@ page import="org.jahia.services.pages.JahiaPageDefinition" %>
<%@ page import="org.jahia.services.sites.SiteLanguageSettings" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

    final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + ".fieldForm");

    final JahiaPage thePage = (JahiaPage) engineMap.get("thePage");
    final String engineUrl = (String) engineMap.get("engineUrl");

    final String pageTitle = (String) engineMap.get("dataPageTitle");
    final String pageURLKey = (String) engineMap.get("dataPageURLKey");
    final int pageTemplateID = ((Integer) engineMap.get("dataPageTemplateID")).intValue();

    final String logForm = (String) engineMap.get("logForm");
    final String theScreen = (String) engineMap.get("screen");

    final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("metadata") ||
            theScreen.equals("rightsMgmt") || theScreen.equals("timeBasedPublishing") ||
            theScreen.equals("ctneditview_rights"));
    request.setAttribute("showEditMenu", Boolean.valueOf(showEditMenu));

    boolean displayURLKeyInput = true;
    if (ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission(LockPrerequisites.URLKEY,
            jParams.getUser(), JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) <= 0) {
        displayURLKeyInput = false;
    }
%>
<div id="header">
  <h1>Jahia</h1>
  <h2 class="page"><fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageSettings.label"/></h2>
  <jsp:include page="../navigation.jsp" flush="true" />
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <div class="dex-TabBar">
            <jsp:include page="../menuBar.jsp" flush="true" />
          </div>
        </td>
        <td style="vertical-align: top;" align="right" nowrap="nowrap">
          <jsp:include page="../multilanguage_links.jsp" flush="true" />
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%" colspan="2">
        <% if (theScreen.equals("edit")) { %>
          <%
          final List templateList = (List) engineMap.get("templateList");
          final Iterator templateListEnum = templateList.iterator();
          %>
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
              <%@ include file="../menu.inc" %>
              <div id="content" class="fit w2">
                <div class="head">
                   <div class="object-title"> <fmt:message key="org.jahia.page.options.label"/></div>
                </div>
                <logic:present name="engineMessages">
                  <p class="errorbold"><fmt:message key="org.jahia.engines.shared.BigText_Field.error.label"/>: </p>
                  <ul>
                    <logic:iterate name="engineMessages" property="messages" id="curMessage">
                      <li class="error"><internal:message name="curMessage"/></li>
                    </logic:iterate>
                  </ul>
                </logic:present>
                <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %>
                <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                  <tr>
                    <th width="120">
                      <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageTitle.label"/>:
                    </th>
                    <td class="t4">
                      <%=pageTitle%>
                    </td>
                  </tr>

                  <% if (templateListEnum.hasNext()) { %>
                  <tr>
                    <th width="120">
                      <fmt:message key="org.jahia.engines.template.label"/>:
                    </th>
                    <td>
                      <%
                      while (templateListEnum.hasNext()) {
                        final JahiaPageDefinition theTemplate = (JahiaPageDefinition) templateListEnum.next();
                      %>
                        <% if (theTemplate.getID() == pageTemplateID) { %>
                          <%=theTemplate.getName()%>
                          <%  break;
                        }
                      } %>
                    </td>
                  </tr>
                  <% } %>

                  <% if (displayURLKeyInput) { %>
                  <tr>
                    <th width="120">
                      <%
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
                      if (localeList.size() > 1) {
                      %>
                      <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageURLKeyShared2lignes.label"/>
                      <% } else { %>
                      <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageURLKey.label"/>
                      <% } %>
                    </th>
                    <td>
                      <%=pageURLKey%>
                    </td>
                  </tr>
                  <% } %>
                </table>
                <% } else { %>
                <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                  <tr>
                    <th width="150">
                      <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageTitle.label"/>
                    </th>
                    <td >
                      <input type="text" name="pageTitle" value="<%=pageTitle%>"/>
                    </td>
                  </tr>
                  <% if (templateListEnum.hasNext()) { %>
                    <tr>
                      <th>
                        <fmt:message key="org.jahia.engines.template.label"/>
                      </th>
                      <td>
                        <select name="pageTemplate">
                          <%
                          while (templateListEnum.hasNext()) {
                            final JahiaPageDefinition theTemplate = (JahiaPageDefinition) templateListEnum.next();
                            pageContext.setAttribute("pageTemplate", theTemplate);
                          %>
                            <option value="<%=theTemplate.getID()%>" <% if (theTemplate.getID() == pageTemplateID) { %> selected="selected" <% } %> title="<fmt:message key='${pageTemplate.description}'/>"><fmt:message key="${pageTemplate.displayName}"/></option>
                          <%}%>
                        </select>
                      </td>
                    </tr>
                  <% } %>
                  <% if (displayURLKeyInput) { %>
                    <tr>
                      <th>
                        <%
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
                          if (localeList.size() > 1) {
                        %>
                          <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageURLKeyShared2lignes.label"/>
                        <% } else { %>
                          <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageURLKey.label"/>
                        <% } %>
                      </th>
                      <td>
                        <input type="text" name="pageURLKey" value="<%=pageURLKey%>"/>
                      </td>
                    </tr>
                  <% } %>
                </table>
                <% } %>
              </div>
            </div>
          </div>
        <% } else if (theScreen.equals("metadata")) { %>
          <jsp:include page="../containeredit/containeredit.jsp" flush="true"/>
        <% } else if (theScreen.equals("logs")) {
            if (logForm != null) { %>
              <%=logForm%>
            <% } else { %>
              <div class="menuwrapper">
                <%@ include file="../tools.inc" %>
                <div class="content">
                  <div id="editor">
                      <p class="error"><fmt:message key="org.jahia.engines.noLogs.label"/></p>
                  </div>
                </div>
                <div class="clearing">&nbsp;</div>
              </div>
            <%}
          } else if (! theScreen.equals("notools") && ! theScreen.equals("edit")) { %>
            <%=fieldForm%>
          <% } %>
        </td>
      </tr>
    </tbody>
  </table>
  <!-- Buttons -->
  <jsp:include page="../buttons.jsp" flush="true" />
  <!-- End Buttons -->
</div>