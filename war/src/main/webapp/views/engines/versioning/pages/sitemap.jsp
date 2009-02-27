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

<%@ page language="java" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.sites.JahiaSite" %>
<%@ page import="org.jahia.services.sites.SiteLanguageSettings" %>
<%@ page import="org.jahia.views.engines.JahiaEngineCommonData" %>
<%@ page import="org.jahia.views.engines.JahiaEngineViewHelper" %>
<%@ page import="org.jahia.engines.calendar.*" %>
<%@ page import="org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.resourcebundle.JahiaResourceBundle"%>

<jsp:useBean id="URL" class="java.lang.String" scope="request"/>

<%@ include file="/views/engines/common/taglibs.jsp" %>

<%
    String actionURL = (String) request.getAttribute("ContentVersioning.ActionURL");
    final PagesVersioningViewHelper pagesVersViewHelper =
            (PagesVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

    final JahiaEngineCommonData engineCommonData =
            (JahiaEngineCommonData) request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
    final ProcessingContext jParams = engineCommonData.getParamBean();
    final String engineView = (String) request.getAttribute("engineView");

    final String restoreDateEventParam = (String) request.getAttribute("restore_dateEventParam");
    final Set selectedPages = pagesVersViewHelper.getSelectedPages();

    final JahiaSite jahiaSite = jParams.getSite();
    final List languageSettings = jahiaSite.getLanguageSettings();
    // Remove unactivated languages.
    Iterator languageEnum = languageSettings.iterator();
    final List languageToRemove = new ArrayList();
    while (languageEnum.hasNext()) {
        final SiteLanguageSettings siteLanguageSettings = (SiteLanguageSettings) languageEnum.next();
        if (!siteLanguageSettings.isActivated()) {
            languageToRemove.add(siteLanguageSettings);
        }
    }
    languageSettings.removeAll(languageToRemove);

    final String displayParam = "display";

    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    final String theScreen = (String) engineMap.get("screen");

    final int now = ServicesRegistry.getInstance().getJahiaVersionService().getCurrentVersionID();

    CalendarHandler calHandler = pagesVersViewHelper.getRestoreDateCalendar();
%>
<!-- versioning/pages/sitemap.jsp (start) -->
<%@ include file="common-javascript.inc" %>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../../../../engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <% if (pagesVersViewHelper.getOperationType() == 1) { %>
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.UndoStagingWizard" defaultValue="Undo Staging Wizard"/>
          <% } else if (pagesVersViewHelper.getOperationType() == 2) { %>
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreArchivedContent" defaultValue="Restore old version Wizard"/>
          <% } else { %>
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.ContentUndeleteWizard" defaultValue="Content Undelete Wizard"/>
          <% } %>
          &nbsp;-&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.stepTwoOfThree"/>
        </div>
      </div>
      <% if (pagesVersViewHelper.getOperationType() == 1) { %>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <th align="left" valign="top" rowspan="3" width="70">
              <span class="errorbold">
                <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.warning"/>:&nbsp;
              </span>
            </th>
            <td align="left" valign="top">
              a)&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.undoStagingApplyToAllLanguages"/>!
            </td>
          </tr>
          <tr>

            <td align="left" valign="top">
              b)&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.pagesThatExistOnlyInStaging"/>.
            </td>
          </tr>
          <tr>
            <td align="left" valign="top">
              c)&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources"  resourceName="org.jahia.engines.version.previouslyDeletedPages"/>.
            </td>
          </tr>
        </table>

      <% } %>
      <% if (pagesVersViewHelper.getOperationType() == 2) { %>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <th align="left" valign="top" width="100">
              <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreDate"/> :
            </th>
            <td>
              <%
              request.setAttribute("calendarHandler", calHandler);
              String calURL = URL+"calendar/calendar.jsp";
              calURL = calURL.substring(request.getContextPath().length());
              %>
              <jsp:include page="<%=calURL%>" flush="true"/>
              <script type="text/javascript">
              <!--
              // override calendar default
              function onCalCloseHandler<%=calHandler.getIdentifier()%>(calendar) {
                  sendForm('showSiteMap');
              }

              function onCalResetHandler<%=calHandler.getIdentifier()%>() {
                  sendForm('showSiteMap');
              }
              -->
              </script>
              <br/>
              <a href="javascript:sendForm('showRevisionsList')"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.chooseTheDateFromRevisionsList"/>.</a><br>
            </td>
          </tr>
          <tr>
            <th align="left" valign="top">
              <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreOptions"/> :
            </th>
            <td>
              <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td valign="top">
                    <input type="radio" name="exact_restore" value="yes" <% if ( pagesVersViewHelper.exactRestore() ){%> checked<%}%> >
                  </td>
                  <td style="padding-top: 5px;">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.exactRestore"/> :<br />
                    <div style="padding:10px 0 0 25px;">
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.exactRestoreDescription"/>.
                    </div>
                  </td>
                </tr>
                <tr>
                  <td valign="top">
                    <input type="radio" name="exact_restore" value="no"<% if ( !pagesVersViewHelper.exactRestore() ){%> checked<%}%>>
                  </td>
                  <td style="padding-top: 5px;">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.partialRestore"/> :<br />
                    <div style="padding:10px 0 0 25px;">
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.partialRestoreDescription"/>.
                    </div>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      <% } %>
      <div class="head">
        <div class="object-title">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.pagesSelection"/>
        </div>
      </div>
      <% if (pagesVersViewHelper.getOperationType() == 2) { %>
        <p>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restorePageSelectionWarning" defaultValue="Pages that exist only in Staging or that do not exist at the selected date (<span style='color:Blue'>blue pages</span>) are not selectable. You could try to choose a precise revision from the revision list: "/>
          <a href="javascript:sendForm('showRevisionsList')"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.chooseTheDateFromRevisionsList"/>.</a>
        </p>
      <% } %>
      <!--<p><span class="error">N.B:&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.lockedPages"/></span></p>-->
      <%
      // Restore specific
      final int restoreVersionID = (int) (pagesVersViewHelper.getRestoreDateCalendar().getDateLong().longValue() / 1000);
      final String title = JahiaResourceBundle.getEngineResource("org.jahia.engines.pages.PageProperties_Engine.pageTitle.label", jParams, jParams.getLocale());
      final String entryPoint = jParams.getContentPage().getObjectKey().toString();
      %>
    </div>
  </div>
</div>

<script type="text/javascript">
<!--
function selectSubTree(startNodeID, operation, language) {
  var elem = document.getElementById(startNodeID);
  if (!elem.disabled && operation == 'select') {
    elem.checked = true;
  } else {
    elem.checked = false;
  }

  // handle childs selection
  var tokens = elem.name.split('_');
  // checkbox_ContentPage_10_1_fr
  var startPageID = tokens[2];

  for (var i = 0; i < (ComplexTreeProperties.COUNTER); i++) {
    var checkboxElem = document.getElementById("checkbox_" + i);
    var inputName = checkboxElem.name;
    if ((inputName.indexOf('checkbox_', 0) != -1)) {
      // it's a page node input
      var tokens2 = inputName.split('_');
      // checkbox_ContentPage_10_1_fr
      var parentID = tokens2[3];
      var lang = tokens2[4];
      if (startPageID == parentID && lang == language && startNodeID != ("checkbox_" + i)) {
        selectSubTree("checkbox_" + i, operation, language);
      }
    }
  }
}
//-->
</script>
<!-- versioning/pages/sitemap.jsp (end) -->