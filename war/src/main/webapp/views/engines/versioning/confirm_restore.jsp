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
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.*" %>
<%@ page import="org.jahia.views.engines.*" %>
<%@ page import="org.jahia.views.engines.versioning.ContentVersioningViewHelper" %>
<%@ page import="org.jahia.views.engines.versioning.ContentVersioningViewHelper" %>

<%@include file="/views/engines/common/taglibs.jsp" %>

<jsp:useBean id="confirmRestoreNav" class="java.lang.String" scope="request"/>

<%
  final ContentVersioningViewHelper versViewHelper = (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

  String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
  String engineView = (String)request.getAttribute("engineView");

  Map engineMap = (Map)request.getAttribute("jahia_session_engineMap");
  final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

  String theScreen = (String)engineMap.get("screen");

  String restoreExactLabel = (String)request.getAttribute("contentVersioning.restore_exact");

  String versionNumber = "";
  if ( versViewHelper.isRestoringContainer() && versViewHelper.getRevisionEntrySet() != null ){
    versionNumber = "v." + versViewHelper.getRevisionEntrySet().getVersionNumber();
  }


%>
<!-- versioning/confirm_restore.jsp (start) -->
<%@include file="common-javascript.inc" %>
<script language="javascript">
<!--

function sendFormApply()
{
  sendForm('restoreApply','');
}

function sendFormSave()
{
  sendForm('restoreSave','');
}

//-->
</script>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../../../jsp/jahia/engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringPage}">
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.PageVersioning.label"/>
          </c:if>
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringContainer}">
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.ContainerVersioning.label"/>
          </c:if>
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringContainerList}">
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.ContainerListVersioning.label"/>
          </c:if>
          <% if ( versViewHelper.isRestoringPage() ){ %>
            &nbsp;-&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.stepThreeOfThree" defaultValue="Step 3 of 3" /> : <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.confirmation" />
          <% } %>
          <% if ( versViewHelper.isRestoringContainer() ){ %>
            &nbsp;-&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.stepTwoOfTwo" defaultValue="Step 2 of 2" /> : <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.confirmation" />
          <% } %>
        </div>
      </div>
      <div class="content-body padded">
        <br />
        <b>&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.clickOnOkOrApplyToRestore" />.</b><br />
        <br />
        <span class="errorbold"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.warning" />&nbsp;:&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.stagingContentWillBeOverriden" /></span><br />
        <br />
      </div>
      <div class="head">
        <div class="object-title">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreOptionsSummary" />
        </div>
      </div>
      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
        <tr>
          <th class="text" align="left" valign="top"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreDate" defaultValue="Restore date" />&nbsp;:&nbsp;</th>
          <td class="text" align="left" valign="top"><bean:write name="contentVersioning.full_restore_date" /><% if ( !"".equals(versionNumber) ){ %>&nbsp;[<%=versionNumber%>]&nbsp;<% } %></td>
        </tr>
        <% if ( versViewHelper.isRestoringPage() ){ %>
        <tr>
          <th class="text" align="left" valign="top"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.exactRestore" defaultValue="Exact restore" />&nbsp;:&nbsp;</th>
          <td class="text" align="left" valign="top"><% if ("yes".equals(restoreExactLabel)){%><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.yes.label" /><%}else{%><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.no.label" /><%}%></td>
        </tr>
        <% } %>
        <% if ( versViewHelper.isRestoringContainer() ){ %>
        <tr>
          <th class="text" align="left" valign="top"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.languageToRestore" defaultValue="Languages to restore" />&nbsp;:&nbsp;</th>
          <td class="text" align="left" valign="top">
            <%
            Iterator languageIterator = versViewHelper.getLanguagesSettings().iterator();
            Locale languageLocale = null;
            while (languageIterator.hasNext()) {
                languageLocale = (Locale)languageIterator.next();
                String alt = languageLocale.getDisplayLanguage(elh.getCurrentLocale()) + " [" + languageLocale.getDisplayName() + "]";
            %>
            <input type="checkbox" name="languageToRestore" value="<%=languageLocale.toString()%>" <% if (versViewHelper.getLanguagesToRestore().contains(languageLocale)){%>checked<%}%>>
            <internal:displayLanguageFlag code="<%=languageLocale.getLanguage()%>" alt="<%=alt%>" />&nbsp;&nbsp;
            <% } %>
            </td>
          </tr>
        <% } %>
        <tr>
          <th class="text" align="left" valign="top"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreMode" defaultValue="Restore mode" />&nbsp;:&nbsp;</th>
          <td class="text" align="left" valign="top">
            <select name="restoreMode" class="input">
               <option value="<%=String.valueOf(ContentVersioningViewHelper.RESTORE_CONTENT)%>" <% if (versViewHelper.getRestoreMode() == ContentVersioningViewHelper.RESTORE_CONTENT){%>checked<%}%>><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreContentOnly" defaultValue="Restore Content only" /></option>
               <%-- 
               // temporary disable all other options except 'content only'
               <option value="<%=String.valueOf(ContentVersioningViewHelper.RESTORE_CONTENT_AND_METADATA)%>" <% if (versViewHelper.getRestoreMode() == ContentVersioningViewHelper.RESTORE_CONTENT_AND_METADATA){%>checked<%}%>><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.restoreContentAndMetadata" defaultValue="Restore Content and metadata" /></option>
               --%>
            </select>
          </td>
        </tr>
      </table>
    </div>
  </div>
</div>
<!-- versioning/confirm_restore.jsp (end) -->