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

<%@include file="/views/engines/common/taglibs.jsp" %>

<%
  String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
  String engineView = (String)request.getAttribute("engineView");

  Map engineMap = (Map)request.getAttribute("jahia_session_engineMap");
  String theScreen = (String)engineMap.get("screen");

  String restoreExactLabel = (String)request.getAttribute("contentVersioning.restore_exact");

%>
<!-- versioning/pages/confirm_restore.jsp (start) -->
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
<div class="menuwrapper">
    <%@ include file="../../../../engines/tools.inc" %>
    <div class="content">
        <div id="editor" class="mainPanel">
        <h4 class="versioningIcon">
          <fmt:message key="org.jahia.engines.include.actionSelector.PageVersioning.label"/>
        </h4>

<h5><fmt:message key="org.jahia.engines.version.stepThreeOfThree" /> : <fmt:message key="org.jahia.engines.version.confirmation" /></h5>

<table border="0" width="100%">
<tr>
    <td align="left" class="text">
      <table border="0" width="90%">
    <tr>
      <td>&nbsp;&nbsp;</td>
      <td colspan="3" align="left" valign="top" nowrap>
        <table>
        <tr>
          <td class="text" align="left" colspan="2">
            <b><fmt:message key="org.jahia.engines.version.clickOnOkOrApplyToRestore" />.</b>
          </td>
        </tr>
        <tr>
          <td class="text" align="left" colspan="2">
            <span class="text2"><b><fmt:message key="org.jahia.engines.version.warning" />&nbsp;:&nbsp;<fmt:message key="org.jahia.engines.version.stagingContentWillBeOverriden" /></b></span>
            <br><br><br>
            <b><fmt:message key="org.jahia.engines.version.restoreOptionsSummary" /> : </b>
            <br><br>
          </td>
        </tr>
        <tr>
          <td>&nbsp;&nbsp;</td>
          <td class="text" align="left">
            <li><fmt:message key="org.jahia.engines.version.restoreDate" />:&nbsp;<bean:write name="contentVersioning.full_restore_date" />
          </td>
        </tr>
        <tr>
          <td>&nbsp;&nbsp;</td>
          <td class="text" align="left">
            <li><fmt:message key="org.jahia.engines.version.exactRestore"/> : <% if ("yes".equals(restoreExactLabel)){%><fmt:message key="org.jahia.engines.yes.label" /><%}else{%><<fmt:message key="org.jahia.engines.no.label" /><%}%>
          </td>
        </tr>
        <!--
        <tr>
          <td>&nbsp;&nbsp;</td>
          <td class="text" align="left">
            <li>Apply archived page move operations&nbsp;:&nbsp;<input class="input" type="radio" name="apply_page_move_when_restore" value="yes">yes&nbsp;<input class="input" type="radio" name="apply_page_move_when_restore" value="no" checked>no
          </td>
        </tr>
        -->
        <!--
        <tr>
          <td>&nbsp;&nbsp;</td>
          <td class="text" align="left">
            <li>Automatically activate restored pages&nbsp;:&nbsp;<input class="input" type="radio" name="activate_pages_after_restore" value="yes">yes&nbsp;<input class="input" type="radio" name="activate_pages_after_restore" value="no" checked>no
          </td>
        </tr>
        -->
        </table>
      </td>
</tr>
    <tr>
        <td colspan="3" nowrap valign="top" align="left">
            <div class="navBox">
                <div class="previousStep">
                <div class="button" style="font-weight: bold;">
                    <a href="javascript:sendForm('showSiteMap','')"><<
                            <fmt:message key="org.jahia.engines.version.backToStep"/> 2 :
                        <fmt:message key="org.jahia.engines.version.selectingPagesToRestore"/>.</a>
                    <br><br>
                </div>
                </div>
                <br/>&nbsp;
                <div class="previousStep">
                <div class="button" style="font-weight: bold;">
                    <a href="javascript:sendForm('showOperationChoices','')"><<
                            <fmt:message key="org.jahia.engines.version.backToStep"/> 1 :
                        <fmt:message key="org.jahia.engines.version.selectAnotherTask"/>.</a>
                </div>
                </div>
            </div>
        </td>
    </tr>
</table>
        </td>
</tr>
</table>

        </div>
    </div>
    <div class="clearing">&nbsp;</div>
</div>
<!-- versioning/pages/confirm_restore.jsp (end) -->