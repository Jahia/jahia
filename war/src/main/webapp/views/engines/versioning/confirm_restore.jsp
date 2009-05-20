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
    <%@ include file="../../../engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringPage}">
            <fmt:message key="org.jahia.engines.include.actionSelector.PageVersioning.label"/>
          </c:if>
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringContainer}">
            <fmt:message key="org.jahia.engines.include.actionSelector.ContainerVersioning.label"/>
          </c:if>
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringContainerList}">
            <fmt:message key="org.jahia.engines.include.actionSelector.ContainerListVersioning.label"/>
          </c:if>
          <% if ( versViewHelper.isRestoringPage() ){ %>
            &nbsp;-&nbsp;<fmt:message key="org.jahia.engines.version.stepThreeOfThree"/>
          <% } %>
          <% if ( versViewHelper.isRestoringContainer() ){ %>
            &nbsp;-&nbsp;<fmt:message key="org.jahia.engines.version.stepTwoOfTwo"/>
          <% } %>
        </div>
      </div>
      <div class="content-body padded">
        <br />
        <b>&nbsp;<fmt:message key="org.jahia.engines.version.clickOnOkOrApplyToRestore" />.</b><br />
        <br />
        <span class="errorbold"><fmt:message key="org.jahia.engines.version.warning" />&nbsp;:&nbsp;<fmt:message key="org.jahia.engines.version.stagingContentWillBeOverriden" /></span><br />
        <br />
      </div>
      <div class="head">
        <div class="object-title">
          <fmt:message key="org.jahia.engines.version.restoreOptionsSummary" />
        </div>
      </div>
      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
        <tr>
          <th class="text" align="left" valign="top"><fmt:message key="org.jahia.engines.version.restoreDate"/>&nbsp;:&nbsp;</th>
          <td class="text" align="left" valign="top"><bean:write name="contentVersioning.full_restore_date" /><% if ( !"".equals(versionNumber) ){ %>&nbsp;[<%=versionNumber%>]&nbsp;<% } %></td>
        </tr>
        <% if ( versViewHelper.isRestoringPage() ){ %>
        <tr>
          <th class="text" align="left" valign="top"><fmt:message key="org.jahia.engines.version.exactRestore"/>&nbsp;:&nbsp;</th>
          <td class="text" align="left" valign="top"><% if ("yes".equals(restoreExactLabel)){%><fmt:message key="org.jahia.engines.yes.label" /><%}else{%><fmt:message key="org.jahia.engines.no.label" /><%}%></td>
        </tr>
        <% } %>
        <% if ( versViewHelper.isRestoringContainer() ){ %>
        <tr>
          <th class="text" align="left" valign="top"><fmt:message key="org.jahia.engines.version.languageToRestore"/>&nbsp;:&nbsp;</th>
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
          <th class="text" align="left" valign="top"><fmt:message key="org.jahia.engines.version.restoreMode"/>&nbsp;:&nbsp;</th>
          <td class="text" align="left" valign="top">
            <select name="restoreMode" class="input">
               <option value="<%=String.valueOf(ContentVersioningViewHelper.RESTORE_CONTENT)%>" <% if (versViewHelper.getRestoreMode() == ContentVersioningViewHelper.RESTORE_CONTENT){%>checked<%}%>><fmt:message key="org.jahia.engines.version.restoreContentOnly"/></option>
               <%-- 
               // temporary disable all other options except 'content only'
               <option value="<%=String.valueOf(ContentVersioningViewHelper.RESTORE_CONTENT_AND_METADATA)%>" <% if (versViewHelper.getRestoreMode() == ContentVersioningViewHelper.RESTORE_CONTENT_AND_METADATA){%>checked<%}%>><fmt:message key="org.jahia.engines.version.restoreContentAndMetadata"/></option>
               --%>
            </select>
          </td>
        </tr>
      </table>
    </div>
  </div>
</div>
<!-- versioning/confirm_restore.jsp (end) -->