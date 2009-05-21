<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.pages.PageProperties_Engine" %>
<%@ page import="org.jahia.services.pages.JahiaPage" %>
<%@ page import="org.jahia.gui.HTMLToolBox" %>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String theScreen = (String) engineMap.get("screen");
    final String engineName = (String) engineMap.get("engineName");
    boolean addNew = ("true".equals(request.getParameter("addnew"))) || ("true".equals(request.getAttribute("addnew")));
    boolean refreshMainPage = ((request.getParameter("refreshMainPage") == null) ||
            "yes".equals(request.getParameter("refreshMainPage")));
    final String refreshMainPageVal;
    if (refreshMainPage) {
        refreshMainPageVal = "yes";
    } else {
        refreshMainPageVal = "no";
    }
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    String refreshOpener = "yes";
    if (engineMap.containsKey("refreshOpener")) {
        refreshOpener = (String) engineMap.get("refreshOpener");
    }
    final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
    final JahiaPage thePage = (JahiaPage) engineMap.get("thePage");
    final String urlKey = jParams.getParameter("pageURLKey");
%>

<script type="text/javascript">

    window.onunload = function () {
    }
    <% if ( !addNew ){ %>
    <% if ("close".equals(theScreen)) { %>
    window.close();
    window.opener.handleActionChange("display");
    <% } else {
            if (theScreen == null || theContainer == null ||
               !("deletecontainer".equals(engineName) && "save".equals(theScreen))) { %>
    <%  if ("categoryEdit".equals(engineName)) { %>
    window.close();
    window.opener.waitForClosePopup();
    <% } else if (PageProperties_Engine.ENGINE_NAME.equals(engineName)) { %>
    if (window.opener.name.indexOf("workflow_showReport_") > -1) {
        // We are currently returning to the workflow error report, so we do not want to refresh the
        // underlying window.
        window.close();
    } else {
        <% Locale originalLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE); %>
        CloseJahiaWindowWithUrl("<%= originalLocale != null ? jParams.composePageUrl(thePage.getID(), originalLocale.toString()) : jParams.composePageUrl(thePage.getID()) %>");
    }
    <% } else { %>
    if (window.opener.name.indexOf("workflow_showReport_") > -1) {
        // We are currently returning to the workflow error report, so we do not want to refresh the
        // underlying window.
        window.close();
    } else {
    <% if (urlKey != null && urlKey.length() != 0) {
        final Locale openerLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
        final int openerPid = Integer.parseInt(session.getAttribute(ProcessingContext.SESSION_LAST_DISPLAYED_PAGE_ID).toString());
        %>
        CloseJahiaWindowWithUrl("<%= openerLocale != null ? jParams.composePageUrl(openerPid, openerLocale.toString()) : jParams.composePageUrl(openerPid) %>");
    <% } else { %>
        CloseJahiaWindow('<%=refreshOpener%>', '<%=HTMLToolBox.drawAnchor(theContainer, true) %>');
    <% } %>
    }
    <% } %>
    <% } else { %>
    <% ContentPage currentPage = jParams.getContentPage();
             if (currentPage != null &&
               ((currentPage.hasStagingEntries() || currentPage.hasActiveEntries())
                 && !currentPage.isStagedEntryMarkedForDeletion(jParams.getLocale().toString()))) { %>
    CloseJahiaWindow('<%=refreshOpener%>');

    <% } else { %>
    <% Locale originalLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE); %>
    CloseJahiaWindowWithUrl("<%= originalLocale != null ? jParams.composePageUrl(theContainer.getPageID(), originalLocale.toString()) : jParams.composePageUrl(theContainer.getPageID()) %>");

    <%
             }
           }
         }
      } else {
        Integer parentID = (Integer) engineMap.get("containerParentID");
        if (parentID == null) parentID = new Integer(theContainer.getPageID());
        final String url = jParams.composeEngineUrl( engineName,
            "?clistid=" + theContainer.getListID() +
            "&cdefid=" + theContainer.getctndefid() +
            "&cpid=" + theContainer.getPageID() +
            "&cparentid=" + parentID.intValue() +
            "&engine_lang=" + elh.getCurrentLanguageCode() );
    %>
    saveAndAddNew('<%=url + "&refreshMainPage=" + refreshMainPageVal %>',
            "<% if (refreshMainPage){ %> yes <% } else { %> no <% } %>");
    <% } %>
</script>