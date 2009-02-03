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
    <%@ include file="../../../../jsp/jahia/engines/tools.inc" %>
    <div class="content">
        <div id="editor" class="mainPanel">
        <h4 class="versioningIcon">
          <internal:engineResourceBundle
            resourceName="org.jahia.engines.include.actionSelector.PageVersioning.label"/>
        </h4>

<h5><internal:engineResourceBundle resourceName="org.jahia.engines.version.stepThreeOfThree" /> : <internal:engineResourceBundle resourceName="org.jahia.engines.version.confirmation" /></h5>

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
            <b><internal:engineResourceBundle resourceName="org.jahia.engines.version.clickOnOkOrApplyToRestore" />.</b>
          </td>
        </tr>
        <tr>
          <td class="text" align="left" colspan="2">
            <span class="text2"><b><internal:engineResourceBundle resourceName="org.jahia.engines.version.warning" />&nbsp;:&nbsp;<internal:engineResourceBundle resourceName="org.jahia.engines.version.stagingContentWillBeOverriden" /></b></span>
            <br><br><br>
            <b><internal:engineResourceBundle resourceName="org.jahia.engines.version.restoreOptionsSummary" /> : </b>
            <br><br>
          </td>
        </tr>
        <tr>
          <td>&nbsp;&nbsp;</td>
          <td class="text" align="left">
            <li><internal:engineResourceBundle resourceName="org.jahia.engines.version.restoreDate" />:&nbsp;<bean:write name="contentVersioning.full_restore_date" />
          </td>
        </tr>
        <tr>
          <td>&nbsp;&nbsp;</td>
          <td class="text" align="left">
            <li><internal:engineResourceBundle resourceName="org.jahia.engines.version.exactRestore" defaultValue="Exact restore" /> : <% if ("yes".equals(restoreExactLabel)){%><internal:engineResourceBundle resourceName="org.jahia.engines.yes.label" /><%}else{%><internal:engineResourceBundle resourceName="org.jahia.engines.no.label" /><%}%>
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
                            <internal:engineResourceBundle resourceName="org.jahia.engines.version.backToStep"/> 2 :
                        <internal:engineResourceBundle resourceName="org.jahia.engines.version.selectingPagesToRestore"/>.</a>
                    <br><br>
                </div>
                </div>
                <br/>&nbsp;
                <div class="previousStep">
                <div class="button" style="font-weight: bold;">
                    <a href="javascript:sendForm('showOperationChoices','')"><<
                            <internal:engineResourceBundle resourceName="org.jahia.engines.version.backToStep"/> 1 :
                        <internal:engineResourceBundle resourceName="org.jahia.engines.version.selectAnotherTask"/>.</a>
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